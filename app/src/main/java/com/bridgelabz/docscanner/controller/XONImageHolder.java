package com.bridgelabz.docscanner.controller;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.activities.XON_Main_UI;
import com.bridgelabz.docscanner.interfaces.ImageFilterHandler;
import com.bridgelabz.docscanner.interfaces.XONClickListener;
import com.bridgelabz.docscanner.utility.Dimension;
import com.bridgelabz.docscanner.utility.IntentUtil;
import com.bridgelabz.docscanner.utility.ScalingFactor;
import com.bridgelabz.docscanner.utility.XONImageUtil;
import com.bridgelabz.docscanner.utility.XONImageUtil.BitmapResizeLogic;
import com.bridgelabz.docscanner.utility.XONObjectCache;
import com.bridgelabz.docscanner.utility.XONPropertyInfo;
import com.bridgelabz.docscanner.utility.XONPropertyInfo.ImageOrientation;
import com.bridgelabz.docscanner.utility.XONUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by bridgeit on 25/11/16.
 */

public class XONImageHolder implements XONClickListener{

    public static final String TAG = "XONImageHolder";
    public Uri m_Uri;

    // This matrix is used to draw the image in the screen
    public Matrix m_ScreenCTM;
    public transient float[] m_ScreenCTMValues;

    // This Matrix is used to convert the screen pts to the image pts
    public Matrix m_ScreenToImageCTM;

    // The Scaled Image Rect displayed on the screen
    public Rect m_CropDisplayRect, m_DisplayRect, m_IntensityAdjRect;

    // This are crop rectangles on the selected and the original image
    public Rect m_CropRect, m_ApplCropRect, m_OrigCropRect;

    private transient String m_ImageFilterParamName;
    private transient ImageFilterHandler m_ImageFilterHandler;

    // The Screen Dimension where this Image is displayed
    public Dimension m_PanelSize, m_ThumbSize;

    public ImageOrientation m_ImageOrientation;
    public transient Map<String, Bitmap> m_ThumbFilterImage;

    // Current Active ImageFilter Params
    private transient Map<String, Object> m_ImageFilterParamVal;

    // Current Active ImageFilter and its parameter being executed
    private transient String m_ImageFilter;

    // This is the scaled image.
    public Bitmap m_ThumbviewImage;

    public int m_AvgImagePixel;
    int m_RetryCounter = 0;

    // ImageFilterController Object to apply Image Filters on the Image Object
    private ImageFilterController m_ImageFilterController;

    public boolean m_ShowOriginalImage = false;

    private LruCache<String, Bitmap> m_ImageMemoryCache;

    public enum ImageType { Potrait, Lanscape, Square }
    public ImageType m_ImageType;

    private enum CompressionLogic { UseDefCompression, UsePanelSize, CompressToSize,
        UseMaxSize, CalcMaxSize, NoCompression }

    @Override
    public void onClick(int actionBut) {

    }

    @Override
    public void onOK(int dialogTitleResId, View customView) {
        IntentUtil.processIntent(XONPropertyInfo.m_SubMainActivity, XON_Main_UI.class);
        XONPropertyInfo.m_SubMainActivity.finish();
    }

    @Override
    public void onCancel(int dialogTitleResId) {

    }

    public boolean isImageFilterActive(String imgFltr)
    {
        ImageFilterController fltrCtrl = m_ImageFilterController;
        ImageFilterHandler imgFltrHdlr = fltrCtrl.getImageFilterHandler(imgFltr);
        Log.i(TAG, "ImageFltr: "+imgFltr+" Hdlr: "+imgFltrHdlr);
        if (imgFltrHdlr != null) return true;
        return false;
    }

    // Main Image Sources
    public enum ImageSource {
        UseBitmap, UseFileDescriptor, UseResource, UseFile, UseInputStream, UseUri
    }
    public ImageSource m_ImageSource;

    // Original and Selected Image Dimension
    Dimension m_OrigImageSize, m_ReSampledSize, m_SelImageSize;

    public String m_FilePath;


    public enum ViewType { CanvasView, CropView }
    public ViewType m_ViewType;

    public XONImageHolder(Uri uri, ViewType viewType, Dimension dispSize,
                          int iconWt, int iconHt)
    {
        m_PanelSize = dispSize; m_Uri = uri; m_ViewType = viewType;
        m_ImageSource = ImageSource.UseUri; m_OrigImageSize = new Dimension();
        try {
            m_FilePath = XONUtil.getFilePath(m_Uri);
        } catch(Throwable th) {
            XONPropertyInfo.showAckMesg(R.string.NoImagePathTitle,
                    R.string.NoImagePathMesg, this);

        }
        m_ImageOrientation = XONUtil.getOrientation(m_FilePath);
        Log.i(TAG, "File Path: "+m_FilePath+" Orientation: "+m_ImageOrientation);
        WeakReference<Bitmap> srcImg = null;
        srcImg = getSrcImage(m_OrigImageSize, CompressionLogic.UsePanelSize, false);
        init(srcImg, srcImg.get(), iconWt, iconHt);
    }

    public Bitmap applyImageFilter(String imgFltr)
    {
        if (m_ThumbFilterImage == null)
            m_ThumbFilterImage = new HashMap<String, Bitmap>();
        Bitmap filterImg = m_ThumbFilterImage.get(imgFltr.toString());
        if (filterImg != null) return filterImg;
        filterImg = ImageFilterController.applyFilter(m_ThumbviewImage, imgFltr, "On", "");
        // if (action.toString().equals("BlurFadeFilter"))
        // ImageUtils.displayImage(filterImg, action.toString());
        m_ThumbFilterImage.put(imgFltr.toString(), filterImg);
        return filterImg;
    }

    public Bitmap getFilteredImage()
    {
        if (m_ImageMemoryCache.get("FilteredImage") == null) createFilteredImage();
        return m_ImageMemoryCache.get("FilteredImage");
    }

    public void applyLastFilter()
    {
        applyCurrFilterParam();
        updateDisplayImage(m_ImageFilterController.applyLastFilter(getFilteredImage()));
    }

    public void applyNewFilter()
    {
        createFilteredImage(); applyLastFilter();
    }

    public Bitmap createFilteredImage()
    {
        updateFilteredImage(getFilteredImage(true));
        return m_ImageMemoryCache.get("FilteredImage");
    }

    public void updateFilteredImage(Bitmap img)
    {
        synchronized (m_ImageMemoryCache) {
            removeMemCache("FilteredImage", true);
            m_ImageMemoryCache.put("FilteredImage", img);
            m_OrigDispImage = null;
        }
    }

    public void resetActiveImageFilter()
    {
        m_ImageFilter = null; m_ImageFilterHandler = null;
        m_ImageFilterParamName = null; m_ImageFilterParamVal.clear();
    }

    public void resetMemoryCache()
    {
        removeMemCache();
        XONObjectCache.removePersistentObject("XONImage");
        clearThumbImages(); m_ImageFilterParamVal.clear();
        Log.i(TAG, "Memory Cache: "+getMemUsage());
    }

    public Bitmap getFinalFilteredImage(WeakReference<Bitmap> origImg)
    {
        Log.i(TAG, "Size of Mem Cache after eviction: "+m_ImageMemoryCache.size());
        Log.i(TAG, "Get Bitmap Size: "+XONUtil.getBitmapSize(origImg.get()));
        try {
            return m_ImageFilterController.applyFilter(origImg.get(), false);
        } catch(Throwable ex) {
            Log.i(TAG, "Unable to Apply Filter. Retry Attempts: "+m_RetryCounter, ex);
            if (m_RetryCounter == 0) {
                origImg.get().recycle(); origImg.clear(); m_RetryCounter++;
                System.gc(); XONUtil.put2Sleep(4);
                XONPropertyInfo.showAckMesg(R.string.SmallImageSavedTitle,
                        R.string.SmallImageSaved, null);
                WeakReference<Bitmap> maxImg = new WeakReference<Bitmap>(
                        getFinalOrigImg(CompressionLogic.UseMaxSize));
                return getFinalFilteredImage(maxImg);
            }
            else if (m_RetryCounter == 1) {
                WeakReference<Bitmap> compImg = null;
                XONPropertyInfo.setToastMessage(R.string.SmallImageSaved);
                compImg = new WeakReference<Bitmap>(XONImageUtil.compressImage(origImg.get()));
                if (compImg.get() != origImg.get()) origImg.get().recycle();
                origImg.clear(); m_RetryCounter++;
                return getFinalFilteredImage(compImg);
            }
            return null;
        }
    }

    public void resetDisplayImage(boolean showOriginal)
    {
        if(m_ShowOriginalImage == showOriginal) return;
        m_ShowOriginalImage = showOriginal;
    }

    public Bitmap getFinalOrigImg()
    {
        m_RetryCounter = 0;
        return this.getFinalOrigImg(CompressionLogic.CalcMaxSize);
    }

    public int getMemUsage()
    {
        if (m_ImageMemoryCache == null) return 0;
        int imgSize = m_ImageMemoryCache.size();
        int thumbSz = m_ThumbFilterImage.size()*XONUtil.getBitmapSize(m_ThumbviewImage);
        Log.i(TAG, "Full Image Num Cache: "+imgSize+" Num of Thumb Img: "+
                m_ThumbFilterImage.size()+" Cache Size: "+thumbSz);
        return (imgSize+thumbSz);
    }

    private Bitmap getFinalOrigImg(CompressionLogic logic)
    {
        Log.i(TAG, "Size of Mem Cache: "+getMemUsage());

        clearThumbImages();
        removeMemCache(CACHE_IDS, true);
        Log.i(TAG, "Size of Mem Cache after Removal: "+getMemUsage());

        WeakReference<Bitmap> srcImg = null;

        srcImg = getSrcImage(m_OrigImageSize, logic, true);
        int size = XONUtil.getBitmapSize(srcImg.get());
        Log.i(TAG, "Get Orig Bitmap Size: "+size);
        return (srcImg.get());
    }

    public Vector<ImageFilterHandler> getUsedImageEffects()
    {
        return m_ImageFilterController.getUsedImageEffects();
    }

    private Bitmap m_OrigDispImage = null;
    public Bitmap getDisplayImage()
    {
        Bitmap dispImg = m_ImageMemoryCache.get("DisplayImage");
        if (m_ShowOriginalImage) {
            if (m_OrigDispImage == null)
                m_OrigDispImage = XONImageUtil.createBitmap(getSelectedImage(), dispImg,
                        this.m_PanelSize);
            if (m_OrigDispImage != null) return m_OrigDispImage;
            return getSelectedImage();
        }
        return dispImg;
    }

    public void resetCropView()
    {
        m_CropRect = null; m_OrigCropRect = null; m_ApplCropRect = null;
    }

    public void cancelCropView()
    {
        m_CropRect = m_ApplCropRect;
    }

    // The startYPt specifies the axis pt y from where the image can be drawn
    // The Scaling Factor determines how to fit the image in the display area

    public void deriveDeviceCTM(int specStartYPt, ScalingFactor scaleFactor,
                                ScalingFactor.FillLogic logic)
    {
        int defStartPt = XONPropertyInfo.getIntRes(R.string.image_start_pt);
        Log.i(TAG, "Def StartPt: "+defStartPt);
        // Scaling Parameters
        double sx = 0.0, sy = 0.0, scale = 0.0;
        // Translation parameters
        double tx = 0.0, ty = 0.0;
        // Defining the Viewable Area
        double startXPt = (double)defStartPt; double startYPt = (double)defStartPt;
        int imgHt = m_SelImageSize.height, imgWt = m_SelImageSize.width;
        Log.i(TAG, "Panel Sz: "+m_PanelSize+" Img Sz: "+m_SelImageSize);
        if (m_PanelSize.height > imgHt) startYPt = (m_PanelSize.height - imgHt)/2;
        if (startYPt < (double) specStartYPt) startYPt = (double) specStartYPt;
        double bottomPanelHt = (double)specStartYPt; // Bottom Margin same as the Top Margin
        double viewablePanelHt = (double) m_PanelSize.height - startYPt - bottomPanelHt;
        double viewablePanelWidth = (double) m_PanelSize.width - startXPt;
        Log.i(TAG, "St xPt: "+startXPt+" yPt: "+startYPt+"Viewable Wt: "+
                viewablePanelWidth+" Ht: "+viewablePanelHt);
        if (scaleFactor == null) {
            Dimension dispSize = new Dimension((int)viewablePanelWidth, (int)viewablePanelHt);
            // This method determines whats the best method to be applied, fit the image along
            // width or the height in the display area
            if (logic ==  null)
                logic = ScalingFactor.FillLogic.FILL_BBOX_WITH_IMAGE;
            scaleFactor = ScalingFactor.getScalingFactor(dispSize, imgWt, imgHt, logic);
        }
        Log.i(TAG, "Scale Factor: "+scaleFactor);
        if (scaleFactor.isFitHeight()) { sy = viewablePanelHt/imgHt;
            if (sy > 1.0) sy = 1.0; scale = sy; }
        else if (scaleFactor.isFitWidth()) { sx = viewablePanelWidth/imgWt;
            if (sx > 1.0) sx = 1.0; scale = sx; }
        else if (scaleFactor.isFitPage()) {
            sy = viewablePanelHt/imgHt; sx = viewablePanelWidth/imgWt;
            if (sx > sy) scale = sy; else scale = sx;
        } else if (scaleFactor.isZoomInOut()) scale = ((double)scaleFactor.m_ZoomValue)/100;
        sx = scale; sy = scale; // Scaling equally along x and y axis.
        double scaledImageWt = imgWt*scale, scaledImageHt = imgHt*scale;
        Log.i(TAG, "Img Wt: "+imgWt+" ImgHt: "+imgHt+" Scale: "+scale+
                " Scaled Img Wt: "+scaledImageWt+" Scaled Img Ht: "+scaledImageHt);
        // Scaled Image Wt and ht are divided by 2 to give equal padding on either side of
        // the Image Width or Height
        if (m_PanelSize.height > scaledImageHt)
            startYPt = (m_PanelSize.height - scaledImageHt)/2;
        if (startYPt < (double) specStartYPt) startYPt = (double) specStartYPt;
        // Calculating Translations in X and Y dirn.
        ty = startYPt;
        if (scaledImageWt < viewablePanelWidth)
        {
            tx = startXPt + (viewablePanelWidth - scaledImageWt)/2;
        }
        else tx = startXPt;
        Log.i(TAG, "TransX: "+tx+" Ty: "+ty);

        // Setting the Screen CTM and the screen to image ctm
        m_ScreenCTM.setScale(Double.valueOf(sx).floatValue(), Double.valueOf(sy).floatValue());
        int left = Double.valueOf(tx).intValue(), top = Double.valueOf(ty).intValue();
        if (left == 0) tx = 0.0; if (top == 0) ty = 0.0;
        if (tx > 0.0 || ty > 0.0) m_ScreenCTM.postTranslate(Double.valueOf(tx).floatValue(),
                Double.valueOf(ty).floatValue());
        m_ScreenCTM.getValues(m_ScreenCTMValues);
        Log.i(TAG, "Screen CTM: " + m_ScreenCTM);
        if (!m_ScreenCTM.invert(m_ScreenToImageCTM))
        Log.i(TAG, "Unable to Invert ScreenCTM to get the Image CTM");
        int scWt = Double.valueOf(scale*imgWt).intValue();
        int scHt = Double.valueOf(scale*imgHt).intValue();
        m_DisplayRect.set(left, top, left+scWt, top+scHt);
        Log.i(TAG, "Display Rect: " + m_DisplayRect);
        if (m_ViewType.equals(ViewType.CanvasView)) return;
        m_CropDisplayRect = new Rect(m_DisplayRect);
        float cropPerc = 0.25f, botPerc = 1f-cropPerc; m_CropRect = new Rect();
        m_CropRect.left = Math.round((float)left+cropPerc*scWt);
        m_CropRect.top = Math.round((float)top+cropPerc*scHt);
        m_CropRect.right = Math.round((float)left+botPerc*scWt);
        m_CropRect.bottom = Math.round((float)top+botPerc*scHt);
        Log.i(TAG, "Crop Rect: "+m_CropRect+" CropDisplayRect: "+
                m_CropDisplayRect);
    }

    public void setPanelSize(Dimension dispSize, ViewType viewType)
    {
        removeMemCache();
        m_ViewType = viewType; m_PanelSize = dispSize;
        Log.i(TAG, "View Type: "+m_ViewType+" Panel Size: "+m_PanelSize);
        WeakReference<Bitmap> srcImg = getSrcImage(m_OrigImageSize,
                CompressionLogic.UsePanelSize, false);
        setXONImage(srcImg, viewType);
        deriveDeviceCTM(0, null, ScalingFactor.FillLogic.FIT_IMAGE_IN_BBOX);
    }

    private static final String[] CACHE_IDS = {"SelectedImage", "FilteredImage",
            "DisplayImage"};

    private void removeMemCache(String[] cacheIds, boolean recycle)
    {
        Bitmap bitmap1 = null, bitmap2 = null;
        Vector<String> cacheIdList = XONUtil.createVector(cacheIds);
        for (int i = 0; i < cacheIdList.size(); i++) {
            String cacheId = cacheIdList.elementAt(i);
            bitmap1 = m_ImageMemoryCache.get(cacheId);
            if (bitmap1 != null && recycle) {
                for (int j = 0; j < CACHE_IDS.length; j++) {
                    bitmap2 =  m_ImageMemoryCache.get(CACHE_IDS[j]);
                    if (bitmap1 == bitmap2) {
                        if (cacheIdList.contains(CACHE_IDS[j])) continue;
                        recycle = false; break;
                    }
                }
                if (recycle) { bitmap1.recycle(); bitmap1 = null; }
            }
            m_ImageMemoryCache.remove(cacheId);
        }
    }

    private void removeMemCache()
    {
        removeMemCache(CACHE_IDS, true);
        m_ImageMemoryCache.evictAll();
    }

    public Rect setCropView(Dimension origSz)
    {
        return setCropView(m_CropRect, origSz);
    }

    public Rect setCropView(Rect cropRect, Dimension origSz)
    {
        if (cropRect == null || !m_CropDisplayRect.contains(cropRect)) return null;
        int x = cropRect.left - m_CropDisplayRect.left, w = cropRect.width();
        int y = cropRect.top - m_CropDisplayRect.top, h = cropRect.height();
        Log.i(TAG, "Disp Rect: "+m_CropDisplayRect+" x: "+x+" y: "+y+" w: "+w+
                " h:"+h+" CropRect: "+cropRect);
        Rect origRect = XONImageUtil.getOrigCropRect(origSz, m_CropSelImageSize, x, y, x+w, y+h);
        return origRect;
    }

    private WeakReference<Bitmap> getSrcImage(Dimension origImgSz,
                                              CompressionLogic compLogic, boolean useOrigCrop)
    {
        WeakReference<Bitmap> srcImg = null; Bitmap resImg = null;
        resImg = XONImageUtil.decodeFile(m_FilePath, m_PanelSize, m_OrigImageSize);
        srcImg = new WeakReference<>(resImg);
        if (XONImageUtil.rotateBitmapNeeded(m_ImageOrientation)) {
            resImg = XONImageUtil.rotateBitmap(srcImg.get(), m_ImageOrientation);
            if (srcImg.get() != resImg) {
                srcImg.get().recycle(); srcImg.clear(); srcImg = null;
                srcImg = new WeakReference<Bitmap>(resImg);
            }
        }

        m_ReSampledSize = new Dimension(srcImg.get().getWidth(), srcImg.get().getHeight());
        Log.i(TAG, "Orig Img Size: "+m_OrigImageSize+" Re-Sampled Size: "+
                m_ReSampledSize+" MaxResampleSz: "+XONPropertyInfo.m_MaxImageSize+
                " Bitmap Sz: "+XONImageUtil.getBitmapSize(srcImg.get()));
        if (m_ViewType != null && m_ViewType.equals(ViewType.CanvasView)) {
            Log.i(TAG, "Getting Cropped Rect: "+m_CropRect+" Display Rect: "+
                    m_CropDisplayRect);
            Rect origRect = setCropView(m_ReSampledSize);
            if (origRect != null) {
                resImg = Bitmap.createBitmap(srcImg.get(), origRect.left, origRect.top,
                        origRect.width(), origRect.height());
                if (srcImg.get() != resImg) {
                    srcImg.get().recycle(); srcImg.clear(); srcImg = null;
                    srcImg = new WeakReference<Bitmap>(resImg);
                }
                clearThumbImages();
                m_OrigCropRect = origRect; m_ApplCropRect = m_CropRect;
            }
        }

        if (compLogic.equals(CompressionLogic.UseDefCompression)) {
            Log.i(TAG, "Using Def Comp: "+compLogic);
            resImg = XONImageUtil.compressImage(srcImg.get());
            if (srcImg.get() != resImg) {
                srcImg.get().recycle(); srcImg.clear(); srcImg = null;
                srcImg = new WeakReference<Bitmap>(resImg);
            }
        } else if (compLogic.equals(CompressionLogic.CompressToSize)) {
            Log.i(TAG, "Using Comp2Sz: "+compLogic);
            resImg = XONImageUtil.compressToSize(srcImg.get());
            if (srcImg.get() != resImg) {
                srcImg.get().recycle(); srcImg.clear(); srcImg = null;
                srcImg = new WeakReference<Bitmap>(resImg);
            }
        } else if (compLogic.equals(CompressionLogic.UsePanelSize)) {
            Log.i(TAG, "Using UsePanelSz: "+compLogic);
            resImg = XONImageUtil.getScaledBitmap(srcImg.get(), m_PanelSize, true);
            if (srcImg.get() != resImg) {
                srcImg.get().recycle(); srcImg.clear(); srcImg = null;
                srcImg = new WeakReference<Bitmap>(resImg);
            }
        } else if (compLogic.equals(CompressionLogic.UseMaxSize)) {
            Log.i(TAG, "Using UseMaxSz: "+compLogic);
            resImg = XONImageUtil.getScaledBitmap(srcImg.get(), XONPropertyInfo.m_MaxImageSize,
                    true);
            if (srcImg.get() != resImg) {
                srcImg.get().recycle(); srcImg.clear(); srcImg = null;
                srcImg = new WeakReference<Bitmap>(resImg);
            }
        } else if (compLogic.equals(CompressionLogic.CalcMaxSize)) {
            int memSz = XONImageUtil.getBitmapSize(srcImg.get());
            float ratio = (float)XONPropertyInfo.MAX_BITMAP_SIZE/(float)memSz;
            Log.i(TAG, "Using CalcMaxSz: "+compLogic+" Bitmap Sz: "+memSz+
                    " Ratio: "+ratio);
            if (ratio >= 1.0) resImg = srcImg.get();
            else {
                ratio = (((float)memSz-(float)XONPropertyInfo.MAX_BITMAP_SIZE)/(1024f*1024f))*
                        XONPropertyInfo.PERC_SIZE_RED_4_1MB;
                Dimension maxSz = new Dimension(Math.round((float)srcImg.get().getWidth()*(1-ratio)),
                        Math.round((float)srcImg.get().getHeight()*(1-ratio)));
                Log.i(TAG, "Ratio: "+ratio+" Max Sz: "+maxSz);
                if (srcImg.get() != null)
                    resImg = XONImageUtil.getScaledBitmap(srcImg.get(), maxSz, true);
                else resImg = XONImageUtil.getScaledBitmap(resImg, maxSz, true);
                if (srcImg.get() == null || srcImg.get() != resImg) {
                    if (srcImg.get() != null) srcImg.get().recycle();
                    srcImg.clear(); srcImg = null; srcImg = new WeakReference<Bitmap>(resImg);
                }
            }
        }

        Log.i(TAG, "Final Image size wt: "+srcImg.get().getWidth()+
                " Ht: "+srcImg.get().getHeight());
        return srcImg;
    }

    public void clearThumbImages()
    {
        if (m_ThumbFilterImage == null) return;
        Vector<Bitmap> thumbImgs = new Vector<Bitmap>(m_ThumbFilterImage.values());
        for (int i = 0; i < thumbImgs.size(); i++) {
            Bitmap thumbImg = thumbImgs.elementAt(i); thumbImg.recycle();
        }
        m_ThumbFilterImage.clear();
    }

    private void createThumbView()
    {
        Bitmap selImage = m_ImageMemoryCache.get("SelectedImage");
        if (m_ImageType.equals(ImageType.Potrait) || m_ImageType.equals(ImageType.Lanscape))
            m_ThumbviewImage = XONImageUtil.getScaledBitmap(selImage, BitmapResizeLogic.SetMaxSizeBitmap,
                    m_ThumbSize.width, m_ThumbSize.height, true);
        else  m_ThumbviewImage = XONImageUtil.getScaledBitmap(selImage, m_ThumbSize.width,
                m_ThumbSize.height, true);
        if (m_ThumbFilterImage != null) clearThumbImages();
    }

    private void init(WeakReference<Bitmap> srcImgRef, Bitmap srcImg, int iconWt, int iconHt)
    {
        m_DisplayRect = new Rect();
        m_ImageMemoryCache = new LruCache<String, Bitmap>(XONPropertyInfo.getMemoryCace()) {
            /**
             * Measure item size in bytes rather than units which is more practical
             * for a bitmap cache
             */
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (bitmap != null)
                    return XONUtil.getBitmapSize(bitmap);
                return 0;
            }

            // Called for entries that have been evicted or removed.
            @Override
            protected void entryRemoved (boolean evicted, String key, Bitmap bitmap,
                                         Bitmap newBitmap)
            {
                Log.i(TAG, "Param Data: evicted = "+evicted+" key: "+key);
            }
        };
        m_IntensityAdjRect = new Rect(0, m_PanelSize.height-XONPropertyInfo.IntensityAdjHt,
                m_PanelSize.width, m_PanelSize.height);
        m_ReSampledSize = new Dimension(srcImg.getWidth(), srcImg.getHeight());
        m_ImageType = ImageType.Lanscape;
        if (m_ReSampledSize.width == m_ReSampledSize.height)
            m_ImageType = ImageType.Square;
        else if (m_ReSampledSize.height > m_ReSampledSize.width)
            m_ImageType = ImageType.Potrait;

        Bitmap selImage = XONImageUtil.getScaledBitmap(srcImg, m_PanelSize, true);
        if (srcImgRef != null) {
            if (srcImgRef.get() != selImage) srcImgRef.get().recycle();
            srcImgRef.clear(); srcImg = null; srcImgRef = null;
        } else { srcImg = null; srcImgRef = null; }
        Log.i(TAG, "Selected Image: "+selImage);
        m_SelImageSize = new Dimension(selImage.getWidth(), selImage.getHeight());
        if (m_ViewType.equals(ViewType.CropView)) m_CropSelImageSize = m_SelImageSize;
        Log.i(TAG, "Orig Img Size: "+m_OrigImageSize+" Re-Sampled Size: "+
                m_ReSampledSize+" Sel Img Size: "+m_SelImageSize+
                " Panel Size: "+m_PanelSize+" IntensityAdjRect: "+
                m_IntensityAdjRect);
        Bitmap dispImage = selImage.copy(Bitmap.Config.ARGB_8888, true);
        m_ImageMemoryCache.put("SelectedImage", selImage);
        m_ImageMemoryCache.put("DisplayImage", dispImage);
        m_ScreenCTMValues = new float[9];
        m_ScreenCTM = new Matrix(); m_ScreenToImageCTM = new Matrix();
        m_ThumbSize = new Dimension(iconWt, iconHt);
        createThumbView();
        m_AvgImagePixel = XONImageUtil.getAvgPixel(m_ThumbviewImage);
        XONObjectCache.addObject("AvgImagePixel", Integer.valueOf(m_AvgImagePixel), true);
        m_ImageFilterController = new ImageFilterController();
        XONObjectCache.addObject("ImageFilterController", m_ImageFilterController, true);
        m_ImageFilterParamVal = new HashMap<String, Object>();
        XONObjectCache.addObject("XONImage", this, true);
    }

    private void removeMemCache(String cacheId, boolean recycle)
    {
        String[] cacheIds = {cacheId}; removeMemCache(cacheIds, recycle);
    }

    public void updateDisplayImage(Bitmap img)
    {
        synchronized (m_ImageMemoryCache) {
            removeMemCache("DisplayImage", true);
            m_ImageMemoryCache.put("DisplayImage", img);
        }
    }

    public Bitmap getSelectedImage()
    {
        Bitmap selImg = m_ImageMemoryCache.get("SelectedImage");
        if (selImg == null) {
            //throw new RuntimeException("No Image is Selected for Filtering.");
            XONPropertyInfo.showAckMesg(R.string.NoImageSelTitle,
                    R.string.NoImageSelMesg, this);
        }
        return selImg;
    }

    private Bitmap getFilteredImage(boolean exclCurrFilter)
    {
        applyCurrFilterParam();
        return m_ImageFilterController.applyFilter(getSelectedImage(), exclCurrFilter);
    }

    public void applyFilter()
    {
        updateDisplayImage(getFilteredImage(false));
    }

    private Dimension m_CropSelImageSize;
    private void setXONImage(WeakReference<Bitmap> srcImg, ViewType viewType)
    {
        Bitmap selImage = XONImageUtil.getScaledBitmap(srcImg.get(), m_PanelSize, true);
        //srcImg.get().recycle();
        srcImg.clear(); srcImg = null;
        m_ImageMemoryCache.put("SelectedImage", selImage);
        m_SelImageSize = new Dimension(selImage.getWidth(), selImage.getHeight());
        if (viewType.equals(ViewType.CropView)) {
            m_CropSelImageSize = m_SelImageSize;
            Bitmap dispImage = selImage.copy(Bitmap.Config.ARGB_8888, true);
            m_ImageMemoryCache.put("DisplayImage", dispImage);
        } else if (viewType.equals(ViewType.CanvasView)) {
            createThumbView();
            m_AvgImagePixel = XONImageUtil.getAvgPixel(m_ThumbviewImage);
            XONObjectCache.addObject("AvgImagePixel", Integer.valueOf(m_AvgImagePixel), true);
            applyFilter();
        }
    }

    private void applyCurrFilterParam()
    {
        String paranNm = m_ImageFilterParamName;
        Map<String, Object> locFldMap = m_ImageFilterParamVal;
        ImageFilterHandler imgFltrHdlr = m_ImageFilterHandler;
        if (imgFltrHdlr != null)
        {
            Map<String, String> imgFldMap = imgFltrHdlr.getFieldParam(paranNm);
            Log.i(TAG, "ImgFltr: "+imgFltrHdlr.getAction()+" Bfor FltrParam Vals: "+
                    imgFldMap+" Local FltrParam: "+locFldMap+
                    " Panel Size: "+m_PanelSize);
            String fldType = imgFltrHdlr.getFieldType(m_ImageFilterParamName);
            if (fldType == null || fldType.length() == 0) return;
            if (fldType.equals("Intensity"))
            {
                int paramVal = ((Integer) locFldMap.get("AmtX")).intValue();
                float flVal = (float)paramVal/(float)m_PanelSize.width;
                Log.i(TAG, "Local Param Val: "+paramVal+
                        "Filter Val Set: "+flVal);
                imgFltrHdlr.setFieldValue(imgFldMap, paranNm,
                        Float.valueOf(flVal).toString(), false);
                Log.i(TAG, "ImgFltrParam Vals: "+imgFldMap);
            } else if (fldType.contains("Intensity")) {
                int paramVal = ((Integer) locFldMap.get("AmtX")).intValue();
                float flVal = (float)paramVal/(float)m_PanelSize.width;
                Log.i(TAG, "Param X Val: "+paramVal+" Filter XVal: "+flVal);
                imgFltrHdlr.setFieldValue(imgFldMap, paranNm,
                        Float.valueOf(flVal).toString(), false);
                paramVal = ((Integer) locFldMap.get("AmtY")).intValue();
                flVal = (float)paramVal/(float)m_PanelSize.height;
                Log.i(TAG, "Param Y Val: "+paramVal+" Filter YVal: "+flVal);
                imgFltrHdlr.setFieldValue(imgFldMap, paranNm, "Amt", "Y",
                        Float.valueOf(flVal).toString(), false);
                float ang = ((Float) locFldMap.get("AngXY")).floatValue();
                flVal = Double.valueOf(ang/(2*Math.PI)).floatValue();
                Log.i(TAG, "Param Ang Val: "+paramVal+" Filter Ang Val: "+flVal);
                imgFltrHdlr.setFieldValue(imgFldMap, paranNm, "Ang", "XY",
                        Float.valueOf(flVal).toString(), false);
            } else if (fldType.equals("CircularImpact")) {
                int paramVal = ((Integer) locFldMap.get("CenterX")).intValue();
                float flVal = (float)(paramVal - m_DisplayRect.left)/(float)m_SelImageSize.width;
                Log.i(TAG, "Center X Val: "+paramVal+" Filter Center XVal: "+flVal);
                imgFltrHdlr.setFieldValue(imgFldMap, paranNm, "Center", "X",
                        Float.valueOf(flVal).toString(), false);
                paramVal = ((Integer) locFldMap.get("CenterY")).intValue();
                flVal = (float)(paramVal - m_DisplayRect.top)/(float)m_SelImageSize.height;
                Log.i(TAG, "Center Y Val: "+paramVal+" Filter Center YVal: "+flVal);
                imgFltrHdlr.setFieldValue(imgFldMap, paranNm, "Center", "Y",
                        Float.valueOf(flVal).toString(), false);
                paramVal = ((Integer) locFldMap.get("RadX")).intValue();
                flVal = (float)paramVal/(float)m_PanelSize.width;
                Log.i(TAG, "Param X Val: "+paramVal+" Filter RadXVal: "+flVal);
                imgFltrHdlr.setFieldValue(imgFldMap, paranNm, "Rad", "X",
                        Float.valueOf(flVal).toString(), false);
                paramVal = ((Integer) locFldMap.get("AmtY")).intValue();
                flVal = (float)paramVal/(float)m_PanelSize.height;
                Log.i(TAG, "Param Y Val: "+paramVal+" Filter YVal: "+flVal);
                imgFltrHdlr.setFieldValue(imgFldMap, paranNm, "Amt", "Y",
                        Float.valueOf(flVal).toString(), false);
                float ang = ((Float) locFldMap.get("AngXY")).floatValue();
                flVal = Double.valueOf(ang/(2*Math.PI)).floatValue();
                Log.i(TAG, "Param Ang Val: "+paramVal+" Filter Ang Val: "+flVal);
                imgFltrHdlr.setFieldValue(imgFldMap, paranNm, "Ang", "XY",
                        Float.valueOf(ang).toString(), false);
            }
        }
    }


    public boolean isCircularImpactSet()
    {
        if (!m_ImageFilterParamVal.containsKey("CircularImpactSet")) return false;
        return ((Boolean)m_ImageFilterParamVal.get("CircularImpactSet")).booleanValue();
    }

    public boolean isCircularIntensitySet()
    {
        if (!m_ImageFilterParamVal.containsKey("CircularIntensitySet")) return false;
        return ((Boolean)m_ImageFilterParamVal.get("CircularIntensitySet")).booleanValue();
    }

    public void setImageFilterParam(Point startPt, Point endPt)
    {
        String paramNm = m_ImageFilterParamName;
        ImageFilterHandler imgFltrHdlr = m_ImageFilterHandler;
        if (imgFltrHdlr == null) return;
        String fldType = imgFltrHdlr.getFieldType(paramNm);
        if (fldType.contains("Intensity")) {
            m_ImageFilterParamVal.put("AmtX", endPt.x);
            m_ImageFilterParamVal.put("AmtY", endPt.y);
            float ang = Double.valueOf(XONUtil.getAngle(startPt, endPt)).floatValue();
            m_ImageFilterParamVal.put("AngXY", ang);
        } else if (fldType.equals("CircularImpact")) {
            if (isCircularImpactSet()) {
                m_ImageFilterParamVal.put("CenterX", endPt.x);
                m_ImageFilterParamVal.put("CenterY", endPt.y);
                m_ImageFilterParamVal.put("ImpactRect", XONUtil.createRect(endPt.x, endPt.y));
                return;
            } else if (isCircularIntensitySet()) {
                m_ImageFilterParamVal.put("RadX", endPt.x);
                m_ImageFilterParamVal.put("AmtY", endPt.y);
                float ang = Double.valueOf(XONUtil.getAngle(startPt, endPt)).floatValue();
                m_ImageFilterParamVal.put("AngXY", ang);
            }
        }
    }

    public Bitmap applyLastFilter(Bitmap srcImage)
    {
        applyCurrFilterParam();
        return m_ImageFilterController.applyLastFilter(srcImage);
    }

    public boolean setOrientation(ImageOrientation imgOrient)
    {
        if (imgOrient.equals(m_ImageOrientation)) return false;
        m_ImageOrientation = imgOrient;
        Bitmap selImage = m_ImageMemoryCache.get("SelectedImage");
        selImage = XONImageUtil.rotateBitmap(selImage, m_ImageOrientation);
        m_SelImageSize = new Dimension(selImage.getWidth(), selImage.getHeight());
        Bitmap dispImage = selImage.copy(Bitmap.Config.ARGB_8888, true);
        synchronized (m_ImageMemoryCache) {
            removeMemCache();
            m_ImageMemoryCache.put("SelectedImage", selImage);
            m_ImageMemoryCache.put("DisplayImage", dispImage);
        }
        createThumbView();
        m_ScreenCTMValues = new float[9];
        m_ScreenCTM = new Matrix(); m_ScreenToImageCTM = new Matrix();
        deriveDeviceCTM(0, null, ScalingFactor.FillLogic.FIT_IMAGE_IN_BBOX);
        return true;
    }
}
