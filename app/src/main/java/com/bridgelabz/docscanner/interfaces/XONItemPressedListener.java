package com.bridgelabz.docscanner.interfaces;

import android.view.MotionEvent;

/**
 * Created by bridgeit on 28/11/16.
 */

public interface XONItemPressedListener
{
    // Notified when a long press occurs with the initial on down MotionEvent that trigged it.
    public void onLongPress(MotionEvent e, long downTime, int pos);

}
