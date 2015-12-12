package kaist.game.battlecar.adapter;

/**
 * Created by user on 2015-12-12.
 */
public class CarApList {
    private String carApSSID;
    private String capabilities;

    public String getCarApSSID() {
        return carApSSID;
    }

    public void setCarApSSID(String carApSSID) {
        this.carApSSID = carApSSID;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public CarApList(String carApSSID, String capabilities) {
        this.carApSSID = carApSSID;
        this.capabilities = capabilities;
    }

}
