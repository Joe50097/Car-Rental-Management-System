import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*; // heda l import lal table
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.*;
import java.sql.*;

public class ManageAccounts extends Application {

    // heda to connect update account bel manage account so when we update an account it auto update the table
    public static ManageAccounts instance;

    // heda to connect update account bel manage account so when we update an account it auto update the table
    public static void refreshTableStatic() {
        if (instance != null) {
            instance.loadAccounts();
        }
    }

    private String adminUsername;
    private String adminRole;

    private TableView<Account> tableView;
    private ObservableList<Account> accountsList = FXCollections.observableArrayList();

    public ManageAccounts(String username, String role) {
        this.adminUsername = username;
        this.adminRole = role;
    }

    public ManageAccounts() {}

    @Override
    public void start(Stage manageStage) {

        // heda to connect update account bel manage account so when we update an account it auto update the table
        ManageAccounts.instance = this;

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        Text title = new Text("Manage Accounts");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 30));
        title.setFill(Color.BLACK);

        // heda to create the table
        tableView = new TableView<>();
        tableView.setPrefHeight(420);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setEditable(false); // heda so the user can't resize the table
        tableView.setMouseTransparent(false);
        tableView.setFocusTraversable(false);

        // table column
        TableColumn<Account, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Account, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Account, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Account, String> colPassword = new TableColumn<>("Password");
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));

        TableColumn<Account, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<Account, Void> colUpdate = new TableColumn<>("Update");
        colUpdate.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Update");

            {
                btn.setOnAction(e -> {
                    Account acc = getTableView().getItems().get(getIndex());
                    new UpdateAccount(acc).start(new Stage());
                    loadAccounts(); // auto refresh akid
                });

                btn.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);" +
                                "-fx-text-fill: black; -fx-font-size: 12px;" +
                                "-fx-font-family: 'Inter'; -fx-padding: 4 10;" +
                                "-fx-background-radius: 15; -fx-cursor: hand;"
                );
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        TableColumn<Account, Void> colDelete = new TableColumn<>("Delete");
        colDelete.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Delete");

            {
                btn.setOnAction(e -> {
                    Account acc = getTableView().getItems().get(getIndex());
                    deleteAccount(acc);
                });

                btn.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                                "-fx-text-fill: black; -fx-font-size: 12px;" +
                                "-fx-font-family: 'Inter'; -fx-padding: 4 10;" +
                                "-fx-background-radius: 15; -fx-cursor: hand;"
                );
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tableView.getColumns().addAll(colId, colEmail, colUsername, colPassword, colRole, colUpdate, colDelete);
        tableView.setItems(accountsList);

        loadAccounts();

        // heda l export button to export l accounts to csv file
        Button btExport = new Button("Export To CSV");
        btExport.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #90ee90, #32cd32);" +
                        "-fx-text-fill: black; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Inter'; -fx-padding: 10 25;" +
                        "-fx-background-radius: 25; -fx-cursor: hand;"
        );

        btExport.setOnAction(e -> exportCSV(manageStage));

        // heda l back to dashboard button
        Button btBack = new Button("Back to Dashboard");
        btBack.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                        "-fx-text-fill: black; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Inter'; -fx-padding: 10 30;" +
                        "-fx-background-radius: 25; -fx-cursor: hand;"
        );
        btBack.setOnAction(e -> {
            new Dashboard(adminUsername, adminRole).start(new Stage());
            manageStage.close();
        });

        // heda l refresh button ta na3mul refresh lal table in case ma 3emlit refresh automatically
        Button btRefresh = new Button("Refresh");
        btRefresh.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);" +
                        "-fx-text-fill: black; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Inter'; -fx-padding: 10 25;" +
                        "-fx-background-radius: 25; -fx-cursor: hand;"
        );
        btRefresh.setOnAction(e -> {
            loadAccounts();  // refresh the table

            // show l alert
            Alert refreshed = new Alert(Alert.AlertType.INFORMATION);
            refreshed.setTitle("Refreshed");
            refreshed.setHeaderText("Table refreshed successfully!");
            refreshed.showAndWait();
        });

        HBox bottomButtons = new HBox(20, btExport, btBack, btRefresh);
        bottomButtons.setAlignment(Pos.CENTER);

        VBox container = new VBox(15, title, tableView, bottomButtons);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));

        root.getChildren().add(container);

        Scene scene = new Scene(root, 1000, 600);
        manageStage.setScene(scene);
        manageStage.setTitle("Manage Accounts");
        manageStage.getIcons().add(new Image("images/logo.png"));
        manageStage.show();
    }

    // hayde l function to export to csv
    private void exportCSV(Stage stage) {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Accounts CSV");

        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));

        chooser.setInitialFileName("accounts.csv");

        File file = chooser.showSaveDialog(stage);

        if (file == null) return;

        try (FileWriter fw = new FileWriter(file)) {

            // l header taba3 l file
            fw.write("ID,Email,Username,Password,Role\n");

            // l data taba3 l file
            for (Account acc : accountsList) {
                fw.write(
                        acc.getId() + "," +
                                acc.getEmail() + "," +
                                acc.getUsername() + "," +
                                acc.getPassword() + "," +
                                acc.getRole() + "\n"
                );
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Export Successful");
            success.setHeaderText("Accounts exported successfully!");
            success.setContentText("Saved to:\n" + file.getAbsolutePath());
            success.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Failed to export CSV");
            error.setContentText("An error occurred while writing the file.");
            error.showAndWait();
        }
    }

    // hayde l function to load l accounts (admins first, then employees, sorted by ID)
    private void loadAccounts() {
        accountsList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            Statement st = conn.createStatement();

            // OPTION A: Admins first, then employees (both sorted by ID)
            ResultSet rs = st.executeQuery(
                    "SELECT id, email, username, password, role FROM authentication " +
                            "ORDER BY role ASC, id ASC"
            );

            while (rs.next()) {
                accountsList.add(new Account(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // hayde l function to delete an account
    private void deleteAccount(Account acc) {

        // prevent deleting the account you're logged into
        if (acc.getUsername().equalsIgnoreCase(adminUsername)) {
            Alert block = new Alert(Alert.AlertType.ERROR);
            block.setTitle("Error");
            block.setHeaderText("You can't delete the account you're currently using.");
            block.setContentText("Please switch to another admin account to delete this one.");
            block.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Account");
        confirm.setHeaderText("Confirm Delete");
        confirm.setContentText(
                "Delete account with ID: " + acc.getId() +
                        "\nUsername: " + acc.getUsername()
        );

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try (Connection conn = DBConnection.getConnection()) {

                    // 1️⃣ DELETE the user's credit card first
                    PreparedStatement psCard = conn.prepareStatement(
                            "DELETE FROM payment_card_details WHERE userID = ?"
                    );
                    psCard.setInt(1, acc.getId());
                    psCard.executeUpdate();

                    // 2️⃣ DELETE the account itself
                    PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM authentication WHERE id = ?"
                    );
                    ps.setInt(1, acc.getId());
                    ps.executeUpdate();

                    loadAccounts(); // refresh table

                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Deleted");
                    info.setHeaderText("Account Deleted");
                    info.setContentText("Account with ID " + acc.getId() + " deleted successfully.");
                    info.showAndWait();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}