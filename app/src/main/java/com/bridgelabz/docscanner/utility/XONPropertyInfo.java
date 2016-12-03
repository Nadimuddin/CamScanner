package com.bridgelabz.docscanner.utility;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.controller.XONImageProcessHandler;
import com.bridgelabz.docscanner.interfaces.XONClickListener;
import com.bridgelabz.docscanner.interfaces.XONImageProcessor;

import java.util.Map;

/**
 * Created by bridgeit on 25/11/16.
 */

public class XONPropertyInfo {

    public static final String TAG = "XONPropertyInfo";
    public static boolean DEBUG = BuildConfig.DEBUG;
    public static Activity m_MainActivity, m_SubMainActivity;
    public static boolean m_SlideMesgShown = false;
    public static boolean m_CircularSlideMesgShown = false;
    public static ProgressBar m_ProgressBar;
    public static final int LONG_TOAST_DELAY = 3500;
    public static Dimension m_MaxImageSize;
    public static float MAX_IMAGE_SCALE = 1.5f;

    public final static int MAX_BITMAP_SIZE = 1024 * 1024 * 2; // 2MB
    public static final float PERC_SIZE_RED_4_1MB = 0.2f;
    public static int IntensityAdjHt = 75;

    public static int[] m_ColorOptions, m_BorderColorOptions;
    public static int ThreadPoolSize = 10;
    public static int ThreadSleepTime = 500;
    public static int ScrollMonitorTime = 1000;

    // This Asyncy Process Handler
    public static XONImageProcessHandler m_AsyncProcessHandler;

    // Default memory cache size
    public static final float MEM_CACHE_PERC = 0.2f;

    public static boolean m_DevelopmentMode = false;

    // Compression settings when writing images to disk cache
    public final static Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    public static int DEFAULT_COMPRESS_QUALITY = 70;

    // Screens Resolution
    public static enum ScreenResolutionType {
        RESOLUTION_ULTRA_LOW, RESOLUTION_LOW, RESOLUTION_MEDIUM, RESOLUTION_HIGH,
        RESOLUTION_XHIGH
    }
    public static ScreenResolutionType m_ScreenResolutionType;

    // Density Screens
    public static enum DensityScreenType {
        DENSITY_LOW, DENSITY_MEDIUM, DENSITY_HIGH, DENSITY_XHIGH
    }
    public static DensityScreenType m_DensityScreenType;

    public static enum ImageOrientation {
        Normal, Rot90, Rot180, Rot270
    }

    public static String getString(int resId, boolean removeSpace)
    {
        Resources res = m_MainActivity.getResources(); String val = res.getString(resId);
        if (!removeSpace) return val;
        return XONUtil.replaceSpace(val, "");
    }

    public static int getIntRes(int resId)
    {
        return Integer.valueOf(getString(resId)).intValue();
    }

    public static void showAckMesg(int titleResId, String mesgRes, XONClickListener listener)
    {
        UIUtil.showAckMesgDialog(m_SubMainActivity, R.drawable.alert_dialog_icon, titleResId,
                mesgRes, listener);
    }

    public static void showAckMesg(int titleResId, int mesgResId, XONClickListener listener)
    {
        UIUtil.showAckMesgDialog(m_SubMainActivity, R.drawable.alert_dialog_icon, titleResId,
                getString(mesgResId), listener);
    }

    public static void setToastMessage(int mesgRes)
    {
        setToastMessage(getString(mesgRes));
    }

    public static void setToastMessage(String mesg)
    {
        Toast.makeText(m_MainActivity,  mesg, Toast.LENGTH_SHORT).show();
    }

    public static Dimension getThumbSize()
    {
        int iconWt = XONPropertyInfo.getIntRes(R.string.icon_width);
        int iconHt = XONPropertyInfo.getIntRes(R.string.icon_height);
        if (m_ScreenResolutionType.equals(ScreenResolutionType.RESOLUTION_XHIGH))
        { iconWt = 60; iconHt = 60; }
        Dimension thumbSize = new Dimension(iconWt, iconHt);
        return thumbSize;
    }

    public static String[] getStringArray(int resId)
    {
        Resources res = m_MainActivity.getResources();
        return res.getStringArray(resId);
    }

    public static void populateResources(Activity act, boolean debug, boolean popFltrRes)
    {
        if (m_MainActivity == null) {
            m_MainActivity = act;
            Log.i(TAG, "New Activity Setup: "+ act.getCallingPackage());
            setDisplayMetrics();
        }
        m_SubMainActivity = act; m_SlideMesgShown = false; m_CircularSlideMesgShown = false;
        DEBUG = debug; populateXONProperty(popFltrRes);
        m_ProgressBar = (ProgressBar) act.findViewById(R.id.progress_bar);
        if (popFltrRes) XONImageFilterDef.populateImageFilterResource(m_MainActivity);
        m_AsyncProcessHandler = null; // If mem allocated then nullify the same for GC
        m_AsyncProcessHandler = new XONImageProcessHandler(act.getApplicationContext());
        m_AsyncProcessHandler.setImageFadeIn(false);
    }

    public static void activateProgressBar(boolean activate)
    {
        if (m_ProgressBar == null)
            m_ProgressBar = (ProgressBar) m_SubMainActivity.findViewById(R.id.progress_bar);
        // This can happen if the Activity is changed before this thread is executed. In
        // such cases nothing is done
        if (m_ProgressBar == null) return;
        if (activate) {
            m_ProgressBar.setVisibility(View.VISIBLE);
            m_ProgressBar.bringToFront();
        } else m_ProgressBar.setVisibility(View.GONE);
        m_ProgressBar.invalidate();
    }

    public static void activateProgressBarOnUIThread(final boolean activate)
    {
        if (m_ProgressBar == null)
            m_ProgressBar = (ProgressBar) m_SubMainActivity.findViewById(R.id.progress_bar);
        if (m_ProgressBar == null || m_SubMainActivity == null) return;
        m_SubMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activate) {
                    m_ProgressBar.setVisibility(View.VISIBLE);
                    m_ProgressBar.bringToFront();
                } else m_ProgressBar.setVisibility(View.GONE);
                m_ProgressBar.invalidate();
            }
        });
    }


    public static void showErrorAndRoute(final int mesgResId,
                                         final XONClickListener listener)
    {
        m_MainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAckMesg(R.string.ErrorTitle,  getString(mesgResId)+
                        " Cache Size: "+getMemoryCace(), listener);
            }
        });
    }

    public static boolean checkActExists()
    {
        if (m_SubMainActivity == null) return false;
        return true;
    }

    public static void processAsyncTack(XONImageProcessor taskProc, Map<String, Object> data)
    {
        m_AsyncProcessHandler.invokeAsyncTask(taskProc, data);
    }

    public static void setSuperLongToastMessage(int mesgRes)
    {
        setLongToastMessage(getString(mesgRes), 2*LONG_TOAST_DELAY);
    }

    public static void setLongToastMessage(String mesg, int duration)
    {
        int iter = Math.round((float)(duration-LONG_TOAST_DELAY)/(float)LONG_TOAST_DELAY);
        if (iter <= 1) iter = 1;
        for (int i = 0; i < iter; i++)
            Toast.makeText(m_MainActivity,  mesg, Toast.LENGTH_LONG).show();
    }

    public static String getString(int resId)
    {
        Resources res = m_MainActivity.getResources();
        return res.getString(resId);
    }


   /* public static void populateResources(Activity act, boolean debug, boolean popFltrRes)
    {
        if (m_MainActivity == null) {
            m_MainActivity = act;
            Log.i(TAG, "New Activity Setup: "+ act.getCallingPackage());
            setDisplayMetrics();
        }
        m_SubMainActivity = act; m_SlideMesgShown = false; m_CircularSlideMesgShown = false;
        DEBUG = debug; populateXONProperty(popFltrRes);
        m_ProgressBar = (ProgressBar) act.findViewById(R.id.progress_bar);
       // if (popFltrRes) XONImageFilterDef.populateImageFilterResource(m_MainActivity);
        m_AsyncProcessHandler = null; // If mem allocated then nullify the same for GC
        m_AsyncProcessHandler = new XONImageProcessHandler(act.getApplicationContext());
        m_AsyncProcessHandler.setImageFadeIn(false);
    }*/

    private static void setDisplayMetrics()
    {
        Dimension screenSize = XONUtil.getScreenDimension(m_MainActivity);
        // Get the screen's density scale
        DisplayMetrics dispMetrics =  m_MainActivity.getResources().getDisplayMetrics();
        int density = dispMetrics.densityDpi;
        m_MaxImageSize = new Dimension(Math.round(screenSize.width*MAX_IMAGE_SCALE),
                Math.round(screenSize.height*MAX_IMAGE_SCALE));

        m_DensityScreenType = DensityScreenType.DENSITY_HIGH;
        if (density > DisplayMetrics.DENSITY_HIGH) {
            DEFAULT_COMPRESS_QUALITY = 70;
            m_DensityScreenType = DensityScreenType.DENSITY_XHIGH;
        } else if (density == DisplayMetrics.DENSITY_HIGH) {
            DEFAULT_COMPRESS_QUALITY = 70;
            m_DensityScreenType = DensityScreenType.DENSITY_HIGH;
        } else if (density >= DisplayMetrics.DENSITY_MEDIUM)
            m_DensityScreenType = DensityScreenType.DENSITY_MEDIUM;
        else if (density >= DisplayMetrics.DENSITY_LOW)
            m_DensityScreenType = DensityScreenType.DENSITY_LOW;

        m_ScreenResolutionType = ScreenResolutionType.RESOLUTION_HIGH;
        if (screenSize.width > 500)
            m_ScreenResolutionType = ScreenResolutionType.RESOLUTION_XHIGH;
        else if (screenSize.width >= 400)
            m_ScreenResolutionType = ScreenResolutionType.RESOLUTION_HIGH;
        else if (screenSize.width >= 320)
            m_ScreenResolutionType = ScreenResolutionType.RESOLUTION_MEDIUM;
        else if (screenSize.width >= 240 && screenSize.height < 400)
            m_ScreenResolutionType = ScreenResolutionType.RESOLUTION_ULTRA_LOW;
        else if (screenSize.width >= 240)
            m_ScreenResolutionType = ScreenResolutionType.RESOLUTION_LOW;

        Log.i(TAG, "Screen Size: "+screenSize+" density: "+density+
                " Display Metrics: "+dispMetrics+" DensityScreenType: "
                +m_DensityScreenType+" ScreenResolutionType: "+
                m_ScreenResolutionType);

    }

    public static boolean getBoolRes(int resId)
    {
        return Boolean.valueOf(getString(resId)).booleanValue();
    }

    private static int[] getColorOptions(int resId)
    {
        String[] colOpt = getString(resId).split("::");
        int[] colorOptions = new int[colOpt.length];
        for(int i = 0; i<colOpt.length; i++) {
//         XONUtil.logDebugMesg("Parsing Color: "+colOpt[i]);
            colorOptions[i] = Color.parseColor(colOpt[i]);
        }
        return colorOptions;
    }

    private static void populateXONProperty(boolean popFltrRes)
    {
        ThreadPoolSize = getIntRes(R.string.thread_pool_size);
        ThreadSleepTime = getIntRes(R.string.thread_sleep_time);
        ScrollMonitorTime = getIntRes(R.string.scroll_monitor_time);
        IntensityAdjHt = getIntRes(R.string.intensity_adj_ht);
        m_DevelopmentMode = getBoolRes(R.string.DevelopmentMode);
        if (popFltrRes) {
            if (m_ColorOptions == null)
                m_ColorOptions = getColorOptions(R.string.colors);
            if (m_BorderColorOptions == null)
                m_BorderColorOptions = getColorOptions(R.string.border_colors);
            Log.i(TAG, "Num of Color Options: "+m_ColorOptions.length);
            Log.i(TAG, "Num of Border Color Options: "+m_BorderColorOptions.length);
        }
    }

    public static int getMemoryCace()
    {
        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        int cacheSize = Math.round(MEM_CACHE_PERC * XONUtil.getDeviceMemory(m_MainActivity));
        Log.i(TAG, "Cache Size: "+cacheSize);
        return cacheSize;
    }
}
