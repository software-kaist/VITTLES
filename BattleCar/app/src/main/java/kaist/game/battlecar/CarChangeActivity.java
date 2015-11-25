package kaist.game.battlecar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class CarChangeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_change);
    }
    public void onCar1BtnClicked(View v) {
        Toast.makeText(getApplicationContext(), "onCar1BtnClicked", Toast.LENGTH_LONG).show();
    }

    public void onCar2BtnClicked(View v) {
        Toast.makeText(getApplicationContext(), "onCar2BtnClicked", Toast.LENGTH_LONG).show();
    }

    public void onXBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}
