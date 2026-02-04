import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.effect.*;

public class Dashboard extends Application {

    private String username;
    private String role;

    public Dashboard(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public Dashboard() {} // we need this to call it so we launch the stage

    @Override
    public void start(Stage dashboardStage) {

        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(25));
        root.setSpacing(25);
        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        // made card slightly smaller so it never goes past screen
        Rectangle card = new Rectangle();
        card.setWidth(650);
        card.setHeight(600); // bigger so everything fits
        card.setArcWidth(35);
        card.setArcHeight(35);

        // glass style card
        card.setFill(Color.web("rgba(255,255,255,0.20)"));
        card.setStroke(Color.web("rgba(255,255,255,0.55)"));
        card.setStrokeWidth(1.3);
        card.setEffect(new GaussianBlur(30));

        StackPane cardPane = new StackPane(card);
        cardPane.setPadding(new Insets(45));

        VBox insideBox = new VBox(25);
        insideBox.setAlignment(Pos.TOP_CENTER);

        Text welcomeText = new Text("Welcome, " + username + " (" + role + ")");
        welcomeText.setFont(Font.font("Inter", FontWeight.BOLD, 30));
        welcomeText.setFill(Color.BLACK);
        insideBox.getChildren().add(welcomeText);

        Button btViewCars = new Button("View Cars");
        btViewCars.setStyle(buttonStyle());
        btViewCars.setOnAction(e -> {
            new ViewCars(username, role).start(new Stage());
            dashboardStage.close();
        });

        insideBox.getChildren().add(btViewCars);

        // eza l role admin
        if (role.equalsIgnoreCase("Admin")) {

            Button btAddAccount = new Button("Add Account");
            btAddAccount.setStyle(buttonStyle());
            btAddAccount.setOnAction(e -> {
                new AddAccount(username, role).start(new Stage());
                dashboardStage.close();
            });

            Button btManageCars = new Button("Manage Cars");
            btManageCars.setStyle(buttonStyle());
            btManageCars.setOnAction(e -> {
                new ManageCars(username, role).start(new Stage());
                dashboardStage.close();
            });

            // admin they can manage all rentals
            Button btManageRentals = new Button("Manage Rentals");
            btManageRentals.setStyle(buttonStyle());
            btManageRentals.setOnAction(e -> {
                new ManageRentals(username, role).start(new Stage());
                dashboardStage.close();
            });

            // admin can manage all payments
            Button btManagePayments = new Button("Manage Payments");
            btManagePayments.setStyle(buttonStyle());
            btManagePayments.setOnAction(e -> {
                new ManagePayments(username, role).start(new Stage());
                dashboardStage.close();
            });

            Button btManageAccounts = new Button("View / Update / Delete Accounts");
            btManageAccounts.setStyle(buttonStyle());
            btManageAccounts.setOnAction(e -> {
                new ManageAccounts(username, role).start(new Stage());
                dashboardStage.close();
            });

            insideBox.getChildren().addAll(
                    btAddAccount,
                    btManageCars,
                    btManageRentals,
                    btManagePayments,
                    btManageAccounts
            );

        } else {
            // eza l role employee aw customer bi fut bel else
            Button btSelfUpdate = new Button("View / Update Account");
            btSelfUpdate.setStyle(buttonStyle());
            btSelfUpdate.setOnAction(e -> {
                new SelfUpdateAccount(username).start(new Stage());
                dashboardStage.close();
            });

            // employee w customer can rent a car
            Button btRentCar = new Button("Rent a Car");
            btRentCar.setStyle(buttonStyle());
            btRentCar.setOnAction(e -> {
                new ManageRentals(username, role).start(new Stage());
                dashboardStage.close();
            });

            // employee aw customer can manage THEIR payments
            Button btManagePayments = new Button("Manage Payments");
            btManagePayments.setStyle(buttonStyle());
            btManagePayments.setOnAction(e -> {
                new ManagePayments(username, role).start(new Stage());
                dashboardStage.close();
            });

            insideBox.getChildren().addAll(
                    btRentCar,
                    btManagePayments,
                    btSelfUpdate
            );
        }

        // heda l logout button
        Button btLogout = new Button("Logout");
        btLogout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                        "-fx-text-fill: black; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Inter'; -fx-padding: 12 30;" +
                        "-fx-background-radius: 25; -fx-cursor: hand;"
        );

        btLogout.setOnAction(e -> {
            try {
                new Login().start(new Stage());
                dashboardStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        insideBox.getChildren().add(btLogout);

        // heda to auto resize l rectangle card height 7asab l content inside aka l buttons
        // and we cap it so it never goes outside window
        insideBox.heightProperty().addListener((obs, oldH, newH) -> {
            double desiredHeight = newH.doubleValue() + 60;
            if (desiredHeight > 600) {
                desiredHeight = 600;
            }
            card.setHeight(desiredHeight);
        });

        cardPane.getChildren().add(insideBox);
        root.getChildren().add(cardPane);

        Scene scene = new Scene(root, 1000, 700);
        dashboardStage.setScene(scene);
        dashboardStage.setTitle("Dashboard");
        dashboardStage.setResizable(false);
        dashboardStage.getIcons().add(new Image("images/logo.png"));
        dashboardStage.show();
    }

    private String buttonStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);" +
                "-fx-text-fill: black; -fx-font-size: 16px;" +
                "-fx-font-family: 'Inter'; -fx-padding: 12 30;" +
                "-fx-background-radius: 25; -fx-cursor: hand;";
    }
}