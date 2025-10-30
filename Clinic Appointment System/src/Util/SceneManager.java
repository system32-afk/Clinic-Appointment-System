package Util;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SceneManager {

    public static void transition(ActionEvent event, String screenName){
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
}
