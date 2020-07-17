package controller;

import Db.DbConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import util.CustomerTM;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CustomerFormController {

    public JFXTextField txtID;
    public JFXTextField txtName;
    public JFXTextField txtAddress;
    public TableView<CustomerTM> tblCustomers;
    public JFXButton btnAdd;
    public JFXButton btnSave;
    public JFXButton btnDelete;
    public ArrayList<CustomerTM> customersList = new ArrayList<CustomerTM>();

    public void initialize(){
        loadCustomers();
        disableFields();

        //mapping columns
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));
    }

    public void btnAdd_OnAction(ActionEvent actionEvent) {
        idAutoIncrement();
        enableFields();
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {

    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {

    }

    private void loadCustomers(){
        try {
            Statement statement = DbConnection.getInstance().getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Customer");
            ObservableList<CustomerTM> customers = tblCustomers.getItems();
            customers.clear();
            while(resultSet.next()){
                String id = resultSet.getString(1);
                String name = resultSet.getString(2);
                String address = resultSet.getString(3);
                customers.add(new CustomerTM(id,name,address));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void idAutoIncrement(){
        try {
            Statement statement = DbConnection.getInstance().getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT CustomerID FROM Customer ORDER BY CustomerID DESC LIMIT 1");

            while(resultSet.next()){
                String id = resultSet.getString(1);
                String number = id.substring(1,4);
                int numberToInt = Integer.parseInt(number);

                if(numberToInt==0){
                    txtID.setText("C001");
                }
                else if(numberToInt>0 && numberToInt<10){
                    numberToInt+=1;
                    txtID.setText("C00" + numberToInt);
                }
                else if(numberToInt>=10 && numberToInt<100){
                    numberToInt+=1;
                    txtID.setText("C0" + numberToInt);
                }
                else{
                    numberToInt+=1;
                    txtID.setText("C" + numberToInt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void disableFields(){
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
        txtID.setDisable(true);
        txtName.setDisable(true);
        txtAddress.setDisable(true);
    }

    private void enableFields(){
        btnSave.setDisable(false);
        btnDelete.setDisable(false);
        txtID.setDisable(false);
        txtName.setDisable(false);
        txtAddress.setDisable(false);
    }
}
