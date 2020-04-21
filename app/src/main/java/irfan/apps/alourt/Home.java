package irfan.apps.alourt;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import irfan.apps.alourt.Handlers.SharedPrefsHandler;
import irfan.apps.alourt.Handlers.User;
import irfan.apps.alourt.Services.UIDGeneratorService;

public class Home extends AppCompatActivity implements View.OnClickListener {

    EditText inviteId;
    Button submitInviteIdButton;
    //Button submitNameButton;
    //EditText nameEdit;
    Button createGroupButton;
    TextView disp;
    String name;
    long mobile;
    FirebaseUser user;
    String UID;

    SharedPrefsHandler sph;

    private final String TAG = "Test";
    private final boolean ADMIN_USER = true;
    private final boolean NOT_ADMIN_USER = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        disp = findViewById(R.id.key_disp);
        inviteId = findViewById(R.id.inviteId);
        submitInviteIdButton = findViewById(R.id.inviteIdSubmitButton);
        createGroupButton = findViewById(R.id.newGroupButton);

        submitInviteIdButton.setOnClickListener(this);
        createGroupButton.setOnClickListener(this);

        sph = new SharedPrefsHandler(getApplicationContext());

        user = FirebaseAuth.getInstance().getCurrentUser();
        UID = user.getUid();
        name = sph.loadName();
        mobile = sph.loadMobile();


    }

//    private void checkAudioPermission() {
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//        // Check if the notification policy access has been granted for the app.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
//                setContentView(R.layout.enable_permissions_display);
//                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                Toast.makeText(this, "Please allow Alourt to modify DND settings, for emergency notifications", Toast.LENGTH_LONG).show();
//                startActivity(intent);
//            }
//        }
//    }
//
//    public void checkAccessibilityPermission() {
//        int accessEnabled = 0;
//        try {
//            accessEnabled = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
//        } catch (Settings.SettingNotFoundException e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG, "Accessibility granted: " + accessEnabled);
//        if (accessEnabled == 0) {
//            setContentView(R.layout.enable_permissions_display);
//            /** if not construct intent to request permission */
//            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//            /** request permission via start activity for result */
//            Toast.makeText(this, "Please allow Alourt to have accessibility, for app activation using volume buttons", Toast.LENGTH_LONG).show();
//            startActivity(intent);
//
//        } else {
//            setContentView(R.layout.activity_home);
//        }
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "Key pressed");
        disp.setText("Key pressed is" + keyCode);
        return true;

    }

    @Override
    public void onClick(View v) {
        //TODO Add a check to prevent NAN and Null inputs
        switch (v.getId()) {
            case R.id.inviteIdSubmitButton:
                final String invID = inviteId.getText().toString();
                mobile = sph.loadMobile();
                Log.d(TAG, "Clicked " + UID + " and mob: " + mobile);
                //TODO add sharedprefs to store user details.
                if (UID != null && mobile != 0 && name != null) {
                    Log.d(TAG, "Started invite helper");
                    User nUser = new User(name, mobile, NOT_ADMIN_USER);
                    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("groups");
                    dbr.child(invID).child("members").child(UID).setValue(nUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sph.addBucketID(invID);
                        }
                    });
                    Toast.makeText(this, "Successfully joined group", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.newGroupButton:
                UIDGeneratorService uid = new UIDGeneratorService(this);
                final String GroupID = uid.generateRandom();
                Log.d(TAG, "New group initiated " + UID + " " + mobile + " " + name);
                if (UID != null && mobile != 0 && name != null) {
                    User nUser = new User(name, mobile, ADMIN_USER);
                    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("groups");
                    dbr.child(GroupID).child("members").child(UID).setValue(nUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sph.addBucketID(GroupID);
                        }
                    });
                    dbr.child(GroupID).child("activated").setValue(0);
                }
                break;


        }


    }
}

