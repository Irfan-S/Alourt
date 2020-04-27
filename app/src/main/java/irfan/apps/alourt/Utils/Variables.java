package irfan.apps.alourt.Utils;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class Variables {

    public static DatabaseReference alourtDatabaseReference;
    public static boolean shouldCleanListener;
    public static FirebaseAuth alourtAuth;
    public static FirebaseUser alourtUser;
    private Context context;
    public static boolean isNetworkConnected = false;

    public Variables(Context context) {
        this.context = context;
    }


}
