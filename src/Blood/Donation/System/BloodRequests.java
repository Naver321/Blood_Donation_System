package Blood.Donation.System;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class BloodRequests {

    private final ObservableList<BloodRequest> requestList = FXCollections.observableArrayList();
    private TableView<BloodRequest> table;

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("🩸 Blood Requests");

        Label title = new Label("🩸 Blood Request Management");
        title.setFont(new Font("Segoe UI Semibold", 24));
        title.setTextFill(Color.DARKRED);

        table = new TableView<>();
        table.setItems(requestList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<BloodRequest, String> nameCol = new TableColumn<>("Receiver Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("receiverName"));

        TableColumn<BloodRequest, String> typeCol = new TableColumn<>("Blood Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<BloodRequest, Integer> qtyCol = new TableColumn<>("Units");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityUnits"));

        TableColumn<BloodRequest, String> dateCol = new TableColumn<>("Request Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));

        TableColumn<BloodRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(nameCol, typeCol, qtyCol, dateCol, statusCol);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by name or blood type");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));

        // Form fields
        TextField nameField = new TextField();
        ComboBox<String> bloodBox = new ComboBox<>(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        Spinner<Integer> unitsSpinner = new Spinner<>(1, 100, 1);
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("Pending", "Approved", "Rejected"));

        // Form layout
        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Receiver Name:"), 0, 0); form.add(nameField, 1, 0);
        form.add(new Label("Blood Type:"), 0, 1); form.add(bloodBox, 1, 1);
        form.add(new Label("Units:"), 0, 2); form.add(unitsSpinner, 1, 2);
        form.add(new Label("Date:"), 0, 3); form.add(datePicker, 1, 3);
        form.add(new Label("Status:"), 0, 4); form.add(statusBox, 1, 4);

        Button addBtn = new Button("➕ Add Request");
        Button updateBtn = new Button("✏️ Update");
        Button deleteBtn = new Button("🗑 Delete");

        HBox btnBox = new HBox(10, addBtn, updateBtn, deleteBtn);
        btnBox.setAlignment(Pos.CENTER);

        Label message = new Label();
        message.setTextFill(Color.FIREBRICK);

        VBox layout = new VBox(15, title, searchField, table, form, btnBox, message);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #fff0f0;");

        addBtn.setOnAction(e -> {
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO blood_requests (receiver_name, blood_type, quantity_units, request_date, status) VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, nameField.getText());
                ps.setString(2, bloodBox.getValue());
                ps.setInt(3, unitsSpinner.getValue());
                ps.setDate(4, java.sql.Date.valueOf(datePicker.getValue()));
                ps.setString(5, statusBox.getValue());
                ps.executeUpdate();
                loadRequests();
                clearForm(nameField, bloodBox, unitsSpinner, datePicker, statusBox);
                message.setText("Request added successfully.");
            } catch (Exception ex) {
                ex.printStackTrace();
                message.setText("Error adding request.");
            }
        });

        updateBtn.setOnAction(e -> {
            BloodRequest selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE blood_requests SET receiver_name=?, blood_type=?, quantity_units=?, request_date=?, status=? WHERE request_id=?");
                ps.setString(1, nameField.getText());
                ps.setString(2, bloodBox.getValue());
                ps.setInt(3, unitsSpinner.getValue());
                ps.setDate(4, java.sql.Date.valueOf(datePicker.getValue()));
                ps.setString(5, statusBox.getValue());
                ps.setInt(6, selected.getRequestId());
                ps.executeUpdate();
                loadRequests();
                clearForm(nameField, bloodBox, unitsSpinner, datePicker, statusBox);
                message.setText("Request updated.");
            } catch (Exception ex) {
                ex.printStackTrace();
                message.setText("Error updating request.");
            }
        });

        deleteBtn.setOnAction(e -> {
            BloodRequest selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM blood_requests WHERE request_id = ?");
                ps.setInt(1, selected.getRequestId());
                ps.executeUpdate();
                loadRequests();
                message.setText("Request deleted.");
            } catch (Exception ex) {
                ex.printStackTrace();
                message.setText("Error deleting request.");
            }
        });

        table.setOnMouseClicked(e -> {
            BloodRequest selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                nameField.setText(selected.getReceiverName());
                bloodBox.setValue(selected.getBloodType());
                unitsSpinner.getValueFactory().setValue(selected.getQuantityUnits());
                datePicker.setValue(LocalDate.parse(selected.getRequestDate()));
                statusBox.setValue(selected.getStatus());
            }
        });

        loadRequests();
        stage.setScene(new Scene(layout, 850, 650));
        stage.show();
    }

    private void loadRequests() {
        requestList.clear();
        try (Connection con = Con.getConnection();
             ResultSet rs = con.createStatement().executeQuery("SELECT * FROM blood_requests")) {
            while (rs.next()) {
                requestList.add(new BloodRequest(
                        rs.getInt("request_id"),
                        rs.getString("receiver_name"),
                        rs.getString("blood_type"),
                        rs.getInt("quantity_units"),
                        rs.getDate("request_date").toString(),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterTable(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            table.setItems(requestList);
            return;
        }
        ObservableList<BloodRequest> filtered = FXCollections.observableArrayList();
        for (BloodRequest br : requestList) {
            if (br.getReceiverName().toLowerCase().contains(keyword.toLowerCase()) ||
                    br.getBloodType().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(br);
            }
        }
        table.setItems(filtered);
    }

    private void clearForm(TextField nameField, ComboBox<String> bloodBox, Spinner<Integer> units, DatePicker date, ComboBox<String> status) {
        nameField.clear();
        bloodBox.setValue(null);
        units.getValueFactory().setValue(1);
        date.setValue(LocalDate.now());
        status.setValue(null);
    }

    public static class BloodRequest {
        private final int requestId;
        private final String receiverName, bloodType, requestDate, status;
        private final int quantityUnits;

        public BloodRequest(int requestId, String receiverName, String bloodType, int quantityUnits, String requestDate, String status) {
            this.requestId = requestId;
            this.receiverName = receiverName;
            this.bloodType = bloodType;
            this.quantityUnits = quantityUnits;
            this.requestDate = requestDate;
            this.status = status;
        }

        public int getRequestId() { return requestId; }
        public String getReceiverName() { return receiverName; }
        public String getBloodType() { return bloodType; }
        public int getQuantityUnits() { return quantityUnits; }
        public String getRequestDate() { return requestDate; }
        public String getStatus() { return status; }
    }

}