package irfan.apps.alourt.Handlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SharedPrefsHandler {

    private SharedPreferences sharedPreferences;

    public SharedPrefsHandler(Context context) {
        sharedPreferences = context.getSharedPreferences("AlourtData",
                Context.MODE_PRIVATE);
    }

    public void saveMobile(long mobile) {
        SharedPreferences.Editor myEdit
                = sharedPreferences.edit();
        myEdit.putLong(
                "mobile",
                mobile);
        myEdit.apply();
    }
    // TODO save name

    public long loadMobile() {
        return sharedPreferences.getLong("mobile", 0);
    }

    //TODO determine how exactly to bucketize
    public void addBucketID(String bucket) {
        ArrayList<String> buckets = retrieveBuckets();
        buckets.add(bucket);
        SharedPreferences.Editor myEdit
                = sharedPreferences.edit();
        Set<String> set = new HashSet<String>();
        set.addAll(buckets);
        myEdit.putStringSet("bucket", set);
        myEdit.apply();
        Log.d("storesharedPreferences", "" + set);

    }

    public ArrayList<String> retrieveBuckets() {
        Set<String> set = sharedPreferences.getStringSet("bucket", null);
        ArrayList<String> buckets = new ArrayList<>();
        if (set != null) {
            buckets.addAll(set);
        }
        Log.d("retrive", "" + set);
        return buckets;

    }

}
