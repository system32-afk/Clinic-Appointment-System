package Controller;

import Util.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EditPrescriptionController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private ComboBox<String> PatientComboBox;

    @FXML
    private ComboBox<String> DoctorComboBox;

    @FXML
    private ComboBox<String> ServiceComboBox; // Illness

    @FXML
    private ComboBox<String> DoctorComboBox1; // Medicine

    @FXML
    private ComboBox<String> ServiceComboBox1; // Dosage quantity

    @FXML
    private ComboBox<String> ServiceComboBox11; // Dosage frequency

    @FXML
    private ComboBox<String> ServiceComboBox111; // Dosage duration

    @FXML
    private ComboBox<String> ServiceComboBox1111; // Status

    @FXML
    private TextArea NotesTextArea; // Instructions

    private AdminPrescriptionController adminParentController;
    private DoctorPrescriptionController doctorParentController;
    private int prescriptionID;

    @FXML
    public void initialize() {
        // Update date and time
        updateDateTime();

        // Populate combo boxes
        populateComboBoxes();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    private void populateComboBoxes() {
        try {
            // Patients
            ResultSet rs = Database.query("SELECT CONCAT(FirstName, ' ', LastName) AS PatientName FROM patient");
            while (rs != null && rs.next()) {
                PatientComboBox.getItems().add(rs.getString("PatientName"));
            }

            // Doctors
            rs = Database.query("SELECT CONCAT(FirstName, ' ', LastName) AS DoctorName FROM doctor");
            while (rs != null && rs.next()) {
                DoctorComboBox.getItems().add(rs.getString("DoctorName"));
            }

            // Illnesses
            rs = Database.query("SELECT IllnessName FROM illness");
            while (rs != null && rs.next()) {
                ServiceComboBox.getItems().add(rs.getString("IllnessName"));
            }

            // Medicines
            rs = Database.query("SELECT MedicineName FROM medicine");
            while (rs != null && rs.next()) {
                DoctorComboBox1.getItems().add(rs.getString("MedicineName"));
            }

            // Dosage quantities
            ServiceComboBox1.getItems().addAll("1 Tablet", "2 Tablets", "3 Tablets", "4 Tablets", "5 Tablets");

            // Dosage frequencies
            ServiceComboBox11.getItems().addAll("1x a day", "2x a day", "3x a day", "4x a day", "5x a day");

            // Dosage durations
            ServiceComboBox111.getItems().addAll("1 day", "2 days", "3 days", "1 week", "2 weeks", "1 month");

            // Statuses
            ServiceComboBox1111.getItems().addAll("In-Progress", "Completed", "Canceled");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setPrescriptionData(int prescriptionID, String patient, String doctor, String illness, String medicine, String dosage, String status) {
        this.prescriptionID = prescriptionID;

        PatientComboBox.setValue(patient);
        DoctorComboBox.setValue(doctor);
        ServiceComboBox.setValue(illness);
        DoctorComboBox1.setValue(medicine);

        // Parse dosage string
        parseDosage(dosage);
    }

    private void parseDosage(String dosage) {
        // Parse dosage like "1 Tablet 2x/day for 1 week note"
        String[] parts = dosage.split(" for ");
        if (parts.length >= 2) {
            String beforeFor = parts[0];
            String afterFor = parts[1];

            String[] beforeParts = beforeFor.split(" ");
            if (beforeParts.length >= 3) {
                String quantity = beforeParts[0] + " " + beforeParts[1];
                String frequency = beforeParts[2].replace("/", " a ");

                ServiceComboBox1.setValue(quantity);
                ServiceComboBox11.setValue(frequency);
            }

            String[] afterParts = afterFor.split(" ");
            if (afterParts.length >= 2) {
                String duration = afterParts[0] + " " + afterParts[1];
                ServiceComboBox111.setValue(duration);

                // Instructions if present
                if (afterParts.length > 2) {
                    String instructions = String.join(" ", java.util.Arrays.copyOfRange(afterParts, 2, afterParts.length));
                    NotesTextArea.setText(instructions);
                }
            }
        }
    }

    public void setAdminParentController(AdminPrescriptionController controller) {
        this.adminParentController = controller;
    }

    public void setDoctorParentController(DoctorPrescriptionController controller) {
        this.doctorParentController = controller;
    }

    @FXML
    private void submitRequest() {
        String selectedPatient = PatientComboBox.getValue();
        String selectedDoctor = DoctorComboBox.getValue();
        String selectedIllness = ServiceComboBox.getValue();
        String selectedMedicine = DoctorComboBox1.getValue();
        String quantity = ServiceComboBox1.getValue();
        String frequency = ServiceComboBox11.getValue();
        String duration = ServiceComboBox111.getValue();
        String status = ServiceComboBox1111.getValue();
        String instructions = NotesTextArea.getText();

        if (selectedPatient == null || selectedDoctor == null || selectedIllness == null ||
            selectedMedicine == null || quantity == null || frequency == null || duration == null || status == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Data");
            alert.setHeaderText("Please fill all required fields");
            alert.setContentText("All fields are required.");
            alert.showAndWait();
            return;
        }

        // Construct dosage string
        String dosage = quantity + " " + frequency.replace(" / ", "/") + " for " + duration +
                       (instructions.isEmpty() ? "" : " " + instructions);

        // Get IDs
        int patientID = -1, doctorID = -1, illnessID = -1, medicineID = -1, appointmentID = -1;
        try {
            ResultSet rs = Database.query("SELECT PatientID FROM patient WHERE CONCAT(FirstName, ' ', LastName) = ?", selectedPatient);
            if (rs != null && rs.next()) patientID = rs.getInt("PatientID");

            rs = Database.query("SELECT DoctorID FROM doctor WHERE CONCAT(FirstName, ' ', LastName) = ?", selectedDoctor);
            if (rs != null && rs.next()) doctorID = rs.getInt("DoctorID");

            rs = Database.query("SELECT IllnessID FROM illness WHERE IllnessName = ?", selectedIllness);
            if (rs != null && rs.next()) illnessID = rs.getInt("IllnessID");

            rs = Database.query("SELECT MedicineID FROM medicine WHERE MedicineName = ?", selectedMedicine);
            if (rs != null && rs.next()) medicineID = rs.getInt("MedicineID");

            // Get appointment ID for this prescription
            rs = Database.query("SELECT AppointmentID FROM prescription WHERE PrescriptionID = ?", prescriptionID);
            if (rs != null && rs.next()) appointmentID = rs.getInt("AppointmentID");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Update database
        String updatePrescriptionQuery = "UPDATE prescription SET IllnessID = ?, MedicineID = ?, Dosage = ? WHERE PrescriptionID = ?";
        int prescriptionResult = Database.update(updatePrescriptionQuery, illnessID, medicineID, dosage, prescriptionID);

        String updateAppointmentQuery = "UPDATE appointment SET Status = ? WHERE AppointmentID = ?";
        int appointmentResult = Database.update(updateAppointmentQuery, status, appointmentID);

        if (prescriptionResult > 0 && appointmentResult > 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Prescription Updated");
            alert.setContentText("The prescription has been updated successfully.");
            alert.showAndWait();

            // Refresh parent table
            if (adminParentController != null) {
                adminParentController.loadData();
                adminParentController.updateStatistics();
            } else if (doctorParentController != null) {
                doctorParentController.loadData();
                doctorParentController.updateStatistics();
            }

            // Close dialog
            Stage stage = (Stage) PatientComboBox.getScene().getWindow();
            stage.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Update Failed");
            alert.setContentText("Failed to update the prescription. Please check the data and try again.");
            alert.showAndWait();
        }
    }

    @FXML
    private void cancelRequest() {
        Stage stage = (Stage) PatientComboBox.getScene().getWindow();
        stage.close();
    }
}
