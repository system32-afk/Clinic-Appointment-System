package Controller;

import Util.Database;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewMedicalHistoryController {

    @FXML private Label patientLabel;
    @FXML private VBox medicalHistoryContainer;
    @FXML private Button closeButton;

    private int patientID;

    public void loadMedicalHistoryData(int patientID) {
        this.patientID = patientID;
        loadAllMedicalHistoryRecords();
    }

    private void loadAllMedicalHistoryRecords() {
        try {
            String patientSql = "SELECT CONCAT(FirstName, ' ', LastName) AS PatientName FROM patient WHERE PatientID = ?";
            ResultSet patientRs = Database.query(patientSql, patientID);

            if (patientRs != null && patientRs.next()) {
                patientLabel.setText(patientRs.getString("PatientName") + " (P-" + patientID + ")");
            }

            medicalHistoryContainer.getChildren().clear();

            String sql = """
                SELECT 
                    mh.MedicalHistoryID,
                    mh.BloodType,
                    mh.Allergies,
                    mh.Height,
                    mh.Conditions,
                    mh.Past_Surgeries,
                    mh.RecordDate
                FROM medicalhistory mh
                WHERE mh.PatientID = ?
                ORDER BY mh.RecordDate DESC
            """;

            ResultSet rs = Database.query(sql, patientID);

            int recordCount = 0;
            while (rs != null && rs.next()) {
                recordCount++;
                createMedicalHistoryRecordCard(rs, recordCount);
            }

            if (recordCount == 0) {
                Text noRecords = new Text("No medical history records found for this patient");
                noRecords.setStyle("-fx-fill: #666; -fx-font-size: 14;");
                medicalHistoryContainer.getChildren().add(noRecords);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createMedicalHistoryRecordCard(ResultSet rs, int recordNumber) throws SQLException {
        int medicalHistoryID = rs.getInt("MedicalHistoryID");
        String bloodType = rs.getString("BloodType");
        String allergies = rs.getString("Allergies");
        String height = rs.getString("Height");
        String conditions = rs.getString("Conditions");
        String pastSurgeries = rs.getString("Past_Surgeries");
        String recordDate = rs.getString("RecordDate");

        VBox recordCard = new VBox(10);
        recordCard.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #ddd; -fx-border-radius: 10;");
        recordCard.setPrefWidth(550);

        Text recordHeader = new Text("Medical Record #" + recordNumber);
        recordHeader.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        if (recordDate != null) {
            Text dateText = new Text("Record Date: " + recordDate);
            dateText.setStyle("-fx-font-size: 12; -fx-fill: #666;");
            recordCard.getChildren().add(dateText);
        }

        VBox detailsBox = new VBox(8);

        if (bloodType != null && !bloodType.isEmpty()) {
            addDetailRow(detailsBox, "Blood Type:", bloodType);
        }

        if (height != null && !height.isEmpty()) {
            addDetailRow(detailsBox, "Height:", height + " cm");
        }

        if (allergies != null && !allergies.isEmpty()) {
            addDetailRow(detailsBox, "Allergies:", allergies);
        } else {
            addDetailRow(detailsBox, "Allergies:", "No known allergies");
        }

        if (conditions != null && !conditions.isEmpty()) {
            addDetailRow(detailsBox, "Medical Conditions:", conditions);
        } else {
            addDetailRow(detailsBox, "Medical Conditions:", "No chronic conditions");
        }

        if (pastSurgeries != null && !pastSurgeries.isEmpty()) {
            addDetailRow(detailsBox, "Past Surgeries:", pastSurgeries);
        } else {
            addDetailRow(detailsBox, "Past Surgeries:", "No past surgeries");
        }

        recordCard.getChildren().addAll(recordHeader, detailsBox);
        medicalHistoryContainer.getChildren().add(recordCard);

        if (recordNumber > 0) {
            VBox spacer = new VBox();
            spacer.setPrefHeight(10);
            medicalHistoryContainer.getChildren().add(spacer);
        }
    }

    private void addDetailRow(VBox container, String label, String value) {
        VBox row = new VBox(2);

        Text labelText = new Text(label);
        labelText.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

        Text valueText = new Text(value);
        valueText.setStyle("-fx-font-size: 12; -fx-wrapping-width: 500;");
        valueText.setWrappingWidth(500);

        row.getChildren().addAll(labelText, valueText);
        container.getChildren().add(row);
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
