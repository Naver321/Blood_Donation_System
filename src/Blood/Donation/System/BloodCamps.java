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
import java.time.LocalDate;

public class BloodCamps {

    private final ObservableList<Camp> campList = FXCollections.observableArrayList();
    private final Label statusLabel = new Label("Ready");

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Blood Donation Camps");

        Label header = new Label("📍 Blood Donation Camps");
        header.setFont(new Font("Segoe UI Semibold", 24));
        header.setTextFill(Color.HOTPINK);

        TableView<Camp> table = new TableView<>();
        table.setItems(campList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        TableColumn<Camp, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Camp, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Camp, String> organizerCol = new TableColumn<>("Organizer");
        organizerCol.setCellValueFactory(new PropertyValueFactory<>("organizer"));

        TableColumn<Camp, String> remarksCol = new TableColumn<>("Remarks");
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        table.getColumns().addAll(locationCol, dateCol, organizerCol, remarksCol);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by Location or Organizer");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal, table));

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(15);
        form.setVgap(10);
        form.setStyle("-fx-background-color: #fff0f5; -fx-border-color: #e91e63; -fx-border-width: 1;");

        TextField locationField = new TextField();
        DatePicker dateField = new DatePicker(LocalDate.now());
        TextField organizerField = new TextField();
        TextArea remarksField = new TextArea();
        remarksField.setPrefRowCount(2);

        form.add(new Label("Location:"), 0, 0); form.add(locationField, 1, 0);
        form.add(new Label("Date:"), 0, 1); form.add(dateField, 1, 1);
        form.add(new Label("Organizer:"), 0, 2); form.add(organizerField, 1, 2);
        form.add(new Label("Remarks:"), 0, 3); form.add(remarksField, 1, 3);

        Button addBtn = new Button("➕ Add Camp");
        addBtn.setStyle("-fx-background-color: #e91e63; -fx-text-fill: white; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> {
            String location = locationField.getText();
            LocalDate date = dateField.getValue();
            String organizer = organizerField.getText();
            String remarks = remarksField.getText();

            if (location.isEmpty() || date == null || organizer.isEmpty()) {
                statusLabel.setText("All fields except remarks are required.");
                return;
            }

            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO blood_camps (location, date, organizer, remarks) VALUES (?, ?, ?, ?)");
                ps.setString(1, location);
                ps.setDate(2, java.sql.Date.valueOf(date));
                ps.setString(3, organizer);
                ps.setString(4, remarks);
                ps.executeUpdate();

                campList.add(new Camp(location, date.toString(), organizer, remarks));

                locationField.clear();
                organizerField.clear();
                dateField.setValue(LocalDate.now());
                remarksField.clear();
                statusLabel.setText("Camp added successfully.");
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Database error.");
            }
        });

        Button deleteBtn = new Button("🗑 Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #c2185b; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            Camp selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this camp?", ButtonType.OK, ButtonType.CANCEL);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM blood_camps WHERE location=? AND date=?");
                ps.setString(1, selected.getLocation());
                ps.setDate(2, java.sql.Date.valueOf(selected.getDate()));
                ps.executeUpdate();

                campList.remove(selected);
                statusLabel.setText("Camp deleted.");
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error deleting record.");
            }
        });

        HBox buttonBox = new HBox(10, addBtn, deleteBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        form.add(buttonBox, 1, 4);

        VBox layout = new VBox(10, header, searchField, table, form, statusLabel);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #fff8f9;");

        loadFromDatabase();

        stage.setScene(new Scene(layout, 850, 600));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void loadFromDatabase() {
        campList.clear();
        try (Connection con = Con.getConnection();
             ResultSet rs = con.createStatement().executeQuery("SELECT * FROM blood_camps")) {
            while (rs.next()) {
                campList.add(new Camp(
                        rs.getString("location"),
                        rs.getDate("date").toString(),
                        rs.getString("organizer"),
                        rs.getString("remarks")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterData(String keyword, TableView<Camp> table) {
        if (keyword == null || keyword.isEmpty()) {
            table.setItems(campList);
            return;
        }
        ObservableList<Camp> filtered = FXCollections.observableArrayList();
        for (Camp c : campList) {
            if (c.getLocation().toLowerCase().contains(keyword.toLowerCase()) ||
                    c.getOrganizer().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(c);
            }
        }
        table.setItems(filtered);
    }

    public static class Camp {
        private final String location, date, organizer, remarks;

        public Camp(String location, String date, String organizer, String remarks) {
            this.location = location;
            this.date = date;
            this.organizer = organizer;
            this.remarks = remarks;
        }

        public String getLocation() { return location; }
        public String getDate() { return date; }
        public String getOrganizer() { return organizer; }
        public String getRemarks() { return remarks; }
    }
}
