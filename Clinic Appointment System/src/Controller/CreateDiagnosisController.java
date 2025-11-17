package Controller;

import Util.Alerts;
import Util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateDiagnosisController {

    @FXML
    private TextField AppointmentField;

    @FXML
    private Button ValidateAppointment;

    @FXML
    private Label ResultText;

    @FXML
    private Label PatientIDLabel;

    @FXML
    private Label PatientNameLabel;

    @FXML
    private ComboBox<String> IllnessSelector;

    @FXML
    private DatePicker DateSelector;

    private List<Integer> IllnessList = new ArrayList<>();

    @FXML
    private Button Cancel;

    @FXML
    private Button AddDiagnosis;

    @FXML
    public void initialize() throws SQLException {
        loadIllness();

        DateSelector.setValue(LocalDate.now());

        DateSelector.setDayCellFactory(picker -> new DateCell() {
            @Override
                public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if(date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });

        // Hide labels
        ResultText.setVisible(false);
        PatientIDLabel.setVisible(false);
        PatientNameLabel.setVisible(false);
    }

    private void loadPatientInfo(int appointmentID) {

        try {
            Connection conn = Database.getConnection();

            String sql = "SELECT p.PatientID, " +
                         "CONCAT(p.FirstName, ' ', p.LastName) AS PatientName " +
                         "FROM appointment a " +
                            "JOIN patient p ON a.PatientID=p.PatientID " +
                         "WHERE a.AppointmentID = ?";

            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setInt(1, appointmentID);

            ResultSet rs = stm.executeQuery();

            if(rs.next()) {
                PatientIDLabel.setText(rs.getString("PatientID"));
                PatientNameLabel.setText(rs.getString("PatientName"));

            } else {
                PatientIDLabel.setText("N/A");
                PatientNameLabel.setText("N/A");
            }

            PatientIDLabel.setVisible(true);
            PatientNameLabel.setVisible(true);

        } catch(SQLException e) {
            e.printStackTrace();
        }

    }

    public void validateAppointment(ActionEvent event){
        String appointmentID = AppointmentField.getText().trim();

        try {
            if(isAppointmentValid(Integer.parseInt(appointmentID))){
                ResultText.setText("Valid Appointment");
                ResultText.setVisible(true);
                ResultText.setStyle("-fx-text-fill: #40D0E0;");

                loadPatientInfo(Integer.parseInt(appointmentID));

            } else {
                ResultText.setText("Invalid Appointment");
                ResultText.setVisible(true);
                ResultText.setStyle("-fx-text-fill: #FF7F7F;");

                PatientIDLabel.setText("N/A");
                PatientNameLabel.setText("N/A");

            }
        } catch (NumberFormatException e) {
            ResultText.setText("Appointment ID should be an number");
            ResultText.setVisible(true);
            ResultText.setStyle("-fx-text-fill: #FF7F7F;");

            PatientIDLabel.setText("N/A");
            PatientNameLabel.setText("N/A");
        }


    }

    private boolean isAppointmentValid(int appointmentID) {
        String sql = "SELECT COUNT(*) AS count FROM appointment WHERE appointmentID = ?";
        try {
            ResultSet result = Database.query(sql, appointmentID);

            if (result.next()) {
                return result.getInt("count") > 0;
            }

        } catch (SQLException e) {
            Alerts.Warning("Error validating appointment: " + e.getMessage());
        }

        return false; // default to false if error or not found
    }

    private void loadIllness() throws SQLException {
        String sql = "SELECT IllnessID, IllnessName FROM illness";
        ResultSet rs = Database.query(sql);

        ObservableList<String> illness = FXCollections.observableArrayList();

        IllnessList.clear(); // reset
        while (rs.next()) {
            int id = rs.getInt("IllnessID");
            String name = rs.getString("IllnessName");

            IllnessList.add(id);
            illness.add(name);
        }

        IllnessSelector.setItems(illness);
    }

    @FXML
    private void Diagnose(ActionEvent event) throws SQLException {

        StringBuilder errors = new StringBuilder();

        // Date - today if default
        LocalDate selectedDate = DateSelector.getValue();

        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }


        // Appointment ID
        int AppointmentID = 0;
        try {
            AppointmentID = Integer.parseInt(AppointmentField.getText().trim());
        } catch (NumberFormatException e) {
            errors.append("Enter a valid Appointment ID");
        }

        int IllnessIndex = IllnessSelector.getSelectionModel().getSelectedIndex();

        if (IllnessIndex < 0) {
            errors.append("Please select a valid Illness");
        }

        if (!errors.isEmpty()) {
            Alerts.Warning(errors.toString());
            return;
        }

        int IllnessID = IllnessList.get(IllnessIndex);

        String sql = "INSERT INTO diagnosis (AppointmentID, IllnessID, DateDiagnosed) " +
                "VALUES (?, ?, ?)";

        Database.update(sql, AppointmentID, IllnessID, selectedDate);

        Alerts.Info("Diagnosis Added Successfully");
        Stage stage = (Stage) AddDiagnosis.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cancel(){
        Stage stage = (Stage) Cancel.getScene().getWindow();
        stage.close();
    }

}
