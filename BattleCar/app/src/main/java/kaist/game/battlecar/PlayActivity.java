package kaist.game.battlecar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.freedesktop.gstreamer.GStreamer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import kaist.game.battlecar.AsyncTasks.SendMessageClient;
import kaist.game.battlecar.service.BluetoothService;
import kaist.game.battlecar.service.CarEventReceiver;
import kaist.game.battlecar.util.Const;
import kaist.game.battlecar.util.Utils;
import kaist.game.battlecar.util.VittlesConnector;
import kaist.game.battlecar.util.VittlesEffector;
import kaist.game.battlecar.view.GStreamerSurfaceView;
import kaist.game.battlecar.view.HealthPointBarView;
import kaist.game.battlecar.view.JoystickView;

public class PlayActivity extends Activity implements SurfaceHolder.Callback {
    private final static String TAG = PlayActivity.class.getSimpleName();
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativeSetUri(String uri); // Set the URI of the media to play
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativeSetPosition(int milliseconds); // Seek to the indicated position, in milliseconds
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface); // A new surface is available
    private native void nativeSurfaceFinalize(); // Surface about to be destroyed
    private long native_custom_data;      // Native code will use this to keep private data

    private PowerManager.WakeLock wake_lock;

    private TextView angleTextView;
    private TextView powerTextView;
    private TextView directionTextView;
    private JoystickView joystick;
    private HealthPointBarView mMyHpBar;
    private HealthPointBarView mEnemyHpBar;
    private SharedPreferences setting;
    private VittlesEffector vtEffector;
    private int preMovement = -1;
    private int preSteering = -1;
    private int preGear = -1;
    private int preWeapon = -1;

    boolean bBattleMode = false;
    private String mWifiIpAddress;
    private BluetoothService btService = null;
    private String mVittlesUrl;
    private Context mContext;
    // todo: 전역 변수로 관리
//    private int myHp = 100;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            switch(inputMessage.what) {
                case CarEventReceiver.SIMSOCK_DATA: // Receive my HP data
                    String msg = (String) inputMessage.obj;
                    int myHp = Integer.parseInt(msg);
                    mMyHpBar.setProgress(myHp / 100.0f);
                    sendGameSyncMessage("enemyHP" + msg); // send my HP data to enemy
                    vtEffector.playEffect(1, 300);
                    Log.d(TAG, "My HP : " + msg);
                    break;

                case CarEventReceiver.SIMSOCK_CONNECTED:
                case CarEventReceiver.SIMSOCK_DISCONNECTED:
                    break;
                // BT Message
                case BluetoothService.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) inputMessage.obj;
                    String writeMessage = new String(writeBuf);
                    if (writeMessage.length() > 0) {
                        //Toast.makeText(getApplicationContext(), writeMessage, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothService.MESSAGE_READ: // read enemy HP data
                    byte[] readBuf = (byte[]) inputMessage.obj;
                    String readMessage = new String(readBuf, 0, inputMessage.arg1);
                    if (readMessage.length() > 0) {
                        if (readMessage.contains("enemyHP")) {
                            int enemyHp = Integer.parseInt(readMessage.substring("enemyHP".length()));
                            mEnemyHpBar.setProgress(enemyHp / 100.0f);
                            vtEffector.playEffect(1, 300);
                            Log.d(TAG, "Enemy HP : " + enemyHp);
                        }
                        else if (readMessage.contains("checkIR")) {
                            new BackgroundTask(mVittlesUrl + "/irRead/" + "KEY_1").execute();
                            Log.d(TAG, "checkIR");
                        }
                        //Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setting =  PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Utils.setCleanView(this, true);

        setContentView(R.layout.activity_play);

        btService = BluetoothService.getInstance(this);
        btService.setHandler(mHandler);
        mVittlesUrl = setting.getString("vittles_url", "");
        mContext = this;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wake_lock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GStreamer battle car");
        wake_lock.setReferenceCounted(false);

        setJoyStickView();

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        int ip = wm.getConnectionInfo().getIpAddress();
        mWifiIpAddress = String.format("%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        ImageButton streamingPlay = (ImageButton) this.findViewById(R.id.button_play);
        //streamingPlay.setVisibility(View.GONE);
        streamingPlay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                vtEffector.playEffect(3, 100);

                // todo : Vittels 접속 루틴 여기로 옮겨서 처리!!
                new BackgroundTask(mVittlesUrl + "/camonoff/" + mWifiIpAddress).execute();
            }
        });

        ImageButton pause = (ImageButton) this.findViewById(R.id.button_stop);
        pause.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                /*is_playing_desired = false;
                wake_lock.release();
                nativePause();*/
            }
        });

        mMyHpBar = (HealthPointBarView) findViewById(R.id.myHpProgress);
        mMyHpBar.setProgressColor(getResources().getColor(R.color.hp_red));
        mMyHpBar.setProgressBackgroundColor(getResources().getColor(R.color.hp_white));
        mMyHpBar.useRoundedRectangleShape(30.0f);
        mMyHpBar.setShowingPercentage(true);
        mMyHpBar.setMaximumPercentage(1.0f);
        mMyHpBar.setTextSize(15);

        mEnemyHpBar = (HealthPointBarView) findViewById(R.id.enemyHpProgress);
        mEnemyHpBar.setProgressColor(getResources().getColor(R.color.blue_500));
        mEnemyHpBar.setProgressBackgroundColor(getResources().getColor(R.color.blue_200));
        mEnemyHpBar.useRoundedRectangleShape(50.0f);
        mEnemyHpBar.setShowingPercentage(true);
        mEnemyHpBar.setMaximumPercentage(1.0f);
        mEnemyHpBar.setTextSize(10);

        Button shoot = (Button) this.findViewById(R.id.button_Shoot);
        View scope = (View) this.findViewById(R.id.imagViewScope);
        if (getIntent()!=null && getIntent().hasExtra(Const.EXTRA_BATTLE_MODE)) {
            bBattleMode = getIntent().getBooleanExtra(Const.EXTRA_BATTLE_MODE, false);
            if (bBattleMode) {
                mMyHpBar.setVisibility(View.VISIBLE);
                mEnemyHpBar.setVisibility(View.VISIBLE);
                shoot.setVisibility(View.VISIBLE);
                scope.setVisibility(View.VISIBLE);
                //new BackgroundTask(mVittlesUrl + "/irThreadEnable").execute();
                Log.i("Battle", "Start");
                Log.i("ShootBtn", "Visible:");
            } else {
                mMyHpBar.setVisibility(View.GONE);
                mEnemyHpBar.setVisibility(View.GONE);
                shoot.setVisibility(View.INVISIBLE);
                scope.setVisibility(View.INVISIBLE);
                Log.i("ShootBtn", "Invisible:");
            }
        }
        shoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                ImageView s = (ImageView) findViewById(R.id.imagViewScope);
                s.startAnimation(shake);
                vtEffector.playEffect(2, 100);

                new BackgroundTask(mVittlesUrl + "/irSend/" + "KEY_1").execute();
                Log.i("Shoot", "빵야~");
                // test
                //mMyHpBar.setProgress(0.6f);
                //mEnemyHpBar.setProgress(0.2f);
                sendGameSyncMessage("checkIR");
            }
        });

        SurfaceView sv = (SurfaceView) this.findViewById(R.id.surface_video);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        // Start with disabled buttons, until native code is initialized
        this.findViewById(R.id.button_play).setEnabled(false);
        this.findViewById(R.id.button_stop).setEnabled(false);

        nativeInit();

        // todo : Vittles 자동 접속을 여기서 하면 최초 한번만 하게됨! Click 시로 변경 요망!
		VittlesConnector wifi = new VittlesConnector(this, setting.getString("vittles_ap_prefix", "VITTLES"));
        // todo: Perference에 암호 방식과 암호도 저장해야 함!!
        wifi.Connecting(setting.getString("my_vittles_ap", ""), "WPA", "intintint");

        // Init Vittles Effector
        vtEffector = new VittlesEffector(getBaseContext());
        vtEffector.setOption(setting.getBoolean("vibration_switch", true), setting.getBoolean("sound_effect_switch", true), true);
        vtEffector.addSound(1, R.raw.explosion6);
        vtEffector.addSound(2, R.raw.gunshot);
        vtEffector.addSound(3, R.raw.car_start);
        vtEffector.addSound(4, R.raw.car_engine);
		
        CarEventReceiver mCarEventReceiver = new CarEventReceiver(this, mHandler);
        mCarEventReceiver.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        //new BackgroundTask(setting.getString("vittles_url", "") + "/camonoff/" + mWifiIpAddress).execute();
    }

    private void sendGameSyncMessage(String message) {
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

    @Override
    protected void onResume() {
        Utils.setCleanView(this, false);
        super.onResume();
    }

    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        private String mURL;

        public BackgroundTask(String url){
            mURL = url;
        }
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            request(mURL);
            return null;
        }

        protected void onPostExecute(Integer a) {
        }

    }

    private String request(String urlStr) {
        StringBuilder output = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(1000);
                conn.setRequestMethod("GET");

                int resCode = conn.getResponseCode();
                if (resCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
                    String line = null;
                    while(true) {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        output.append(line + "\n");
                    }
                    reader.close();
                    conn.disconnect();
                }
            }
        } catch(Exception ex) {
            Log.e(TAG, "Exception in processing response.", ex);
            ex.printStackTrace();
        }

        return output.toString();
    }

    private void setJoyStickView() {
        angleTextView = (TextView) findViewById(R.id.angleTextView);
        powerTextView = (TextView) findViewById(R.id.powerTextView);
        directionTextView = (TextView) findViewById(R.id.directionTextView);
        //Referencing also other views
        joystick = (JoystickView) findViewById(R.id.joystickView);

        //Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {
                final String delimiter = "$";
                int movement = 0;
                int steering = 0;
                int gear = 0;
                int weapon = 0;

//                angleTextView.setText(" " + String.valueOf(angle) + "°");
//                powerTextView.setText(" " + String.valueOf(power) + "%");

                switch (direction) {
                    case JoystickView.FRONT:
                        movement = 1;
//                        directionTextView.setText("front");
                        break;
                    case JoystickView.FRONT_RIGHT:
                        movement = 1;
//                        directionTextView.setText("front_right");
                        break;
                    case JoystickView.RIGHT:
                        movement = 0;
//                        directionTextView.setText("right");
                        break;
                    case JoystickView.RIGHT_BOTTOM:
                        movement = 2;
//                        directionTextView.setText("right_bottom");
                        break;
                    case JoystickView.BOTTOM:
                        movement = 2;
//                        directionTextView.setText("bottom");
                        break;
                    case JoystickView.BOTTOM_LEFT:
                        movement = 2;
//                        directionTextView.setText("bottom_left");
                        break;
                    case JoystickView.LEFT:
                        movement = 0;
//                        directionTextView.setText("left");
                        break;
                    case JoystickView.LEFT_FRONT:
                        movement = 1;
//                        directionTextView.setText("left_front");
                        break;
                    default:
                        movement = 0;
//                        directionTextView.setText("center");
                }
                if (angle < -23 && angle > -157) {
                    steering = 1;
                } else if (angle > 23 && angle < 157) {
                    steering = 2;
                } else {
                    steering = 0;
                }
                if (angle > -113 && angle < -67) {
                    steering = 0;
                } else if (angle > 67 && angle < 113) {
                    steering = 0;
                }
                if (power == 0){
                    gear = 0;
                } else if( power > 0 && power <= 25){
                    gear = 1;
                } else if ( power >25 && power <= 50){
                    gear = 2;
                } else if ( power >50 && power <= 75){
                    gear = 3;
                } else if ( power >75 && power <= 100){
                    gear = 4;
                }
                /*
                neutral    0
                movement forward|reverse (1|2)
                steering left|right (1|2)
                "movement,steering,angle(-180~180),power(0~100%),weapon"
                */

                // todo : 주행 효과
//                vtEffector.playEffect(4, 0);

                if ((preMovement == movement) && (preSteering == steering) && (preGear == gear) && (preWeapon == weapon)) {
                    return;
                }

                preMovement = movement;
                preSteering = steering;
                preGear = gear;
                preWeapon = weapon;

                Log.i("Drive", movement + " " + steering + " " + weapon);

//                directionTextView.setText("VITTLES URL: " + mVittlesUrl);

                StringBuilder commandCode = new StringBuilder();
                commandCode.append(movement).append(delimiter).append(steering).append(delimiter).append(angle).append(delimiter).append(gear).append(delimiter).append(weapon);
                //commandCode.append(movement).append(delimiter).append(steering).append(delimiter).append(angle).append(delimiter).append(power).append(delimiter).append(weapon);
                //new BackgroundTask(mVittlesUrl + "/inputBattleCar/" + commandCode.toString()).execute();
                new SendMessageClient(mVittlesUrl).execute(commandCode.toString());
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        nativeFinalize();
        if (bBattleMode) {
            //new BackgroundTask(mVittlesUrl + "/irThreadDisable").execute();
        }
        if (wake_lock.isHeld())
            wake_lock.release();
        //new BackgroundTask(setting.getString("vittles_url", "") + "/camonoff/" + mWifiIpAddress).execute();
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
        final TextView tv = (TextView) this.findViewById(R.id.textview_message);
        runOnUiThread (new Runnable() {
            public void run() {
                tv.setText(message);
            }
        });
    }

    // Set the URI to play, and record whether it is a local or remote file
    private void setMediaUri() {
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "GStreamer initialized:");

        // Restore previous playing state
        //setMediaUri ();
        nativeSetPosition (0);
        //if (is_playing_desired) {
            nativePlay();
            wake_lock.acquire();
        //}
        /* else {
            nativePause();
            wake_lock.release();
        }*/

        // Re-enable buttons, now that GStreamer is initialized
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            public void run() {
                activity.findViewById(R.id.button_play).setEnabled(true);
                activity.findViewById(R.id.button_stop).setEnabled(true);
            }
        });
    }


    // Called from native code
    private void setCurrentPosition(final int position, final int duration) {
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("CarViewStreamer");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit(holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize ();
    }

    // Called from native code when the size of the media changes or is first detected.
    // Inform the video surface about the new size and recalculate the layout.
    private void onMediaSizeChanged (int width, int height) {
        Log.i ("GStreamer", "Media size changed to " + width + "x" + height);
        final GStreamerSurfaceView gsv = (GStreamerSurfaceView) this.findViewById(R.id.surface_video);
        gsv.media_width = width;
        gsv.media_height = height;
        runOnUiThread(new Runnable() {
            public void run() {
                gsv.requestLayout();
            }
        });
    }
}