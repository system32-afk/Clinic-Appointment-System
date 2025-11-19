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

public class IllnessController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private VBox IllnessRows;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button addIllnessButton;

    @FXML
    private Label TotalIllnessCount;

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
            ResultSet IllnessData = Database.query(
                    "SELECT " +
                            "i.IllnessID, " +
                            "i.IllnessName " +
                            "FROM illness i"
            );

            IllnessRows.getChildren().clear();
            scrollPane.setFitToWidth(true);
            int rowIndex = 0;

            while (IllnessData.next()) {

                int IllnessID = IllnessData.getInt("IllnessID");
                String IllnessName = IllnessData.getString("IllnessName");

                // HBox for each row
                HBox row = new HBox(40);
                row.setPadding(new Insets(8, 20, 8, 40));
                row.setAlignment(Pos.BASELINE_LEFT);

                // === Add cells ===
                Label idLabel = new Label(String.valueOf(IllnessID));
                Label nameLabel = new Label(IllnessName);

                // === Update ====
                Image editImage = new Image(getClass().getResourceAsStream("/Assets/Update.png"));
                ImageView editIcon = new ImageView(editImage);
                editIcon.setFitHeight(20);
                editIcon.setFitWidth(20);

                Button edit =  new Button();
                edit.setGraphic(editIcon);
                edit.setStyle("-fx-background-color: transparent;");
                edit.setOnAction(event -> editIllness(IllnessID));


                // Adjust margins
                idLabel.setPrefWidth(100);
                nameLabel.setPrefWidth(200);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(idLabel, nameLabel, edit, spacer);

                // === Add separator line below each row ===
                Separator separator = new Separator();
                separator.prefWidthProperty().bind(IllnessRows.widthProperty());

                IllnessRows.getChildren().addAll(row, separator);

                rowIndex++;
            }

            TotalIllnessCount.setText(String.valueOf(rowIndex));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editIllness(int IllnessID) {

        try {
            String sql = "SELECT IllnessName FROM illness WHERE IllnessID = ?";
            PreparedStatement ps = Database.getConnection().prepareStatement(sql);
            ps.setInt(1, IllnessID);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                Stage editStage = new Stage();
                editStage.setTitle("Edit Illness");

                GridPane grid = new GridPane();
                grid.setPadding(new  Insets(20));
                grid.setVgap(8);
                grid.setHgap(8);

                Label nameLabel = new Label("Illness Name: ");
                TextField nameField = new TextField(rs.getString("IllnessName"));

                Button save = new Button("Save");
                save.setOnAction(e-> {
                    updateIllness(IllnessID, nameField.getText());
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

    private void updateIllness(int IllnessID, String IllnessName) {
        String sql = "UPDATE illness SET IllnessName = ? WHERE IllnessID = ?";
        try(PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, IllnessName);
            ps.setInt(2, IllnessID);
            ps.executeUpdate();
        } catch(SQLException e) {
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
    private void AddIllness(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add new Illness");
        dialog.setHeaderText("Enter the Illness Name");
        dialog.setContentText("Illness Name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(illnessName -> {
            illnessName = illnessName.trim();

            if(illnessName.isEmpty()) {
                statusLabel.setText("Illness name cannot be empty");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            String checkQuery = "SELECT * FROM illness WHERE illnessName = ?";
            ResultSet rs = Database.query(checkQuery, illnessName);

            try{
                if(rs.next()) {
                    statusLabel.setText("Illness already exists");
                    statusLabel.setStyle("-fx-text-fill: red;");
                } else {

                    String query = "INSERT INTO illness (IllnessName) VALUES (?)";
                    Database.update(query, illnessName);

                    loadData();
                    statusLabel.setText("Illness added successfully");
                    statusLabel.setStyle("-fx-text-fill: green;");

                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(e-> statusLabel.setVisible(false));
                    pause.play();
                }

            } catch (Exception e) {
                statusLabel.setText("Database error: could not add Illness");
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
