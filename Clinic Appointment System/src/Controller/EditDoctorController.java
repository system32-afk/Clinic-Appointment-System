package Controller;

import Util.Alerts;
import Util.Database;
import Util.SceneManager;
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

public class EditDoctorController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField fullNameField1;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private TextField contactField;

    @FXML
    private ComboBox<String> specializationComboBox;

    private int doctorID;

    @FXML
    public void initialize() {
        // Populate gender combo box
        ObservableList<String> genders = FXCollections.observableArrayList("Male", "Female", "Other");
        genderComboBox.setItems(genders);

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

    public void setDoctorData(int doctorID) {
        this.doctorID = doctorID;

        try {
            PreparedStatement stmt = Database.getConnection().prepareStatement(
                "SELECT d.FirstName, d.LastName, d.Sex, s.SpecializationName, d.Contact " +
                "FROM doctor d " +
                "LEFT JOIN specialization s ON d.SpecializationID = s.SpecializationID " +
                "WHERE d.DoctorID = ?"
            );
            stmt.setInt(1, doctorID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                fullNameField.setText(rs.getString("FirstName"));
                fullNameField1.setText(rs.getString("LastName"));
                genderComboBox.setValue(rs.getString("Sex"));
                contactField.setText(rs.getString("Contact"));
                specializationComboBox.setValue(rs.getString("SpecializationName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Failed to load doctor data: " + e.getMessage());
        }
    }

    @FXML
    private void updateDoctor(ActionEvent event) {
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

            // Get specialization ID
            int specializationID = getSpecializationID(specializationName);
            if (specializationID == -1) {
                Alerts.Warning("Invalid specialization selected.");
                return;
            }

            // Update doctor in database
            String updateQuery = "UPDATE doctor SET FirstName = ?, LastName = ?, Sex = ?, SpecializationID = ?, Contact = ? WHERE DoctorID = ?";
            PreparedStatement stmt = Database.getConnection().prepareStatement(updateQuery);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, sex);
            stmt.setInt(4, specializationID);
            stmt.setString(5, contact);
            stmt.setInt(6, doctorID);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                Alerts.Info("Doctor updated successfully!");
                // Close the popup
                Stage stage = (Stage) fullNameField.getScene().getWindow();
                stage.close();
            } else {
                Alerts.Warning("Failed to update doctor.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Database error: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (fullNameField.getText().trim().isEmpty()) {
            Alerts.Warning("First name is required.");
            return false;
        }

        if (fullNameField1.getText().trim().isEmpty()) {
            Alerts.Warning("Last name is required.");
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
