package com.bridgelabz.docscanner;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.bridgelabz.docscanner.interfaces.XONItemPressedListener;
import com.bridgelabz.docscanner.interfaces.XONUIAdapterInterface;

// Adapter that creates List View. This adapter creates each View to be displayed
// in the Horizontal Lists.
// OnItemClickListener Interface is implemented for a callback to be invoked when
// a view has been clicked.
// OnItemSelectedListener Interface is implemented for a callback to be invoked when
// a view has been selected.
public class XONHorzListAdapter extends BaseAdapter
        implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener,
        XONItemPressedListener
{
    XONUIAdapterInterface m_XONUIAdapter;
    XONHorzScrollView m_XONHorzScrollView;

    public XONHorzListAdapter(XONUIAdapterInterface xonUIAdapter, XONHorzScrollView listview)
    {
        m_XONUIAdapter = xonUIAdapter;
        m_XONHorzScrollView = listview;
        m_XONHorzScrollView.setAdapter(this);
        m_XONHorzScrollView.setXONItemPressedListener(this);
        m_XONHorzScrollView.setOnItemSelectedListener(this);
        m_XONHorzScrollView.setOnItemClickListener(this);
    }

    public void reset()
    {
        m_XONHorzScrollView.setAdapter(this);
    }

    public boolean viewExists(int position)
    {
        int cnt = m_XONUIAdapter.getCount();
        if (position >= 0 && position < cnt) return true;
        return false;
    }

    @Override
    public int getCount() { return m_XONUIAdapter.getCount(); }

    @Override
    public Object getItem(int position)
    {
        if (!viewExists(position)) return null;
        return m_XONUIAdapter.getItem(position);
    }

    @Override
    public long getItemId(int position)
    {
        if (!viewExists(position)) return 0;
        return m_XONUIAdapter.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup paren)
    {
        if (!viewExists(position)) return null;
        return m_XONUIAdapter.getView(position);
    }

    // This method is invoked when an item in this view has been selected. This is invoked
    // only when the newly selected position is different from the previously selected one.
    // parent - The AdapterView where the selection happened
    // view - The view within the AdapterView that was clicked
    // position - The position of the view in the adapter
    // rowId - The row id of the item that is selected
    public void onItemSelected(AdapterView<?> parent, View view, int position, long rowId)
    {
        m_XONUIAdapter.onItemSelected(parent, view, position, rowId);
    }

    // This method is invoked when the selection disappears from this view. The selection
    // can disappear for instance when touch is activated or when the adapter becomes empty.
    public void onNothingSelected(AdapterView<?> parent)
    {
        m_XONUIAdapter.onNothingSelected(parent);
    }

    // This method is invoked when an item in this AdapterView has been clicked.
    // Implementers can call getItemAtPosition(position) if they need to access the data
    // associated with the selected item.
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId)
    {
        m_XONUIAdapter.onItemClick(parent, view, position, rowId);
    }

    @Override
    public void onLongPress(MotionEvent e, long downTime, int pos)
    {
        m_XONUIAdapter.onLongPress(e, downTime, pos);
    }
}
