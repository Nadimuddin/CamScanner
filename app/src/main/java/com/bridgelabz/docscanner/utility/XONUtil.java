package com.bridgelabz.docscanner.utility;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.utility.XONPropertyInfo.ImageOrientation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by bridgeit on 25/11/16.
 */

public class XONUtil {

    public static final String TAG = "XONUtil";
    public static int POINT_SIZE = 80;

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

    public static Vector<Integer> createVector(int[] array)
    {
        return (new Vector<Integer>(intArrayAsList(array)));
    }

    //Helper method to convert int arrays into Lists
    public static java.util.List<Integer> intArrayAsList(final int[] a)
    {
        if(a == null) throw new NullPointerException();
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return a[i];//autoboxing
            }
            @Override
            public Integer set(int i, Integer val) {
                final int old = a[i];
                a[i] = val;//auto-unboxing
                return old;//autoboxing
            }
            @Override
            public int size() {
                return a.length;
            }
        };
    }

    // Retrieve Object from Serialized File
    public static Object getSerializeObject(String absFilePath) throws Exception
    {
        Object obj = null;
        FileInputStream fileStream = null;
        ObjectInputStream objStr = null;
        try {
            fileStream =  new FileInputStream(absFilePath);
            objStr = new ObjectInputStream(fileStream);
            obj = objStr.readObject();
        }
        finally
        {
            if(objStr != null) objStr.close();
            if (fileStream != null) fileStream.close();
        }
        return obj;
    }

    public static int[] toArray(Vector<Integer> vector)
    {
        return toIntArray(Arrays.asList(vector.toArray(new Integer[0])));
    }

    public static int[] toIntArray(java.util.List<Integer> list)
    {
        int[] ret = new int[list.size()]; int i = 0;
        for (Integer e : list)  ret[i++] = e.intValue();
        return ret;
    }

    // Serializes the Object.
    public static void serializeObject(String absFilePath, Object obj) throws Exception
    {
        FileOutputStream fileStream = null;
        ObjectOutputStream objStream = null;
        try {
            fileStream = new FileOutputStream(absFilePath);
            objStream = new ObjectOutputStream(fileStream);
            objStream.writeObject(obj);
        }
        finally
        {
            if(objStream != null) objStream.close();
            if (fileStream != null) fileStream.close();
        }
    }

    public static String[] objToStringArray(Object[] objValues, String ignoreVal)
    {
        String strVal = ""; Vector<String> strValueList = new Vector<String>();
        for (int i = 0; i<objValues.length; i++) {
            strVal = objValues[i].toString();
            if (ignoreVal == null) { strValueList.addElement(strVal); continue; }
            if (strVal.contains(ignoreVal)) continue;
            strValueList.addElement(strVal);
        }
        return strValueList.toArray(new String[0]);
    }

    public static int put2Sleep(int multiples)
    {
        int sleepTime = multiples*XONPropertyInfo.ThreadSleepTime;
        try { Thread.sleep(sleepTime); }
        catch(Exception ex) {}
        return sleepTime;
    }

    public static String replaceSpace(String original, String delim)
    {
        StringBuffer httpTxt = new StringBuffer();
        String[] words = original.split(" ");
        for (int i = 0; i < words.length; i++) {
            httpTxt.append(words[i]);
            if (i < words.length-1) httpTxt.append(delim);
        }
        return httpTxt.toString();
    }

    public static void deleteFiles(File file)
    {
        if (file == null || !file.exists()) return;
        file.delete(); if (!file.exists()) return;
        Log.i(TAG, "Delete exec Runtime Command");
        String deleteCmd = "rm -r " + file.getAbsolutePath();
        Runtime runtime = Runtime.getRuntime();
        try { runtime.exec(deleteCmd); } catch (Exception e) {}
    }

    // This function takes the file Name with extension and returns file name
    // without extension
    public static String getFileName(String file)
    {
        String fileName = file;
        int i = file.lastIndexOf('.');
        if (i > 0 &&  i < file.length() - 1) {
            fileName = file.substring(0, i);
        }
        int j = fileName.lastIndexOf("/");
        if (j > 0 &&  j < fileName.length() - 1) {
            fileName = fileName.substring(j+1, fileName.length());
        }
        return fileName;
    }

    public static boolean hasJellyBean()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasHoneycombMR1()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static Vector<String> createVector(String[] array)
    {
        return (new Vector<>(Arrays.asList(array)));
    }

    public static ImageOrientation getOrientation(String filePath)
    {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            ImageOrientation imgOrient = ImageOrientation.Normal;
            if (orient == ExifInterface.ORIENTATION_ROTATE_90)
                imgOrient = ImageOrientation.Rot90;
            else if (orient == ExifInterface.ORIENTATION_ROTATE_180)
                imgOrient = ImageOrientation.Rot180;
            else if (orient == ExifInterface.ORIENTATION_ROTATE_270)
                imgOrient = ImageOrientation.Rot270;
            return imgOrient;
        } catch(Exception ex) { return ImageOrientation.Normal; }
    }

    public static boolean hasHoneycomb()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static String getFilePath(Uri uri)
    {
        return getFilePath(XONPropertyInfo.m_MainActivity, uri);
    }

    public static String getFilePath(Activity act, Uri uri)
    {
        String filePath = "";
        // OI FILE Manager
        String filemanagerstring = uri.getPath();
        // MEDIA GALLERY
        String selectedImagePath = getPath(act, uri);
        if (selectedImagePath != null) filePath = selectedImagePath;
        else if (filemanagerstring != null) filePath = filemanagerstring;
        return filePath;
    }

    // Fetch screen height and width
    public static Dimension getScreenDimension(Activity act)
    {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;
        return new Dimension(width, height);
    }

    @SuppressWarnings("deprecation")
    public static String getPath(Activity act, Uri uri)
    {
        String imgProj = MediaStore.Images.Media.DATA; String[] projection = { imgProj };
        Cursor cursor = null;
        if (hasHoneycomb()) {
            cursor = act.getApplicationContext().
                    getContentResolver().query(uri, projection, null, null, null);
        }
        else cursor = act.managedQuery(uri, projection, null, null, null);
        if (cursor == null) return null;
        // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
        // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
        int column_index = cursor.getColumnIndexOrThrow(imgProj);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static Rect createRect(int centX, int centY, int width, int height)
    {
        Log.i(TAG, "centX: "+centX+" centY: "+centY+" Wt: "+width+" Ht:"+height);
        int left = centX-Math.round(width/2), top = centY-Math.round(height/2);
        return new Rect(left, top, left+width, top+height);
    }

    public static int contains(Vector<Point> pts, int x, int y)
    {
        Point pt = null; Rect ptRect = null; int index = -1;
        for (int i = 0; i < pts.size(); i++) {
            pt = pts.elementAt(i); ptRect = createRect(pt.x, pt.y, POINT_SIZE, POINT_SIZE);
            if (ptRect.contains(x, y)) { index = i; break; }
            pt = null;
        }
        return index;
    }

    public static int getDeviceMemory(Context context)
    {
        int memClass = ((ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
        return memClass * 1024 * 1024;
    }

    // Creates a new Rect by accommodating the translation in x and y dirn
    public static Rect createRect(Vector<Point> pts, int distX, int distY)
    {
        if (pts.size() != 4) return null;
        Point topPt = pts.elementAt(0), botPt = pts.elementAt(2);
        int left = topPt.x+distX, top = topPt.y+distY;
        int right = botPt.x+distX, bot = botPt.y+distY;
        return new Rect(left, top, right, bot);
    }

    // Creates a new Rect by accommodating the scaling of a pt in x and y dirn
    public static Rect createRect(Vector<Point> pts, int selIndex, int distX, int distY)
    {
        if (pts.size() != 4) return null;
        Point topPt = pts.elementAt(0), botPt = pts.elementAt(2);
        int left = topPt.x, top = topPt.y, right = botPt.x, bot = botPt.y;
        if (selIndex == 0) {
            left = topPt.x+distX; top = topPt.y+distY;
        } else if (selIndex == 1) {
            right = botPt.x+distX; top = topPt.y+distY;
        } else if (selIndex == 3) {
            left = topPt.x+distX;  bot = botPt.y+distY;
        } else { right = botPt.x+distX; bot = botPt.y+distY; }
        return new Rect(left, top, right, bot);
    }

    // Clears the Data
    @SuppressWarnings("rawtypes")
    public static void clear(HashMap data)
    {
        if (data != null) data.clear(); data = null;
    }


    // Return angle in radians
    public static double getAngle(Point vertPt, Point edgePt)
    {
        // if loop is 1st and 4th qudrant, else loop is 2nd and 3rd quadrant
        double dx = Math.abs(edgePt.x - vertPt.x), dy = Math.abs(edgePt.y - vertPt.y);
        if (dx == 0.0D) {
            if (edgePt.y > vertPt.y) return Math.PI/2.0D; else return 3*Math.PI/2.0D;
        } else if (dy == 0.0D) { if(edgePt.x > vertPt.x) return 0.0D; else return Math.PI; }
        if(edgePt.x > vertPt.x) {
            // If loop is 1st Quadrant and else is 4th Quadrant
            if (edgePt.y > vertPt.y) return Math.atan(dy/dx);
            else return (3*Math.PI/2.0D + Math.atan(dy/dx));
        } else {
            // If loop is 2nd Quadrant and else is 3rd Quadrant
            if (edgePt.y > vertPt.y) return (Math.PI/2.0D + Math.atan(dx/dy));
            else return (Math.PI + Math.atan(dy/dx));
        }
    }

    public static Rect createRect(int centX, int centY)
    {
        int size = XONPropertyInfo.getIntRes(R.string.CircularImpactSize);
        return createRect(centX, centY, size, size);
    }

    public static long getCurrentTime()
    {
        return Calendar.getInstance().getTime().getTime();
    }

    public static void put2Sleep()
    {
        put2Sleep(1);
    }

    // Retrieve Object from Serialized File
    public static Object getSerializeObject(Activity act, int resId) throws Exception
    {
        Object obj = null;
        InputStream inputStream = null; ObjectInputStream objStr = null;
        try {
            inputStream = act.getResources().openRawResource(resId);
            objStr = new ObjectInputStream(inputStream);
            obj = objStr.readObject();
        }
        finally
        {
            Log.i(TAG, "Closing Streams for res: "+resId);
            if(objStr != null) objStr.close();
            if (inputStream != null) { inputStream.close(); }
        }
        return obj;
    }
}
