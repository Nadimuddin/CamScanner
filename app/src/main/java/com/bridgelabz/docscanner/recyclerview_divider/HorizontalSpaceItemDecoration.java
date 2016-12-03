package com.bridgelabz.docscanner.recyclerview_divider;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Nadimuddin on 18/11/16.
 */

public class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int mHorizontalSpace;
    private final int mSpaceAtStartEnd = 50;

    public HorizontalSpaceItemDecoration(int horizontalSpace) {
        mHorizontalSpace = horizontalSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        int spaceAtStartEnd = 50;
        int position = parent.getChildAdapterPosition(view);
        //if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1)
        if(position == 0)
            outRect.left = spaceAtStartEnd;
        if(position != parent.getAdapter().getItemCount()-1)
            outRect.right = mHorizontalSpace;
        else
            outRect.right = spaceAtStartEnd;
    }
}
