package irfan.apps.alourt.Handlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import irfan.apps.alourt.R;

public class SharedPrefsHandler {

    private SharedPreferences sharedPreferences;
    private Context context;

    public SharedPrefsHandler(Context context) {
        this.context = context;
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

    public void saveName(String name) {
        SharedPreferences.Editor myEdit
                = sharedPreferences.edit();
        myEdit.putString(
                "name",
                name);
        myEdit.apply();
    }

    public void saveUID(String UID) {
        SharedPreferences.Editor myEdit
                = sharedPreferences.edit();
        myEdit.putString(
                "UID",
                UID);
        myEdit.apply();
    }

    public String loadName() {
        return sharedPreferences.getString(context.getString(R.string.username_sharedpreferences), "");
    }
    public long loadMobile() {
        return sharedPreferences.getLong(context.getString(R.string.mobile_sharedpreferences), 0);
    }

    public String loadUID() {
        return sharedPreferences.getString(context.getString(R.string.UID_sharedpreferences), "");
    }

    public void addBucketID(String bucket) {
        ArrayList<String> buckets = retrieveBuckets();
        buckets.add(bucket);
        SharedPreferences.Editor myEdit
                = sharedPreferences.edit();
        Set<String> set = new HashSet<String>();
        set.addAll(buckets);
        myEdit.putStringSet(context.getString(R.string.userGroupSet_sharedpreferences), set);
        myEdit.apply();
        Log.d("storesharedPreferences", "" + set);

    }

    public ArrayList<String> retrieveBuckets() {
        Set<String> set = sharedPreferences.getStringSet(context.getString(R.string.userGroupSet_sharedpreferences), null);
        ArrayList<String> buckets = new ArrayList<>();
        if (set != null) {
            buckets.addAll(set);
        }
        Log.d("retrive", "" + set);
        return buckets;
    }

}
