package Controller;

import Util.Alerts;
import Util.Database;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddEditMedicineController implements Initializable {

    @FXML private Text dialogTitle;
    @FXML private TextField medicineNameField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private String mode = "add"; 
    private int medicineID;
    private MedicineManagementController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setMode(String mode) {
        this.mode = mode;
        if ("edit".equals(mode)) {
            dialogTitle.setText("Edit Medicine");
            saveButton.setText("Update Medicine");
        }
    }

    public void setMedicineData(int medicineID, String medicineName) {
        this.medicineID = medicineID;
        medicineNameField.setText(medicineName);
    }

    public void setParentController(MedicineManagementController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void saveMedicine() {
        String medicineName = medicineNameField.getText().trim();

        if (medicineName.isEmpty()) {
            Alerts.Warning("Medicine name is required");
            return;
        }

        try {
            if ("add".equals(mode)) {
                String checkSql = "SELECT COUNT(*) as count FROM medicine WHERE MedicineName = ?";
                ResultSet checkRs = Database.query(checkSql, medicineName);

                if (checkRs != null && checkRs.next() && checkRs.getInt("count") > 0) {
                    Alerts.Warning("Medicine with this name already exists!");
                    return;
                }

                String insertSql = "INSERT INTO medicine (MedicineName) VALUES (?)";
                int result = Database.update(insertSql, medicineName);

                if (result > 0) {
                    Alerts.Info("Medicine added successfully!");
                    closeWindow();
                } else {
                    Alerts.Warning("Failed to add medicine");
                }
            } else {
                String updateSql = "UPDATE medicine SET MedicineName = ? WHERE MedicineID = ?";
                int result = Database.update(updateSql, medicineName, medicineID);

                if (result > 0) {
                    Alerts.Info("Medicine updated successfully!");
                    closeWindow();
                } else {
                    Alerts.Warning("Failed to update medicine");
                }
            }

            if (parentController != null) {
                parentController.refreshData();
            }

        } catch (SQLException e) {
            Alerts.Warning("Error saving medicine: " + e.getMessage());
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
