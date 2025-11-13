package Controller;

import Util.Alerts;
import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DoctorRecordController {

    @FXML
    private Text Date;

    @FXML
    private Label CompletedAppointments; // Total Doctors

    @FXML
    private Label InProgressAppointments; // Specialization count

    @FXML
    private Label CanceledAppointments; // Today's Appointments

    @FXML
    private Label InProgressAppointments1; // Active Patients

    @FXML
    private TextField SearchBar;

    @FXML
    private TableView<Doctor> DoctorTable;

    @FXML
    private TableColumn<Doctor, Integer> DoctorIDColumn;

    @FXML
    private TableColumn<Doctor, String> NameColumn;

    @FXML
    private TableColumn<Doctor, String> SexColumn;

    @FXML
    private TableColumn<Doctor, String> SpecializationColumn;

    @FXML
    private TableColumn<Doctor, String> ContactColumn;

    @FXML
    private TableColumn<Doctor, Integer> PatientsColumn;

    @FXML
    private TableColumn<Doctor, Button> ProfileColumn;

    @FXML
    private TableColumn<Doctor, Button> ActionColumn;

    @FXML
    public void initialize() {
        // Set up table columns
        DoctorIDColumn.setCellValueFactory(new PropertyValueFactory<>("doctorID"));
        NameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        SexColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
        SpecializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        ContactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        PatientsColumn.setCellValueFactory(new PropertyValueFactory<>("patients"));
        ProfileColumn.setCellValueFactory(new PropertyValueFactory<>("profileButton"));
        ActionColumn.setCellValueFactory(new PropertyValueFactory<>("actionButton"));

        // Load initial data
        loadData();
        updateStatistics();

        // Update date and time
        updateDateTime();
        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void loadData() {
        ObservableList<Doctor> doctors = FXCollections.observableArrayList();

        String query = "SELECT d.DoctorID, CONCAT(d.FirstName, ' ', d.LastName) AS Name, d.Sex, " +
                      "s.SpecializationName, d.Contact, " +
                      "COUNT(DISTINCT a.PatientID) AS Patients " +
                      "FROM doctor d " +
                      "LEFT JOIN specialization s ON d.SpecializationID = s.SpecializationID " +
                      "LEFT JOIN appointment a ON d.DoctorID = a.DoctorID " +
                      "GROUP BY d.DoctorID, d.FirstName, d.LastName, d.Sex, s.SpecializationName, d.Contact";

        try (ResultSet rs = Database.query(query)) {
            if (rs != null) {
                while (rs.next()) {
                    int doctorID = rs.getInt("DoctorID");
                    String name = rs.getString("Name");
                    String sex = rs.getString("Sex");
                    String specialization = rs.getString("SpecializationName");
                    String contact = rs.getString("Contact");
                    int patients = rs.getInt("Patients");

                    // Create buttons for Profile and Action
                    Button profileButton = new Button("View");
                    Button actionButton = new Button("Edit");

                    // Add action handlers
                    profileButton.setOnAction(e -> viewDoctorProfile(doctorID));
                    actionButton.setOnAction(e -> editDoctor(doctorID));

                    Doctor doctor = new Doctor(doctorID, name, sex, specialization, contact, patients, profileButton, actionButton);
                    doctors.add(doctor);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DoctorTable.setItems(doctors);
    }

    private void updateStatistics() {
        try {
            // Total Doctors
            ResultSet rs = Database.query("SELECT COUNT(*) AS total FROM doctor");
            if (rs != null && rs.next()) {
                CompletedAppointments.setText(String.valueOf(rs.getInt("total")));
            }

            // Specialization count (assuming this means unique specializations)
            rs = Database.query("SELECT COUNT(DISTINCT SpecializationID) AS specs FROM doctor WHERE SpecializationID IS NOT NULL");
            if (rs != null && rs.next()) {
                InProgressAppointments.setText(String.valueOf(rs.getInt("specs")));
            }

            // Today's Appointments
            rs = Database.query("SELECT COUNT(*) AS today FROM appointment WHERE Date = CURDATE()");
            if (rs != null && rs.next()) {
                CanceledAppointments.setText(String.valueOf(rs.getInt("today")));
            }

            // Active Patients (patients with appointments today or in progress)
            rs = Database.query("SELECT COUNT(DISTINCT PatientID) AS active FROM appointment WHERE Status IN ('Pending', 'In-Progress')");
            if (rs != null && rs.next()) {
                InProgressAppointments1.setText(String.valueOf(rs.getInt("active")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        Date.setText(dateFormatter.format(now));
    }

    @FXML
    private void Search() {
        String search = SearchBar.getText().trim();
        if (search.isEmpty()) {
            loadData();
            return;
        }

        ObservableList<Doctor> doctors = FXCollections.observableArrayList();

        String query = "SELECT d.DoctorID, CONCAT(d.FirstName, ' ', d.LastName) AS Name, d.Sex, " +
                      "s.SpecializationName, d.Contact, " +
                      "COUNT(DISTINCT a.PatientID) AS Patients " +
                      "FROM doctor d " +
                      "LEFT JOIN specialization s ON d.SpecializationID = s.SpecializationID " +
                      "LEFT JOIN appointment a ON d.DoctorID = a.DoctorID " +
                      "WHERE d.FirstName LIKE ? OR d.LastName LIKE ? OR s.SpecializationName LIKE ? " +
                      "GROUP BY d.DoctorID, d.FirstName, d.LastName, d.Sex, s.SpecializationName, d.Contact";

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            String searchLike = "%" + search + "%";
            stmt.setString(1, searchLike);
            stmt.setString(2, searchLike);
            stmt.setString(3, searchLike);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int doctorID = rs.getInt("DoctorID");
                String name = rs.getString("Name");
                String sex = rs.getString("Sex");
                String specialization = rs.getString("SpecializationName");
                int patients = rs.getInt("Patients");

                Button profileButton = new Button("View");
                Button actionButton = new Button("Edit");
                profileButton.setOnAction(e -> viewDoctorProfile(doctorID));
                actionButton.setOnAction(e -> editDoctor(doctorID));

                Doctor doctor = new Doctor(doctorID, name, sex, specialization, rs.getString("Contact"), patients, profileButton, actionButton);
                doctors.add(doctor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DoctorTable.setItems(doctors);
    }

    @FXML
    private void reloadData() {
        loadData();
        updateStatistics();
        SearchBar.clear();
    }

    @FXML
    private void AddDoctor(ActionEvent e) throws IOException {
        SceneManager.OpenPopup(e, "AddDoctor", "Add a Doctor");
    }

    private void viewDoctorProfile(int doctorID) {
        Alerts.Info("View profile for Doctor ID: " + doctorID);
    }

    private void editDoctor(int doctorID) {
        Alerts.Info("Edit doctor ID: " + doctorID);
    }

    // Inner class for Doctor data
    public static class Doctor {
        private final int doctorID;
        private final String name;
        private final String sex;
        private final String specialization;
        private final String contact;
        private final int patients;
        private final Button profileButton;
        private final Button actionButton;

        public Doctor(int doctorID, String name, String sex, String specialization, String contact, int patients, Button profileButton, Button actionButton) {
            this.doctorID = doctorID;
            this.name = name;
            this.sex = sex;
            this.specialization = specialization;
            this.contact = contact;
            this.patients = patients;
            this.profileButton = profileButton;
            this.actionButton = actionButton;
        }

        // Getters
        public int getDoctorID() { return doctorID; }
        public String getName() { return name; }
        public String getSex() { return sex; }
        public String getSpecialization() { return specialization; }
        public String getContact() { return contact; }
        public int getPatients() { return patients; }
        public Button getProfileButton() { return profileButton; }
        public Button getActionButton() { return actionButton; }
    }
}
