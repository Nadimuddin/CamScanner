package com.bridgelabz.docscanner.utility;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.bridgelabz.docscanner.interfaces.XONGestureAPI;

/**
 * Created by bridgeit on 25/11/16.
 */

public class XONGestureListener implements GestureDetector.OnGestureListener,
                                            GestureDetector.OnDoubleTapListener{
    public static final String TAG = "XONGestureListener";

    XONGestureAPI m_XONGestureView;

    public XONGestureListener(XONGestureAPI view)
    {
        m_XONGestureView = view;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return m_XONGestureView.onSingleTapEvent(motionEvent);
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return m_XONGestureView.onDoubleTapEvent(motionEvent);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return m_XONGestureView.onDown(motionEvent);
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        m_XONGestureView.onShowPress(motionEvent);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distX, float distY) {
        Log.i(TAG, "DistX: "+distX+" DistY: "+distY);
        // Both the distance in X and Y is the old point minus the new point, hence
        // negative of distance is applied for translation.
        return m_XONGestureView.onScroll(motionEvent, motionEvent1, distX, distY);
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        m_XONGestureView.onLongPress(motionEvent);
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velX, float velY) {
        Log.i(TAG, "velx: "+velX+" velY: "+velY);
        return m_XONGestureView.onFling(motionEvent, motionEvent1, velX, velY);
    }
}
