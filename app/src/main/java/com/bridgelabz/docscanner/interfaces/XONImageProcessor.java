package com.bridgelabz.docscanner.interfaces;

import android.graphics.Bitmap;

import java.util.Map;

/**
 * Created by bridgeit on 26/11/16.
 */

public interface XONImageProcessor
{
    Bitmap processImage(Map<String, Object> data);
}
