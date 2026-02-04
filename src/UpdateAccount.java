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
import java.sql.*;
import java.util.*;

public class UpdateAccount extends Application {

    private Account account;

    public UpdateAccount(Account account) {
        this.account = account;
    }

    public UpdateAccount() {}

    @Override
    public void start(Stage updateStage) {

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        // background image
        root.setStyle(
                "-fx-background-image: url('images/background.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        Rectangle card = new Rectangle(550, 350);
        card.setArcWidth(30);
        card.setArcHeight(30);
        card.setFill(Color.web("#f0f8ffcc"));

        StackPane cardPane = new StackPane(card);
        cardPane.setPadding(new Insets(25));

        Text title = new Text("Update Account - ID: " + account.getId());
        title.setFont(Font.font("Inter", FontWeight.BOLD, 22));

        VBox inside = new VBox(20);
        inside.setAlignment(Pos.CENTER);

        Label lblInfo = new Label("Choose what you want to update:");
        lblInfo.setFont(Font.font("Inter", 16));

        Button btnUpdateEmail = new Button("Update Email");
        styleUpdateButton(btnUpdateEmail);
        btnUpdateEmail.setOnAction(e -> updateEmail());

        Button btnUpdateUsername = new Button("Update Username");
        styleUpdateButton(btnUpdateUsername);
        btnUpdateUsername.setOnAction(e -> updateUsername());

        Button btnUpdatePassword = new Button("Update Password");
        styleUpdateButton(btnUpdatePassword);
        btnUpdatePassword.setOnAction(e -> updatePassword());

        Button btnUpdateRole = new Button("Update Role");
        styleUpdateButton(btnUpdateRole);
        btnUpdateRole.setOnAction(e -> updateRole());

        Button btnClose = new Button("Close");
        btnClose.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fad0c4);" +
                        "-fx-text-fill: black; -fx-font-size: 14px;" +
                        "-fx-font-family: 'Inter'; -fx-padding: 8 20;" +
                        "-fx-background-radius: 20; -fx-cursor: hand;"
        );
        btnClose.setOnAction(e -> updateStage.close());

        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(
                btnUpdateEmail,
                btnUpdateUsername,
                btnUpdatePassword,
                btnUpdateRole,
                btnClose
        );

        inside.getChildren().addAll(title, lblInfo, buttonsBox);

        cardPane.getChildren().add(inside);
        root.getChildren().add(cardPane);

        Scene scene = new Scene(root, 600, 400);
        updateStage.setScene(scene);
        updateStage.setTitle("Update Account");
        updateStage.getIcons().add(new Image("images/logo.png"));
        updateStage.setResizable(false);
        updateStage.show();
        updateStage.setOnHidden(e -> ManageAccounts.refreshTableStatic());
    }

    private void styleUpdateButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);" +
                        "-fx-text-fill: black; -fx-font-size: 14px;" +
                        "-fx-font-family: 'Inter'; -fx-padding: 8 20;" +
                        "-fx-background-radius: 20; -fx-cursor: hand;"
        );
    }

    private void updateEmail() {
        TextInputDialog dialog = new TextInputDialog(account.getEmail());
        dialog.setTitle("Update Email");
        dialog.setHeaderText("Enter new email:");
        dialog.setContentText("Email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newEmail -> {
            newEmail = newEmail.trim();
            if (newEmail.isEmpty()) return;

            try (Connection conn = DBConnection.getConnection()) {

                PreparedStatement check = conn.prepareStatement(
                        "SELECT * FROM authentication WHERE email = ? AND id <> ?"
                );
                check.setString(1, newEmail);
                check.setInt(2, account.getId());
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    showError("Email already used!");
                    return;
                }

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE authentication SET email = ? WHERE id = ?"
                );
                ps.setString(1, newEmail);
                ps.setInt(2, account.getId());
                ps.executeUpdate();

                account.setEmail(newEmail);
                showInfo("Email updated successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Database error while updating email.");
            }
        });
    }

    private void updateUsername() {
        TextInputDialog dialog = new TextInputDialog(account.getUsername());
        dialog.setTitle("Update Username");
        dialog.setHeaderText("Enter new username:");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newUsername -> {
            newUsername = newUsername.trim();
            if (newUsername.isEmpty()) return;

            try (Connection conn = DBConnection.getConnection()) {

                PreparedStatement check = conn.prepareStatement(
                        "SELECT * FROM authentication WHERE username = ? AND id <> ?"
                );
                check.setString(1, newUsername);
                check.setInt(2, account.getId());
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    showError("Username already used!");
                    return;
                }

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE authentication SET username = ? WHERE id = ?"
                );
                ps.setString(1, newUsername);
                ps.setInt(2, account.getId());
                ps.executeUpdate();

                account.setUsername(newUsername);
                showInfo("Username updated successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Database error while updating username.");
            }
        });
    }

    private void updatePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Update Password");
        dialog.setHeaderText("Enter new password:");
        dialog.setContentText("Password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            newPassword = newPassword.trim();
            if (newPassword.isEmpty()) return;

            try (Connection conn = DBConnection.getConnection()) {

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE authentication SET password = ? WHERE id = ?"
                );
                ps.setString(1, newPassword);
                ps.setInt(2, account.getId());
                ps.executeUpdate();

                account.setPassword(newPassword);
                showInfo("Password updated successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Database error while updating password.");
            }
        });
    }

    private void updateRole() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(account.getRole(), "Admin", "Employee", "Customer");
        dialog.setTitle("Update Role");
        dialog.setHeaderText("Choose new role:");
        dialog.setContentText("Role:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            newRole = newRole.trim();
            if (newRole.isEmpty()) return;

            try (Connection conn = DBConnection.getConnection()) {

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE authentication SET role = ? WHERE id = ?"
                );
                ps.setString(1, newRole);
                ps.setInt(2, account.getId());
                ps.executeUpdate();

                account.setRole(newRole);
                showInfo("Role updated successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Database error while updating role.");
            }
        });
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }
}