package kaist.game.battlecar;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import kaist.game.battlecar.util.Utils;

public class StoreActivity extends Activity {

    TextView Vcoin;
    int coin=1500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Vcoin= (TextView)findViewById(R.id.vcoin);
    }

    @Override
    protected void onResume() {
        Utils.setCleanView(this, false);
        super.onResume();
    }

    public void onBack3BtnClicked(View v) {
        finish();
    }
    public void onBuy1BtnClicked(View v) {
        Toast.makeText(getApplicationContext(), "M60 Buy", Toast.LENGTH_SHORT).show();
        coin = coin-2;
        Vcoin.setText("V-coin :" + coin);

    }
    public void onBuy2BtnClicked(View v) {
        Toast.makeText(getApplicationContext(),"M61 Buy", Toast.LENGTH_SHORT).show();
        coin = coin-5;
        Vcoin.setText("V-coin :" + coin);

    }
    public void onBuy3BtnClicked(View v) {

        Toast.makeText(getApplicationContext(),"M62 Buy", Toast.LENGTH_SHORT).show();
        coin = coin-10;
        Vcoin.setText("V-coin :" + coin);

    }
}
