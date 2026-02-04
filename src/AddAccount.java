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
import java.sql.*;
import java.time.*;
import java.util.*;

public class AddAccount extends Application {

    private String adminUsername;
    private String adminRole;

    public AddAccount(String username, String role) {
        this.adminUsername = username;
        this.adminRole = role;
    }

    public AddAccount() {}

    @Override
    public void start(Stage addAccountStage) {

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        // glass/blur white rectangle (same as Login.java)
        Rectangle card = new Rectangle(650, 500);
        card.setArcWidth(35);
        card.setArcHeight(35);
        card.setStyle(
                "-fx-fill: rgba(255,255,255,0.20);" +
                        "-fx-stroke: rgba(255,255,255,0.55);" +
                        "-fx-stroke-width: 1.3;"
        );
        GaussianBlur blur = new GaussianBlur(40);
        card.setEffect(blur);

        StackPane cardPane = new StackPane(card);
        cardPane.setPadding(new Insets(35));

        Text title = new Text("Add New Account");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 26));

        VBox inside = new VBox(25);
        inside.setAlignment(Pos.CENTER);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        Label lblEmail = new Label("Email:");
        lblEmail.setFont(Font.font("Inter", FontWeight.BOLD, 16));

        TextField emailField = new TextField();
        emailField.setPromptText("Enter email...");
        emailField.setMaxWidth(260);

        Label lblUsername = new Label("Username:");
        lblUsername.setFont(Font.font("Inter", FontWeight.BOLD, 16));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username...");
        usernameField.setMaxWidth(260);

        Label lblPassword = new Label("Password:");
        lblPassword.setFont(Font.font("Inter", FontWeight.BOLD, 16));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password...");
        passwordField.setMaxWidth(260);

        Label lblRole = new Label("Role:");
        lblRole.setFont(Font.font("Inter", FontWeight.BOLD, 16));

        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("Admin", "Employee", "Customer");
        cbRole.setValue("Customer");
        cbRole.setMaxWidth(260);

        // same text field style as Login.java
        String textFieldStyle =
                "-fx-background-color: rgba(255,255,255,0.65);" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 8;" +
                        "-fx-font-size: 15px;";

        emailField.setStyle(textFieldStyle);
        usernameField.setStyle(textFieldStyle);
        passwordField.setStyle(textFieldStyle);

        // same style for dropdown
        cbRole.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-background-color: rgba(255,255,255,0.65);" +
                        "-fx-padding: 6;" +
                        "-fx-font-size: 15px;"
        );

        formGrid.add(lblEmail, 0, 0);
        formGrid.add(emailField, 1, 0);
        formGrid.add(lblUsername, 0, 1);
        formGrid.add(usernameField, 1, 1);
        formGrid.add(lblPassword, 0, 2);
        formGrid.add(passwordField, 1, 2);
        formGrid.add(lblRole, 0, 3);
        formGrid.add(cbRole, 1, 3);

        // lblMsg hiye label li fiya l error
        Label lblMsg = new Label();
        lblMsg.setFont(Font.font("Inter", 14));

        // heda l add account button
        Button btAdd = new Button("Add Account");
        btAdd.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);" +
                        "-fx-text-fill: black; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Inter'; -fx-padding: 12 30;" +
                        "-fx-background-radius: 25; -fx-cursor: hand;"
        );

        // heda lal hover animation
        btAdd.setOnMouseEntered(e->{
            btAdd.setOpacity(0.75);
        });
        // heda lal hover animation
        btAdd.setOnMouseExited(e->{
            btAdd.setOpacity(1.0);
        });

        btAdd.setOnAction(e -> {
            String email = emailField.getText().trim();
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();
            String role = cbRole.getValue();

            if (email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                lblMsg.setText("All fields are required!");
                lblMsg.setStyle("-fx-text-fill: red;");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {

                // check duplicates
                PreparedStatement check = conn.prepareStatement(
                        "SELECT * FROM authentication WHERE email = ? OR username = ?"
                );
                check.setString(1, email);
                check.setString(2, user);
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    lblMsg.setText("Email or Username already used!");
                    lblMsg.setStyle("-fx-text-fill: red;");
                    return;
                }

                // heda to generate l ID: yyyyMM + 3 digit random (example: 202511483)
                int generatedId = generateUniqueId(conn);

                PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO authentication (id, email, username, password, role) VALUES (?, ?, ?, ?, ?)"
                );
                insert.setInt(1, generatedId);
                insert.setString(2, email);
                insert.setString(3, user);
                insert.setString(4, pass);
                insert.setString(5, role);
                insert.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Account Created");
                alert.setHeaderText("Account Added Successfully!");
                alert.setContentText("Account created with ID: " + generatedId);
                alert.showAndWait();

                lblMsg.setText("Account added successfully!");
                lblMsg.setStyle("-fx-text-fill: green;");

                emailField.clear();
                usernameField.clear();
                passwordField.clear();

            } catch (Exception ex) {
                ex.printStackTrace();
                lblMsg.setText("Database error!");
                lblMsg.setStyle("-fx-text-fill: red;");
            }
        });

        // heda l back to dashboard button
        Button btBack = new Button("Back to Dashboard");
        btBack.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                        "-fx-text-fill: black; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Inter'; -fx-padding: 12 30;" +
                        "-fx-background-radius: 25; -fx-cursor: hand;"
        );

        btBack.setOnAction(e -> {
            new Dashboard(adminUsername, adminRole).start(new Stage());
            addAccountStage.close();
        });

        inside.getChildren().addAll(
                title,
                formGrid,
                btAdd, lblMsg,
                btBack
        );

        cardPane.getChildren().add(inside);
        root.getChildren().add(cardPane);

        Scene scene = new Scene(root, 900, 600);
        addAccountStage.setScene(scene);
        addAccountStage.setTitle("Add Account");
        addAccountStage.getIcons().add(new Image("images/logo.png"));
        addAccountStage.show();
    }

    // hayde l function to generate unique ID bi heda l format yyyyMM + 3 digit random (example: 202511483)
    // l year w l month mne5edun men l pc date
    private int generateUniqueId(Connection conn) throws SQLException {
        Random random = new Random();
        LocalDate now = LocalDate.now(); // l date taba3 l pc
        int year = now.getYear(); // 2025
        int month = now.getMonthValue(); // 11
        int prefix = year * 100 + month; // 202511

        while (true) {
            int random3 = random.nextInt(900) + 100; // 100-999 heda to generate a 3 digit random number
            int id = prefix * 1000 + random3;// 202511483 for example

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM authentication WHERE id = ?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return id; // mna3mul return lal unique ID
            }
        }
    }
}