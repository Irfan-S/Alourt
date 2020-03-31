package irfan.apps.alourt.Services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.security.Policy;

public class AccessibilityKeyDetector extends AccessibilityService {

    AudioManager audioM ;
    CameraManager mCameraManager;
    android.hardware.Camera mCamera;
    android.hardware.Camera.Parameters parameters;
    private final String TAG = "AccessKeyDetector";
    private int counter =0;

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        audioM = (AudioManager)getSystemService(AUDIO_SERVICE);
        Log.d(TAG,"Key pressed via accessibility is: "+event.getKeyCode()+" and counter is: "+counter);


        //To trigger, press vol up, vol down and vol up again. Need a wait system that will wait for input within the next 2 seconds, or it will just reset

        if(counter%2==0 && event.getKeyCode()==24){
            counter+=1;
            Log.d(TAG,"Counter after 24 is: "+counter);
            new CountDownTimer(2000,1000){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Log.d(TAG,"Resetting due to wait trigger");
                    counter = 0;
                }
            }.start();
        }
        else if(counter%2==1 && event.getKeyCode()==25){
            counter+=2;
            Log.d(TAG,"Counter after 25 is: "+counter);
        }
        else if(counter==5) {
            Log.d(TAG,"Triggering ringtone...");
            startNotificationCycle();
            counter=0;
        }else if(counter>1){
            Log.d(TAG,"Resetting due to invalid trigger");
            counter= 0;
        }
        return super.onKeyEvent(event);
    }

    private void turnAudioOn(){
        audioM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        int volume = audioM.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        audioM.setStreamVolume(AudioManager.STREAM_ALARM,volume,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        MediaPlayer mp=new MediaPlayer();
        Uri ringtoneUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        try
        {
            mp.setDataSource(getApplicationContext(), ringtoneUri);
            mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mp.prepare();
            mp.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void vibrateOn(){
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    private void startNotificationCycle(){
        boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if(isFlashAvailable) {
            turnAudioOn();
            while(true) {
                vibrateOn();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
                        String cameraId = null;
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
        }
    }


    @Override
    protected void onServiceConnected() {
        Log.i(TAG,"Service connected");

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }


    @Override
    public void onInterrupt() {

    }
}
