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
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
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
                        Database.update("DELETE FROM illness WHERE IllnessID = ?", IllnessID);
                        IllnessRows.getChildren().remove(row);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }

                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(idLabel, nameLabel, spacer, deleteButton);

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
            if(illnessName.trim().isEmpty()) {
                statusLabel.setText("Illness name cannot be empty");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            String query = "INSERT INTO illness (IllnessName) VALUES (?)";
            Database.update(query, illnessName);

            loadData();
            statusLabel.setText("Illness added successfully");
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
