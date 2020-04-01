package irfan.apps.alourt.Services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import irfan.apps.alourt.AlertPage;

public class AccessibilityKeyDetector extends AccessibilityService {


    private final String TAG = "AccessKeyDetector";
    private int counter = 0;

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG, "Key pressed via accessibility is: " + event.getKeyCode() + " and counter is: " + counter);


        //To trigger, press vol up, vol down and vol up again. Need a wait system that will wait for input within the next 2 seconds, or it will just reset

        if (counter % 2 == 0 && event.getKeyCode() == 24) {
            counter += 1;
            Log.d(TAG, "Counter after 24 is: " + counter);
            new CountDownTimer(2000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Resetting due to wait trigger");
                    counter = 0;
                }
            }.start();
        } else if (counter % 2 == 1 && event.getKeyCode() == 25) {
            counter += 2;
            Log.d(TAG, "Counter after 25 is: " + counter);
        } else if (counter == 5) {
            Log.d(TAG, "Triggering ringtone...");
            Intent notifyIntent = new Intent(this, AlertPage.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(notifyIntent);
            counter = 0;
        } else if (counter > 1) {
            Log.d(TAG, "Resetting due to invalid trigger");
            counter = 0;
        }

        return super.onKeyEvent(event);
    }


    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "Service connected");

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }


    @Override
    public void onInterrupt() {

    }
}
