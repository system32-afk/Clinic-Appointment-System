package Controller;

import Util.Database;
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
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminPrescriptionController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private Label CompletedAppointments; // Total Prescriptions

    @FXML
    private Label InProgressAppointments; // Active Prescriptions

    @FXML
    private Label CanceledAppointments; // Patients

    @FXML
    private Label InProgressAppointments1; // Prescribing Doctors

    @FXML
    private TextField SearchBar;

    @FXML
    private TableView<Prescription> PrescriptionTable;

    @FXML
    private TableColumn<Prescription, Integer> PrescriptionIDColumn;

    @FXML
    private TableColumn<Prescription, String> PatientColumn;

    @FXML
    private TableColumn<Prescription, String> DoctorColumn;

    @FXML
    private TableColumn<Prescription, String> IllnessColumn;

    @FXML
    private TableColumn<Prescription, String> MedicineColumn;

    @FXML
    private TableColumn<Prescription, String> DosageColumn;

    @FXML
    private TableColumn<Prescription, HBox> ActionColumn;

    @FXML
    private TableColumn<Prescription, String> StatusColumn;

    @FXML
    public void initialize() {
        // Set up table columns
        PrescriptionIDColumn.setCellValueFactory(new PropertyValueFactory<>("prescriptionID"));
        PrescriptionIDColumn.setCellFactory(column -> new TableCell<Prescription, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("PRE" + String.format("%03d", item));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        PatientColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        PatientColumn.setCellFactory(column -> new TableCell<Prescription, String>() {
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

        DoctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        DoctorColumn.setCellFactory(column -> new TableCell<Prescription, String>() {
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

        IllnessColumn.setCellValueFactory(new PropertyValueFactory<>("illness"));
        IllnessColumn.setCellFactory(column -> new TableCell<Prescription, String>() {
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

        MedicineColumn.setCellValueFactory(new PropertyValueFactory<>("medicine"));
        MedicineColumn.setCellFactory(column -> new TableCell<Prescription, String>() {
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

        DosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        DosageColumn.setCellFactory(column -> new TableCell<Prescription, String>() {
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

        StatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        StatusColumn.setCellFactory(column -> new TableCell<Prescription, String>() {
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

        ActionColumn.setCellValueFactory(new PropertyValueFactory<>("actionHBox"));
        ActionColumn.setCellFactory(column -> new TableCell<Prescription, HBox>() {
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

    public void loadData() {
        ObservableList<Prescription> prescriptions = FXCollections.observableArrayList();

        String query = "SELECT p.PrescriptionID, " +
                      "CONCAT(pt.FirstName, ' ', pt.LastName) AS Patient, " +
                      "CONCAT(d.FirstName, ' ', d.LastName) AS Doctor, " +
                      "i.IllnessName AS Illness, " +
                      "m.MedicineName AS Medicine, " +
                      "p.Dosage, " +
                      "a.Status " +
                      "FROM prescription p " +
                      "JOIN appointment a ON p.AppointmentID = a.AppointmentID " +
                      "JOIN patient pt ON a.PatientID = pt.PatientID " +
                      "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                      "JOIN illness i ON p.IllnessID = i.IllnessID " +
                      "JOIN medicine m ON p.MedicineID = m.MedicineID";

        try (ResultSet rs = Database.query(query)) {
            while (rs.next()) {
                int prescriptionID = rs.getInt("PrescriptionID");
                String patient = rs.getString("Patient");
                String doctor = rs.getString("Doctor");
                String illness = rs.getString("Illness");
                String medicine = rs.getString("Medicine");
                String dosage = rs.getString("Dosage");
                String status = rs.getString("Status");

                // Create buttons for Action
                Button viewButton = new Button("View");
                Button editButton = new Button("Edit");

                // Add action handlers
                viewButton.setOnAction(e -> viewPrescription(prescriptionID));
                editButton.setOnAction(e -> editPrescription(prescriptionID));

                // Create HBox for action buttons
                HBox actionHBox = new HBox(10, viewButton, editButton);
                actionHBox.setAlignment(javafx.geometry.Pos.CENTER);

                Prescription prescription = new Prescription(prescriptionID, patient, doctor, illness, medicine, dosage, status, actionHBox);
                prescriptions.add(prescription);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PrescriptionTable.setItems(prescriptions);
    }

    public void updateStatistics() {
        try {
            // Total Prescriptions
            ResultSet rs = Database.query("SELECT COUNT(*) AS total FROM prescription");
            if (rs != null && rs.next()) {
                CompletedAppointments.setText(String.valueOf(rs.getInt("total")));
            }

            // Active Prescriptions - count where status is 'In-Progress'
            rs = Database.query("SELECT COUNT(*) AS active FROM prescription p JOIN appointment a ON p.AppointmentID = a.AppointmentID WHERE a.Status = 'In-Progress'");
            if (rs != null && rs.next()) {
                InProgressAppointments.setText(String.valueOf(rs.getInt("active")));
            }

            // Patients
            rs = Database.query("SELECT COUNT(DISTINCT a.PatientID) AS patients FROM prescription p JOIN appointment a ON p.AppointmentID = a.AppointmentID");
            if (rs != null && rs.next()) {
                CanceledAppointments.setText(String.valueOf(rs.getInt("patients")));
            }

            // Prescribing Doctors
            rs = Database.query("SELECT COUNT(DISTINCT a.DoctorID) AS doctors FROM prescription p JOIN appointment a ON p.AppointmentID = a.AppointmentID");
            if (rs != null && rs.next()) {
                InProgressAppointments1.setText(String.valueOf(rs.getInt("doctors")));
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

        ObservableList<Prescription> prescriptions = FXCollections.observableArrayList();

        String query = "SELECT p.PrescriptionID, " +
                      "CONCAT(pt.FirstName, ' ', pt.LastName) AS Patient, " +
                      "CONCAT(d.FirstName, ' ', d.LastName) AS Doctor, " +
                      "i.IllnessName AS Illness, " +
                      "m.MedicineName AS Medicine, " +
                      "p.Dosage, " +
                      "a.Status " +
                      "FROM prescription p " +
                      "JOIN appointment a ON p.AppointmentID = a.AppointmentID " +
                      "JOIN patient pt ON a.PatientID = pt.PatientID " +
                      "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                      "JOIN illness i ON p.IllnessID = i.IllnessID " +
                      "JOIN medicine m ON p.MedicineID = m.MedicineID " +
                      "WHERE CONCAT(pt.FirstName, ' ', pt.LastName) LIKE ? OR " +
                      "CONCAT(d.FirstName, ' ', d.LastName) LIKE ? OR " +
                      "m.MedicineName LIKE ?";

        try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            String searchLike = "%" + search + "%";
            stmt.setString(1, searchLike);
            stmt.setString(2, searchLike);
            stmt.setString(3, searchLike);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int prescriptionID = rs.getInt("PrescriptionID");
                String patient = rs.getString("Patient");
                String doctor = rs.getString("Doctor");
                String illness = rs.getString("Illness");
                String medicine = rs.getString("Medicine");
                String dosage = rs.getString("Dosage");
                String status = rs.getString("Status");

                Button viewButton = new Button("View");
                Button editButton = new Button("Edit");
                viewButton.setOnAction(e -> viewPrescription(prescriptionID));
                editButton.setOnAction(e -> editPrescription(prescriptionID));

                HBox actionHBox = new HBox(10, viewButton, editButton);
                actionHBox.setAlignment(javafx.geometry.Pos.CENTER);

                Prescription prescription = new Prescription(prescriptionID, patient, doctor, illness, medicine, dosage, status, actionHBox);
                prescriptions.add(prescription);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PrescriptionTable.setItems(prescriptions);
    }

    @FXML
    private void reloadData() {
        loadData();
        updateStatistics();
        SearchBar.clear();
    }

    @FXML
    private void createPrescription(ActionEvent e) throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/CreatePrescription.fxml"));
        CreatePrescriptionController controller = new CreatePrescriptionController();
        loader.setController(controller);
        Parent root = loader.load();

        controller.setAdminParentController(this);

        // Create dialog stage
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Create New Prescription");
        dialogStage.setResizable(false);
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root));
        dialogStage.showAndWait();
    }

    private void viewPrescription(int prescriptionID) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/ViewPrescription.fxml"));
            ViewPrescriptionController controller = new ViewPrescriptionController();
            loader.setController(controller);
            Parent root = loader.load();

            // Fetch prescription details
            String query = "SELECT p.PrescriptionID, CONCAT(pt.FirstName, ' ', pt.LastName) AS Patient, " +
                          "CONCAT(d.FirstName, ' ', d.LastName) AS Doctor, i.IllnessName AS Illness, " +
                          "m.MedicineName AS Medicine, p.Dosage, a.Status " +
                          "FROM prescription p " +
                          "JOIN appointment a ON p.AppointmentID = a.AppointmentID " +
                          "JOIN patient pt ON a.PatientID = pt.PatientID " +
                          "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                          "JOIN illness i ON p.IllnessID = i.IllnessID " +
                          "JOIN medicine m ON p.MedicineID = m.MedicineID " +
                          "WHERE p.PrescriptionID = ?";

            try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
                stmt.setInt(1, prescriptionID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String patient = rs.getString("Patient");
                    String doctor = rs.getString("Doctor");
                    String illness = rs.getString("Illness");
                    String medicine = rs.getString("Medicine");
                    String dosage = rs.getString("Dosage");
                    String status = rs.getString("Status");

                    // Set prescription ID in the controller
                    controller.setPrescriptionData(prescriptionID);
                }
            }

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("View Prescription");
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load view");
            alert.setContentText("Could not load the prescription view: " + e.getMessage());
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load prescription details");
            alert.setContentText("Could not retrieve prescription details: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void editPrescription(int prescriptionID) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/EditPrescription.fxml"));
            EditPrescriptionController controller = new EditPrescriptionController();
            loader.setController(controller);
            Parent root = loader.load();

            controller.setAdminParentController(this);

            // Fetch current prescription data
            String selectQuery = "SELECT p.AppointmentID, p.IllnessID, p.MedicineID, p.Dosage, a.Status, " +
                                "i.IllnessName, m.MedicineName, " +
                                "CONCAT(pt.FirstName, ' ', pt.LastName) AS Patient, " +
                                "CONCAT(d.FirstName, ' ', d.LastName) AS Doctor " +
                                "FROM prescription p " +
                                "JOIN appointment a ON p.AppointmentID = a.AppointmentID " +
                                "JOIN patient pt ON a.PatientID = pt.PatientID " +
                                "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                                "JOIN illness i ON p.IllnessID = i.IllnessID " +
                                "JOIN medicine m ON p.MedicineID = m.MedicineID " +
                                "WHERE p.PrescriptionID = ?";

            try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(selectQuery)) {
                stmt.setInt(1, prescriptionID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String patient = rs.getString("Patient");
                    String doctor = rs.getString("Doctor");
                    String illness = rs.getString("IllnessName");
                    String medicine = rs.getString("MedicineName");
                    String dosage = rs.getString("Dosage");
                    String status = rs.getString("Status");

                    // Set data in the controller
                    controller.setPrescriptionData(prescriptionID, patient, doctor, illness, medicine, dosage, status);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Prescription not found");
                    alert.setContentText("Could not find prescription details.");
                    alert.showAndWait();
                    return;
                }
            }

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Prescription");
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load edit form");
            alert.setContentText("Could not load the prescription edit form: " + e.getMessage());
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load prescription details");
            alert.setContentText("Could not retrieve prescription details: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Inner class for Prescription data
    public static class Prescription {
        private final int prescriptionID;
        private final String patient;
        private final String doctor;
        private final String illness;
        private final String medicine;
        private final String dosage;
        private final String status;
        private final HBox actionHBox;

        public Prescription(int prescriptionID, String patient, String doctor, String illness, String medicine, String dosage, String status, HBox actionHBox) {
            this.prescriptionID = prescriptionID;
            this.patient = patient;
            this.doctor = doctor;
            this.illness = illness;
            this.medicine = medicine;
            this.dosage = dosage;
            this.status = status;
            this.actionHBox = actionHBox;
        }

        // Getters
        public int getPrescriptionID() { return prescriptionID; }
        public String getPatient() { return patient; }
        public String getDoctor() { return doctor; }
        public String getIllness() { return illness; }
        public String getMedicine() { return medicine; }
        public String getDosage() { return dosage; }
        public String getStatus() { return status; }
        public HBox getActionHBox() { return actionHBox; }
    }
}
