package kaist.game.battlecar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import kaist.game.battlecar.service.BluetoothService;
import kaist.game.battlecar.util.Const;
import kaist.game.battlecar.util.Utils;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private BluetoothService btService = null;
    private boolean mIsOwner = false;
    private SharedPreferences setting;

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setting =  PreferenceManager.getDefaultSharedPreferences(this);

        TextView text = (TextView)findViewById(R.id.textViewName);
        text.setText("My VITTLES Name: " + setting.getString("my_vittles_ap", "NO_VITTLES"));
        text.setTextSize(20);
        text.setTextColor(Color.YELLOW);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        Utils.setCleanView(this, false);
        super.onResume();
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
        Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
        startActivity(intent);
    }

    public void onSettingBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void onExitBtnClicked(View v) {
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
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
        intent.putExtra(Const.EXTRA_BATTLE_MODE, false);
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
        mIsOwner = isOwner;
        if(btService == null) {
            btService = BluetoothService.getInstance(this);
            btService.setHandler(mHandler);
        }
        if (!btService.isEnable()) {
            if (btService.getDeviceState()) {
                // 블루투스가 지원 가능한 기기일 때
                btService.enableBluetooth();
            } else {
                Toast.makeText(getApplicationContext(), "Your devices is not supported bluetooth", Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(getApplicationContext(),"Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
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
