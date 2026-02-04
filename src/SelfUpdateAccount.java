import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.sql.*;

public class SelfUpdateAccount extends Application {

    private String loggedUsername;

    private int accountId;
    private String email;
    private String username;
    private String password;
    private String role;

    public SelfUpdateAccount(String loggedUsername) {
        this.loggedUsername = loggedUsername;
    }

    @Override
    public void start(Stage selfUpdateAccountStage) {

        loadUserData();

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        Text title = new Text("Account Details");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 24));
        title.setFill(Color.BLACK);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(15);
        gridPane.setAlignment(Pos.CENTER);

        // ID (user can't edit the ID)
        Label lblId = new Label("ID:");
        lblId.setTextFill(Color.WHITE);
        lblId.setFont(Font.font("Inter", FontWeight.BOLD, 17));
        TextField tfId = new TextField(String.valueOf(accountId));
        tfId.setEditable(false);

        // email
        Label lblEmail = new Label("Email:");
        lblEmail.setTextFill(Color.WHITE);
        lblEmail.setFont(Font.font("Inter", FontWeight.BOLD, 17));
        TextField emailTextField = new TextField(email);
        emailTextField.setEditable(false);
        CheckBox cbEmail = new CheckBox("Edit");
        cbEmail.setTextFill(Color.WHITE);
        cbEmail.setFont(Font.font("Inter", FontWeight.BOLD, 15));
        cbEmail.setOnAction(e -> emailTextField.setEditable(cbEmail.isSelected()));

        // username
        Label lblUsername = new Label("Username:");
        lblUsername.setTextFill(Color.WHITE);
        lblUsername.setFont(Font.font("Inter", FontWeight.BOLD, 17));
        TextField usernameTextField = new TextField(username);
        usernameTextField.setEditable(false);
        CheckBox cbUsername = new CheckBox("Edit");
        cbUsername.setTextFill(Color.WHITE);
        cbUsername.setFont(Font.font("Inter", FontWeight.BOLD, 15));
        cbUsername.setOnAction(e -> usernameTextField.setEditable(cbUsername.isSelected()));

        // password (hidden, can show)
        Label lblPassword = new Label("Password:");
        lblPassword.setTextFill(Color.WHITE);
        lblPassword.setFont(Font.font("Inter", FontWeight.BOLD, 17));

        PasswordField passwordFieldHidden = new PasswordField();
        passwordFieldHidden.setText(password);
        passwordFieldHidden.setEditable(false);

        TextField passwordFieldShown = new TextField(password);
        passwordFieldShown.setVisible(false);
        passwordFieldShown.setEditable(false);

        CheckBox cbPassword = new CheckBox("Edit");
        cbPassword.setTextFill(Color.WHITE);
        cbPassword.setFont(Font.font("Inter", FontWeight.BOLD, 15));
        cbPassword.setOnAction(e -> {
            passwordFieldHidden.setEditable(cbPassword.isSelected());
            passwordFieldShown.setEditable(cbPassword.isSelected());
        });

        CheckBox cbShowPassword = new CheckBox("Show");
        cbShowPassword.setTextFill(Color.WHITE);
        cbPassword.setFont(Font.font("Inter", FontWeight.BOLD, 15));
        cbShowPassword.setOnAction(e -> {
            if (cbShowPassword.isSelected()) {
                passwordFieldShown.setText(passwordFieldHidden.getText());
                passwordFieldShown.setVisible(true);
                passwordFieldHidden.setVisible(false);
            } else {
                passwordFieldHidden.setText(passwordFieldShown.getText());
                passwordFieldHidden.setVisible(true);
                passwordFieldShown.setVisible(false);
            }
        });

        // role (user can't edit role)
        Label lblRole = new Label("Role:");
        lblRole.setTextFill(Color.WHITE);
        lblRole.setFont(Font.font("Inter", FontWeight.BOLD, 17));
        TextField tfRole = new TextField(role);
        tfRole.setEditable(false);

        Label roleMsg = new Label("You can't change your role.\nTo request an update, email the admin at me@joe50097.is-a.dev");
        roleMsg.setTextFill(Color.WHITE);
        roleMsg.setFont(Font.font("Inter", FontWeight.BOLD, 12));

        // hon we add kel field 3al gridPane
        gridPane.add(lblId, 0, 0);
        gridPane.add(tfId, 1, 0);

        gridPane.add(lblEmail, 0, 1);
        gridPane.add(emailTextField, 1, 1);
        gridPane.add(cbEmail, 2, 1);

        gridPane.add(lblUsername, 0, 2);
        gridPane.add(usernameTextField, 1, 2);
        gridPane.add(cbUsername, 2, 2);

        gridPane.add(lblPassword, 0, 3);
        gridPane.add(passwordFieldHidden, 1, 3); // stacked but only one visible (l user bass bi chuf we7de)
        gridPane.add(passwordFieldShown, 1, 3); // stacked but only one visible (l user bass bi chuf we7de)
        gridPane.add(cbPassword, 2, 3);
        gridPane.add(cbShowPassword, 3, 3);

        gridPane.add(lblRole, 0, 4);
        gridPane.add(tfRole, 1, 4);

        gridPane.add(roleMsg, 1, 5);

        Button btSaveClose = new Button("Save / Close");
        btSaveClose.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);" +
                        "-fx-text-fill: black; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Inter'; -fx-padding: 10 25;" +
                        "-fx-background-radius: 20; -fx-cursor: hand;"
        );

        btSaveClose.setOnAction(e -> {

            String newEmail = emailTextField.getText().trim();
            String newUsername = usernameTextField.getText().trim();
            String newPassword = cbShowPassword.isSelected()
                    ? passwordFieldShown.getText().trim()
                    : passwordFieldHidden.getText().trim();

            // keep old values if fields empty
            if (newEmail.isEmpty()) newEmail = email;
            if (newUsername.isEmpty()) newUsername = username;
            if (newPassword.isEmpty()) newPassword = password;

            // check if nothing changed
            if (newEmail.equals(email) && newUsername.equals(username) && newPassword.equals(password)) {
                new Dashboard(username, role).start(new Stage());
                selfUpdateAccountStage.close();
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {

                PreparedStatement check = conn.prepareStatement(
                        "SELECT * FROM authentication WHERE (email=? OR username=?) AND id <> ?"
                );
                check.setString(1, newEmail);
                check.setString(2, newUsername);
                check.setInt(3, accountId);
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    showError("Email or username already used!");
                    return;
                }

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE authentication SET email=?, username=?, password=? WHERE id=?"
                );
                ps.setString(1, newEmail);
                ps.setString(2, newUsername);
                ps.setString(3, newPassword);
                ps.setInt(4, accountId);
                ps.executeUpdate();

                showInfo("Account updated successfully!");
                new Dashboard(newUsername, role).start(new Stage());
                selfUpdateAccountStage.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Database connection error.");
            }
        });

        VBox box = new VBox(20, title, gridPane, btSaveClose);
        box.setAlignment(Pos.CENTER);

        root.getChildren().add(box);

        Scene scene = new Scene(root, 700, 480);
        selfUpdateAccountStage.setScene(scene);
        selfUpdateAccountStage.setTitle("Account Details");
        selfUpdateAccountStage.getIcons().add(new Image("images/logo.png"));
        selfUpdateAccountStage.show();
    }

    private void loadUserData() {
        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM authentication WHERE username=?"
            );
            ps.setString(1, loggedUsername);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                accountId = rs.getInt("id");
                email = rs.getString("email");
                username = rs.getString("username");
                password = rs.getString("password");
                role = rs.getString("role");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(msg);
        a.showAndWait();
    }
}