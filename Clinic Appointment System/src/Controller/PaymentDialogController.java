package Controller;

import Util.Alerts;
import Util.Database;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PaymentDialogController implements Initializable {

    @FXML private Label patientLabel;
    @FXML private Label doctorLabel;
    @FXML private Label serviceLabel;
    @FXML private Label amountLabel;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Button processButton;
    @FXML private Button cancelButton;

    private int procedureID;
    private double amount;
    private PaymentProcessingController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        paymentMethodCombo.setItems(FXCollections.observableArrayList(
                "Cash", "Credit Card", "Debit Card", "Insurance", "Other"
        ));
    }

    public void setProcedureData(int procedureID, String patientName, String doctorName, String serviceName, double amount) {
        this.procedureID = procedureID;
        this.amount = amount;

        patientLabel.setText(patientName);
        doctorLabel.setText(doctorName);
        serviceLabel.setText(serviceName);
        amountLabel.setText(String.format("₱%.2f", amount));
    }

    public void setParentController(PaymentProcessingController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void processPayment() {
        String paymentMethod = paymentMethodCombo.getValue();

        if (paymentMethod == null || paymentMethod.isEmpty()) {
            Alerts.Warning("Please select a payment method");
            return;
        }

        try {
            // Check if payment already exists
            String checkSql = "SELECT COUNT(*) as count FROM payment WHERE ProcedureID = ?";
            ResultSet checkRs = Database.query(checkSql, procedureID);

            if (checkRs != null && checkRs.next() && checkRs.getInt("count") > 0) {
                Alerts.Warning("Payment for this procedure has already been processed");
                return;
            }

            // Insert payment record
            String sql = "INSERT INTO payment (ProcedureID, PaymentDate, AmountDue, ModeOfPayment) VALUES (?, ?, ?, ?)";
            int result = Database.update(sql, procedureID, LocalDate.now(), amount, paymentMethod);

            if (result > 0) {
                Alerts.Info("Payment processed successfully!");

                // Refresh parent controller data
                if (parentController != null) {
                    parentController.refreshData();
                }

                closeWindow();
            } else {
                Alerts.Warning("Failed to process payment");
            }
        } catch (Exception e) {
            Alerts.Warning("Error processing payment: " + e.getMessage());
        }
    }

    @FXML
    private void cancelPayment() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) processButton.getScene().getWindow();
        stage.close();
    }
}
