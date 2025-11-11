package Controller;

import Util.Alerts;
import Util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditAppointmentController {

    @FXML
    private TextField PatientField;

    @FXML
    private Button ValidatePatient;

    @FXML
    private Button ScheduleAppointment;

    @FXML
    private Label ResultText;

    @FXML
    private ComboBox<String> DoctorSelector;

    @FXML
    private DatePicker DateSelector;

    @FXML
    private ComboBox<LocalTime> TimeSelector;

    @FXML
    private TextField ReasonField;

    @FXML
    private Button Cancel;

    private List<Integer> doctorIds = new ArrayList<>();

    private List<Integer> patientIds = new ArrayList<>();

    private int APPID = 0;

    @FXML
    public void initialize() throws SQLException {
        loadDoctors();
        DoctorSelector.setOnAction(e -> updateAvailableTimes());
        DateSelector.setOnAction(e -> updateAvailableTimes());
        ResultText.setVisible(false); //hide this first
    }

    public void setAPPID(int APPOINTMENTID){
        this.APPID = APPOINTMENTID;
        loadAppointmentData();
    }


    private void loadAppointmentData() {
        String sql = "SELECT * FROM appointment WHERE appointmentID = ?";
        try {
            ResultSet rs = Database.query(sql, APPID);
            if (rs != null && rs.next()) {

                // fill up the forms for editing
                String patientID = rs.getString("PatientID");
                PatientField.setText("P-" + patientID); // add the P- prefix if needed


                ReasonField.setText(rs.getString("ReasonForVisit"));


                DateSelector.setValue(rs.getDate("Date").toLocalDate());


                int doctorId = rs.getInt("DoctorID");
                int doctorIndex = doctorIds.indexOf(doctorId);
                if (doctorIndex >= 0) {
                    DoctorSelector.getSelectionModel().select(doctorIndex);
                }


                LocalTime time = rs.getTime("Time").toLocalTime();

                updateAvailableTimes();
                TimeSelector.getSelectionModel().select(time);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Failed to load appointment data");
        }
    }





    @FXML
    public void validatePatient(ActionEvent event){
        String patientID = PatientField.getText().trim();

        if (patientID.toUpperCase().startsWith("P-")) {
            patientID = patientID.substring(2);
        }else{
            Alerts.Warning("Make sure patient ID starts with 'P-' ");
            return;
        }

        if(isPatientValid(Integer.parseInt(patientID))){
            ResultText.setText("Valid Patient");
            ResultText.setVisible(true);
            ResultText.setStyle("-fx-text-fill: #40D0E0;");

        } else {
            ResultText.setText("Invalid Patient");
            ResultText.setVisible(true);
            ResultText.setStyle("-fx-text-fill: #FF7F7F;");

        }

    }

    private boolean isPatientValid(int patientID) {
        System.out.println(patientID);
        String sql = "SELECT COUNT(*) AS count FROM patient WHERE patientID = ?";
        try {
            ResultSet result = Database.query(sql, patientID);

            if (result.next()) {
                return result.getInt("count") > 0;
            }

        } catch (SQLException e) {
            Alerts.Warning("Error validating patient: " + e.getMessage());
        }

        return false; // default to false if error or not found
    }


    private void loadDoctors() throws SQLException {
        String sql = "SELECT DoctorID, FirstName, LastName FROM doctor";
        ResultSet rs = Database.query(sql);

        ObservableList<String> doctors = FXCollections.observableArrayList();

        doctorIds.clear(); // reset
        while (rs.next()) {
            int id = rs.getInt("DoctorID");
            String name = rs.getString("FirstName") + " " + rs.getString("LastName");

            doctorIds.add(id);
            doctors.add(name);
        }

        DoctorSelector.setItems(doctors);
    }


    private void updateAvailableTimes() {
        int selectedIndex = DoctorSelector.getSelectionModel().getSelectedIndex();
        LocalDate selectedDate = DateSelector.getValue();

        if (selectedIndex < 0 || selectedDate == null) {
            TimeSelector.getItems().clear();
            return;
        }

        int doctorId = doctorIds.get(selectedIndex);

        try {
            // Pass current appointment ID so its time is not filtered out
            List<String> availableSlots = getAvailableSlots(doctorId, selectedDate, APPID);

            List<LocalTime> timeSlots = availableSlots.stream()
                    .map(slot -> LocalTime.parse(slot, DateTimeFormatter.ofPattern("HH:mm")))
                    .collect(Collectors.toList());

            TimeSelector.getItems().setAll(timeSlots);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }



    private List<String> generateTimeSlots() {
        List<String> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(17, 0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (!start.isAfter(end.minusHours(1))) { // Stop 1 hour before end
            slots.add(start.format(formatter));
            start = start.plusMinutes(30);
        }

        return slots;
    }

    private List<String> getAvailableSlots(int doctorId, LocalDate date, Integer excludeAppointmentID) throws SQLException {
        List<String> allSlots = generateTimeSlots();

        String sql = "SELECT Time, AppointmentID FROM appointment WHERE DoctorID = ? AND Date = ? AND Status != 'Canceled'";
        ResultSet rs = Database.query(sql, doctorId, date);

        List<String> bookedSlots = new ArrayList<>();
        while (rs.next()) {
            int appId = rs.getInt("AppointmentID");
            if (excludeAppointmentID != null && appId == excludeAppointmentID) continue; // Skip current appointment
            bookedSlots.add(rs.getTime("Time").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        return allSlots.stream()
                .filter(slot -> bookedSlots.stream().noneMatch(booked -> overlaps(slot, booked)))
                .collect(Collectors.toList());
    }



    private boolean overlaps(String slot, String booked) {
        LocalTime slotStart = LocalTime.parse(slot, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime slotEnd = slotStart.plusHours(1);
        LocalTime bookedStart = LocalTime.parse(booked, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime bookedEnd = bookedStart.plusHours(1);

        return !slotEnd.isBefore(bookedStart) && !slotStart.isAfter(bookedEnd);
    }

    @FXML
    private void AddAppointment() throws SQLException {
        LocalDate datetoday = LocalDate.now();
        LocalTime timeNow = LocalTime.now();
        String patientID = PatientField.getText().toUpperCase().trim();
        LocalDate date = DateSelector.getValue();
        LocalTime time = TimeSelector.getValue();
        String reason = ReasonField.getText();

        int selectedDoctorIndex = DoctorSelector.getSelectionModel().getSelectedIndex();

        StringBuilder errors = new StringBuilder();

        if(patientID.isEmpty()) {
            errors.append("PatientID is required!\n");
        } else if (patientID.startsWith("P-")) {
            patientID = patientID.substring(2); // remove "P-"
        }

        int patientIDInt = Integer.parseInt(patientID);

        if (!isPatientValid(Integer.parseInt(patientID))) {
            errors.append("You must input a valid patientID\n");
        }

        if(selectedDoctorIndex < 0){
            errors.append("Doctor is required!\n");
        }

        int doctorID = doctorIds.get(selectedDoctorIndex);

        if(date == null){
            errors.append("Date is required!\n");
        } else if (date.isBefore(datetoday)) {
            errors.append("Date cannot be before today!\n");
        } else if (date.isEqual(datetoday) && time != null && time.isBefore(timeNow)){
            errors.append("Time selected cannot be earlier than the current time!\n");
        }

        if(time == null){
            errors.append("Time is required!\n");
        }

        if(reason.isEmpty()){
            errors.append("Reason is required!\n");
        }

        if(hasOverlappingAppointment(patientIDInt, date, time, APPID)){
            errors.append("Patient has an overlapping schedule, please pick another time or date\n");
        }

        if (errors.length() > 0) {
            Alerts.Warning(errors.toString());
            return;
        }

        String sql = "UPDATE appointment SET PatientID = ?, DoctorID = ?, ReasonForVisit = ?, Date = ?, Time = ?, Status = ? WHERE AppointmentID = ?";
        Database.update(sql, patientIDInt, doctorID, reason, date, time, "Pending", APPID);


        Alerts.Info("Appointment details successfully edited!");
        Stage stage = (Stage) ScheduleAppointment.getScene().getWindow();
        stage.close();

        initialize();
    }

    private boolean hasOverlappingAppointment(int patientId, LocalDate date, LocalTime selectedTime, Integer excludeAppointmentID) throws SQLException {
        String sql = """
        SELECT AppointmentID, Time 
        FROM appointment 
        WHERE PatientID = ? 
          AND Date = ? 
          AND Status != 'Canceled'
    """;

        ResultSet rs = Database.query(sql, patientId, date);

        while (rs.next()) {
            int appId = rs.getInt("AppointmentID");
            if (excludeAppointmentID != null && appId == excludeAppointmentID) continue; // Skip the current appointment

            LocalTime bookedStart = rs.getTime("Time").toLocalTime();
            LocalTime bookedEnd = bookedStart.plusHours(1);
            LocalTime selectedEnd = selectedTime.plusHours(1);

            boolean overlaps = !selectedEnd.isBefore(bookedStart) && !selectedTime.isAfter(bookedEnd);
            if (overlaps) return true;
        }

        return false;
    }


    @FXML
    public void cancel(){
        Stage stage = (Stage) Cancel.getScene().getWindow();
        stage.close();
    }
}
