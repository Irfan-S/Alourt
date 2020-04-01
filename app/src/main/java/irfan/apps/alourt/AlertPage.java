package irfan.apps.alourt;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlertPage extends AppCompatActivity {
    AudioManager audioM;
    CameraManager mCameraManager;
    public static final String NOTIFICATION_CHANNEL_ID = "1201";
    MediaPlayer mp;
    android.hardware.Camera mCamera;
    android.hardware.Camera.Parameters parameters;
    private final String TAG = "AlertPage";
    private boolean toggleSwitch = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        Log.d(TAG, "Activity launched");
        audioM = (AudioManager) getSystemService(AUDIO_SERVICE);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                startNotificationCycle();
            }
        });
        thread.start();
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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.setValue(0);
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
        toggleSwitch = false;
        audioOff();
        stopAlertBroadcast();
    }

}
