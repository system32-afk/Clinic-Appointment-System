package Controller;

import Util.Database;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewProcedureController {

    @FXML
    private Text ProcedureIDText;

    @FXML
    private Text PatientText;

    @FXML
    private Text DoctorText;

    @FXML
    private Text ServiceText;

    @FXML
    private Text DateText;

    @FXML
    private Text CostText;

    @FXML
    private Text StatusText;

    @FXML
    private Text NotesText;

    private int procedureID;

    @FXML
    public void initialize() {
        // No initialization needed
    }

    public void setProcedureData(int procedureID) {
        this.procedureID = procedureID;
        loadProcedureData();
    }

    private void loadProcedureData() {
        try {
            String query = "SELECT pr.ProcedureID, CONCAT(p.FirstName, ' ', p.LastName) AS Patient, " +
                           "CONCAT(d.FirstName, ' ', d.LastName) AS Doctor, s.ServiceName AS Service, " +
                           "pr.ProcedureDate AS Date, s.Price AS Cost, pr.Status AS Status, pr.Notes " +
                           "FROM procedurerequest pr " +
                           "JOIN appointment a ON pr.AppointmentID = a.AppointmentID " +
                           "JOIN patient p ON a.PatientID = p.PatientID " +
                           "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                           "JOIN service s ON pr.ServiceID = s.ServiceID " +
                           "WHERE pr.ProcedureID = ?";
            ResultSet rs = Database.query(query, procedureID);

            if (rs != null && rs.next()) {
                ProcedureIDText.setText("PROC" + String.format("%03d", rs.getInt("ProcedureID")));
                PatientText.setText(rs.getString("Patient"));
                DoctorText.setText(rs.getString("Doctor"));
                ServiceText.setText(rs.getString("Service"));
                DateText.setText(rs.getString("Date"));
                CostText.setText("₱" + String.format("%.2f", rs.getDouble("Cost")));
                StatusText.setText(rs.getString("Status"));
                NotesText.setText(rs.getString("Notes") != null ? rs.getString("Notes") : "None");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelRequest() {
        ProcedureIDText.getScene().getWindow().hide();
    }
}
