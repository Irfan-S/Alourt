/*
 * Copyright (c) 2020 Irfan S.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Syed Irfan Ahmed <irfansa1@ymail.com>
 */

package irfan.apps.alourt.Services;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import irfan.apps.alourt.AlertPage;
import irfan.apps.alourt.Handlers.SharedPrefsHandler;
import irfan.apps.alourt.R;
import irfan.apps.alourt.Utils.Activator;
import irfan.apps.alourt.Utils.FetchAddressTask;
import irfan.apps.alourt.Utils.Variables;


//TODO location not being sent check and fix

public class AccessibilityKeyDetector extends AccessibilityService implements
        FetchAddressTask.OnTaskCompleted {


    private final String TAG = "AccessKeyDetector";
    private int counter = 0;
    Intent notifyIntent;

    String activatorName;

    ValueEventListener listener;

    String group;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;


    String location;

    Long mobile;

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
            checkGpsStatus();
            attachGPSListener();
            startTrackingLocation();
            activatorName = sph.loadName();
            Variables.isCreator = true;
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

//    /**
//     * Sends alert message to firebase thru "activated" node for all groups the user is a part of. Attaches mobile number into node.
//     */
//    private void sendAlertBroadcast() {
//        if (Variables.alourtDatabaseReference == null) {
//            Variables.alourtDatabaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.groups_Firebase));
//        }
//        // Write a message to the database
//        //for (String bucket : buckets) {
//        Log.d(TAG,"Sending broadcast..");
//        if (!group.isEmpty()) {
//            checkGpsStatus();attachGPSListener();startTrackingLocation();
//
//
//        }
//
//        //}
//    }

    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "Service connected");
        sph = new SharedPrefsHandler(getApplicationContext());
        group = sph.loadGroup();
        if (Variables.alourtUser == null) {
            Variables.alourtUser = FirebaseAuth.getInstance().getCurrentUser();
        }
        if (group.isEmpty()) {
            detachListener();
        }
        if (Variables.alourtUser != null) {
            attachListener();
        } else {
            Log.d(TAG, "Unable to start Firebase Auth, please retry");
        }

    }

    @SuppressLint("MissingPermission")
    public void attachGPSListener() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);

        mLocationCallback = new LocationCallback() {
            /**
             * This is the callback that is triggered when the
             * FusedLocationClient updates your location.
             *
             * @param locationResult The result containing the device location.
             */
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // If tracking is turned on, reverse geocode into an address
                if (Variables.shouldGpsBeOff) {
                    new FetchAddressTask(AccessibilityKeyDetector.this, AccessibilityKeyDetector.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };
    }

    private void startTrackingLocation() {
        Variables.shouldGpsBeOff = true;
        mFusedLocationClient.requestLocationUpdates
                (getLocationRequest(),
                        mLocationCallback,
                        null /* Looper */);
        }


    /*
    Used to detect external activations from Firebase.
     */
    @SuppressLint("MissingPermission")
    public void attachListener() {
        if (listener != null) {
            group = sph.loadGroup();
            if (Variables.alourtDatabaseReference == null) {
                Variables.alourtDatabaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.groups_Firebase));
            }


            mobile = sph.loadMobile();
            Log.d(TAG, "Group is: " + group + " and is creator = " + Variables.isCreator);

            Log.d(TAG, "Attaching listener for" + group);
            listener = Variables.alourtDatabaseReference.child(group).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Detected database change...");
                    Activator resp;
                    try {
                        resp = dataSnapshot.child(getString(R.string.activated_Firebase)).getValue(Activator.class);
                    } catch (NullPointerException e) {
                        resp = null;
                    }
                    Log.d(TAG, "Response from group is " + resp);
                    if (resp != null) {
                        mobile = resp.getMobile();
                        if (!Variables.isCreator) {
                            location = resp.getLocation();
                            startAlertPage();
                        }
                        activatorName = resp.getName();

                    } else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            // }
        }

    }

    public void detachListener() {
        if (listener != null) {
            Log.d(TAG, "Detaching listener");
            Variables.alourtDatabaseReference.child(group).removeEventListener(listener);
            listener = null;
            sph.saveGroup(null);
        }
    }

    private void startAlertPage() {
        Log.d(TAG, "Initiating alert box");
        notifyIntent = new Intent(this, AlertPage.class);
        notifyIntent.putExtra(getString(R.string.group_name_IntentPackage), group);
        notifyIntent.putExtra("activator_name", activatorName);
        notifyIntent.putExtra(getString(R.string.mobile_IntentPackage), mobile);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(notifyIntent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "Accessibility event triggered");
        if (Variables.shouldCleanListener) {
            Log.d(TAG, "Cleaning old listener");
            detachListener();
            Variables.shouldCleanListener = false;
        }

    }


    @Override
    public void onInterrupt() {

    }

    public void checkGpsStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (GpsStatus) {
            Log.d(TAG, "Location enabled");
        } else {
            Toast.makeText(this, "Please enable location for active tracking", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    @Override
    public void onTaskCompleted(String result) {
        if (Variables.shouldGpsBeOff) {
            if (Variables.alourtDatabaseReference == null) {
                Variables.alourtDatabaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.groups_Firebase));
            }
            // Update the UI
            Log.v(TAG, "Task response: " + result);
            //Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).child("location").setValue(result);
            Activator user;
            location = result;
            if (location != null && sph.loadName() != null) {
                user = new Activator(sph.loadMobile(), location, sph.loadName());
            } else {
                user = new Activator(sph.loadMobile(), "NA", sph.loadName());
            }
            Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).setValue(user);
            startAlertPage();
            stopTrackingLocation();
            //Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).child("longitude").setValue(longitude);
        }
    }

    private void stopTrackingLocation() {
        if (Variables.shouldGpsBeOff) {
            Variables.shouldGpsBeOff = false;
        }
    }


}

