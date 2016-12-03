package com.bridgelabz.docscanner.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.bridgelabz.docscanner.controller.XONImageHolder;
import com.bridgelabz.docscanner.interfaces.XONGestureAPI;
import com.bridgelabz.docscanner.utility.Dimension;
import com.bridgelabz.docscanner.utility.UIUtil;
import com.bridgelabz.docscanner.utility.XONGestureListener;
import com.bridgelabz.docscanner.utility.XONObjectCache;
import com.bridgelabz.docscanner.utility.XONUtil;

import java.util.Vector;

/**
 * Created by bridgeit on 25/11/16.
 */

public class XONCropView extends ImageView implements XONGestureAPI {

    public static final String TAG = "XONCropView";

    // The Main Activity that invoked this view
    Activity m_MainActivity;
    public Vector<Point> m_CropPoints;
    XONImageHolder m_XONImage;
    private GestureDetector m_UserGestures;

    // Size to display the image in the Canvas View
    public Rect m_LayoutRect;
    public Dimension m_LayoutSize;

    // Indicates the Point where the screen gesture started
    private Point m_GestureStartPt, m_SelectedPt;

    // Indicates the Distance Scrolled it also includes the distance to be scrolled
    private int m_TotalScrolledDistX, m_TotalScrolledDistY, m_SelPtIndex;

    public enum ScrollGesture {
        SCROLL_CROP_BOX, SCROLL_RECT_POINT, NO_SCROLL
    }
    private ScrollGesture m_ScrollGesture;

    public XONCropView(Activity act)
    {
        super(act);
        m_MainActivity = act; m_CropPoints = new Vector<>();
        m_XONImage = (XONImageHolder) XONObjectCache.getObjectForKey("XONImage");
        m_UserGestures = new GestureDetector(m_MainActivity, new XONGestureListener(this));
    }

    public void setXONImage(XONImageHolder xonImg)
    {
        Log.i(TAG, "XONImage: "+xonImg);
        m_XONImage = xonImg;
    }

    private void handleScrollGesture(int distX, int distY)
    {
        Rect cropRect = null;
        if (m_ScrollGesture.equals(ScrollGesture.NO_SCROLL)) return;
        if (m_ScrollGesture.equals(ScrollGesture.SCROLL_CROP_BOX)) {
            cropRect = XONUtil.createRect(m_CropPoints, distX, distY);
        } else if (m_ScrollGesture.equals(ScrollGesture.SCROLL_RECT_POINT)) {
            if (m_SelPtIndex == -1) return;
            cropRect = XONUtil.createRect(m_CropPoints, m_SelPtIndex, distX, distY);
        }
        if (!m_XONImage.m_DisplayRect.contains(cropRect)) return;
        m_XONImage.m_CropRect = cropRect;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return m_UserGestures.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
       // super.onDraw(canvas);
        Log.i(TAG, "onDraw: ");
        if (m_XONImage == null || m_XONImage.getDisplayImage() == null) return;
        Bitmap dispImg = m_XONImage.getDisplayImage();
        Matrix dispMat = m_XONImage.m_ScreenCTM;
        canvas.drawBitmap(dispImg, dispMat, null);
        UIUtil.drawRectPath(canvas, m_XONImage.m_CropRect, m_CropPoints, true);
    }

    @Override
    public boolean onSingleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        int x = Math.round(e.getX()), y = Math.round(e.getY());
        m_GestureStartPt = null; m_SelPtIndex = -1;
        m_ScrollGesture = ScrollGesture.NO_SCROLL;
        m_SelPtIndex  = XONUtil.contains(m_CropPoints, x, y);
        if (m_SelPtIndex != -1) {
            m_SelectedPt = m_CropPoints.elementAt(m_SelPtIndex);
            if (m_SelectedPt != null) {
                m_GestureStartPt = new Point(x, y);
                m_TotalScrolledDistX = m_TotalScrolledDistY = 0;
                Log.i(TAG, "Sel Pt: "+m_SelectedPt+" StartPt: "+m_GestureStartPt);
                m_ScrollGesture = ScrollGesture.SCROLL_RECT_POINT;
                return true;
            }
        }
        if (m_XONImage.m_DisplayRect.contains(x, y)) {
            m_ScrollGesture = ScrollGesture.SCROLL_CROP_BOX;
            m_GestureStartPt = new Point(x, y);
            m_TotalScrolledDistX = m_TotalScrolledDistY = 0;
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX, float distY) {
        if (!m_ScrollGesture.equals(ScrollGesture.SCROLL_CROP_BOX) &&
                !m_ScrollGesture.equals(ScrollGesture.SCROLL_RECT_POINT)) return false;
        Log.i(TAG, "SelPtIndex: "+m_SelPtIndex+" distX: "+distX+" distY: "+distY);
        synchronized(this){
            m_TotalScrolledDistX += (int)-distX;
            m_TotalScrolledDistY += (int)-distY;
            handleScrollGesture((int)-distX, (int)-distY);
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velX, float velY) {
        return false;
    }
}