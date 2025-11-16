package Controller;

import Util.Alerts;
import Util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.text.Font;


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

        // Define specialization list from clinicDB-NOV-15.sql
        String[] specializationNames = {
            "General Medicine", "Pediatrics", "Cardiology", "Dermatology", "Endocrinology"
        };

        // Insert specializations into database if not exists
        for (String name : specializationNames) {
            Database.update("INSERT IGNORE INTO specialization (SpecializationName) VALUES (?)", name);
        }

        // Populate specialization combo box with only the predefined specializations
        loadSpecializations();

        // Specializations are limited to predefined ones for Add Doctor

        // Generate initial email
        generateEmail();
    }

    private void loadSpecializations() {
        // Only display the predefined specializations
        ObservableList<String> specializations = FXCollections.observableArrayList(
            "General Medicine", "Pediatrics", "Cardiology", "Dermatology", "Endocrinology"
        );
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
            String insertQuery = "INSERT INTO doctor (FirstName, LastName, Sex, SpecializationID, ContactNumber) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = Database.getConnection().prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, sex);
            stmt.setInt(4, specializationID);
            stmt.setString(5, contact);

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
        generateEmail();
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

    private void generateEmail() {
        try {
            // Get the next doctor ID
            ResultSet rs = Database.query("SELECT MAX(DoctorID) AS maxID FROM doctor");
            int nextID = 1;
            if (rs != null && rs.next()) {
                nextID = rs.getInt("maxID") + 1;
            }
            String email = "dr" + String.format("%03d", nextID) + "@doctor.com";
            emailField.setText(email);
        } catch (SQLException e) {
            e.printStackTrace();
            emailField.setText("dr001@doctor.com");
        }
    }


}
