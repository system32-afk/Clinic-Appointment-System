import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Driver extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Scenes/Login.fxml"));
        primaryStage.setTitle("Clinic Appointment System");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
