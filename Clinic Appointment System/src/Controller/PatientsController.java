package Controller;

import Util.Alerts;
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
import javafx.scene.control.*;
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

public class PatientsController {

    @FXML
    private Text Date;

    @FXML
    private Text Time;

    @FXML
    private VBox PatientRows; // this VBox will contain multiple GridPanes (rows)

    @FXML
    private TextField SearchBar;






    @FXML
    public void initialize() {
        try {
            // Load EditPatient.fxml once when the Patients screen initializes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/EditPatientRecord.fxml"));
            Parent root = loader.load();
            EditPatientRecordController controller = loader.getController();

            // Continue with your existing setup
            updateDateTime();

            Timeline clock = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> updateDateTime())
            );
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();

            //Default Select statement when screen loads
            ResultSet InitialData = Database.query(
                    "SELECT p.PatientID, CONCAT(p.LastName, ', ',p.FirstName) CompleteName," +
                            "p.Sex, p.Age, p.ContactNumber, CONCAT(p.BuildingNo,' ',p.Street,' ',p.BarangayNo,' ',p.City," +
                            "' ',p.Province) AS Address FROM Patient p"
            );
            loadData(InitialData);

        } catch (IOException e) {
            e.printStackTrace(); // Or show an alert if you want
        }
    }

    private void loadData(ResultSet PatientsData) {
        try {


            PatientRows.getChildren().clear();


            int rowIndex = 0;

            while (PatientsData.next()) {

                int patientID = PatientsData.getInt("PatientID");
                String Name = PatientsData.getString("CompleteName");
                String Sex = PatientsData.getString("Sex");
                int Age = PatientsData.getInt("Age");
                String ContactNumber = PatientsData.getString("ContactNumber");
                String Address = PatientsData.getString("Address");



                // === Create the GridPane row ===
                GridPane grid = new GridPane();
                grid.setPadding(new Insets(8, 20, 8, 20));
                grid.setHgap(20);
                grid.setVgap(4);
                grid.setAlignment(Pos.CENTER_LEFT);
                grid.setStyle("-fx-background-color: #FFFFFF;");

                // Define column widths
                ColumnConstraints col1 = new ColumnConstraints(50);  // PatientID
                ColumnConstraints col2 = new ColumnConstraints(110);  // Name
                ColumnConstraints col3 = new ColumnConstraints(50);  // Age
                ColumnConstraints col4 = new ColumnConstraints(80);  // Sex
                ColumnConstraints col5 = new ColumnConstraints(120);  // Contact Number
                ColumnConstraints col6 = new ColumnConstraints(250);  // Address
                ColumnConstraints col7 = new ColumnConstraints(120);

                grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6,col7);

                // === Add cells ===
                Label idLabel = new Label("P-"+String.valueOf(patientID));

                Label NameLabel = new Label(Name);
                NameLabel.setPrefWidth(100);
                NameLabel.setMinHeight(Region.USE_PREF_SIZE);
                NameLabel.setWrapText(true);
                NameLabel.setTextAlignment(TextAlignment.LEFT);
                NameLabel.setAlignment(Pos.CENTER_LEFT);



                Label SexLabel = new Label(Sex);
                Label AgeLabel = new Label(String.valueOf(Age));
                Label ContactNumberLabel = new Label(ContactNumber);
                Label AddressLabel = new Label(Address);
                AddressLabel.setPrefWidth(300);
                AddressLabel.setWrapText(true);
                AddressLabel.setTextOverrun(OverrunStyle.CLIP);


                Button Update = new Button();
                Button Delete = new Button();

                //set images
                Image updateImg = new Image(getClass().getResourceAsStream("/Assets/Update.png"));
                Image deleteImg = new Image(getClass().getResourceAsStream("/Assets/Delete.png"));

                //put images in ImageView
                ImageView updateIcon = new ImageView(updateImg);
                ImageView deleteIcon = new ImageView(deleteImg);

                //resize buttons
                updateIcon.setFitWidth(16);
                updateIcon.setFitHeight(16);
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);

                //set the images as graphic in the buttons
                Update.setGraphic(updateIcon);
                Delete.setGraphic(deleteIcon);


                Update.setOnAction(event -> {
                    try {
                        // Load EditPatient.fxml dynamically
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scenes/EditPatientRecord.fxml"));
                        Parent root = loader.load();

                        // Get the EditPatient controller
                        EditPatientRecordController controller = loader.getController();

                        // Pass the selected PatientID
                        controller.setPatientID(patientID);

                        // Create a new stage for editing
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Edit Patient");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                Delete.setOnAction(event -> {
                    if(Alerts.Confirmation("Are you sure you want to delete this patient?")){
                        Database.update("DELETE FROM patient WHERE Patientid = ?", patientID);
                        Alerts.Info("Patient deleted successfully!");
                        initialize(); // refresh the screen
                    }
                });


                // Add to GridPane
                grid.add(idLabel, 0, 0);
                grid.add(NameLabel, 1, 0);
                grid.add(AgeLabel, 2, 0);
                grid.add(SexLabel, 3, 0);
                grid.add(ContactNumberLabel, 4, 0);
                grid.add(AddressLabel, 5, 0);
                // Create an HBox for the action buttons
                HBox actionButtons = new HBox(8); // spacing = 8px between buttons
                actionButtons.setAlignment(Pos.CENTER_LEFT); // or CENTER if you prefer
                actionButtons.getChildren().addAll(Update, Delete);

                grid.add(actionButtons, 6, 0);


                // === Add separator line below each row ===
                Line line = new Line(0, 0, 900, 0);
                line.setStroke(Color.LIGHTGRAY);
                line.setStrokeWidth(1);

                VBox rowBox = new VBox(grid, line);
                PatientRows.getChildren().add(rowBox);

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


    @FXML
    private void Search() {
        String search = SearchBar.getText().trim();

        // Remove "P-" prefix if user includes it
        boolean isIdSearch = false;
        if (search.toUpperCase().startsWith("P-")) {
            search = search.substring(2);
            isIdSearch = true;
        }

        String query =
                "SELECT *, " +
                        "CONCAT(BuildingNo, ' ', Street, ' ', BarangayNo, ' ', City, ' ', Province) AS Address, " +
                        "CONCAT(LastName, ', ', FirstName) AS CompleteName " +
                        "FROM Patient " +
                        "WHERE " +
                        "(? = TRUE AND PatientID = ?) " +   // Exact match for PatientID
                        "OR (? = FALSE AND (FirstName LIKE ? OR LastName LIKE ? OR ContactNumber LIKE ?))";


        try (
             PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {

            String searchLike = "%" + search + "%";

            // Parameters:
            stmt.setBoolean(1, isIdSearch);   // ? = TRUE if searching by ID
            stmt.setString(2, search);        // PatientID = ?
            stmt.setBoolean(3, isIdSearch);   // ? = FALSE when not ID search
            stmt.setString(4, searchLike);    // FirstName LIKE ?
            stmt.setString(5, searchLike);    // LastName LIKE ?
            stmt.setString(6, searchLike);    // ContactNumber LIKE ?

            ResultSet PatientsData = stmt.executeQuery();
            loadData(PatientsData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    //Opens up add patient form
    @FXML
    private void AddPatient(ActionEvent e) throws IOException{
        SceneManager.onOpenPopup(e,"AddPatient","Add a patient");
    }


    public void DashboardScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "Dashboard");
    }
}
