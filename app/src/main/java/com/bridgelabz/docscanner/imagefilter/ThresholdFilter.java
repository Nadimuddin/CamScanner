package com.bridgelabz.docscanner.imagefilter;

import android.graphics.Bitmap;

/**
 * Created by bridgeit on 21/11/16.
 */

public class ThresholdFilter extends PointFilter {

    private int lowerThreshold;
    private int upperThreshold;
    private int white = 0xffffff;
    private int black = 0x000000;

    /**
     * Construct a ThresholdFilter.
     */
    public ThresholdFilter() {
        this(127);
    }

    /**
     * Construct a ThresholdFilter.
     * @param t the threshold value
     */
    public ThresholdFilter(int t) {
        setLowerThreshold(t);
        setUpperThreshold(t);
    }

    /**
     * Set the lower threshold value.
     * @param lowerThreshold the threshold value
     */
    public void setLowerThreshold(int lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    /**
     * Set the upper threshold value.
     * @param upperThreshold the threshold value
     */
    public void setUpperThreshold(int upperThreshold) {
        this.upperThreshold = upperThreshold;
    }

    public int filterRGB(int x, int y, int rgb) {
        int v = PixelUtils.brightness( rgb );
        float f = ImageMath.smoothStep( lowerThreshold, upperThreshold, v );
        return (rgb & 0xff000000) | (ImageMath.mixColors( f, black, white ) & 0xffffff);
    }

    public String toString() {
        return "Stylize/Threshold...";
    }

    public static Bitmap changeToBW (Bitmap bitmap) {
        int width, height;
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        ThresholdFilter threshFltr = new ThresholdFilter();

       // int intVal = 253;
      //  int intVal = 105;
        int intVal = 90;
        if (intVal < 127) threshFltr.setLowerThreshold(intVal);
        else threshFltr.setUpperThreshold(intVal);
        int[] returnPixels = threshFltr.filter(pixels, width, height, null, null);
        Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);

        return returnBitmap;
    }
}
