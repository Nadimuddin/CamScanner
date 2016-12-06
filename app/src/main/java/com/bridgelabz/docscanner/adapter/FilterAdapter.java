package com.bridgelabz.docscanner.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.holder.Holder;
import com.bridgelabz.docscanner.interfaces.FilterImage;
import com.bridgelabz.docscanner.model.FilterModel;
import com.bridgelabz.docscanner.preference.SaveSharedPreference;

import java.util.ArrayList;

/**
 * Created by Nadimuddin on 26/10/16.
 */

public class FilterAdapter extends RecyclerView.Adapter
{
    private ArrayList<FilterModel> mArrayList;
    private Context mContext;
    private FilterImage mFilter;
    private int mPreviousPosition = -1;
    private Holder mPreviousHolder;
    private static final int BLACK = 0;
    private static final int WHITE = 1;
    private static final String LAST_USED_FILTER = "last_used_filter";

    public FilterAdapter(ArrayList<FilterModel> arrayList, FilterImage filter)
    {
        mArrayList = arrayList;
        mFilter = filter;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.filter_button, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position)
    {

        final Holder holder = (Holder)viewHolder;
        final FilterModel filterModel = mArrayList.get(position);
        holder.filterName.setText(filterModel.getFilterName());

        SaveSharedPreference sharedPreference = new SaveSharedPreference(holder.filterIcon.getContext());
        String lastUsedFilter = sharedPreference.getPreference(LAST_USED_FILTER);


        if(lastUsedFilter == null && filterModel.getFilterName().equals("Auto"))
            processFilter(position, filterModel, holder);

        else if(lastUsedFilter.equals(filterModel.getFilterName()))
            processFilter(position, filterModel, holder);

        else
            changeFilterButtonColor(filterModel, holder, BLACK);

        holder.filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ProgressDialog progress = new ProgressDialog(holder.filterIcon.getContext());
                progress.setMessage("Applying effects...");
                progress.show();
                progress.isIndeterminate();
                processFilter(position, filterModel, holder);
                progress.dismiss();
            }
        });

        holder.filterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                processFilter(position, filterModel, holder);
            }
        });
    }

    private void processFilter(int position, FilterModel filterModel, Holder holder)
    {
        SaveSharedPreference sharedPreference = new SaveSharedPreference(holder.filterIcon.getContext());
        sharedPreference.setPreferences(LAST_USED_FILTER, filterModel.getFilterName());
        mFilter.applyChangesToImage(position+1);

        changeFilterButtonColor(filterModel, holder,WHITE);

        if(mPreviousHolder != null && mPreviousPosition != -1)
        {
            if(mPreviousPosition != position)
            {
                changeFilterButtonColor(mArrayList.get(mPreviousPosition), mPreviousHolder, BLACK);
            }
        }

        mPreviousHolder = holder;
        mPreviousPosition = position;
    }

    private Bitmap getBitmapIcon(FilterModel filterModel, String blackOrWhite)
    {
        Drawable drawable = null;
        if(blackOrWhite.equals("black")) {
            drawable = mContext.getResources().getDrawable(filterModel.getFilterIconIdBlack());
        }
        else if(blackOrWhite.equals("white")) {
            drawable = mContext.getResources().getDrawable(filterModel.getFilterIconIdWhite());
        }
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        return bitmap;
    }

    private Bitmap changeFilterButtonColor(FilterModel filterModel, Holder holder, int blackOrWhite)
    {
        Drawable drawable;
        Bitmap bitmap = null;
        int color = 0;
        if(blackOrWhite == BLACK)
        {
            drawable = mContext.getResources().getDrawable(filterModel.getFilterIconIdBlack());
            bitmap = ((BitmapDrawable)drawable).getBitmap();
            color = Color.BLACK;
        }
        else if(blackOrWhite == WHITE)
        {
            drawable = mContext.getResources().getDrawable(filterModel.getFilterIconIdWhite());
            bitmap = ((BitmapDrawable)drawable).getBitmap();
            color = Color.WHITE;
        }
        //Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        holder.filterIcon.setImageBitmap(bitmap);
        holder.filterName.setTextColor(color);
        return bitmap;
    }

    @Override
    public int getItemCount()
    {
        return mArrayList.size();
    }

    /*@Override
    public void onClick(View v)
    {

    }*/
}
