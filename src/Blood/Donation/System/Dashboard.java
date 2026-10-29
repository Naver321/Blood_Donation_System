package Blood.Donation.System;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Dashboard {

    private String role = "Admin";

    public void setRole(String userRole) {
        this.role = userRole;
    }

    public void showDashboard() {
        Stage stage = new Stage();

        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30, 10, 30, 10));
        sidebar.setStyle("-fx-background-color: #ffe6e6; -fx-background-radius: 0 15 15 0;");
        sidebar.setPrefWidth(110);
        sidebar.setAlignment(Pos.TOP_CENTER);

        addSidebarButton(sidebar, "Donor Management", "/icons/icon_donor.png", () -> new DonorManagement().show());
        if (!role.equalsIgnoreCase("Donor")) {
            addSidebarButton(sidebar, "Blood Inventory", "/icons/icon_inventory.png", () -> new BloodInventory().show());
            addSidebarButton(sidebar, "Blood Requests", "/icons/icon_requests.png", () -> new BloodRequests().show());
            addSidebarButton(sidebar, "Blood Camps", "/icons/icon_camps.png", () -> new BloodCamps().show());
            addSidebarButton(sidebar, "Donation History", "/icons/icon_history.png", () -> new DonationHistory().show());
        }
        addSidebarButton(sidebar, "Reports", "/icons/icon_reports.png", () -> new Reports().show());
        addSidebarButton(sidebar, "Receivers", "/icons/icon_receivers.png", () -> new Receivers().show());

        Button signOutBtn = new Button("⏻");
        signOutBtn.setTooltip(new Tooltip("Sign Out"));
        signOutBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 24; -fx-text-fill: #b71c1c;");
        signOutBtn.setOnAction(e -> {
            stage.close();
            new Login().start(new Stage());
        });
        sidebar.getChildren().add(signOutBtn);
        VBox.setMargin(signOutBtn, new Insets(50, 0, 0, 0));

        // Header
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/icons/dashboard_logo.gif")));
        logo.setFitWidth(80);
        logo.setPreserveRatio(true);

        Label appTitle = new Label("DonorOS Dashboard");
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        appTitle.setTextFill(Color.web("#b71c1c"));

        Label welcome = new Label("Welcome, " + role);
        welcome.setFont(Font.font("Segoe UI", 14));
        welcome.setTextFill(Color.GRAY);

        VBox header = new VBox(logo, appTitle, welcome);
        header.setAlignment(Pos.CENTER);
        header.setSpacing(12);
        header.setPadding(new Insets(30, 10, 20, 10));
        header.setStyle("-fx-background-color: #fffafa; -fx-background-radius: 15;");

        // Stats
        GridPane stats = new GridPane();
        stats.setPadding(new Insets(20));
        stats.setHgap(30);
        stats.setVgap(30);
        stats.setAlignment(Pos.TOP_CENTER);

        stats.add(createCard("Total Donors", getStat("SELECT COUNT(*) FROM donors")), 0, 0);
        stats.add(createCard("Available Units", getStat("SELECT SUM(quantity_units) FROM blood_inventory")), 1, 0);
        stats.add(createCard("Pending Requests", getStat("SELECT COUNT(*) FROM blood_requests WHERE status='Pending'")), 0, 1);
        stats.add(createCard("Completed Donations", getStat("SELECT COUNT(*) FROM donations")), 1, 1);
        stats.add(createCard("Upcoming Camps", getStat("SELECT COUNT(*) FROM blood_camps WHERE date >= CURDATE()")), 0, 2);

        VBox content = new VBox(20, header, stats);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #fff0f5;");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");

        HBox mainContent = new HBox(sidebar, scrollPane);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(mainContent, 1250, 750);
        stage.setScene(scene);
        stage.setTitle("DonorOS Dashboard - " + role);
        stage.show();
    }

    private void addSidebarButton(VBox sidebar, String label, String iconPath, Runnable action) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        icon.setFitWidth(42);
        icon.setFitHeight(42);
        Button btn = new Button("", icon);
        btn.setStyle("-fx-background-color: transparent;");
        btn.setTooltip(new Tooltip(label));
        btn.setOnAction(e -> action.run());
        btn.setPadding(new Insets(6));
        sidebar.getChildren().add(btn);
    }

    private VBox createCard(String label, int count) {
        Label countLabel = new Label(String.valueOf(count));
        countLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        countLabel.setTextFill(Color.web("#d32f2f"));

        Label name = new Label(label);
        name.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 16));
        name.setTextFill(Color.web("#333"));

        VBox box = new VBox(8, countLabel, name);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(20));
        box.setMinWidth(250);
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0.3, 0, 4);");

        TranslateTransition bounce = new TranslateTransition(Duration.seconds(1), box);
        bounce.setFromY(0);
        bounce.setToY(-5);
        bounce.setCycleCount(TranslateTransition.INDEFINITE);
        bounce.setAutoReverse(true);
        bounce.play();

        Timeline counter = new Timeline();
        counter.getKeyFrames().add(new KeyFrame(Duration.millis(25), e -> {
            int current = Integer.parseInt(countLabel.getText());
            if (current < count) {
                countLabel.setText(String.valueOf(current + 1));
            }
        }));
        counter.setCycleCount(count);
        counter.play();

        return box;
    }

    private int getStat(String query) {
        try (Connection con = Con.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}