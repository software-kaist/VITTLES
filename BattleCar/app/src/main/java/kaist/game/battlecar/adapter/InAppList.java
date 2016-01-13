package kaist.game.battlecar.adapter;

/**
 * Created by user on 2016-01-13.
 */
public class InAppList {
    private String productId;
    private String description;
    private String price;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public InAppList(String productId, String description, String price) {
        this.productId = productId;
        this.description = description;
        this.price = price;
    }
}
