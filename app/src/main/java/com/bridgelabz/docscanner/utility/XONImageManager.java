package com.bridgelabz.docscanner.utility;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.XONFullImageActivity;
//import com.bridgelabz.docscanner.activities.XON_Main_UI;
import com.bridgelabz.docscanner.activities.ImageCropping;
import com.bridgelabz.docscanner.controller.TemplateFilterHolder;
import com.bridgelabz.docscanner.controller.XONImageHolder;
import com.bridgelabz.docscanner.interfaces.XONClickListener;
import com.bridgelabz.docscanner.interfaces.XONImageProcessor;
import com.bridgelabz.docscanner.utility.XONImageFilterDef.ImageFilter;
import com.bridgelabz.docscanner.utility.XONImageUtil.BitmapResizeLogic;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Created by bridgeit on 25/11/16.
 */

public class XONImageManager implements XONImageProcessor, XONClickListener{
    public static final String TAG = "XONImageManager";

    private static XONImageManager m_XONImageManager;

    private XONImages m_XONImages;

    @Override
    public void onClick(int actionBut) {

    }

    @Override
    public void onOK(int dialogTitleResId, View customView) {
        IntentUtil.processIntent(XONPropertyInfo.m_SubMainActivity, ImageCropping.class);
    }

    @Override
    public void onCancel(int dialogTitleResId) {

    }

    private enum XONImageProcRequest {
        GetOrigImage, ApplyChangesOnImage, SaveThumbImage, SaveFullImage
    }

    private File m_XONImageSerFile, m_XONImagesDir, m_XONSavedEffectsDir;


    public static XONImageManager getInstance()
    {
        if (m_XONImageManager == null)
            m_XONImageManager = new XONImageManager();
        return m_XONImageManager;
    }

    public void buildTemplateFilters()
    {
        TemplateFilterHolder templFltrHldr = null;
        templFltrHldr = TemplateFilterHolder.getInstance(ImageFilter.
                PersonalizedTemplateFilter);
        templFltrHldr.buildTemplateFilters(m_XONImageManager.m_XONSavedEffectsDir);
    }

    public void serXONImage()
    {
        try {
            XONUtil.serializeObject(m_XONImageSerFile.getAbsolutePath(), m_XONImages);
        } catch (Exception ex) {
            Log.i(TAG, "Unable to serialize XON Image Object", ex);
        }
    }

    public void startFullImageView(int id)
    {
        Log.i(TAG, "Check Activity Status1 : "+XONPropertyInfo.checkActExists());
        final Intent i = new Intent(XONPropertyInfo.m_MainActivity, XONFullImageActivity.class);
        i.putExtra(XONFullImageActivity.EXTRA_IMAGE, (int) id);
        i.putExtra(XONFullImageActivity.Caller, (int) XONFullImageActivity.CALL_FROM_IMAGE_MAKER);
        XONPropertyInfo.activateProgressBarOnUIThread(false);
        XONPropertyInfo.m_MainActivity.startActivity(i);
        Log.i(TAG, "Check Activity Status2: "+XONPropertyInfo.checkActExists());
    }

    @Override
    public Bitmap processImage(Map<String, Object> data) {

        Bitmap bitmap = null, thumbImg = null; int size = 0; XONImageHolder xonImg = null;
        Activity act = XONPropertyInfo.m_MainActivity;
        XONImageProcRequest req = (XONImageProcRequest) data.get("Request");
        try {
            switch(req)
            {
                case ApplyChangesOnImage :
                    break;
                case GetOrigImage :
                    xonImg = (XONImageHolder) data.get("XONImage");
                    data.put("Request", XONImageProcRequest.SaveFullImage);
                    data.put("OrigImg", xonImg.getFinalOrigImg());
                    XONPropertyInfo.processAsyncTack(this, data);
                    Log.i(TAG, "Check Activity Status: "+XONPropertyInfo.checkActExists());
                    break;
                case SaveFullImage :
                    int uniqueNum =m_XONImages.nextCounter();
                    WeakReference<Bitmap> origRefImg = null;
                    xonImg = (XONImageHolder) data.get("XONImage");
                    origRefImg = new WeakReference<Bitmap>((Bitmap) data.remove("OrigImg"));
                    String fName = XONUtil.getFileName(xonImg.m_FilePath), ext = ".png";
                    bitmap = xonImg.getFinalFilteredImage(origRefImg);
                    if (origRefImg.get() != null &&
                            origRefImg.get() != bitmap) origRefImg.get().recycle();
                    origRefImg.clear();
                    Log.i(TAG, "fName: "+fName+" ext: "+ext);
                    File fullImgFile = null;
                    if (fName.contains("_full_"))
                        fullImgFile = new File(m_XONImagesDir, fName+"_"+uniqueNum+ext);
                    else fullImgFile = new File(m_XONImagesDir, fName+"_full_"+uniqueNum+ext);
                    if (!XONImageUtil.saveImage(bitmap, fullImgFile)) return null;
                    size = act.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
                    Log.i(TAG, "ThumbNail Size: "+size);
                    thumbImg = XONImageUtil.getScaledBitmap(bitmap,
                            BitmapResizeLogic.SetMaxSizeBitmap, size, size, true);
                    File thumbImgFile = null;
                    if (fName.contains("_full_")) {
                        String thumbFile = m_XONImages.generatePath(fName, "_full_", "_thumb_");
                        thumbImgFile = new File(m_XONImagesDir, thumbFile+"_"+uniqueNum+ext);
                    } else thumbImgFile = new File(m_XONImagesDir, fName+"_thumb_"+uniqueNum+ext);
                    Log.i(TAG, "Full Img File: "+fullImgFile.getAbsolutePath()+
                            " Thumb Img File: "+thumbImgFile.getAbsolutePath());
                    if (!XONImageUtil.saveImage(thumbImg, thumbImgFile)) return null;
                    m_XONImages.addXONImageData(fullImgFile.getAbsolutePath(),
                            thumbImgFile.getAbsolutePath());
                    serXONImage(); if (bitmap != null) bitmap.recycle(); bitmap = null;
                    if (thumbImg != null) thumbImg.recycle(); thumbImg = null;

                    Log.i(TAG, "Check Activity Status: "+XONPropertyInfo.checkActExists());
                    XONPropertyInfo.activateProgressBarOnUIThread(false);
                    xonImg.resetMemoryCache();
                    startFullImageView(m_XONImages.getCount()-1);
                    break;
                case SaveThumbImage :
                    break;
                default : break;
            }
        } catch (Throwable ex) {
            Log.i(TAG, "Error while saving the Image", ex);
            if (data.containsKey("XONImage")) { xonImg = (XONImageHolder) data.get("XONImage");
                if (xonImg != null) xonImg.resetMemoryCache(); }
            XONPropertyInfo.showErrorAndRoute(R.string.NoImageSaved, this);
        }
        return null;
    }
}
