package Controller;

import Util.Alerts;
import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MedicineManagementController implements Initializable {

    @FXML private Text Date;
    @FXML private Text Time;
    @FXML private VBox medicineList;
    @FXML private Button addMedicineBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateDateTime();

        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        loadMedicines();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    private void loadMedicines() {
        try {
            String sql = "SELECT MedicineID, MedicineName FROM medicine ORDER BY MedicineName";
            ResultSet rs = Database.query(sql);
            medicineList.getChildren().clear();

            while (rs != null && rs.next()) {
                int medicineID = rs.getInt("MedicineID");
                String medicineName = rs.getString("MedicineName");

                HBox row = new HBox(20);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;");
                row.setPrefWidth(900);

                Label idLabel = new Label("M-" + medicineID);
                idLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 80;");

                Label nameLabel = new Label(medicineName);
                nameLabel.setStyle("-fx-font-size: 14; -fx-min-width: 300;");

                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-background-radius: 5;");
                editBtn.setOnAction(e -> openEditMedicineDialog(medicineID, medicineName));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteBtn.setOnAction(e -> deleteMedicine(medicineID, medicineName));

                HBox buttons = new HBox(10, editBtn, deleteBtn);
                buttons.setAlignment(Pos.CENTER_RIGHT);

                row.getChildren().addAll(idLabel, nameLabel, buttons);

                Line separator = new Line(0, 0, 900, 0);
                separator.setStroke(Color.LIGHTGRAY);
                separator.setStrokeWidth(1);

                VBox container = new VBox(10);
                container.getChildren().addAll(row, separator);
                medicineList.getChildren().add(container);
            }

            if (medicineList.getChildren().isEmpty()) {
                Label noRecords = new Label("No medicines found in inventory");
                noRecords.setStyle("-fx-text-fill: #666; -fx-font-size: 16;");
                medicineList.getChildren().add(noRecords);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error loading medicines: " + e.getMessage());
        }
    }

    @FXML
    private void openAddMedicineDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/AddEditMedicine.fxml"));
            Parent root = loader.load();

            AddEditMedicineController controller = loader.getController();
            controller.setParentController(this);
            controller.setMode("add");

            Stage stage = new Stage();
            stage.setTitle("Add New Medicine");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alerts.Warning("Error opening medicine dialog");
        }
    }

    private void openEditMedicineDialog(int medicineID, String medicineName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/AddEditMedicine.fxml"));
            Parent root = loader.load();

            AddEditMedicineController controller = loader.getController();
            controller.setParentController(this);
            controller.setMode("edit");
            controller.setMedicineData(medicineID, medicineName);

            Stage stage = new Stage();
            stage.setTitle("Edit Medicine");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alerts.Warning("Error opening edit dialog");
        }
    }

    private void deleteMedicine(int medicineID, String medicineName) {
        boolean confirm = Alerts.Confirmation("Are you sure you want to delete medicine: " + medicineName + "?");
        if (!confirm) return;

        try {
            String checkSql = "SELECT COUNT(*) as count FROM prescription WHERE MedicineID = ?";
            ResultSet checkRs = Database.query(checkSql, medicineID);

            if (checkRs != null && checkRs.next() && checkRs.getInt("count") > 0) {
                Alerts.Warning("Cannot delete medicine. It is used in existing prescriptions.");
                return;
            }

            String deleteSql = "DELETE FROM medicine WHERE MedicineID = ?";
            int result = Database.update(deleteSql, medicineID);

            if (result > 0) {
                Alerts.Info("Medicine deleted successfully!");
                loadMedicines(); 
            } else {
                Alerts.Warning("Failed to delete medicine");
            }
        } catch (SQLException e) {
            Alerts.Warning("Error deleting medicine: " + e.getMessage());
        }
    }

    public void refreshData() {
        loadMedicines();
    }

    public void DashboardScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "ADMINDashboard");
    }

    public void AppointmentScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "Appointments");
    }

    public void openMedicineManagement(ActionEvent e) throws IOException {
    }

    public void openPaymentScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "PaymentProcessing");
    }

    public void openMedicalHistory(ActionEvent e) throws IOException {
        SceneManager.transition(e, "MedicalHistory");
    }
}
