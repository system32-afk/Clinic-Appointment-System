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
import javafx.scene.control.ComboBox;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PaymentProcessingController implements Initializable {

    @FXML private Text Date;
    @FXML private Text Time;
    @FXML private VBox procedureList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateDateTime();

        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        loadUnpaidProcedures();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    private void loadUnpaidProcedures() {
        try {
            String sql = """
            SELECT 
                pr.ProcedureID,
                pr.ProcedureDate,
                s.ServiceName,
                s.Price,
                CONCAT(p.FirstName, ' ', p.LastName) AS PatientName,
                CONCAT('Dr. ', d.FirstName, ' ', d.LastName) AS DoctorName,
                a.AppointmentID
            FROM procedurerequest pr
            JOIN service s ON pr.ServiceID = s.ServiceID
            JOIN appointment a ON pr.AppointmentID = a.AppointmentID
            JOIN patient p ON a.PatientID = p.PatientID
            JOIN doctor d ON a.DoctorID = d.DoctorID
            WHERE NOT EXISTS (
                SELECT 1 FROM payment pm WHERE pm.ProcedureID = pr.ProcedureID
            )
            ORDER BY pr.ProcedureDate DESC
        """;
            
            ResultSet rs = Database.query(sql);
            procedureList.getChildren().clear();

            while (rs != null && rs.next()) {
                int procedureID = rs.getInt("ProcedureID");
                String patientName = rs.getString("PatientName");
                String doctorName = rs.getString("DoctorName");
                String serviceName = rs.getString("ServiceName");
                double price = rs.getDouble("Price");
                String procedureDate = rs.getString("ProcedureDate");

                // Create procedure row
                HBox row = new HBox(20);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;");
                row.setPrefWidth(900);

                // Patient and Doctor info
                VBox patientInfo = new VBox(5);
                Label patientLabel = new Label(patientName);
                patientLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                Label doctorLabel = new Label(doctorName);
                doctorLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
                patientInfo.getChildren().addAll(patientLabel, doctorLabel);

                // Service info
                VBox serviceInfo = new VBox(5);
                Label serviceLabel = new Label(serviceName);
                serviceLabel.setStyle("-fx-font-weight: bold;");
                Label dateLabel = new Label(procedureDate);
                dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
                serviceInfo.getChildren().addAll(serviceLabel, dateLabel);

                // Price
                Label priceLabel = new Label(String.format("₱%.2f", price));
                priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #40D0E0;");

                // Process Payment button
                Button processBtn = new Button("Process Payment");
                processBtn.setStyle("-fx-background-color: #40D0E0; -fx-text-fill: white; -fx-background-radius: 5;");
                processBtn.setOnAction(e -> openPaymentDialog(procedureID, patientName, doctorName, serviceName, price));

                row.getChildren().addAll(patientInfo, serviceInfo, priceLabel, processBtn);

                // Add separator
                Line separator = new Line(0, 0, 900, 0);
                separator.setStroke(Color.LIGHTGRAY);
                separator.setStrokeWidth(1);

                VBox container = new VBox(10);
                container.getChildren().addAll(row, separator);
                procedureList.getChildren().add(container);
            }

            if (procedureList.getChildren().isEmpty()) {
                Label noRecords = new Label("No unpaid procedures found");
                noRecords.setStyle("-fx-text-fill: #666; -fx-font-size: 16;");
                procedureList.getChildren().add(noRecords);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error loading procedures: " + e.getMessage());
        }
    }

    private void openPaymentDialog(int procedureID, String patientName, String doctorName, String serviceName, double amount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/PaymentDialog.fxml"));
            Parent root = loader.load();

            PaymentDialogController controller = loader.getController();
            controller.setProcedureData(procedureID, patientName, doctorName, serviceName, amount);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Process Payment");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alerts.Warning("Error opening payment dialog");
        }
    }

    public void refreshData() {
        loadUnpaidProcedures();
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
}
