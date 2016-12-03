package com.bridgelabz.docscanner.interfaces;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by bridgeit on 28/11/16.
 */

public interface XONUIAdapterInterface {

    // How many items are in the data set represented by this Adapter.
    public int getCount();

    // Get the data item associated with the specified position in the data set.
    public Object getItem(int position);

    // Get the row id associated with the specified position in the list.
    public long getItemId(int position);

    // Get a View that displays the data at the specified position in the data set.
    // You can either create a View manually or inflate it from an XML layout file.
    public View getView(int position);

    // This method is invoked when an item in this view has been selected. This is invoked
    // only when the newly selected position is different from the previously selected one.
    // parent - The AdapterView where the selection happened
    // view - The view within the AdapterView that was clicked
    // position - The position of the view in the adapter
    // rowId - The row id of the item that is selected
    public void onItemSelected(AdapterView<?> parent, View view, int position, long rowId);

    // This method is invoked when the selection disappears from this view. The selection
    // can disappear for instance when touch is activated or when the adapter becomes empty.
    public void onNothingSelected(AdapterView<?> parent);

    // This method is invoked when an item in this AdapterView has been clicked.
    // Implementers can call getItemAtPosition(position) if they need to access the data
    // associated with the selected item.
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId);

    // Notified when a long press occurs with the initial on down MotionEvent that trigged it.
    public void onLongPress(MotionEvent e, long downTime, int pos);
}
