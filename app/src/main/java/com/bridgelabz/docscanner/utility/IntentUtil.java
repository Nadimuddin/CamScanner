package com.bridgelabz.docscanner.utility;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bridgeit on 24/11/16.
 */

public class IntentUtil
{

    public static Uri getImageIntent(Activity act)
    {
        Intent intent = act.getIntent();
        if (intent == null) return null;
        Bundle bundle = intent.getExtras(); if (bundle == null) return null;
        if (bundle.containsKey(Intent.EXTRA_STREAM))
            return (Uri)bundle.get(Intent.EXTRA_STREAM);
        return null;
    }

    // // This method shows only the Gallery
    public static void selectPicture(Activity act, int reqCode)
    {
        String action = Intent.ACTION_PICK;
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Intent intent = new Intent(action, uri);
        intent.setType("image/*");
        act.startActivityForResult(intent, reqCode);
    }

    public static void processIntent(Activity act, Class<?> cls)
    {
        Intent intent = new Intent(act, cls);
        // start the new activity
        act.startActivity(intent);
    }

    public static void processIntent(Activity act, Class<?> cls, Uri imageUri)
    {
        Map<String, Object> extras = new HashMap<>();
        extras.put(Intent.EXTRA_STREAM, imageUri);
        processIntent(act, cls, extras);
    }

    public static void processIntent(Activity act, Class<?> cls, Map<String, Object> extras)
    {
        Intent intent = new Intent(act, cls);
        String[] keys = extras.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++)
        {
            if (keys[i].equals(Intent.EXTRA_STREAM)) {
                Uri imageUri = (Uri) extras.get(keys[i]);
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            }
        }

        intent.putExtra("from", "MainActivity");
        Log.i("IntentUtil", "processIntent: "+act.getComponentName().toString());

        // start the new activity
        act.startActivity(intent);
    }
}
