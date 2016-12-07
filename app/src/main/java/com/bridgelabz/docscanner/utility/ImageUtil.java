package com.bridgelabz.docscanner.utility;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.bridgelabz.docscanner.activities.MainActivity;
//import com.soundcloud.android.crop.Crop;

import java.io.File;

/**
 * Created by Nadimuddin on 19/10/16.
 */

public class ImageUtil extends Activity
{
    private Activity mActivity;
    private static final int RESULT_OK = -1;

    public ImageUtil()
    {
    }

    public ImageUtil(Activity activity)
    {
        mActivity = activity;
    }

    public static final String TAG = "ImageUtil";
    public void beginCrop(Uri source)
    {
        int id = nextImageID("Images");
        String imageName = "CamScannerImage"+id;
        Uri destination = Uri.fromFile(new File(mActivity.getDir("images", MainActivity.MODE_PRIVATE), imageName));
        Log.i(TAG, "beginCrop: destination image: "+destination.toString());
        //Crop.of(source, destination).asSquare().start(mActivity);
    }

   /* public Uri handleCrop(int resultCode, Intent result)
    {
        Uri imageUri = null;

        if (resultCode == RESULT_OK)
            imageUri = Crop.getOutput(result);

        else if (resultCode == Crop.RESULT_ERROR)
            Toast.makeText(mActivity, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();

        return imageUri;
    }*/
    public int nextImageID(String tableName)
    {
        int id;
        DatabaseUtil database = new DatabaseUtil(mActivity, tableName);

        id = database.getLastID(tableName);

        return id+1;
    }

    public Bitmap modifyBitmap(Bitmap bitmap, int imageViewWidth, int imageViewHeight)
    {
        Bitmap modifiedBitmap;
        int x, y, width, height;
        int bitmapWidth = bitmap.getWidth(), bitmapHeight = bitmap.getHeight();

        if(bitmapWidth <= imageViewWidth) {
            x = 0;
            width = bitmapWidth;
        }
        else {
            x = (bitmapWidth - imageViewWidth)/2;
            width = imageViewWidth;
        }

        if(bitmapHeight <= imageViewHeight) {
            y = 0;
            height = bitmapHeight;
        }
        else {
            y = (bitmapHeight - imageViewHeight)/2;
            height = imageViewHeight;
        }

        //Log.i(TAG, "modifyBitmap: "+(x+imageViewWidth) +" <= "+ bitmap.getWidth());

        modifiedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
        int w = modifiedBitmap.getWidth();
        int h = modifiedBitmap.getHeight();
        return modifiedBitmap;
    }

    public Bitmap grabImage(File image)
    {
        Bitmap bitmap = null;

        if(image.exists())
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), options);
            //imageUri = storeImage(bitmap);

        }
        return bitmap;
    }
}