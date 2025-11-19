package Controller;

import Util.Alerts;
import Util.Database;
import Util.HoverPopup;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IllnessReportController {

    @FXML
    private BarChart casesGraph;

    @FXML
    private Text Date;
    @FXML
    private Text Time;

    @FXML
    private Text otherIllnesses;

    @FXML
    private ToggleButton citiesButton;

    @FXML
    private ToggleButton BarangayButton;

    @FXML
    private TextField illnessSearch;

    @FXML
    private Button IllnessSearchButton;

    @FXML
    private Text IllnessLocationText;



    private String FILTERBY = null;

    @FXML
    private Pane ManagementPane;

    @FXML
    private Pane RecordsManagementButton;

    @FXML
    private Pane ReportsButton;

    @FXML
    private Pane ReportsManagement;

    @FXML
    private DatePicker caseFromFilter;
    @FXML
    private DatePicker caseToFilter;

    @FXML
    private DatePicker LocFromFilter;
    @FXML
    private DatePicker LocToFilter;



    @FXML
    public void initialize() {

            Timeline clock = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> updateDateTime())
            );
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();


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

    }

    @FXML
    private void loadData(){
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        StringBuilder top10cases = new StringBuilder();
        String [] colors = {"#4CAF50", "#2196F3", "#FF9800", "#9C27B0", "#F44336"};
        int i=0; // used for color rotation

        if (caseFromFilter.getValue() == null){
            Alerts.Warning("Please select a start date");
            return;
        }
        if (caseToFilter.getValue() == null){
            Alerts.Warning("Please select an end date");
            return;
        }

        LocalDate sd = caseFromFilter.getValue(); //raw date
        LocalDate ed = caseToFilter.getValue();
        String startDate = sd.toString();
        String endDate = ed.toString();




        ResultSet top5 = Database.query(
                "SELECT i.IllnessName AS Illness, COUNT(*) AS Cases " +
                        "FROM diagnosis d " +
                        "JOIN illness i ON d.illnessID = i.illnessID " +
                        "WHERE d.dateDiagnosed BETWEEN ? AND ? " +
                        "GROUP BY i.IllnessName " +
                        "ORDER BY Cases DESC " +
                        "LIMIT 5",
                startDate, endDate
        );

        ResultSet top10 = Database.query(
                "SELECT i.IllnessName AS Illness, COUNT(*) AS Cases " +
                        "FROM diagnosis d " +
                        "JOIN illness i ON d.illnessID = i.illnessID " +
                        "WHERE d.dateDiagnosed BETWEEN ? AND ? " +
                        "GROUP BY i.IllnessName " +
                        "ORDER BY Cases DESC " +
                        "LIMIT 5 OFFSET 5",
                startDate, endDate
        );


        try {
            while(top5.next()){
                String illness = top5.getString("Illness");
                int count = top5.getInt("Cases");

                XYChart.Data<String, Number> data = new XYChart.Data<>(illness, count);
                series.getData().add(data);

                int colorIndex = i;
                // Set custom bar color
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-bar-fill:"+colors[colorIndex]+";"); // green bar, change color if needed
                    }

                });

                i++;
            }

            // Update chart after loop
            casesGraph.getData().clear();
            casesGraph.getData().add(series);
        }catch (SQLException e){
            Alerts.Warning("There was an error getting data for the bar chart");
        }



        try {
            while (top10.next()) {
                top10cases.append(top10.getString("Illness"))
                        .append(" - Cases: ")
                        .append(top10.getInt("CaseCount"))
                        .append("\n");
            }
            otherIllnesses.setText(top10cases.toString());
            System.out.println(top10cases);
        }catch (SQLException e){
            Alerts.Warning("There was an error getting the top 10 data.");
        }
    }


    @FXML
    public void filterBYBarangay(ActionEvent e){
        this.FILTERBY = "Barangay";
        BarangayButton.setOpacity(1);
        citiesButton.setOpacity(0.5);
    }

    @FXML
    public void filterBYCity(ActionEvent e){
        this.FILTERBY = "City";
        citiesButton.setOpacity(1);
        BarangayButton.setOpacity(0.5);
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }






    @FXML
    private void searchTopAreas() throws SQLException {


        if (LocFromFilter.getValue() == null){
            Alerts.Warning("Please input a start date");
            return;
        }

        if (LocToFilter.getValue() == null){
            Alerts.Warning("Please input an end date");
            return;
        }
        StringBuilder AreasAndIllnesses = new StringBuilder();
        String illness = illnessSearch.getText().trim();
        LocalDate sd = LocFromFilter.getValue();
        LocalDate ed = LocToFilter.getValue();
        String startDate = sd.toString();
        String endDate = ed.toString();

        if (illness.isEmpty()){
            Alerts.Warning("Please input an illness to search");
            return;
        };

        // Validate filter first
        if (FILTERBY == null || FILTERBY.isEmpty()) {
            Alerts.Warning("Please select either City or Barangay");
            return;
        }

        String areaColumn = FILTERBY.equals("City") ? "p.City" : "p.BarangayNo";

        String sql =
                "SELECT " + areaColumn + " AS Area, " +
                        "COUNT(*) AS Cases " +
                        "FROM diagnosis d " +
                        "JOIN appointment a ON d.AppointmentID = a.AppointmentID " +
                        "JOIN patient p ON a.PatientID = p.PatientID " +
                        "JOIN illness i ON d.IllnessID = i.IllnessID " +
                        "WHERE i.IllnessName LIKE ? " +
                        "AND d.DateDiagnosed BETWEEN ? AND ? " +
                        "GROUP BY Area " +
                        "ORDER BY Cases DESC " +
                        "LIMIT 10";

        ResultSet rs = Database.query(sql, "%" + illness + "%", startDate, endDate);

        if (rs == null) {
            Alerts.Warning("Query failed. Please check SQL.");
            return;
        }

        while (rs.next()) {
            if(FILTERBY.equals("Barangay")){
                AreasAndIllnesses.append(
                    "Barangay "+ rs.getString("Area") + " - " + rs.getInt("Cases") + " cases\n"
                );
                }
            else if (FILTERBY.equals("City")) {
                AreasAndIllnesses.append(
                        rs.getString("Area") + " - " + rs.getInt("Cases") + " cases\n"
                );
            }
        }

        IllnessLocationText.setText(AreasAndIllnesses.toString());
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
        //SceneManager.transition(e,"SpecializationReport");
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
