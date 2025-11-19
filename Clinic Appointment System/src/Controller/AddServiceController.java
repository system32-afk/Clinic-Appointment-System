package Controller;

import Util.Alerts;
import Util.Database;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddServiceController {

    @FXML
    private TextField ServiceNameField;

    @FXML
    private TextField PriceField;

    @FXML
    private Button AddService;

    private int ServiceID=-1; //-1: default for not editing, given a value if editing

    @FXML
    public void AddService() {
        String serviceName = ServiceNameField.getText().trim();
        String priceText = PriceField.getText().trim();
        StringBuilder errors = new StringBuilder();

        // Validate service name
        if (serviceName.isEmpty()) {
            errors.append("Service name is required.\n");
        }

        // Validate price
        float price = 0;
        if (priceText.isEmpty()) {
            errors.append("Price is required.\n");
        } else {
            try {
                price = Float.parseFloat(priceText);
                if (price <= 0) {
                    errors.append("Price must be greater than zero.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Price must be a valid number.\n");
            }
        }

        // Show errors if any
        if (errors.length() > 0) {
            Alerts.Warning(errors.toString());
            return;
        }

        /*
        DUPLICATE NAME CHECK
        */
        if (this.ServiceID == -1) {  // Only check duplicates when adding
            ResultSet rs = Database.query(
                    "SELECT COUNT(*) AS count FROM service WHERE serviceName = ?",
                    serviceName
            );

            try {
                if (rs.next() && rs.getInt("count") > 0) {
                    Alerts.Warning("A service with this name already exists.\nPlease choose a different name.");
                    return;
                }
            } catch (SQLException e) {
                Alerts.Warning("Error checking for duplicate service name.");
                return;
            }
        }

        // If editing
        if (this.ServiceID != -1) {
            String sql = "UPDATE service SET serviceName = ?, price = ? WHERE serviceID = ?";
            Database.update(sql, serviceName, price, this.ServiceID);
            Alerts.Info("Service record edited successfully!");
            Stage stage = (Stage) AddService.getScene().getWindow();
            stage.close();
            return;
        }

        // If adding new
        String sql = "INSERT INTO service (serviceName, price) VALUES (?, ?)";
        Database.update(sql, serviceName, price);

        Alerts.Info("Service added successfully!");
        Stage stage = (Stage) AddService.getScene().getWindow();
        stage.close();
    }




    public void setServiceID(int serviceID) {
        this.ServiceID = serviceID;
        LoadData();
    }

    //this loads the data of the selected service for editing.
    //this is probably more efficient than having multiple duplicates of fxml for editing lol. will implement to all if I have time lol
    public void LoadData(){
        ResultSet data = Database.query("SELECT * FROM service WHERE serviceID= ?",this.ServiceID);
        try{
            while(data.next()){
                ServiceNameField.setText(data.getString("serviceName"));
                PriceField.setText(String.valueOf(data.getFloat("price")));

            }
        } catch (Exception e) {
            Alerts.Warning("There was an error loading service data.");
        }
    }
}
