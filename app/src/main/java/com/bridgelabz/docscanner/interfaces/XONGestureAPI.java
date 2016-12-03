package com.bridgelabz.docscanner.interfaces;

import android.view.MotionEvent;

/**
 * Created by bridgeit on 25/11/16.
 */

public interface XONGestureAPI {

    // Notified when a single-tap occurs. This method is called when Single Tap is confirmed.
    public boolean onSingleTapEvent(MotionEvent e);

    // Notified when an event within a double-tap gesture occurs, including the down,
    // move, and up events. This method is called when Double Tap is confirmed.
    public boolean onDoubleTapEvent(MotionEvent e);

    // Notified when a tap occurs with the down MotionEvent that triggered it. This
    // is the first event that is fired whenever any activity happens on the gesture.
    public boolean onDown(MotionEvent e);

    // Notified when a long press occurs with the initial on down MotionEvent that
    // trigged it.
    public void onLongPress(MotionEvent e);

    // This method is fired when the user has performed a down MotionEvent and not
    // performed a move or up yet. This event is fired just before the scroll is done.
    public void onShowPress(MotionEvent e);

    // Notified when a scroll occurs with the initial on down MotionEvent and the
    // current move MotionEvent.
    // A scroll event occurs when the user touches the screen and then moves their
    // finger across it. This gesture is also known as a drag event.
    // distX - The distance along the X axis that has been scrolled since the last
    //         call to onScroll. This is NOT the distance between e1 and e2.
    // distY - The distance along the Y axis that has been scrolled since the last
    //         call to onScroll. This is NOT the distance between e1 and e2.
    // NOTE  - Both the distance in X and Y is the old point minus the new point, hence
    //         negative of distance is applied for translation.
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX, float distY);

    // A fling gesture is, essentially, leaving velocity on an item that was being
    // dragged across a screen. The fling gesture is detected only after the users
    // finger is no longer touching the display. This mthd is used for animation.
    // velX   The velocity of this fling measured in pixels per second along the x axis.
    // velY   The velocity of this fling measured in pixels per second along the y axis.
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velX, float velY);
}
