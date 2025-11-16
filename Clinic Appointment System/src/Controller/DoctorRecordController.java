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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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
    private Text Time;

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
    private TableColumn<Doctor, HBox> ActionColumn;

    @FXML
    public void initialize() {
        // Set up table columns
        DoctorIDColumn.setCellValueFactory(new PropertyValueFactory<>("doctorID"));
        DoctorIDColumn.setCellFactory(column -> new TableCell<Doctor, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("DR" + String.format("%03d", item));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        NameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        NameColumn.setCellFactory(column -> new TableCell<Doctor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        SexColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
        SexColumn.setCellFactory(column -> new TableCell<Doctor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        SpecializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        SpecializationColumn.setCellFactory(column -> new TableCell<Doctor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        ContactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        ContactColumn.setCellFactory(column -> new TableCell<Doctor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("N/A");
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        PatientsColumn.setCellValueFactory(new PropertyValueFactory<>("patients"));
        PatientsColumn.setCellFactory(column -> new TableCell<Doctor, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        ProfileColumn.setCellValueFactory(new PropertyValueFactory<>("profileButton"));
        ProfileColumn.setCellFactory(column -> new TableCell<Doctor, Button>() {
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        ActionColumn.setCellValueFactory(new PropertyValueFactory<>("actionHBox"));
        ActionColumn.setCellFactory(column -> new TableCell<Doctor, HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Load initial data
        loadData();
        updateStatistics();

        // Add search functionality to SearchBar
        SearchBar.setOnKeyReleased(event -> {
            Search();
        });

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
                      "GROUP_CONCAT(DISTINCT COALESCE(s2.SpecializationName, s1.SpecializationName) ORDER BY COALESCE(s2.SpecializationName, s1.SpecializationName) SEPARATOR ', ') AS Specializations, " +
                      "d.ContactNumber AS Contact, " +
                      "COUNT(DISTINCT a.PatientID) AS Patients " +
                      "FROM doctor d " +
                      "LEFT JOIN specialization s1 ON d.SpecializationID = s1.SpecializationID " +
                      "LEFT JOIN doctor_specialization ds ON d.DoctorID = ds.DoctorID " +
                      "LEFT JOIN specialization s2 ON ds.SpecializationID = s2.SpecializationID " +
                      "LEFT JOIN appointment a ON d.DoctorID = a.DoctorID " +
                      "GROUP BY d.DoctorID, d.FirstName, d.LastName, d.Sex, d.ContactNumber";

        try (ResultSet rs = Database.query(query)) {
            if (rs != null) {
                while (rs.next()) {
                    int doctorID = rs.getInt("DoctorID");
                    String name = rs.getString("Name");
                    String sex = rs.getString("Sex");
                    String specialization = rs.getString("Specializations");
                    String contact = rs.getString("Contact");
                    int patients = rs.getInt("Patients");

                    // Create buttons for Profile and Action
                    Button profileButton = new Button("View");
                    Button editButton = new Button("Edit");

                    // Add action handlers
                    profileButton.setOnAction(e -> viewDoctorProfile(doctorID));
                    editButton.setOnAction(e -> editDoctor(doctorID));

                    // Create HBox for action buttons
                    HBox actionHBox = new HBox(10, profileButton, editButton);
                    actionHBox.setAlignment(javafx.geometry.Pos.CENTER);

                    Doctor doctor = new Doctor(doctorID, name, sex, specialization, contact, patients, profileButton, actionHBox);
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
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
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
                      "GROUP_CONCAT(DISTINCT COALESCE(s2.SpecializationName, s1.SpecializationName) ORDER BY COALESCE(s2.SpecializationName, s1.SpecializationName) SEPARATOR ', ') AS Specializations, " +
                      "d.ContactNumber AS Contact, " +
                      "COUNT(DISTINCT a.PatientID) AS Patients " +
                      "FROM doctor d " +
                      "LEFT JOIN specialization s1 ON d.SpecializationID = s1.SpecializationID " +
                      "LEFT JOIN doctor_specialization ds ON d.DoctorID = ds.DoctorID " +
                      "LEFT JOIN specialization s2 ON ds.SpecializationID = s2.SpecializationID " +
                      "LEFT JOIN appointment a ON d.DoctorID = a.DoctorID " +
                      "WHERE d.FirstName LIKE ? OR d.LastName LIKE ? OR COALESCE(s2.SpecializationName, s1.SpecializationName) LIKE ? OR CAST(d.DoctorID AS CHAR) LIKE ? " +
                      "GROUP BY d.DoctorID, d.FirstName, d.LastName, d.Sex, d.ContactNumber";

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            String searchLike = "%" + search + "%";
            stmt.setString(1, searchLike);
            stmt.setString(2, searchLike);
            stmt.setString(3, searchLike);
            stmt.setString(4, searchLike);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int doctorID = rs.getInt("DoctorID");
                String name = rs.getString("Name");
                String sex = rs.getString("Sex");
                String specialization = rs.getString("Specializations");
                int patients = rs.getInt("Patients");

                Button profileButton = new Button("View");
                Button editButton = new Button("Edit");
                profileButton.setOnAction(e -> viewDoctorProfile(doctorID));
                editButton.setOnAction(e -> editDoctor(doctorID));

                // Create HBox for action buttons
                HBox actionHBox = new HBox(10, profileButton, editButton);
                actionHBox.setAlignment(javafx.geometry.Pos.CENTER);

                Doctor doctor = new Doctor(doctorID, name, sex, specialization, rs.getString("Contact"), patients, profileButton, actionHBox);
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/ViewProfile.fxml"));
            Parent root = loader.load();

            ViewProfileController controller = loader.getController();
            controller.setDoctorData(doctorID);

            Stage stage = new Stage();
            stage.setTitle("Doctor Profile");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerts.Warning("Failed to load doctor profile: " + e.getMessage());
        }
    }

    private void editDoctor(int doctorID) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/EditDoctor.fxml"));
            Parent root = loader.load();

            EditDoctorController controller = loader.getController();
            controller.setDoctorData(doctorID);

            Stage stage = new Stage();
            stage.setTitle("Edit Doctor");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Use showAndWait to wait for the edit window to close

            // Refresh the table after editing
            loadData();
            updateStatistics();
        } catch (IOException e) {
            e.printStackTrace();
            Alerts.Warning("Failed to load edit doctor form: " + e.getMessage());
        }
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
        private final HBox actionHBox;

        public Doctor(int doctorID, String name, String sex, String specialization, String contact, int patients, Button profileButton, HBox actionHBox) {
            this.doctorID = doctorID;
            this.name = name;
            this.sex = sex;
            this.specialization = specialization;
            this.contact = contact;
            this.patients = patients;
            this.profileButton = profileButton;
            this.actionHBox = actionHBox;
        }

        // Getters
        public int getDoctorID() { return doctorID; }
        public String getName() { return name; }
        public String getSex() { return sex; }
        public String getSpecialization() { return specialization; }
        public String getContact() { return contact; }
        public int getPatients() { return patients; }
        public Button getProfileButton() { return profileButton; }
        public HBox getActionHBox() { return actionHBox; }
    }
}
