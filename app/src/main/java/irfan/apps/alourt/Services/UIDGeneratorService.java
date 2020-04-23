package irfan.apps.alourt.Services;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import irfan.apps.alourt.R;


public class UIDGeneratorService {

    private final String TAG = "UIDGeneratorService";
    private final int MAX_RANGE = 999999;
    private String randomUID;
    private Activity activity;


    public UIDGeneratorService(Activity activity) {
        this.activity = activity;
        this.randomUID = "";
    }

    public String generateRandom() {

        DatabaseReference staticDBR = FirebaseDatabase.getInstance().getReference();
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR) - 2000;
        int month = cal.get(Calendar.MONTH) + 1;
        Random r = new Random();
        randomUID = String.valueOf(r.nextInt(MAX_RANGE));
        staticDBR.child(activity.getString(R.string.groups_Firebase)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (randomUID.equals(ds.getKey())) {
                        Log.d(TAG, "Group found, creating a new UID");
                        randomUID = generateRandom();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return randomUID;
    }
}

