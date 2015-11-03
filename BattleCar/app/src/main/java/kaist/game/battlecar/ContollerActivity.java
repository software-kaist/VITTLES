package kaist.game.battlecar;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import kaist.game.battlecar.view.JoystickView;

public class ContollerActivity extends Activity {

    private TextView angleTextView;
    private TextView powerTextView;
    private TextView directionTextView;
    // Importing also other views
    private JoystickView joystick;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contoller);

        angleTextView = (TextView) findViewById(R.id.angleTextView);
        powerTextView = (TextView) findViewById(R.id.powerTextView);
        directionTextView = (TextView) findViewById(R.id.directionTextView);
        //Referencing also other views
        joystick = (JoystickView) findViewById(R.id.joystickView);

        //Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {
                // TODO Auto-generated method stub
                angleTextView.setText(" " + String.valueOf(angle) + "°");
                powerTextView.setText(" " + String.valueOf(power) + "%");
                switch (direction) {
                    case JoystickView.FRONT:
                        directionTextView.setText("front");
                        break;
                    case JoystickView.FRONT_RIGHT:
                        directionTextView.setText("front_right");
                        break;
                    case JoystickView.RIGHT:
                        directionTextView.setText("right");
                        break;
                    case JoystickView.RIGHT_BOTTOM:
                        directionTextView.setText("right_bottom");
                        break;
                    case JoystickView.BOTTOM:
                        directionTextView.setText("bottom");
                        break;
                    case JoystickView.BOTTOM_LEFT:
                        directionTextView.setText("bottom_left");
                        break;
                    case JoystickView.LEFT:
                        directionTextView.setText("left");
                        break;
                    case JoystickView.LEFT_FRONT:
                        directionTextView.setText("left_front");
                        break;
                    default:
                        directionTextView.setText("center");
                }
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

}
