package Controller;

import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentsController {

    @FXML
    private Label AppointmentCount;

    @FXML
    private Label CompletedAppointments;

    @FXML
    private Label PendingAppointments;

    @FXML
    private Label CanceledAppointments;

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private VBox AppointmentRows; // this VBox will contain multiple GridPanes (rows)


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
            ResultSet AppointmentData = Database.query(
                    "SELECT " +
                            "a.AppointmentID, p.PatientID, " +
                            "CONCAT('Dr. ', d.FirstName, ' ', d.LastName) AS DoctorName, " +
                            "CONCAT(p.FirstName, ' ', p.LastName) AS PatientName, " +
                            "a.Status, a.Time, a.Date, a.ReasonForVisit, " +
                            "COUNT(*) OVER() AS TotalAppointment, " +
                            "SUM(CASE WHEN a.Status = 'Completed' THEN 1 ELSE 0 END) OVER() AS CompletedAppointments, " +
                            "SUM(CASE WHEN a.Status = 'Pending' THEN 1 ELSE 0 END) OVER() AS PendingAppointments " +
                            "FROM appointment a " +
                            "JOIN patient p ON a.PatientID = p.PatientID " +
                            "JOIN doctor d ON a.DoctorID = d.DoctorID"
            );

            AppointmentRows.getChildren().clear();

            boolean statsSet = false;
            int rowIndex = 0;

            while (AppointmentData.next()) {
                if (!statsSet) {
                    AppointmentCount.setText(String.valueOf(AppointmentData.getInt("TotalAppointment")));
                    CompletedAppointments.setText(String.valueOf(AppointmentData.getInt("CompletedAppointments")));
                    PendingAppointments.setText(String.valueOf(AppointmentData.getInt("PendingAppointments")));
                    statsSet = true;
                }

                int appointmentID = AppointmentData.getInt("AppointmentID");
                String DoctorName = AppointmentData.getString("DoctorName");
                String PatientName = AppointmentData.getString("PatientName");
                String Status = AppointmentData.getString("Status");
                String Time = AppointmentData.getString("Time");
                String Date = AppointmentData.getString("Date");
                String Reason = AppointmentData.getString("ReasonForVisit");


                // === Create the GridPane row ===
                GridPane grid = new GridPane();
                grid.setPadding(new Insets(8, 20, 8, 20));
                grid.setHgap(20);
                grid.setVgap(4);
                grid.setAlignment(Pos.CENTER_LEFT);
                grid.setStyle("-fx-background-color: #FFFFFF;");

                // Define column widths
                ColumnConstraints col1 = new ColumnConstraints(80);  // ID
                ColumnConstraints col2 = new ColumnConstraints(130);  // Patient
                ColumnConstraints col3 = new ColumnConstraints(100);  // Doctor
                ColumnConstraints col4 = new ColumnConstraints(140);  // Date & Time
                ColumnConstraints col5 = new ColumnConstraints(130);  // Reason
                ColumnConstraints col6 = new ColumnConstraints(150);  // Status
                ColumnConstraints col7 = new ColumnConstraints(0);  // Update button
                grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);

                // === Add cells ===
                Label idLabel = new Label(String.valueOf(appointmentID));
                Label patientLabel = new Label(PatientName);
                Label doctorLabel = new Label(DoctorName);
                Label dateTimeLabel = new Label(Time + " " + Date);
                Label reasonLabel = new Label(Reason);
                reasonLabel.setMinHeight(Region.USE_PREF_SIZE);
                reasonLabel.setWrapText(true);
                reasonLabel.setTextAlignment(TextAlignment.LEFT);
                reasonLabel.setAlignment(Pos.CENTER_LEFT);

                ComboBox<String> statusDropdown = new ComboBox<>();
                statusDropdown.getItems().addAll("Pending", "In-Progress", "Completed", "Canceled");
                statusDropdown.setValue(Status);
                updateDropdownColor(statusDropdown, Status);


                Button Update = new Button();
//                Button Delete = new Button();

                //set images
                Image updateImg = new Image(getClass().getResourceAsStream("/Assets/Update.png"));
//                Image deleteImg = new Image(getClass().getResourceAsStream("/Assets/Delete.png"));

                //put images in ImageView
                ImageView updateIcon = new ImageView(updateImg);
//                ImageView deleteIcon = new ImageView(deleteImg);

                //resize buttons
                updateIcon.setFitWidth(16);
                updateIcon.setFitHeight(16);
//                deleteIcon.setFitWidth(16);
//                deleteIcon.setFitHeight(16);

                //set the images as graphic in the buttons
                Update.setGraphic(updateIcon);
//                Delete.setGraphic(deleteIcon);


                //edit appointment record
                Update.setOnAction(event -> {
                    try {
                        // Load EditPatient.fxml dynamically
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/EditAppointment.fxml"));
                        Parent root = loader.load();

                        // Get the EditPatient controller
                        EditAppointmentController controller = loader.getController();

                        // Pass the selected PatientID
                        controller.setAPPID(appointmentID);

                        // Create a new stage for editing
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                            stage.setTitle("Edit Appointment");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


                // Handle dropdown changes
                statusDropdown.setOnAction(e -> {
                    String newStatus = statusDropdown.getValue();
                    updateDropdownColor(statusDropdown, newStatus);
                    Database.update("UPDATE Appointment SET Status = '" + newStatus + "' WHERE AppointmentID = " + appointmentID);
                });

                // Add to GridPane
                grid.add(idLabel, 0, 0);
                grid.add(patientLabel, 1, 0);
                grid.add(doctorLabel, 2, 0);
                grid.add(dateTimeLabel, 3, 0);
                grid.add(reasonLabel, 4, 0);
                grid.add(statusDropdown, 5, 0);
                grid.add(Update,6,0);

                GridPane.setHalignment(statusDropdown, HPos.CENTER);

                // === Add separator line below each row ===
                Line line = new Line(0, 0, 920, 0);
                line.setStroke(Color.LIGHTGRAY);
                line.setStrokeWidth(1);

                VBox rowBox = new VBox(grid, line);
                AppointmentRows.getChildren().add(rowBox);

                rowIndex++;
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

    private void updateDropdownColor(ComboBox<String> comboBox, String status) {
        switch (status) {
            case "Pending" ->
                    comboBox.setStyle("-fx-background-color: #FCFF9D; -fx-font-weight: bold;");
            case "Completed" ->
                    comboBox.setStyle("-fx-background-color: #9DFFAF; -fx-font-weight: bold;");
            case "Canceled" ->
                    comboBox.setStyle("-fx-background-color: #FF9D9F; -fx-font-weight: bold;");
            case "In-Progress" ->
                    comboBox.setStyle("-fx-background-color: #9DEFFF; -fx-font-weight: bold;");
            default ->
                    comboBox.setStyle("");
        }
    }

    //Opens up scheduling appointment window
    @FXML
    private void openScheduleAppointment(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/ScheduleAppointment.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Schedule an Appointment");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }


    public void DashboardScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "ADMINDashboard");
    }

        public void openPaymentScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "PaymentProcessing");
    }
}
