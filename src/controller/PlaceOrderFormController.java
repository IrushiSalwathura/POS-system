package controller;

import Db.DbConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.CustomerTM;
import util.ItemTM;
import util.OrderDetailTM;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlaceOrderFormController {
    public AnchorPane root;
    public Label lblDate;
    public Label lblOrdeID;
    public JFXButton btnBack;
    public JFXButton btnPlaceOrder;
    public JFXButton btnDelete;
    public JFXButton btnSave;
    public TableView<OrderDetailTM> tblOrders;
    public JFXTextField txtCustomerName;
    public JFXTextField txtDescription;
    public JFXTextField txtOtyOnHand;
    public JFXTextField txtUnitPrice;
    public JFXTextField txtQuantity;
    public JFXTextField txtNetTotal;
    public JFXComboBox<CustomerTM> cmbCustomerID;
    public JFXComboBox<ItemTM> cmbItemCode;
    private boolean readOnly = false;

    public void initialize(){
        FadeTransition ft = new FadeTransition(Duration.millis(1000));
        ft.setNode(root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        lblDate.setText(dateFormat.format(date));
        generateOrderID();

        tblOrders.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        tblOrders.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrders.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrders.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrders.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));

        tblOrders.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<OrderDetailTM>() {
            @Override
            public void changed(ObservableValue<? extends OrderDetailTM> observable, OrderDetailTM oldValue, OrderDetailTM selectedOrderDetail) {
                OrderDetailTM selectedItem = tblOrders.getSelectionModel().getSelectedItem();

                if (selectedOrderDetail == null) {
                    return;
                }
                String selectedItemCode = selectedOrderDetail.getItemCode();
                ObservableList<ItemTM> items = cmbItemCode.getItems();
                for (ItemTM item : items) {
                    if (item.getItem_code().equals(selectedItemCode)) {
                        cmbItemCode.getSelectionModel().select(item);
                        txtOtyOnHand.setText(item.getQuantity_on_hand() + "");
                        txtQuantity.setText(selectedOrderDetail.getQty() + "");
                        if (!readOnly){
                            btnSave.setText("Update");
                        }
                        if (readOnly){
                            txtQuantity.setDisable(true);
                            btnSave.setDisable(true);
                        }
                        cmbItemCode.setDisable(true);
                        Platform.runLater(() -> {
                            txtQuantity.requestFocus();
                        });
                        break;
                    }
                }
            }
        });
        loadCustomerIDs();
        cmbCustomerID.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CustomerTM>() {
            @Override
            public void changed(ObservableValue<? extends CustomerTM> observable, CustomerTM oldValue, CustomerTM newValue) {
                if(newValue == null){
                    txtCustomerName.clear();
                    return;
                }
                txtCustomerName.setText(newValue.getName());
            }
        });
        loadItemCodes();
        cmbItemCode.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemTM>() {
            @Override
            public void changed(ObservableValue<? extends ItemTM> observable, ItemTM oldValue, ItemTM newValue) {
                if(newValue == null){
                    txtDescription.clear();
                    txtUnitPrice.clear();
                    txtOtyOnHand.clear();
                    btnSave.setDisable(true);
                }
                String qtyOnHand = String.valueOf(newValue.getQuantity_on_hand());
                String unitPrice = String.valueOf(newValue.getUnit_price());
                txtDescription.setText(newValue.getDescription());
                txtOtyOnHand.setText(qtyOnHand);
                txtUnitPrice.setText(unitPrice);
            }
        });
    }
    public void btnBack_OnAction(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(this.getClass().getResource("/view/MainForm.fxml"));
            Scene mainScene = new Scene(root);
            Stage mainStage = (Stage)this.root.getScene().getWindow();
            mainStage.setScene(mainScene);
            mainStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) {
        String orderId = lblOrdeID.getText();
        String orderDate = lblDate.getText();
        String customerId = cmbCustomerID.getSelectionModel().getSelectedItem().toString();
        String itemCode = cmbItemCode.getSelectionModel().getSelectedItem().toString();
        int qty = Integer.parseInt(txtQuantity.getText());
        double unitPrice = Double.parseDouble(txtUnitPrice.getText());
        double total = qty*unitPrice;
        int qtyOnHand = Integer.parseInt(txtOtyOnHand.getText());
        try {
            PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                    ("INSERT INTO Orders VALUES (?,?,?)");
            pstm.setObject(1,orderId);
            pstm.setObject(2,orderDate);
            pstm.setObject(3,customerId);
            int affected = pstm.executeUpdate();
            if(affected == 0){
                new Alert(Alert.AlertType.ERROR,"Failed to add the order",ButtonType.OK).show();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ObservableList<OrderDetailTM> olOrderDetails = tblOrders.getItems();
        try {
            PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                    ("INSERT INTO Orderdetail VALUES (?,?,?,?)");
            for(OrderDetailTM orderDetail : olOrderDetails){
                pstm.setObject(1,orderId);
                pstm.setObject(2,orderDetail.getItemCode());
                pstm.setObject(3,orderDetail.getQty());
                pstm.setObject(4,orderDetail.getUnitPrice());
                updateStockQty(orderDetail.getItemCode(), orderDetail.getQty());
            }
            int affected = pstm.executeUpdate();
            if(affected == 0){
                new Alert(Alert.AlertType.ERROR,"Couldn't insert to orderDetail table",ButtonType.OK).show();
            }else{
                new Alert(Alert.AlertType.INFORMATION,"Added Successfully",ButtonType.OK).show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        generateOrderID();
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {
        String itemCode = cmbItemCode.getSelectionModel().getSelectedItem().toString();
        String description = txtDescription.getText();
        double unitPrice = Double.parseDouble(txtUnitPrice.getText());
        int qty = Integer.parseInt(txtQuantity.getText());
        double total = unitPrice * qty;
        ObservableList<OrderDetailTM> orders = tblOrders.getItems();
        orders.remove(new OrderDetailTM(itemCode, description, qty, unitPrice, total));
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
        String orderID = lblOrdeID.getText();
        String orderDate = lblDate.getText();
        //String customerID = cmbCustomerID.getSelectionModel().getSelectedItem().toString();
        String itemCode = cmbItemCode.getSelectionModel().getSelectedItem().toString();
        String description = txtDescription.getText();
        double unitPrice = Double.parseDouble(txtUnitPrice.getText());
        int qty = Integer.parseInt(txtQuantity.getText());
        double total = unitPrice * qty;
        int qtyOnHand = Integer.parseInt(txtOtyOnHand.getText()) - qty;

        if(btnSave.getText().equals("Save")) {
            ObservableList<OrderDetailTM> orders = tblOrders.getItems();
            orders.add(new OrderDetailTM(itemCode, description, qty, unitPrice, total));
            txtOtyOnHand.setText(Integer.toString(qtyOnHand));
        }else if(btnSave.getText().equals("Update")){
            ObservableList<OrderDetailTM> orders = tblOrders.getItems();
            int selectedIndex = tblOrders.getSelectionModel().getSelectedIndex();
            orders.get(selectedIndex).setItemCode(cmbItemCode.getSelectionModel().getSelectedItem().toString());
            orders.get(selectedIndex).setDescription(txtDescription.getText());
            orders.get(selectedIndex).setQty(Integer.parseInt(txtQuantity.getText()));
            orders.get(selectedIndex).setUnitPrice(Double.parseDouble(txtUnitPrice.getText()));
            orders.get(selectedIndex).setTotal(Integer.parseInt(txtQuantity.getText())*Double.parseDouble(txtUnitPrice.getText()));
            tblOrders.getSelectionModel().clearSelection();
            tblOrders.refresh();
        }
        calculateTotal();
    }

    private void updateStockQty(String itemCode, int qty) {
        try {
            PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                    ("UPDATE Item SET QtyOnHand=QtyOnHand - ? where ItemCode =?");
            pstm.setObject(1, qty);
            pstm.setObject(2, itemCode);
            int affected = pstm.executeUpdate();
            if (affected == 0) {
                new Alert(Alert.AlertType.ERROR, "Could not update the stock", ButtonType.OK).show();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void generateOrderID(){
        int maxId = 0;
        try {
            PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                    ("SELECT * FROM Orders ORDER BY OrderID DESC LIMIT 1");
            ResultSet rst = pstm.executeQuery();
            while(rst.next()) {
                String orderID = rst.getString("OrderID");
                int id = Integer.parseInt(orderID.replace("D",""));
                if(maxId<id){
                    maxId=id;
                }
                maxId=maxId+1;
                String newId = "";
                if(maxId<10){
                    newId="D00"+maxId;
                }else if(maxId<100){
                    newId="D0"+maxId;
                }else{
                    newId="D"+maxId;
                }
                lblOrdeID.setText(newId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadOrderDetails(){
        try {
            PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                    ("SELECT od.ItemCode as ItemCode,i.Description as Description,od.OrderQTY as Qty,i.UnitPrice as UnitPrice, od.OrderQTY*i.UnitPrice as Total\n" +
                            "FROM orderdetail od,Item i\n" +
                            "WHERE od.ItemCode=i.ItemCode");
            ResultSet rst = pstm.executeQuery();
            ObservableList<OrderDetailTM> orders = tblOrders.getItems();
            orders.clear();
            while(rst.next()){
                String itemCode = rst.getString("ItemCode");
                String description = rst.getString("Description");
                int qty = rst.getInt("Qty");
                double unitPrice = rst.getDouble("UnitPrice");
                double total = rst.getDouble("Total");
                orders.add(new OrderDetailTM(itemCode,description,qty,unitPrice,total));
                calculateTotal();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void calculateTotal(){
        ObservableList<OrderDetailTM> orderDetails = tblOrders.getItems();
        double netTotal = 0;
        for (OrderDetailTM orderdetail : orderDetails) {
            netTotal+=orderdetail.getTotal();
        }
        NumberFormat numberInstance = NumberFormat.getNumberInstance();
        numberInstance.setMaximumFractionDigits(2);
        numberInstance.setMinimumFractionDigits(2);
        numberInstance.setGroupingUsed(false);
        String formattedText =numberInstance.format(netTotal);
        txtNetTotal.setText(formattedText);
    }
    @SuppressWarnings("Duplicates")
    public void loadCustomerIDs(){
        try {
            PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                    ("SELECT * FROM Customer");
            ResultSet rst = pstm.executeQuery();
            ObservableList<CustomerTM> customers = cmbCustomerID.getItems();
            customers.clear();
            while(rst.next()) {
                String id = rst.getString("CustomerId");
                String name = rst.getString("CustomerName");
                String address = rst.getString("CustomerAddress");
                customers.add(new CustomerTM(id,name,address));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadItemCodes(){
        try {
            PreparedStatement pstm = DbConnection.getInstance().getConnection().prepareStatement
                    ("SELECT * FROM Item");
            ResultSet rst = pstm.executeQuery();
            ObservableList<ItemTM> items = cmbItemCode.getItems();
            items.clear();
            while(rst.next()) {
                String itemCode = rst.getString("ItemCode");
                String description = rst.getString("Description");
                double unitPrice = rst.getDouble("UnitPrice");
                int qtyOnHand = rst.getInt("QtyOnHand");
                items.add(new ItemTM(itemCode,description,qtyOnHand,unitPrice));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
