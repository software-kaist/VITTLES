package kaist.game.battlecar.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import java.util.HashMap;

/**
 * Created by SUNgHOOn on 2015-12-13.
 */
public class VittlesEffector {
    private final static String TAG = VittlesEffector.class.getSimpleName();
    private Context ctx;
    // 진동, 소리, 그래픽
    private boolean vibrationOn = true;
    private boolean soundOn = true;
    private boolean actionOn = true;

    private SoundPool soundPool;
    private HashMap<Integer, Integer> soundPoolMap;
    private AudioManager  audioManager;

//    private boolean shotVibration;
//    private boolean shotSound;
//    private boolean shotAction;
//    private boolean hitVibration;
//    private boolean hitSound;
//    private boolean hitAction;

    public VittlesEffector(Context ctx) {
        this.ctx = ctx;
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        soundPoolMap = new HashMap<Integer, Integer>();
        audioManager = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
    }

    public void playVibration(int msec) {
        if (vibrationOn) {
            Vibrator vibe = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
//         long[] pattern = {1000, 200, 1000, 2000, 1200};
//         vibe.vibrate(pattern, 0);
            vibe.vibrate(msec);
        }
    }

    public void addSound(int index,int soundId) {
        soundPoolMap.put(index, soundPool.load(ctx, soundId, index));
    }

    public void playSound(int index) {
        if (soundOn) {
            int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            soundPool.play(soundPoolMap.get(index), streamVolume, streamVolume, 1, 0, 1f);
        }
    }

    public void playLoopedSound(int index) {
        if (soundOn) {
            int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            soundPool.play(soundPoolMap.get(index), streamVolume, streamVolume, 1, -1, 1f);
        }
    }

    public void playAction(int index) {
        if (actionOn) {

        }
    }

    public void playEffect (int index, int msec) {
        playSound(index);
        playVibration(msec);
        playAction(index);
    }
}
