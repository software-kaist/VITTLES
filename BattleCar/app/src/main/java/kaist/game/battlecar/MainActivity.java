package kaist.game.battlecar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import kaist.game.battlecar.service.BluetoothService;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private BluetoothService btService = null;
    private int currentApiVersion;
    private boolean mIsOwner = false;

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
        }

        setContentView(R.layout.activity_main);
    }

    public void onChangeCarBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), CarChangeActivity.class);
        startActivity(intent);
    }

    public void onPlayGameBtnClicked(View v) {
        //Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        //startActivity(intent);

        findViewById(R.id.ChangeCarBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.PlayGameBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.ItemStoreBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.SettingBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.ExitBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.BattleModeBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.RacingModeBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.Back1Btn).setVisibility(View.VISIBLE);
    }

    public void onItemStoreBtnClicked(View v) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.daum.net"));
        startActivity(myIntent);
    }

    public void onSettingBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void onExitBtnClicked(View v) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
        startActivity(myIntent);
    }

    public void onBattleModeBtnClicked(View v) {

        findViewById(R.id.BattleModeBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.RacingModeBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.Back1Btn).setVisibility(View.INVISIBLE);
        findViewById(R.id.Back2Btn).setVisibility(View.VISIBLE);
        findViewById(R.id.CreateBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.JoinBtn).setVisibility(View.VISIBLE);
    }

    public void onRacingModeBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        startActivity(intent);
    }

    public void onCreateBtnClicked(View v) {
        BTSetup(true);
    }

    public void onJoinBtnClicked(View v) {
        BTSetup(false);
    }

    public void onBack1BtnClicked(View v) {
        findViewById(R.id.ChangeCarBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.PlayGameBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.ItemStoreBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.SettingBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.ExitBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.BattleModeBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.RacingModeBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.Back1Btn).setVisibility(View.INVISIBLE);
    }
    public void onBack2BtnClicked(View v) {
        findViewById(R.id.BattleModeBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.RacingModeBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.Back1Btn).setVisibility(View.VISIBLE);
        findViewById(R.id.CreateBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.JoinBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.Back2Btn).setVisibility(View.INVISIBLE);
    }

    private void BTSetup(boolean isOwner) {
        if(btService == null) {
            btService = new BluetoothService(this, mHandler);
        }
        if (!btService.isEnable()) {
            if (btService.getDeviceState()) {
                // 블루투스가 지원 가능한 기기일 때
                btService.enableBluetooth();
            } else {
                Toast.makeText(getApplicationContext(), "Your devices is not supported bluetooth", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(),"Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), WaitingRoomActivity.class);
            intent.putExtra(WaitingRoomActivity.EXTRA_ROOM_OWNER, isOwner);
            startActivity(intent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {
            case BluetoothService.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(),"Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), WaitingRoomActivity.class);
                    intent.putExtra(WaitingRoomActivity.EXTRA_ROOM_OWNER, mIsOwner);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
