package kaist.game.battlecar.service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

/**
 * Created by songhochan on 15. 12. 2..
 */
public class BluetoothService {
    // Debugging
    private static final String TAG = BluetoothService.class.getSimpleName();

    // Intent request code
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    private BluetoothAdapter btAdapter;

    private Activity mActivity;
    private Handler mHandler;

    // Constructors
    public BluetoothService(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;

        // BluetoothAdapter 얻기
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Check the Bluetooth support
     * @return boolean
     */
    public boolean getDeviceState() {
        Log.i(TAG, "Check the Bluetooth support");

        if(btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");

            return false;

        } else {
            Log.d(TAG, "Bluetooth is available");

            return true;
        }
    }

    /**
     * Check the enabled Bluetooth
     */
    public void enableBluetooth() {
        Log.i(TAG, "Check the enabled Bluetooth");


        if(btAdapter.isEnabled()) {
            // 기기의 블루투스 상태가 On인 경우
            Log.d(TAG, "Bluetooth Enable Now");

            // Next Step
        } else {
            // 기기의 블루투스 상태가 Off인 경우
            Log.d(TAG, "Bluetooth Enable Request");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    public boolean isEnable() {
        return btAdapter.isEnabled();
    }

}