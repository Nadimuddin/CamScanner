package com.bridgelabz.docscanner.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bridgelabz.docscanner.R;

/**
 * Created by Nadimuddin on 26/10/16.
 */

public class Holder extends RecyclerView.ViewHolder
{
    public ImageButton filterIcon;
    public TextView filterName;
    public Holder(View view)
    {
        super(view);
        filterIcon = (ImageButton)view.findViewById(R.id.filterButton);
        filterName = (TextView)view.findViewById(R.id.filter_name);
    }
}
