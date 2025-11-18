package Controller;

import Util.Alerts;
import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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
    public void initialize() {

            Timeline clock = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> updateDateTime())
            );
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
            loadData();
    }

    private void loadData(){
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        StringBuilder top10cases = new StringBuilder();
        String [] colors = {"#4CAF50", "#2196F3", "#FF9800", "#9C27B0", "#F44336"};
        int i=0; // used for color rotation
        //top 5 results
        ResultSet top5 = Database.query(
                "SELECT i.IllnessName as Illness, COUNT(*) AS Cases " +
                        "FROM diagnosis d " +
                        "JOIN illness i ON d.illnessID = I.illnessID " +
                        "GROUP BY Illness " +
                        "ORDER BY Cases DESC " +
                        "LIMIT 5"
        );

        //honorable mentions LOL
        ResultSet top10 = Database.query(
                "SELECT i.IllnessName as Illness, COUNT(*) AS Cases " +
                        "FROM diagnosis d " +
                        "JOIN illness i ON d.illnessID = I.illnessID " +
                        "GROUP BY Illness " +
                        "ORDER BY Cases DESC " +
                        "LIMIT 5 OFFSET 5"
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

    public void DashboardScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "Dashboard");
    }

    public void openPaymentScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "PaymentProcessing");
    }




    @FXML
    private void searchTopAreas() throws SQLException {
        StringBuilder AreasAndIllnesses = new StringBuilder();
        String illness = illnessSearch.getText().trim();

        if (illness.isEmpty()) return;

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
                        "GROUP BY Area " +
                        "ORDER BY Cases DESC " +
                        "LIMIT 10";

        ResultSet rs = Database.query(sql, "%" + illness + "%");

        if (rs == null) {
            Alerts.Warning("Query failed. Please check SQL.");
            return;
        }

        while (rs.next()) {
            AreasAndIllnesses.append(
                    rs.getString("Area") + " - " + rs.getInt("Cases") + " cases\n"
            );
        }

        IllnessLocationText.setText(AreasAndIllnesses.toString());
    }


}
