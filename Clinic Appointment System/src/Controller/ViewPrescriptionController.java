package Controller;

import Util.Database;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ViewPrescriptionController {

    @FXML
    private Text Date1;

    @FXML
    private Text PatientText;

    @FXML
    private Text DoctorText;

    @FXML
    private Text IllnessText;

    @FXML
    private Text MedicineText;

    @FXML
    private Text DosageText;

    @FXML
    private Text FrequencyText;

    @FXML
    private Text DurationText;

    @FXML
    private Text InstructionsText;

    @FXML
    private Text PrescriptionIDText;

    @FXML
    private Text StatusText;

    private int prescriptionID;

    @FXML
    public void initialize() {
        // Update date and time
        updateDateTime();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Date1.setText(timeFormatter.format(now));
    }

    public void setPrescriptionData(int prescriptionID) {
        this.prescriptionID = prescriptionID;
        loadPrescriptionData();
    }

    private void loadPrescriptionData() {
        try {
            String query = "SELECT p.PrescriptionID, CONCAT(pt.FirstName, ' ', pt.LastName) AS Patient, " +
                           "CONCAT(d.FirstName, ' ', d.LastName) AS Doctor, i.IllnessName AS Illness, " +
                           "m.MedicineName AS Medicine, p.Dosage, a.Status, a.Date, a.Time " +
                           "FROM prescription p " +
                           "JOIN appointment a ON p.AppointmentID = a.AppointmentID " +
                           "JOIN patient pt ON a.PatientID = pt.PatientID " +
                           "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                           "JOIN illness i ON p.IllnessID = i.IllnessID " +
                           "JOIN medicine m ON p.MedicineID = m.MedicineID " +
                           "WHERE p.PrescriptionID = ?";
            ResultSet rs = Database.query(query, prescriptionID);

            if (rs != null && rs.next()) {
                PatientText.setText(rs.getString("Patient"));
                DoctorText.setText(rs.getString("Doctor"));
                IllnessText.setText(rs.getString("Illness"));
                MedicineText.setText(rs.getString("Medicine"));

                // Parse dosage
                String dosage = rs.getString("Dosage");
                parseDosage(dosage);

                // Set prescription ID and created date (using appointment date and time)
                PrescriptionIDText.setText("PRE" + String.format("%03d", prescriptionID));
                java.sql.Date appointmentDate = rs.getDate("Date");
                java.sql.Time appointmentTime = rs.getTime("Time");
                if (appointmentDate != null && appointmentTime != null) {
                    LocalDateTime createdDate = LocalDateTime.of(appointmentDate.toLocalDate(), appointmentTime.toLocalTime());
                    Date1.setText("Created: " + createdDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm:ss a")));
                } else {
                    Date1.setText("Created: N/A");
                }

                // Set status
                StatusText.setText(rs.getString("Status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void parseDosage(String dosage) {
        DosageText.setText("N/A");
        FrequencyText.setText("N/A");
        DurationText.setText("N/A");
        InstructionsText.setText("None");

        String[] parts = dosage.split(" ");
        if (parts.length >= 2) {
            DosageText.setText(parts[0] + " " + parts[1]);
        }
        if (parts.length >= 3) {
            FrequencyText.setText(parts[2].replace("/", " / "));
        }

        // Find "for" to parse duration and instructions
        int forIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("for")) {
                forIndex = i;
                break;
            }
        }
        if (forIndex != -1 && forIndex + 2 < parts.length) {
            DurationText.setText(parts[forIndex + 1] + " " + parts[forIndex + 2]);
            if (forIndex + 3 < parts.length) {
                InstructionsText.setText(String.join(" ", java.util.Arrays.copyOfRange(parts, forIndex + 3, parts.length)));
            }
        }
    }

    @FXML
    private void cancelRequest() {
        Date1.getScene().getWindow().hide();
    }
}
