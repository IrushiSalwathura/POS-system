package controller;

import Db.DbConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.ItemTM;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;

public class ItemFormController {
    public AnchorPane root;
    public Button btnNavigate;
    public Button btnAddNewItem;
    public TextField txtItemCode;
    public TextField txtDescription;
    public TextField txtQuantityOnHand;
    public TextField txtUnitPrice;
    public Button btnSave;
    public Button btnDelete;
    public TableView <ItemTM>tblItems;

    public void initialize(URL location, ResourceBundle resources){
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("item_code"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("quantity_on_hand"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unit_price"));

        enableFields();
        loadAllItems();

        tblItems.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemTM>() {
            @Override
            public void changed(ObservableValue<? extends ItemTM> observableValue, ItemTM itemTM, ItemTM t1) {
                ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();

                if(selectedItem == null ){
                    btnSave.setText("Save");
                    btnDelete.setDisable(true);
                    clearFields();
                    return;
                }
                btnSave.setText("Update");
                disableFields();
                txtItemCode.setText(selectedItem.getItem_code());
                txtDescription.setText(selectedItem.getDescription());
                txtQuantityOnHand.setText(String.valueOf(selectedItem.getQuantity_on_hand()));
                txtUnitPrice.setText(String.valueOf(selectedItem.getUnit_price()));
            }
        });
    }



    public void btnNavigate_OnAction(ActionEvent actionEvent) throws IOException {
        URL resource = this.getClass().getResource("/view/MainForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage)(this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void btnAddNewItem_OnAction(ActionEvent actionEvent) {
        clearFields();
        tblItems.getSelectionModel().clearSelection();
        txtDescription.setDisable(false);
        txtQuantityOnHand.setDisable(false);
        txtUnitPrice.setDisable(false);
        txtDescription.requestFocus();
        btnSave.setDisable(false);

        //TODO: GENERATE NEW ID
        int maxCode = 0;
        for(ItemTM item : tblItems.getItems()){
            int code = Integer.parseInt(item.getItem_code().replace("I",""));
            if(code > maxCode){
                maxCode = code;
            }
        }
        maxCode = maxCode + 1;
        String code = "";
        if(maxCode < 10){
            code = "I00" + maxCode;
        }else if(maxCode < 100){
            code = "I0" + maxCode;
        }else{
            code = "I" + maxCode;
        }
        txtItemCode.setText(code);


    }

    public void btnSave_OnAction(ActionEvent event) {
        if(txtDescription.getText().trim().isEmpty() ||
                txtQuantityOnHand.getText().trim().isEmpty() ||
                txtUnitPrice.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Description, Qty. on Hand or Unit Price can't be empty").show();
            return;
        }

        int quantityOnHand = Integer.parseInt(txtQuantityOnHand.getText().trim());
        double unitPrice = Double.parseDouble(txtUnitPrice.getText().trim());

        if(quantityOnHand < 0 || unitPrice <= 0){
            new Alert(Alert.AlertType.ERROR,"Invalid qty or unit price").show();
            return;

        }

        if(btnSave.getText().equals("Save")){
            try {
                PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                        ("INSERT INTO Item(ItemCode,Description,QtyOnHand,UnitPrice) VALUES (?,?,?,?)");
                pstm.setObject(1,txtItemCode.getText());
                pstm.setObject(2,txtDescription.getText());
                pstm.setObject(3,quantityOnHand);
                pstm.setObject(4,unitPrice);
                if(pstm.executeUpdate()==0){
                    new Alert(Alert.AlertType.ERROR,"Failed to save the item", ButtonType.OK).show();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            btnAddNewItem_OnAction(event);
        }else{
            ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();

            try {
                PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                        ("UPDATE Item SET Description=?, QtyOnHand=?, UnitPrice=? WHERE ItemCode=?");
                pstm.setObject(1,txtDescription.getText());
                pstm.setObject(2,quantityOnHand);
                pstm.setObject(3,unitPrice);
                pstm.setObject(4,selectedItem.getItem_code());
                if(pstm.executeUpdate()==0){
                    new Alert(Alert.AlertType.ERROR,"Failed to update the item").show();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            tblItems.refresh();
           btnAddNewItem_OnAction(event);
        }
        loadAllItems();
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure whether you want to delete this item?",ButtonType.YES,ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();
        if(buttonType.get() == ButtonType.YES){
            ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();
            try {
                PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                        ("DELETE FROM Item WHERE ItemCode=?");
                pstm.setObject(1,selectedItem.getItem_code());
                if(pstm.executeUpdate() == 0){
                    new Alert(Alert.AlertType.ERROR,"Failed to delete the item",ButtonType.OK).show();
                }else{
                    tblItems.getItems().remove(selectedItem);
                    tblItems.getSelectionModel().clearSelection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadAllItems() {
        try {
            Statement stm = DbConnection.getInstance().getConnection().createStatement();
            ResultSet rst = stm.executeQuery("SELECT ItemCode,Description,QtyOnHand,UnitPrice FROM Item");
            ObservableList<ItemTM> Items = tblItems.getItems();
            Items.clear();
            while (rst.next()){
                String code = rst.getString(1);
                String description = rst.getString(2);
                int quantityOnHand = rst.getInt(3);
                double unitPrice = Double.parseDouble(rst.getString(4));
                Items.add(new ItemTM(code,description,quantityOnHand,unitPrice));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void disableFields() {
        btnSave.setDisable(false);
        btnDelete.setDisable(false);
        txtDescription.setDisable(false);
        txtQuantityOnHand.setDisable(false);
        txtUnitPrice.setDisable(false);
    }

    private void enableFields(){
        txtItemCode.setDisable(true);
        txtDescription.setDisable(true);
        txtQuantityOnHand.setDisable(true);
        txtUnitPrice.setDisable(true);
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void clearFields(){
        txtItemCode.clear();
        txtDescription.clear();
        txtQuantityOnHand.clear();
        txtUnitPrice.clear();
    }
}
