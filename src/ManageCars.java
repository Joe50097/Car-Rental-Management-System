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
import java.util.*;

public class ManageCars extends Application {

    private String username;
    private String role;

    private TableView<Car> tableView;
    private ObservableList<Car> carList = FXCollections.observableArrayList();

    // store selected image bytes while adding/updating
    private byte[] selectedImageBytes = null;

    public ManageCars(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public ManageCars() {}

    @Override
    public void start(Stage manageCarsStage) {

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        Text title = new Text("Manage Cars");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 30));

        tableView = new TableView<>();
        tableView.setPrefHeight(420);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // table column
        TableColumn<Car, String> colId = new TableColumn<>("Car ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("carID"));

        TableColumn<Car, Integer> colPrice = new TableColumn<>("Price Per Day in $");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Car, Integer> colYear = new TableColumn<>("Year");
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Car, String> colPlate = new TableColumn<>("Plate");
        colPlate.setCellValueFactory(new PropertyValueFactory<>("plateNb"));

        TableColumn<Car, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Car, String> colBrand = new TableColumn<>("Brand");
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));

        TableColumn<Car, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));

        // image column (80x80)
        TableColumn<Car, byte[]> colImage = new TableColumn<>("Car Image");
        colImage.setCellValueFactory(new PropertyValueFactory<>("carImage"));

        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(byte[] imgBytes, boolean empty) {
                super.updateItem(imgBytes, empty);

                if (empty || imgBytes == null) {
                    setGraphic(null);
                } else {
                    try {
                        Image img = new Image(new ByteArrayInputStream(imgBytes));
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // update button
        TableColumn<Car, Void> colUpdate = new TableColumn<>("Update");
        colUpdate.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Update");

            {
                btn.setStyle("-fx-background-color: #4facfe; -fx-background-radius: 10; -fx-padding: 5 12;");
                btn.setOnAction(e -> {
                    Car c = getTableView().getItems().get(getIndex());
                    openUpdateWindow(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // delete button — NOW uses cardIDFixed instead of carID
        TableColumn<Car, Void> colDelete = new TableColumn<>("Delete");
        colDelete.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Delete");

            {
                btn.setStyle("-fx-background-color: #ff9a9e; -fx-background-radius: 10; -fx-padding: 5 12;");
                btn.setOnAction(e -> {
                    Car c = getTableView().getItems().get(getIndex());
                    deleteCar(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tableView.getColumns().addAll(
                colId, colPrice, colYear, colPlate,
                colStatus, colBrand, colModel,
                colImage,
                colUpdate, colDelete
        );

        loadCars();

        Button btAdd = new Button("Add Car");
        btAdd.setStyle(btnStyle());
        btAdd.setOnAction(e -> openAddWindow());

        Button btRefresh = new Button("Refresh");
        btRefresh.setStyle(btnStyle());
        btRefresh.setOnAction(e -> {
            loadCars();
            new Alert(Alert.AlertType.INFORMATION, "Table refreshed successfully!").showAndWait();
        });

        Button btExport = new Button("Export To CSV");
        btExport.setStyle(btnStyle());
        btExport.setOnAction(e -> exportCSV(manageCarsStage));

        Button btBack = new Button("Back to Dashboard");
        btBack.setStyle(backStyle());
        btBack.setOnAction(e -> {
            new Dashboard(username, role).start(new Stage());
            manageCarsStage.close();
        });

        HBox topButtons = new HBox(15, btAdd, btRefresh, btExport, btBack);
        topButtons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, tableView, topButtons);

        Scene scene = new Scene(root, 1100, 650);
        manageCarsStage.setScene(scene);
        manageCarsStage.setTitle("Manage Cars");
        manageCarsStage.getIcons().add(new Image("images/logo.png"));
        manageCarsStage.show();
    }

    private String btnStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);" +
                "-fx-text-fill:black; -fx-font-size:14px; -fx-padding:10 20; -fx-background-radius:20;";
    }

    private String backStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                "-fx-text-fill:black; -fx-font-size:14px; -fx-padding:10 20; -fx-background-radius:20;";
    }

    // hayde l function to generate a unique 5 digit code
    private String generateUniqueCode(Connection conn) throws Exception {
        Random rand = new Random();
        while (true) {
            int number = rand.nextInt(100000);
            String code = String.format("%05d", number);

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT carID FROM cars WHERE carID LIKE ?"
            );
            ps.setString(1, "%-" + code);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return code;
        }
    }

    // hayde l function to load cars
    private void loadCars() {
        carList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT cardIDFixed, carID, price, year, plateNb, status, brand, model, carImage FROM cars ORDER BY carID ASC");

            while (rs.next()) {
                carList.add(new Car(
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

            tableView.setItems(carList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // delete using cardIDFixed (not carID)
    private void deleteCar(Car car) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Are you sure you want to delete this car?");
        confirm.setContentText("Car ID: " + car.getCarID());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {

                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM cars WHERE cardIDFixed=?");
                    ps.setInt(1, car.getCardIDFixed());
                    ps.executeUpdate();

                    loadCars();
                    new Alert(Alert.AlertType.INFORMATION, "Car deleted successfully.").showAndWait();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    // export to csv
    private void exportCSV(Stage stage) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("cars.csv");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File file = chooser.showSaveDialog(stage);
            if (file == null) return;

            FileWriter fw = new FileWriter(file);

            fw.write("carID,price,year,plateNb,status,brand,model\n");

            for (Car c : carList) {
                fw.write(
                        c.getCarID() + "," +
                                c.getPrice() + "," +
                                c.getYear() + "," +
                                c.getPlateNb() + "," +
                                c.getStatus() + "," +
                                c.getBrand() + "," +
                                c.getModel() + "\n"
                );
            }

            fw.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "Exported Successfully!\nSaved to: " + file.getAbsolutePath()).showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // add window with image akid
    private void openAddWindow() {

        selectedImageBytes = null; // reset

        Stage addStage = new Stage();
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label lblPrice = new Label("Price Per Day In $:");
        TextField priceField = new TextField();

        Label lblYear = new Label("Year:");
        TextField yearField = new TextField();

        Label lblPlate = new Label("Plate Number:");
        TextField plateField = new TextField();

        Label lblBrand = new Label("Brand:");
        TextField brandField = new TextField();

        Label lblModel = new Label("Model:");
        TextField modelField = new TextField();

        Label lblStatus = new Label("Status:");
        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Available", "Rented");
        cbStatus.setValue("Available");

        // label to show chosen image name
        Label lblImageName = new Label("No image selected");
        lblImageName.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");

        // choose image
        Button chooseImageBtn = new Button("Choose Car Image");
        chooseImageBtn.setStyle(btnStyle());

        chooseImageBtn.setOnAction(e -> {
            try {
                FileChooser fc = new FileChooser();
                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
                );

                File file = fc.showOpenDialog(addStage);
                if (file != null) {
                    selectedImageBytes = java.nio.file.Files.readAllBytes(file.toPath());
                    lblImageName.setText("Selected: " + file.getName());
                }

            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Please choose a valid image!").show();
            }
        });

        Button saveBtn = new Button("Save");
        saveBtn.setStyle(btnStyle());
        saveBtn.setOnAction(e -> {

            // hayde validation to make sure user has filled all the fields
            if (priceField.getText().isEmpty() ||
                    yearField.getText().isEmpty() ||
                    plateField.getText().isEmpty() ||
                    brandField.getText().isEmpty() ||
                    modelField.getText().isEmpty()) {

                new Alert(Alert.AlertType.ERROR,
                        "Please fill out all fields before saving!").show();
                return;
            }

            // validate numeric values
            int price, year;
            try {
                price = Integer.parseInt(priceField.getText());
                year = Integer.parseInt(yearField.getText());
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR,
                        "Price and Year must be valid numbers!").show();
                return;
            }

            // image required
            if (selectedImageBytes == null) {
                new Alert(Alert.AlertType.ERROR,
                        "Please choose a car image!").show();
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {

                // generate ID
                String brandPrefix = brandField.getText().substring(0, 3).toUpperCase();
                String modelPrefix = modelField.getText().substring(0, 3).toUpperCase();
                String code = generateUniqueCode(conn);

                String carID = brandPrefix + "-" + modelPrefix + "-" + code;

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO cars(carID,price,year,plateNb,status,brand,model,carImage) VALUES(?,?,?,?,?,?,?,?)"
                );

                ps.setString(1, carID);
                ps.setInt(2, price);
                ps.setInt(3, year);
                ps.setString(4, plateField.getText());
                ps.setString(5, cbStatus.getValue());
                ps.setString(6, brandField.getText());
                ps.setString(7, modelField.getText());
                ps.setBytes(8, selectedImageBytes);

                ps.executeUpdate();

                loadCars();
                addStage.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(
                lblPrice, priceField,
                lblYear, yearField,
                lblPlate, plateField,
                lblBrand, brandField,
                lblModel, modelField,
                lblStatus, cbStatus,
                chooseImageBtn,
                lblImageName,
                saveBtn
        );

        addStage.setScene(new Scene(box, 400, 620));
        addStage.setTitle("Add Car");
        addStage.setResizable(false);
        addStage.getIcons().add(new Image("images/logo.png"));
        addStage.show();
    }
    // update window with image + brand/model/year updates + regenerate carID
    private void openUpdateWindow(Car car) {

        selectedImageBytes = car.getCarImage(); // preload existing image

        Stage updateStage = new Stage();
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        // SHOW CARID BUT NOT EDITABLE
        Label lblCarID = new Label("Car ID:");
        TextField carIDField = new TextField(car.getCarID());
        carIDField.setEditable(false);
        carIDField.setStyle("-fx-opacity: 0.7;");

        Label lblPrice = new Label("Price Per Day:");
        TextField priceField = new TextField(String.valueOf(car.getPrice()));

        Label lblYear = new Label("Year:");
        TextField yearField = new TextField(String.valueOf(car.getYear()));

        Label lblBrand = new Label("Brand:");
        TextField brandField = new TextField(car.getBrand());

        Label lblModel = new Label("Model:");
        TextField modelField = new TextField(car.getModel());

        Label lblPlate = new Label("Plate Number:");
        TextField plateField = new TextField(car.getPlateNb());

        Label lblStatus = new Label("Status:");
        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Available", "Rented");
        cbStatus.setValue(car.getStatus());

        // hayde label that shows the selected file name
        Label imageNameLabel = new Label("Current Image Loaded");
        imageNameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        Button chooseImageBtn = new Button("Choose New Image (Optional)");
        chooseImageBtn.setStyle(btnStyle());
        chooseImageBtn.setOnAction(e -> {
            try {
                FileChooser fc = new FileChooser();
                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
                );

                File file = fc.showOpenDialog(updateStage);
                if (file != null) {
                    selectedImageBytes = java.nio.file.Files.readAllBytes(file.toPath());
                    imageNameLabel.setText("Selected: " + file.getName());
                }

            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Please choose a valid image!").show();
            }
        });

        Button updateBtn = new Button("Update");
        updateBtn.setStyle(btnStyle());
        updateBtn.setOnAction(e -> {

            try (Connection conn = DBConnection.getConnection()) {

                // CHECK IF BRAND OR MODEL CHANGED
                boolean brandChanged = !brandField.getText().equals(car.getBrand());
                boolean modelChanged = !modelField.getText().equals(car.getModel());
                String newCarID = car.getCarID();

                if (brandChanged || modelChanged) {
                    // regenerate carID
                    String brandPrefix = brandField.getText().substring(0, 3).toUpperCase();
                    String modelPrefix = modelField.getText().substring(0, 3).toUpperCase();
                    String code = generateUniqueCode(conn);
                    newCarID = brandPrefix + "-" + modelPrefix + "-" + code;
                }

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE cars SET carID=?, price=?, year=?, plateNb=?, status=?, brand=?, model=?, carImage=? WHERE cardIDFixed=?"
                );

                ps.setString(1, newCarID);
                ps.setInt(2, Integer.parseInt(priceField.getText()));
                ps.setInt(3, Integer.parseInt(yearField.getText()));
                ps.setString(4, plateField.getText());
                ps.setString(5, cbStatus.getValue());
                ps.setString(6, brandField.getText());
                ps.setString(7, modelField.getText());
                ps.setBytes(8, selectedImageBytes);
                ps.setInt(9, car.getCardIDFixed());

                ps.executeUpdate();

                loadCars();
                updateStage.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(
                lblCarID, carIDField,
                lblPrice, priceField,
                lblYear, yearField,
                lblBrand, brandField,
                lblModel, modelField,
                lblPlate, plateField,
                lblStatus, cbStatus,
                chooseImageBtn,
                imageNameLabel,
                updateBtn
        );

        updateStage.setScene(new Scene(box, 400, 700));
        updateStage.setTitle("Update Car");
        updateStage.setResizable(false);
        updateStage.getIcons().add(new Image("images/logo.png"));
        updateStage.show();
    }
}