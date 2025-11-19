package Controller;

import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.TextField;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DiagnosisController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private VBox DiagnosisRows;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button addDiagnosisButton;

    @FXML
    private Label TotalDiagnosisCount;

    @FXML
    private TextField searchField;

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

            ResultSet DiagnosisData = Database.query(
                    "SELECT " +
                            "d.DiagnosisID, " +
                            "d.AppointmentID, " +
                            "CONCAT(p.FirstName, ' ', p.LastName) AS PatientName, " +
                            "i.IllnessName, " +
                            "d.DateDiagnosed " +
                            "FROM diagnosis d " +
                            "JOIN appointment a ON d.AppointmentID=a.AppointmentID " +
                            "JOIN patient p ON a.PatientID=p.PatientID " +
                            "JOIN illness i ON d.IllnessID=i.IllnessID " +
                            "ORDER BY d.DiagnosisID"
            );

            DiagnosisRows.getChildren().clear();
            scrollPane.setFitToWidth(true);
            int rowIndex = 0;

            while (DiagnosisData.next()) {

                int DiagnosisID = DiagnosisData.getInt("DiagnosisID");
                int AppointmentID = DiagnosisData.getInt("AppointmentID");
                String PatientName = DiagnosisData.getString("PatientName");
                String IllnessName = DiagnosisData.getString("IllnessName");
                String DateDiagnosed = DiagnosisData.getString("DateDiagnosed");


                // HBox for each row
                HBox row = new HBox(40);
                row.setPadding(new Insets(8, 20, 8, 40));
                row.setAlignment(Pos.BASELINE_LEFT);

                // === Add cells ===
                Label diagnosisLabel = new Label(String.valueOf(DiagnosisID));
                Label appointmentLabel = new Label(String.valueOf(AppointmentID));
                Label patientLabel = new Label(PatientName);
                Label illnessLabel = new Label(IllnessName);
                Label dateLabel = new Label(DateDiagnosed);

                // Adjust margins
                diagnosisLabel.setPrefWidth(100);
                appointmentLabel.setPrefWidth(100);
                patientLabel.setPrefWidth(100);
                illnessLabel.setPrefWidth(100);
                dateLabel.setPrefWidth(200);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(diagnosisLabel, appointmentLabel, patientLabel, illnessLabel, dateLabel, spacer);

                // === Add separator line below each row ===
                Separator separator = new Separator();
                separator.prefWidthProperty().bind(DiagnosisRows.widthProperty());

                DiagnosisRows.getChildren().addAll(row, separator);

                rowIndex++;
            }

            TotalDiagnosisCount.setText(String.valueOf(rowIndex));

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
    private void AddDiagnosis(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/CreateDiagnosis.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Diagnose");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    /*
     ===================SIDE PANEL BUTTONS=========================
      */
    @FXML
    public void logout(MouseEvent e) throws IOException {
        SceneManager.transition(e,"login");
    }

    @FXML
    public void openMedicalHistory(MouseEvent e) throws IOException {
        SceneManager.transition(e,"MedicalHistory");
    }

    @FXML
    public void openDiagnosis(MouseEvent e) throws IOException {
        SceneManager.transition(e,"Diagnosis");
    }

    @FXML
    public void openRequestProcedure(MouseEvent e) throws IOException {
        SceneManager.OpenPopup(e,"RequestProcedure","Reqest for procedure form");

    }

    @FXML
    public void openPrescription(MouseEvent e) throws IOException {
        SceneManager.transition(e,"OpenPrescription");
    }

    @FXML
    public void openDashboard(MouseEvent e) throws IOException {
        SceneManager.transition(e,"DOCTORDashboard");
    }
}
