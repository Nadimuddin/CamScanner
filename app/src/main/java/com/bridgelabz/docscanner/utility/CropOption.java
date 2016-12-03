package com.bridgelabz.docscanner.utility;

/**
 * Created by bridgelabz1 on 27/10/16.
 */

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class CropOption {
    public CharSequence title;
    public Drawable icon;
    public Intent appIntent;

    /*public void Filter(String imgFilter) {
        String imgFltr = null;
        *//*<string-array name="AutoFix1Enhance_Val">
        <item>Name:=Bias::Type:=Intensity::AmtX:=-1.0::MinX:=0.0::MaxX:=1.0::FactorX:=1.0</item>
        </string-array>
        <string-array name="AutoFix2Enhance_Val">
        <item>Name:=Brightness::Type:=Intensity::AmtX:=-1.0::MinX:=0.0::MaxX:=3.0::FactorX:=3.0</item>
        </string-array>*//*
        final String AutoFix1Enhance = "Name:=Bias::Type:=Intensity::AmtX:=-1.0::MinX:=0.0::MaxX:=1.0::FactorX:=1.0";
        final String AutoFix2Enhance = "Name:=Brightness::Type:=Intensity::AmtX:=-1.0::MinX:=0.0::MaxX:=3.0::FactorX:=3.0";

        switch (imgFiltero) {
            // Apply Image Enhance Filters
            case AutoFix1Enhance:
                Vector<Float> settings = XONPropertyInfo.getFixImageSettings(avgImgPixel);
                hdlr = ImageFilter.createImageFilterHandler(ImageFilter.BrightnessEnhance.toString());
                hdlr.setFieldValue("Brightness",
                        String.valueOf(settings.elementAt(0).floatValue()), false);
                pixels = hdlr.applyFilter(pixels, wt, ht, inPixels, outPixels);
                hdlr = ImageFilter.createImageFilterHandler(ImageFilter.ContrastEnhance.toString());
                hdlr.setFieldValue("Contrast",
                        String.valueOf(settings.elementAt(1).floatValue()), false);
                pixels = hdlr.applyFilter(pixels, wt, ht, inPixels, outPixels);
                hdlr = ImageFilter.createImageFilterHandler(ImageFilter.BiasEnhance.toString());
                flVal = ((Float) getFieldValue("Bias", false)).floatValue();
                float biasStngs = settings.elementAt(2).floatValue();
                reFactor = true;
                if (flVal < 0) {
                    flVal = biasStngs;
                    reFactor = false;
                }
                hdlr.setFieldValue("Bias", String.valueOf(flVal), reFactor);
                pixels = hdlr.applyFilter(pixels, wt, ht, inPixels, outPixels);
                break;
            case FixMthd2Enhance:
            case AutoFix2Enhance:
                Vector<Float> gainStngs = XONPropertyInfo.getFixImageGainSettings(avgImgPixel);
                hdlr = ImageFilter.createImageFilterHandler(ImageFilter.GainEnhance.toString());
                XONUtil.logDebugMesg("Fltr Hdlr: " + hdlr + " Settings: " + gainStngs);
                flVal = gainStngs.elementAt(0).floatValue();
                XONUtil.logDebugMesg("Val: " + flVal);
                hdlr.setFieldValue("Gain", String.valueOf(flVal), false);
                pixels = hdlr.applyFilter(pixels, wt, ht, inPixels, outPixels);
                hdlr = ImageFilter.createImageFilterHandler(ImageFilter.BrightnessEnhance.toString());
                flVal = ((Float) getFieldValue("Brightness", false)).floatValue();
                float brightStng = gainStngs.elementAt(1).floatValue();
                reFactor = true;
                if (flVal < 0) {
                    flVal = brightStng;
                    reFactor = false;
                }
                XONUtil.logDebugMesg("Brightness Val: " + flVal);
                hdlr.setFieldValue("Brightness", String.valueOf(flVal), reFactor);
                pixels = hdlr.applyFilter(pixels, wt, ht, inPixels, outPixels);
                break;
        }
    }

    public static enum ImageFilter
    {
        // Enhance Filters
        AutoFix1Enhance, AutoFix1Enhance_On, AutoFix1Enhance_Off,
        AutoFix2Enhance, AutoFix2Enhance_On, AutoFix2Enhance_Off;
    }*/
}