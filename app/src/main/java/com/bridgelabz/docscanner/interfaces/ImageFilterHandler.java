package com.bridgelabz.docscanner.interfaces;

import android.graphics.Bitmap;

import com.bridgelabz.docscanner.utility.XONImageFilterDef;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by bridgeit on 25/11/16.
 */

public interface ImageFilterHandler extends Serializable {

    // Returns the HashMap associated with the ImageFilterHandler
    public Map<String, String> getFieldParam(String fldName);

    // Returns the Action Component associated with the ImageFilterHandler
    public String getAction();

    // Returns the type associated with the Field
    public String getFieldType(String fldName);

    // Returns the value associated with the Field. Here the param fld is default to
    // Amt and ext to x.
    public Map<String, String> setFieldValue(Map<String, String> imgFltrValMaps,
                                             String fldName, String val, boolean reFactor);

    // Returns the value associated with the Field and the Param Field
    public Map<String, String> setFieldValue(Map<String, String> imgFltrValMaps,
                                             String fldName, String paramFld,
                                             String fldExt, String val, boolean reFactor);

    // This enum specifies the set and unset options for ImageFilterHandler.
    public static enum SetImageFilter { ON, OFF, ACTIVATED }

    public SetImageFilter setImageFilterParam(String action, String val);
    public SetImageFilter setImageFilterParam(XONImageFilterDef.ImageFilter imgFltr, String val);

    // Apply Filter
    public Bitmap applyFilter(Bitmap src, boolean useNewThread);
    public int[] applyFilter(int[] pixels, int wt, int ht, int[] inPixels, int[] outPixels);
}

