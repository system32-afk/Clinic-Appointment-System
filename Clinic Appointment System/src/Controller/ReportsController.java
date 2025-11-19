package Controller;

import Util.Database;
import Util.HoverPopup;
import Util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportsController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private Label completedAppointments; // Total Appointments

    @FXML
    private Label topSpecialization; // Top Specialization

    @FXML
    private Label fastestGrowing; // Fastest Growing

    @FXML
    private Label activeSpecializations; // Active Specializations

    @FXML
    private AreaChart<String, Number> lineChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private final String[] specializations = {"Cardiology", "Neurology", "Oncology", "Pediatrics", "Orthopedics"};
    private final String[] colors = {"#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF"}; // Red, Green, Blue, Yellow, Magenta
    @FXML
    private Pane ManagementPane;

    @FXML
    private Pane RecordsManagementButton;

    @FXML
    private Pane ReportsButton;

    @FXML
    private Pane ReportsManagement;

    @FXML
    public void initialize() {
        updateDateTime();
        updateStatistics();

        /*
        ======================== HOVER FEATURE =========================
         */
        HoverPopup.attachHoverPopup(
                RecordsManagementButton,
                ManagementPane,
                Duration.seconds(0.3)
        );

        HoverPopup.attachHoverPopup(
                ReportsButton,
                ReportsManagement,
                Duration.seconds(0.3)
        );

        loadChartData();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    private void updateStatistics() {
        try {
            // Total Appointments (current month)
            ResultSet rs = Database.query("SELECT COUNT(*) AS total FROM appointment WHERE MONTH(Date) = MONTH(CURDATE()) AND YEAR(Date) = YEAR(CURDATE())");
            if (rs != null && rs.next()) {
                completedAppointments.setText(String.valueOf(rs.getInt("total")));
            }

            // Top Specialization (most appointments in current month)
            rs = Database.query("SELECT s.SpecializationName, COUNT(a.AppointmentID) AS count " +
                                "FROM appointment a " +
                                "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                                "JOIN ref_specialization s ON d.SpecializationID = s.SpecializationID " +
                                "WHERE MONTH(a.Date) = MONTH(CURDATE()) AND YEAR(a.Date) = YEAR(CURDATE()) " +
                                "GROUP BY s.SpecializationName ORDER BY count DESC LIMIT 1");
            if (rs != null && rs.next()) {
                topSpecialization.setText(rs.getString("SpecializationName"));
            }

            // Fastest Growing (simplified: specialization with most appointments in current month)
            rs = Database.query("SELECT s.SpecializationName, COUNT(a.AppointmentID) AS count " +
                                "FROM appointment a " +
                                "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                                "JOIN ref_specialization s ON d.SpecializationID = s.SpecializationID " +
                                "WHERE MONTH(a.Date) = MONTH(CURDATE()) AND YEAR(a.Date) = YEAR(CURDATE()) " +
                                "GROUP BY s.SpecializationName ORDER BY count DESC LIMIT 1");
            if (rs != null && rs.next()) {
                fastestGrowing.setText(rs.getString("SpecializationName"));
            }

            // Active Specializations (unique specializations with appointments in current month)
            rs = Database.query("SELECT COUNT(DISTINCT s.SpecializationName) AS active " +
                                "FROM appointment a " +
                                "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                                "JOIN ref_specialization s ON d.SpecializationID = s.SpecializationID " +
                                "WHERE MONTH(a.Date) = MONTH(CURDATE()) AND YEAR(a.Date) = YEAR(CURDATE())");
            if (rs != null && rs.next()) {
                activeSpecializations.setText(String.valueOf(rs.getInt("active")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadChartData() {
        lineChart.getData().clear();
        xAxis.setLabel("Month");
        yAxis.setLabel("Number of Appointments");

        // Months
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (int i = 0; i < specializations.length; i++) {
            String spec = specializations[i];
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(spec);

            for (int month = 1; month <= 12; month++) {
                try {
                    ResultSet rs = Database.query("SELECT COUNT(a.AppointmentID) AS count " +
                                                  "FROM appointment a " +
                                                  "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                                                  "JOIN ref_specialization s ON d.SpecializationID = s.SpecializationID " +
                                                  "WHERE s.SpecializationName = ? AND MONTH(a.Date) = ? AND YEAR(a.Date) = YEAR(CURDATE())",
                                                  spec, month);
                    int count = 0;
                    if (rs != null && rs.next()) {
                        count = rs.getInt("count");
                    }
                    series.getData().add(new XYChart.Data<>(months[month - 1], count));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            lineChart.getData().add(series);

            // Set color for the series
            series.getNode().setStyle("-fx-stroke: " + colors[i] + "; -fx-stroke-width: 2px; -fx-fill: " + colors[i] + ";");
        }
    }

    /*
     =============SIDE PANEL FUNCTIONS==========================
      */
    @FXML
    public void AppointmentScreen(MouseEvent e) throws IOException{
        SceneManager.transition(e,"Appointments");
    }
    @FXML
    public void openPaymentScreen(MouseEvent e) throws IOException {
        SceneManager.transition(e, "PaymentProcessing");
    }
    @FXML
    public void openDoctorRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e, "DoctorRecord");
    }
    @FXML
    public void openMedicineManagement(MouseEvent e) throws IOException {
        SceneManager.transition(e, "MedicineManagement");
    }
    @FXML
    public void openPatientsRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e,"Patients");
    }

    @FXML
    public void openServicesRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e,"Services");
    }


    @FXML
    public void openIllnessesRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e,"Illness");
    }

    @FXML
    public void openSpecializationRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e,"SpecializationRecord");
    }

    @FXML
    public void openIllnessReport(MouseEvent e) throws IOException {
        SceneManager.transition(e,"IllnessReport");
    }

    @FXML
    public void openAppointmentReport(MouseEvent e) throws IOException {
        SceneManager.transition(e,"AppointmentReport");
    }

    @FXML
    public void openServiceRevenue(MouseEvent e) throws IOException {
        SceneManager.transition(e,"ServiceRevenueReport");
    }

    @FXML
    public void openSpecializationReport(MouseEvent e) throws IOException {
        SceneManager.transition(e,"Reports");
    }



    @FXML
    public void logout(MouseEvent e) throws IOException {
        SceneManager.transition(e,"login");
    }

    @FXML
    public void openDashboard(MouseEvent e) throws IOException {
        SceneManager.transition(e,"ADMINDashboard");
    }
}
