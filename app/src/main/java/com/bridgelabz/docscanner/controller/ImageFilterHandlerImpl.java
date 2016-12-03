package com.bridgelabz.docscanner.controller;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.bridgelabz.docscanner.interfaces.ImageFilterHandler;
import com.bridgelabz.docscanner.utility.XONImageFilterDef;
import com.bridgelabz.docscanner.utility.XONImageFilterDef.ImageFilter;

import java.util.Map;
import java.util.Vector;

/**
 * Created by bridgeit on 28/11/16.
 */

public class ImageFilterHandlerImpl implements ImageFilterHandler {

    public XONImageFilterDef.ImageFilter m_ImageFilter;
    public Vector<Map<String, String>> m_FilterParamValues = null;
    int m_Color = Color.BLACK;

    public ImageFilterHandlerImpl(String imgFltr)
    {
        m_ImageFilter = XONImageFilterDef.ImageFilter.valueOf(imgFltr);
        m_FilterParamValues = XONImageFilterDef.getImageFilterRes(m_ImageFilter.toString());
        if (ImageFilter.showColorPickerDialog(m_ImageFilter.toString()))
        {
            Map<String, String> imgFltrValMaps = getFieldParam("Color");
            m_Color = Color.parseColor(imgFltrValMaps.get("Value"));
        }
        setDefaultParamValues();
    }

    private void setDefaultParamValues() {}

    @Override
    public Map<String, String> getFieldParam(String fldName) {
        return null;
    }

    @Override
    public String getAction() {
        return null;
    }

    @Override
    public String getFieldType(String fldName) {
        return null;
    }

    @Override
    public Map<String, String> setFieldValue(Map<String, String> imgFltrValMaps, String fldName, String val, boolean reFactor) {
        return null;
    }

    @Override
    public Map<String, String> setFieldValue(Map<String, String> imgFltrValMaps, String fldName, String paramFld, String fldExt, String val, boolean reFactor) {
        return null;
    }

    @Override
    public SetImageFilter setImageFilterParam(String action, String val) {
        return null;
    }

    @Override
    public SetImageFilter setImageFilterParam(ImageFilter imgFltr, String val) {
        return null;
    }

    @Override
    public Bitmap applyFilter(Bitmap src, boolean useNewThread) {
        return null;
    }

    @Override
    public int[] applyFilter(int[] pixels, int wt, int ht, int[] inPixels, int[] outPixels) {
        return new int[0];
    }
}
