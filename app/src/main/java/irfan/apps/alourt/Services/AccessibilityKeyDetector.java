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

import irfan.apps.alourt.AlertPage;
import irfan.apps.alourt.Handlers.SharedPrefsHandler;
import irfan.apps.alourt.Home;
import irfan.apps.alourt.R;

public class AccessibilityKeyDetector extends AccessibilityService {


    private final String TAG = "AccessKeyDetector";
    private int counter = 0;
    Intent notifyIntent;
    FirebaseDatabase database;
    DatabaseReference myRef;

    ValueEventListener listener;

    String group;

    boolean isCreator;

    Long mobile;
//    ArrayList<String> buckets,alertBuckets;
//    ArrayList<String> mobileAlerts;

    SharedPrefsHandler sph;

    //String temp;

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        boolean op = super.onKeyEvent(event);
        Log.d(TAG, "Key pressed via accessibility is: " + event.getKeyCode() + " and counter is: " + counter);

        group = sph.loadGroup();
        attachListener();


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
        } else if (counter == 5) {
            Log.d(TAG, "Triggering ringtone...");
            sendAlertBroadcast();
            isCreator = true;
            attachListener();
            counter = 0;
        } else if (counter % 2 == 1 && event.getKeyCode() == 25) {
            counter += 2;
            Log.d(TAG, "Counter after 25 is: " + counter);
        } else if (counter > 1) {
            Log.d(TAG, "Resetting due to invalid trigger");
            counter = 0;
        }

        return op;
    }

    /**
     * Sends alert message to firebase thru "activated" node for all groups the user is a part of. Attaches mobile number into node.
     */
    private void sendAlertBroadcast() {
        // Write a message to the database
        //for (String bucket : buckets) {
        if (!group.isEmpty()) {
            myRef.child(group).child(getString(R.string.activated_Firebase)).setValue(sph.loadMobile());
        }

        //}
    }

    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "Service connected");
        isCreator = false;
        sph = new SharedPrefsHandler(getApplicationContext());
        group = sph.loadGroup();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (group.isEmpty()) {
            detachListener();
        }
        if (user != null) {
            attachListener();
        } else {
            Log.d(TAG, "Unable to start Firebase Auth, please retry");
        }

    }


    public void attachListener() {
        database = FirebaseDatabase.getInstance();
        group = sph.loadGroup();
        myRef = database.getReference(getString(R.string.groups_Firebase));

        notifyIntent = new Intent(this, AlertPage.class);

        //buckets = sph.retrieveBuckets();
        //alertBuckets= new ArrayList<>();
        //mobileAlerts = new ArrayList<>();

        mobile = sph.loadMobile();
        Log.d(TAG, "Group is: " + group + " and is creator = " + isCreator);

        //for (String bucket : buckets) {
        //temp =bucket;
        Log.d(TAG, "Attaching listener for" + group);
        listener = myRef.child(group).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long resp;
                try {
                    resp = dataSnapshot.child(getString(R.string.activated_Firebase)).getValue(Long.class);
                } catch (NullPointerException e) {
                    resp = 0;
                }
                Log.d(TAG, "Response from group is " + resp);
                if (resp != 0) {
//                        alertBuckets.add(temp);
//                        mobileAlerts.add(String.valueOf(resp));
                    mobile = resp;
                    startAlertPage();
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // }

    }

    public void detachListener() {
        if (listener != null) {
            Log.d(TAG, "Detaching listener");
            myRef.child(group).removeEventListener(listener);
            listener = null;
            sph.saveGroup(null);
        }
    }

    private void startAlertPage() {
        Log.d(TAG, "Initiating alert box");
        notifyIntent.putExtra(getString(R.string.isCreator_IntentPackage), isCreator);
        notifyIntent.putExtra(getString(R.string.group_name_IntentPackage), group);
        notifyIntent.putExtra(getString(R.string.mobile_IntentPackage), mobile);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        alertBuckets.clear();
//        mobileAlerts.clear();
        getApplicationContext().startActivity(notifyIntent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "Accessibility event triggered");
        if (Home.clean) {
            Log.d(TAG, "Cleaning old listener");
            detachListener();
        }

    }


    @Override
    public void onInterrupt() {

    }
}
