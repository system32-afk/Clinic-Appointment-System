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

            // Active Prescriptions - No Status column in table, set to 0
            InProgressAppointments.setText("0");

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
        Parent root = loader.load();

        // Get the controller
        CreatePrescriptionController controller = loader.getController();
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
                String details = "Prescription ID: PRE" + String.format("%03d", rs.getInt("PrescriptionID")) + "\n" +
                                 "Patient: " + rs.getString("Patient") + "\n" +
                                 "Doctor: " + rs.getString("Doctor") + "\n" +
                                 "Illness: " + rs.getString("Illness") + "\n" +
                                 "Medicine: " + rs.getString("Medicine") + "\n" +
                                 "Dosage: " + rs.getString("Dosage") + "\n" +
                                 "Status: " + rs.getString("Status");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Prescription Details");
                alert.setHeaderText("Details for Prescription PRE" + String.format("%03d", prescriptionID));
                alert.setContentText(details);
                alert.showAndWait();
            }
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
        final int[] currentAppointmentID = new int[1];
        final String[] currentIllness = new String[1];
        final String[] currentMedicine = new String[1];
        final String[] currentDosage = new String[1];
        final String[] currentStatus = new String[1];
        try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(selectQuery)) {
            stmt.setInt(1, prescriptionID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentAppointmentID[0] = rs.getInt("AppointmentID");
                currentIllness[0] = rs.getString("IllnessName");
                currentMedicine[0] = rs.getString("MedicineName");
                currentDosage[0] = rs.getString("Dosage");
                currentStatus[0] = rs.getString("Status");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Prescription not found");
                alert.setContentText("Could not find prescription details.");
                alert.showAndWait();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load prescription details");
            alert.setContentText("Could not retrieve prescription details: " + e.getMessage());
            alert.showAndWait();
            return;
        }

        // Create dialog stage
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Prescription");
        dialogStage.setResizable(false);

        // Create form components
        Label illnessLabel = new Label("Illness:");
        ComboBox<String> illnessComboBox = new ComboBox<>();
        illnessComboBox.setPromptText("Select Illness");

        Label medicineLabel = new Label("Medicine:");
        ComboBox<String> medicineComboBox = new ComboBox<>();
        medicineComboBox.setPromptText("Select Medicine");

        Label dosageLabel = new Label("Dosage:");
        TextField dosageTextField = new TextField();

        Label statusLabel = new Label("Status:");
        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
        statusChoiceBox.getItems().addAll("In-Progress", "Completed", "Canceled");

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        // Populate ComboBoxes
        try {
            // Illnesses
            ResultSet rs = Database.query("SELECT IllnessID, IllnessName FROM illness");
            while (rs != null && rs.next()) {
                illnessComboBox.getItems().add(rs.getString("IllnessName"));
            }

            // Medicines
            rs = Database.query("SELECT MedicineID, MedicineName FROM medicine");
            while (rs != null && rs.next()) {
                medicineComboBox.getItems().add(rs.getString("MedicineName"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Set current values
        illnessComboBox.setValue(currentIllness[0]);
        medicineComboBox.setValue(currentMedicine[0]);
        dosageTextField.setText(currentDosage[0]);
        statusChoiceBox.setValue(currentStatus[0]);

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(illnessLabel, 0, 0);
        grid.add(illnessComboBox, 1, 0);
        grid.add(medicineLabel, 0, 1);
        grid.add(medicineComboBox, 1, 1);
        grid.add(dosageLabel, 0, 2);
        grid.add(dosageTextField, 1, 2);
        grid.add(statusLabel, 0, 3);
        grid.add(statusChoiceBox, 1, 3);

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        grid.add(buttonBox, 1, 4);

        Scene scene = new Scene(grid);
        dialogStage.setScene(scene);

        // Button actions
        saveButton.setOnAction(e -> {
            String selectedIllness = illnessComboBox.getValue();
            String selectedMedicine = medicineComboBox.getValue();
            String dosage = dosageTextField.getText();
            String status = statusChoiceBox.getValue();

            if (selectedIllness == null || selectedMedicine == null || dosage.isEmpty() || status == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Incomplete Data");
                alert.setHeaderText("Please fill all fields");
                alert.setContentText("All fields are required.");
                alert.showAndWait();
                return;
            }

            // Get IDs
            int illnessID = -1, medicineID = -1;
            try {
                ResultSet rs = Database.query("SELECT IllnessID FROM illness WHERE IllnessName = ?", selectedIllness);
                if (rs != null && rs.next()) illnessID = rs.getInt("IllnessID");

                rs = Database.query("SELECT MedicineID FROM medicine WHERE MedicineName = ?", selectedMedicine);
                if (rs != null && rs.next()) medicineID = rs.getInt("MedicineID");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // Update database
            try {
                String updatePrescriptionQuery = "UPDATE prescription SET IllnessID = ?, MedicineID = ?, Dosage = ? WHERE PrescriptionID = ?";
                int prescriptionResult = Database.update(updatePrescriptionQuery, illnessID, medicineID, dosage, prescriptionID);

                String updateAppointmentQuery = "UPDATE appointment SET Status = ? WHERE AppointmentID = ?";
                int appointmentResult = Database.update(updateAppointmentQuery, status, currentAppointmentID[0]);

                if (prescriptionResult > 0 && appointmentResult > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Prescription Updated");
                    alert.setContentText("The prescription has been updated successfully.");
                    alert.showAndWait();

                    // Refresh table
                    loadData();
                    updateStatistics();

                    dialogStage.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Update Failed");
                alert.setContentText("Failed to update the prescription: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        cancelButton.setOnAction(e -> dialogStage.close());

        dialogStage.showAndWait();
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
