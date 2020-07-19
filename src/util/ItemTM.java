package util;

public class ItemTM {
    private String item_code;
    private String description;
    private int quantity_on_hand;
    private double unit_price;

    public ItemTM() {
    }

    public ItemTM(String item_code, String description, int quantity_on_hand, double unit_price) {
        this.item_code = item_code;
        this.description = description;
        this.quantity_on_hand = quantity_on_hand;
        this.unit_price = unit_price;
    }

    public String getItem_code() {
        return item_code;
    }

    public void setItem_code(String item_code) {
        this.item_code = item_code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity_on_hand() {
        return quantity_on_hand;
    }

    public void setQuantity_on_hand(int quantity_on_hand) {
        this.quantity_on_hand = quantity_on_hand;
    }

    public double getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(double unit_price) {
        this.unit_price = unit_price;
    }

    @Override
    public String toString() {
        return "ItemTM{" +
                "item_code='" + item_code + '\'' +
                '}';
    }
}
