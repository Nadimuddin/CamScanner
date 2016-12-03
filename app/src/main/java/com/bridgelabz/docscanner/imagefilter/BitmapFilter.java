package com.bridgelabz.docscanner.imagefilter;

import android.graphics.Bitmap;
import android.util.Log;


/**
 * Created by bridgelabz1 on 25/10/16.
 */

public class BitmapFilter {
    public static final String TAG = "BitmapFilter";
    /**
     * filter style id;
     */
    public static final int BRIGHTNESS_ENHANCE_STYLE = 3; // brightness enhance
    public static final int CONTRAST_ENHANCE_STYLE = 4; // contrast enhance
    public static final int BIAS_ENHANCE_STYLE = 0; // bias enhance
    public static final int GAIN_ENHANCE_STYLE = 0; // gain enhance
    public static final int GREY_COLOR_STYLE = 5; // grey color
    public static final int GREY_OUT_COLOR_STYLE = 0; // grey out color
    public static final int AUTOFIX_ENHANCE_STYLE = 1; // auto fix
    //public static final int AUTOFIX2_ENHANCE_STYLE = 8; // auto fix 2
    public static final int BW_STYLE = 6; // black and white

    /**
     * change bitmap filter style
     * @param styleNo, filter style id
     * @param bitmap
     */
    public static Bitmap[] changeStyle(Bitmap bitmap, int styleNo) {

            if (styleNo == BRIGHTNESS_ENHANCE_STYLE) {
                Log.i(TAG, "applyStyle: .........................BRIGHTNESS_ENHANCE_STYLE."+BRIGHTNESS_ENHANCE_STYLE);
                return new Bitmap[]{ContrastFilter.changeBrightness(bitmap)};
            } else if (styleNo == CONTRAST_ENHANCE_STYLE) {
                Log.i(TAG, "applyStyle: .........................CONTRAST_ENHANCE_STYLE."+CONTRAST_ENHANCE_STYLE);
                return new Bitmap[]{ContrastFilter.changeContrast(bitmap)};
            } else if (styleNo == BIAS_ENHANCE_STYLE) {
                Log.i(TAG, "applyStyle: .........................BIAS_ENHANCE_STYLE."+BIAS_ENHANCE_STYLE);
                return new Bitmap[]{GainFilter.changeToBias(bitmap)};

            } else if (styleNo == GAIN_ENHANCE_STYLE) {
                Log.i(TAG, "applyStyle: .........................GAIN_ENHANCE_STYLE."+GAIN_ENHANCE_STYLE);
                return new Bitmap[]{GainFilter.changeToGain(bitmap)};

            } else if (styleNo == GREY_COLOR_STYLE) {
                Log.i(TAG, "applyStyle: .........................GREY_COLOR_STYLE."+GREY_COLOR_STYLE);
                return new Bitmap[]{GrayscaleFilter.changeToGrayScale(bitmap)};

            } else if (styleNo == GREY_OUT_COLOR_STYLE) {
                Log.i(TAG, "applyStyle: .........................GREY_OUT_COLOR_STYLE."+GREY_OUT_COLOR_STYLE);
                return new Bitmap[]{GreyFilter.changeToGrey(bitmap)};

            } else if (styleNo == AUTOFIX_ENHANCE_STYLE) {
                Log.i(TAG, "applyStyle: .........................AUTOFIX_ENHANCE_STYLE."+AUTOFIX_ENHANCE_STYLE);
                return AutoFilter.changeToAuto(bitmap);

            }/* else if (styleNo == AUTOFIX2_ENHANCE_STYLE) {
                Log.i(TAG, "applyStyle: .........................AUTOFIX2_ENHANCE_STYLE."+AUTOFIX2_ENHANCE_STYLE);
                return AutoFilter.changeToAuto2(bitmap);
            }*/
            else if (styleNo == BW_STYLE) {
                Log.i(TAG, "applyStyle: .........................BW_STYLE."+BW_STYLE);
                return new Bitmap[]{ThresholdFilter.changeToBW(bitmap)};
            }
        return new Bitmap[]{bitmap};
    }
}