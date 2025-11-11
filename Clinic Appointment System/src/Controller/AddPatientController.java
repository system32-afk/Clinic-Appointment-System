package Controller;

import Util.Alerts;
import Util.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddPatientController {

    @FXML
    private TextField FirstNameField;

    @FXML
    private TextField LastNameField;

    @FXML
    private TextField ContactNumberField;

    @FXML
    private ComboBox<String> SexField;

    @FXML
    private TextField AgeField;

    @FXML
    private TextField BuildingNoField;

    @FXML
    private TextField StreetField;

    @FXML
    private TextField CityField;

    @FXML
    private TextField BrgyField;

    @FXML
    private TextField ProvinceField;

    @FXML
    private Button AddPatient;




    @FXML
    private void AddPatient(ActionEvent e){
        String FirstName = FirstNameField.getText();
        String LastName = LastNameField.getText();
        String ContactNumber = ContactNumberField.getText();
        String Sex = SexField.getValue();
        String Age = AgeField.getText();
        String BuildingNo = BuildingNoField.getText();
        String Street = StreetField.getText();
        String city = CityField.getText();
        String BarangayNo = BrgyField.getText();
        String province = ProvinceField.getText();


        StringBuilder errors = new StringBuilder();

        // Validate all fields
        if (FirstName.isEmpty())
            errors.append("First name is required.\n");

        if (LastName.isEmpty())
            errors.append("Last name is required.\n");

        if (BarangayNo.isEmpty())
            errors.append("Barangay Number is required.\n");

        if (ContactNumber.isEmpty()) {
            errors.append("Contact number is required.\n");
        } else if (!ContactNumber.matches("\\d+")) {
            errors.append("Contact number should contain digits only.\n");
        }

        if (Sex == null || Sex.isEmpty())
            errors.append("Please select a sex.\n");


        if (Age.isEmpty()) {
            errors.append("Age is required.\n");
        } else {
            try {
                int age = Integer.parseInt(Age);
            } catch (NumberFormatException ex) {
                errors.append("Age must be a valid number.\n");
            }
        }

        if (BuildingNo.isEmpty())
            errors.append("Building number is required.\n");

        if (Street.isEmpty())
            errors.append("Street is required.\n");

        if (city.isEmpty())
            errors.append("City is required.\n");

        if (province.isEmpty())
            errors.append("Province is required.\n");

        // If there are errors, show them and stop execution
        if (errors.length() > 0) {
            Alerts.Warning(errors.toString());


            return;
        }

        String insert = "INSERT INTO patient (FirstName, LastName, Age,Sex,ContactNumber,BuildingNo,Street,BarangayNo,City,Province) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String checkDatabase = "SELECT COUNT(*) AS count FROM patient WHERE FirstName = ? AND LastName = ? AND ContactNumber = ?";
        try{
            ResultSet check = Database.query(checkDatabase, FirstName,LastName,ContactNumber);
            if(check.next()){
                int count = check.getInt("count");
                if(count > 0){
                    Alerts.Warning("A Patient with the same name and contact number already exists!");
                    return;
                }else{
                    Database.update(insert,FirstName, LastName, Age, Sex, ContactNumber, BuildingNo, Street, BarangayNo, city, province);
                }
            }
        } catch (SQLException ex) {
            Alerts.Warning("Error checking for duplicate patients");
            return;
        }


        Alerts.Info("Patient Successfully added");
        Stage stage = (Stage) AddPatient.getScene().getWindow();
        stage.close();
    }


}

