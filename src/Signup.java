import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.scene.effect.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.Random;

public class Signup extends Application {

    private VBox root = new VBox();
    private VBox vBoxTextSignup = new VBox(20); // moved title slightly up
    private VBox vBoxEmailUsernamePasswordButton = new VBox(25);
    private GridPane gridPane = new GridPane();
    private StackPane stackPane = new StackPane();
    private Rectangle rectangle = new Rectangle();
    private Text textSignup = new Text("Signup");
    private Label lblEmail = new Label("Email:");
    private Label lblUsername = new Label("Username:");
    private Label lblPassword = new Label("Password:");
    private TextField emailField = new TextField();
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Button btSignup = new Button("Signup");
    private Label lblBackToLogin = new Label("Back To Login!");

    @Override
    public void start(Stage signupStage) {
        emailField.setPromptText("Enter your email...");
        emailField.setMaxWidth(250);
        usernameField.setPromptText("Enter your username...");
        usernameField.setMaxWidth(250);
        passwordField.setPromptText("Enter your password...");
        passwordField.setMaxWidth(250);

        rectangle.setWidth(480);
        rectangle.setHeight(380);
        rectangle.setArcWidth(30);
        rectangle.setArcHeight(30);
        rectangle.setStyle("-fx-fill: rgba(255,255,255,0.20); -fx-stroke: rgba(255,255,255,0.55); -fx-stroke-width: 1.3;");
        rectangle.setEffect(new GaussianBlur(40));

        textSignup.setFont(Font.font("Inter", FontWeight.BOLD, 32));

        lblEmail.setFont(Font.font("Inter", FontWeight.BOLD, 16)); // made bold
        lblUsername.setFont(Font.font("Inter", FontWeight.BOLD, 16)); // made bold
        lblPassword.setFont(Font.font("Inter", FontWeight.BOLD, 16)); // made bold
        lblBackToLogin.setFont(Font.font("Inter", FontWeight.BOLD, 16)); // made bold
        lblBackToLogin.setUnderline(true);

        emailField.setFont(Font.font("Inter", 14));
        usernameField.setFont(Font.font("Inter", 14));
        passwordField.setFont(Font.font("Inter", 14));

        emailField.setStyle("-fx-background-color: rgba(255,255,255,0.65); -fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 15px;");
        usernameField.setStyle("-fx-background-color: rgba(255,255,255,0.65); -fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 15px;");
        passwordField.setStyle("-fx-background-color: rgba(255,255,255,0.65); -fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 15px;");

        // heda l styles lal login button
        btSignup.setStyle(
                "-fx-background-color: linear-gradient(to right, #4facfe, #00f2fe);" +
                        "-fx-text-fill: black;" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-family: 'Inter';" +
                        "-fx-padding: 12 50;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.30), 10, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        );

        // heda lal hover animation
        btSignup.setOnMouseEntered(e->{
            btSignup.setOpacity(0.75);
        });
        // heda lal hover animation
        btSignup.setOnMouseExited(e->{
            btSignup.setOpacity(1.0);
        });

        // heda l set on action taba3 l login button
        btSignup.setOnAction(e->{
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String role = "Customer";

            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Please fill all the fields!");
                alert.showAndWait();
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                // check duplicates
                PreparedStatement check = conn.prepareStatement(
                        "SELECT * FROM authentication WHERE email = ? OR username = ?"
                );
                check.setString(1, email);
                check.setString(2, username);
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Email or Username already used!");
                    alert.showAndWait();
                    return;
                }

                // heda to generate l ID: yyyyMM + 3 digit random (example: 202511483)
                int generatedId = generateUniqueId(conn);

                PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO authentication (id, email, username, password, role) VALUES (?, ?, ?, ?, ?)"
                );
                insert.setInt(1, generatedId);
                insert.setString(2, email);
                insert.setString(3, username);
                insert.setString(4, password);
                insert.setString(5, role);
                insert.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Account Created");
                alert.setHeaderText("Account Added Successfully!");
                alert.setContentText("Account created with ID: " + generatedId);
                alert.showAndWait();

                emailField.clear();
                usernameField.clear();
                passwordField.clear();

                new Login().start(new Stage());
                signupStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Database connection error");
                alert.showAndWait();
            }
        });

        lblBackToLogin.setOnMouseClicked(e->{
            new Login().start(new Stage());
            signupStage.close();
        });

        gridPane.setHgap(20);
        gridPane.setVgap(15);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.add(lblEmail, 0, 0);
        gridPane.add(emailField, 1, 0);
        gridPane.add(lblUsername, 0, 1);
        gridPane.add(usernameField, 1, 1);
        gridPane.add(lblPassword, 0, 2);
        gridPane.add(passwordField, 1, 2);

        vBoxTextSignup.setAlignment(Pos.TOP_CENTER);
        vBoxTextSignup.setPadding(new Insets(5, 25, 10, 25));
        vBoxTextSignup.getChildren().add(textSignup);

        vBoxEmailUsernamePasswordButton.getChildren().addAll(gridPane, btSignup, lblBackToLogin);
        vBoxEmailUsernamePasswordButton.setAlignment(Pos.CENTER);

        stackPane.getChildren().addAll(rectangle, vBoxTextSignup, vBoxEmailUsernamePasswordButton);
        stackPane.setAlignment(Pos.CENTER);

        root.getChildren().add(stackPane);
        root.setAlignment(Pos.CENTER);

        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        Scene scene = new Scene(root, 800, 600);
        signupStage.setScene(scene);
        signupStage.setTitle("Signup");
        signupStage.setResizable(false);
        signupStage.getIcons().add(new Image("images/logo.png"));
        signupStage.show();
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