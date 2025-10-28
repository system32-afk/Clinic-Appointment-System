package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private final String email = "Admin@email.com";
    private final String password = "Admin123";

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void Signin(ActionEvent event) {
        String inputEmail = emailField.getText();
        String inputPassword = passwordField.getText();

        if (inputEmail.equals(email) && inputPassword.equals(password)) {
            System.out.println("LOGIN SUCCESSFUL");
        } else {
            System.out.println("INVALID CREDENTIALS");
        }
    }
}
