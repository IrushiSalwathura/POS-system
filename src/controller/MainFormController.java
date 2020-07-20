package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFormController {
    public AnchorPane root;

    public void btnManageCustomer_OnAction(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(this.getClass().getResource("/view/CustomerForm.fxml"));
        Scene customerScene = new Scene(root);
        Stage mainStage = (Stage)this.root.getScene().getWindow();
        mainStage.setScene(customerScene);
        mainStage.centerOnScreen();
    }

    public void btnManageItem_OnAction(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(this.getClass().getResource("/view/ItemForm.fxml"));
            Scene itemScene = new Scene(root);
            Stage mainStage = (Stage)this.root.getScene().getWindow();
            mainStage.setScene(itemScene);
            mainStage.centerOnScreen();
        } catch (IOException e) {
            System.out.println("There's problem with loading the Manage Item Form");
            e.printStackTrace();
        }
    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(this.getClass().getResource("/view/PlaceOrderForm.fxml"));
            Scene orderScene = new Scene(root);
            Stage mainStage = (Stage)this.root.getScene().getWindow();
            mainStage.setScene(orderScene);
            mainStage.centerOnScreen();
        } catch (IOException e) {
            System.out.println("There's problem with loading the Place Order Form");
            e.printStackTrace();
        }
    }

    public void btnSearchOrder_OnAction(ActionEvent actionEvent) {
    }
}
