package Blood.Donation.System;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;

public class Reports {

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("📊 Reports Dashboard");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #ffe6e6;");

        Label title = new Label("📊 Blood Donation Reports");
        title.setFont(new Font("Segoe UI Semibold", 26));
        title.setTextFill(Color.DARKRED);

        Button donorsBtn = new Button("👥 Donor Statistics");
        Button bloodTypeBtn = new Button("🩸 Blood Inventory Distribution");
        Button requestsBtn = new Button("📄 Blood Requests Summary");

        for (Button btn : new Button[]{donorsBtn, bloodTypeBtn, requestsBtn}) {
            btn.setPrefWidth(280);
            btn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;");
        }

        donorsBtn.setOnAction(e -> showDonorPieChart());
        bloodTypeBtn.setOnAction(e -> showBloodTypeChart());
        requestsBtn.setOnAction(e -> showRequestTable());

        layout.getChildren().addAll(title, donorsBtn, bloodTypeBtn, requestsBtn);
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 400, 350);
        stage.setScene(scene);
        stage.show();
    }

    private void showDonorPieChart() {
        Stage stage = new Stage();
        stage.setTitle("Donor Statistics");

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Total Donations per Donor");

        try {
            Connection con = Con.getConnection();
            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT donor_id, COUNT(*) as total FROM donations GROUP BY donor_id");
            while (rs.next()) {
                pieChart.getData().add(new PieChart.Data("Donor " + rs.getInt("donor_id"), rs.getInt("total")));
            }
        } catch (Exception e) {
            showError("Error loading donor stats.");
        }

        VBox layout = new VBox(15, pieChart);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(layout, 500, 400));
        stage.show();
    }

    private void showBloodTypeChart() {
        Stage stage = new Stage();
        stage.setTitle("Blood Type Inventory");

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Available Units by Blood Type");

        try {
            Connection con = Con.getConnection();
            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT blood_type, SUM(quantity_units) as total FROM blood_inventory GROUP BY blood_type");
            while (rs.next()) {
                pieChart.getData().add(new PieChart.Data(rs.getString("blood_type"), rs.getInt("total")));
            }
        } catch (Exception e) {
            showError("Error loading blood type chart.");
        }

        VBox layout = new VBox(15, pieChart);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(layout, 500, 400));
        stage.show();
    }

    private void showRequestTable() {
        Stage stage = new Stage();
        stage.setTitle("📄 Blood Requests Report");

        Label header = new Label("📄 All Blood Requests");
        header.setFont(new Font("Segoe UI Semibold", 20));
        header.setTextFill(Color.DARKRED);

        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        TableColumn<String[], String> nameCol = new TableColumn<>("Receiver");
        TableColumn<String[], String> bloodCol = new TableColumn<>("Blood Type");
        TableColumn<String[], String> qtyCol = new TableColumn<>("Units");
        TableColumn<String[], String> statusCol = new TableColumn<>("Status");

        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        bloodCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));

        table.getColumns().addAll(nameCol, bloodCol, qtyCol, statusCol);

        ObservableList<String[]> rows = FXCollections.observableArrayList();
        try {
            Connection con = Con.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT receiver_name, blood_type, quantity_units, status FROM blood_requests");
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("receiver_name"),
                        rs.getString("blood_type"),
                        rs.getString("quantity_units"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            showError("Error loading blood requests.");
        }

        table.setItems(rows);

        VBox layout = new VBox(15, header, table);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #fff5f7, #ffeef0);");
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 620, 420);
        stage.setScene(scene);
        stage.show();
    }


    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Something went wrong");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
