package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
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
import java.time.format.DateTimeFormatter;
import java.sql.Date;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class ServiceRevenueReportController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private TableView<ServiceRevenueRecord> RevenueTable;
    @FXML
    private TableColumn<ServiceRevenueRecord, String> YearColumn;
    @FXML
    private TableColumn<ServiceRevenueRecord, String> MonthColumn;
    @FXML
    private TableColumn<ServiceRevenueRecord, String> ServiceColumn;
    @FXML
    private TableColumn<ServiceRevenueRecord, Double> RevenueColumn;

    @FXML
    private ComboBox<String> yearFilter;
    @FXML
    private ComboBox<String> monthFilter;
    @FXML
    private ComboBox<String> serviceFilter;
    @FXML
    private Label totalRevenueLabel;

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
        RevenueTable.setItems(FilteredData);

        updateTotalRevenue();
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

        updateTotalRevenue();

    }

    private void updateTotalRevenue() {

        double total =0;

        for(ServiceRevenueRecord record : FilteredData) {
            total += record.getRevenue();
        }

        totalRevenueLabel.setText(String.format("₱%,.2f", total));

    }



    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    public void LineChart(ActionEvent e) throws IOException {
        SceneManager.transition(e, "ServiceRevenueLine");
    }

    public void DashboardScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "ADMINDashboard");
    }

    public void AppointmentScreen(ActionEvent e) throws IOException{
        SceneManager.transition(e,"Appointments");
    }



}
