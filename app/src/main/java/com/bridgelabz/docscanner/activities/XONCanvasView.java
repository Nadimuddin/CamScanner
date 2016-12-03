package com.bridgelabz.docscanner.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.GestureDetector;
import android.widget.ImageView;

import com.bridgelabz.docscanner.XONImageFilterView;
import com.bridgelabz.docscanner.controller.XONImageHolder;
import com.bridgelabz.docscanner.controller.XONImageProcessHandler;
import com.bridgelabz.docscanner.interfaces.ThreadInvokerMethod;
import com.bridgelabz.docscanner.utility.ThreadCreator;
import com.bridgelabz.docscanner.utility.XONPropertyInfo;
import com.bridgelabz.docscanner.utility.XONPropertyInfo.ImageOrientation;
import com.bridgelabz.docscanner.utility.XONUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bridgeit on 25/11/16.
 */

public class XONCanvasView extends ImageView implements ThreadInvokerMethod{
    public static final String TAG = "XONCanvasView";
    XONImageHolder m_XONImage;

    // Indicates the Point where the screen gesture started
    private Point m_GestureStartPt;
    private long m_ScrollAccessTime;

    // The Main Activity that invoked this view
    Activity m_MainActivity;
    private GestureDetector m_UserGestures;

    private enum ScrollStatus {
        Started, InProgress, Finished
    }
    private ScrollStatus m_ScrollStatus;

    WeakReference<Bitmap> m_CanvasDisplayImage;
    WeakReference<Bitmap> m_CanvasFilteredImage;

    XONImageProcessHandler m_XONImageProcessHandler;
    float[] m_CanvasDisplayCTM;

    // This holds the Layout Position and Size of the Canvas View
    public Rect m_LayoutRect;

    public XONCanvasView(Context context) {
        super(context);
    }

    public void setXONImage(XONImageHolder xonImg)
    {
        Log.i(TAG, "XONImage using Util: "+xonImg);
        m_XONImage = xonImg; resetMemParams();
    }

    public void resetCache()
    {
        this.setImageDrawable(null);
        m_CanvasDisplayImage = null; m_CanvasFilteredImage = null; m_MainActivity = null;
        m_UserGestures = null; m_XONImageProcessHandler = null; m_CanvasDisplayCTM = null;
    }

    public void resetMemParams()
    {
        m_ScrollStatus = null; m_CanvasDisplayImage = null; m_CanvasFilteredImage = null;
    }

    XONImageFilterView m_XONImageFilterView;
    public void setImageFilterScrollView(XONImageFilterView imgFltrScrollView)
    {
        m_XONImageFilterView = imgFltrScrollView;
    }





    public void showOriginalImage()
    {
        m_XONImage.resetDisplayImage(true);
        handleScrollGesture(null, AsyncTaskReq.ShowImageInCanvas);
    }

    private enum AsyncTaskReq {
        ApplyFilterOnCanvasView, ShowFilteredImageInCanvas,
        ShowImageInCanvas, ReApplyFilterOnCanvasView
    }

    private enum ThreadProcessReq {
        ProcessFilterUpdate, ProcessFilterMonitor
    }
    private void handleScrollGesture(Point endPt, AsyncTaskReq asyncTaskReq)
    {
        Map<String, Object> requestData = new HashMap<String, Object>();
        requestData.put("ThreadProcessReq", ThreadProcessReq.ProcessFilterUpdate);
        requestData.put("AsyncTaskReq", asyncTaskReq);
        if (endPt != null) requestData.put("EndPt", endPt);
        Log.i(TAG, "Request Data: "+requestData);
        ThreadCreator.getInstance().createThread(this, requestData, true);
    }

    private Bitmap getCanvasFilterImage()
    {
        if (m_CanvasFilteredImage == null) return null;
        return m_CanvasFilteredImage.get();
    }

    private Bitmap getCanvasDisplayImage()
    {
        if (m_CanvasDisplayImage == null) return null;
        return m_CanvasDisplayImage.get();
    }

    private boolean isScrollActive()
    {
        int scrollMonTime = XONPropertyInfo.ScrollMonitorTime;
        if (m_ScrollStatus != null && m_ScrollStatus == ScrollStatus.InProgress &&
                XONUtil.getCurrentTime()-m_ScrollAccessTime <= scrollMonTime) return true;
        return false;
    }

    @Override
    public void processThreadRequest(Map<String, Object> requestData) throws Exception {
        ThreadProcessReq reqId = ThreadProcessReq.ProcessFilterMonitor;
        if (requestData != null) reqId = (ThreadProcessReq) requestData.get("ThreadProcessReq");
        Log.i(TAG, "Request: "+reqId+" Scroll Status: "+m_ScrollStatus);
        switch(reqId)
        {
            case ProcessFilterUpdate:
                final ImageOrientation imgOrient = (ImageOrientation)
                        requestData.get("ImageOrientation");
                final AsyncTaskReq asyncTaskReq = (AsyncTaskReq) requestData.get("AsyncTaskReq");
                if (requestData.containsKey("EndPt")) {
                    Point endPt = (Point) requestData.get("EndPt");
                    m_XONImage.setImageFilterParam(m_GestureStartPt, endPt);
                }
                m_MainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = null;
                        switch(asyncTaskReq)
                        {
                            case ApplyFilterOnCanvasView :
                                if (getCanvasFilterImage() == null) {
                                    Log.i(TAG, "Canvas Filter Image is null");
                                    return;
                                }
                                if (getCanvasDisplayImage() != null){
                                    if (!getCanvasDisplayImage().equals(getCanvasFilterImage())) {
                                        getCanvasDisplayImage().recycle();
                                        m_CanvasDisplayImage.clear();
                                    }
                                }
                                bitmap = m_XONImage.applyLastFilter(getCanvasFilterImage());
                                m_CanvasDisplayImage = new WeakReference<Bitmap>(bitmap);
                                break;
                            case ShowFilteredImageInCanvas:
                                m_XONImage.applyLastFilter();
                                XONPropertyInfo.activateProgressBar(false);
                                invalidate();
                            case ReApplyFilterOnCanvasView:
                                if (imgOrient != null) {
                                    m_XONImage.setOrientation(imgOrient);
                                    m_XONImage.applyFilter();
                                    XONPropertyInfo.activateProgressBar(false);
                                    m_XONImageFilterView.resetView();
                                    invalidate();
                                }
                                break;
                            case ShowImageInCanvas:
                                bitmap = m_XONImage.getDisplayImage();
                            default : bitmap = m_XONImage.getDisplayImage();
                        }
                        XONCanvasView.this.invalidate();
                    }
                });
                break;
            case ProcessFilterMonitor :
                boolean applyFilterUpdate = false;
                int scrollMonTime = XONPropertyInfo.ScrollMonitorTime;
                if (m_ScrollStatus == null) return;
                if (m_ScrollStatus == ScrollStatus.Finished) applyFilterUpdate = true;
                else if (m_ScrollStatus == ScrollStatus.InProgress &&
                        XONUtil.getCurrentTime()-m_ScrollAccessTime >= scrollMonTime)
                    applyFilterUpdate = true;
                if (!applyFilterUpdate) { XONUtil.put2Sleep();
                    processThreadRequest(requestData); }
                else {
                    Log.i(TAG, "Applying Filter on Original Image");
                    m_MainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isScrollActive()) {
                                XONUtil.put2Sleep();
                                try { processThreadRequest(null); } catch(Exception ex) {}
                            } else {
                                m_ScrollStatus = null; m_CanvasDisplayImage = null;
                                m_XONImage.applyLastFilter(); XONCanvasView.this.invalidate();
                                Log.i(TAG, "The Monitor Thread has finished and returned");
                            }
                        }
                    });
                }
                break;
            default : break;
        }
    }
}
