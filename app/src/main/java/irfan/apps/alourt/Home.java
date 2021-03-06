/*
 * Copyright (c) 2020 Irfan S.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Syed Irfan Ahmed <irfansa1@ymail.com>
 */

package irfan.apps.alourt;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import irfan.apps.alourt.Handlers.MemberAdapter;
import irfan.apps.alourt.Handlers.PermissionHandler;
import irfan.apps.alourt.Handlers.SharedPrefsHandler;
import irfan.apps.alourt.Services.AccessibilityKeyDetector;
import irfan.apps.alourt.Services.UIDGeneratorService;
import irfan.apps.alourt.Utils.DialogUtil;
import irfan.apps.alourt.Utils.User;
import irfan.apps.alourt.Utils.Variables;

//TODO on leaving a group, create a way to remove all listeners before starting a new one.


public class Home extends AppCompatActivity implements View.OnClickListener {

    EditText inviteId;
    Button submitInviteIdButton;

    List<User> userList;

    Button createGroupButton;
    Button leaveGroupButton;
    TextView disp;
    String name;
    String group;
    long mobile;

    TextView tumbleTxt;

    private RecyclerView membersRecycler;
    private MemberAdapter memberAdapter;
    String UID;
    PermissionHandler permissionHandler;

    ImageView img;
    Animation aniRotate;

    SharedPrefsHandler sph;


    private final String TAG = "AlourtHome";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Objects.requireNonNull(getSupportActionBar()).hide();
        sph = new SharedPrefsHandler(getApplicationContext());
        isNetworkConnected();
        disp = findViewById(R.id.key_disp);
        group = sph.loadGroup();

        if (sph.isNewUser()) {
            DialogUtil dialogUtil = new DialogUtil(this);
            dialogUtil.setCancelable(true);
            dialogUtil.show();
            sph.saveNewUser();
        }


        if (Variables.alourtUser == null) {
            Variables.alourtUser = FirebaseAuth.getInstance().getCurrentUser();
        }
        if (Variables.alourtDatabaseReference == null) {
            Variables.alourtDatabaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.groups_Firebase));
        }
        img = findViewById(R.id.tumbleweed_anim_image);
        tumbleTxt = findViewById(R.id.tumbleweed_txt);



        if (userList == null) {
            userList = new ArrayList<>();
        }
        memberAdapter = new MemberAdapter(userList);

        permissionHandler = new PermissionHandler(this);
        membersRecycler = findViewById(R.id.memberDisplayRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        membersRecycler.setLayoutManager(mLayoutManager);
        membersRecycler.setItemAnimator(new DefaultItemAnimator());
        membersRecycler.setAdapter(memberAdapter);

        inviteId = findViewById(R.id.inviteId);
        submitInviteIdButton = findViewById(R.id.inviteIdSubmitButton);
        createGroupButton = findViewById(R.id.newGroupButton);
        leaveGroupButton = findViewById(R.id.leaveGroupButton);


        submitInviteIdButton.setOnClickListener(this);
        createGroupButton.setOnClickListener(this);
        leaveGroupButton.setOnClickListener(this);


        UID = Variables.alourtUser.getUid();
        name = sph.loadName();
        mobile = sph.loadMobile();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!permissionHandler.isAccessibilityEnabled()) {
            permissionHandler.setCancelable(false);
            permissionHandler.show();
        }
        isNetworkConnected();
        updateAnimation();
    }

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
        if (group != null) {

            Variables.alourtDatabaseReference.child(group).child(getString(R.string.members_Firebase)).addListenerForSingleValueEvent(new ValueEventListener() {
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
        } else {
            //Toast.makeText(this,"No network detected",Toast.LENGTH_LONG).show();
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
                if (sph.loadGroup().isEmpty()) {
                    final String invID = inviteId.getText().toString();
                    mobile = sph.loadMobile();
                    Log.d(TAG, "Clicked " + UID + " and mob: " + mobile);
                    if (UID != null && mobile != 0 && name != null && !invID.isEmpty()) {
                        Log.d(TAG, "Started invite helper");
                        boolean NOT_ADMIN_USER = false;
                        final User nUser = new User(name, mobile, NOT_ADMIN_USER);

                        Variables.alourtDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(invID)) {
                                    Variables.alourtDatabaseReference.child(invID).child(getString(R.string.members_Firebase)).child(UID).setValue(nUser).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                } else {
                    Toast.makeText(getApplicationContext(), "Please leave your existing group before joining a new one", Toast.LENGTH_SHORT).show();
                }
                break;

            /*
              Creating a new group, uses UIDGeneratorService to create a new, unique UID for the group.
             */
            case R.id.newGroupButton:
                if (sph.loadGroup().isEmpty()) {
                    UIDGeneratorService uid = new UIDGeneratorService(this);
                    final String GroupID = uid.generateRandom();
                    Log.d(TAG, "New group initiated " + UID + " " + mobile + " " + name);
                    if (UID != null && mobile != 0 && name != null) {
                        boolean ADMIN_USER = true;
                        User nUser = new User(name, mobile, ADMIN_USER);
                        Variables.alourtDatabaseReference.child(GroupID).child(getString(R.string.members_Firebase)).child(UID).setValue(nUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                sph.saveGroup(GroupID);
                                disp.setText("Your group is " + GroupID);
                                startService(new Intent(getApplicationContext(), AccessibilityKeyDetector.class));
                                Toast.makeText(getApplicationContext(), "Group created successfully", Toast.LENGTH_LONG).show();
                                //dbr.child(GroupID).child(getString(R.string.activated_Firebase)).setValue(0);
                                updateAnimation();
                            }
                        });

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please leave your existing group before creating a new one", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.leaveGroupButton:
                final String groupID = sph.loadGroup();
                if (!groupID.isEmpty()) {
                    Variables.alourtDatabaseReference.child(groupID).child(getString(R.string.members_Firebase)).child(UID).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Variables.shouldCleanListener = true;
                            disp.setText("No group assigned");
                            startService(new Intent(getApplicationContext(), AccessibilityKeyDetector.class));
                            Toast.makeText(getApplicationContext(), "Successfully left group", Toast.LENGTH_LONG).show();
                            sph.saveGroup("");
                            updateAnimation();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "You aren't a part of any group", Toast.LENGTH_SHORT).show();
                }

        }


    }


    // TODO doesnt work
    public void isNetworkConnected() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                                                                       @Override
                                                                       public void onAvailable(Network network) {
                                                                           Variables.isNetworkConnected = true; // Global Static Variable
                                                                       }

                                                                       @Override
                                                                       public void onLost(Network network) {
                                                                           Variables.isNetworkConnected = false; // Global Static Variable
                                                                       }
                                                                   }

                );
            } else {
                ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
                Variables.isNetworkConnected = (cm.getActiveNetworkInfo() != null);
            }
            Variables.isNetworkConnected = false;
        } catch (Exception e) {
            Variables.isNetworkConnected = false;
        }
    }


}

