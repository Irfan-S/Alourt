package irfan.apps.alourt;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {


    //TODO terminate the notif flasher on button press, could create a notifcation that pops up, with a timer of 5 secs, that will trigger if not stopped?
    TextView disp;
    private final String TAG = "Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAccessibilityPermission();
        setContentView(R.layout.activity_home);
        disp = findViewById(R.id.key_disp);
        checkAudioPermission();


    }

    private void checkAudioPermission() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Check if the notification policy access has been granted for the app.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                setContentView(R.layout.enable_permissions_display);
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                Toast.makeText(this, "Please allow Alourt to modify DND settings, for emergency notifications", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        }
    }

    public void checkAccessibilityPermission() {
        int accessEnabled = 0;
        try {
            accessEnabled = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Accessibility granted: " + accessEnabled);
        if (accessEnabled == 0) {
            setContentView(R.layout.enable_permissions_display);
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            /** request permission via start activity for result */
            Toast.makeText(this, "Please allow Alourt to have accessibility, for app activation using volume buttons", Toast.LENGTH_LONG).show();
            startActivity(intent);

        } else {

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "Key pressed");
        disp.setText("Key pressed is" + keyCode);
        return true;

    }
}

