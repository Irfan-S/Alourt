package irfan.apps.alourt.Handlers;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseHandler {

    private FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int response = 0;

    public DatabaseHandler(String bucketId) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Buckets").child(bucketId);
    }

    public void sendAlert() {
        databaseReference.child("notifyUsers").setValue(1);
    }

    public void cancelAlert() {
        databaseReference.child("notifyUsers").setValue(0);
    }

    public int getAlertStatus() {
        databaseReference.child("notifyUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                response = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return response;
    }


}
