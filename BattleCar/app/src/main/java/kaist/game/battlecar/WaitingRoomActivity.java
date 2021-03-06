package kaist.game.battlecar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import kaist.game.battlecar.service.BluetoothService;
import kaist.game.battlecar.util.Const;
import kaist.game.battlecar.util.Utils;

public class WaitingRoomActivity extends Activity {
    // Debugging
    private static final String TAG = WaitingRoomActivity.class.getSimpleName();
    private static final boolean D = true;

    public final static String EXTRA_ROOM_OWNER = "isOwner";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private boolean mIsOwner = false;
    private BluetoothService btService = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        boolean mConnected = false;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            if(mIsOwner) {
                                findViewById(R.id.button_start).setVisibility(View.VISIBLE);
                            }
                            mConnected = true;
                            break;
                        case BluetoothService.STATE_CONNECTING:
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            if(mIsOwner) {
                                findViewById(R.id.button_start).setVisibility(View.GONE);
                            } else {
                                if(mConnected) {
                                    finish();
                                }
                            }
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    if (writeMessage.length() > 0) {
                        Toast.makeText(getApplicationContext(), writeMessage, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothService.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.length() > 0) {
                        if(readMessage.equals(getString(R.string.start_battle))) {
                            startBattleGame();
                        }
                        Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothService.MESSAGE_DEVICE_NAME:
                    String connectedDeviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                    //Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    setCarList(connectedDeviceName);
                    break;
                case BluetoothService.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothService.TOAST),
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, msg.getData().getString(BluetoothService.TOAST));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_waiting_room);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        btService = BluetoothService.getInstance(this);
        btService.setHandler(mHandler);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (getIntent() != null && getIntent().hasExtra(EXTRA_ROOM_OWNER)) {
            mIsOwner = getIntent().getBooleanExtra(EXTRA_ROOM_OWNER, false);
            if(mIsOwner) {
                //Toast.makeText(getApplicationContext(),"I am an owner",Toast.LENGTH_SHORT).show();
                ensureDiscoverable();
                btService.ServerStart();
            } else {
                //Toast.makeText(getApplicationContext(),"I am a guest",Toast.LENGTH_SHORT).show();
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        }
        ((Button)findViewById(R.id.button_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((ImageButton)findViewById(R.id.button_start)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(getString(R.string.start_battle));
                startBattleGame();
            }
        });

        if (mIsOwner) {
            findViewById(R.id.Car1Btn).setVisibility(View.VISIBLE);
            TextView car1Name = (TextView) findViewById(R.id.car1_name);
            car1Name.setVisibility(View.VISIBLE);
            car1Name.setText("Me:" + mBluetoothAdapter.getName());
        }
    }

    @Override
    protected void onResume() {
        Utils.setCleanView(this, false);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        btService.stop();
        super.onDestroy();
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (btService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            btService.write(send);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    btService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    //setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "BT not enabled", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void startBattleGame() {
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        intent.putExtra(Const.EXTRA_BATTLE_MODE, true);
        startActivity(intent);
    }

    private void setCarList(String connectedDeviceName) {
        if(mIsOwner) {
            findViewById(R.id.Car2Btn).setVisibility(View.VISIBLE);
            TextView car2Name = (TextView)findViewById(R.id.car2_name);
            car2Name.setVisibility(View.VISIBLE);
            car2Name.setText(connectedDeviceName);
        } else {
            findViewById(R.id.Car1Btn).setVisibility(View.VISIBLE);
            TextView car1Name = (TextView)findViewById(R.id.car1_name);
            car1Name.setVisibility(View.VISIBLE);
            car1Name.setText("Owner:"+connectedDeviceName);
            findViewById(R.id.Car2Btn).setVisibility(View.VISIBLE);
            TextView car2Name = (TextView)findViewById(R.id.car2_name);
            car2Name.setVisibility(View.VISIBLE);
            car2Name.setText("Me:"+mBluetoothAdapter.getName());
        }
    }
}
