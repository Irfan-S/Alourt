package irfan.apps.alourt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import irfan.apps.alourt.Handlers.SharedPrefsHandler;
import irfan.apps.alourt.Handlers.User;
import irfan.apps.alourt.Services.AccessibilityKeyDetector;
import irfan.apps.alourt.Services.UIDGeneratorService;

//TODO create online and offline mode versions, and link it to the sms handler.
//TODO on leaving a group, create a way to remove all listeners before starting a new one.

public class Home extends AppCompatActivity implements View.OnClickListener {

    EditText inviteId;
    Button submitInviteIdButton;

    public static boolean clean = false;

    Button createGroupButton;
    Button leaveGroupButton;
    TextView disp;
    String name;
    long mobile;
    FirebaseUser user;
    String UID;

    SharedPrefsHandler sph;
    DatabaseReference dbr;

    private final String TAG = "AlourtHome";
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

        leaveGroupButton = findViewById(R.id.leaveGroupButton);
        submitInviteIdButton.setOnClickListener(this);
        createGroupButton.setOnClickListener(this);
        leaveGroupButton.setOnClickListener(this);

        sph = new SharedPrefsHandler(getApplicationContext());

        user = FirebaseAuth.getInstance().getCurrentUser();
        dbr = FirebaseDatabase.getInstance().getReference(getString(R.string.groups_Firebase));

        UID = user.getUid();
        name = sph.loadName();
        mobile = sph.loadMobile();
        String group = sph.loadGroup();
        if (!group.isEmpty()) {
            disp.setText("Your group is " + group);
        } else {
            disp.setText("No groups assigned");
        }


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

//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d(TAG, "Key pressed");
//        disp.setText("Key pressed is" + keyCode);
//        return true;
//
//    }

    @Override
    public void onClick(View v) {
        //TODO Add a check to prevent NAN and Null inputs
        switch (v.getId()) {
            /**
             *  Joining a group, using a specified invite ID.
             */
            case R.id.inviteIdSubmitButton:
                final String invID = inviteId.getText().toString();
                mobile = sph.loadMobile();
                Log.d(TAG, "Clicked " + UID + " and mob: " + mobile);
                if (UID != null && mobile != 0 && name != null && !invID.isEmpty()) {
                    Log.d(TAG, "Started invite helper");
                    final User nUser = new User(name, mobile, NOT_ADMIN_USER);

                    dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(invID)) {
                                dbr.child(invID).child(getString(R.string.members_Firebase)).child(UID).setValue(nUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sph.saveGroup(invID);
                                        disp.setText("Your group is " + invID);
                                        startService(new Intent(getApplicationContext(), AccessibilityKeyDetector.class));
                                        Toast.makeText(getApplicationContext(), "Successfully joined group", Toast.LENGTH_LONG).show();
                                    }

                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid ID", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                break;

            /**
             * Creating a new group, uses UIDGeneratorService to create a new, unique UID for the group.
             */
            case R.id.newGroupButton:
                UIDGeneratorService uid = new UIDGeneratorService(this);
                final String GroupID = uid.generateRandom();
                Log.d(TAG, "New group initiated " + UID + " " + mobile + " " + name);
                if (UID != null && mobile != 0 && name != null) {
                    User nUser = new User(name, mobile, ADMIN_USER);
                    dbr.child(GroupID).child(getString(R.string.members_Firebase)).child(UID).setValue(nUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sph.saveGroup(GroupID);
                            disp.setText("Your group is " + GroupID);
                            startService(new Intent(getApplicationContext(), AccessibilityKeyDetector.class));
                            Toast.makeText(getApplicationContext(), "Group created successfully", Toast.LENGTH_LONG).show();
                        }
                    });
                    dbr.child(GroupID).child(getString(R.string.activated_Firebase)).setValue(0);
                }
                break;

            case R.id.leaveGroupButton:
                final String groupID = sph.loadGroup();
                if (!groupID.isEmpty()) {
                    dbr.child(groupID).child(getString(R.string.members_Firebase)).child(UID).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            clean = true;
                            disp.setText("No group assigned");
                            startService(new Intent(getApplicationContext(), AccessibilityKeyDetector.class));
                            Toast.makeText(getApplicationContext(), "Successfully left group", Toast.LENGTH_LONG).show();
                        }
                    });
                }

        }


    }
}

