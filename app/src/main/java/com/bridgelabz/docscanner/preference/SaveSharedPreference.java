package com.bridgelabz.docscanner.preference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Nadimuddin on 18/10/16.
 */

public class SaveSharedPreference
{
    //key for shared preference
    private static final String PREFERENCE = "Cam Scanner";

    static Context mContext;

    static SharedPreferences preferences;

    //constructor
    public SaveSharedPreference(Context context)
    {
        mContext = context;

        //get shared preference
        preferences=context.getSharedPreferences(PREFERENCE,mContext.MODE_PRIVATE);
    }

    //get String preference value
    public String getPreference(String key)
    {
        return preferences.getString(key, null);
    }

    /*get int preference value
    public int getIntPreferences(String key)
    {
        return preferences.getInt(key, 0);
    }*/

    public void setPreferences(String key, String stringToSet)
    {
        //initialize editor to edit preference value
        SharedPreferences.Editor editor = preferences.edit();

        //set preference value for the key
        editor.putString(key, stringToSet);

        //apply changes to value
        editor.apply();
    }

    public void setPreferences(String key, int valueToSet)
    {
        //initialize editor to edit preference value
        SharedPreferences.Editor editor = preferences.edit();

        //set preference value for the key
        editor.putInt(key, valueToSet);

        //apply changes to value
        editor.apply();
    }
}
