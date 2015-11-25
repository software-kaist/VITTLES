package kaist.game.battlecar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.github.com"));
        startActivity(myIntent);
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
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        startActivity(intent);
    }

    public void onJoinBtnClicked(View v) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
        startActivity(myIntent);
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
}
