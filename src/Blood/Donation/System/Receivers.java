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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Receivers {

    private final ObservableList<Receiver> receiverList = FXCollections.observableArrayList();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Receivers");

        Label header = new Label("🏥 Blood Receivers Management");
        header.setFont(new Font("Segoe UI Semibold", 26));
        header.setTextFill(Color.DARKMAGENTA);

        TableView<Receiver> table = new TableView<>();
        table.setItems(receiverList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        TableColumn<Receiver, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Receiver, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));

        TableColumn<Receiver, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        table.getColumns().addAll(nameCol, contactCol, typeCol);

        // Status label
        Label statusLabel = new Label("Ready");
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-background-color: #fce4ec; -fx-border-color: #f8bbd0; -fx-font-size: 12px;");

        // Filter/search
        TextField searchField = new TextField();
        searchField.setPromptText("Search by Name...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterReceivers(newVal, table));

        HBox searchBox = new HBox(10, new Label("🔍"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Form
        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(15);
        form.setVgap(10);
        form.setStyle("-fx-background-color: #fce4ec; -fx-border-color: #f8bbd0; -fx-border-width: 1;");

        TextField nameField = new TextField();
        TextField contactField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("Hospital", "Individual"));
        typeBox.setPromptText("Select Type");

        form.add(new Label("Name:"), 0, 0); form.add(nameField, 1, 0);
        form.add(new Label("Contact:"), 0, 1); form.add(contactField, 1, 1);
        form.add(new Label("Type:"), 0, 2); form.add(typeBox, 1, 2);

        Button addBtn = new Button("➕ Add");
        Button updateBtn = new Button("✏️ Update");
        Button deleteBtn = new Button("🗑 Delete");

        HBox btnBox = new HBox(10, addBtn, updateBtn, deleteBtn);
        btnBox.setAlignment(Pos.CENTER);
        form.add(btnBox, 1, 3);

        // Click to select
        table.setOnMouseClicked(e -> {
            Receiver selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                nameField.setText(selected.getName());
                contactField.setText(selected.getContact());
                typeBox.setValue(selected.getType());
            }
        });

        // Add
        addBtn.setOnAction(e -> {
            String name = nameField.getText();
            String contact = contactField.getText();
            String type = typeBox.getValue();

            if (name.isEmpty() || contact.isEmpty() || type == null) {
                statusLabel.setText("Fill all fields.");
                return;
            }

            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO receivers (name, contact, type) VALUES (?, ?, ?)");
                ps.setString(1, name);
                ps.setString(2, contact);
                ps.setString(3, type);
                ps.executeUpdate();
                statusLabel.setText("Receiver added.");

                nameField.clear();
                contactField.clear();
                typeBox.setValue(null);
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error adding receiver.");
            }
        });

        // Update
        updateBtn.setOnAction(e -> {
            Receiver selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE receivers SET name=?, contact=?, type=? WHERE receiver_id=?");
                ps.setString(1, nameField.getText());
                ps.setString(2, contactField.getText());
                ps.setString(3, typeBox.getValue());
                ps.setInt(4, selected.getReceiverId());
                ps.executeUpdate();
                statusLabel.setText("Receiver updated.");
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error updating receiver.");
            }
        });

        // Delete
        deleteBtn.setOnAction(e -> {
            Receiver selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM receivers WHERE receiver_id=?");
                ps.setInt(1, selected.getReceiverId());
                ps.executeUpdate();
                statusLabel.setText("Receiver deleted.");
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error deleting receiver.");
            }
        });

        VBox layout = new VBox(15, header, searchBox, table, form, statusLabel);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #ffffff;");
        Scene scene = new Scene(layout, 750, 550);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

        refreshTable();
    }

    private void refreshTable() {
        receiverList.clear();
        try (Connection con = Con.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM receivers");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                receiverList.add(new Receiver(
                        rs.getInt("receiver_id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("type")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterReceivers(String keyword, TableView<Receiver> table) {
        if (keyword == null || keyword.isEmpty()) {
            table.setItems(receiverList);
            return;
        }
        ObservableList<Receiver> filtered = FXCollections.observableArrayList();
        for (Receiver r : receiverList) {
            if (r.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(r);
            }
        }
        table.setItems(filtered);
    }

    public static class Receiver {
        private final int receiverId;
        private final String name, contact, type;

        public Receiver(int receiverId, String name, String contact, String type) {
            this.receiverId = receiverId;
            this.name = name;
            this.contact = contact;
            this.type = type;
        }

        public int getReceiverId() { return receiverId; }
        public String getName() { return name; }
        public String getContact() { return contact; }
        public String getType() { return type; }
    }
}
