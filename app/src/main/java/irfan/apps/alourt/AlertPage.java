package irfan.apps.alourt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import irfan.apps.alourt.Handlers.SharedPrefsHandler;
import irfan.apps.alourt.Services.AccessibilityKeyDetector;

public class AlertPage extends AppCompatActivity {

    //TODO create an SMS service that sends the bucket(s) the user is attached to as a body. Which is then used later on by Alourt's server. Sending credentials could be vulnerable.
    //TODO add in latitude, longitude and name displays.
    AudioManager audioM;
    CameraManager mCameraManager;
    TextView dispTxt;
    TextView nametxt;
    TextView latidudeDisp;
    TextView longitudeDisp;
    MediaPlayer mp;
    android.hardware.Camera mCamera;
    android.hardware.Camera.Parameters parameters;
    private final String TAG = "AlertPage";
    private boolean toggleSwitch = true;
    SharedPrefsHandler sph;
    boolean isAdminOrCreator;

    private LocationManager locationManager = null;
//    private LocationListener locationListener=null;

    String name;
    String latitude;
    String longitude;
    String group;
    long mobile;

//    ArrayList<String> buckets;
//    ArrayList<String> mobiles;
//    String bucket;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        getSupportActionBar().hide();
        //locationListener = new MyLocationListener();


        Intent in = getIntent();
        dispTxt = findViewById(R.id.alertText);
        nametxt = findViewById(R.id.nameAlertTxt);
        latidudeDisp = findViewById(R.id.latitudetxt);
        longitudeDisp = findViewById(R.id.longitudetxt);
//        buckets = new ArrayList<>();
//        mobiles = new ArrayList<>();
        group = in.getStringExtra(getString(R.string.group_name_IntentPackage));
        mobile = in.getLongExtra(getString(R.string.mobile_IntentPackage), 0);
        latitude = in.getStringExtra("latitude");
        longitude = in.getStringExtra("longitude");
        name = in.getStringExtra("activator_name");
        isAdminOrCreator = in.getBooleanExtra(getString(R.string.isCreator_IntentPackage), false);
        dispTxt.setText(mobile + " from " + group + " needs help");
        nametxt.setText("Name: " + name);
        latidudeDisp.setText("Last known latitude: " + latitude);
        longitudeDisp.setText("Last known longitude: " + longitude);
        sph = new SharedPrefsHandler(getApplicationContext());
        Log.d(TAG, "Activity launched");
        audioM = (AudioManager) getSystemService(AUDIO_SERVICE);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                startNotificationCycle();
            }
        });
        thread.start();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
    }


    private void audioOn() {

        audioM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        int volume = audioM.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        audioM.setStreamVolume(AudioManager.STREAM_ALARM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        mp = new MediaPlayer();
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        try {
            mp.setDataSource(getApplicationContext(), ringtoneUri);
            mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAlertBroadcast() {
        // Write a message to the database
        //ArrayList<String> buckets = sph.retrieveBuckets();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(getString(R.string.groups_Firebase));

        //for (String locBucket : buckets) {
        //    bucket = locBucket;
//            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if(isAdminOrCreator){
//
//                    }
//                    try {
//                        isAdminOrCreator = dataSnapshot.child(group).child(getString(R.string.members_Firebase)).child(sph.loadUID()).child(getString(R.string.adminStatus_Firebase)).getValue(Boolean.class);
//                    }catch (NullPointerException e){
//                        Log.d(TAG,"Data not found");
//                    }
//                        Log.d(TAG,"Is current user admin/creator for "+group+ "is "+ isAdminOrCreator);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
        if (isAdminOrCreator && !group.isEmpty()) {
            myRef.child(group).child(getString(R.string.activated_Firebase)).setValue(null);
            }
        //    }
        finish();
    }


    private void audioOff() {
        if (mp != null) {
            try {
                mp.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void vibrateOn() {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    private void flashOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
                String cameraId;
                if (mCameraManager != null) {
                    cameraId = mCameraManager.getCameraIdList()[0];
                    mCameraManager.setTorchMode(cameraId, true);
                }
            } catch (CameraAccessException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            mCamera = android.hardware.Camera.open();
            parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String cameraId;
                if (mCameraManager != null) {
                    cameraId = mCameraManager.getCameraIdList()[0]; // Usually front camera is at 0 position.
                    mCameraManager.setTorchMode(cameraId, false);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            mCamera = Camera.open();
            parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            mCamera.stopPreview();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startNotificationCycle() {
        toggleSwitch = true;

        // When notification is triggered, stop others from triggering it.
        stopService(new Intent(this, AccessibilityKeyDetector.class));


        boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (isFlashAvailable) {
            audioOn();
            while (toggleSwitch) {
                vibrateOn();
                flashOn();
            }
        }
    }

    public void endNotificationCycle(View v) {

        //startService(new Intent(this, AccessibilityKeyDetector.class));
        toggleSwitch = false;
        audioOff();
        stopAlertBroadcast();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //startService(new Intent(this, AccessibilityKeyDetector.class));
        toggleSwitch = false;
        audioOff();
        stopAlertBroadcast();
    }


}


