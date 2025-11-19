package Controller;

import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class DOCTORDashboardController {

    @FXML
    private Label AppointmentCount;

    @FXML
    private Label CompletedAppointments;

    @FXML
    private Label InProgressAppointments;

    @FXML
    private Label CanceledAppointments;

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private VBox statsContainer;

    @FXML
    private HBox row = new HBox();

    @FXML
    public void initialize() {
        // Set and update time
        updateDateTime();

        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Load database data
        loadData();
    }



    private void loadData() {
        try {
            // Get appointment count
            // Get appointment count
            ResultSet result = Database.query(
                    "SELECT " +
                            "COUNT(*) AS total, " +
                            "SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END) AS completed, " +
                            "SUM(CASE WHEN status = 'Canceled' THEN 1 ELSE 0 END) AS canceled, " +
                            "SUM(CASE WHEN status = 'In-Progress' THEN 1 ELSE 0 END) AS in_progress " +
                            "FROM appointment"
            );

            if (result != null && result.next()) {
                AppointmentCount.setText(String.valueOf(result.getInt("total")));
                CompletedAppointments.setText(String.valueOf(result.getInt("completed")));
                CanceledAppointments.setText(String.valueOf(result.getInt("canceled")));
                InProgressAppointments.setText(String.valueOf(result.getInt("in_progress")));
            }

            // Get appointment info
            ResultSet Appointment = Database.query(
                    "SELECT a.AppointmentID, " +
                            "CONCAT(p.LastName, ', ', p.FirstName) AS PatientName, " +
                            "CONCAT('Dr. ', d.LastName) AS DoctorName, " +
                            "a.Time, a.Status " +
                            "FROM appointment a " +
                            "JOIN patient p ON a.PatientID = p.PatientID " +
                            "JOIN doctor d ON a.DoctorID = d.DoctorID"
            );

            statsContainer.getChildren().clear(); // clear previous rows
            while (Appointment.next()) {
                String patientName = Appointment.getString("PatientName");
                String doctorName = Appointment.getString("DoctorName");
                String appointmentTime = Appointment.getString("Time");
                String status = Appointment.getString("Status");

                // Left side: Patient + Doctor
                VBox names = new VBox(2);
                Label patientLabel = new Label(patientName);
                patientLabel.setFont(Font.font("Arial", 30));

                Label doctorLabel = new Label(doctorName);
                doctorLabel.setFont(Font.font("Arial", 15));
                doctorLabel.setStyle("-fx-text-fill: gray;");

                names.getChildren().addAll(patientLabel, doctorLabel);

                // Right side: Time + Status Label
                Label timeLabel = new Label(appointmentTime);
                timeLabel.setFont(Font.font("Arial", 30));

                Label statusLabel = new Label(status);
                statusLabel.setFont(Font.font("Arial", 15));
                statusLabel.setPadding(new Insets(5, 15, 5, 15));

                updateStatusColor(statusLabel, status);

                // Combine time + status in one HBox
                HBox timeAndStatus = new HBox(20, timeLabel, statusLabel);
                timeAndStatus.setAlignment(Pos.CENTER_LEFT);

                // Main row
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(names, spacer, timeAndStatus);

                // Separator line
                Line separator = new Line(0, 0, 900, 0);
                separator.setStroke(Color.LIGHTGRAY);
                separator.setStrokeWidth(1);

                VBox rowWithLine = new VBox(5);
                rowWithLine.getChildren().addAll(row, separator);

                statsContainer.getChildren().add(rowWithLine);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    private void updateStatusColor(Label label, String status) {
        String color;
        switch (status) {
            case "Pending" -> color = "#FCFF9D";
            case "Completed" -> color = "#9DFFAF";
            case "Canceled" -> color = "#FF9D9F";
            case "In-Progress" -> color = "#9DEFFF";
            default -> color = "#E0E0E0";
        }
        label.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 15;"
        );
    }



    /*
    ===================SIDE PANEL BUTTONS=========================
     */
    @FXML
    public void logout(MouseEvent e) throws IOException {
        SceneManager.transition(e,"login");
    }

    @FXML
    public void openMedicalHistory(MouseEvent e) throws IOException {
        SceneManager.transition(e,"MedicalHistory");
    }

    @FXML
    public void openDiagnosis(MouseEvent e) throws IOException {
        SceneManager.transition(e,"Diagnosis");
    }

    @FXML
    public void openRequestProcedure(MouseEvent e) throws IOException {
        SceneManager.OpenPopup(e,"RequestProcedure","Reqest for procedure form");
        
    }

    @FXML
    public void openPrescription(MouseEvent e) throws IOException {
        SceneManager.transition(e,"OpenPrescription");
    }

    @FXML
    public void openDashboard(MouseEvent e) throws IOException {
        SceneManager.transition(e,"DOCTORDashboard");
    }



}
