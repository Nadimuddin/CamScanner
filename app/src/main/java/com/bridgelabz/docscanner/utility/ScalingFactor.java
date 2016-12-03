package com.bridgelabz.docscanner.utility;

import android.util.Log;

/**
 * Created by bridgeit on 25/11/16.
 */

public class ScalingFactor {

    public static final String TAG = "ScalingFactor";

    public static final int FIT_PAGE_SCALE_FACTOR = 1;
    public static final int FIT_WIDTH_SCALE_FACTOR = 2;
    public static final int FIT_HEIGHT_SCALE_FACTOR = 3;
    public static final int ZOOM_IN_OUT_SCALE_FACTOR = 4;
    private int m_ScaleFactor;
    private String m_ScaleFactorName;

    // This is set if the ScalingFactor is set to ZOOM_IN_OUT
    public int m_ZoomValue;

    // This fits the PDF Page Image to the Height of the Viewable Panel
    public static final ScalingFactor FIT_HEIGHT;

    // This fits the PDF Page Image to the Width of the Viewable Panel
    public static final ScalingFactor FIT_WIDTH;

    // This fits the whole of the PDF Page Image to the  Viewable Panel
    public static final ScalingFactor FIT_PAGE;

    // This allows to Zoom in or Zoom out of the PDF Page
    public static final ScalingFactor ZOOM_IN_OUT;

    static
    {
        FIT_PAGE = new ScalingFactor(FIT_PAGE_SCALE_FACTOR, "FIT PAGE");
        FIT_WIDTH = new ScalingFactor(FIT_WIDTH_SCALE_FACTOR, "FIT WIDTH");
        FIT_HEIGHT = new ScalingFactor(FIT_HEIGHT_SCALE_FACTOR, "FIT HEIGHT");
        ZOOM_IN_OUT = new ScalingFactor(ZOOM_IN_OUT_SCALE_FACTOR, "ZOOM IN OUT");
    }

    // Fill Logic is used determine the Scaling Factor to be used for the image to fill
    // the BBox.
    // FIT_IMAGE_IN_BBOX - Used for occupying the complete image in the BBox. And may times
    // it is possible that the BBox is not occupied completely.
    // FILL_BBOX_WITH_IMAGE - Used for occupying the complete BBox, thus allowing image adustments
    // along the horz or vert axis
    public static enum FillLogic { FIT_IMAGE_IN_BBOX, FILL_BBOX_WITH_IMAGE }

    public ScalingFactor(int scaleFactor, String scaleFactorName)
    {
        m_ScaleFactor = scaleFactor;
        m_ScaleFactorName = scaleFactorName;
    }

    public boolean isFitHeight() { return this.equals(FIT_HEIGHT); }
    public boolean isFitWidth() { return this.equals(FIT_WIDTH); }
    public boolean isFitPage() { return this.equals(FIT_PAGE); }
    public boolean isZoomInOut() { return this.equals(ZOOM_IN_OUT); }

    // This method is used to Fill Image in the BBox
    private static ScalingFactor
    getScalingFactorToFillImage(Dimension dispSize, double imgWt, double imgHt)
    {
        ScalingFactor scaleFactor = FIT_WIDTH;
        double dispWt = 0, dispHt = 0;
        double aspectRatio =  ((double)imgHt / (double) imgWt);
        if (imgWt < imgHt) {
            scaleFactor = FIT_HEIGHT;
            dispHt = (double) dispSize.height;
            dispWt = dispHt / aspectRatio;
            if (dispWt > dispSize.width) scaleFactor = FIT_WIDTH;
        } else if (imgWt > imgHt) {
            scaleFactor = FIT_WIDTH;
            dispWt = (double) dispSize.width;
            dispHt = dispWt * aspectRatio;;
            if (dispHt > dispSize.height) scaleFactor = FIT_HEIGHT;
        }
        return scaleFactor;
    }

    public static ScalingFactor getScalingFactor(Dimension dispSize, double imgWt,
                                                 double imgHt, FillLogic fillLogic)
    {
        ScalingFactor scaleFactor = null;
        double dispWt = 0, dispHt = 0;
        double aspectRatio =  ((double)imgHt / (double) imgWt);
        double dispAspectRatio = ((double)dispSize.height / (double) dispSize.width);
        Log.i(TAG, "Img aspectRatio: "+aspectRatio+" Disp aspectRatio: "+
                dispAspectRatio);
        if (dispAspectRatio == aspectRatio ||
                Math.abs(dispAspectRatio-aspectRatio)<0.01) return FIT_PAGE;
        if (fillLogic.equals(FillLogic.FIT_IMAGE_IN_BBOX))
            return getScalingFactorToFillImage(dispSize, imgWt, imgHt);
        if (imgWt < imgHt) {
            dispWt = (double)dispSize.width;
            dispHt = dispWt * aspectRatio;
            scaleFactor = FIT_WIDTH;
            if (dispHt < (double)dispSize.height) {
                dispHt = (double) dispSize.height;
                dispWt = dispHt / aspectRatio;
                scaleFactor = FIT_HEIGHT;
            }
        }
        else {
            dispHt = (double) dispSize.height;
            dispWt = dispHt / aspectRatio;
            scaleFactor = FIT_HEIGHT;
            if (dispWt < (double) dispSize.width) {
                dispWt = (double) dispSize.width;
                dispHt = dispWt * aspectRatio;;
                scaleFactor = FIT_WIDTH;
            }
        }
        return scaleFactor;
    }
}
