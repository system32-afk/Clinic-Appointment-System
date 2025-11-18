package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.sql.Date;
import java.time.format.TextStyle;
import java.util.*;

public class ServiceRevenueLineController {

        @FXML
        private Text Date;

        @FXML
        private Text Time;

        @FXML
        private ComboBox<String> yearFilter;
        @FXML
        private ComboBox<String> monthFilter;
        @FXML
        private ComboBox<String> serviceFilter;

        @FXML
        private LineChart<String, Number> revenueLineChart;
        @FXML
        private CategoryAxis xAxis;
        @FXML
        private NumberAxis yAxis;

        private FilteredList<ServiceRevenueRecord> FilteredData;
        private ObservableList<ServiceRevenueRecord> Data;

        private Connection conn;

        @FXML
        private void initialize() {

            Data = FXCollections.observableArrayList();

            // Sort unique values
            Set<String> years = new TreeSet<>();
            Set<String> months = new TreeSet<>();
            Set<String> services = new TreeSet<>();

            conn = Database.getConnection();

            try {
                String sql = "SELECT p.PaymentDate, " +
                        "s.ServiceName, " +
                        "p.AmountDue AS Revenue " +
                        "FROM payment p " +
                        "JOIN procedurerequest pr ON p.ProcedureID=pr.ProcedureID " +
                        "JOIN service s ON pr.ServiceID=s.ServiceID";

                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Date paymentDate = rs.getDate("PaymentDate");
                    LocalDate localDate = paymentDate.toLocalDate();

                    String year = String.valueOf(localDate.getYear());
                    String month = localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                    String service = rs.getString("ServiceName");
                    double revenue = rs.getDouble("Revenue");

                    Data.add(new ServiceRevenueRecord(year, month, service, revenue));

                    years.add(year);
                    months.add(month);
                    services.add(service);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            yearFilter.getItems().addAll(FXCollections.observableArrayList(years));
            monthFilter.getItems().addAll(FXCollections.observableArrayList(months));
            serviceFilter.getItems().addAll(FXCollections.observableArrayList(services));

            // Filter Data
            FilteredData = new FilteredList<>(Data, record -> true);

            yearFilter.valueProperty().addListener((observable, oldValue, newValue) -> {});
            monthFilter.valueProperty().addListener((observable, oldValue, newValue) -> {});
            serviceFilter.valueProperty().addListener((observable, oldValue, newValue) -> {});

            updateRevenueChart();

        }

        private void updateFilter() {
            String selectedYear = yearFilter.getValue();
            String selectedMonth = monthFilter.getValue();
            String selectedService = serviceFilter.getValue();

            FilteredData.setPredicate(record-> {
                boolean matchYear = selectedYear == null || selectedYear.isEmpty() || record.getYear().equals(selectedYear);
                boolean matchMonth = selectedMonth == null || selectedMonth.isEmpty() || record.getMonth().equals(selectedMonth);
                boolean matchService = selectedService == null || selectedService.isEmpty() || record.getService().equals(selectedService);
                return matchYear && matchMonth && matchService;
            });

            updateRevenueChart();

        }

        private Map<String, Double> aggregateMonthlyRevenue() {
            Map<String, Double> monthlyRevenue = new TreeMap<>();

            for(ServiceRevenueRecord record : FilteredData) {

                String monthName = record.getMonth();
                int monthNumber = Month.valueOf(monthName.toUpperCase(Locale.ENGLISH)).getValue();
                String sortMonth = String.format("%02d", monthNumber);

                String key = record.getYear() + "-" + sortMonth;

                double currentRevenue = monthlyRevenue.getOrDefault(key, 0.0);
                monthlyRevenue.put(key, currentRevenue + record.getRevenue());

            }
            return monthlyRevenue;

        }



        private void updateRevenueChart() {

            revenueLineChart.getData().clear();
            revenueLineChart.setTitle("Revenue Trend (Filtered Data)");
            yAxis.setLabel("Total Revenue");

            Map<String, Double> aggregatedData = aggregateMonthlyRevenue();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Revenue");

            for(Map.Entry<String, Double> entry : aggregatedData.entrySet()) {
                String sortKey = entry.getKey();
                Double totalRevenue = entry.getValue();

                String[] parts = sortKey.split("-");
                int monthNumber = Integer.parseInt(parts[1]);
                String monthName = Month.of(monthNumber).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

                String label = monthName + " " + parts[0];

                series.getData().add(new XYChart.Data<>(label, totalRevenue));

            }

            if(!series.getData().isEmpty()) {
                revenueLineChart.getData().add(series);
            }


        }

        private void updateDateTime() {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

            Date.setText(dateFormatter.format(now));
            Time.setText(timeFormatter.format(now));
        }

        public void RevenueReport(ActionEvent e) throws IOException {
            SceneManager.transition(e, "ServiceRevenueReport");
        }

        public void DashboardScreen(ActionEvent e) throws IOException {
            SceneManager.transition(e, "ADMINDashboard");
        }

        public void AppointmentScreen(ActionEvent e) throws IOException{
            SceneManager.transition(e,"Appointments");
        }





}
