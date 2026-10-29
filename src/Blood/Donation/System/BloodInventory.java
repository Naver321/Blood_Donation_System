package Blood.Donation.System;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class BloodInventory {

    private final ObservableList<BloodUnit> inventoryList = FXCollections.observableArrayList();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Blood Inventory Management");

        // Header Label
        Label header = new Label("🩸 Blood Inventory Management");
        header.setFont(Font.font("Segoe UI Semibold", 22));
        header.setStyle("-fx-text-fill: #b71c1c;");

        TableView<BloodUnit> table = new TableView<>();
        table.setStyle("-fx-font-size: 14px;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<BloodUnit, String> groupCol = new TableColumn<>("Blood Type");
        groupCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<BloodUnit, Integer> quantityCol = new TableColumn<>("Units Available");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantityUnits"));

        TableColumn<BloodUnit, String> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        table.getColumns().addAll(groupCol, quantityCol, expiryCol);
        table.setItems(inventoryList);
        refreshTable();

        Button addBtn = new Button("➕ Add");
        addBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;");
        addBtn.setOnAction(e -> showAddDialog());

        Button updateBtn = new Button("✏️ Update");
        updateBtn.setStyle("-fx-background-color: #fb8c00; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> {
            BloodUnit selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showUpdateDialog(selected);
        });

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            BloodUnit selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try (Connection con = Con.getConnection()) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM blood_inventory WHERE inventory_id = ?");
                    ps.setInt(1, selected.getId());
                    ps.executeUpdate();
                    refreshTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        HBox buttonBox = new HBox(10, addBtn, updateBtn, deleteBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, header, table, buttonBox);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #fdecea;");

        stage.setScene(new Scene(root, 620, 500));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void showAddDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add Blood Units");

        ComboBox<String> groupBox = new ComboBox<>(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        TextField qtyField = new TextField();
        qtyField.setPromptText("Units");
        DatePicker expiryPicker = new DatePicker(LocalDate.now().plusMonths(1));
        Label msg = new Label();

        Button saveBtn = new Button("💾 Save");
        saveBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> {
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO blood_inventory (blood_type, quantity_units, expiry_date) VALUES (?, ?, ?)");
                ps.setString(1, groupBox.getValue());
                ps.setInt(2, Integer.parseInt(qtyField.getText()));
                ps.setDate(3, java.sql.Date.valueOf(expiryPicker.getValue()));
                ps.executeUpdate();
                dialog.close();
                refreshTable();
            } catch (Exception ex) {
                msg.setText("Error while saving");
                ex.printStackTrace();
            }
        });

        VBox box = new VBox(10,
                new Label("Blood Group:"), groupBox,
                new Label("Units:"), qtyField,
                new Label("Expiry Date:"), expiryPicker,
                saveBtn, msg);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER_LEFT);
        dialog.setScene(new Scene(box, 320, 300));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.show();
    }

    private void showUpdateDialog(BloodUnit unit) {
        Stage dialog = new Stage();
        dialog.setTitle("Update Blood Units");

        TextField qtyField = new TextField(String.valueOf(unit.getQuantityUnits()));
        DatePicker expiryPicker = new DatePicker(LocalDate.parse(unit.getExpiryDate()));
        Label msg = new Label();

        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #fb8c00; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> {
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE blood_inventory SET quantity_units = ?, expiry_date = ? WHERE inventory_id = ?");
                ps.setInt(1, Integer.parseInt(qtyField.getText()));
                ps.setDate(2, java.sql.Date.valueOf(expiryPicker.getValue()));
                ps.setInt(3, unit.getId());
                ps.executeUpdate();
                dialog.close();
                refreshTable();
            } catch (Exception ex) {
                msg.setText("Error while updating");
                ex.printStackTrace();
            }
        });

        VBox box = new VBox(10,
                new Label("Units:"), qtyField,
                new Label("Expiry Date:"), expiryPicker,
                updateBtn, msg);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER_LEFT);
        dialog.setScene(new Scene(box, 300, 260));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.show();
    }

    private void refreshTable() {
        inventoryList.clear();
        try (Connection con = Con.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM blood_inventory");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                inventoryList.add(new BloodUnit(
                        rs.getInt("inventory_id"),
                        rs.getString("blood_type"),
                        rs.getInt("quantity_units"),
                        rs.getDate("expiry_date").toString()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class BloodUnit {
        private final int id;
        private final String bloodType;
        private final int quantityUnits;
        private final String expiryDate;

        public BloodUnit(int id, String bloodType, int quantityUnits, String expiryDate) {
            this.id = id;
            this.bloodType = bloodType;
            this.quantityUnits = quantityUnits;
            this.expiryDate = expiryDate;
        }

        public int getId() { return id; }
        public String getBloodType() { return bloodType; }
        public int getQuantityUnits() { return quantityUnits; }
        public String getExpiryDate() { return expiryDate; }
    }
}
