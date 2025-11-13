package Controller;

import Util.Alerts;
import Util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddDoctorController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField fullNameField1;

    @FXML
    private TextField ageField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private TextField contactField;

    @FXML
    private ComboBox<String> specializationComboBox;

    @FXML
    private TextField emailField;

    @FXML
    public void initialize() {
        // Populate gender combo box
        ObservableList<String> genders = FXCollections.observableArrayList("Male", "Female", "Other");
        genderComboBox.setItems(genders);

        // Define specialization list
        String[] specializationNames = {
            "Cardiology", "Neurology", "Oncology", "Pediatrics", "Orthopedics",
            "Dermatology", "Psychiatry", "Radiology", "Anesthesiology", "Endocrinology"
        };

        // Insert specializations into database if not exists
        for (String name : specializationNames) {
            Database.update("INSERT IGNORE INTO specialization (SpecializationName) VALUES (?)", name);
        }

        // Populate specialization combo box from database
        loadSpecializations();
    }

    private void loadSpecializations() {
        ObservableList<String> specializations = FXCollections.observableArrayList();

        try {
            ResultSet rs = Database.query("SELECT SpecializationName FROM specialization ORDER BY SpecializationName");
            while (rs.next()) {
                specializations.add(rs.getString("SpecializationName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        specializationComboBox.setItems(specializations);
    }

    @FXML
    private void saveDoctor(ActionEvent event) {
        // Validate required fields
        if (!validateFields()) {
            return;
        }

        try {
            // Parse full name from separate fields and capitalize properly
            String firstName = capitalizeName(fullNameField.getText().trim());
            String lastName = capitalizeName(fullNameField1.getText().trim());

            String sex = genderComboBox.getValue();
            String contact = contactField.getText().trim();
            String specializationName = specializationComboBox.getValue();
            String email = emailField.getText().trim();

            // Get specialization ID
            int specializationID = getSpecializationID(specializationName);
            if (specializationID == -1) {
                Alerts.Warning("Invalid specialization selected.");
                return;
            }

            // Insert doctor into database
            String insertQuery = "INSERT INTO doctor (FirstName, LastName, Sex, SpecializationID) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = Database.getConnection().prepareStatement(insertQuery);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, sex);
            stmt.setInt(4, specializationID);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                Alerts.Info("Doctor added successfully!");
                clearFields();
                // Close the popup
                Stage stage = (Stage) fullNameField.getScene().getWindow();
                stage.close();
            } else {
                Alerts.Warning("Failed to add doctor.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Database error: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (fullNameField.getText().trim().isEmpty()) {
            Alerts.Warning("Full name is required.");
            return false;
        }

        if (ageField.getText().trim().isEmpty()) {
            Alerts.Warning("Age is required.");
            return false;
        }

        try {
            int age = Integer.parseInt(ageField.getText().trim());
            if (age < 0 || age > 150) {
                Alerts.Warning("Please enter a valid age.");
                return false;
            }
        } catch (NumberFormatException e) {
            Alerts.Warning("Age must be a valid number.");
            return false;
        }

        if (genderComboBox.getValue() == null) {
            Alerts.Warning("Gender is required.");
            return false;
        }

        if (contactField.getText().trim().isEmpty()) {
            Alerts.Warning("Contact number is required.");
            return false;
        }

        if (specializationComboBox.getValue() == null) {
            Alerts.Warning("Specialization is required.");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            Alerts.Warning("Email is required.");
            return false;
        }

        // Basic email validation
        if (!emailField.getText().trim().contains("@")) {
            Alerts.Warning("Please enter a valid email address.");
            return false;
        }

        return true;
    }

    private int getSpecializationID(String specializationName) {
        try {
            PreparedStatement stmt = Database.getConnection().prepareStatement(
                "SELECT SpecializationID FROM specialization WHERE SpecializationName = ?"
            );
            stmt.setString(1, specializationName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("SpecializationID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void clearFields() {
        fullNameField.clear();
        fullNameField1.clear();
        ageField.clear();
        genderComboBox.setValue(null);
        contactField.clear();
        specializationComboBox.setValue(null);
        emailField.clear();
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.close();
    }

    private String capitalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
