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
        Toast.makeText(getApplicationContext(), "onChangeCarBtnClicked", Toast.LENGTH_LONG).show();
    }

    public void onPlayGameBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        startActivity(intent);
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
}
