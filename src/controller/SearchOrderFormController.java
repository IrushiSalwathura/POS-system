package controller;

import DBConnect.DBConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.CustomerTM;
import util.OrderDetailTM;
import util.SearchTM;

import javax.xml.transform.Result;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SearchOrderFormController {
    public AnchorPane root;
    public Button btnBack;
    public TextField txtSearch;
    public TableView<SearchTM> tblSearch;
    private ArrayList<SearchTM> searchOrdersArray = new ArrayList<>();

    public void initialize(){
        tblSearch.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("orderID"));
        tblSearch.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        tblSearch.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("customerID"));
        tblSearch.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("customerName"));
        tblSearch.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
        loadOrderDetails();

        txtSearch.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ObservableList<SearchTM> search = tblSearch.getItems();
                search.clear();
                String lowerCaseFilter = newValue.toLowerCase();

                for (SearchTM order : searchOrdersArray) {
                    if ((order.getOrderID().toLowerCase().contains(lowerCaseFilter))||
                            order.getCustomerID().toLowerCase().contains(lowerCaseFilter) ||
                            order.getCustomerName().toLowerCase().contains(lowerCaseFilter) ||
                            order.getOrderDate().contains(newValue) ||
                            String.valueOf(order.getTotal()).contains(lowerCaseFilter)){
                        search.add(order);
                    }
                }
            }
        });

    }

    public void loadOrderDetails(){
        try {
            PreparedStatement pstm = DBConnection.getInstance().geConnection().prepareStatement
                    ("SELECT o.OrderID, o.OrderDate, o.CustomerID,c.CustomerName, SUM(od.OrderQTY * od.UnitPrice) AS Total\n" +
                            "FROM orders o INNER JOIN customer c ON c.CustomerID = o.CustomerID\n" +
                            "INNER JOIN orderdetail od ON od.OrderID = o.OrderID\n" +
                            "GROUP BY o.OrderID");
            ResultSet rst = pstm.executeQuery();
            ObservableList<SearchTM> searchDetails = tblSearch.getItems();
            searchDetails.clear();
            while(rst.next()){
                String orderID = rst.getString("OrderID");
                String orderDate = rst.getString("OrderDate");
                String customerID = rst.getString("CustomerID");
                String customerName = rst.getString("CustomerName");
                double total = rst.getDouble("Total");
                SearchTM searchitem = new SearchTM(orderID,orderDate,customerID,customerName,total);
                searchDetails.add(searchitem);
                searchOrdersArray.add(searchitem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

    public void tblSearch_OnMouseClicked(MouseEvent mouseEvent) throws IOException {
        if (tblSearch.getSelectionModel().getSelectedItem() == null){
            return;
        }
        if (mouseEvent.getClickCount() == 2){
            FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/PlaceOrderForm.fxml"));
            Parent root = fxmlLoader.load();
            PlaceOrderFormController controller = (PlaceOrderFormController) fxmlLoader.getController();
            controller.initializeWithSearchOrderForm(tblSearch.getSelectionModel().getSelectedItem().getOrderID());
            Scene orderScene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(orderScene);
            stage.centerOnScreen();
            stage.show();
        }
    }
}
