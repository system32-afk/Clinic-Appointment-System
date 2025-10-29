package Controller;

import Util.Database;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class DashboardController {

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

            // Get patient names
            ResultSet Appointment = Database.query(
                    "SELECT a.AppointmentID, " +
                            "CONCAT(p.LastName, ', ', p.FirstName) AS PatientName, " +
                            "CONCAT('Dr. ', d.LastName) AS DoctorName, " +
                            "Time, Status " +
                            "FROM appointment a " +
                            "JOIN patient p ON a.PatientID = p.PatientID " +
                            "JOIN doctor d ON a.DoctorID = d.DoctorID"
            );


            //this loads the appointments
            statsContainer.getChildren().clear(); // clear previous rows
            while (Appointment.next()) { // loop through each row

                //gets the data from each record
                int appointmentID = Appointment.getInt("AppointmentID");
                String patientName = Appointment.getString("PatientName");
                String DoctorName = Appointment.getString("DoctorName");
                String appointmentTime = Appointment.getString("Time");
                String Status = Appointment.getString("Status");

                HBox row = new HBox();
                row.setSpacing(10);

                //creates a dropdown per row
                ComboBox<String> statusDropdown = new ComboBox<>();
                statusDropdown.getItems().addAll("Pending","In-Progress","Completed","Canceled");
                statusDropdown.setValue(Status);

                // Set rounded corners
                statusDropdown.setStyle(
                        "-fx-background-radius: 15; " +
                                "-fx-border-radius: 15; " +
                                "-fx-border-color: gray; " +
                                "-fx-border-width: 1; " +
                                "-fx-padding: 0 0 0 5;" // optional: padding for text inside
                );

                //Update color upon loading
                updateDropdownColor(statusDropdown,Status);

                //update color when changed
                statusDropdown.setOnAction(e -> {
                    String newStatus = statusDropdown.getValue();
                    updateDropdownColor(statusDropdown,newStatus);

                    Database.update("UPDATE Appointment SET Status = '"+ newStatus + "' WHERE AppointmentID = "+appointmentID);
                });


                VBox Names = new VBox();
                Names.setSpacing(2);

                Label patientLabel = new Label(patientName);
                patientLabel.setFont(Font.font("Arial",30));

                Label doctorLabel = new Label(DoctorName);
                doctorLabel.setFont(Font.font("Arial",15));
                doctorLabel.setStyle("-fx-text-fill: gray;");

                Names.getChildren().addAll(patientLabel,doctorLabel);

                //Spacer lang
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                //a separate HBOX for time and status
                Label Timelabel = new Label(appointmentTime);
                Timelabel.setFont(Font.font("Arial",30));
                HBox timeAndStatus = new HBox();
                timeAndStatus.setSpacing(30);
                timeAndStatus.getChildren().addAll(Timelabel,statusDropdown);





                HBox.setMargin(patientLabel,new Insets(0,0,0,10));
                row.getChildren().addAll(Names,spacer,timeAndStatus);

                row.setAlignment(Pos.CENTER_LEFT);

                // Separator line under each row
                Line separator = new Line(0, 0, 900, 0); // 800px wide line
                separator.setStroke(Color.LIGHTGRAY);
                separator.setStrokeWidth(1);
                VBox rowWithLine = new VBox(5); // 5px spacing between content and line
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

    private void updateDropdownColor(ComboBox<String> comboBox, String status) {
        switch (status) {
            case "Pending" -> comboBox.setStyle("-fx-background-color: #FCFF9D; -fx-font-weight: bold;");
            case "Completed" -> comboBox.setStyle("-fx-background-color: #9DFFAF; -fx-font-weight: bold;");
            case "Canceled" -> comboBox.setStyle("-fx-background-color: #FF9D9F; -fx-font-weight: bold;");
            case "In-Progress" -> comboBox.setStyle("-fx-background-color: #9DEFFF; -fx-font-weight: bold;");
            default -> comboBox.setStyle(""); // default styling
        }
    }

}
