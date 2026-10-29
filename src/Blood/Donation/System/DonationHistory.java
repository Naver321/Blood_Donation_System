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

public class DonationHistory {

    private final ObservableList<DonationRecord> donationList = FXCollections.observableArrayList();
    private final Label statusLabel = new Label("Ready");

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Donation History");

        Label header = new Label("💉 Blood Donation History");
        header.setFont(new Font("Segoe UI Semibold", 26));
        header.setTextFill(Color.DEEPPINK);

        TableView<DonationRecord> table = new TableView<>();
        table.setItems(donationList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        TableColumn<DonationRecord, Integer> idCol = new TableColumn<>("Donor ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("donorId"));

        TableColumn<DonationRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<DonationRecord, Integer> volumeCol = new TableColumn<>("Volume (ml)");
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));

        table.getColumns().addAll(idCol, dateCol, volumeCol);

        // Filter section
        TextField searchIdField = new TextField();
        searchIdField.setPromptText("Search by Donor ID");

        DatePicker searchDatePicker = new DatePicker();
        searchDatePicker.setPromptText("Search by Date");

        Button filterBtn = new Button("🔍 Filter");
        filterBtn.setStyle("-fx-background-color: #ec407a; -fx-text-fill: white; -fx-font-weight: bold;");
        filterBtn.setOnAction(e -> filterRecords(searchIdField.getText(), searchDatePicker.getValue(), table));

        HBox filterBox = new HBox(10, searchIdField, searchDatePicker, filterBtn);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(10));

        // Form fields
        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(15);
        form.setVgap(10);
        form.setStyle("-fx-background-color: #fce4ec; -fx-border-color: #f8bbd0; -fx-border-width: 1; -fx-background-radius: 10;");

        TextField donorIdField = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField volumeField = new TextField();

        form.add(new Label("Donor ID:"), 0, 0); form.add(donorIdField, 1, 0);
        form.add(new Label("Date:"), 0, 1); form.add(datePicker, 1, 1);
        form.add(new Label("Volume (ml):"), 0, 2); form.add(volumeField, 1, 2);

        // Action Buttons
        Button addBtn = new Button("➕ Add");
        Button updateBtn = new Button("✏️ Update");
        Button deleteBtn = new Button("🗑 Delete");

        for (Button btn : new Button[]{addBtn, updateBtn, deleteBtn}) {
            btn.setStyle("-fx-background-color: #f06292; -fx-text-fill: white; -fx-background-radius: 5;");
        }

        HBox btnBox = new HBox(10, addBtn, updateBtn, deleteBtn);
        btnBox.setAlignment(Pos.CENTER);
        form.add(btnBox, 1, 3);

        table.setOnMouseClicked(e -> {
            DonationRecord selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                donorIdField.setText(String.valueOf(selected.getDonorId()));
                datePicker.setValue(LocalDate.parse(selected.getDate()));
                volumeField.setText(String.valueOf(selected.getVolume()));
            }
        });

        addBtn.setOnAction(e -> {
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO donations (donor_id, date, volume_ml) VALUES (?, ?, ?)");
                ps.setInt(1, Integer.parseInt(donorIdField.getText()));
                ps.setDate(2, java.sql.Date.valueOf(datePicker.getValue()));
                ps.setInt(3, Integer.parseInt(volumeField.getText()));
                ps.executeUpdate();
                statusLabel.setText("Donation added.");
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error adding donation.");
            }
        });

        updateBtn.setOnAction(e -> {
            DonationRecord selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE donations SET donor_id=?, date=?, volume_ml=? WHERE donation_id=?");
                ps.setInt(1, Integer.parseInt(donorIdField.getText()));
                ps.setDate(2, java.sql.Date.valueOf(datePicker.getValue()));
                ps.setInt(3, Integer.parseInt(volumeField.getText()));
                ps.setInt(4, selected.getDonationId());
                ps.executeUpdate();
                statusLabel.setText("Donation updated.");
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error updating donation.");
            }
        });

        deleteBtn.setOnAction(e -> {
            DonationRecord selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM donations WHERE donation_id=?");
                ps.setInt(1, selected.getDonationId());
                ps.executeUpdate();
                statusLabel.setText("Donation deleted.");
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error deleting donation.");
            }
        });

        VBox layout = new VBox(15, header, filterBox, table, form, statusLabel);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #fff0f5;");

        Scene scene = new Scene(layout, 750, 600);
        stage.setScene(scene);
        stage.show();

        refreshTable();
    }

    private void refreshTable() {
        donationList.clear();
        try (Connection con = Con.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM donations");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                donationList.add(new DonationRecord(
                        rs.getInt("donation_id"),
                        rs.getInt("donor_id"),
                        rs.getDate("date").toString(),
                        rs.getInt("volume_ml")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterRecords(String donorId, LocalDate date, TableView<DonationRecord> table) {
        ObservableList<DonationRecord> filtered = FXCollections.observableArrayList();
        for (DonationRecord record : donationList) {
            boolean match = true;
            if (donorId != null && !donorId.isEmpty()) {
                match = String.valueOf(record.getDonorId()).contains(donorId);
            }
            if (match && date != null) {
                match = record.getDate().equals(date.toString());
            }
            if (match) filtered.add(record);
        }
        table.setItems(filtered);
    }

    public static class DonationRecord {
        private final int donationId;
        private final int donorId;
        private final String date;
        private final int volume;

        public DonationRecord(int donationId, int donorId, String date, int volume) {
            this.donationId = donationId;
            this.donorId = donorId;
            this.date = date;
            this.volume = volume;
        }

        public int getDonationId() { return donationId; }
        public int getDonorId() { return donorId; }
        public String getDate() { return date; }
        public int getVolume() { return volume; }
    }
}
