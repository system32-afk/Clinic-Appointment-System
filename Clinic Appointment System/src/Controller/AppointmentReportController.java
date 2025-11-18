package Controller;

import Util.Alerts;
import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AppointmentReportController implements Initializable {

    @FXML private Text Date;
    @FXML private Text Time;
    @FXML private ComboBox<String> periodCombo;
    @FXML private ComboBox<String> doctorCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private HBox dateRangeBox;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label completedAppointmentsLabel;
    @FXML private Label pendingAppointmentsLabel;
    @FXML private Label canceledAppointmentsLabel;
    @FXML private VBox appointmentList;

    private Map<String, Integer> doctorMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateDateTime();

        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        setupFilters();
        loadDefaultReport();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    private void setupFilters() {
        // Setup period options
        periodCombo.setItems(FXCollections.observableArrayList(
                "Today", "This Week", "This Month", "This Year", "Custom Range", "All Time"
        ));
        periodCombo.setValue("This Month");

        // Setup status options
        statusCombo.setItems(FXCollections.observableArrayList(
                "All", "Pending", "In-Progress", "Completed", "Canceled"
        ));
        statusCombo.setValue("All");

        // Load doctors
        loadDoctors();

        // Setup period combo listener
        periodCombo.setOnAction(e -> {
            if ("Custom Range".equals(periodCombo.getValue())) {
                dateRangeBox.setVisible(true);
            } else {
                dateRangeBox.setVisible(false);
            }
        });

        // Set default dates
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());
    }

    private void loadDoctors() {
        try {
            String sql = "SELECT DoctorID, CONCAT(FirstName, ' ', LastName) AS DoctorName FROM doctor ORDER BY LastName, FirstName";
            ResultSet rs = Database.query(sql);

            doctorMap.clear();
            doctorMap.put("All Doctors", 0);

            while (rs != null && rs.next()) {
                int doctorID = rs.getInt("DoctorID");
                String doctorName = rs.getString("DoctorName");
                doctorMap.put(doctorName, doctorID);
            }

            doctorCombo.setItems(FXCollections.observableArrayList(doctorMap.keySet()));
            doctorCombo.setValue("All Doctors");

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error loading doctors: " + e.getMessage());
        }
    }

    @FXML
    private void generateReport() {
        try {
            String period = periodCombo.getValue();
            String doctorFilter = doctorCombo.getValue();
            String statusFilter = statusCombo.getValue();

            int doctorID = doctorMap.get(doctorFilter);

            // Build SQL query based on filters
            StringBuilder sql = new StringBuilder("""
                SELECT 
                    a.AppointmentID,
                    a.Date,
                    a.Time,
                    a.Status,
                    a.ReasonForVisit,
                    CONCAT(p.FirstName, ' ', p.LastName) AS PatientName,
                    CONCAT('Dr. ', d.FirstName, ' ', d.LastName) AS DoctorName,
                    s.SpecializationName
                FROM appointment a
                JOIN patient p ON a.PatientID = p.PatientID
                JOIN doctor d ON a.DoctorID = d.DoctorID
                JOIN ref_specialization s ON d.SpecializationID = s.SpecializationID
                WHERE 1=1
            """);

            // Add doctor filter
            if (doctorID != 0) {
                sql.append(" AND a.DoctorID = ").append(doctorID);
            }

            // Add status filter
            if (!"All".equals(statusFilter)) {
                sql.append(" AND a.Status = '").append(statusFilter).append("'");
            }

            // Add date filter based on period
            if ("Today".equals(period)) {
                sql.append(" AND a.Date = CURDATE()");
            } else if ("This Week".equals(period)) {
                sql.append(" AND YEARWEEK(a.Date, 1) = YEARWEEK(CURDATE(), 1)");
            } else if ("This Month".equals(period)) {
                sql.append(" AND YEAR(a.Date) = YEAR(CURDATE()) AND MONTH(a.Date) = MONTH(CURDATE())");
            } else if ("This Year".equals(period)) {
                sql.append(" AND YEAR(a.Date) = YEAR(CURDATE())");
            } else if ("Custom Range".equals(period)) {
                if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                    sql.append(" AND a.Date BETWEEN '")
                            .append(fromDatePicker.getValue())
                            .append("' AND '")
                            .append(toDatePicker.getValue())
                            .append("'");
                }
            }

            sql.append(" ORDER BY a.Date DESC, a.Time DESC");

            ResultSet rs = Database.query(sql.toString());
            displayAppointmentReport(rs);
            updateStatistics();

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error generating report: " + e.getMessage());
        }
    }

    private void displayAppointmentReport(ResultSet rs) throws SQLException {
        appointmentList.getChildren().clear();

        // Header row
        HBox headerRow = new HBox(20);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setStyle("-fx-background-color: #e9ecef; -fx-padding: 10; -fx-background-radius: 5;");
        headerRow.setPrefWidth(900);

        Label idHeader = new Label("Appointment ID");
        idHeader.setStyle("-fx-font-weight: bold; -fx-min-width: 100;");

        Label dateHeader = new Label("Date & Time");
        dateHeader.setStyle("-fx-font-weight: bold; -fx-min-width: 150;");

        Label patientHeader = new Label("Patient");
        patientHeader.setStyle("-fx-font-weight: bold; -fx-min-width: 150;");

        Label doctorHeader = new Label("Doctor");
        doctorHeader.setStyle("-fx-font-weight: bold; -fx-min-width: 150;");

        Label reasonHeader = new Label("Reason");
        reasonHeader.setStyle("-fx-font-weight: bold; -fx-min-width: 200;");

        Label statusHeader = new Label("Status");
        statusHeader.setStyle("-fx-font-weight: bold; -fx-min-width: 100;");

        headerRow.getChildren().addAll(idHeader, dateHeader, patientHeader, doctorHeader, reasonHeader, statusHeader);
        appointmentList.getChildren().add(headerRow);

        int count = 0;
        while (rs != null && rs.next()) {
            count++;
            int appointmentID = rs.getInt("AppointmentID");
            String date = rs.getString("Date");
            String time = rs.getString("Time");
            String status = rs.getString("Status");
            String reason = rs.getString("ReasonForVisit");
            String patientName = rs.getString("PatientName");
            String doctorName = rs.getString("DoctorName");
            String specialization = rs.getString("SpecializationName");

            // Create appointment row
            HBox row = new HBox(20);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");
            row.setPrefWidth(900);

            // Appointment ID
            Label idLabel = new Label("A-" + appointmentID);
            idLabel.setStyle("-fx-min-width: 100;");

            // Date & Time
            Label dateTimeLabel = new Label(date + " " + time);
            dateTimeLabel.setStyle("-fx-min-width: 150;");

            // Patient
            Label patientLabel = new Label(patientName);
            patientLabel.setStyle("-fx-min-width: 150;");

            // Doctor
            VBox doctorInfo = new VBox(2);
            Label doctorLabel = new Label(doctorName);
            Label specLabel = new Label(specialization);
            specLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10;");
            doctorInfo.getChildren().addAll(doctorLabel, specLabel);
            doctorInfo.setStyle("-fx-min-width: 150;");

            // Reason
            Label reasonLabel = new Label(reason);
            reasonLabel.setStyle("-fx-min-width: 200; -fx-wrap-text: true;");

            // Status with color coding
            Label statusLabel = new Label(status);
            statusLabel.setPadding(new Insets(5, 10, 5, 10));
            statusLabel.setStyle("-fx-font-weight: bold; -fx-background-radius: 10; -fx-min-width: 100;");

            // Set status color
            switch (status) {
                case "Completed" -> statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #9DFFAF;");
                case "Pending" -> statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #FCFF9D;");
                case "In-Progress" -> statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #9DEFFF;");
                case "Canceled" -> statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #FF9D9F;");
            }

            row.getChildren().addAll(idLabel, dateTimeLabel, patientLabel, doctorInfo, reasonLabel, statusLabel);

            // Add separator
            Line separator = new Line(0, 0, 900, 0);
            separator.setStroke(Color.LIGHTGRAY);
            separator.setStrokeWidth(1);

            VBox container = new VBox(5);
            container.getChildren().addAll(row, separator);
            appointmentList.getChildren().add(container);
        }

        if (count == 0) {
            Label noRecords = new Label("No appointments found matching the selected criteria");
            noRecords.setStyle("-fx-text-fill: #666; -fx-font-size: 16; -fx-padding: 20;");
            appointmentList.getChildren().add(noRecords);
        }
    }

    private void updateStatistics() {
        try {
            String sql = "SELECT Status, COUNT(*) as count FROM appointment GROUP BY Status";
            ResultSet rs = Database.query(sql);

            int total = 0;
            int completed = 0;
            int pending = 0;
            int canceled = 0;

            while (rs != null && rs.next()) {
                String status = rs.getString("Status");
                int count = rs.getInt("count");
                total += count;

                switch (status) {
                    case "Completed" -> completed = count;
                    case "Pending" -> pending = count;
                    case "Canceled" -> canceled = count;
                    case "In-Progress" -> pending += count; // Include in-progress in pending
                }
            }

            totalAppointmentsLabel.setText(String.valueOf(total));
            completedAppointmentsLabel.setText(String.valueOf(completed));
            pendingAppointmentsLabel.setText(String.valueOf(pending));
            canceledAppointmentsLabel.setText(String.valueOf(canceled));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDefaultReport() {
        generateReport();
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

    public void openAppointmentReport(ActionEvent e) throws IOException {
    }

    public void openPaymentScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "PaymentProcessing");
    }
}
