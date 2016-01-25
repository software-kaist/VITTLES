package kaist.game.battlecar.adapter;

/**
 * Created by user on 2016-01-13.
 */
public class ItemInfo {
    private String itemName;
    private int itemValue;
    private int itemPrice;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemValue() {
        return itemValue;
    }

    public void setItemValue(int itemValue) {
        this.itemValue = itemValue;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public ItemInfo(String itemName, int itemValue, int itemPrice) {
        this.itemName = itemName;
        this.itemValue = itemValue;
        this.itemPrice = itemPrice;
    }
}
