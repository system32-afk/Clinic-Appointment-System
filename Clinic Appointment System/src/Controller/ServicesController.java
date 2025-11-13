package Controller;

import Util.Alerts;
import Util.Database;
import Util.SceneManager;
import com.mysql.cj.protocol.Resultset;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServicesController {




    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private VBox ServicesRows; // this VBox will contain multiple GridPanes (rows)

    @FXML
    private TextField SearchBar;






    @FXML
    public void initialize() {

        // Continue with existing setup
        updateDateTime();

        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        //Default Select statement when screen loads
        ResultSet InitialData = Database.query(
                "SELECT * FROM service"
        );
        loadData(InitialData);

    }


    private void loadData(ResultSet PatientsData) {
        try {


            ServicesRows.getChildren().clear();


            int rowIndex = 0;

            while (PatientsData.next()) {

                int serviceID = PatientsData.getInt("serviceID");
                String serviceName = PatientsData.getString("serviceName");
                long price = PatientsData.getLong("price");




                // === Create the GridPane row ===
                GridPane grid = new GridPane();
                grid.setPadding(new Insets(8, 20, 8, 20));
                grid.setHgap(20);
                grid.setVgap(4);
                grid.setAlignment(Pos.CENTER_LEFT);
                grid.setStyle("-fx-background-color: #FFFFFF;");

                // Define column widths
                ColumnConstraints col1 = new ColumnConstraints(150);  // ServiceID
                ColumnConstraints col2 = new ColumnConstraints(290);  // Service Name
                ColumnConstraints col3 = new ColumnConstraints(250);  // Price
                ColumnConstraints col4 = new ColumnConstraints(80);  // Action


                grid.getColumnConstraints().addAll(col1, col2, col3, col4);

                // === Add cells ===
                Label idLabel = new Label("SRV-"+String.valueOf(serviceID));

                Label ServiceNameLabel = new Label(serviceName);
                ServiceNameLabel.setPrefWidth(200);
                ServiceNameLabel.setMinHeight(Region.USE_PREF_SIZE);
                ServiceNameLabel.setWrapText(true);
                ServiceNameLabel.setTextAlignment(TextAlignment.LEFT);
                ServiceNameLabel.setAlignment(Pos.CENTER_LEFT);



                Label priceLabel = new Label(String.valueOf(price) );



                Button Update = new Button();


                //set images
                Image updateImg = new Image(getClass().getResourceAsStream("/Assets/Update.png"));


                //put images in ImageView
                ImageView updateIcon = new ImageView(updateImg);


                //resize buttons
                updateIcon.setFitWidth(16);
                updateIcon.setFitHeight(16);


                //set the images as graphic in the buttons
                Update.setGraphic(updateIcon);

                Update.setOnAction(event -> {
                    try {
                        // Load EditPatient.fxml dynamically
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/AddService.fxml"));
                        Parent root = loader.load();

                        // Get the EditPatient controller
                        AddServiceController controller = loader.getController();

                        // Pass the selected PatientID
                        controller.setServiceID(serviceID);

                        // Create a new stage for editing
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Edit Service");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });



                // Add to GridPane
                grid.add(idLabel, 0, 0);
                grid.add(ServiceNameLabel, 1, 0);
                grid.add(priceLabel, 2, 0);


                // Create an HBox for the action buttons
                HBox actionButtons = new HBox(8); // spacing = 8px between buttons
                actionButtons.setAlignment(Pos.CENTER_LEFT); // or CENTER if you prefer
                actionButtons.getChildren().addAll(Update);

                grid.add(actionButtons, 3, 0);


                // === Add separator line below each row ===
                Line line = new Line(0, 0, 900, 0);
                line.setStroke(Color.LIGHTGRAY);
                line.setStrokeWidth(1);

                VBox rowBox = new VBox(grid, line);
                ServicesRows.getChildren().add(rowBox);

                rowIndex++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }


    @FXML
    private void Search() {
        String search = SearchBar.getText().trim();

        if (search.isEmpty()) {
            // Reload all services if search bar is empty
            ResultSet rs = Database.query("SELECT * FROM service");
            loadData(rs);
            return;
        }

        boolean isIdSearch = false;
        int idValue = -1;

        // Detect if searching by "SRV-###"
        if (search.toUpperCase().startsWith("SRV-")) {
            isIdSearch = true;
            try {
                idValue = Integer.parseInt(search.substring(4)); // Remove "SRV-"
            } catch (NumberFormatException e) {
                Alerts.Warning("Invalid service ID format.");
                return;
            }
        }

        String searchLike = "%" + search + "%";

        String query = """
        SELECT * 
        FROM service
        WHERE 
            (? = TRUE AND serviceID = ?)
            OR (? = FALSE AND serviceName LIKE ?)
        """;

        ResultSet services = Database.query(query, isIdSearch, idValue, isIdSearch, searchLike);
        loadData(services);
    }



    //Opens up add patient form
    @FXML
    private void AddService(ActionEvent e) throws IOException{
        SceneManager.OpenPopup(e,"AddService","Add a service");
    }


    public void DashboardScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "Dashboard");
    }
}
