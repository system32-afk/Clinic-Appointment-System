package Controller;

import Util.Alerts;
import Util.Database;
import Util.SceneManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PaymentProcessingController implements Initializable {

    @FXML private Text Date;
    @FXML private Text Time;

    @FXML private Label totalPaymentsLabel;
    @FXML private Label completedPaymentsLabel;
    @FXML private Label pendingPaymentsLabel;

    @FXML private VBox paymentListContainer;
    @FXML private ComboBox<String> filterComboBox;

    private ObservableList<String> appointmentList = FXCollections.observableArrayList();
    private int selectedAppointmentID = -1;
    private double serviceAmount = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateDateTime();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateTime()));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        setupFilters();
        loadPaymentStatistics();
        loadPendingPayments();
    }

    private void setupFilters() {
        filterComboBox.getItems().addAll("All", "Pending", "Completed", "Today");
        filterComboBox.setValue("All");
        filterComboBox.setOnAction(e -> loadPendingPayments());
    }

    private void loadPaymentStatistics() {
        try {
            ResultSet totalStats = Database.query(
                    "SELECT COUNT(*) as total FROM payment"
            );
            if (totalStats.next()) {
                totalPaymentsLabel.setText(String.valueOf(totalStats.getInt("total")));
            }

            ResultSet completedStats = Database.query(
                    "SELECT COUNT(*) as completed FROM payment WHERE PaymentDate = ?",
                    LocalDate.now()
            );
            if (completedStats.next()) {
                completedPaymentsLabel.setText(String.valueOf(completedStats.getInt("completed")));
            }

            ResultSet pendingStats = Database.query(
                    "SELECT COUNT(*) as pending FROM appointment WHERE Status = 'Completed' " +
                            "AND AppointmentID NOT IN (SELECT AppointmentID FROM payment)"
            );
            if (pendingStats.next()) {
                pendingPaymentsLabel.setText(String.valueOf(pendingStats.getInt("pending")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPendingPayments() {
        try {
            String filter = filterComboBox.getValue();
            StringBuilder query = new StringBuilder(
                    "SELECT a.AppointmentID, p.FirstName, p.LastName, " +
                            "d.FirstName as DocFirstName, d.LastName as DocLastName, " +
                            "s.ServiceName, s.Price, a.Date, a.Time, a.Status, " +
                            "CASE WHEN pay.PaymentID IS NULL THEN 'Pending' ELSE 'Completed' END as PaymentStatus " +
                            "FROM appointment a " +
                            "JOIN patient p ON a.PatientID = p.PatientID " +
                            "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                            "JOIN service s ON a.ServiceNeeded = s.ServiceID " +
                            "LEFT JOIN payment pay ON a.AppointmentID = pay.AppointmentID " +
                            "WHERE a.Status = 'Completed' "
            );

            switch (filter) {
                case "Pending":
                    query.append("AND pay.PaymentID IS NULL ");
                    break;
                case "Completed":
                    query.append("AND pay.PaymentID IS NOT NULL ");
                    break;
                case "Today":
                    query.append("AND a.Date = ? ");
                    break;
            }

            query.append("ORDER BY a.Date DESC, a.Time DESC");

            ResultSet appointments;
            if ("Today".equals(filter)) {
                appointments = Database.query(query.toString(), LocalDate.now());
            } else {
                appointments = Database.query(query.toString());
            }

            paymentListContainer.getChildren().clear();

            boolean hasRecords = false;
            while (appointments.next()) {
                hasRecords = true;
                createPaymentRow(appointments);
            }

            if (!hasRecords) {
                Label noRecords = new Label("No payments found for the selected filter.");
                noRecords.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
                paymentListContainer.getChildren().add(noRecords);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.Warning("Error loading payments: " + e.getMessage());
        }
    }

    private void createPaymentRow(ResultSet appointment) throws SQLException {
        int appointmentID = appointment.getInt("AppointmentID");
        String patientName = appointment.getString("FirstName") + " " + appointment.getString("LastName");
        String doctorName = "Dr. " + appointment.getString("DocFirstName") + " " + appointment.getString("DocLastName");
        String serviceName = appointment.getString("ServiceName");
        double price = appointment.getDouble("Price");
        String time = appointment.getTime("Time").toString();
        String paymentStatus = appointment.getString("PaymentStatus");

        VBox leftInfo = new VBox(5);
        leftInfo.setAlignment(Pos.CENTER_LEFT);

        Label patientLabel = new Label(patientName);
        patientLabel.setFont(Font.font("Arial", 16));
        patientLabel.setStyle("-fx-font-weight: bold;");

        Label doctorLabel = new Label(doctorName);
        doctorLabel.setFont(Font.font("Arial", 12));
        doctorLabel.setStyle("-fx-text-fill: gray;");

        Label serviceLabel = new Label(serviceName);
        serviceLabel.setFont(Font.font("Arial", 12));
        serviceLabel.setStyle("-fx-text-fill: #666;");

        leftInfo.getChildren().addAll(patientLabel, doctorLabel, serviceLabel);

        VBox centerInfo = new VBox(5);
        centerInfo.setAlignment(Pos.CENTER);

        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("Arial", 14));

        Label amountLabel = new Label(String.format("₱%.2f", price));
        amountLabel.setFont(Font.font("Arial", 16));
        amountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E8B57;");

        centerInfo.getChildren().addAll(timeLabel, amountLabel);

        VBox rightInfo = new VBox(10);
        rightInfo.setAlignment(Pos.CENTER_RIGHT);

        Label statusLabel = new Label(paymentStatus);
        statusLabel.setFont(Font.font("Arial", 12));
        statusLabel.setPadding(new Insets(5, 15, 5, 15));
        updatePaymentStatusColor(statusLabel, paymentStatus);

        Button actionButton = new Button();
        actionButton.setFont(Font.font("Arial", 12));
        actionButton.setPadding(new Insets(8, 20, 8, 20));

        if ("Pending".equals(paymentStatus)) {
            actionButton.setText("Process Payment");
            actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            actionButton.setOnAction(e -> openPaymentDialog(appointmentID, patientName, doctorName, serviceName, price));
        } else {
            actionButton.setText("View Receipt");
            actionButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            actionButton.setOnAction(e -> viewPaymentReceipt(appointmentID));
        }

        rightInfo.getChildren().addAll(statusLabel, actionButton);

        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setStyle("-fx-background-color: #FFFFFF; -fx-border-radius: 5; -fx-background-radius: 5;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(leftInfo, spacer, centerInfo, rightInfo);

        Line separator = new Line(0, 0, 900, 0);
        separator.setStroke(Color.LIGHTGRAY);
        separator.setStrokeWidth(1);

        VBox rowWithLine = new VBox();
        rowWithLine.getChildren().addAll(row, separator);

        paymentListContainer.getChildren().add(rowWithLine);
    }

    private void updatePaymentStatusColor(Label label, String status) {
        String color;
        switch (status) {
            case "Pending" -> color = "#FFA500";
            case "Completed" -> color = "#4CAF50";
            default -> color = "#E0E0E0";
        }
        label.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-font-weight: bold;"
        );
    }

    private void openPaymentDialog(int appointmentID, String patientName, String doctorName, String serviceName, double amount) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Process Payment");
        dialog.setHeaderText("Payment Processing");

        ButtonType processButtonType = new ButtonType("Process Payment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(processButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        Label patientLabel = new Label("Patient:");
        Label patientValue = new Label(patientName);

        Label doctorLabel = new Label("Doctor:");
        Label doctorValue = new Label(doctorName);

        Label serviceLabel = new Label("Service:");
        Label serviceValue = new Label(serviceName);

        Label amountLabel = new Label("Amount Due:");
        Label amountValue = new Label(String.format("₱%.2f", amount));
        amountValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E8B57;");

        TextField amountPaidField = new TextField();
        amountPaidField.setPromptText("Enter amount paid");

        Label changeLabel = new Label("Change: ₱0.00");
        changeLabel.setStyle("-fx-text-fill: #666;");

        amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double amountPaid = Double.parseDouble(newValue);
                double change = amountPaid - amount;
                if (change >= 0) {
                    changeLabel.setText(String.format("Change: ₱%.2f", change));
                    changeLabel.setStyle("-fx-text-fill: #4CAF50;");
                } else {
                    changeLabel.setText(String.format("Balance: ₱%.2f", Math.abs(change)));
                    changeLabel.setStyle("-fx-text-fill: #FF5722;");
                }
            } catch (NumberFormatException e) {
                changeLabel.setText("Change: ₱0.00");
                changeLabel.setStyle("-fx-text-fill: #666;");
            }
        });

        grid.add(patientLabel, 0, 0);
        grid.add(patientValue, 1, 0);
        grid.add(doctorLabel, 0, 1);
        grid.add(doctorValue, 1, 1);
        grid.add(serviceLabel, 0, 2);
        grid.add(serviceValue, 1, 2);
        grid.add(amountLabel, 0, 3);
        grid.add(amountValue, 1, 3);
        grid.add(new Label("Amount Paid:"), 0, 4);
        grid.add(amountPaidField, 1, 4);
        grid.add(changeLabel, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Button processButton = (Button) dialog.getDialogPane().lookupButton(processButtonType);
        processButton.setDisable(true);

        amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double amountPaid = Double.parseDouble(newValue);
                processButton.setDisable(amountPaid < amount);
            } catch (NumberFormatException e) {
                processButton.setDisable(true);
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == processButtonType) {
                try {
                    double amountPaid = Double.parseDouble(amountPaidField.getText());

                    Database.update(
                            "INSERT INTO payment (AppointmentID, PatientID, DoctorID, ServiceAvailed, PaymentDate, AmountDue) " +
                                    "SELECT AppointmentID, PatientID, DoctorID, ServiceNeeded, ?, ? " +
                                    "FROM appointment WHERE AppointmentID = ?",
                            LocalDate.now(), amount, appointmentID
                    );

                    Alerts.Info("Payment processed successfully!\n" +
                            String.format("Amount: ₱%.2f\nChange: ₱%.2f",
                                    amountPaid, amountPaid - amount));

                    return true;
                } catch (NumberFormatException | SQLException e) {
                    Alerts.Warning("Error processing payment: " + e.getMessage());
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                loadPaymentStatistics();
                loadPendingPayments();
            }
        });
    }

    private void viewPaymentReceipt(int appointmentID) {
        try {
            ResultSet paymentData = Database.query(
                    "SELECT p.*, pat.FirstName, pat.LastName, " +
                            "doc.FirstName as DocFirstName, doc.LastName as DocLastName, " +
                            "s.ServiceName " +
                            "FROM payment p " +
                            "JOIN appointment a ON p.AppointmentID = a.AppointmentID " +
                            "JOIN patient pat ON p.PatientID = pat.PatientID " +
                            "JOIN doctor doc ON p.DoctorID = doc.DoctorID " +
                            "JOIN service s ON p.ServiceAvailed = s.ServiceID " +
                            "WHERE p.AppointmentID = ?",
                    appointmentID
            );

            if (paymentData.next()) {
                StringBuilder receipt = new StringBuilder();
                receipt.append("=== PAYMENT RECEIPT ===\n\n");
                receipt.append("Receipt #: ").append(paymentData.getInt("PaymentID")).append("\n");
                receipt.append("Date: ").append(paymentData.getDate("PaymentDate")).append("\n");
                receipt.append("Patient: ").append(paymentData.getString("FirstName")).append(" ")
                        .append(paymentData.getString("LastName")).append("\n");
                receipt.append("Doctor: Dr. ").append(paymentData.getString("DocFirstName")).append(" ")
                        .append(paymentData.getString("DocLastName")).append("\n");
                receipt.append("Service: ").append(paymentData.getString("ServiceName")).append("\n");
                receipt.append("Amount: ").append(String.format("₱%.2f", paymentData.getDouble("AmountDue"))).append("\n");
                receipt.append("\nThank you for your payment!");

                Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
                receiptAlert.setTitle("Payment Receipt");
                receiptAlert.setHeaderText("Payment Details");
                receiptAlert.setContentText(receipt.toString());
                receiptAlert.showAndWait();
            }
        } catch (SQLException e) {
            Alerts.Warning("Error loading receipt: " + e.getMessage());
        }
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        Date.setText(dateFormatter.format(now));
        Time.setText(timeFormatter.format(now));
    }

    public void DashboardScreen(ActionEvent e) throws IOException {
        SceneManager.transition(e, "Dashboard");
    }

    @FXML
    private void refreshPayments() {
        loadPaymentStatistics();
        loadPendingPayments();
        Alerts.Info("Payments list refreshed!");
    }
}
