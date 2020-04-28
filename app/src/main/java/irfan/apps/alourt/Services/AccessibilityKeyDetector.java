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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import irfan.apps.alourt.AlertPage;
import irfan.apps.alourt.Handlers.SharedPrefsHandler;
import irfan.apps.alourt.R;
import irfan.apps.alourt.Utils.Activator;
import irfan.apps.alourt.Utils.Variables;


//TODO location not being sent check and fix

public class AccessibilityKeyDetector extends AccessibilityService {


    private final String TAG = "AccessKeyDetector";
    private int counter = 0;
    Intent notifyIntent;

    String activatorName;

    ValueEventListener listener;

    String group;

    boolean isCreator;


    String latitude;
    String longitude;

    LocationManager locationManager;
    LocationListener locationListener;
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
            activatorName = sph.loadName();
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
            Activator user;
            if (latitude != null & longitude != null) {
                user = new Activator(sph.loadMobile(), latitude, longitude, sph.loadName());
            } else {
                user = new Activator(sph.loadMobile(), "NA", "NA", sph.loadName());
            }
            Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).setValue(user);
            if (isCreator) {
                checkGpsStatus();
                attachGPSListener();
            }

        }

        //}
    }

    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "Service connected");
        isCreator = false;
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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Log.d(TAG, "Attaching GPS Listener");
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            Log.d(TAG, "Using old GPS location");
            latitude = String.valueOf(locationGPS.getLatitude());
            longitude = String.valueOf(locationGPS.getLongitude());
            Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).child("latitude").setValue(latitude);
            Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).child("longitude").setValue(longitude);
        } else {
            Log.d(TAG, "Using old Net location");
            latitude = String.valueOf(locationNet.getLatitude());
            longitude = String.valueOf(locationNet.getLongitude());
            Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).child("latitude").setValue(latitude);
            Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).child("longitude").setValue(longitude);
        }
        if (latitude == null && longitude == null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Variables.shouldGpsBeOff = true;
                    longitude = String.valueOf(location.getLongitude());
                    Log.v(TAG, longitude);
                    latitude = String.valueOf(location.getLatitude());
                    Log.v(TAG, latitude);
                    Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).child("latitude").setValue(latitude);
                    Variables.alourtDatabaseReference.child(group).child(getString(R.string.activated_Firebase)).child("longitude").setValue(longitude);
                    detachGPSListener();
                }


                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        }
    }

    public void detachGPSListener() {
        Log.d(TAG, "Detaching GPS listener");
        if (locationManager != null && Variables.shouldGpsBeOff) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @SuppressLint("MissingPermission")
    public void attachListener() {
        group = sph.loadGroup();
        if (Variables.alourtDatabaseReference == null) {
            Variables.alourtDatabaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.groups_Firebase));
        }

        notifyIntent = new Intent(this, AlertPage.class);

        //buckets = sph.retrieveBuckets();
        //alertBuckets= new ArrayList<>();
        //mobileAlerts = new ArrayList<>();

        mobile = sph.loadMobile();
        Log.d(TAG, "Group is: " + group + " and is creator = " + isCreator);

        //for (String bucket : buckets) {
        //temp =bucket;
        Log.d(TAG, "Attaching listener for" + group);
        listener = Variables.alourtDatabaseReference.child(group).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Activator resp;
                try {
                    resp = dataSnapshot.child(getString(R.string.activated_Firebase)).getValue(Activator.class);
                } catch (NullPointerException e) {
                    resp = null;
                }
                Log.d(TAG, "Response from group is " + resp);
                if (resp != null) {
//                        alertBuckets.add(temp);
//                        mobileAlerts.add(String.valueOf(resp));
                    mobile = resp.getMobile();
                    if (!isCreator) {
                        latitude = resp.getLatitude();
                        longitude = resp.getLongitude();
                    }
                    activatorName = resp.getName();
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
            Variables.alourtDatabaseReference.child(group).removeEventListener(listener);
            listener = null;
            sph.saveGroup(null);
        }
    }

    private void startAlertPage() {
        Log.d(TAG, "Initiating alert box");
        notifyIntent.putExtra(getString(R.string.isCreator_IntentPackage), isCreator);
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
}
