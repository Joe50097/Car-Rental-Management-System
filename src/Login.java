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

public class Login extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private VBox root = new VBox();
    private HBox hBoxUsername = new HBox(15);
    private HBox hBoxPassword = new HBox(15);
    private VBox vBoxTextLogin = new VBox(25);
    private VBox vBoxUsernamePasswordButton = new VBox(25);
    private StackPane stackPane = new StackPane();
    private Rectangle rectangle = new Rectangle();
    private Text textLogin = new Text("Login");
    private Label lblUsername = new Label("Username:");
    private Label lblPassword = new Label("Password:");
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Button btLogin = new Button("Login");
    private Label lblSignup = new Label("Don't have an account? Signup!");

    @Override
    public void start(Stage primaryStage) {

        usernameField.setPromptText("Enter your username...");
        usernameField.setMaxWidth(260);
        passwordField.setPromptText("Enter your password...");
        passwordField.setMaxWidth(260);

        // glass/blur white rectangle
        rectangle.setWidth(500);
        rectangle.setHeight(420);
        rectangle.setArcWidth(35);
        rectangle.setArcHeight(35);

        rectangle.setStyle(
                "-fx-fill: rgba(255, 255, 255, 0.20);" +
                        "-fx-stroke: rgba(255,255,255,0.55);" +
                        "-fx-stroke-width: 1.3;"
        );

        GaussianBlur blur = new GaussianBlur(40);
        rectangle.setEffect(blur);

        textLogin.setFont(Font.font("Inter", FontWeight.BOLD, 34));

        // labels bold for visibility
        lblUsername.setFont(Font.font("Inter", FontWeight.BOLD, 17));
        lblPassword.setFont(Font.font("Inter", FontWeight.BOLD, 17));
        lblSignup.setFont(Font.font("Inter", FontWeight.BOLD, 15));
        lblSignup.setUnderline(true);

        // text field styles
        String textFieldStyle =
                "-fx-background-color: rgba(255,255,255,0.65);" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 8;" +
                        "-fx-font-size: 15px;";

        usernameField.setStyle(textFieldStyle);
        passwordField.setStyle(textFieldStyle);

        // button style
        btLogin.setStyle(
                "-fx-background-radius: 20;" +
                        "-fx-background-color: linear-gradient(to right, #4facfe, #00f2fe);" +
                        "-fx-text-fill: black;" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-family: 'Inter';" +
                        "-fx-padding: 12 50;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.30), 10, 0, 0, 3);"
        );

        // heda lal hover animation
        btLogin.setOnMouseEntered(e->{
            btLogin.setOpacity(0.75);
        });
        // heda lal hover animation
        btLogin.setOnMouseExited(e->{
            btLogin.setOpacity(1.0);
        });

        // heda l set on action taba3 l login button
        btLogin.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Please fill all the fields!");
                alert.showAndWait();
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT * FROM authentication WHERE username = ? AND password = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    new Dashboard(username, rs.getString("role")).start(new Stage());
                    ((Stage) btLogin.getScene().getWindow()).close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid username or password!");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Database connection error");
                alert.showAndWait();
            }
        });

        lblSignup.setOnMouseClicked(e -> {
            new Signup().start(new Stage());
            ((Stage) btLogin.getScene().getWindow()).close();
        });

        // layouts
        hBoxUsername.getChildren().addAll(lblUsername, usernameField);
        hBoxUsername.setAlignment(Pos.CENTER);

        hBoxPassword.getChildren().addAll(lblPassword, passwordField);
        hBoxPassword.setAlignment(Pos.CENTER);

        vBoxTextLogin.setAlignment(Pos.TOP_CENTER);
        vBoxTextLogin.setPadding(new Insets(30));
        vBoxTextLogin.getChildren().add(textLogin);

        vBoxUsernamePasswordButton.getChildren().addAll(
                hBoxUsername, hBoxPassword, btLogin, lblSignup
        );
        vBoxUsernamePasswordButton.setAlignment(Pos.CENTER);
        vBoxUsernamePasswordButton.setSpacing(18);

        stackPane.getChildren().addAll(rectangle, vBoxTextLogin, vBoxUsernamePasswordButton);
        stackPane.setAlignment(Pos.CENTER);

        root.getChildren().add(stackPane);
        root.setAlignment(Pos.CENTER);

        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("images/logo.png"));
        primaryStage.show();
    }
}