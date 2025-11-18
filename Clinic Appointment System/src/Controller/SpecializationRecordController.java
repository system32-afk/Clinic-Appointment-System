package Controller;

import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SpecializationRecordController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private VBox SpecializationRows;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button addSpecializationButton;

    @FXML
    private Label TotalSpecializationCount;

    @FXML
    public void initialize() {
        updateDateTime();

        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        loadData();
    }

    private void loadData() {
        try {
            ResultSet SpecializationData = Database.query(
                    "SELECT " +
                            "s.SpecializationID, " +
                            "s.SpecializationName " +
                            "FROM ref_specialization s"
            );

            SpecializationRows.getChildren().clear();
            scrollPane.setFitToWidth(true);
            int rowIndex = 0;

            while (SpecializationData.next()) {

                int SpecializationID = SpecializationData.getInt("SpecializationID");
                String SpecializationName = SpecializationData.getString("SpecializationName");

                // HBox for each row
                HBox row = new HBox(40);
                row.setPadding(new Insets(8, 20, 8, 40));
                row.setAlignment(Pos.BASELINE_LEFT);

                // === Add cells ===
                Label idLabel = new Label(String.valueOf(SpecializationID));
                Label nameLabel = new Label(SpecializationName);

                // Adjust margins
                idLabel.setPrefWidth(100);
                nameLabel.setPrefWidth(200);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(idLabel, nameLabel, spacer);

                // === Add separator line below each row ===
                Separator separator = new Separator();
                separator.prefWidthProperty().bind(SpecializationRows.widthProperty());

                SpecializationRows.getChildren().addAll(row, separator);

                rowIndex++;
            }

            TotalSpecializationCount.setText(String.valueOf(rowIndex));

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
    private Label statusLabel;

    @FXML
    private void AddSpecialization(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add new Specialization");
        dialog.setHeaderText("Enter the Specialization Name");
        dialog.setContentText("Specialization Name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(specializationName -> {
            specializationName = specializationName.trim();

            if(specializationName.isEmpty()) {
                statusLabel.setText("Specialization name cannot be empty");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            String checkQuery = "SELECT * FROM ref_specialization WHERE SpecializationName = ?";
            ResultSet rs = Database.query(checkQuery, specializationName);

            try {
                if(rs.next()) {
                    statusLabel.setText("Specialization already exists");
                    statusLabel.setStyle("-fx-text-fill: red;");
                } else {

                    String query = "INSERT INTO ref_specialization (SpecializationName) VALUES (?)";
                    Database.update(query, specializationName);

                    loadData();
                    statusLabel.setText("Specialization added successfully");
                    statusLabel.setStyle("-fx-text-fill: green;");

                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(e-> statusLabel.setVisible(false));
                    pause.play();

                }

            } catch (Exception e) {
                statusLabel.setText("Database error: could not add Specialization");
                statusLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }


        });

    }


    /*
    =============SIDE PANEL FUNCTIONS==========================
     */
    @FXML
    public void AppointmentScreen(MouseEvent e) throws IOException{
        SceneManager.transition(e,"Appointments");
    }
    @FXML
    public void openPaymentScreen(MouseEvent e) throws IOException {
        SceneManager.transition(e, "PaymentProcessing");
    }
    @FXML
    public void openDoctorRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e, "DoctorRecord");
    }
    @FXML
    public void openMedicineManagement(MouseEvent e) throws IOException {
        SceneManager.transition(e, "MedicineManagement");
    }
    @FXML
    public void openPatientsRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e,"Patients");
    }

    @FXML
    public void openServicesRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e,"Services");
    }


    @FXML
    public void openIllnessesRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e,"Illness");
    }

    @FXML
    public void openSpecializationRecord(MouseEvent e) throws IOException {
        SceneManager.transition(e,"SpecializationRecord");
    }

    @FXML
    public void openIllnessReport(MouseEvent e) throws IOException {
        SceneManager.transition(e,"IllnessReport");
    }

    @FXML
    public void openAppointmentReport(MouseEvent e) throws IOException {
        SceneManager.transition(e,"AppointmentReport");
    }

    @FXML
    public void openServiceRevenue(MouseEvent e) throws IOException {
        SceneManager.transition(e,"ServiceRevenueReport");
    }

    @FXML
    public void openSpecializationReport(MouseEvent e) throws IOException {
        //SceneManager.transition(e,"SpecializationReport");
    }



    @FXML
    public void logout(MouseEvent e) throws IOException {
        SceneManager.transition(e,"login");
    }

    @FXML
    public void openDashboard(MouseEvent e) throws IOException {
        SceneManager.transition(e,"ADMINDashboard");
    }
}
