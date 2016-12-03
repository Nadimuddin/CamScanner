/*
package com.bridgelabz.docscanner.custom_view;

*/
/**
 * Created by Nadimuddin on 28/10/16.
 *//*


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DragRectView extends View
{
    public static final String TAG = "DragRectView";

    private Paint mRectPaint;
    private Context mContext;

    private int mStartX = 0;
    private int mStartY = 0;
    private int mEndX = 0;
    private int mEndY = 0;

    private int mCircleX = 100;
    private int mCircleY = 300;
    private boolean mDrawRect = false;
    private TextPaint mTextPaint = null;

    private OnUpCallback mCallback = null;

    public interface OnUpCallback {
        void onRectFinished(Rect rect);
    }

    public DragRectView(final Context context) {
        super(context);
        init();
        invalidate();
    }

    public DragRectView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
        mContext = context;
    }

    public DragRectView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
        Log.i(TAG, "DragRectView: ");
        init();
    }

    */
/**
     * Sets callback for up
     *
     * @param callback {@link OnUpCallback}
     *//*

    public void setOnUpCallback(OnUpCallback callback) {
        mCallback = callback;
    }

    */
/**
     * Init internal data
     *//*

    private void init() {
        mRectPaint = new Paint();
        mRectPaint.setColor(getContext().getResources().getColor(android.R.color.holo_green_light));
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setStrokeWidth(3); // TODO: should take from resources

        mTextPaint = new TextPaint();
        mTextPaint.setColor(getContext().getResources().getColor(android.R.color.holo_green_light));
        mTextPaint.setTextSize(20);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        // TODO: be aware of multi-touches
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mDrawRect = false;
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                TouchPoint touchPoint = new TouchPoint(mStartX, mStartY);
                CircleArea circleArea = new CircleArea(100, 300, 10);
                if(intersect(touchPoint, circleArea));
                    invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                final int x = (int) event.getX();
                final int y = (int) event.getY();

                mCircleX = x;
                mCircleY = y;


                if (!mDrawRect || Math.abs(x - mEndX) > 5 || Math.abs(y - mEndY) > 5) {
                    mEndX = x;
                    mEndY = y;
                    invalidate();
                }

                mDrawRect = true;
                break;

            case MotionEvent.ACTION_UP:
                if (mCallback != null)
                {
                    mCallback.onRectFinished(new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                            Math.max(mEndX, mStartX), Math.max(mEndY, mStartX)));
                }
                invalidate();
                break;

            default:
                break;
        }

        return true;
    }

    private boolean intersect(TouchPoint touchPoint, CircleArea area)
    {
        int X = touchPoint.mX;
        int Y = touchPoint.mY;
        if(X >= area.point1.mX && X <= area.point2.mX && X <= area.point3.mX && X >= area.point4.mX
                && Y >= area.point1.mY && Y <= area.point4.mY && Y >= area.point2.mY && Y <= area.point3.mY)
        {
            Log.i(TAG, "intersect: clicked on circle");
            Toast.makeText(mContext, "You touch on circle", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(mCircleX, mCircleY, 500, 300, mRectPaint);
        canvas.drawLine(500, 300, 500, 800, mRectPaint);
        canvas.drawLine(500, 800, 100, 800, mRectPaint);
        canvas.drawLine(100, 800, mCircleX, mCircleY, mRectPaint);

        canvas.drawCircle(mCircleX, mCircleY, 30, mRectPaint);
        */
/*if (mDrawRect) {
            canvas.drawRect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                    Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mRectPaint);
            canvas.drawText("  (" + Math.abs(mStartX - mEndX) + ", " + Math.abs(mStartY - mEndY) + ")",
                    Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mTextPaint);
        }*//*

    }

    class TouchPoint
    {
        int mX, mY;
        TouchPoint(int x, int y)
        {
            mX = x;
            mY = y;
        }
    }

    class CircleArea
    {
        Point point1, point2, point3, point4;
        CircleArea(int x, int y, int radius)
        {
            point1 = new Point(x-radius, y-radius);
            point2 = new Point(x+radius, y-radius);
            point3 = new Point(x+radius, y+radius);
            point4 = new Point(x-radius, y+radius);
        }
    }

    class Point
    {
        int mX, mY;
        Point(int x, int y)
        {
            mX = x;
            mY = y;
        }
    }
}
*/
