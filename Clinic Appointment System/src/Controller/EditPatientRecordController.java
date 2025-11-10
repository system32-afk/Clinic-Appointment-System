package Controller;

import Util.Alerts;
import Util.Database;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EditPatientRecordController {

    private int patientID;

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
    private Button EditPatient;


    public void setPatientID(int patientID) {
        this.patientID = patientID;
        loadPatientData();
    }

    private void loadPatientData() {
        String sql = "SELECT * FROM patient WHERE PatientID = ?";
        try {
            ResultSet rs = Database.query(sql, patientID);
            if (rs != null && rs.next()) {
                FirstNameField.setText(rs.getString("FirstName"));
                LastNameField.setText(rs.getString("LastName"));
                ContactNumberField.setText(rs.getString("ContactNumber"));
                AgeField.setText(String.valueOf(rs.getInt("Age")));
                BuildingNoField.setText(rs.getString("BuildingNo"));
                BrgyField.setText(rs.getString("BarangayNo"));
                StreetField.setText(rs.getString("Street"));
                CityField.setText(rs.getString("City"));
                ProvinceField.setText(rs.getString("Province"));
                SexField.setValue(rs.getString("Sex"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void UpdateRecord(){
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

        String Update = """
                UPDATE patient 
                SET FirstName = ?, 
                    LastName = ?, 
                    Age = ?,
                    Sex = ?,
                    ContactNumber = ?,  
                    BuildingNo = ?, 
                    Street = ?, 
                    BarangayNo = ?,
                    City = ?, 
                    Province = ?
                WHERE PatientID = ?
            """;


        Database.update(Update,FirstName, LastName, Age, Sex, ContactNumber, BuildingNo, Street, BarangayNo, city, province,patientID);

        Alerts.Info("Patient Record Updated!");
        Stage stage = (Stage) EditPatient.getScene().getWindow();
        stage.close();


    }
}
