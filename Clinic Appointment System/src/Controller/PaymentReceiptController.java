package Controller;

import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentReceiptController {

    @FXML private Text receiptDate;
    @FXML private Text receiptNumber;
    @FXML private Text patientName;
    @FXML private Text doctorName;
    @FXML private Text serviceName;
    @FXML private Text procedureDate;
    @FXML private Text amountPaid;
    @FXML private Text paymentMethod;
    @FXML private Text paymentDate;
    @FXML private Button printButton;
    @FXML private Button closeButton;

    @FXML
    public void initialize() {
        // Set current date/time for receipt
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        receiptDate.setText("Date: " + currentDateTime);
    }

    public void setReceiptData(int paymentID, String patientName, String doctorName,
                               String serviceName, String procedureDate,
                               double amount, String paymentMethod, String paymentDate) {

        this.receiptNumber.setText("RCPT-" + String.format("%06d", paymentID));
        this.patientName.setText(patientName);
        this.doctorName.setText(doctorName);
        this.serviceName.setText(serviceName);
        this.procedureDate.setText(procedureDate);
        this.amountPaid.setText(String.format("₱%.2f", amount));
        this.paymentMethod.setText(paymentMethod);
        this.paymentDate.setText(paymentDate);
    }

    @FXML
    private void printReceipt() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean success = job.printPage(closeButton.getScene().getRoot());
            if (success) {
                job.endJob();
            }
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}