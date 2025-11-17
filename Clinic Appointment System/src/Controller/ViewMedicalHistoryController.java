package Controller;

import Util.Database;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewMedicalHistoryController {

    @FXML private Label patientLabel;
    @FXML private Label bloodTypeLabel;
    @FXML private Label heightLabel;
    @FXML private TextArea allergiesArea;
    @FXML private TextArea conditionsArea;
    @FXML private TextArea pastSurgeriesArea;
    @FXML private Button closeButton;

    public void loadMedicalHistoryData(int medicalHistoryID) {
        try {
            String sql = """
                SELECT 
                    mh.BloodType,
                    mh.Allergies,
                    mh.Height,
                    mh.Conditions,
                    mh.Past_Surgeries,
                    CONCAT(p.FirstName, ' ', p.LastName) AS PatientName,
                    p.PatientID
                FROM medicalhistory mh
                JOIN patient p ON mh.PatientID = p.PatientID
                WHERE mh.MedicalHistoryID = ?
            """;

            ResultSet rs = Database.query(sql, medicalHistoryID);

            if (rs != null && rs.next()) {
                patientLabel.setText(rs.getString("PatientName") + " (P-" + rs.getInt("PatientID") + ")");

                String bloodType = rs.getString("BloodType");
                bloodTypeLabel.setText(bloodType != null ? bloodType : "Not specified");

                String height = rs.getString("Height");
                heightLabel.setText(height != null ? height + " cm" : "Not specified");

                String allergies = rs.getString("Allergies");
                allergiesArea.setText(allergies != null ? allergies : "No known allergies");

                String conditions = rs.getString("Conditions");
                conditionsArea.setText(conditions != null ? conditions : "No chronic conditions");

                String pastSurgeries = rs.getString("Past_Surgeries");
                pastSurgeriesArea.setText(pastSurgeries != null ? pastSurgeries : "No past surgeries");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
