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

    private final String adminEmail = "Admin@email.com";
    private final String adminPassword = "Admin123";
    private final String doctorEmail = "Doctor@email.com";
    private final String doctorPassword = "Doctor123";

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

        if (inputEmail.equalsIgnoreCase(adminEmail) && inputPassword.equals(adminPassword)) {
            SceneManager.transition(event,"ADMINDashboard");

        } else if (inputEmail.equalsIgnoreCase(doctorEmail) && inputPassword.equals(doctorPassword)) {
            SceneManager.transition(event,"DOCTORDashboard");
        } else {
            manipulate.setText("INVALID CREDENTIALS");
        }
    }
}
