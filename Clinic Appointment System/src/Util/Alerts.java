package Util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Alerts {

    /*
    This function gives a popup in a warning format
    Use this function for warning popups

    @Param msg the custom message you want it to display
     */
    public static void Warning(String msg){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Validation");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /*
        This function gives a popup in a information format
        Use this function for informing the user

        @Param msg the custom message you want it to display
         */
    public static void Info(String msg){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /*
        This function gives a popup in a Confirmation format
        Use this function if you want to confrim something with the user (i.e record deletion)
        ONLY GIVES "OK" OR "CANCEL" OPTIONS

        @Param msg the custom message you want it to display
        @Return returns TRUE if the user clicks "OK" false if "Cancel"
         */
    public static Boolean Confirmation(String msg){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText(msg);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK; // RETURNS IF USER PRESSES OK
    }


}
