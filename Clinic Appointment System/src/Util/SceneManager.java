package Util;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SceneManager {


    //for buttons
    public static void transition(ActionEvent event, String screenName) throws IOException {
        try{
            Parent root = FXMLLoader.load(SceneManager.class.getResource("/Scenes/"+screenName+".fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);

            FadeTransition FT = new FadeTransition(Duration.millis(400),root);
            FT.setFromValue(0);
            FT.setToValue(1);

            stage.setScene(scene);
            stage.show();
            FT.play();
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    //for panes
    public static void transition(MouseEvent event, String screenName) throws IOException {
        try{
            Parent root = FXMLLoader.load(SceneManager.class.getResource("/Scenes/"+screenName+".fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);

            FadeTransition FT = new FadeTransition(Duration.millis(400),root);
            FT.setFromValue(0);
            FT.setToValue(1);

            stage.setScene(scene);
            stage.show();
            FT.play();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void OpenPopup(ActionEvent event, String screenName, String title) throws IOException {
        // get current window (the stage that fired the event)
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/Scenes/"+screenName+".fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));

        // set owner BEFORE show/showAndWait
        stage.initOwner(currentStage);
        stage.initModality(Modality.WINDOW_MODAL); // blocks input to owner
        stage.showAndWait(); // use showAndWait to block until closed
    }
}


