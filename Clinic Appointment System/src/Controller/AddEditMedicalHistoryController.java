package Controller;

import Util.Alerts;
import Util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddEditMedicalHistoryController implements Initializable {

    @FXML private Text dialogTitle;
    @FXML private ComboBox<String> patientCombo;
    @FXML private ComboBox<String> bloodTypeCombo;
    @FXML private TextField heightField;
    @FXML private TextArea allergiesField;
    @FXML private TextArea conditionsField;
    @FXML private TextArea pastSurgeriesField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private String mode = "add";
    private int medicalHistoryID;
    private MedicalHistoryController parentController;
    private Map<String, Integer> patientMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPatients();
        loadBloodTypes();
    }

    private void loadBloodTypes() {
        bloodTypeCombo.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"
        ));
    }

    private void loadPatients() {
        try {
            String sql = "SELECT PatientID, CONCAT(FirstName, ' ', LastName) AS PatientName FROM patient ORDER BY LastName, FirstName";
            ResultSet rs = Database.query(sql);

            ObservableList<String> patients = FXCollections.observableArrayList();
            patientMap.clear();

            while (rs != null && rs.next()) {
                int patientID = rs.getInt("PatientID");
                String patientName = rs.getString("PatientName");
                patients.add(patientName);
                patientMap.put(patientName, patientID);
            }

            patientCombo.setItems(patients);

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error loading patients: " + e.getMessage());
        }
    }

    public void setMode(String mode) {
        this.mode = mode;
        if ("edit".equals(mode)) {
            dialogTitle.setText("Edit Medical History");
            saveButton.setText("Update Medical History");
        }
    }

    public void setParentController(MedicalHistoryController parentController) {
        this.parentController = parentController;
    }

    public void loadMedicalHistoryData(int medicalHistoryID) {
        this.medicalHistoryID = medicalHistoryID;

        try {
            String sql = """
                SELECT mh.*, CONCAT(p.FirstName, ' ', p.LastName) AS PatientName 
                FROM medicalhistory mh 
                JOIN patient p ON mh.PatientID = p.PatientID 
                WHERE mh.MedicalHistoryID = ?
            """;
            ResultSet rs = Database.query(sql, medicalHistoryID);

            if (rs != null && rs.next()) {
                patientCombo.setValue(rs.getString("PatientName"));

                bloodTypeCombo.setValue(rs.getString("BloodType"));

                String height = rs.getString("Height");
                if (height != null) heightField.setText(height);

                String allergies = rs.getString("Allergies");
                if (allergies != null) allergiesField.setText(allergies);

                String conditions = rs.getString("Conditions");
                if (conditions != null) conditionsField.setText(conditions);

                String pastSurgeries = rs.getString("Past_Surgeries");
                if (pastSurgeries != null) pastSurgeriesField.setText(pastSurgeries);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error loading medical history data: " + e.getMessage());
        }
    }

    @FXML
    private void saveMedicalHistory() {
        String selectedPatient = patientCombo.getValue();
        String bloodType = bloodTypeCombo.getValue();
        String height = heightField.getText().trim();
        String allergies = allergiesField.getText().trim();
        String conditions = conditionsField.getText().trim();
        String pastSurgeries = pastSurgeriesField.getText().trim();

        if (selectedPatient == null || selectedPatient.isEmpty()) {
            Alerts.Warning("Please select a patient");
            return;
        }

        int patientID = patientMap.get(selectedPatient);

        try {
            if ("add".equals(mode)) {
                String checkSql = "SELECT COUNT(*) as count FROM medicalhistory WHERE PatientID = ?";
                ResultSet checkRs = Database.query(checkSql, patientID);

                if (checkRs != null && checkRs.next() && checkRs.getInt("count") > 0) {
                    Alerts.Warning("This patient already has a medical history record. Please edit the existing record instead.");
                    return;
                }

                String insertSql = """
                    INSERT INTO medicalhistory (PatientID, BloodType, Allergies, Height, Conditions, Past_Surgeries) 
                    VALUES (?, ?, ?, ?, ?, ?)
                """;
                int result = Database.update(insertSql, patientID, bloodType, allergies, height, conditions, pastSurgeries);

                if (result > 0) {
                    Alerts.Info("Medical history added successfully!");
                    closeWindow();
                } else {
                    Alerts.Warning("Failed to add medical history");
                }
            } else {
                String updateSql = """
                    UPDATE medicalhistory 
                    SET PatientID = ?, BloodType = ?, Allergies = ?, Height = ?, Conditions = ?, Past_Surgeries = ?
                    WHERE MedicalHistoryID = ?
                """;
                int result = Database.update(updateSql, patientID, bloodType, allergies, height, conditions, pastSurgeries, medicalHistoryID);

                if (result > 0) {
                    Alerts.Info("Medical history updated successfully!");
                    closeWindow();
                } else {
                    Alerts.Warning("Failed to update medical history");
                }
            }

            if (parentController != null) {
                parentController.refreshData();
            }

        } catch (SQLException e) {
            Alerts.Warning("Error saving medical history: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }


}
