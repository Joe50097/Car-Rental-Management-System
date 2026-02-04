import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.effect.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.property.*;
import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ManagePayments extends Application {

    private String username;
    private String role;

    private TableView<Payments> table;
    private ObservableList<Payments> paymentList = FXCollections.observableArrayList();

    public ManagePayments(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public ManagePayments() {}

    private boolean isOwnerOfPayment(String rentalID) {
        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT a.username FROM rentals r " +
                            "JOIN authentication a ON r.userID = a.id " +
                            "WHERE r.rentalID=?"
            );
            stmt.setString(1, rentalID);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String owner = rs.getString("username");
                return username.equals(owner); // logged-in user matches owner
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // check if user has saved card
    private boolean userHasCard(int userID) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM payment_card_details WHERE userID=?"
            );
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // load saved card for popup
    private ResultSet loadCard(int userID) throws Exception {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM payment_card_details WHERE userID=?"
        );
        stmt.setInt(1, userID);
        return stmt.executeQuery();
    }

    // save or update card in DB
    private void saveCard(int userID, String holder, String number, String cvv, String expM, String expY) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {

            // check if exists
            PreparedStatement chk = conn.prepareStatement(
                    "SELECT cardID FROM payment_card_details WHERE userID=?"
            );
            chk.setInt(1, userID);
            ResultSet rs = chk.executeQuery();

            if (rs.next()) {
                // update
                PreparedStatement up = conn.prepareStatement(
                        "UPDATE payment_card_details SET cardHolderName=?, cardNumber=?, cvv=?, expiryMonth=?, expiryYear=? WHERE userID=?"
                );
                up.setString(1, holder);
                up.setString(2, number);
                up.setString(3, cvv);
                up.setInt(4, Integer.parseInt(expM));
                up.setInt(5, Integer.parseInt(expY));
                up.setInt(6, userID);
                up.executeUpdate();
            } else {
                // insert
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO payment_card_details(userID, cardHolderName, cardNumber, cvv, expiryMonth, expiryYear) VALUES (?, ?, ?, ?, ?, ?)"
                );
                ins.setInt(1, userID);
                ins.setString(2, holder);
                ins.setString(3, number);
                ins.setString(4, cvv);
                ins.setInt(5, Integer.parseInt(expM));
                ins.setInt(6, Integer.parseInt(expY));
                ins.executeUpdate();
            }
        }
    }

    // card validation
    private boolean isValidCard(String number, String cvv, String expM, String expY, String holder) {

        if (holder.isEmpty()) return false;

        if (!number.matches("\\d{16}")) return false;
        if (!cvv.matches("\\d{3}")) return false;
        if (!expM.matches("\\d{2}")) return false;
        if (!expY.matches("\\d{4}")) return false;

        int m = Integer.parseInt(expM);

        return m >= 1 && m <= 12;
    }

    // force user to fill card popup
    private boolean[] cardWasSaved = { false }; // array used to modify inside lambda

    // show card popup window
    private void showCardPopup(int userID) {

        Stage popup = new Stage();
        popup.setTitle("Credit Card Details");

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label lblHolder = new Label("Card Holder Name:");
        TextField holderField = new TextField();
        holderField.setPromptText("Card Holder Name");

        Label lblNumber = new Label("Card Number (16 digits):");
        TextField numberField = new TextField();
        numberField.setPromptText("1234-5678-9012-3456");
        numberField.setPrefColumnCount(19);

        // auto format credit card number (####-####-####-####)
        numberField.textProperty().addListener((obs, oldValue, newValue) -> {

            // remove non-digits
            String digits = newValue.replaceAll("\\D", "");

            // limit to 16 digits
            if (digits.length() > 16) {
                digits = digits.substring(0, 16);
            }

            // rebuild with dashes every 4 digits
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append("-");
                }
                formatted.append(digits.charAt(i));
            }

            // prevent looping
            if (!newValue.equals(formatted.toString())) {
                numberField.setText(formatted.toString());
            }
        });

        Label lblCVV = new Label("CVV:");
        PasswordField cvvField = new PasswordField();
        cvvField.setPromptText("CVV (3 digits)");

        Label lblExpMonth = new Label("Expiry Month (MM):");
        TextField expMonthField = new TextField();
        expMonthField.setPromptText("MM");

        Label lblExpYear = new Label("Expiry Year (YYYY):");
        TextField expYearField = new TextField();
        expYearField.setPromptText("YYYY");

        // load existing card
        try {
            ResultSet rs = loadCard(userID);
            if (rs.next()) {

                // format DB number into dashed form
                String num = rs.getString("cardNumber");
                String clean = num.replaceAll("\\D", "");
                String formatted = clean.replaceAll("(.{4})", "$1-");
                if (formatted.endsWith("-")) formatted = formatted.substring(0, formatted.length() - 1);

                holderField.setText(rs.getString("cardHolderName"));
                numberField.setText(formatted);
                cvvField.setText(rs.getString("cvv"));
                expMonthField.setText(String.valueOf(rs.getInt("expiryMonth")));
                expYearField.setText(String.valueOf(rs.getInt("expiryYear")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {

            String holder = holderField.getText();
            String rawNumber = numberField.getText().replaceAll("\\D", ""); // strip dashes aka remove the dashes so we only save the card number bel database
            String cvv = cvvField.getText();
            String expM = expMonthField.getText();
            String expY = expYearField.getText();

            if (!isValidCard(rawNumber, cvv, expM, expY, holder)) {
                new Alert(Alert.AlertType.ERROR, "Invalid credit card information!").show();
                return;
            }

            try {
                saveCard(userID, holder, rawNumber, cvv, expM, expY); // save raw 16 digits
                cardWasSaved[0] = true;
                popup.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(
                lblHolder, holderField,
                lblNumber, numberField,
                lblCVV, cvvField,
                lblExpMonth, expMonthField,
                lblExpYear, expYearField,
                saveBtn
        );

        popup.setScene(new Scene(box, 350, 480));
        popup.getIcons().add(new Image("images/logo.png"));
        popup.showAndWait();
    }

    @Override
    public void start(Stage stage) {

        // hayde l main pane
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        // second pane li hiye l card (GLASSMORPHISM STYLE)
        StackPane cardPane = new StackPane();

        Rectangle glass = new Rectangle(900, 550);
        glass.setArcWidth(35);
        glass.setArcHeight(35);
        glass.setStyle(
                "-fx-fill: rgba(255,255,255,0.20);" +
                        "-fx-stroke: rgba(255,255,255,0.55);" +
                        "-fx-stroke-width: 1.3;"
        );
        glass.setEffect(new GaussianBlur(40));

        VBox card = new VBox(20);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30));

        cardPane.getChildren().addAll(glass, card);

        // heda l title
        Label title = new Label("Manage Payments");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 30));

        // hayde l table
        table = new TableView<>();
        table.setPrefHeight(280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Payments, String> colID = new TableColumn<>("Payment ID");
        colID.setCellValueFactory(new PropertyValueFactory<>("paymentID"));

        TableColumn<Payments, String> colRental = new TableColumn<>("Rental ID");
        colRental.setCellValueFactory(new PropertyValueFactory<>("rentalID"));

        TableColumn<Payments, Integer> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Payments, String> colMethod = new TableColumn<>("Method");
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));

        TableColumn<Payments, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));

        TableColumn<Payments, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty("Paid"));

        table.getColumns().addAll(colID, colRental, colAmount, colMethod, colDate, colStatus);

        // only admins get a view button to view all the users payment + info but users can still view only their own payments
        TableColumn<Payments, Void> colView = new TableColumn<>("View");
        colView.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View");

            {
                btn.setMinWidth(80);
                btn.setPadding(new Insets(5, 10, 5, 10));
                btn.setStyle(
                        "-fx-background-color: #baffc9;" +
                                "-fx-background-radius: 10;" +
                                "-fx-font-size: 12px;"
                );
                btn.setOnAction(e -> {
                    Payments p = getTableView().getItems().get(getIndex());
                    viewUserInfo(p.getRentalID());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Payments p = getTableView().getItems().get(getIndex());

                // SHOW BUTTON IF:
                // Admin OR owner of the payment
                boolean show =
                        role.equalsIgnoreCase("Admin") ||
                                isOwnerOfPayment(p.getRentalID());

                setGraphic(show ? btn : null);
            }
        });

        table.getColumns().add(colView);

        loadPayments();

        // kel chi inputs
        HBox inputRow = new HBox(15);
        inputRow.setAlignment(Pos.CENTER);

        ComboBox<String> rentalIDBox = new ComboBox<>();
        rentalIDBox.setPromptText("Unpaid Rental ID");
        rentalIDBox.setPrefWidth(200);
        loadUnpaidRentals(rentalIDBox);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setEditable(false);
        amountField.setPrefWidth(120);

        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Cash", "Card");
        methodBox.setPromptText("Method");
        methodBox.setPrefWidth(120);
        // when user selects Card → instantly show popup
        methodBox.setOnAction(e -> {
            if ("Card".equals(methodBox.getValue())) {

                try (Connection conn = DBConnection.getConnection()) {

                    PreparedStatement u = conn.prepareStatement(
                            "SELECT id FROM authentication WHERE username=?"
                    );
                    u.setString(1, username);
                    ResultSet uRS = u.executeQuery();

                    int userID = -1;
                    if (uRS.next()) userID = uRS.getInt("id");

                    cardWasSaved[0] = false;
                    showCardPopup(userID);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(140);

        rentalIDBox.setOnAction(e -> autoFillAmount(rentalIDBox, amountField));

        inputRow.getChildren().addAll(rentalIDBox, amountField, methodBox, datePicker);

        // kel chi buttons
        HBox buttonsRow = new HBox(20);
        buttonsRow.setAlignment(Pos.CENTER);

        Button payBtn = new Button("Pay");
        payBtn.setMinWidth(150);
        payBtn.setStyle(primaryButton());
        payBtn.setOnAction(e -> pay(rentalIDBox, amountField, methodBox, datePicker));

        Button exportBtn = new Button("Export To CSV");
        exportBtn.setMinWidth(150);
        exportBtn.setStyle(primaryButton());
        exportBtn.setOnAction(e -> exportPayments(stage));

        if (role != null && role.equalsIgnoreCase("Admin")) {
            Button clearBtn = new Button("Clear All");
            clearBtn.setMinWidth(150);
            clearBtn.setStyle(dangerButton());
            clearBtn.setOnAction(e -> clearAllHistory());
            buttonsRow.getChildren().addAll(payBtn, exportBtn, clearBtn);
        } else {
            buttonsRow.getChildren().addAll(payBtn, exportBtn);
        }

        // heda l back to dashboard button
        Button backBtn = new Button("Back to Dashboard");
        backBtn.setMinWidth(200);
        backBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                "-fx-text-fill:black; -fx-font-size:15px;" +
                "-fx-background-radius:20; -fx-padding:10 20;");
        backBtn.setOnAction(e -> {
            new Dashboard(username, role).start(new Stage());
            stage.close();
        });

        HBox backBox = new HBox(backBtn);
        backBox.setAlignment(Pos.CENTER);

        // mna3mul add la kel chi 3al card
        card.getChildren().addAll(title, table, inputRow, buttonsRow, backBox);

        root.getChildren().add(cardPane);

        Scene scene = new Scene(root, 1000, 650);
        stage.setScene(scene);
        stage.setTitle("Manage Payments");
        stage.getIcons().add(new Image("images/logo.png"));
        stage.show();
    }

    // hawde l buttons style
    private String primaryButton() {
        return "-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);" +
                "-fx-text-fill:black; -fx-font-size:15px;" +
                "-fx-background-radius:20; -fx-padding:10 20;";
    }

    private String dangerButton() {
        return "-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                "-fx-text-fill:black; -fx-font-size:15px;" +
                "-fx-background-radius:20; -fx-padding:10 20;";
    }

    // hayde l function to load l payments
    private void loadPayments() {
        paymentList.clear();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt;

            if (role != null && role.equalsIgnoreCase("Admin")) {
                stmt = conn.prepareStatement("SELECT * FROM payments");
            } else {
                stmt = conn.prepareStatement(
                        "SELECT p.* FROM payments p " +
                                "JOIN rentals r ON p.rentalID = r.rentalID " +
                                "JOIN authentication a ON r.userID = a.id " +
                                "WHERE a.username=?"
                );
                stmt.setString(1, username);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                paymentList.add(new Payments(
                        rs.getString("paymentID"),
                        rs.getString("rentalID"),
                        rs.getInt("amount"),
                        rs.getString("method"),
                        rs.getString("paymentDate")
                ));
            }

            table.setItems(paymentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // hayde l function to load unpaid rentals li bikuno bel combox box dropdown
    private void loadUnpaidRentals(ComboBox<String> rentalIDBox) {
        rentalIDBox.getItems().clear();

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement u = conn.prepareStatement(
                    "SELECT id FROM authentication WHERE username=?"
            );
            u.setString(1, username);
            ResultSet uRS = u.executeQuery();

            int userID = -1;
            if (uRS.next()) userID = uRS.getInt("id");

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT rentalID FROM rentals WHERE userID=? AND PaymentStatus='Unpaid'"
            );
            stmt.setInt(1, userID);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) rentalIDBox.getItems().add(rs.getString("rentalID"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // hayde l function to auto fill l amount that the user must pay
    private void autoFillAmount(ComboBox<String> rentalIDBox, TextField amountField) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT totalPrice FROM rentals WHERE rentalID=?"
            );
            stmt.setString(1, rentalIDBox.getValue());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) amountField.setText(String.valueOf(rs.getInt("totalPrice")));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // hayde l function to generate l unique payment ID
    private String generateUniquePaymentID(Connection conn) throws Exception {

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Random r = new Random();

        while (true) {
            String id = "PAY-" + date + "-" + (r.nextInt(90000) + 10000);

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT paymentID FROM payments WHERE paymentID=?"
            );
            stmt.setString(1, id);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return id;
        }
    }

    // hayde l pay function
    private void pay(ComboBox<String> rentalIDBox, TextField amountField,
                     ComboBox<String> methodBox, DatePicker datePicker) {

        if (rentalIDBox.getValue() == null ||
                amountField.getText().isEmpty() ||
                methodBox.getValue() == null ||
                datePicker.getValue() == null) {

            new Alert(Alert.AlertType.WARNING, "Please complete all fields.").show();
            return;
        }

        // if card selected then user must have a saved valid card
        if ("Card".equals(methodBox.getValue())) {

            try (Connection conn = DBConnection.getConnection()) {

                PreparedStatement u = conn.prepareStatement(
                        "SELECT id FROM authentication WHERE username=?"
                );
                u.setString(1, username);
                ResultSet uRS = u.executeQuery();

                int userID = -1;
                if (uRS.next()) userID = uRS.getInt("id");

                if (!userHasCard(userID)) {
                    new Alert(Alert.AlertType.ERROR,
                            "You must add a valid credit card before paying."
                    ).showAndWait();
                    showCardPopup(userID);
                    return;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try (Connection conn = DBConnection.getConnection()) {

            String paymentID = generateUniquePaymentID(conn);

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO payments VALUES(?,?,?,?,?)"
            );
            stmt.setString(1, paymentID);
            stmt.setString(2, rentalIDBox.getValue());
            stmt.setInt(3, Integer.parseInt(amountField.getText()));
            stmt.setString(4, methodBox.getValue());
            stmt.setString(5, datePicker.getValue().toString());
            stmt.executeUpdate();

            PreparedStatement up = conn.prepareStatement(
                    "UPDATE rentals SET PaymentStatus='Paid' WHERE rentalID=?"
            );
            up.setString(1, rentalIDBox.getValue());
            up.executeUpdate();

            loadPayments();
            loadUnpaidRentals(rentalIDBox);

            rentalIDBox.setValue(null);
            amountField.clear();
            methodBox.setValue(null);
            datePicker.setValue(LocalDate.now());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // hayde l function to clear all payments/history
    private void clearAllHistory() {
        if (!role.equalsIgnoreCase("Admin")) return;

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Delete ALL payments?");
        conf.showAndWait();

        if (conf.getResult() != ButtonType.OK) return;

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement("DELETE FROM payments");
            stmt.executeUpdate();

            loadPayments();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // hayde l function to export payments to csv file
    private void exportPayments(Stage stage) {
        try {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("payments.csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File file = fc.showSaveDialog(stage);
            if (file == null) return;

            FileWriter fw = new FileWriter(file);
            fw.write("paymentID,rentalID,amount,method,paymentDate,status\n");

            for (Payments p : paymentList) {
                fw.write(
                        p.getPaymentID() + "," +
                                p.getRentalID() + "," +
                                p.getAmount() + "," +
                                p.getMethod() + "," +
                                p.getPaymentDate() + "," +
                                "Paid\n"
                );
            }

            fw.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "Payments exported to:\n" + file.getAbsolutePath()).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // hayde l function to view user info (bass l admins have access to it)
    private void viewUserInfo(String rentalID) {
        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT a.id, a.username, a.email, a.role " +
                            "FROM rentals r JOIN authentication a ON r.userID = a.id " +
                            "WHERE r.rentalID=?"
            );
            stmt.setString(1, rentalID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                int userID = rs.getInt("id");

                // fetch card
                PreparedStatement cardStmt = conn.prepareStatement(
                        "SELECT cardHolderName, cardNumber, expiryMonth, expiryYear " +
                                "FROM payment_card_details WHERE userID=?"
                );
                cardStmt.setInt(1, userID);
                ResultSet c = cardStmt.executeQuery();

                String cardInfo = "No Card On File";

                if (c.next()) {
                    String num = c.getString("cardNumber");
                    String last4 = num.length() >= 4 ? num.substring(num.length() - 4) : "XXXX";
                    cardInfo =
                            "Card Holder: " + c.getString("cardHolderName") +
                                    "\nLast 4 Digits: **** **** **** " + last4 +
                                    "\nExpiry: " + c.getInt("expiryMonth") + "/" + c.getInt("expiryYear");
                }

                new Alert(Alert.AlertType.INFORMATION,
                        "--- User Info ---" +
                                "\nUser ID: " + userID +
                                "\nUsername: " + rs.getString("username") +
                                "\nEmail: " + rs.getString("email") +
                                "\nRole: " + rs.getString("role") +
                                "\n\n--- Card Info ---\n" + cardInfo
                ).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}