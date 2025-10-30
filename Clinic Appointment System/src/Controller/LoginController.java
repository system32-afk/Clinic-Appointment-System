package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import Util.Database;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import Util.SceneManager;

public class LoginController {

    private final String email = "Admin@email.com";
    private final String password = "Admin123";

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Text manipulate;
    @FXML
    private void Signin(ActionEvent event) throws IOException {
        String inputEmail = emailField.getText();
        String inputPassword = passwordField.getText();

        if (inputEmail.equals(email) && inputPassword.equals(password)) {
            System.out.println("LOGIN SUCCESSFUL");

            SceneManager.transition(event,"Dashboard");

        } else {
            manipulate.setText("INVALID CREDENTIALS");
        }
    }
}
