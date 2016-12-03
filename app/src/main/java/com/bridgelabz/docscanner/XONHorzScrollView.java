package com.bridgelabz.docscanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

import com.bridgelabz.docscanner.interfaces.XONItemPressedListener;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.AdapterView.OnItemSelectedListener;

//An AdapterView is a view whose children are determined by an Adapter.
public class XONHorzScrollView extends AdapterView<ListAdapter>
{
    public static final String TAG = "XONHorzScrollView";

    // This maintains the reference of the Adapter to get the Views to be displayed in
    // the Horizontal Scroll
    protected ListAdapter m_Adapter;

    // This maintains the number of Views currently added to Horz Scroll
    private int m_NumOfAddedViews = 0;

    // This specifies the Distance actually Scrolled.
    protected int m_ScrolledDistX;

    // Indicates the Distance Scrolled it also includes the distance to be scrolled
    protected int m_TotalScrolledDistX;

    // Indicates the Maximum Distance that can be scrolled
    private int m_MaxScrolledDistX = Integer.MAX_VALUE;

    // This specifies the Distance Actually Scrolled. It is used to position the View in
    // this Horizontal Scroll. Its the negative value of m_ScrolledDistX basically to
    // indicate where the first child view and the corr child view should be placed
    private int m_DisplayOffset = 0;

    // This class is used to enable scrolling of the views in Horx Scroll Avtivity View
    protected Scroller m_Scroller;

    // This captures user finger action like the tap, down, scroll, etc on the screen
    private GestureDetector m_Gesture;

    // This object is invoked when an item in this view has been selected.
    private OnItemSelectedListener m_OnItemSelected;

    // This object is invoked when an item in this view has been clicked.
    private OnItemClickListener m_OnItemClicked;

    // This Object is invoked to pass the Gesture Events
    XONItemPressedListener m_XONItemPressedListener;

    public static String DEBUG_TAG = "XON HORZ SCROLL";

    public XONHorzScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView();
    }

    public static void debugLog(int priority, String logMesg)
    {
        final Throwable t = new Throwable();
        final StackTraceElement[] methodCallers = t.getStackTrace();
        String fname = methodCallers[1].getFileName();
        String mthName = methodCallers[1].getMethodName();
        int index = fname.lastIndexOf('.');
        fname = fname.substring(0,index);
        String mesg = fname + "." + mthName + ": " + logMesg;
        Log.println(priority, DEBUG_TAG, mesg);
    }

    private void initView()
    {
        m_NumOfAddedViews = 0; m_DisplayOffset = 0;
        m_ScrolledDistX = 0; m_TotalScrolledDistX = 0;
        m_MaxScrolledDistX = Integer.MAX_VALUE;
        m_Scroller = new Scroller(getContext());
        m_Gesture = new GestureDetector(getContext(), m_OnGesture);
    }

    // Register a callback to be invoked when an item in this AdapterView has been selected.
    // This method is currently not invoked as the listener is not registered
    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener listener)
    {
        m_OnItemSelected = listener;
    }

    // Register a callback to be invoked when this view is clicked. If this view is not
    // clickable, it becomes clickable.
    // This method is currently not invoked as the listener is not registered
    @Override
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        m_OnItemClicked = listener;
    }

    // This Object is invoked to pass the Gesture Events
    public void setXONItemPressedListener(XONItemPressedListener listener)
    {
        m_XONItemPressedListener = listener;
    }

    @Override
    public View getSelectedView() { return null; }

    @Override
    public void setSelection(int position) {}

    @Override
    public ListAdapter getAdapter() { return m_Adapter; }

    // Sets the adapter that provides the data and the views to represent the data
    // in this widget.
    @Override
    public void setAdapter(ListAdapter adapter)
    {
        if(m_Adapter != null) m_Adapter.unregisterDataSetObserver(m_DataObserver);
        m_Adapter = adapter; m_Adapter.registerDataSetObserver(m_DataObserver);
        initView();

        // This method is called to remove child views from itself. This method is called
        // because only few children are rendered that can currently fit inside the object
        // on screen. Do not call this method unless you are extending ViewGroup and
        // understand the view measuring and layout pipeline.
        removeAllViewsInLayout();

        // This method is called to recreate the Layout after it has been invalidated in the
        // above call.
        // This internally calls the onLayout method to assign size and position -- ????
        // to each of its view.
        requestLayout();
    }

    // Pass the touch screen motion event down to the gesture
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        boolean handled = m_Gesture.onTouchEvent(ev);
        return handled;
    }

    // Called from layout when this view should assign a size and position to each of its children.
    @SuppressLint("DrawAllocation")
    @Override
    protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        if(m_Adapter == null) return;

        Log.i(TAG, "Scroll Status: "+m_Scroller.computeScrollOffset()+
                " ScrollX: "+m_Scroller.getCurrX()+" TotScrDist: "+m_TotalScrolledDistX);
        // Call this when you want to know the new location. True is returned if not finished
        if(m_Scroller.computeScrollOffset()) {
            int scrollx = m_Scroller.getCurrX(); m_TotalScrolledDistX = scrollx;
            Log.i(TAG, "TotScrDist reasigned: "+m_TotalScrolledDistX);
        }
        Log.i(TAG, "TotScrDist: "+m_TotalScrolledDistX);

        if(m_TotalScrolledDistX < 0){
            m_TotalScrolledDistX = 0;
            m_Scroller.forceFinished(true);
        }
        if(m_TotalScrolledDistX > m_MaxScrolledDistX) {
            m_TotalScrolledDistX = m_MaxScrolledDistX;
            m_Scroller.forceFinished(true);
        }

        // dx is the distance this view should scroll. It is actuall dlta btw the actuall
        // scroll pt to the total scroll
        // Negative if moved to right and +ve if scrolled to left
        int dx = m_ScrolledDistX - m_TotalScrolledDistX;
        Log.i(TAG, "TotScrDist: "+m_TotalScrolledDistX+" mCurrentX: "+m_ScrolledDistX+
                " dx: "+dx);
        // Fill List is called only till all the child views are populated.
        if (m_NumOfAddedViews < m_Adapter.getCount()) fillViews(dx);
        positionItems(dx);

        m_ScrolledDistX = m_TotalScrolledDistX;

        // This loop redraws the views to be redrawed till the scrolling is finished. Thus
        // enabling smooth scrolling
        if(!m_Scroller.isFinished()){
            Log.i(TAG, "Not Finished Scrolling");
            // Causes the Runnable to be added to the message queue. The runnable will be
            // run on the user interface thread.
            post(new Runnable(){
                @Override
                public void run() { requestLayout(); }
            });
        }
    }

    // This is the internal method to position the views within this XONHorzScroll widget
    private void positionItems(final int dx)
    {
        if(getChildCount() <= 0) return;
        Log.i(TAG, "dx: "+dx+" DisplayOffset: "+m_DisplayOffset);
        m_DisplayOffset += dx;
        int left = m_DisplayOffset;
        for(int i=0;i<getChildCount();i++){
            View child = getChildAt(i);
            int childWidth = child.getMeasuredWidth(); int right = left+childWidth;
            child.layout(left, 0, right, child.getMeasuredHeight());
            left += childWidth;
        }
    }

    // This method is used only till all the child views are created.
    private void fillViews(final int dx)
    {
        Log.i(TAG, "ChildCount: "+getChildCount());
        int rightEdge = 0;
        // Getting the Last Child
        View lastChild = getChildAt(getChildCount()-1);
        if(lastChild != null) rightEdge = lastChild.getRight();
        Log.i(TAG, "edge: "+rightEdge+" dx: "+dx+" wt: "+getWidth()+
                " RtViewInd: "+m_NumOfAddedViews+" Cnt: "+m_Adapter.getCount());
        while(rightEdge + dx < getWidth() && m_NumOfAddedViews < m_Adapter.getCount())
        {
            View child = m_Adapter.getView(m_NumOfAddedViews, null, this);
            addAndMeasureChild(child, -1);
            rightEdge += child.getMeasuredWidth();

            if(m_NumOfAddedViews == m_Adapter.getCount()-1){
                m_MaxScrolledDistX = m_ScrolledDistX + rightEdge - getWidth();
                Log.i(TAG, "MaxX: "+m_MaxScrolledDistX+" mCurrX: "+m_ScrolledDistX);
            }
            m_NumOfAddedViews++;
            Log.i(TAG, "In While Loop rightEdge: "+rightEdge+
                    " Child Rt Edge: "+child.getRight()+" RtViewInd:"+m_NumOfAddedViews);
        }
        Log.i(TAG, "Aftr fillListRight ChildCount: "+getChildCount());
    }

    // This method is internally called to add View to this XONHorzScroll Widget
    private void addAndMeasureChild(final View child, int viewPos)
    {
        Log.i(TAG, "Child added at viewPos: "+viewPos);
        LayoutParams params = child.getLayoutParams();
        if(params == null) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        addViewInLayout(child, viewPos, params, true);
        child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
    }

    private long m_MotionDownTime = 0;
    // This is internal method called when onDown method was invoked on the Gesture
    protected boolean onDown(MotionEvent e)
    {
        m_MotionDownTime = System.currentTimeMillis();
        int left = getLeft(), top = getTop(), right = getRight(), bot = getBottom();
        Rect rect = new Rect(); rect.set(left, top, right, bot);
        Point pt = new Point(Math.round(e.getX()), Math.round(e.getY()));
        Log.i(TAG, "Layout Rect: "+rect+" Point Clicked: "+pt);

        // This Forces the Scroller to stop and show the corresponding view on which the
        // down gesture occurred.
        m_Scroller.forceFinished(true);
        return true;
    }

    // Scrolls based on fling gesture. The distance travelled is based on the velocity
    // of the fling
    protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                              float velocityY)
    {
        synchronized(XONHorzScrollView.this){
            m_Scroller.fling(m_TotalScrolledDistX, 0, (int)-velocityX, 0, 0,
                    m_MaxScrolledDistX, 0, 0);
        }
        requestLayout();

        return true;
    }

    private OnGestureListener m_OnGesture = new GestureDetector.SimpleOnGestureListener()
    {
        // Notified when a tap occurs with the down MotionEvent that triggered it. This
        // is the first event that is fired whenever any activity happens on the gesture.
        @Override
        public boolean onDown(MotionEvent e)
        {
            // Interestingly, if the onDown() method does not return true, the scroll
            // (or drag)  wont be detected.
            return XONHorzScrollView.this.onDown(e);
        }

        // Notified when a scroll occurs with the initial on down MotionEvent and the
        // current move MotionEvent.
        // A scroll event occurs when the user touches the screen and then moves their
        // finger across it. This gesture is also known as a drag event.
        // distanceX - The distance along the X axis that has been scrolled since the last
        //             call to onScroll. This is NOT the distance between e1 and e2.
        // distanceY - The distance along the Y axis that has been scrolled since the last
        //             call to onScroll. This is NOT the distance between e1 and e2.
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,float distX, float distY)
        {
            Log.i(TAG, "distanceX: "+distX);
            synchronized(XONHorzScrollView.this){
                m_TotalScrolledDistX += (int)distX;
            }
            requestLayout();

            return true;
        }

        // A fling gesture is, essentially, leaving velocity on an item that was being
        // dragged across a screen. The fling gesture is detected only after the users
        // finger is no longer touching the display.
        // velocityX   The velocity of this fling measured in pixels per second along the x axis.
        // velocityY   The velocity of this fling measured in pixels per second along the y axis.
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velX, float velY)
        {
            return XONHorzScrollView.this.onFling(e1, e2, velX, velY);
        }

        // Notified when a long press occurs with the initial on down MotionEvent that trigged it.
        @Override
        public void onLongPress(MotionEvent e)
        {
            int index = getSelChild(e);
            Log.i(TAG, "Child View Selected Index: "+index);
            if (index != -1) m_XONItemPressedListener.onLongPress(e, m_MotionDownTime, index);
        }

        // Notified when a tap occurs with the up MotionEvent that triggered it.
        // Single Tap Up Event is fired first.
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            int index = getSelChild(e);
            if (index != -1) {
                View child = getChildAt(index);
                if(m_OnItemClicked != null){
                    // Callback method to be invoked when an item in this AdapterView has been clicked.
                    m_OnItemClicked.onItemClick(XONHorzScrollView.this, child, index, 0);
                }
                if(m_OnItemSelected != null){
                    // Callback method to be invoked when an item in this AdapterView has been Selected.
                    m_OnItemSelected.onItemSelected(XONHorzScrollView.this, child, index, 0);
                }
            }
            return true;
        }

        public int getSelChild(MotionEvent e)
        {
            Rect viewRect = new Rect();
            for(int i=0;i<getChildCount();i++)
            {
                View child = getChildAt(i);
                int left = child.getLeft();
                int right = child.getRight();
                int top = child.getTop();
                int bottom = child.getBottom();
                viewRect.set(left, top, right, bottom);
                if(viewRect.contains((int)e.getX(), (int)e.getY())) return i;
            }
            return -1;
        }

    }; // private OnGestureListener m_OnGesture = new GestureDetector.SimpleOnGestureListener()

    // DataSetObserver Object Receives call backs when a data set has been changed, or made
    // invalid. The typically data sets that are observed are Cursors or Adapters.
    // DataSetObserver must be implemented by objects which are added to a DataSetObservable.
    private DataSetObserver m_DataObserver = new DataSetObserver()
    {
        // This method is called when the entire data set has changed, most likely through a
        // call to requery() on a Cursor.
        @Override
        public void onChanged() {
            Log.v(DEBUG_TAG, "DataSetObserver onChanged Method");
            super.onChanged();
        }

        // This method is called when the entire data becomes invalid, most likely through a
        // call to deactivate() or close() on a Cursor.
        @Override
        public void onInvalidated() {
            Log.v(DEBUG_TAG, "DataSetObserver onInvalidated Method");
            super.onInvalidated();
        }

    };


}
