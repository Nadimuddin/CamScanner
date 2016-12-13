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
import com.bridgelabz.docscanner.model.DocumentDetails;
import com.bridgelabz.docscanner.utility.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nadimuddin on 10/10/16.
 */
public class MainAdapter extends BaseAdapter
{
    private static final String TAG = "MainAdapter";
    LayoutInflater mInflater;
    ArrayList<DocumentDetails> mArrayList;
    public MainAdapter(Context context, ArrayList<DocumentDetails> arrayList)
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
    public Object getItem(int i)
    {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        Bitmap bitmap = null, modifiedBitmap;

        if(view == null)
            view = mInflater.inflate(R.layout.list_content, viewGroup, false);

        ImageView coverImage = (ImageView)view.findViewById(R.id.coverImage);
        TextView docName = (TextView)view.findViewById(R.id.docName);
        TextView dateTime = (TextView)view.findViewById(R.id.dateTime);
        TextView imageCount = (TextView)view.findViewById(R.id.imageCount);

        //String uriString = mArrayList.get(i).getImageUri();
        //Uri imageUri = Uri.fromFile(new File(uriString));
        Uri imageUri = mArrayList.get(i).getImageUri();

        try {
            bitmap = MediaStore.Images.Media.getBitmap(view.getContext().getContentResolver(), imageUri);
        } catch (IOException e) { e.printStackTrace(); }

        ImageUtil imageUtil = new ImageUtil();
        modifiedBitmap = imageUtil.getThumbnailImage(bitmap, 100, 70);

        coverImage.setImageBitmap(modifiedBitmap);
        docName.setText(mArrayList.get(i).getDocumentName());
        dateTime.setText(mArrayList.get(i).getDateTime());
        imageCount.setText(Integer.toString(mArrayList.get(i).getImageCount()));

        return view;
    }
}