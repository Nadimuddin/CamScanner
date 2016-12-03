package com.bridgelabz.docscanner.controller;

import android.graphics.Bitmap;
import android.util.Log;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.interfaces.ImageFilterHandler;
import com.bridgelabz.docscanner.utility.XONImageFilterDef;
import com.bridgelabz.docscanner.utility.XONImageFilterDef.ImageFilter;
import com.bridgelabz.docscanner.utility.XONImageUtil;
import com.bridgelabz.docscanner.utility.XONPropertyInfo;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

public class ImageFilterController implements Serializable
{
    public static final String TAG = "ImageFilterController";

    private static final long serialVersionUID = 3472959907695899361L;

    // This maintains the set of Image Filters that is applied on the Image
    public Vector<ImageFilterHandler> m_UsedImageFilterHdlrs;

    public ImageFilterController()
    {
        m_UsedImageFilterHdlrs = new Vector<ImageFilterHandler>();
    }

    public static Bitmap applyFilter(Bitmap srcImg, String imgFltr,
                                     String paramAction, String val)
    {
        ImageFilterHandler imgFltrHdlr = ImageFilter.createImageFilterHandler(imgFltr);
        Log.i(TAG, "ImageFilterHandler: "+imgFltrHdlr);
        imgFltrHdlr.setImageFilterParam(imgFltr+"_"+paramAction, val);
        Bitmap finImg = imgFltrHdlr.applyFilter(srcImg, true);
        return finImg;
    }

    public ImageFilterHandler getImageFilterHandler(String imgFltr)
    {
        return getImageFilterHandler(imgFltr, false);
    }

    public ImageFilterHandler getImageFilterHandler(String imgFltr, boolean isContained)
    {
        ImageFilterHandler imgFilterHdlr = null;
        for (int i = 0; i < m_UsedImageFilterHdlrs.size(); i++)
        {
            imgFilterHdlr = m_UsedImageFilterHdlrs.elementAt(i);
            if (isContained) {
                if (imgFilterHdlr.getAction().contains(imgFltr)) return imgFilterHdlr;
            } else if (imgFilterHdlr.getAction().equals(imgFltr)) return imgFilterHdlr;
        }
        return null;
    }

    public void removeImageFilterHandler(String imgFltr)
    {
        this.removeImageFilterHandler(imgFltr, false);
    }

    public String removeImageFilterHandler(String imgFltr, boolean isContained)
    {
        ImageFilterHandler imgFilterHdlr = getImageFilterHandler(imgFltr, isContained);
        if (imgFilterHdlr == null) return null;
        Log.i(TAG, "ImgFltr: "+imgFltr+" hdlr action: "+imgFilterHdlr.getAction());
        if (XONImageFilterDef.containsImageFilter(imgFltr)) {
            setImageFilterParam(imgFltr+"_Off", "", isContained);
        } else setImageFilterParam(imgFilterHdlr.getAction()+"_Off", "");
        return imgFilterHdlr.getAction();
    }

    public ImageFilterHandler setImageFilterHandler(String imgFltr)
    {
        return setImageFilterHandler(imgFltr, false);
    }

    public ImageFilterHandler setImageFilterHandler(String imgFltr, boolean isContained)
    {
        ImageFilterHandler imgFilterHdlr = getImageFilterHandler(imgFltr, isContained);
        if (imgFilterHdlr == null) {
            imgFilterHdlr = XONImageFilterDef.ImageFilter.
                    createImageFilterHandler(imgFltr);
            m_UsedImageFilterHdlrs.addElement(imgFilterHdlr);
        }
        return imgFilterHdlr;
    }

    public ImageFilterHandler setImageFilterParam(String imgFltr, String val)
    {
        return setImageFilterParam(imgFltr, val, false);
    }

    public ImageFilterHandler setImageFilterParam(String imgFltr, String val,
                                                  boolean isContained)
    {
        Log.i(TAG, "Creating Image Filter for: "+imgFltr);
        ImageFilterHandler.SetImageFilter status = ImageFilterHandler.SetImageFilter.OFF;
        String[] filterParams = imgFltr.split("_");
        for (int i = 0; i < filterParams.length; i++)
        Log.i(TAG, "Filter Param at: "+i+" is: "+filterParams[i]);
        if (filterParams.length == 0)
        Log.i(TAG, "Issue with Action Param: "+imgFltr);
        String action = filterParams[0], paramSetting = filterParams[1];
        if (filterParams.length == 3) paramSetting = filterParams[2];
        Log.i(TAG, "Param Settings: "+paramSetting);
        ImageFilterHandler imgFilterHdlr = setImageFilterHandler(action, isContained);
        if (paramSetting.equals("Cancel")) { m_UsedImageFilterHdlrs.remove(imgFilterHdlr);
            return null; }
        status = imgFilterHdlr.setImageFilterParam(imgFltr, val);

        Log.i(TAG, "Image Fltr: "+imgFilterHdlr.getAction()+" Status: "+status);
        if (status.toString().equals(ImageFilterHandler.SetImageFilter.OFF.toString()))
        { m_UsedImageFilterHdlrs.remove(imgFilterHdlr); return null; }
        return imgFilterHdlr;
    }

    // Apply all the applicable Filters
    public Bitmap applyFilter(Bitmap srcImg, boolean exclLastFltr)
    {
        return applyFilter(srcImg, null, exclLastFltr);
    }

    public Bitmap applyFilter(Bitmap srcImg, ImageFilterHandler imgFilterHdlr, boolean exclLastFltr)
    {
        Log.i(TAG, "Size of UsedImgFltrs: "+m_UsedImageFilterHdlrs.size());
        Bitmap resImg = srcImg; int wt = srcImg.getWidth(), ht = srcImg.getHeight();
        if (imgFilterHdlr != null) {
            resImg = imgFilterHdlr.applyFilter(resImg, true);
        } else {
            if (m_UsedImageFilterHdlrs.size() == 0) return resImg;
            int[] pixels = XONImageUtil.getBitmapPixels(srcImg); resImg = srcImg = null;
            ImageFilterHandler frameFilterHdlr = null; //applyFilterRules();
            int[] inPixels = new int[wt], outPixels = new int[wt*ht];
            String framesGrpName = XONPropertyInfo.getString(R.string.frame_effects, true);
            for (int i = 0; i < m_UsedImageFilterHdlrs.size(); i++)
            {
                if (exclLastFltr) { if (i+1 == m_UsedImageFilterHdlrs.size()) break; }
                imgFilterHdlr = m_UsedImageFilterHdlrs.elementAt(i);
                Log.i(TAG, "Applying Filter using: "+imgFilterHdlr.getAction());
                if (imgFilterHdlr.getAction().contains(framesGrpName))
                { frameFilterHdlr = imgFilterHdlr; continue; }
                pixels = imgFilterHdlr.applyFilter(pixels, wt, ht, inPixels, outPixels);
            }
            if (frameFilterHdlr != null)
                pixels = frameFilterHdlr.applyFilter(pixels, wt, ht, inPixels, outPixels);
            inPixels = null; outPixels = null;
            resImg = XONImageUtil.createBitmap(pixels, wt, ht);
        }
        return resImg;
    }

    public boolean canApplyImageFilter(String imageFltr)
    {
        ImageFilterHandler imgFilterHdlr = null; String appImgFltr = "";
        Map<String, Vector<String>> imgFltrRules = XONImageFilterDef.m_ImageFilterRules;
        for (int i = 0; i < m_UsedImageFilterHdlrs.size(); i++)
        {
            imgFilterHdlr = m_UsedImageFilterHdlrs.elementAt(i);
           // if (!imgFilterHdlr.canApplyImageFilter(imageFltr)) return false;
            appImgFltr = imgFilterHdlr.getAction().toString();
            Vector<String> negateFltrs = imgFltrRules.get(appImgFltr);
            if (negateFltrs == null) continue;
            if (negateFltrs.contains(imageFltr)) return false;
        }
        return true;
    }

    public void applyFilterRules()
    {
        ImageFilterHandler imgFilterHdlr = null; String appImgFltr = "";
        for (int i = 0; i < m_UsedImageFilterHdlrs.size(); i++)
        {
            imgFilterHdlr = m_UsedImageFilterHdlrs.elementAt(i);
            appImgFltr = imgFilterHdlr.getAction().toString();
            applyFilterRule(appImgFltr);
        }
    }

    public Vector<String> applyFilterRule(String appImgFltr)
    {
        Vector<String> deSelFltr = new Vector<String>(); String imgFlr = null;
        String framesGrpName = XONPropertyInfo.getString(R.string.frame_effects, true);
        Map<String, Vector<String>> imgFltrRules = XONImageFilterDef.m_ImageFilterRules;
        Log.i(TAG, "Applying Rules for: "+appImgFltr);
        if (appImgFltr.contains(ImageFilter.PopularTemplateFilter.toString()) ||
                appImgFltr.contains(ImageFilter.PersonalizedTemplateFilter.toString())) {
            imgFlr = removeImageFilterHandler(ImageFilter.PopularTemplateFilter.toString(), true);
            if (imgFlr != null) deSelFltr.add(imgFlr);
            imgFlr = removeImageFilterHandler(ImageFilter.PersonalizedTemplateFilter.toString(), true);
            if (imgFlr != null) deSelFltr.add(imgFlr);
        } else if (appImgFltr.contains(framesGrpName)) {
            imgFlr = removeImageFilterHandler(framesGrpName, true);
            if (imgFlr != null) deSelFltr.add(imgFlr);
        }
        if (!imgFltrRules.containsKey(appImgFltr)) return deSelFltr;
        Vector<String> negateFltrs = imgFltrRules.get(appImgFltr);
        for(int iter = 0; iter < negateFltrs.size(); iter++) {
            removeImageFilterHandler(negateFltrs.elementAt(iter).toString());
            deSelFltr.add(negateFltrs.elementAt(iter).toString());
        }
        Log.i(TAG, "Negate Filters: "+deSelFltr);
        return deSelFltr;
    }

    // Apply Last Filters
    public Bitmap applyLastFilter(Bitmap srcImg)
    {
        Log.i(TAG, "Size of UsedImgFltrs: "+m_UsedImageFilterHdlrs.size());
        if (m_UsedImageFilterHdlrs.size() == 0) return srcImg;
        try {
            ImageFilterHandler imgFilterHdlr = m_UsedImageFilterHdlrs.lastElement();
            return imgFilterHdlr.applyFilter(srcImg, false);
        } catch(Exception ex) {
            Log.i(TAG, "Unable to Apply Last Filter", ex);
        }
        return srcImg;
    }

    public boolean isImageFilterSpecified()
    {
        if (m_UsedImageFilterHdlrs.size() == 0) return false;
        return true;
    }

    public Vector<ImageFilterHandler> getUsedImageEffects()
    {
        return m_UsedImageFilterHdlrs;
    }

    public String toString() { return "Set ImageFilterHandler are "+m_UsedImageFilterHdlrs; }
}