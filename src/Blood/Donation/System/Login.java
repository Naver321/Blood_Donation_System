package Blood.Donation.System;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login {

    public void start(Stage stage) {
        show(stage);
    }

    public void show(Stage primaryStage) {
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/icons/blood.gif")));
        logoView.setFitHeight(200);
        logoView.setPreserveRatio(true);
        logoView.setEffect(new DropShadow(25, Color.DARKRED));

        Label heading = new Label("DonorOS");
        heading.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 24));
        heading.setTextFill(Color.web("#b00020"));

        TextField userField = new TextField();
        userField.setPromptText("👤 Username");
        userField.setStyle("-fx-background-radius: 8; -fx-border-color: #d50000; -fx-border-radius: 8;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("🔒 Password");
        passField.setStyle("-fx-background-radius: 8; -fx-border-color: #d50000; -fx-border-radius: 8;");

        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);

        Button loginBtn = new Button("🔓 Login");
        Button registerBtn = new Button("🩸 Register Donor");

        loginBtn.setStyle("""
            -fx-background-color: linear-gradient(to right, #b71c1c, #880e4f);
            -fx-text-fill: white;
            -fx-background-radius: 30;
            -fx-font-weight: bold;
            -fx-padding: 10 22;
        """);

        registerBtn.setStyle("""
            -fx-background-color: linear-gradient(to right, #c2185b, #d81b60);
            -fx-text-fill: white;
            -fx-background-radius: 30;
            -fx-font-weight: bold;
            -fx-padding: 10 22;
        """);

        loginBtn.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both fields");
                return;
            }

            try {
                Con con = new Con();
                Connection c = con.connection;
                PreparedStatement ps = c.prepareStatement("SELECT role, password FROM users WHERE username = ?");
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String role = rs.getString("role");
                    if (storedHash.equals(hashPassword(password))) {
                        Dashboard dashboard = new Dashboard();
                        dashboard.setRole(role);
                        dashboard.showDashboard();
                        primaryStage.close();
                    } else {
                        messageLabel.setText("Invalid credentials");
                    }
                } else {
                    messageLabel.setText("User not found");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        registerBtn.setOnAction(e -> showRegisterForm());

        VBox form = new VBox(12, heading, userField, passField, messageLabel, loginBtn, registerBtn);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(25));
        form.setMaxWidth(360);
        form.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.96);
            -fx-background-radius: 22;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 12);
        """);

        VBox card = new VBox(20, logoView, form);
        card.setAlignment(Pos.CENTER);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        ScaleTransition grow = new ScaleTransition(Duration.seconds(1), logoView);
        grow.setFromX(0.7);
        grow.setFromY(0.7);
        grow.setToX(1);
        grow.setToY(1);
        grow.play();

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffebee, #ffcdd2);");

        Scene scene = new Scene(root, 560, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login - Blood Donation System");
        primaryStage.show();
    }

    private void showRegisterForm() {
        Stage regStage = new Stage();
        regStage.setTitle("Register Donor Account");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("Admin", "Blood Bank Staff", "Donor"));
        Label msg = new Label();
        msg.setTextFill(Color.RED);

        Button createBtn = new Button("✅ Create");
        createBtn.setStyle("-fx-background-color: linear-gradient(to right, #b71c1c, #d32f2f); -fx-text-fill: white; -fx-font-weight: bold;");

        createBtn.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = roleBox.getValue();
            if (username.isEmpty() || password.isEmpty() || role == null) {
                msg.setText("All fields are required");
                return;
            }
            try {
                Con con = new Con();
                Connection c = con.connection;
                PreparedStatement ps = c.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
                ps.setString(1, username);
                ps.setString(2, hashPassword(password));
                ps.setString(3, role);
                ps.executeUpdate();
                msg.setTextFill(Color.GREEN);
                msg.setText("Account created successfully");
            } catch (Exception ex) {
                msg.setText("Username may already exist");
                ex.printStackTrace();
            }
        });

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roleBox, 1, 2);
        grid.add(createBtn, 1, 3);
        grid.add(msg, 1, 4);

        Scene scene = new Scene(grid, 370, 260);
        regStage.setScene(scene);
        regStage.show();
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
