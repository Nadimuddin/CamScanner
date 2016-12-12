package com.bridgelabz.docscanner.utility;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Nadimuddin on 18/10/16.
 */

public class StorageUtil
{
    private static final String TAG = "StorageUtil";
    private static final String FOLDER_NAME = "images";
    private Context mContext;

    public StorageUtil(Context context)
    {
        mContext = context;
    }

    public Uri storeImage(Bitmap bitmap, String directory, String imageName)
    {
        /*int id = nextImageID();
        imageName = imageName+id+".png";*/

        File file = new File(directory, imageName);

        if(!file.exists())
            file.mkdirs();

        if(file.exists())
            file.delete();

        try
        {
            OutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            out.flush();
            out.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Uri uriInput = Uri.fromFile(file);
        return uriInput;
    }

    public void deleteImage(String directory)
    {
        File file = new File(directory);

        if(file.exists())
        {
            file.delete();
            Log.i(TAG, "deleteImage: "+directory+" file successfully deleted");
        }
    }

    public String getDirectoryForOriginalImage()
    {
        ContextWrapper wrapper = new ContextWrapper(mContext);

        /* get directory
         * i.e. /data/data/com.bridgelabz.camscannertrail/app_images */
        File directory = wrapper.getDir("original_images", ContextWrapper.MODE_PRIVATE);

        String myDirectory = directory.toString();
        Log.i(TAG, "getDirectoryForOriginalImage: "+myDirectory);
        return myDirectory;
    }

    public String getDirectoryForCroppedImage()
    {
        ContextWrapper wrapper = new ContextWrapper(mContext);

        /* get directory
         * i.e. /data/data/com.bridgelabz.camscannertrail/app_cropped_images */
        File directory = wrapper.getDir("cropped_images", ContextWrapper.MODE_PRIVATE);

        String myDirectory = directory.toString();
        Log.i(TAG, "getDirectoryForCroppedImage: "+myDirectory);
        return myDirectory;
    }

    public String getDirectoryForFilteredImage()
    {
        ContextWrapper wrapper = new ContextWrapper(mContext);

        /* get directory
         * i.e. /data/data/com.bridgelabz.camscannertrail/app_filtered_images */
        File directory = wrapper.getDir("filtered_images", ContextWrapper.MODE_PRIVATE);

        String myDirectory = directory.toString();
        Log.i(TAG, "getDirectoryForFilteredImage: "+myDirectory);
        return myDirectory;
    }

    public String getTempDirectory()
    {
        /*File temp = Environment.getExternalStorageDirectory();
        String directory = temp.getAbsolutePath();*/

        ContextWrapper wrapper = new ContextWrapper(mContext);

        /* get directory
         * i.e. /data/data/com.bridgelabz.camscannertrail/app_temp */
        File file = wrapper.getDir("temp", ContextWrapper.MODE_PRIVATE);

        String directory = file.toString();
        Log.i(TAG, "getTempDirectory: "+directory);
        return directory;
    }

    public File createTemporaryFile()throws Exception
    {
        File tempFile = Environment.getExternalStorageDirectory();
        tempFile = new File(tempFile.getAbsolutePath()+"/temp", "Picture.png");
        if(!tempFile.exists())
            tempFile.mkdirs();
        else if(tempFile.exists())
            tempFile.delete();
        tempFile.createNewFile();

        return tempFile;
    }

    /*public Bitmap compressImage(File image)
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
    }*/
}
