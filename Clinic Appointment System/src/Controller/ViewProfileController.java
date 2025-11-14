package Controller;

import Util.Alerts;
import Util.Database;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewProfileController {

    @FXML
    private Text doctorIDText;

    @FXML
    private Text nameText;

    @FXML
    private Text sexText;

    @FXML
    private Text specializationText;

    @FXML
    private Text contactText;

    @FXML
    private Text emailText;

    @FXML
    private Text upcomingAppointmentsText;

    @FXML
    private Text associatedPatientsText;

    @FXML
    private VBox upcomingVBox;

    @FXML
    private VBox patientsVBox;

    public void setDoctorData(int doctorID) {
        try {
            // Query doctor details
            String query = "SELECT d.DoctorID, d.FirstName, d.LastName, d.Sex, " +
                          "s.SpecializationName, d.Contact " +
                          "FROM doctor d " +
                          "LEFT JOIN specialization s ON d.SpecializationID = s.SpecializationID " +
                          "WHERE d.DoctorID = ?";
        ResultSet rs = Database.query(query, doctorID);
        if (rs != null && rs.next()) {
            // Format Doctor ID
            doctorIDText.setText("DR" + String.format("%03d", rs.getInt("DoctorID")));
            
            // Properly format name with title
            String firstName = rs.getString("FirstName");
            String lastName = rs.getString("LastName");
            String fullName = "DR. " + firstName + " " + lastName;
            nameText.setText(fullName);
            
            // Set other fields
            sexText.setText(rs.getString("Sex"));
            
            // Handle null specialization
            String specialization = rs.getString("SpecializationName");
            specializationText.setText(specialization != null ? specialization : "N/A");
            
            // Format contact
            String contact = rs.getString("Contact");
            contactText.setText(contact != null ? contact : "N/A");

            // Generate automated email based on doctor ID
            String email = "dr" + String.format("%03d", doctorID) + "@doctor.com";
            emailText.setText(email);
        } else {
            // Handle case where doctor is not found
            Alerts.Warning("Doctor with ID " + doctorID + " not found.");
            return;
        }

            // Query upcoming appointments
            query = "SELECT a.Date, a.Time, CONCAT(p.FirstName, ' ', p.LastName) AS PatientName, a.Status " +
                    "FROM appointment a " +
                    "JOIN patient p ON a.PatientID = p.PatientID " +
                    "WHERE a.DoctorID = ? AND a.Date >= CURDATE() AND a.Status = 'Pending' " +
                    "ORDER BY a.Date, a.Time";
            rs = Database.query(query, doctorID);
            int upcomingCount = 0;
            upcomingVBox.getChildren().clear();
            if (rs != null) {
                while (rs.next()) {
                    upcomingCount++;
                    String patientName = rs.getString("PatientName");
                    String date = rs.getString("Date");
                    String time = rs.getString("Time");
                    String status = rs.getString("Status");

                    // Create appointment pane
                    javafx.scene.layout.Pane pane = new javafx.scene.layout.Pane();
                    pane.setPrefHeight(40.0);
                    pane.setPrefWidth(354.0);
                    pane.setStyle("-fx-background-color: dbf0f0; -fx-border-color: #808080; -fx-background-radius: 10; -fx-border-radius: 10;");

                    Text nameText = new Text(26.0, 18.0, patientName);
                    nameText.setFont(new javafx.scene.text.Font("Arial", 13.0));

                    Text dateTimeText = new Text(25.0, 31.0, date + " " + time);
                    dateTimeText.setFill(javafx.scene.paint.Color.GRAY);
                    dateTimeText.setFont(new javafx.scene.text.Font(10.0));

                    // Status pane
                    javafx.scene.layout.Pane statusPane = new javafx.scene.layout.Pane();
                    statusPane.setLayoutX(255.0);
                    statusPane.setLayoutY(9.0);
                    statusPane.setPrefHeight(21.0);
                    statusPane.setPrefWidth(75.0);
                    statusPane.setStyle("-fx-background-color: #ffffcc; -fx-border-color: #ffcc00; -fx-background-radius: 10; -fx-border-radius: 10;");

                    Text statusText = new Text(16.0, 14.0, status);
                    statusText.setFill(javafx.scene.paint.Color.ORANGE);
                    statusText.setFont(new javafx.scene.text.Font(10.0));
                    statusPane.getChildren().add(statusText);

                    pane.getChildren().addAll(nameText, dateTimeText, statusPane);
                    upcomingVBox.getChildren().add(pane);
                }
            }
            upcomingAppointmentsText.setText("Upcoming Appointments (" + upcomingCount + ")");

            // Query associated patients
            query = "SELECT DISTINCT CONCAT(p.FirstName, ' ', p.LastName) AS PatientName " +
                    "FROM appointment a " +
                    "JOIN patient p ON a.PatientID = p.PatientID " +
                    "WHERE a.DoctorID = ?";
            rs = Database.query(query, doctorID);
            int patientsCount = 0;
            patientsVBox.getChildren().clear();
            if (rs != null) {
                while (rs.next()) {
                    patientsCount++;
                    String patientName = rs.getString("PatientName");

                    // Create patient pane
                    javafx.scene.layout.Pane pane = new javafx.scene.layout.Pane();
                    pane.setPrefHeight(21.0);
                    pane.setPrefWidth(75.0);
                    pane.setStyle("-fx-background-color: #e6ffff; -fx-border-color: #00cccc; -fx-background-radius: 10; -fx-border-radius: 10;");

                    Text patientText = new Text(17.0, 14.0, patientName);
                    patientText.setFill(javafx.scene.paint.Color.web("#00cccc"));
                    patientText.setFont(new javafx.scene.text.Font(10.0));
                    pane.getChildren().add(patientText);

                    patientsVBox.getChildren().add(pane);
                }
            }
            associatedPatientsText.setText("Associated Patients (" + patientsCount + ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
