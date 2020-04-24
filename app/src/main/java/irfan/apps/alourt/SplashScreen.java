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
import com.google.firebase.auth.PhoneAuthProvider;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Intent intent;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private final String TAG = "AlourtSplash";

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();


        mAuth = FirebaseAuth.getInstance();


        intent = new Intent(getApplicationContext(), Login.class);

        CountDownTimer countDownTimer = new CountDownTimer(1000, 1000) {

            public void onTick(long millisecondsUntilDone) {

                //countdown is counting(every second)
            }

            public void onFinish() {
                Log.d(TAG, "Background sign-in running...");

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "signInWithCredential:success");

                    // [START_EXCLUDE]
                    startActivity(new Intent(getApplicationContext(), Home.class));
                    finish();
                } else {
                    startActivity(intent);
                }

            }


        };
        countDownTimer.start();
    }

}
