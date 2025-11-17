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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DoctorProceduresController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private Label CompletedAppointments; // Total Procedures

    @FXML
    private Label InProgressAppointments; // Scheduled

    @FXML
    private Label CanceledAppointments; // Completed

    @FXML
    private Label InProgressAppointments1; // Total Value

    @FXML
    private TextField SearchBar;

    @FXML
    private TableView<Procedure> ProcedureTable;

    @FXML
    private TableColumn<Procedure, Integer> ProcedureIDColumn;

    @FXML
    private TableColumn<Procedure, String> PatientColumn;

    @FXML
    private TableColumn<Procedure, String> DoctorColumn;

    @FXML
    private TableColumn<Procedure, String> ServiceColumn;

    @FXML
    private TableColumn<Procedure, String> DateColumn;

    @FXML
    private TableColumn<Procedure, Double> CostColumn;

    @FXML
    private TableColumn<Procedure, String> StatusColumn;

    @FXML
    private TableColumn<Procedure, HBox> ActionColumn;

    // Assume logged-in doctor ID is stored somewhere, e.g., in a session or static variable
    // For now, hardcode or get from login. Let's assume it's 2 for example.
    private int loggedInDoctorID = 2; // Replace with actual logic to get logged-in doctor ID

    @FXML
    public void initialize() {
        // Set up table columns
        ProcedureIDColumn.setCellValueFactory(new PropertyValueFactory<>("procedureID"));
        ProcedureIDColumn.setCellFactory(column -> new TableCell<Procedure, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("PROC" + String.format("%03d", item));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        PatientColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        PatientColumn.setCellFactory(column -> new TableCell<Procedure, String>() {
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
        DoctorColumn.setCellFactory(column -> new TableCell<Procedure, String>() {
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

        ServiceColumn.setCellValueFactory(new PropertyValueFactory<>("service"));
        ServiceColumn.setCellFactory(column -> new TableCell<Procedure, String>() {
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

        DateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        DateColumn.setCellFactory(column -> new TableCell<Procedure, String>() {
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

        CostColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        CostColumn.setCellFactory(column -> new TableCell<Procedure, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("₱" + String.format("%.2f", item));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        StatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        StatusColumn.setCellFactory(column -> new TableCell<Procedure, String>() {
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
        ActionColumn.setCellFactory(column -> new TableCell<Procedure, HBox>() {
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
        ObservableList<Procedure> procedures = FXCollections.observableArrayList();

        String query = "SELECT pr.ProcedureID, " +
                      "CONCAT(p.FirstName, ' ', p.LastName) AS Patient, " +
                      "CONCAT(d.FirstName, ' ', d.LastName) AS Doctor, " +
                      "s.ServiceName AS Service, " +
                      "pr.ProcedureDate AS Date, " +
                      "s.Price AS Cost, " +
                      "pr.Status AS Status " +
                      "FROM procedurerequest pr " +
                      "JOIN appointment a ON pr.AppointmentID = a.AppointmentID " +
                      "JOIN patient p ON a.PatientID = p.PatientID " +
                      "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                      "JOIN service s ON pr.ServiceID = s.ServiceID";

        try (ResultSet rs = Database.query(query)) {
            if (rs != null) {
                while (rs.next()) {
                    int procedureID = rs.getInt("ProcedureID");
                    String patient = rs.getString("Patient");
                    String doctor = rs.getString("Doctor");
                    String service = rs.getString("Service");
                    String date = rs.getString("Date");
                    double cost = rs.getDouble("Cost");
                    String status = rs.getString("Status");

                    // Create buttons for Action
                    Button viewButton = new Button("View");
                    Button editButton = new Button("Edit");

                    // Add action handlers
                    viewButton.setOnAction(e -> viewProcedure(procedureID));
                    editButton.setOnAction(e -> editProcedure(procedureID));

                    // Create HBox for action buttons
                    HBox actionHBox = new HBox(10, viewButton, editButton);
                    actionHBox.setAlignment(javafx.geometry.Pos.CENTER);

                    Procedure procedure = new Procedure(procedureID, patient, doctor, service, date, cost, status, actionHBox);
                    procedures.add(procedure);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ProcedureTable.setItems(procedures);
    }

    private void updateStatistics() {
        try {
            // Total Procedures
            ResultSet rs = Database.query("SELECT COUNT(*) AS total FROM procedurerequest");
            if (rs != null && rs.next()) {
                CompletedAppointments.setText(String.valueOf(rs.getInt("total")));
            }

            // Scheduled
            rs = Database.query("SELECT COUNT(*) AS scheduled FROM procedurerequest WHERE Status = 'Pending'");
            if (rs != null && rs.next()) {
                InProgressAppointments.setText(String.valueOf(rs.getInt("scheduled")));
            }

            // Completed
            rs = Database.query("SELECT COUNT(*) AS completed FROM procedurerequest WHERE Status = 'Completed'");
            if (rs != null && rs.next()) {
                CanceledAppointments.setText(String.valueOf(rs.getInt("completed")));
            }

            // Total Value
            rs = Database.query("SELECT SUM(s.Price) AS totalValue FROM procedurerequest pr JOIN service s ON pr.ServiceID = s.ServiceID");
            if (rs != null && rs.next()) {
                double totalValue = rs.getDouble("totalValue");
                InProgressAppointments1.setText("₱" + String.format("%.2f", totalValue));
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

        ObservableList<Procedure> procedures = FXCollections.observableArrayList();

        String query = "SELECT pr.ProcedureID, " +
                      "CONCAT(p.FirstName, ' ', p.LastName) AS Patient, " +
                      "CONCAT(d.FirstName, ' ', d.LastName) AS Doctor, " +
                      "s.ServiceName AS Service, " +
                      "pr.ProcedureDate AS Date, " +
                      "s.Price AS Cost, " +
                      "pr.Status AS Status " +
                      "FROM procedurerequest pr " +
                      "JOIN appointment a ON pr.AppointmentID = a.AppointmentID " +
                      "JOIN patient p ON a.PatientID = p.PatientID " +
                      "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                      "JOIN service s ON pr.ServiceID = s.ServiceID " +
                      "WHERE CONCAT(p.FirstName, ' ', p.LastName) LIKE ? OR " +
                      "CONCAT(d.FirstName, ' ', d.LastName) LIKE ? OR " +
                      "s.ServiceName LIKE ?";

        try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            String searchLike = "%" + search + "%";
            stmt.setString(1, searchLike);
            stmt.setString(2, searchLike);
            stmt.setString(3, searchLike);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int procedureID = rs.getInt("ProcedureID");
                String patient = rs.getString("Patient");
                String doctor = rs.getString("Doctor");
                String service = rs.getString("Service");
                String date = rs.getString("Date");
                double cost = rs.getDouble("Cost");
                String status = rs.getString("Status");

                Button viewButton = new Button("View");
                Button editButton = new Button("Edit");
                viewButton.setOnAction(e -> viewProcedure(procedureID));
                editButton.setOnAction(e -> editProcedure(procedureID));

                HBox actionHBox = new HBox(10, viewButton, editButton);
                actionHBox.setAlignment(javafx.geometry.Pos.CENTER);

                Procedure procedure = new Procedure(procedureID, patient, doctor, service, date, cost, status, actionHBox);
                procedures.add(procedure);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ProcedureTable.setItems(procedures);
    }

    @FXML
    private void reloadData() {
        loadData();
        updateStatistics();
        SearchBar.clear();
    }

    @FXML
    private void requestProcedure(ActionEvent e) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/RequestProcedure.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Request Procedure");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the table after requesting
            loadData();
            updateStatistics();
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open request form");
            alert.setContentText("Could not load the procedure request form: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void viewProcedure(int procedureID) {
        // Fetch procedure details
        String query = "SELECT pr.ProcedureID, CONCAT(p.FirstName, ' ', p.LastName) AS Patient, " +
                      "CONCAT(d.FirstName, ' ', d.LastName) AS Doctor, s.ServiceName AS Service, " +
                      "pr.ProcedureDate AS Date, s.Price AS Cost, pr.Status AS Status, pr.Notes " +
                      "FROM procedurerequest pr " +
                      "JOIN appointment a ON pr.AppointmentID = a.AppointmentID " +
                      "JOIN patient p ON a.PatientID = p.PatientID " +
                      "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                      "JOIN service s ON pr.ServiceID = s.ServiceID " +
                      "WHERE pr.ProcedureID = ?";

        try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            stmt.setInt(1, procedureID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String details = "Procedure ID: PROC" + String.format("%03d", rs.getInt("ProcedureID")) + "\n" +
                                 "Patient: " + rs.getString("Patient") + "\n" +
                                 "Doctor: " + rs.getString("Doctor") + "\n" +
                                 "Service: " + rs.getString("Service") + "\n" +
                                 "Date: " + rs.getString("Date") + "\n" +
                                 "Cost: ₱" + String.format("%.2f", rs.getDouble("Cost")) + "\n" +
                                 "Status: " + rs.getString("Status") + "\n" +
                                 "Notes: " + rs.getString("Notes");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Procedure Details");
                alert.setHeaderText("Details for Procedure PROC" + String.format("%03d", procedureID));
                alert.setContentText(details);
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load procedure details");
            alert.setContentText("Could not retrieve procedure details: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void editProcedure(int procedureID) {
        // Fetch current procedure data
        String selectQuery = "SELECT pr.AppointmentID, pr.ServiceID, pr.ProcedureDate, pr.Notes, " +
                            "a.PatientID, a.DoctorID, pr.Status " +
                            "FROM procedurerequest pr " +
                            "JOIN appointment a ON pr.AppointmentID = a.AppointmentID " +
                            "WHERE pr.ProcedureID = ?";
        final int[] appointmentID = {-1};
        int currentPatientID = -1;
        int currentDoctorID = -1;
        int currentServiceID = -1;
        LocalDate currentDate = null;
        String currentStatus = "";
        String currentNotes = "";
        try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(selectQuery)) {
            stmt.setInt(1, procedureID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                appointmentID[0] = rs.getInt("AppointmentID");
                currentPatientID = rs.getInt("PatientID");
                currentDoctorID = rs.getInt("DoctorID");
                currentServiceID = rs.getInt("ServiceID");
                currentDate = rs.getDate("ProcedureDate").toLocalDate();
                currentStatus = rs.getString("Status");
                currentNotes = rs.getString("Notes");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Procedure not found");
                alert.setContentText("Could not find procedure details.");
                alert.showAndWait();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load procedure details");
            alert.setContentText("Could not retrieve procedure details: " + e.getMessage());
            alert.showAndWait();
            return;
        }

        // Create dialog stage
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Procedure");
        dialogStage.setResizable(false);

        // Create form components
        Label patientLabel = new Label("Patient:");
        ComboBox<String> patientComboBox = new ComboBox<>();
        patientComboBox.setPromptText("Select Patient");

        Label doctorLabel = new Label("Doctor:");
        ComboBox<String> doctorComboBox = new ComboBox<>();
        doctorComboBox.setPromptText("Select Doctor");

        Label serviceLabel = new Label("Service:");
        ComboBox<String> serviceComboBox = new ComboBox<>();
        serviceComboBox.setPromptText("Select Service");

        Label dateLabel = new Label("Date:");
        DatePicker datePicker = new DatePicker();

        Label statusLabel = new Label("Status:");
        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
        statusChoiceBox.getItems().addAll("Pending", "Completed", "Canceled");
        statusChoiceBox.setValue("Pending");

        Label notesLabel = new Label("Notes:");
        TextArea notesTextArea = new TextArea();

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        // Populate ComboBoxes
        try {
            // Patients
            ResultSet rs = Database.query("SELECT PatientID, CONCAT(FirstName, ' ', LastName) AS Name FROM patient");
            while (rs != null && rs.next()) {
                patientComboBox.getItems().add(rs.getString("Name"));
                if (rs.getInt("PatientID") == currentPatientID) {
                    patientComboBox.setValue(rs.getString("Name"));
                }
            }

            // Doctors
            rs = Database.query("SELECT DoctorID, CONCAT(FirstName, ' ', LastName) AS Name FROM doctor");
            while (rs != null && rs.next()) {
                doctorComboBox.getItems().add(rs.getString("Name"));
                if (rs.getInt("DoctorID") == currentDoctorID) {
                    doctorComboBox.setValue(rs.getString("Name"));
                }
            }

            // Services
            rs = Database.query("SELECT ServiceID, ServiceName FROM service");
            while (rs != null && rs.next()) {
                serviceComboBox.getItems().add(rs.getString("ServiceName"));
                if (rs.getInt("ServiceID") == currentServiceID) {
                    serviceComboBox.setValue(rs.getString("ServiceName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set current values
        datePicker.setValue(currentDate);
        statusChoiceBox.setValue(currentStatus);
        notesTextArea.setText(currentNotes);

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(patientLabel, 0, 0);
        grid.add(patientComboBox, 1, 0);
        grid.add(doctorLabel, 0, 1);
        grid.add(doctorComboBox, 1, 1);
        grid.add(serviceLabel, 0, 2);
        grid.add(serviceComboBox, 1, 2);
        grid.add(dateLabel, 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(statusLabel, 0, 4);
        grid.add(statusChoiceBox, 1, 4);
        grid.add(notesLabel, 0, 5);
        grid.add(notesTextArea, 1, 5);

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        grid.add(buttonBox, 1, 6);

        Scene scene = new Scene(grid);
        dialogStage.setScene(scene);

        // Button actions
        saveButton.setOnAction(e -> {
            // Get selected IDs
            String selectedPatient = patientComboBox.getValue();
            String selectedDoctor = doctorComboBox.getValue();
            String selectedService = serviceComboBox.getValue();
            LocalDate selectedDate = datePicker.getValue();
            String selectedStatus = statusChoiceBox.getValue();
            String selectedNotes = notesTextArea.getText();

            if (selectedPatient == null || selectedDoctor == null || selectedService == null || selectedDate == null || selectedStatus == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Incomplete Data");
                alert.setHeaderText("Please fill all fields");
                alert.setContentText("All fields are required.");
                alert.showAndWait();
                return;
            }

            // Get IDs from names
            int patientID = -1, doctorID = -1, serviceID = -1;
            try {
                ResultSet rs = Database.query("SELECT PatientID FROM patient WHERE CONCAT(FirstName, ' ', LastName) = ?", selectedPatient);
                if (rs != null && rs.next()) patientID = rs.getInt("PatientID");

                rs = Database.query("SELECT DoctorID FROM doctor WHERE CONCAT(FirstName, ' ', LastName) = ?", selectedDoctor);
                if (rs != null && rs.next()) doctorID = rs.getInt("DoctorID");

                rs = Database.query("SELECT ServiceID FROM service WHERE ServiceName = ?", selectedService);
                if (rs != null && rs.next()) serviceID = rs.getInt("ServiceID");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // Update database
            try {
                // Update appointment
                String updateAppointment = "UPDATE appointment SET PatientID = ?, DoctorID = ?, Status = ? WHERE AppointmentID = ?";
                try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(updateAppointment)) {
                    stmt.setInt(1, patientID);
                    stmt.setInt(2, doctorID);
                    stmt.setString(3, selectedStatus);
                    stmt.setInt(4, appointmentID[0]);
                    stmt.executeUpdate();
                }

                // Update procedurerequest
                String updateProcedure = "UPDATE procedurerequest SET ServiceID = ?, ProcedureDate = ?, Notes = ?, Status = ? WHERE ProcedureID = ?";
                try (java.sql.PreparedStatement stmt = Database.getConnection().prepareStatement(updateProcedure)) {
                    stmt.setInt(1, serviceID);
                    stmt.setDate(2, java.sql.Date.valueOf(selectedDate));
                    stmt.setString(3, selectedNotes);
                    stmt.setString(4, selectedStatus);
                    stmt.setInt(5, procedureID);
                    stmt.executeUpdate();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Procedure Updated");
                alert.setContentText("The procedure has been updated successfully.");
                alert.showAndWait();

                // Refresh table
                loadData();
                updateStatistics();

                dialogStage.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Update Failed");
                alert.setContentText("Failed to update the procedure: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        cancelButton.setOnAction(e -> dialogStage.close());

        dialogStage.showAndWait();
    }

    // Inner class for Procedure data
    public static class Procedure {
        private final int procedureID;
        private final String patient;
        private final String doctor;
        private final String service;
        private final String date;
        private final double cost;
        private final String status;
        private final HBox actionHBox;

        public Procedure(int procedureID, String patient, String doctor, String service, String date, double cost, String status, HBox actionHBox) {
            this.procedureID = procedureID;
            this.patient = patient;
            this.doctor = doctor;
            this.service = service;
            this.date = date;
            this.cost = cost;
            this.status = status;
            this.actionHBox = actionHBox;
        }

        // Getters
        public int getProcedureID() { return procedureID; }
        public String getPatient() { return patient; }
        public String getDoctor() { return doctor; }
        public String getService() { return service; }
        public String getDate() { return date; }
        public double getCost() { return cost; }
        public String getStatus() { return status; }
        public HBox getActionHBox() { return actionHBox; }
    }
}
