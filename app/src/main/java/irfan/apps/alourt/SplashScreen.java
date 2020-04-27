package irfan.apps.alourt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import irfan.apps.alourt.Utils.Variables;


public class SplashScreen extends AppCompatActivity {

    private Intent intent;

    private final String TAG = "AlourtSplash";

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();


        intent = new Intent(getApplicationContext(), Login.class);

        CountDownTimer countDownTimer = new CountDownTimer(2000, 1000) {

            public void onTick(long millisecondsUntilDone) {

                //Do nothing.
            }

            public void onFinish() {
                Log.d(TAG, "Background sign-in running...");
                Variables.alourtAuth = FirebaseAuth.getInstance();
                FirebaseUser user = Variables.alourtAuth.getCurrentUser();

                //Fetching local instance of Firebase Auth. If it doesn't exist, then relaunch login page.
                if (user != null) {
                    Log.d(TAG, "signInWithCredential:success");
                    startActivity(new Intent(getApplicationContext(), Home.class));
                    finish();
                } else {
                    startActivity(intent);
                    finish();
                }

            }


        };
        countDownTimer.start();
    }

}