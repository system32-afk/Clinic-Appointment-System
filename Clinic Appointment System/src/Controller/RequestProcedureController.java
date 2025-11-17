package Controller;

import Util.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestProcedureController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private ComboBox<String> PatientComboBox;

    @FXML
    private ComboBox<String> DoctorComboBox;

    @FXML
    private ComboBox<String> ServiceComboBox;

    @FXML
    private DatePicker ProcedureDatePicker;

    @FXML
    private TextArea NotesTextArea;

    @FXML
    private Button SubmitButton;

    @FXML
    private Button CancelButton;

    private List<Patient> patients = new ArrayList<>();
    private List<Doctor> doctors = new ArrayList<>();
    private List<Service> services = new ArrayList<>();

    @FXML
    public void initialize() {
        updateDateTime();
        loadPatients();
        loadDoctors();
        loadServices();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    private void loadPatients() {
        PatientComboBox.getItems().clear();
        patients.clear();

        String query = "SELECT PatientID, CONCAT(FirstName, ' ', LastName) AS Name, Age, Sex FROM patient";
        try (ResultSet rs = Database.query(query)) {
            if (rs != null) {
                while (rs.next()) {
                    int id = rs.getInt("PatientID");
                    String name = rs.getString("Name");
                    int age = rs.getInt("Age");
                    String sex = rs.getString("Sex");
                    patients.add(new Patient(id, name, age, sex));
                    PatientComboBox.getItems().add("APT" + String.format("%03d", id) + " - " + name + " (" + age + "/" + sex.charAt(0) + ")");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDoctors() {
        DoctorComboBox.getItems().clear();
        doctors.clear();

        String query = "SELECT DoctorID, CONCAT(FirstName, ' ', LastName) AS Name, SpecializationName FROM doctor d LEFT JOIN specialization s ON d.SpecializationID = s.SpecializationID";
        try (ResultSet rs = Database.query(query)) {
            if (rs != null) {
                while (rs.next()) {
                    int id = rs.getInt("DoctorID");
                    String name = rs.getString("Name");
                    String specialization = rs.getString("SpecializationName");
                    doctors.add(new Doctor(id, name, specialization));
                    DoctorComboBox.getItems().add("DR" + String.format("%03d", id) + " - " + name + " (" + (specialization != null ? specialization : "N/A") + ")");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadServices() {
        ServiceComboBox.getItems().clear();
        services.clear();

        String query = "SELECT ServiceID, ServiceName, Price FROM service";
        try (ResultSet rs = Database.query(query)) {
            if (rs != null) {
                while (rs.next()) {
                    int id = rs.getInt("ServiceID");
                    String name = rs.getString("ServiceName");
                    double price = rs.getDouble("Price");
                    services.add(new Service(id, name, price));
                    ServiceComboBox.getItems().add(name + " - ₱" + String.format("%.2f", price));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void submitRequest(ActionEvent event) {
        // Validate inputs
        if (PatientComboBox.getValue() == null || DoctorComboBox.getValue() == null ||
            ServiceComboBox.getValue() == null || ProcedureDatePicker.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Missing Information");
            alert.setContentText("Please fill in all required fields.");
            alert.showAndWait();
            return;
        }

        // Get selected IDs
        int patientIndex = PatientComboBox.getSelectionModel().getSelectedIndex();
        int doctorIndex = DoctorComboBox.getSelectionModel().getSelectedIndex();
        int serviceIndex = ServiceComboBox.getSelectionModel().getSelectedIndex();

        int patientID = patients.get(patientIndex).id;
        int doctorID = doctors.get(doctorIndex).id;
        int serviceID = services.get(serviceIndex).id;
        LocalDate procedureDate = ProcedureDatePicker.getValue();
        String notes = NotesTextArea.getText();

        // Create appointment first (assuming procedure is linked to appointment)
        String insertAppointment = "INSERT INTO appointment (PatientID, DoctorID, ReasonForVisit, Date, Time, Status) VALUES (?, ?, ?, ?, '08:00:00', 'Pending')";
        try {
            int appointmentID = Database.insertAndGetKey(insertAppointment, patientID, doctorID, "Procedure Request", procedureDate);

            if (appointmentID > 0) {
                // Insert procedure request
                String insertProcedure = "INSERT INTO procedurerequest (AppointmentID, ServiceID, ProcedureDate, Notes) VALUES (?, ?, ?, ?)";
                int result = Database.update(insertProcedure, appointmentID, serviceID, procedureDate, notes);

                if (result > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Procedure Requested");
                    alert.setContentText("The procedure has been successfully requested.");
                    alert.showAndWait();

                    // Close the window
                    Stage stage = (Stage) SubmitButton.getScene().getWindow();
                    stage.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Request Failed");
                    alert.setContentText("Failed to request the procedure. Please try again.");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Request Failed");
                alert.setContentText("Failed to create appointment. Please try again.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database Error");
            alert.setContentText("An error occurred while processing the request: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void cancelRequest(ActionEvent event) {
        Stage stage = (Stage) CancelButton.getScene().getWindow();
        stage.close();
    }

    // Inner classes for data models
    private static class Patient {
        int id;
        String name;
        int age;
        String sex;

        Patient(int id, String name, int age, String sex) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.sex = sex;
        }
    }

    private static class Doctor {
        int id;
        String name;
        String specialization;

        Doctor(int id, String name, String specialization) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
        }
    }

    private static class Service {
        int id;
        String name;
        double price;

        Service(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }
}
