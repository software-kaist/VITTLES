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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import kaist.game.battlecar.service.BluetoothService;
import kaist.game.battlecar.util.Const;
import kaist.game.battlecar.util.Utils;
import kaist.game.battlecar.util.WifiApManager;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private BluetoothService btService = null;
    private boolean mIsOwner = false;
    private SharedPreferences setting;

    private View mChangeCarMenu;
    private View mPlayMenu;
    private View mItemStoreMenu;
    private View mSettingMenu;
    private View mExitMenu;
    private View mBattleMenu;
    private View mRacingMenu;
    private View mCreateMenu;
    private View mJoinMenu;

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

        mChangeCarMenu = findViewById(R.id.ChangeCarBtn);
        mPlayMenu = findViewById(R.id.PlayGameBtn);
        mItemStoreMenu = findViewById(R.id.ItemStoreBtn);
        mSettingMenu = findViewById(R.id.SettingBtn);
        mExitMenu = findViewById(R.id.ExitBtn);
        mBattleMenu = findViewById(R.id.BattleModeBtn);
        mRacingMenu = findViewById(R.id.RacingModeBtn);
        mCreateMenu = findViewById(R.id.CreateBtn);
        mJoinMenu = findViewById(R.id.JoinBtn);
        startMenuAnimation(mChangeCarMenu, true);
        startMenuAnimation(mPlayMenu, true);
        startMenuAnimation(mItemStoreMenu, true);
        startMenuAnimation(mSettingMenu, true);
        startMenuAnimation(mExitMenu, true);
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
        startMenuAnimation(mChangeCarMenu, false);
        startMenuAnimation(mPlayMenu, false);
        startMenuAnimation(mItemStoreMenu, false);
        startMenuAnimation(mSettingMenu, false);
        startMenuAnimation(mExitMenu, false);
        startMenuAnimation(mBattleMenu, true);
        startMenuAnimation(mRacingMenu, true);

        findViewById(R.id.Back1Btn).setVisibility(View.VISIBLE);
        boolean isWifiAP = WifiApManager.setWifiApEnabled(this, true);
        Log.w(TAG, "wifi AP mode :" + isWifiAP);
    }

    public void onItemStoreBtnClicked(View v) {
        boolean isWifiAP = WifiApManager.setWifiApEnabled(this, false);
        Log.w(TAG, "wifi AP mode :" + isWifiAP);
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
        startMenuAnimation(mBattleMenu, false);
        startMenuAnimation(mRacingMenu, false);
        startMenuAnimation(mCreateMenu, true);
        startMenuAnimation(mJoinMenu, true);

        findViewById(R.id.Back1Btn).setVisibility(View.INVISIBLE);
        findViewById(R.id.Back2Btn).setVisibility(View.VISIBLE);
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
        startMenuAnimation(mChangeCarMenu, true);
        startMenuAnimation(mPlayMenu, true);
        startMenuAnimation(mItemStoreMenu, true);
        startMenuAnimation(mSettingMenu, true);
        startMenuAnimation(mExitMenu, true);
        startMenuAnimation(mBattleMenu, false);
        startMenuAnimation(mRacingMenu, false);

        findViewById(R.id.Back1Btn).setVisibility(View.INVISIBLE);
    }
    public void onBack2BtnClicked(View v) {
        startMenuAnimation(mBattleMenu, true);
        startMenuAnimation(mRacingMenu, true);
        startMenuAnimation(mCreateMenu, false);
        startMenuAnimation(mJoinMenu, false);

        findViewById(R.id.Back1Btn).setVisibility(View.VISIBLE);
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

    private void startMenuAnimation(View view, boolean isShow) {
        if(isShow) {
            view.startAnimation(inFromRightAnimation(view));
        } else {
            view.startAnimation(outToRightAnimation(view));
        }
    }

    private Animation inFromRightAnimation(final View view) {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(600);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        inFromRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return inFromRight;
    }

    private Animation outToRightAnimation(final View view) {
        Animation outToRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outToRight.setDuration(600);
        outToRight.setInterpolator(new AccelerateInterpolator());
        outToRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return outToRight;
    }
}
