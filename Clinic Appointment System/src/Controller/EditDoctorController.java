package Controller;

import Util.Alerts;
import Util.Database;
import Util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
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

    @FXML
    private ComboBox<String> additionalSpecializationComboBox;

    @FXML
    private Button addSpecializationButton;

    @FXML
    private Button removeSpecializationButton;

    @FXML
    private Button setAsPrimaryButton;

    @FXML
    private ListView<String> additionalSpecializationsListView;

    private int doctorID;

    @FXML
    public void initialize() {
        // Populate gender combo box
        ObservableList<String> genders = FXCollections.observableArrayList("Male", "Female", "Other");
        genderComboBox.setItems(genders);

        // Populate specialization combo box with only the predefined specializations
        loadSpecializations();

        // Re-enable add specialization button to allow adding multiple specializations
        addSpecializationButton.setDisable(false);

        // Re-enable additional specialization combo box to allow adding multiple specializations
        additionalSpecializationComboBox.setDisable(false);
    }

    private void loadSpecializations() {
        // Only display the predefined specializations
        ObservableList<String> specializations = FXCollections.observableArrayList(
            "General Medicine", "Pediatrics", "Cardiology", "Dermatology", "Endocrinology"
        );
        specializationComboBox.setItems(specializations);
        additionalSpecializationComboBox.setItems(specializations);
    }

    public void setDoctorData(int doctorID) {
        this.doctorID = doctorID;

        try {
            PreparedStatement stmt = Database.getConnection().prepareStatement(
                "SELECT d.FirstName, d.LastName, d.Sex, s.SpecializationName, d.ContactNumber AS Contact " +
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
                String spec = rs.getString("SpecializationName");
                if (specializationComboBox.getItems().contains(spec)) {
                    specializationComboBox.setValue(spec);
                } else {
                    specializationComboBox.setValue(null); // or set to first item if preferred
                }
            }

            // Load additional specializations into the ListView
            loadAdditionalSpecializations();

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
            // Get the old specialization ID before updating
            int oldSpecializationID = getCurrentSpecializationID(doctorID);

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
            String updateQuery = "UPDATE doctor SET FirstName = ?, LastName = ?, Sex = ?, SpecializationID = ?, ContactNumber = ? WHERE DoctorID = ?";
            PreparedStatement stmt = Database.getConnection().prepareStatement(updateQuery);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, sex);
            stmt.setInt(4, specializationID);
            stmt.setString(5, contact);
            stmt.setInt(6, doctorID);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // If specialization changed, remove old primary and add new primary
                if (oldSpecializationID != specializationID) {
                    Database.update("DELETE FROM doctor_specialization WHERE DoctorID = ? AND SpecializationID = ?", doctorID, oldSpecializationID);
                    Database.update("INSERT IGNORE INTO doctor_specialization (DoctorID, SpecializationID) VALUES (?, ?)", doctorID, specializationID);
                }
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
        if (fullNameField.getText() == null || fullNameField.getText().trim().isEmpty()) {
            Alerts.Warning("First name is required.");
            return false;
        }

        if (fullNameField1.getText() == null || fullNameField1.getText().trim().isEmpty()) {
            Alerts.Warning("Last name is required.");
            return false;
        }

        if (genderComboBox.getValue() == null) {
            Alerts.Warning("Gender is required.");
            return false;
        }

        if (contactField.getText() == null || contactField.getText().trim().isEmpty()) {
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

    private int getCurrentSpecializationID(int doctorID) {
        try {
            PreparedStatement stmt = Database.getConnection().prepareStatement(
                "SELECT SpecializationID FROM doctor WHERE DoctorID = ?"
            );
            stmt.setInt(1, doctorID);
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

    @FXML
    private void addSpecialization(ActionEvent event) {
        String additionalSpec = additionalSpecializationComboBox.getValue();
        if (additionalSpec == null || additionalSpec.trim().isEmpty()) {
            Alerts.Warning("Please select a specialization to add.");
            return;
        }
        addSpecialization(additionalSpec);
        additionalSpecializationComboBox.setValue(null); // Reset the combo box
        // Refresh the ListView to show the newly added specialization
        loadAdditionalSpecializations();
    }

    private void addSpecialization(String specializationName) {
        try {
            int specializationID = getSpecializationID(specializationName);
            if (specializationID == -1) {
                Alerts.Warning("Invalid specialization selected.");
                return;
            }

            // Check if this specialization is already assigned to the doctor
            PreparedStatement checkStmt = Database.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM doctor_specialization WHERE DoctorID = ? AND SpecializationID = ?"
            );
            checkStmt.setInt(1, doctorID);
            checkStmt.setInt(2, specializationID);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                Alerts.Warning("This specialization is already assigned to the doctor.");
                return;
            }

            // Add the specialization to the junction table
            PreparedStatement insertStmt = Database.getConnection().prepareStatement(
                "INSERT INTO doctor_specialization (DoctorID, SpecializationID) VALUES (?, ?)"
            );
            insertStmt.setInt(1, doctorID);
            insertStmt.setInt(2, specializationID);
            insertStmt.executeUpdate();

            Alerts.Info("Specialization added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Failed to add specialization: " + e.getMessage());
        }
    }

    private void loadAdditionalSpecializations() {
        ObservableList<String> additionalSpecs = FXCollections.observableArrayList();
        try {
            PreparedStatement stmt = Database.getConnection().prepareStatement(
                "SELECT s.SpecializationName FROM doctor_specialization ds " +
                "JOIN specialization s ON ds.SpecializationID = s.SpecializationID " +
                "WHERE ds.DoctorID = ? AND ds.SpecializationID != (SELECT SpecializationID FROM doctor WHERE DoctorID = ?)"
            );
            stmt.setInt(1, doctorID);
            stmt.setInt(2, doctorID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                additionalSpecs.add(rs.getString("SpecializationName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Failed to load additional specializations: " + e.getMessage());
        }
        additionalSpecializationsListView.setItems(additionalSpecs);
    }

    @FXML
    private void removeSpecialization(ActionEvent event) {
        String selectedSpec = additionalSpecializationsListView.getSelectionModel().getSelectedItem();
        if (selectedSpec == null) {
            Alerts.Warning("Please select a specialization to remove.");
            return;
        }
        removeSpecialization(selectedSpec);
        // Refresh the ListView to reflect the removal
        loadAdditionalSpecializations();
    }

    private void removeSpecialization(String specializationName) {
        try {
            int specializationID = getSpecializationID(specializationName);
            if (specializationID == -1) {
                Alerts.Warning("Invalid specialization selected.");
                return;
            }

            // Remove the specialization from the junction table
            PreparedStatement deleteStmt = Database.getConnection().prepareStatement(
                "DELETE FROM doctor_specialization WHERE DoctorID = ? AND SpecializationID = ?"
            );
            deleteStmt.setInt(1, doctorID);
            deleteStmt.setInt(2, specializationID);
            int rowsAffected = deleteStmt.executeUpdate();

            if (rowsAffected > 0) {
                Alerts.Info("Specialization removed successfully!");
            } else {
                Alerts.Warning("Specialization not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Failed to remove specialization: " + e.getMessage());
        }
    }

    @FXML
    private void setAsPrimary(ActionEvent event) {
        String selectedSpec = additionalSpecializationsListView.getSelectionModel().getSelectedItem();
        if (selectedSpec == null) {
            Alerts.Warning("Please select a specialization to set as primary.");
            return;
        }
        setAsPrimary(selectedSpec);
        // Refresh the ListView to reflect the change
        loadAdditionalSpecializations();
    }

    private void setAsPrimary(String specializationName) {
        try {
            int specializationID = getSpecializationID(specializationName);
            if (specializationID == -1) {
                Alerts.Warning("Invalid specialization selected.");
                return;
            }

            // Update the doctor's primary specialization
            PreparedStatement updateStmt = Database.getConnection().prepareStatement(
                "UPDATE doctor SET SpecializationID = ? WHERE DoctorID = ?"
            );
            updateStmt.setInt(1, specializationID);
            updateStmt.setInt(2, doctorID);
            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update the combo box to reflect the new primary specialization
                specializationComboBox.setValue(specializationName);
                Alerts.Info("Primary specialization updated successfully!");
            } else {
                Alerts.Warning("Failed to update primary specialization.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Failed to set primary specialization: " + e.getMessage());
        }
    }
}
