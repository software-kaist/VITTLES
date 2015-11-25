package kaist.game.battlecar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class CreateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_activity);
    }

    public void onAnonymous1BtnClicked(View v) {
        Toast.makeText(getApplicationContext(), "onAnonymous1BtnClicked", Toast.LENGTH_LONG).show();
    }

    public void onAnonymous2BtnClicked(View v) {
        Toast.makeText(getApplicationContext(), "onAnonymous2BtnClicked", Toast.LENGTH_LONG).show();
    }

    public void onXBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
    public void onGoBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        startActivity(intent);
    }
}
