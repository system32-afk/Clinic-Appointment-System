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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ADMINDashboardController {

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
            ResultSet result = Database.query("SELECT COUNT(*) AS Count FROM patient");
            if (result.next()) {
                int appointments = result.getInt("Count");
                AppointmentCount.setText(String.valueOf(appointments));
                CompletedAppointments.setText(String.valueOf(appointments));
                CanceledAppointments.setText(String.valueOf(appointments));
                InProgressAppointments.setText(String.valueOf(appointments));
            }

            // Get appointment info
            ResultSet Appointment = Database.query(
                    "SELECT a.AppointmentID, " +
                            "CONCAT(p.LastName, ', ', p.FirstName) AS PatientName, " +
                            "CONCAT('Dr. ', d.LastName) AS DoctorName, " +
                            "Time, Status " +
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



    public void AppointmentScreen(ActionEvent e) throws IOException{
        SceneManager.transition(e,"Appointments");
    }


}
