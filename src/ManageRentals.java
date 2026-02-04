import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;

public class ManageRentals extends Application {

    private String username;
    private String role;

    private int loggedUserId;

    private TableView<Rental> tableView;
    private ObservableList<Rental> rentalList = FXCollections.observableArrayList();

    public ManageRentals(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public ManageRentals() {}

    @Override
    public void start(Stage manageRentalsStage) {

        loadLoggedUserId();

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        Text title = new Text("Manage Rentals");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 30));

        tableView = new TableView<>();
        tableView.setPrefHeight(420);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setEditable(false);

        // columns
        TableColumn<Rental, String> colRentId = new TableColumn<>("Rental ID");
        colRentId.setCellValueFactory(new PropertyValueFactory<>("rentalID"));
        lockColumn(colRentId);
        colRentId.setPrefWidth(240); // wider so the full ID appears

        TableColumn<Rental, String> colRentDate = new TableColumn<>("Rent Date");
        colRentDate.setCellValueFactory(new PropertyValueFactory<>("rentDate"));
        lockColumn(colRentDate);

        TableColumn<Rental, String> colReturnDate = new TableColumn<>("Return Date");
        colReturnDate.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        lockColumn(colReturnDate);

        TableColumn<Rental, Integer> colTotalPrice = new TableColumn<>("Total Price");
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        lockColumn(colTotalPrice);

        TableColumn<Rental, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        lockColumn(colStatus);

        TableColumn<Rental, String> colPaymentStatus = new TableColumn<>("Payment Status");
        colPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        lockColumn(colPaymentStatus);

        TableColumn<Rental, Integer> colUserId = new TableColumn<>("User ID");
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userID"));
        lockColumn(colUserId);

        TableColumn<Rental, String> colCarId = new TableColumn<>("Car ID");
        colCarId.setCellValueFactory(new PropertyValueFactory<>("carID"));
        lockColumn(colCarId);

        // return button
        TableColumn<Rental, Void> colReturn = new TableColumn<>("Return");
        lockColumn(colReturn);
        colReturn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Return");

            {
                btn.setStyle("-fx-background-color: #9be7ff; -fx-background-radius: 10; -fx-padding: 5 12;");
                btn.setOnAction(e -> {
                    Rental r = getTableView().getItems().get(getIndex());
                    returnRental(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Rental r = getTableView().getItems().get(getIndex());

                if (r.getStatus().equalsIgnoreCase("Active")) {
                    btn.setDisable(false); // enabled
                } else {
                    btn.setDisable(true);  // disabled
                }

                setGraphic(btn);
            }
        });

        // view info button (bass lal admins)
        TableColumn<Rental, Void> colInfo = new TableColumn<>("Info");
        lockColumn(colInfo);
        colInfo.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View");

            {
                btn.setStyle("-fx-background-color: #baffc9; -fx-background-radius: 10; -fx-padding: 5 12;");
                btn.setOnAction(e -> {
                    Rental r = getTableView().getItems().get(getIndex());
                    showUserInfo(r.getUserID());
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || !role.equalsIgnoreCase("Admin")) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        // delete button (bass lal admins)
        TableColumn<Rental, Void> colDelete = new TableColumn<>("Delete");
        lockColumn(colDelete);
        colDelete.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Delete");

            {
                btn.setStyle("-fx-background-color: #ff9a9e; -fx-background-radius: 10; -fx-padding: 5 12;");
                btn.setOnAction(e -> {
                    Rental r = getTableView().getItems().get(getIndex());
                    deleteRental(r);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || !role.equalsIgnoreCase("Admin")) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        // add columns depending on role
        if (role.equalsIgnoreCase("Admin")) {
            tableView.getColumns().addAll(
                    colRentId, colRentDate, colReturnDate, colTotalPrice,
                    colStatus, colPaymentStatus, colUserId, colCarId,
                    colReturn, colInfo, colDelete
            );
        } else {
            tableView.getColumns().addAll(
                    colRentId, colRentDate, colReturnDate, colTotalPrice,
                    colStatus, colPaymentStatus, colUserId, colCarId,
                    colReturn
            );
        }

        loadRentals();

        // buttons
        Button btAdd = new Button("Add Rental");
        btAdd.setStyle(btnStyle());
        btAdd.setOnAction(e -> openAddRentalWindow());

        Button btRefresh = new Button("Refresh");
        btRefresh.setStyle(btnStyle());
        btRefresh.setOnAction(e -> {
            loadRentals();
            alert("Refreshed", "Table refreshed successfully!", Alert.AlertType.INFORMATION);
        });

        Button btExport = new Button("Export To CSV");
        btExport.setStyle(btnStyle());
        btExport.setOnAction(e -> exportCSV(manageRentalsStage));

        Button btBack = new Button("Back to Dashboard");
        btBack.setStyle(backStyle());
        btBack.setOnAction(e -> {
            new Dashboard(username, role).start(new Stage());
            manageRentalsStage.close();
        });

        HBox topButtons = new HBox(15, btAdd, btRefresh, btExport, btBack);
        topButtons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, tableView, topButtons);

        Scene scene = new Scene(root, 1475, 800);
        manageRentalsStage.setScene(scene);
        manageRentalsStage.setTitle("Manage Rentals");
        manageRentalsStage.getIcons().add(new Image("images/logo.png"));
        manageRentalsStage.show();
    }

    // lock column size + disabling resizing + reorder
    private void lockColumn(TableColumn<?, ?> col) {
        col.setResizable(false);
        col.setReorderable(false);
        col.setPrefWidth(120);
    }

    private String btnStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);" +
                "-fx-text-fill:black; -fx-font-size:14px; -fx-padding:10 20; -fx-background-radius:20;";
    }

    private String backStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                "-fx-text-fill:black; -fx-font-size:14px; -fx-padding:10 20; -fx-background-radius:20;";
    }

    private void alert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(msg);
        a.showAndWait();
    }

    // load current user's ID
    private void loadLoggedUserId() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM authentication WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) loggedUserId = rs.getInt("id");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // load rentals (admin = all, others = only active)
    private void loadRentals() {
        rentalList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps;

            if (role.equalsIgnoreCase("Admin")) {
                ps = conn.prepareStatement(
                        "SELECT r.*, c.carID FROM rentals r " +
                                "JOIN cars c ON r.carIDFixed = c.cardIDFixed " +
                                "ORDER BY r.rentalID ASC"
                );
            } else {
                ps = conn.prepareStatement(
                        "SELECT r.*, c.carID FROM rentals r " +
                                "JOIN cars c ON r.carIDFixed = c.cardIDFixed " +
                                "WHERE r.userID=? AND r.status='Active' " +
                                "ORDER BY r.rentalID ASC"
                );
                ps.setInt(1, loggedUserId);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rentalList.add(new Rental(
                        rs.getString("rentalID"),
                        rs.getString("rentDate"),
                        rs.getString("returnDate"),
                        rs.getInt("totalPrice"),
                        rs.getString("status"),
                        rs.getString("PaymentStatus"),
                        rs.getInt("userID"),
                        rs.getInt("carIDFixed"),
                        rs.getString("carID")
                ));
            }

            tableView.setItems(rentalList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // hayde l function to generate rental ID
    private String generateRentalId(LocalDate rent, LocalDate ret) {
        DateTimeFormatter fmt = DateTimeFormatter.BASIC_ISO_DATE;
        String code = String.format("%05d", new Random().nextInt(100000));
        return "RENT-" + rent.format(fmt) + "-" + ret.format(fmt) + "-" + code;
    }

    // hayde l function to return rental
    private void returnRental(Rental rental) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Return Car");
        conf.setHeaderText("Are you sure you want to return this car?");
        conf.setContentText("Rental ID: " + rental.getRentalID());
        conf.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try (Connection conn = DBConnection.getConnection()) {

                    // set rental returned
                    PreparedStatement ps1 = conn.prepareStatement(
                            "UPDATE rentals SET status='Returned' WHERE rentalID=?"
                    );
                    ps1.setString(1, rental.getRentalID());
                    ps1.executeUpdate();

                    // make car available
                    PreparedStatement ps2 = conn.prepareStatement(
                            "UPDATE cars SET status='Available' WHERE cardIDFixed=?"
                    );
                    ps2.setInt(1, rental.getCarIDFixed());
                    ps2.executeUpdate();

                    loadRentals();
                    alert("Returned", "Car returned successfully!", Alert.AlertType.INFORMATION);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // delete rental as Admin
    private void deleteRental(Rental rental) {
        if (rental.getStatus().equalsIgnoreCase("Active")) {
            alert("Cannot Delete", "Please return the car before deleting this rental.", Alert.AlertType.ERROR);
            return;
        }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Delete Rental");
        conf.setHeaderText("Are you sure?");
        conf.setContentText("Delete rental: " + rental.getRentalID());
        conf.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try (Connection conn = DBConnection.getConnection()) {

                    // 1️⃣ DELETE ANY PAYMENT for this rental
                    PreparedStatement psDelPayments = conn.prepareStatement(
                            "DELETE FROM payments WHERE rentalID=?"
                    );
                    psDelPayments.setString(1, rental.getRentalID());
                    psDelPayments.executeUpdate();

                    // 2️⃣ DELETE THE RENTAL
                    PreparedStatement psDel = conn.prepareStatement(
                            "DELETE FROM rentals WHERE rentalID=?"
                    );
                    psDel.setString(1, rental.getRentalID());
                    psDel.executeUpdate();

                    loadRentals();
                    alert("Deleted", "Rental deleted.", Alert.AlertType.INFORMATION);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // show renter info popup (bass lal admin)
    private void showUserInfo(int userId) {
        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT username, email, role FROM authentication WHERE id=?"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                alert("Error", "User not found.", Alert.AlertType.ERROR);
                return;
            }

            String name = rs.getString("username");
            String email = rs.getString("email");
            String role = rs.getString("role");

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("User Info");
            info.setHeaderText("Renter Information:");
            info.setContentText(
                    "User ID: " + userId +
                            "\nUsername: " + name +
                            "\nEmail: " + email +
                            "\nRole: " + role
            );
            info.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // hayde l function to export rentals to CSV
    private void exportCSV(Stage stage) {
        try {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("rentals.csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File file = fc.showSaveDialog(stage);
            if (file == null) return;

            FileWriter fw = new FileWriter(file);
            fw.write("rentalID,rentDate,returnDate,totalPrice,status,PaymentStatus,userID,carID\n");

            for (Rental r : rentalList) {
                fw.write(
                        r.getRentalID() + "," +
                                r.getRentDate() + "," +
                                r.getReturnDate() + "," +
                                r.getTotalPrice() + "," +
                                r.getStatus() + "," +
                                r.getPaymentStatus() + "," +
                                r.getUserID() + "," +
                                r.getCarIDFixed() + "\n"
                );
            }

            fw.close();

            alert("Exported", "Saved to:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // add rental window
    private void openAddRentalWindow() {

        Stage addStage = new Stage();
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label lblRent = new Label("Rent Date:");
        DatePicker dpRent = new DatePicker(LocalDate.now());

        Label lblReturn = new Label("Return Date:");
        DatePicker dpReturn = new DatePicker(LocalDate.now().plusDays(1));

        Label lblCar = new Label("Select Car:");
        ComboBox<Car> cbCar = new ComboBox<>();
        cbCar.setPrefWidth(300);

        ObservableList<Car> availableCars = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT * FROM cars WHERE status='Available'"
            );

            while (rs.next()) {
                availableCars.add(new Car(
                        rs.getInt("cardIDFixed"),
                        rs.getString("carID"),
                        rs.getInt("price"),
                        rs.getInt("year"),
                        rs.getString("plateNb"),
                        rs.getString("status"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getBytes("carImage")
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        cbCar.setItems(availableCars);

        cbCar.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Car c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) setText(null);
                else setText(c.getCarID() + " - " + c.getBrand() + " " + c.getModel()
                        + " (" + c.getPrice() + " per day)");
            }
        });

        cbCar.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Car c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) setText(null);
                else setText(c.getCarID() + " - " + c.getBrand() + " " + c.getModel()
                        + " (" + c.getPrice() + " per day)");
            }
        });

        Button btSave = new Button("Save");
        btSave.setStyle(btnStyle());
        btSave.setOnAction(e -> {

            Car selected = cbCar.getValue();
            LocalDate rent = dpRent.getValue();
            LocalDate ret = dpReturn.getValue();

            if (selected == null || rent == null || ret == null) {
                alert("Error", "Fill all fields.", Alert.AlertType.ERROR);
                return;
            }

            if (!ret.isAfter(rent)) {
                alert("Error", "Return date must be after rent date.", Alert.AlertType.ERROR);
                return;
            }

            long days = ChronoUnit.DAYS.between(rent, ret);
            int totalPrice = (int) (days * selected.getPrice());

            String rentalId = generateRentalId(rent, ret);

            try (Connection conn = DBConnection.getConnection()) {

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO rentals (rentalID, rentDate, returnDate, totalPrice, status, PaymentStatus, userID, carIDFixed) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                );

                ps.setString(1, rentalId);
                ps.setDate(2, java.sql.Date.valueOf(rent));
                ps.setDate(3, java.sql.Date.valueOf(ret));
                ps.setInt(4, totalPrice);
                ps.setString(5, "Active");
                ps.setString(6, "Unpaid");
                ps.setInt(7, loggedUserId);
                ps.setInt(8, selected.getCardIDFixed());
                ps.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE cars SET status='Rented' WHERE cardIDFixed=?"
                );
                ps2.setInt(1, selected.getCardIDFixed());
                ps2.executeUpdate();

                loadRentals();

                alert("Rental Created", "Rental ID: " + rentalId, Alert.AlertType.INFORMATION);

                addStage.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(lblRent, dpRent, lblReturn, dpReturn, lblCar, cbCar, btSave);

        addStage.setScene(new Scene(box, 400, 450));
        addStage.setTitle("Add Rental");
        addStage.setResizable(false);
        addStage.getIcons().add(new Image("images/logo.png"));
        addStage.show();
    }
}