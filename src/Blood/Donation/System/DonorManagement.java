package Blood.Donation.System;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class  DonorManagement {

    private final ObservableList<Donor> donorList = FXCollections.observableArrayList();
    private TableView<Donor> table;

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Donor Management");

        table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Donor, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Donor, String> bloodCol = new TableColumn<>("Blood Type");
        bloodCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<Donor, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));

        TableColumn<Donor, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<Donor, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));

        TableColumn<Donor, String> descriptionCol = new TableColumn<>("Health Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(nameCol, bloodCol, ageCol, genderCol, contactCol, descriptionCol);
        table.setItems(donorList);
        refreshTable();

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by name or blood type");
        searchField.setMaxWidth(250);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));

        Button addBtn = new Button("➕ Add");
        Button editBtn = new Button("✏ Edit");
        Button deleteBtn = new Button("🗑 Delete");

        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        editBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

        addBtn.setOnAction(e -> showDonorDialog(null));
        editBtn.setOnAction(e -> {
            Donor selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showDonorDialog(selected);
        });
        deleteBtn.setOnAction(e -> deleteSelectedDonor());

        HBox buttons = new HBox(15, addBtn, editBtn, deleteBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, searchField, table, buttons);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #fce4ec, #f3e5f5);");
        layout.setEffect(new DropShadow(10, Color.GRAY));

        Scene scene = new Scene(layout, 850, 550);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void refreshTable() {
        donorList.clear();
        try (Connection con = Con.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM donors");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                donorList.add(new Donor(
                        rs.getInt("donor_id"),
                        rs.getString("name"),
                        rs.getString("blood_type"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("contact"),
                        rs.getString("description")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSelectedDonor() {
        Donor selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM donors WHERE donor_id = ?");
                ps.setInt(1, selected.getId());
                ps.executeUpdate();
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showDonorDialog(Donor donor) {
        Stage dialog = new Stage();
        dialog.setTitle(donor == null ? "Add Donor" : "Edit Donor");

        TextField nameField = new TextField();
        ComboBox<String> bloodBox = new ComboBox<>(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        TextField ageField = new TextField();
        ComboBox<String> genderBox = new ComboBox<>(FXCollections.observableArrayList("Male", "Female", "Other"));
        TextField contactField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("E.g., No illness, Diabetic, Asthma patient...");

        nameField.setPromptText("Full name");
        contactField.setPromptText("Contact No.");
        bloodBox.setPromptText("Select blood group");
        genderBox.setPromptText("Select gender");
        ageField.setPromptText("Age");

        if (donor != null) {
            nameField.setText(donor.getName());
            bloodBox.setValue(donor.getBloodType());
            ageField.setText(String.valueOf(donor.getAge()));
            genderBox.setValue(donor.getGender());
            contactField.setText(donor.getContact());
            descriptionArea.setText(donor.getDescription());
        }

        Button saveBtn = new Button("💾 Save");
        saveBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> {
            try (Connection con = Con.getConnection()) {
                PreparedStatement ps;
                if (donor == null) {
                    ps = con.prepareStatement("INSERT INTO donors (name, blood_type, age, gender, contact, description) VALUES (?, ?, ?, ?, ?, ?)");
                } else {
                    ps = con.prepareStatement("UPDATE donors SET name=?, blood_type=?, age=?, gender=?, contact=?, description=? WHERE donor_id=?");
                    ps.setInt(7, donor.getId());
                }
                ps.setString(1, nameField.getText());
                ps.setString(2, bloodBox.getValue());
                ps.setInt(3, Integer.parseInt(ageField.getText()));
                ps.setString(4, genderBox.getValue());
                ps.setString(5, contactField.getText());
                ps.setString(6, descriptionArea.getText());
                ps.executeUpdate();
                refreshTable();
                dialog.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Blood Type:"), 0, 1);
        grid.add(bloodBox, 1, 1);
        grid.add(new Label("Age:"), 0, 2);
        grid.add(ageField, 1, 2);
        grid.add(new Label("Gender:"), 0, 3);
        grid.add(genderBox, 1, 3);
        grid.add(new Label("Contact:"), 0, 4);
        grid.add(contactField, 1, 4);
        grid.add(new Label("Health Description:"), 0, 5);
        grid.add(descriptionArea, 1, 5);
        grid.add(saveBtn, 1, 6);

        Scene scene = new Scene(grid, 420, 450);
        dialog.setScene(scene);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.show();
    }

    private void filterData(String keyword) {
        ObservableList<Donor> filtered = FXCollections.observableArrayList();
        keyword = keyword.toLowerCase();
        for (Donor d : donorList) {
            if (d.getName().toLowerCase().contains(keyword) || d.getBloodType().toLowerCase().contains(keyword)) {
                filtered.add(d);
            }
        }
        table.setItems(filtered);
    }

    public static class Donor {
        private final int id;
        private final String name;
        private final String bloodType;
        private final int age;
        private final String gender;
        private final String contact;
        private final String description;

        public Donor(int id, String name, String bloodType, int age, String gender, String contact, String description) {
            this.id = id;
            this.name = name;
            this.bloodType = bloodType;
            this.age = age;
            this.gender = gender;
            this.contact = contact;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getBloodType() {
            return bloodType;
        }

        public int getAge() {
            return age;
        }

        public String getGender() {
            return gender;
        }

        public String getContact() {
            return contact;
        }

        public String getDescription() {
            return description;
        }
    }
}