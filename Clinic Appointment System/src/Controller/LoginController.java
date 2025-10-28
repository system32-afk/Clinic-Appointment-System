package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import Util.Database;
import java.sql.Connection;
import java.sql.ResultSet;


public class LoginController {
    private final Connection conn = Database.getConnection();
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

            ResultSet result = Database.query("SELECT * FROM patient");
            try {
                while (result.next()) {
                    System.out.println("Patient: " + result.getString("FirstName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }



        } else {
            System.out.println("INVALID CREDENTIALS");
        }
    }
}
