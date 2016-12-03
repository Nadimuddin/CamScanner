package com.bridgelabz.docscanner.controller;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;

import com.bridgelabz.docscanner.interfaces.ImageFilterHandler;
import com.bridgelabz.docscanner.utility.XONImageFilterDef;
import com.bridgelabz.docscanner.utility.XONImageFilterDef.ImageFilter;
import com.bridgelabz.docscanner.utility.XONImageUtil;
import com.bridgelabz.docscanner.utility.XONObjectCache;

import java.util.Map;
import java.util.Vector;

/**
 * Created by bridgeit on 25/11/16.
 */

public class TemplateFilterHandlerImpl implements ImageFilterHandler {
    public static final String TAG = "TemplateFilterHandlerImpl";
    public String m_ImageFilterName;
    public String m_TemplateSerFile;
    public int m_FilterPosId;

    public ImageFilter m_ImageFilter;
    public SetImageFilter m_ImageFilterStatus = SetImageFilter.OFF;
    public Vector<ImageFilterHandler> m_ImageFilterHdlrs;

    @Override
    public Map<String, String> getFieldParam(String fldName) {
        return null;
    }

    @Override
    public String getAction() {
        return XONImageFilterDef.getImageFilter(m_ImageFilter, m_ImageFilterName);
    }

    @Override
    public String getFieldType(String fldName) {
        return "";
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
        ImageFilter imgFltr = null; String[] filterParams = action.split("_");
        if (filterParams[1].equals("On"))
            imgFltr = ImageFilter.valueOf(m_ImageFilter+"_On");
        else imgFltr = ImageFilter.valueOf(m_ImageFilter+"_Off");
        return setImageFilterParam(imgFltr, val);
    }

    @Override
    public SetImageFilter setImageFilterParam(ImageFilter imgFltr, String val) {
        ImageFilterHandler imgFilterHdlr = null;
        String[] filterParams = imgFltr.toString().split("_");
        if (filterParams[1].equals("On")) m_ImageFilterStatus = SetImageFilter.ON;
        else if (filterParams[1].equals("Active"))
            m_ImageFilterStatus = SetImageFilter.ACTIVATED;
        else m_ImageFilterStatus = SetImageFilter.OFF;
        for (int i = 0; i < m_ImageFilterHdlrs.size(); i++)
        {
            imgFilterHdlr = m_ImageFilterHdlrs.elementAt(i);
            imgFilterHdlr.setImageFilterParam(imgFltr, val);
        }
        return m_ImageFilterStatus;
    }

    private Bitmap applyFilter(ImageFilter imgFltr, Bitmap srcImg, boolean useNewThread)
    {
        if (m_ImageFilterStatus == SetImageFilter.OFF) return srcImg;
        int wt = srcImg.getWidth(), ht = srcImg.getHeight();
        int[] pixels = XONImageUtil.getBitmapPixels(srcImg);
        return XONImageUtil.createBitmap(applyFilter(imgFltr, pixels, wt, ht, null, null), wt, ht);
    }

    @Override
    public Bitmap applyFilter(Bitmap srcImg, boolean useNewThread) {
        try {
            return applyFilter(m_ImageFilter, srcImg, useNewThread);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return srcImg;
    }

    @Override
    public int[] applyFilter(int[] pixels, int wt, int ht, int[] inPixels, int[] outPixels) {
        return new int[0];
    }


    @SuppressLint("LongLogTag")
    private int[] applyFilter(ImageFilter imgFltr, int[] pixels, int wt, int ht,
                              int[] inPixels, int[] outPixels)
    {
        ImageFilterHandler imgFilterHdlr = null; ImageFilterController cntrlr = null;
        cntrlr = (ImageFilterController) XONObjectCache.
                getObjectForKey("ImageFilterController");
        int avgImgPixel = ((Integer) XONObjectCache.getObjectForKey("AvgImagePixel")).
                intValue();
        for (int i = 0; i < m_ImageFilterHdlrs.size(); i++)
        {
            imgFilterHdlr = m_ImageFilterHdlrs.elementAt(i);
          /*  if (imgFilterHdlr.getAction().equals(ImageFilter.AutoFix2Enhance) &&
                    avgImgPixel < 96)
                imgFilterHdlr = ImageFilter.createImageFilterHandler(
                        ImageFilter.AutoFix1Enhance.toString());
            if (imgFilterHdlr.getAction().contains("Enhance")) {
                if (cntrlr.getImageFilterHandler(imgFilterHdlr.getAction()) != null) continue;
                if (imgFilterHdlr.getAction().contains("AutoFix")){
                    if (cntrlr.getImageFilterHandler(
                            ImageFilter.AutoFix1Enhance.toString()) != null) continue;
                    if (cntrlr.getImageFilterHandler(
                            ImageFilter.AutoFix2Enhance.toString()) != null) continue;
                }
            }*/
            Log.i(TAG, "Applying Filter using: "+imgFilterHdlr.getAction());
            pixels = imgFilterHdlr.applyFilter(pixels, wt, ht, inPixels, outPixels);
        }
        return pixels;
    }
}
