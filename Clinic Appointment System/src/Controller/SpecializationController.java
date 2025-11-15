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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

public class SpecializationController {

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
                            "FROM specialization s"
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

                Image deleteImg = new Image(getClass().getResourceAsStream("/Assets/Delete.png"));
                ImageView deleteIcon = new ImageView(deleteImg);
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);

                Button deleteButton = new Button();
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                deleteButton.setOnAction(e -> {
                    try {
                        Database.update("DELETE FROM specialization WHERE SpecializationID = ?", SpecializationID);
                        SpecializationRows.getChildren().remove(row);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }

                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(idLabel, nameLabel, spacer, deleteButton);

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

            String query = "INSERT INTO specialization (SpecializationName) VALUES (?)";
            Database.update(query, specializationName);

            loadData();
            statusLabel.setText("Specialization added successfully");
            statusLabel.setStyle("-fx-text-fill: green;");

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e-> statusLabel.setVisible(false));
            pause.play();
        });

    }


    public void DashboardScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "ADMINDashboard");
    }

    public void AppointmentScreen(ActionEvent e) throws IOException{
        SceneManager.transition(e,"Appointments");
    }
}
