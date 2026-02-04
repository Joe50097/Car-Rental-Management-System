import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class Home extends Application {

    @Override
    public void start(Stage homeStage) {

        Text title = new Text("Rent-A-Car MIS");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 50));
        title.setStyle("-fx-fill: white;");

        Text subtitle = new Text("Management Information System");
        subtitle.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        subtitle.setStyle("-fx-fill: #f2f2f2;");

        ImageView logo = new ImageView(new Image("images/logo.png"));
        logo.setFitWidth(110);
        logo.setPreserveRatio(true);

        Rectangle glass = new Rectangle(550, 420);
        glass.setArcWidth(40);
        glass.setArcHeight(40);
        glass.setStyle(
                "-fx-fill: rgba(255,255,255,0.18);" +
                        "-fx-stroke: rgba(255,255,255,0.45);" +
                        "-fx-stroke-width: 1.2;"
        );
        glass.setEffect(new GaussianBlur(38));

        String buttonStyle =
                "-fx-background-color: linear-gradient(to right, #4facfe, #00f2fe);" +
                        "-fx-text-fill: black;" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-family: 'Inter';" +
                        "-fx-padding: 12 50;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 10, 0, 0, 3);";

        Button btLogin = new Button("Login");
        Button btSignup = new Button("Signup");

        btLogin.setStyle(buttonStyle);
        btSignup.setStyle(buttonStyle);

        btLogin.setOnMouseEntered(e -> btLogin.setOpacity(0.75));
        btLogin.setOnMouseExited(e -> btLogin.setOpacity(1.0));

        btSignup.setOnMouseEntered(e -> btSignup.setOpacity(0.75));
        btSignup.setOnMouseExited(e -> btSignup.setOpacity(1.0));

        btLogin.setOnAction(e -> {
            new Login().start(new Stage());
            homeStage.close();
        });

        btSignup.setOnAction(e -> {
            new Signup().start(new Stage());
            homeStage.close();
        });

        VBox vbox = new VBox(22);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25, 35, 25, 35));
        vbox.getChildren().addAll(logo, title, subtitle, btLogin, btSignup);

        StackPane stackPane = new StackPane(glass, vbox);
        stackPane.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(stackPane);
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );


        Scene scene = new Scene(root, 900, 600);
        homeStage.setScene(scene);
        homeStage.setTitle("Home");
        homeStage.setResizable(false);
        homeStage.getIcons().add(new Image("images/logo.png"));
        homeStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}