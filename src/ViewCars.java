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

public class ViewCars extends Application {

    private TableView<Car> tableView;
    private ObservableList<Car> carList = FXCollections.observableArrayList();
    private String username;
    private String role;

    public ViewCars(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public ViewCars() {}

    @Override
    public void start(Stage stage) {

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        Text title = new Text("View All Cars");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 30));

        // hayde l combo box lal dropdown filter
        ComboBox<String> cbFilter = new ComboBox<>();
        cbFilter.getItems().addAll("All", "Available", "Rented");
        cbFilter.setValue("All");
        cbFilter.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
        cbFilter.setOnAction(e -> filterCars(cbFilter.getValue()));

        tableView = new TableView<>();
        tableView.setPrefHeight(450);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // table columns
        TableColumn<Car, String> colId = new TableColumn<>("Car ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("carID"));

        TableColumn<Car, Integer> colPrice = new TableColumn<>("Price Per Day");
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

        // car image column (80x80)
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

        tableView.getColumns().addAll(
                colId, colPrice, colYear, colPlate, colStatus, colBrand, colModel,
                colImage // add here
        );

        loadCars();

        // kel l buttons hon
        Button btRefresh = new Button("Refresh");
        btRefresh.setStyle(btnStyle());
        btRefresh.setOnAction(e -> {
            loadCars();
            filterCars(cbFilter.getValue());

            Alert refreshed = new Alert(Alert.AlertType.INFORMATION);
            refreshed.setTitle("Refreshed");
            refreshed.setHeaderText("Table refreshed successfully!");
            refreshed.showAndWait();
        });

        Button btExport = new Button("Export To CSV");
        btExport.setStyle(btnStyle());
        btExport.setOnAction(e -> exportCSV(stage));

        Button btBack = new Button("Back to Dashboard");
        btBack.setStyle(backStyle());
        btBack.setOnAction(e -> {
            new Dashboard(username, role).start(new Stage());
            stage.close();
        });

        HBox allButtons = new HBox(20, btRefresh, btExport, btBack);
        allButtons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, cbFilter, tableView, allButtons);

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("View Cars");
        stage.getIcons().add(new Image("images/logo.png"));
        stage.show();
    }

    private void loadCars() {
        carList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM cars ORDER BY carID ASC");

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
                        rs.getBytes("carImage") // load image bytes
                ));
            }

            tableView.setItems(carList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void filterCars(String filter) {
        if (filter.equals("All")) {
            tableView.setItems(carList);
            return;
        }

        ObservableList<Car> filtered = FXCollections.observableArrayList();

        for (Car c : carList) {
            if (c.getStatus().equalsIgnoreCase(filter)) {
                filtered.add(c);
            }
        }

        tableView.setItems(filtered);
    }

    private void exportCSV(Stage stage) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("cars.csv");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File file = chooser.showSaveDialog(stage);
            if (file == null) return;

            FileWriter fw = new FileWriter(file);
            fw.write("carID,price,year,plateNb,status,brand,model\n");

            for (Car car : tableView.getItems()) {
                fw.write(
                        car.getCarID() + "," +
                                car.getPrice() + "," +
                                car.getYear() + "," +
                                car.getPlateNb() + "," +
                                car.getStatus() + "," +
                                car.getBrand() + "," +
                                car.getModel() + "\n"
                );
            }

            fw.close();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Export Successful");
            success.setHeaderText("Cars exported successfully!");
            success.setContentText("Saved to:\n" + file.getAbsolutePath());
            success.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String btnStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #4facfe, #00c8ee);" +
                "-fx-text-fill:black; -fx-font-size:14px; -fx-padding:10 20;" +
                "-fx-background-radius:20;";
    }

    private String backStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                "-fx-text-fill:black; -fx-font-size:14px;" +
                "-fx-padding:10 20; -fx-background-radius:20;";
    }
}