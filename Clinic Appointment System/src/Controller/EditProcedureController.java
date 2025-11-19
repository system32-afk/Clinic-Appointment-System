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
import java.util.ArrayList;
import java.util.List;

public class EditProcedureController {



    @FXML
    private ComboBox<String> PatientComboBox;

    @FXML
    private ComboBox<String> DoctorComboBox;

    @FXML
    private ComboBox<String> ServiceComboBox;

    @FXML
    private ComboBox<String> StatusComboBox;



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

    private int procedureID;
    private int appointmentID;

    public void setProcedureID(int procedureID) {
        this.procedureID = procedureID;
        loadExistingData();
    }

    @FXML
    public void initialize() {
        loadPatients();
        loadDoctors();
        loadServices();
        loadStatusOptions();
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

    private void loadStatusOptions() {
        StatusComboBox.getItems().clear();
        StatusComboBox.getItems().addAll("Pending", "Completed", "Canceled");
    }

    private void loadExistingData() {
        String query = "SELECT pr.AppointmentID, pr.ServiceID, pr.ProcedureDate, pr.Notes, pr.Status, " +
                      "a.PatientID, a.DoctorID " +
                      "FROM procedurerequest pr " +
                      "JOIN appointment a ON pr.AppointmentID = a.AppointmentID " +
                      "WHERE pr.ProcedureID = ?";
        try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            stmt.setInt(1, procedureID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                appointmentID = rs.getInt("AppointmentID");
                int patientID = rs.getInt("PatientID");
                int doctorID = rs.getInt("DoctorID");
                int serviceID = rs.getInt("ServiceID");
                LocalDate date = rs.getDate("ProcedureDate").toLocalDate();
                String notes = rs.getString("Notes");
                String status = rs.getString("Status");

                // Set values in ComboBoxes
                for (int i = 0; i < patients.size(); i++) {
                    if (patients.get(i).id == patientID) {
                        PatientComboBox.getSelectionModel().select(i);
                        break;
                    }
                }
                for (int i = 0; i < doctors.size(); i++) {
                    if (doctors.get(i).id == doctorID) {
                        DoctorComboBox.getSelectionModel().select(i);
                        break;
                    }
                }
                for (int i = 0; i < services.size(); i++) {
                    if (services.get(i).id == serviceID) {
                        ServiceComboBox.getSelectionModel().select(i);
                        break;
                    }
                }

                ProcedureDatePicker.setValue(date);
                NotesTextArea.setText(notes);
                // Map 'Cancelled' to 'Canceled' to match database ENUM
                String mappedStatus = status.equals("Cancelled") ? "Canceled" : status;
                StatusComboBox.setValue(mappedStatus);
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
        String status = StatusComboBox.getValue();

        // Update appointment
        String updateAppointment = "UPDATE appointment SET PatientID = ?, DoctorID = ? WHERE AppointmentID = ?";
        try {
            Database.update(updateAppointment, patientID, doctorID, appointmentID);

            // Update procedure request
            String updateProcedure = "UPDATE procedurerequest SET ServiceID = ?, ProcedureDate = ?, Notes = ?, Status = ? WHERE ProcedureID = ?";
            int result = Database.update(updateProcedure, serviceID, procedureDate, notes, status, procedureID);

            if (result > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Procedure Updated");
                alert.setContentText("The procedure has been successfully updated.");
                alert.showAndWait();

                // Close the window
                Stage stage = (Stage) SubmitButton.getScene().getWindow();
                stage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Update Failed");
                alert.setContentText("Failed to update the procedure. Please try again.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database Error");
            alert.setContentText("An error occurred while updating the procedure: " + e.getMessage());
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
