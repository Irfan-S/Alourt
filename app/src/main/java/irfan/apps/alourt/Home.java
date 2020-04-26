package irfan.apps.alourt;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import irfan.apps.alourt.Handlers.MemberAdapter;
import irfan.apps.alourt.Handlers.SharedPrefsHandler;
import irfan.apps.alourt.Handlers.User;
import irfan.apps.alourt.Services.AccessibilityKeyDetector;
import irfan.apps.alourt.Services.UIDGeneratorService;

//TODO on leaving a group, create a way to remove all listeners before starting a new one.

public class Home extends AppCompatActivity implements View.OnClickListener {

    EditText inviteId;
    Button submitInviteIdButton;

    public static boolean clean = false;

    List<User> userList;

    Button createGroupButton;
    Button leaveGroupButton;
    TextView disp;
    String name;
    String group;
    long mobile;

    TextView tumbleTxt;

    FirebaseUser user;
    private RecyclerView membersRecycler;
    private MemberAdapter memberAdapter;
    String UID;

    LocationManager locationManager;
    boolean GpsStatus;

    ImageView img;
    Animation aniRotate;

    SharedPrefsHandler sph;
    DatabaseReference dbr;


    private final String TAG = "AlourtHome";
    private final boolean ADMIN_USER = true;
    private final boolean NOT_ADMIN_USER = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();
        sph = new SharedPrefsHandler(getApplicationContext());

        disp = findViewById(R.id.key_disp);
        group = sph.loadGroup();
        user = FirebaseAuth.getInstance().getCurrentUser();
        dbr = FirebaseDatabase.getInstance().getReference(getString(R.string.groups_Firebase));
        img = (ImageView) findViewById(R.id.tumbleweed_anim_image);
        tumbleTxt = findViewById(R.id.tumbleweed_txt);


        if (userList == null) {
            userList = new ArrayList<>();
        }
        memberAdapter = new MemberAdapter(userList);


        membersRecycler = findViewById(R.id.memberDisplayRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        membersRecycler.setLayoutManager(mLayoutManager);
        membersRecycler.setItemAnimator(new DefaultItemAnimator());
        membersRecycler.setAdapter(memberAdapter);
        // updateAnimation();
//        if (!group.isEmpty()) {
//            img.setVisibility(View.GONE);
//            tumbleTxt.setVisibility(View.GONE);
//            disp.setText("Group: " + group);
//            updateMemberDisplay();
//        } else {
//            disp.setText("No groups assigned");
//            img.setVisibility(View.VISIBLE);
//            membersRecycler.setVisibility(View.GONE);
//        }

        inviteId = findViewById(R.id.inviteId);
        submitInviteIdButton = findViewById(R.id.inviteIdSubmitButton);
        createGroupButton = findViewById(R.id.newGroupButton);
        leaveGroupButton = findViewById(R.id.leaveGroupButton);


        submitInviteIdButton.setOnClickListener(this);
        createGroupButton.setOnClickListener(this);
        leaveGroupButton.setOnClickListener(this);


        UID = user.getUid();
        name = sph.loadName();
        mobile = sph.loadMobile();


    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAnimation();
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

    public void updateAnimation() {
        group = sph.loadGroup();
        if (group != null & !group.isEmpty()) {
            Log.d(TAG, "Hiding animation");
            if (aniRotate != null) {
                img.clearAnimation();
            }
            tumbleTxt.setVisibility(View.GONE);
            img.setVisibility(View.GONE);
            disp.setText("Group: " + group);
            membersRecycler.setVisibility(View.VISIBLE);
            updateMemberDisplay();
        } else {
            Log.d(TAG, "Showing animation");
            membersRecycler.setVisibility(View.GONE);
            disp.setText("No groups assigned");
            img.setVisibility(View.VISIBLE);
            tumbleTxt.setVisibility(View.VISIBLE);
            aniRotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.tumbleweed);
            img.startAnimation(aniRotate);

        }
    }

    public void updateMemberDisplay() {
        //Check if network present and if user is in group
        if (true) {

            dbr.child(group).child(getString(R.string.members_Firebase)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        //Log.d(TAG,ds.getValue(User.class).getUserName());
                        User nUser = ds.getValue(User.class);
                        userList.add(nUser);
                    }
                    Log.d(TAG, "Users are: " + userList.size());
                    memberAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

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
                                        updateAnimation();
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
                            dbr.child(GroupID).child(getString(R.string.activated_Firebase)).setValue(0);
                            updateAnimation();
                        }
                    });

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
                            sph.saveGroup("");
                            updateAnimation();
                        }
                    });
                }

        }


    }

}

