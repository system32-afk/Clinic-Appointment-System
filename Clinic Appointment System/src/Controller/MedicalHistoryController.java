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

public class MedicalHistoryController implements Initializable {

    @FXML private Text Date;
    @FXML private Text Time;
    @FXML private VBox medicalHistoryList;
    @FXML private Button addMedicalHistoryBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateDateTime();

        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        loadMedicalHistories();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    private void loadMedicalHistories() {
        try {
            String sql = """
                SELECT 
                    mh.MedicalHistoryID,
                    mh.BloodType,
                    mh.Allergies,
                    mh.Height,
                    mh.Conditions,
                    mh.Past_Surgeries,
                    CONCAT(p.FirstName, ' ', p.LastName) AS PatientName,
                    p.PatientID
                FROM medicalhistory mh
                JOIN patient p ON mh.PatientID = p.PatientID
                ORDER BY p.LastName, p.FirstName
            """;

            ResultSet rs = Database.query(sql);
            medicalHistoryList.getChildren().clear();

            while (rs != null && rs.next()) {
                int medicalHistoryID = rs.getInt("MedicalHistoryID");
                String patientName = rs.getString("PatientName");
                String bloodType = rs.getString("BloodType");
                String allergies = rs.getString("Allergies");
                String height = rs.getString("Height");
                String conditions = rs.getString("Conditions");
                String pastSurgeries = rs.getString("Past_Surgeries");
                int patientID = rs.getInt("PatientID");

                HBox row = new HBox(20);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;");
                row.setPrefWidth(900);

                VBox patientInfo = new VBox(5);
                Label patientLabel = new Label(patientName);
                patientLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                Label patientIdLabel = new Label("P-" + patientID);
                patientIdLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
                patientInfo.getChildren().addAll(patientLabel, patientIdLabel);

                VBox medicalDetails = new VBox(3);
                if (bloodType != null && !bloodType.isEmpty()) {
                    Label bloodLabel = new Label("Blood Type: " + bloodType);
                    bloodLabel.setStyle("-fx-font-size: 12;");
                    medicalDetails.getChildren().add(bloodLabel);
                }
                if (conditions != null && !conditions.isEmpty()) {
                    Label conditionsLabel = new Label("Conditions: " + conditions);
                    conditionsLabel.setStyle("-fx-font-size: 12;");
                    medicalDetails.getChildren().add(conditionsLabel);
                }
                if (allergies != null && !allergies.isEmpty()) {
                    Label allergiesLabel = new Label("Allergies: " + allergies);
                    allergiesLabel.setStyle("-fx-font-size: 12;");
                    medicalDetails.getChildren().add(allergiesLabel);
                }

                Button viewBtn = new Button("View Details");
                viewBtn.setStyle("-fx-background-color: #40D0E0; -fx-text-fill: white; -fx-background-radius: 5;");
                viewBtn.setOnAction(e -> openViewMedicalHistoryDialog(medicalHistoryID));

                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-background-radius: 5;");
                editBtn.setOnAction(e -> openEditMedicalHistoryDialog(medicalHistoryID));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteBtn.setOnAction(e -> deleteMedicalHistory(medicalHistoryID, patientName));

                HBox buttons = new HBox(10, viewBtn, editBtn, deleteBtn);
                buttons.setAlignment(Pos.CENTER_RIGHT);

                row.getChildren().addAll(patientInfo, medicalDetails, buttons);

                Line separator = new Line(0, 0, 900, 0);
                separator.setStroke(Color.LIGHTGRAY);
                separator.setStrokeWidth(1);

                VBox container = new VBox(10);
                container.getChildren().addAll(row, separator);
                medicalHistoryList.getChildren().add(container);
            }

            if (medicalHistoryList.getChildren().isEmpty()) {
                Label noRecords = new Label("No medical history records found");
                noRecords.setStyle("-fx-text-fill: #666; -fx-font-size: 16;");
                medicalHistoryList.getChildren().add(noRecords);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error loading medical histories: " + e.getMessage());
        }
    }

    @FXML
    private void openAddMedicalHistoryDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/AddEditMedicalHistory.fxml"));
            Parent root = loader.load();

            AddEditMedicalHistoryController controller = loader.getController();
            controller.setParentController(this);
            controller.setMode("add");

            Stage stage = new Stage();
            stage.setTitle("Add Medical History");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alerts.Warning("Error opening medical history dialog");
        }
    }

    private void openViewMedicalHistoryDialog(int medicalHistoryID) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/ViewMedicalHistory.fxml"));
            Parent root = loader.load();

            ViewMedicalHistoryController controller = loader.getController();
            controller.loadMedicalHistoryData(medicalHistoryID);

            Stage stage = new Stage();
            stage.setTitle("Medical History Details");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alerts.Warning("Error opening medical history details");
        }
    }

    private void openEditMedicalHistoryDialog(int medicalHistoryID) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/AddEditMedicalHistory.fxml"));
            Parent root = loader.load();

            AddEditMedicalHistoryController controller = loader.getController();
            controller.setParentController(this);
            controller.setMode("edit");
            controller.loadMedicalHistoryData(medicalHistoryID);

            Stage stage = new Stage();
            stage.setTitle("Edit Medical History");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alerts.Warning("Error opening edit dialog");
        }
    }

    private void deleteMedicalHistory(int medicalHistoryID, String patientName) {
        boolean confirm = Alerts.Confirmation("Are you sure you want to delete medical history for: " + patientName + "?");
        if (!confirm) return;

        try {
            String deleteSql = "DELETE FROM medicalhistory WHERE MedicalHistoryID = ?";
            int result = Database.update(deleteSql, medicalHistoryID);

            if (result > 0) {
                Alerts.Info("Medical history deleted successfully!");
                loadMedicalHistories();
            } else {
                Alerts.Warning("Failed to delete medical history");
            }
        } catch (Exception e) {
            Alerts.Warning("Error deleting medical history: " + e.getMessage());
        }
    }

    public void refreshData() {
        loadMedicalHistories();
    }

    public void DashboardScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "ADMINDashboard");
    }

    public void AppointmentScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "Appointments");
    }

    public void openMedicineManagement(ActionEvent e) throws IOException {
        SceneManager.transition(e, "MedicineManagement");
    }

    public void openMedicalHistory(ActionEvent e) throws IOException {
        SceneManager.transition(e, "MedicalHistory");
    }

    public void openPaymentScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "PaymentProcessing");
    }

}
