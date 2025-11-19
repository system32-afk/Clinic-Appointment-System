package Controller;

import Util.Database;
import Util.HoverPopup;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.PreparedStatement;
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
    private Pane ManagementPane;

    @FXML
    private Pane RecordsManagementButton;

    @FXML
    private Pane ReportsButton;

    @FXML
    private Pane ReportsManagement;

    @FXML
    public void initialize() {
        updateDateTime();

        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        /*
        ======================== HOVER FEATURE =========================
         */
        HoverPopup.attachHoverPopup(
                RecordsManagementButton,
                ManagementPane,
                Duration.seconds(0.3)
        );

        HoverPopup.attachHoverPopup(
                ReportsButton,
                ReportsManagement,
                Duration.seconds(0.3)
        );



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

                // === Update ====
                Image editImage = new Image(getClass().getResourceAsStream("/Assets/Update.png"));
                ImageView editIcon = new ImageView(editImage);
                editIcon.setFitHeight(20);
                editIcon.setFitWidth(20);

                Button edit =  new Button();
                edit.setGraphic(editIcon);
                edit.setStyle("-fx-background-color: transparent;");
                edit.setOnAction(event -> editSpecialization(SpecializationID));

                // Adjust margins
                idLabel.setPrefWidth(100);
                nameLabel.setPrefWidth(200);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(idLabel, nameLabel, edit, spacer);

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
            if(specializationName.trim().isEmpty()) {
                statusLabel.setText("Specialization name cannot be empty");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            String query = "INSERT INTO ref_specialization (SpecializationName) VALUES (?)";
            Database.update(query, specializationName);

            loadData();
            statusLabel.setText("Specialization added successfully");
            statusLabel.setStyle("-fx-text-fill: green;");

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e-> statusLabel.setVisible(false));
            pause.play();
        });

    }

    private void editSpecialization(int SpecializationID) {

        try {
            String sql = "SELECT SpecializationName FROM ref_specialization WHERE SpecializationID = ?";
            PreparedStatement ps = Database.getConnection().prepareStatement(sql);
            ps.setInt(1, SpecializationID);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                Stage editStage = new Stage();
                editStage.setTitle("Edit Specialization");

                GridPane grid = new GridPane();
                grid.setPadding(new  Insets(20));
                grid.setVgap(8);
                grid.setHgap(8);

                Label nameLabel = new Label("Specialization Name: ");
                TextField nameField = new TextField(rs.getString("SpecializationName"));

                Button save = new Button("Save");
                save.setOnAction(e-> {
                    updateSpecialization(SpecializationID, nameField.getText());
                    editStage.close();
                    loadData();
                });

                grid.add(nameLabel, 0, 0);
                grid.add(nameField, 1, 0);
                grid.add(save, 1, 1);

                Scene scene = new Scene(grid);
                editStage.setScene(scene);
                editStage.initModality(Modality.APPLICATION_MODAL);
                editStage.showAndWait();



            }


        } catch(SQLException e) {
            e.printStackTrace();
        }

    }

    private void updateSpecialization(int SpecializationID, String SpecializationName) {
        String sql = "UPDATE ref_specialization SET SpecializationName = ? WHERE SpecializationID = ?";
        try(PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, SpecializationName);
            ps.setInt(2, SpecializationID);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }


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
        SceneManager.transition(e,"Reports");
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
