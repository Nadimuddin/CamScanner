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
import com.bridgelabz.docscanner.model.PageDetail;
import com.bridgelabz.docscanner.utility.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nadimuddin on 19/10/16.
 */

public class DocumentAdapter extends BaseAdapter
{
    private static final String TAG = "DocumentAdapter";
    private ArrayList<PageDetail> mArrayList;
    private LayoutInflater mInflater;
    public DocumentAdapter(Context context, ArrayList<PageDetail> arrayList)
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
        Bitmap bitmap = null, thumbnail;
        if(view == null)
            view = mInflater.inflate(R.layout.document_content, viewGroup, false);

        ImageView imageView = (ImageView)view.findViewById(R.id.page);
        TextView pageNo = (TextView)view.findViewById(R.id.page_number);

        PageDetail pageDetail = mArrayList.get(position);
        String uriString = pageDetail.getImageUri();
        Uri imageUri = Uri.fromFile(new File(uriString));
        String pageName = pageDetail.getPageName();
        try {
            bitmap = MediaStore.Images.Media.getBitmap(view.getContext().getContentResolver(), imageUri);
        } catch (IOException e) { e.printStackTrace(); }

        ImageUtil imageUtil = new ImageUtil();
        thumbnail = imageUtil.getThumbnailImage(bitmap, 150, 200);

        imageView.setImageBitmap(thumbnail);
        pageNo.setText(Integer.toString(position+1)+"   "+pageName);
        return view;
    }
}
