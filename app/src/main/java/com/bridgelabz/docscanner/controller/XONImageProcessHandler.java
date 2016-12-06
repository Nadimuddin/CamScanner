package com.bridgelabz.docscanner.controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.activities.XON_IM_UI;
import com.bridgelabz.docscanner.fragment.ImageFiltering;
import com.bridgelabz.docscanner.interfaces.XONImageProcessor;
import com.bridgelabz.docscanner.preference.SaveSharedPreference;
import com.bridgelabz.docscanner.utility.StorageUtil;
import com.bridgelabz.docscanner.utility.XONPropertyInfo;
import com.bridgelabz.docscanner.utility.XONUtil;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bridgeit on 26/11/16.
 */

public class XONImageProcessHandler {

    public static final String TAG = "XONImageProcessHandler";
    private static final String KEY_FOR_TEMP_CROPPED_IMAGE = "temp_cropped_image_uri";

    // Set placeholder bitmap that shows when the the background thread is running.
    private Bitmap mLoadingBitmap;
    private static final int FADE_IN_TIME = 200;

    protected Resources mResources;
    private boolean mFadeInBitmap = true;
    private Context mContext;
    private Uri mCroppedImageUri;

    public XONImageProcessHandler(Context context)
    {
        mResources = context.getResources();
        mContext = context;
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn)
    {
        mFadeInBitmap = fadeIn;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId)
    {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    protected class AsyncTaskInvoker extends AsyncTask<Object, Void, Void>
    {
        @Override
        protected Void doInBackground(Object... params)
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) params[0];
            Log.i(TAG, "doInBackground - starting work");
            XONImageProcessor taskProc = (XONImageProcessor) params[1];
            taskProc.processImage(data);
            return null;
        }
    }

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        // An AtomicInteger is used in applications for atomically incremented counters
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    public void processImage(XONImageProcessor imgProc, Map<String, Object> data,
                             ImageView imageView)
    {
        if (imgProc == null) return;
        Log.i(TAG, "Data: "+data);
        final ImageProcessTask task = new ImageProcessTask(imgProc, imageView);
        final AsyncDrawable asyncDrawable =
                new AsyncDrawable(mResources, mLoadingBitmap, task);

        asyncDrawable.setGravity(Gravity.CENTER);
        imageView.setImageDrawable(asyncDrawable);

        Bitmap bitmap = (asyncDrawable).getBitmap();

        storeCroppedImage(bitmap);

        saveUriInPreference();

        // NOTE: This uses a custom version of AsyncTask that has been pulled from the
        // framework and slightly modified. Refer to the docs at the top of the class
        // for more info on what was changed.
        task.executeOnExecutor( Executors.newFixedThreadPool(2, sThreadFactory), data);
    }


    private void storeCroppedImage(Bitmap bitmap)
    {
        StorageUtil storage = new StorageUtil(mContext);
        String directory = storage.getTempDirectory();
        mCroppedImageUri = storage.storeImage(bitmap, directory, "TempImage.jpg");
    }

    private void saveUriInPreference()
    {
        SaveSharedPreference sharedPreference = new SaveSharedPreference(mContext);
        sharedPreference.setPreferences(KEY_FOR_TEMP_CROPPED_IMAGE, mCroppedImageUri.toString());
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap)
    {
        mLoadingBitmap = bitmap;
    }

    public void invokeAsyncTask(XONImageProcessor taskProc, Map<String, Object> data)
    {
        if (taskProc == null) return;
        Log.i(TAG, "Data: "+data);
        AsyncTaskInvoker task = new AsyncTaskInvoker();
        task.execute(data, taskProc);
    }

    @TargetApi(16)
    private void setBackground(ImageView imageView, BitmapDrawable drawable)
    {
        imageView.setBackground(drawable);
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable
    {
        private final WeakReference<ImageProcessTask> imageProcTaskRef;

        public AsyncDrawable(Resources res, Bitmap bitmap, ImageProcessTask imgProcTask)
        {
            super(res, bitmap);
            imageProcTaskRef = new WeakReference<ImageProcessTask>(imgProcTask);
        }

        public ImageProcessTask getImageProcessTask()
        {
            return imageProcTaskRef.get();
        }
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static ImageProcessTask getImageProcessTask(ImageView imageView)
    {
        if (imageView == null) return null;

        final Drawable drawable = imageView.getDrawable();
        if (drawable instanceof AsyncDrawable) {
            final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
            return asyncDrawable.getImageProcessTask();
        }

        return null;
    }

    /**
     * Called when the processing is complete and the final bitmap should be set on the ImageView.
     *
     * @param imageView
     * @param bitmap
     */
    @SuppressWarnings("deprecation")
    private void setImageBitmap(ImageView imageView, Bitmap bitmap)
    {
        if (!mFadeInBitmap)
        {
            imageView.setImageBitmap(bitmap);

            /*storeCroppedImage(bitmap);

            saveUriInPreference();*/

            return;
        }

        // TransitionDrawable is intended to cross-fade between multiple Drawable Arrays.
        // Here it is first with a transparent drawable color and then the final bitmap
        Drawable[] drawable = new Drawable[] {
                new ColorDrawable(android.R.color.transparent),
                new BitmapDrawable(mResources, bitmap) };
        final TransitionDrawable td = new TransitionDrawable(drawable);

        // Set background to loading bitmap
        if (XONUtil.hasJellyBean())
            this.setBackground(imageView, new BitmapDrawable(mResources, mLoadingBitmap));
        else
            imageView.setBackgroundDrawable(new BitmapDrawable(mResources, mLoadingBitmap));
        imageView.setImageDrawable(td);
        // Start the Transition
        td.startTransition(FADE_IN_TIME);
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class ImageProcessTask extends AsyncTask<Object, String , Bitmap>
    {
        private final WeakReference<XONImageProcessor> m_XONImageProcessorRef;
        private final WeakReference<ImageView> imageViewReference;
        ProgressDialog progress = new ProgressDialog(mContext);

        public ImageProcessTask(XONImageProcessor imgProc, ImageView imageView)
        {
            imageViewReference = new WeakReference<>(imageView);
            m_XONImageProcessorRef = new WeakReference<>(imgProc);
        }

        /**
         * Background processing.
         */
        @Override
        protected Bitmap doInBackground(Object... params)
        {
            XONPropertyInfo.activateProgressBar(true);
            XONImageProcessor imgProc = m_XONImageProcessorRef.get();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) params[0];
            Log.i(TAG, "doInBackground - starting work");
            Bitmap bitmap = imgProc.processImage(data);
            Log.i(TAG, "doInBackground - finished work ");

            publishProgress("Cropping image....");

            //progress.setMessage("Cropping image....");
            //progress.show();
            return bitmap;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            /*progress.setMessage(values[0]);
            progress.show();*/
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            final ImageView imageView = getAttachedImageView();
            if (bitmap != null && imageView != null) {
                Log.i(TAG, "onPostExecute - setting bitmap");
                setImageBitmap(imageView, bitmap);
            }
            XONPropertyInfo.activateProgressBar(false);
            progress.dismiss();
        }

        @Override
        protected void onCancelled(Bitmap bitmap)
        {
            super.onCancelled(bitmap);
            XONPropertyInfo.activateProgressBar(false);
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView()
        {
            final ImageView imageView = imageViewReference.get();
            final ImageProcessTask imgProcTask = getImageProcessTask(imageView);
            if (this == imgProcTask) return imageView;
            return null;
        }

    }
}
