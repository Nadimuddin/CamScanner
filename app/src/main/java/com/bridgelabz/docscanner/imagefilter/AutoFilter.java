package com.bridgelabz.docscanner.imagefilter;

import android.graphics.Bitmap;

/**
 * Created by bridgeit on 15/11/16.
 */

public class AutoFilter {

    public static Bitmap[] changeToAuto (Bitmap bitmap) {
        int width, height;
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        ContrastFilter briFltr = new ContrastFilter();
        ContrastFilter contFltr = new ContrastFilter();
        GainFilter biasFltr = new GainFilter();

        float flVal = (float) 1.4;
        briFltr.setBrightness(flVal);

        float flVal1 = (float) 1.6;
        contFltr.setContrast(flVal1);

        float flVal2 = (float) 0.8;
        biasFltr.setBias(flVal2);

        int[] returnPixels = briFltr.filter(pixels, width, height, null, null);
        int[] returnPixels1 = contFltr.filter(pixels, width, height, null, null);
        int[] returnPixels2 = biasFltr.filter(pixels, width, height, null, null);

        Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);
        Bitmap returnBitmap1 = Bitmap.createBitmap(returnPixels1, width, height, Bitmap.Config.ARGB_8888);
        Bitmap returnBitmap2 = Bitmap.createBitmap(returnPixels2, width, height, Bitmap.Config.ARGB_8888);

        return new Bitmap[] {returnBitmap, returnBitmap1, returnBitmap2};

    }


    public static Bitmap[] changeToAuto2 (Bitmap bitmap) {
        int width, height;
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        ContrastFilter briFltr = new ContrastFilter();
        GainFilter gainFltr = new GainFilter();

        float flVal = (float) 2.6;
        briFltr.setBrightness(flVal);

        float flVal1 = (float) 0.4;
        gainFltr.setGain(flVal1);

        int[] returnPixels = briFltr.filter(pixels, width, height, null, null);
        int[] returnPixels1 = gainFltr.filter(pixels, width, height, null, null);

        Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);
        Bitmap returnBitmap1 = Bitmap.createBitmap(returnPixels1, width, height, Bitmap.Config.ARGB_8888);

        return new Bitmap[] {returnBitmap, returnBitmap1};

    }
}
