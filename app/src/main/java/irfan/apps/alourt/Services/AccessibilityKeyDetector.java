package irfan.apps.alourt.Services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import irfan.apps.alourt.AlertPage;
import irfan.apps.alourt.Handlers.SharedPrefsHandler;

public class AccessibilityKeyDetector extends AccessibilityService {


    private final String TAG = "AccessKeyDetector";
    private int counter = 0;
    Intent notifyIntent;
    FirebaseDatabase database;
    DatabaseReference myRef;

    SharedPrefsHandler sph;

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
            sendAlertBroadcast();
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(notifyIntent);
            counter = 0;
        } else if (counter > 1) {
            Log.d(TAG, "Resetting due to invalid trigger");
            counter = 0;
        }

        return super.onKeyEvent(event);
    }

    private void sendAlertBroadcast() {
        // Write a message to the database

        myRef.setValue(1);
    }

    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "Service connected");

        sph = new SharedPrefsHandler(getApplicationContext());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String UID = user.getUid();
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("groups");
            //Extracting user's buckets, that will be notified.


            notifyIntent = new Intent(this, AlertPage.class);

            ArrayList<String> buckets = sph.retrieveBuckets();

            for (final String bucket : buckets) {
                Log.d(TAG, "Attaching listener for" + bucket);
                myRef.child(bucket).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int resp = dataSnapshot.child("activated").getValue(Integer.class);
                        if (resp == 1) {
                            notifyIntent.putExtra("bucket_name", bucket);
                            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(notifyIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            //demo has been set to a fixed id
//            myRef.child("10001").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    int resp = dataSnapshot.child("activated").getValue(Integer.class);
//                    if (resp == 1) {
//                        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        getApplicationContext().startActivity(notifyIntent);
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
        } else {
            Log.d(TAG, "Unable to start Firebase Auth, please retry");
        }

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }


    @Override
    public void onInterrupt() {

    }
}
