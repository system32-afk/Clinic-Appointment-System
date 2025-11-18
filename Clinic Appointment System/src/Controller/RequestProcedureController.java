package Controller;

import Util.Alerts;
import Util.Database;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class RequestProcedureController implements Initializable {

    @FXML private ComboBox<String> appointmentCombo;
    @FXML private ComboBox<String> serviceCombo;
    @FXML private DatePicker procedureDatePicker;
    @FXML private VBox serviceDetailsBox;
    @FXML private Label serviceNameLabel;
    @FXML private Label servicePriceLabel;
    @FXML private Button requestButton;
    @FXML private Button cancelButton;

    private Map<String, Integer> appointmentMap = new HashMap<>();
    private Map<String, Integer> serviceMap = new HashMap<>();
    private Map<String, Double> servicePriceMap = new HashMap<>();
    private PaymentProcessingController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAppointments();
        loadServices();
        setupEventListeners();

        procedureDatePicker.setValue(LocalDate.now());
    }

    private void loadAppointments() {
        try {
            String sql = """
                SELECT 
                    a.AppointmentID,
                    CONCAT(p.FirstName, ' ', p.LastName, ' - ', a.Date, ' ', a.Time) AS AppointmentInfo
                FROM appointment a
                JOIN patient p ON a.PatientID = p.PatientID
                WHERE a.Status IN ('Completed', 'In-Progress')
                ORDER BY a.Date DESC, a.Time DESC
            """;

            ResultSet rs = Database.query(sql);
            appointmentMap.clear();

            while (rs != null && rs.next()) {
                int appointmentID = rs.getInt("AppointmentID");
                String appointmentInfo = rs.getString("AppointmentInfo");
                appointmentMap.put(appointmentInfo, appointmentID);
            }

            appointmentCombo.setItems(FXCollections.observableArrayList(appointmentMap.keySet()));

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error loading appointments: " + e.getMessage());
        }
    }

    private void loadServices() {
        try {
            String sql = "SELECT ServiceID, ServiceName, Price FROM service ORDER BY ServiceName";
            ResultSet rs = Database.query(sql);

            serviceMap.clear();
            servicePriceMap.clear();

            while (rs != null && rs.next()) {
                int serviceID = rs.getInt("ServiceID");
                String serviceName = rs.getString("ServiceName");
                double price = rs.getDouble("Price");

                serviceMap.put(serviceName, serviceID);
                servicePriceMap.put(serviceName, price);
            }

            serviceCombo.setItems(FXCollections.observableArrayList(serviceMap.keySet()));

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error loading services: " + e.getMessage());
        }
    }

    private void setupEventListeners() {
        serviceCombo.setOnAction(e -> updateServiceDetails());
    }

    private void updateServiceDetails() {
        String selectedService = serviceCombo.getValue();
        if (selectedService != null && !selectedService.isEmpty()) {
            Double price = servicePriceMap.get(selectedService);
            if (price != null) {
                serviceNameLabel.setText(selectedService);
                servicePriceLabel.setText(String.format("₱%.2f", price));
                serviceDetailsBox.setVisible(true);
            }
        } else {
            serviceDetailsBox.setVisible(false);
        }
    }

    public void setParentController(PaymentProcessingController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void requestProcedure() {
        String selectedAppointment = appointmentCombo.getValue();
        String selectedService = serviceCombo.getValue();
        LocalDate procedureDate = procedureDatePicker.getValue();

        if (selectedAppointment == null || selectedAppointment.isEmpty()) {
            Alerts.Warning("Please select an appointment");
            return;
        }

        if (selectedService == null || selectedService.isEmpty()) {
            Alerts.Warning("Please select a service");
            return;
        }

        if (procedureDate == null) {
            Alerts.Warning("Please select a procedure date");
            return;
        }

        if (procedureDate.isAfter(LocalDate.now())) {
            Alerts.Warning("Procedure date cannot be in the future");
            return;
        }

        try {
            int appointmentID = appointmentMap.get(selectedAppointment);
            int serviceID = serviceMap.get(selectedService);

            String sql = "INSERT INTO procedurerequest (AppointmentID, ServiceID, ProcedureDate) VALUES (?, ?, ?)";
            int result = Database.update(sql, appointmentID, serviceID, procedureDate);

            if (result > 0) {
                Alerts.Info("Procedure requested successfully!");

                if (parentController != null) {
                    parentController.refreshData();
                }

                closeWindow();
            } else {
                Alerts.Warning("Failed to request procedure");
            }

        } catch (Exception e) {
            Alerts.Warning("Error requesting procedure: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) requestButton.getScene().getWindow();
        stage.close();
    }
}
