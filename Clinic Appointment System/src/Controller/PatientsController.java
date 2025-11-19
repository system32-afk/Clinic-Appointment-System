package Controller;

import Util.Alerts;
import Util.Database;
import Util.HoverPopup;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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
    private Pane ManagementPane;

    @FXML
    private Pane RecordsManagementButton;

    @FXML
    private Pane ReportsButton;

    @FXML
    private Pane ReportsManagement;

    private final PauseTransition hideDelay = new PauseTransition(Duration.millis(200));




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




            //Default Select statement when screen loads
            ResultSet InitialData = Database.query(
                    "SELECT p.PatientID, CONCAT(p.LastName, ', ',p.FirstName) CompleteName," +
                            "p.Sex, p.Age, p.ContactNumber,p.status, CONCAT(p.BuildingNo,' ',p.Street,' ',p.BarangayNo,' ',p.City," +
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
                String status = PatientsData.getString("Status");


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
                ColumnConstraints col7 = new ColumnConstraints(200);

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
                ComboBox<String> statusCombo = new ComboBox<>();
                statusCombo.getItems().addAll("Active", "In-Active");
                statusCombo.setValue(status); // set current status from DB


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

                statusCombo.setOnAction(event -> {
                    String newStatus = statusCombo.getValue();
                    try {
                        String updateQuery = "UPDATE Patient SET Status = ? WHERE PatientID = ?";
                        PreparedStatement ps = Database.getConnection().prepareStatement(updateQuery);
                        ps.setString(1, newStatus);
                        ps.setInt(2, patientID);
                        ps.executeUpdate();
                        ps.close();
                        Alerts.Info("Status Updated, Patient status has been updated successfully.");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        Alerts.Warning("Database Error, Failed to update patient status.");
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
                actionButtons.getChildren().addAll(Update,statusCombo);

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
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }


    @FXML
    private void Search() {
        String search = SearchBar.getText().trim();
        String searchLike;
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

               searchLike = "%" + search + "%";
        loadData(Objects.requireNonNull(Database.query(query, isIdSearch, search, isIdSearch, searchLike, searchLike, searchLike)));

    }



    //Opens up add patient form
    @FXML
    private void AddPatient(ActionEvent e) throws IOException{
        SceneManager.OpenPopup(e,"AddPatient","Add a patient");
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
