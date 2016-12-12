package com.bridgelabz.docscanner.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.utility.ImageUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nadimuddin on 19/10/16.
 */

public class DocumentAdapter extends BaseAdapter
{
    private static final String TAG = "DocumentAdapter";
    private ArrayList<Uri> mArrayList;
    private LayoutInflater mInflater;
    public DocumentAdapter(Context context, ArrayList<Uri> arrayList)
    {
        mInflater = LayoutInflater.from(context);
        mArrayList = arrayList;
    }

    @Override
    public int getCount()
    {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        Bitmap bitmap = null, modifiedBitmap;
        if(view == null)
            view = mInflater.inflate(R.layout.document_content, viewGroup, false);

        ImageView imageView = (ImageView)view.findViewById(R.id.page);
        TextView pageNo = (TextView)view.findViewById(R.id.page_number);

        Uri imageUri = mArrayList.get(position);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(view.getContext().getContentResolver(), imageUri);
        } catch (IOException e) { e.printStackTrace(); }

        ImageUtil imageUtil = new ImageUtil();
        modifiedBitmap = imageUtil.getThumbnailImage(bitmap, 150, 200);

        imageView.setImageBitmap(modifiedBitmap);
        pageNo.setText(Integer.toString(position+1));
        return view;
    }
}
