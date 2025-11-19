package Controller;

import Util.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreatePrescriptionController {

    @FXML
    private ComboBox<String> PatientComboBox;

    @FXML
    private ComboBox<String> DoctorComboBox;

    @FXML
    private ComboBox<String> ServiceComboBox; // Illness/Diagnosis

    @FXML
    private ComboBox<String> DoctorComboBox1; // Medicine

    @FXML
    private ComboBox<String> ServiceComboBox1; // Dosage

    @FXML
    private ComboBox<String> ServiceComboBox11; // Frequency

    @FXML
    private ComboBox<String> ServiceComboBox111; // Duration

    @FXML
    private TextArea NotesTextArea;

    @FXML
    private Button SubmitButton;

    @FXML
    private Button CancelButton;

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    private AdminPrescriptionController adminParentController;
    private DoctorPrescriptionController doctorParentController;

    public void setAdminParentController(AdminPrescriptionController parentController) {
        this.adminParentController = parentController;
    }

    public void setDoctorParentController(DoctorPrescriptionController parentController) {
        this.doctorParentController = parentController;
    }

    @FXML
    public void initialize() {
        populateComboBoxes();
        updateDateTime();
    }

    private void populateComboBoxes() {
        try {
            // Patients
            ResultSet rs = Database.query("SELECT PatientID, CONCAT(FirstName, ' ', LastName) AS PatientName FROM patient");
            while (rs != null && rs.next()) {
                PatientComboBox.getItems().add(rs.getString("PatientName"));
            }

            // Doctors
            rs = Database.query("SELECT DoctorID, CONCAT(FirstName, ' ', LastName) AS DoctorName FROM doctor");
            while (rs != null && rs.next()) {
                DoctorComboBox.getItems().add(rs.getString("DoctorName"));
            }

            // Illnesses (ServiceComboBox)
            rs = Database.query("SELECT IllnessID, IllnessName FROM illness");
            while (rs != null && rs.next()) {
                ServiceComboBox.getItems().add(rs.getString("IllnessName"));
            }

            // Medicines (DoctorComboBox1)
            rs = Database.query("SELECT MedicineID, MedicineName FROM medicine");
            while (rs != null && rs.next()) {
                DoctorComboBox1.getItems().add(rs.getString("MedicineName"));
            }

            // Dosage options
            ServiceComboBox1.getItems().addAll("1 Tablet", "2 Tablets", "3 Tablets", "1 Capsule", "2 Capsules");

            // Frequency options
            ServiceComboBox11.getItems().addAll("1x / day", "2x / day", "3x / day", "4x / day");

            // Duration options
            ServiceComboBox111.getItems().addAll("1 week", "2 weeks", "1 month", "2 months");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    @FXML
    private void submitRequest(ActionEvent event) {
        String selectedPatient = PatientComboBox.getValue();
        String selectedDoctor = DoctorComboBox.getValue();
        String selectedIllness = ServiceComboBox.getValue();
        String selectedMedicine = DoctorComboBox1.getValue();
        String dosage = ServiceComboBox1.getValue();
        String frequency = ServiceComboBox11.getValue();
        String duration = ServiceComboBox111.getValue();
        String instructions = NotesTextArea.getText();

        if (selectedPatient == null || selectedDoctor == null || selectedIllness == null ||
            selectedMedicine == null || dosage == null || frequency == null || duration == null ||
            instructions.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Data");
            alert.setHeaderText("Please fill all fields");
            alert.setContentText("All fields are required.");
            alert.showAndWait();
            return;
        }

        // Get IDs
        int patientID = -1, doctorID = -1, illnessID = -1, medicineID = -1;
        try {
            ResultSet rs = Database.query("SELECT PatientID FROM patient WHERE CONCAT(FirstName, ' ', LastName) = ?", selectedPatient);
            if (rs != null && rs.next()) patientID = rs.getInt("PatientID");

            rs = Database.query("SELECT DoctorID FROM doctor WHERE CONCAT(FirstName, ' ', LastName) = ?", selectedDoctor);
            if (rs != null && rs.next()) doctorID = rs.getInt("DoctorID");

            rs = Database.query("SELECT IllnessID FROM illness WHERE IllnessName = ?", selectedIllness);
            if (rs != null && rs.next()) illnessID = rs.getInt("IllnessID");

            rs = Database.query("SELECT MedicineID FROM medicine WHERE MedicineName = ?", selectedMedicine);
            if (rs != null && rs.next()) medicineID = rs.getInt("MedicineID");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Create appointment
        int appointmentID = -1;
        try {
            String insertAppointment = "INSERT INTO appointment (PatientID, DoctorID, ReasonForVisit, Date, Time, Status) VALUES (?, ?, ?, CURDATE(), CURTIME(), 'In-Progress')";
            int result = Database.update(insertAppointment, patientID, doctorID, selectedIllness);
            if (result > 0) {
                ResultSet rs = Database.query("SELECT LAST_INSERT_ID() AS ID");
                if (rs != null && rs.next()) {
                    appointmentID = rs.getInt("ID");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (appointmentID == -1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Appointment Creation Failed");
            alert.setContentText("Failed to create appointment.");
            alert.showAndWait();
            return;
        }

        // Insert prescription
        String fullDosage = dosage + ", " + frequency + ", for " + duration + " - " + instructions;
        try {
            String insertQuery = "INSERT INTO prescription (AppointmentID, IllnessID, MedicineID, Dosage) VALUES (?, ?, ?, ?)";
            int result = Database.update(insertQuery, appointmentID, illnessID, medicineID, fullDosage);
            if (result > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Prescription Created");
                alert.setContentText("The prescription has been created successfully.");
                alert.showAndWait();

                // Refresh parent tables
                if (adminParentController != null) {
                    adminParentController.loadData();
                    adminParentController.updateStatistics();
                }
                if (doctorParentController != null) {
                    doctorParentController.loadData();
                    doctorParentController.updateStatistics();
                }

                // Close dialog
                Stage stage = (Stage) SubmitButton.getScene().getWindow();
                stage.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Creation Failed");
            alert.setContentText("Failed to create the prescription: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void cancelRequest(ActionEvent event) {
        Stage stage = (Stage) CancelButton.getScene().getWindow();
        stage.close();
    }
}
