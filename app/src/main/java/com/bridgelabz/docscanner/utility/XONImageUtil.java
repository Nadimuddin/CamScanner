package com.bridgelabz.docscanner.utility;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.utility.XONPropertyInfo.ImageOrientation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Created by bridgeit on 25/11/16.
 */

public class XONImageUtil {
    public static final String TAG = "XONImageUtil";

    static int RED    = 0;
    static int GREEN  = 1;
    static int BLUE   = 2;
    static int ALPHA  = 3;  // ignored in RGB

    /**
     * Get the size in bytes of a bitmap.
     * @param bitmap
     * @return size in bytes
     */
    @TargetApi(12)
    public static int getBitmapSize(Bitmap bitmap)
    {
        if (XONUtil.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static boolean saveImage(Bitmap bitmap, File file)
    {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            if (bitmap.hasAlpha()) bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            else bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return true;
        } catch(Exception ex) {
            Log.i(TAG, "Unable to save file: "+file.getPath(), ex);
        }
        return false;
    }

    public static int[][][] getBitmapARGBPixels(Bitmap bitmap)
    {
        int wt = bitmap.getWidth(), ht = bitmap.getHeight();
        return getBitmapARGBPixels(getBitmapPixels(bitmap), wt, ht);
    }

    public static int[][][] getBitmapARGBPixels(int[] pixel, int imgCols, int imgRows)
    {
        //Create the One Dimensional  array of type int to be populated with pixel data, one int value
        // per pixel, with four color and alpha bytes  per int value.

        int[][][] pixel_rgb = new int[imgRows][imgCols][4];
        for(int row = 0;row < imgRows;row++)
        {
            for(int col = 0;col < imgCols;col++)
            {
                int element = row * imgCols + col;
                //Alpha data
                pixel_rgb[row][col][ALPHA] = (pixel[element] >> 24) & 0xFF;
                //Red data
                pixel_rgb[row][col][RED] = (pixel[element] >> 16) & 0xFF;
                //Green data
                pixel_rgb[row][col][GREEN] = (pixel[element] >> 8)  & 0xFF;
                //Blue data
                pixel_rgb[row][col][BLUE] = (pixel[element]) & 0xFF;
            }
        }
        return pixel_rgb;
    }

    public static int getAvgPixel(Bitmap bimg)
    {
        float avg = 0;
        int max=0;

        int maxCols = bimg.getWidth();
        int maxRows = bimg.getHeight();
        int[][][]pixel_rgb = getBitmapARGBPixels(bimg);

        for (int row=0 ; row< maxRows ; row++)
        {
            for (int col=0 ; col<maxCols ; col++)
            {
                max = (pixel_rgb[row][col][RED] + pixel_rgb[row][col][BLUE] + pixel_rgb[row][col][GREEN]) ;
                avg = (avg + (float)max/3f) / 2f ;
            }
        }

        Log.i(TAG, "Avg Pixel Value: "+avg);
        printRGBPixel(Math.round(avg));
        return Math.round(avg);
    }

    public static int[] getARGBPixel(int pixel)
    {
        int[] argb = new int[4];
        argb[ALPHA] = (pixel >> 24) & 0xFF; //Alpha data
        argb[RED] = (pixel >> 16) & 0xFF; //Red data
        argb[GREEN] = (pixel >> 8)  & 0xFF; //Green data
        argb[BLUE] = (pixel) & 0xFF; //Blue data
        return argb;
    }

    public static String printRGBPixel(int pixel)
    {
        int[] argb = getARGBPixel(pixel);
        String rgbPixelStr = "Avg Pixel: "+pixel+" R Pixel: "+argb[RED]+" G Pixel: "+
                argb[GREEN]+" And B Pixel: "+argb[BLUE];
        Log.i(TAG, rgbPixelStr);
        return rgbPixelStr;
    }

    public enum BitmapResizeLogic {
        SetScaledBitmap, SetMaxSizeBitmap, SetExactSizeBitmap, NoResize
    }

    public static Bitmap getScaledBitmap(Bitmap srcImg, BitmapResizeLogic resizeLogic,
                                         int dispWt, int dispHt, boolean preserveAspectRatio)
    {
        if (!preserveAspectRatio)
            return Bitmap.createScaledBitmap(srcImg, dispWt, dispHt, false);
        switch(resizeLogic)
        {
            case SetScaledBitmap :
                return getScaledBitmap(srcImg, dispWt, dispHt, preserveAspectRatio);
            case SetExactSizeBitmap :
                return createExactSizeBitmap(srcImg, dispWt, dispHt);
            case SetMaxSizeBitmap :
                return createMaxSizeBitmap(srcImg, dispWt, dispHt, preserveAspectRatio);
            case NoResize :
                return srcImg;
        }
        return getScaledBitmap(srcImg, dispWt, dispHt, preserveAspectRatio);
    }

    public static int[] getBitmapPixels(Bitmap bitmap)
    {
        int bitmapWidth = bitmap.getWidth(), bitmapHeight = bitmap.getHeight();
        int[] pixels = new int[bitmapWidth * bitmapHeight];
        bitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
        return pixels;
    }

    public static Bitmap createBitmap(int[] pixels, int wt, int ht)
    {
        return Bitmap.createBitmap(pixels, wt, ht, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap createExactSizeBitmap(Bitmap srcImg, int dispWt, int dispHt)
    {
        Bitmap newImg = createMaxSizeBitmap(srcImg, dispWt, dispHt, true);
        int[] inPixel = getBitmapPixels(newImg);
        int[] outPixel = new int[dispWt*dispHt]; int index = 0;
        for(int irow = 0; irow < newImg.getHeight(); irow++) {
            if (irow >= dispHt) continue;
            for(int icol = 0; icol < newImg.getWidth(); icol++) {
                if (icol >= dispWt) continue;
                outPixel[index] = inPixel[index];
                index++;
            }
        }
        return createBitmap(outPixel, dispWt, dispHt);
    }

    public static Bitmap createMaxSizeBitmap(Bitmap srcImg, int dispWt, int dispHt,
                                             boolean preserveAspectRatio)
    {
        if (!preserveAspectRatio)
            return Bitmap.createScaledBitmap(srcImg, dispWt, dispHt, false);
        int imgWt = srcImg.getWidth(), imgHt = srcImg.getHeight();
        double scImgWt = 0, scImgHt = 0;
        double aspectRatio =  ((double)imgHt / (double) imgWt);
        Log.i(TAG, "Orig Img Wt: " + imgWt + " ImgHt: " + imgHt +
                " AspectRat: " + aspectRatio);
        if (imgWt > imgHt) { scImgHt = dispHt; scImgWt = scImgHt / aspectRatio; }
        else { scImgWt = dispWt; scImgHt = scImgWt * aspectRatio; }
        Log.i(TAG, "Creating Bitmap of wt: "+scImgWt+" ht: "+scImgHt);
        return Bitmap.createScaledBitmap(srcImg, Double.valueOf(scImgWt).intValue(),
                Double.valueOf(scImgHt).intValue(), false);
    }

    public static Bitmap getScaledBitmap(Bitmap srcImg, Dimension size,
                                         boolean preserveAspectRatio)
    {
        return getScaledBitmap(srcImg, size.width, size.height, preserveAspectRatio);
    }

    public static Bitmap getScaledBitmap(Bitmap srcImg, int dispWt, int dispHt,
                                         boolean preserveAspectRatio)
    {
        int imgWt = srcImg.getWidth(), imgHt = srcImg.getHeight();
        if (imgWt < dispWt && imgHt < dispHt) return srcImg;
        if (!preserveAspectRatio)
            return Bitmap.createScaledBitmap(srcImg, dispWt, dispHt, false);
        double scImgWt = 0, scImgHt = 0;
        double aspectRatio =  ((double)imgHt / (double) imgWt);
        Log.i(TAG, "Orig Img Wt: " + imgWt + " ImgHt: " + imgHt +
                " AspectRat: " + aspectRatio);
        if (imgWt < imgHt) {
            scImgWt = dispWt;
            scImgHt = scImgWt * aspectRatio;
            if (scImgHt > dispHt) {
                scImgHt = dispHt;
                scImgWt = scImgHt / aspectRatio;
            }
        }
        else {
            scImgHt = dispHt;
            scImgWt = scImgHt / aspectRatio;
            Log.i(TAG, "Inside scImgWt: " + scImgWt + " scImgHt: " + scImgHt);
            if (scImgWt > dispWt) {
                scImgWt = dispWt;
                scImgHt = scImgWt * aspectRatio;;
                Log.i(TAG, "Inside2 scImgWt: " + scImgWt + " scImgHt: " + scImgHt);
            }
        }
        return Bitmap.createScaledBitmap(srcImg, Double.valueOf(scImgWt).intValue(),
                Double.valueOf(scImgHt).intValue(), false);
    }

    public static Bitmap compressToSize(Bitmap srcImg)
    {
        return compressToSize(srcImg, XONPropertyInfo.MAX_BITMAP_SIZE);
    }

    public static Bitmap compressToSize(Bitmap srcImg, int size)
    {
        WeakReference<Bitmap> srcImgRef = null;
        int percDec = 5, percComp = 100, srcImgSize = getBitmapSize(srcImg), cntr = 0;
        int origImgSize = srcImgSize, newImgSize = srcImgSize; Bitmap resImg = null;
        while(true)
        {
            if (srcImgSize < size) break;
            cntr++; newImgSize = srcImgSize; percComp -= percDec;
            if (srcImgRef == null) resImg = compressImage(srcImg, percComp);
            else { resImg = compressImage(srcImgRef.get(), percComp);
                if (srcImgRef.get() != resImg) srcImgRef.get().recycle();
                srcImgRef.clear(); srcImgRef = null; }
            srcImgRef = new WeakReference<Bitmap>(resImg);
            srcImgSize = getBitmapSize(srcImgRef.get());
            if (percComp < 60 || newImgSize == srcImgSize) break;
        }
        Log.i(TAG, "origImgSize: "+origImgSize+" compImgSz: "+srcImgSize+
                " compPerc: "+percComp+" NumTimesCompressed: "+cntr);
        if (srcImgRef != null) return srcImgRef.get();
        return srcImg;
    }

    public static Bitmap createBitmap(Bitmap orig, Bitmap fin, Dimension size)
    {
        Vector<Bitmap> bitmaps = new Vector<Bitmap>(2); bitmaps.add(orig); bitmaps.add(fin);
        return createBitmap(bitmaps, size, 1, 2, 1.0f, 0.5f);
    }

    public static Bitmap createBitmap(Vector<Bitmap> bitmaps, Dimension size, int numCols,
                                      int numRows, float wtRatio, float htRatio)
    {
        Bitmap finBitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
        int col = 0, row = 0; float startX = 0f, startY = 0f;
        int dispHt = XONPropertyInfo.getIntRes(R.string.img_disp_gap);
        int finWt = Math.round(size.width*wtRatio), finHt = Math.round(size.height*htRatio)-dispHt;
        Canvas canvas = new Canvas(finBitmap);
        for (int i = 0; i < bitmaps.size(); i++)
        {
            Bitmap temp = bitmaps.elementAt(i);
            Bitmap scImg = getScaledBitmap(temp, BitmapResizeLogic.SetScaledBitmap,
                    finWt, finHt, true);
            int scWt = scImg.getWidth(), scHt = scImg.getHeight();
            float totHGap = (float)(size.width*wtRatio-scWt);
            float totVGap = (float)(size.height*htRatio-scHt);
            float hGap = totHGap/2f, vGap = totVGap/2f; startX += hGap; startY += vGap;
            Log.i(TAG, "Row: "+row+" Col: "+col+" stX: "+startX+" stY: "+startY+
                    " wt: "+finWt+" ht: "+finHt);
            canvas.drawBitmap(scImg, startX, startY, null); col++;
            if (col == numCols) { col = 0; startX = 0; startY += finHt; row++; }
            else { startX += finWt; }
        }
        return finBitmap;
    }

    public static Bitmap compressImage(Bitmap srcImg)
    {
        return compressImage(srcImg, XONPropertyInfo.DEFAULT_COMPRESS_QUALITY);
    }

    public static Bitmap compressImage(Bitmap srcImg, int quality)
    {
        ByteArrayOutputStream outStream = null; ByteArrayInputStream inputStream = null;
        try {
            // The output will be a ByteArrayOutputStream (in memory)
            int wt = srcImg.getWidth(), ht = srcImg.getHeight();
            Log.i(TAG, "Bfro Wt: "+wt+" Ht: "+ht+" Img Density: "+
                    srcImg.getDensity()+" quality: "+quality+" size: "+
                    getBitmapSize(srcImg));
            outStream = new ByteArrayOutputStream();
            srcImg.compress(XONPropertyInfo.DEFAULT_COMPRESS_FORMAT, quality, outStream);
            // From the ByteArrayOutputStream create a RenderedImage.
            inputStream = new ByteArrayInputStream(outStream.toByteArray());
            if (inputStream != null) {
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                wt = bitmap.getWidth(); ht = bitmap.getHeight();
                Log.i(TAG, "After Wt: "+wt+" Ht: "+ht+" Image Density: "+
                        bitmap.getDensity()+" size: "+getBitmapSize(bitmap));
                return bitmap;
            }
        } catch (Exception ex) {
            Log.i(TAG, "Error in Compressing Image", ex);
        } finally {
            try { if (outStream != null) outStream.close();
                if (inputStream != null) inputStream.close(); }
            catch (IOException e) {}
        }
        return null;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, ImageOrientation imgOrient)
    {
        Matrix matrix = new Matrix();
        int wt = bitmap.getWidth(), ht = bitmap.getHeight();
        switch(imgOrient)
        {
            case Rot90: matrix.postRotate(90); break;
            case Rot180: matrix.postRotate(180); break;
            case Rot270: matrix.postRotate(270); break;
            default : return bitmap;
        }
        // create a new bitmap from the original using the matrix to transform the result
        return Bitmap.createBitmap(bitmap , 0, 0, wt, ht, matrix, true);
    }

    public static boolean rotateBitmapNeeded(ImageOrientation imgOrient)
    {
        boolean rotNeeded = false;
        switch(imgOrient)
        {
            case Rot90 : case Rot180:  case Rot270: rotNeeded = true; break;
            default : break;
        }
        // create a new bitmap from the original using the matrix to transform the result
        return rotNeeded;
    }

    public static Bitmap decodeFile(String filePath, Dimension size,
                                    Dimension origImgSize)
    {
        // Decode image size
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, size, origImgSize);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, Dimension size,
                                            Dimension origImgSize)
    {
        // Raw height and width of image
        int reqWidth = size.width, reqHeight = size.height;
        final int height = options.outHeight;
        final int width = options.outWidth;
        if (origImgSize != null) { origImgSize.width = width; origImgSize.height = height; }
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {
            if (width > height) inSampleSize = Math.round((float) height / (float) reqHeight);
            else inSampleSize = Math.round((float) width / (float) reqWidth);

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap)
            { inSampleSize++; }
        }
        return inSampleSize;
    }

    public static Rect getOrigCropRect(Dimension origImgSz, Dimension scImgSz, int startx,
                                       int starty, int lastx, int lasty)
    {
        int x, y, w, h;
        x = Math.min(startx, lastx);
        y = Math.min(starty, lasty);
        w = Math.abs(startx - lastx);
        h = Math.abs(starty - lasty);

        int scwd = scImgSz.width, scht = scImgSz.height;
        int origwd = origImgSz.width, oright = origImgSz.height;

        Log.i(TAG, "Sc Wd: " + scwd + " Sc Ht: " + scht +
                " OrWd: " + origwd + " OrHt: " + oright);
        double scalex = 0.0, scaley = 0.0;

        scalex = (double) origwd/(double) scwd;
        scaley = (double)oright/(double)scht;

        Log.i(TAG, "Scale X: " + scalex + " scaley: " + scaley);

        int start_offsetx = 0, start_offsety = 0;
        int image_startx = x - start_offsetx, image_starty = y - start_offsety;

        int origx = (int) (image_startx * scalex), origy = (int) (image_starty * scaley);
        int origw = (int) (w*scalex), origh = (int) (h*scaley);

        Log.i(TAG, "Orig x: "+origx+" y: "+origy+" w: "+origw+" h: "+origh);
        return new Rect(origx, origy, origx+origw, origy+origh);

    }
}
