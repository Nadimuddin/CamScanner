package com.bridgelabz.docscanner.async_task;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.bridgelabz.docscanner.utility.StorageUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by bridgeit on 15/12/16.
 */

public class SaveToGallery extends AsyncTask<Void, Void, String >
{
    private Context mContext;
    private SimpleDateFormat mFormat;
    private ArrayList<Integer> mSelectedItems;
    private ArrayList<Uri> mUris;
    private StorageUtil mStorage;
    String mDirectory;
    public SaveToGallery(Context context, ArrayList<Integer> selectedItems, ArrayList<Uri> uris)
    {
        mStorage = new StorageUtil(context);
        mContext = context;

        mSelectedItems = selectedItems;
        mUris = uris;

        mDirectory = mStorage.getPublicDirectory();
        mFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    }

    @Override
    protected String doInBackground(Void... voids)
    {
        for(int i=0; i<mSelectedItems.size(); i++)
        {
            String timeStamp = mFormat.format(new Date());
            String imageName = "Doc_"+timeStamp+i;
            int pos = mSelectedItems.get(i);
            Uri uri = mUris.get(pos);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mStorage.storeImage(bitmap, mDirectory, imageName+".jpg");
        }
        return null;
    }
}
