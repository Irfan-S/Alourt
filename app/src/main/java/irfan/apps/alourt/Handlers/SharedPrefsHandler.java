package irfan.apps.alourt.Handlers;

import android.content.Context;
import android.content.SharedPreferences;

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

    public long loadMobile() {
        return sharedPreferences.getLong("mobile", 0);
    }

    //TODO determine how exactly to bucketize
    public void addBucketID(String bucket) {
        String value = sharedPreferences.getString("bucket", "");
        String appendValue = value + ";" + bucket;
        SharedPreferences.Editor myEdit
                = sharedPreferences.edit();
        myEdit.putString(
                "bucket",
                appendValue);
        myEdit.apply();
    }

    //public ArrayList<String> retrieveBucketID(S)

}
