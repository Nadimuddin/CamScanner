package com.bridgelabz.docscanner;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bridgelabz.docscanner.activities.XONCanvasView;
import com.bridgelabz.docscanner.activities.XON_IM_UI;
import com.bridgelabz.docscanner.activities.XON_Main_UI;
import com.bridgelabz.docscanner.controller.TemplateFilterHolder;
import com.bridgelabz.docscanner.controller.XONImageHolder;
import com.bridgelabz.docscanner.controller.XONImageHolder.ImageType;
import com.bridgelabz.docscanner.controller.XONImageProcessHandler;
import com.bridgelabz.docscanner.interfaces.ThreadInvokerMethod;
import com.bridgelabz.docscanner.interfaces.XONClickListener;
import com.bridgelabz.docscanner.interfaces.XONImageProcessor;
import com.bridgelabz.docscanner.interfaces.XONUIAdapterInterface;
import com.bridgelabz.docscanner.utility.IntentUtil;
import com.bridgelabz.docscanner.utility.ThreadCreator;
import com.bridgelabz.docscanner.utility.UIUtil;
import com.bridgelabz.docscanner.utility.XONImageFilterDef;
import com.bridgelabz.docscanner.utility.XONImageFilterDef.ImageFilter;
import com.bridgelabz.docscanner.utility.XONPropertyInfo;
import com.bridgelabz.docscanner.utility.XONUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bridgeit on 25/11/16.
 */

public class XONImageFilterView implements XONImageProcessor, ThreadInvokerMethod,
        XONClickListener, XONUIAdapterInterface
{

    // View on which the image is drawn
    private XONCanvasView m_XONCanvasView;

    // Map of Image Filter to the corresponding View
    Map<String, View> m_ImageFilterView;
    int m_MainMenuResId;
    int m_ImageFilterGroupResId;
    // Image Selected by the user on which the filter has to apply
    XONImageHolder m_XONImage;

    // Image Filter Grouping identifying the image filters
    private String m_ImageFilterGrouping;

    // Singleton Object and real time based on the Image Filter Grouping the
    // Views are drawn on the XONScrollView
    private static XONImageFilterView m_XONImageFilterView = null;

    // This object is the adapter to the Image Filters shown in the Horz Scroll
    XONHorzListAdapter m_XONImageFilterAdapter;

    // The Main Activity that invoked this view
    XON_IM_UI m_MainActivity;
    private String[] m_ImageFilters, m_ImageFiltersDispName;

    XONImageProcessHandler m_XONImageProcessHandler;

    // This object is the adapter to the Image Filters shown in the Horz Scroll
  //  XONHorzListAdapter m_XONImageFilterAdapter;

    private int m_SelectedFilterPos = -1;


    public static final String TAG = "XONImageFilterView";
    XONImageProcessHandler m_XONCanvasImageProcessHandler;

    public XONCanvasView getCanvasView() {
        return m_XONCanvasView;
    }

    private XONImageFilterView(XON_IM_UI act, XONCanvasView canvas)
    {
        m_MainActivity = act; m_XONCanvasView = canvas;
        m_ImageFilterView = new HashMap<String, View>();
        m_XONImageProcessHandler = new XONImageProcessHandler(
                m_MainActivity.getApplicationContext());
        m_XONImageProcessHandler.setLoadingImage(R.drawable.empty_photo);
        m_XONCanvasImageProcessHandler = new XONImageProcessHandler(
                m_MainActivity.getApplicationContext());
        m_XONCanvasImageProcessHandler.setImageFadeIn(false);
        m_XONCanvasView.setImageFilterScrollView(this);
    }

    @Override
    public Bitmap processImage(Map<String, Object> data) {
        AsyncTaskReq asyncTaskReq = (AsyncTaskReq) data.get("AsyncTaskReq");
        try {
            switch(asyncTaskReq)
            {
                case ApplyFilterOnThumbView :
                    String imgFltrOpt = (String)data.get("ImageFilterOption");
                    Bitmap fltrImg = m_XONImage.m_ThumbviewImage;
                    fltrImg = m_XONImage.applyImageFilter(imgFltrOpt);
                    return fltrImg;
                case ApplyNewFilterOnCanvasView :
                    m_XONImage.applyNewFilter();
                    m_XONCanvasView.resetMemParams();
                    return m_XONImage.getDisplayImage();
                case ApplyFilterOnCanvasView :
                    m_XONImage.applyFilter();
                    m_XONCanvasView.resetMemParams();
                    return m_XONImage.getDisplayImage();
                case ShowImageInCanvas:
                    m_XONCanvasView.resetMemParams();
                    return m_XONImage.getDisplayImage();
                default:
                    break;
            }
        } catch(Throwable ex) {
            Log.i(TAG, "Mem Cache Used: "+m_XONImage.getMemUsage());
            Log.i(TAG, "Error while applying task: "+asyncTaskReq, ex);
            XONPropertyInfo.showErrorAndRoute(R.string.NoImageFound, this);
        }
        return m_XONImage.getDisplayImage();
    }

    public boolean isImageFilterValid() { return isImageFilterValid(-1); }

    @Override
    public int getCount()
    {
        int cnt = 0;
        if (isImageFilterValid()) cnt = m_ImageFilters.length;
        Log.i(TAG, "ImageFilterGrouping: "+m_ImageFilterGrouping+
                " Image Filter Count: "+cnt);
        return cnt;
    }

    @Override
    public Object getItem(int position) {
        String imgFltr = "";
        if (isImageFilterValid(position)) imgFltr = m_ImageFilters[position];
        Log.i(TAG, "ImageFilterGrouping: "+m_ImageFilterGrouping+
                " Image Filter: "+imgFltr);
        return imgFltr;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // Resetting the Background for all the Image Filters
    private void setImageFilterBG(View fltrView, boolean isActive)
    {
        TextView title = (TextView) fltrView.findViewById(R.id.title);
        if (isActive) title.setBackgroundResource(R.color.XONSelBackground);
        else { title.setBackgroundResource(R.color.XONTextSelColor);
            Log.i(TAG, "Fltr Title: "+title);
        }
    }

    @Override
    public View getView(int position) {
        String imgFltr = "", imgFltrOpt = null;
        if (isImageFilterValid(position)) imgFltr = m_ImageFilters[position];
        Log.i(TAG, "ImageFilterGrouping: "+m_ImageFilterGrouping+
                " Image Filter Name: "+imgFltr);
        if (imgFltr == null || imgFltr.length() <= 0) return null;

        String imgFltrKey = imgFltr;
        imgFltrOpt = XONImageFilterDef.getImageFilter(m_MainMenuResId, m_ImageFilterGrouping,
                imgFltr);
        Log.i(TAG, "Image Filter: "+imgFltrOpt);
        if (imgFltrOpt != null) imgFltrKey = imgFltrOpt.toString();
        View fltrView = m_ImageFilterView.get(imgFltrKey);
        if(fltrView != null) return fltrView;
        // To inflate a view you use the LayoutInflater class.
        LayoutInflater inflater = m_MainActivity.getLayoutInflater();
        // To actually create the view object you use the inflate() method of the
        // LayoutInflater. Here the inflate method is used to create each list item of
        // the Horizontal List View
        fltrView = inflater.inflate(R.layout.xon_horz_list_view, null);
        if (imgFltrOpt != null && m_XONImage.isImageFilterActive(imgFltrOpt))
            setImageFilterBG(fltrView, true);
        ImageView imgView = (ImageView) fltrView.findViewById(R.id.image);
        if (m_XONImage.m_ImageType.equals(ImageType.Potrait))
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (imgFltrOpt != null) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("AsyncTaskReq", AsyncTaskReq.ApplyFilterOnThumbView);
            data.put("ImageFilterOption", imgFltrOpt);
            m_XONImageProcessHandler.processImage(this, data, imgView);
        }
        TextView title = (TextView) fltrView.findViewById(R.id.title);
        title.setText(m_ImageFiltersDispName[position]);
        return fltrView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long rowId) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i(TAG, "ImageFilterGrouping: "+m_ImageFilterGrouping);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {

    }

    @Override
    public void onLongPress(MotionEvent e, long downTime, int pos) {
        m_SelectedFilterPos = pos;
        String userImgEfctsGrp = XONUtil.replaceSpace(XONPropertyInfo.
                getString(R.string.user_effects), "");
        if (!m_ImageFilterGrouping.equals(userImgEfctsGrp)) return;
        long diffTime = System.currentTimeMillis() - downTime;
        Log.i(TAG, "Diff Time: " + diffTime);
        if (diffTime > XONPropertyInfo.getIntRes(R.string.popup_scroll_time)) {
            UIUtil.createSingleChoiceDialog(XONPropertyInfo.m_SubMainActivity,
                    R.drawable.alert_dialog_icon, R.string.user_effects_actions,
                    R.array.user_effects_action_list, this).show();
        }
    }

    @Override
    public void processThreadRequest(Map<String, Object> requestData) throws Exception {
        m_MainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_XONImageFilterAdapter.m_XONHorzScrollView.setVisibility(View.VISIBLE);
            }
        });
    }



    public boolean isImageFilterValid(int position)
    {
        boolean isValid = false;
        if (m_ImageFilterGrouping != null && m_ImageFilters != null &&
                m_ImageFilters.length > 0) isValid = true;
        if (position == -1 || !isValid) return isValid;
        if (m_ImageFilters[position] == null) isValid = false;
        return isValid;
    }

    private void deleteUserEffect()
    {
        String imgFltr = ""; String imgFltrOpt = null; int position = m_SelectedFilterPos;
        if (isImageFilterValid(position)) imgFltr = m_ImageFilters[position];
        imgFltrOpt = XONImageFilterDef.getImageFilter(m_MainMenuResId, m_ImageFilterGrouping,
                imgFltr);
        Log.i(TAG, "ImageFilterGrouping: "+m_ImageFilterGrouping+
                " Image Fltr Name: "+imgFltr+" Image Filter: "+imgFltrOpt);
        boolean result = TemplateFilterHolder.delTemplateFilterImpl(imgFltrOpt);
        if (result) {
            activateXONImageFilterView(m_MainMenuResId, m_ImageFilterGroupResId, m_XONImage);
        }
    }

    private String m_UserEffectsAction = null;

    @Override
    public void onClick(int actionBut) {
        String[] userEffectsActions = XONPropertyInfo.getStringArray(R.array.user_effects_action_list);
        m_UserEffectsAction = userEffectsActions[actionBut];
        XONPropertyInfo.setToastMessage("Selected User Effect Action: "+m_UserEffectsAction);
    }

    @Override
    public void onOK(int dialogTitleResId, View customView) {
        if (dialogTitleResId == R.string.user_effects_actions) {
            String[] userEffectsActions = XONPropertyInfo.getStringArray(R.array.user_effects_action_list);
            if (m_UserEffectsAction == null) m_UserEffectsAction = userEffectsActions[0];
            if (m_UserEffectsAction.equals(userEffectsActions[0])) deleteUserEffect();
            return;
        }
        IntentUtil.processIntent(XONPropertyInfo.m_SubMainActivity, XON_Main_UI.class);
    }

    @Override
    public void onCancel(int dialogTitleResId) {

    }



    private enum AsyncTaskReq {
        // ApplyFilterOnThumbView - Applies filter on Thumb View
        // ApplyNewFilterOnCanvasView - This task is used if new Filter is added
        // ApplyNewFilterOnCanvasView - This task is used if a filter is removed
        // ShowImageInCanvas - This task is used to show the current Displayable Image
        ApplyFilterOnThumbView, ApplyNewFilterOnCanvasView, ApplyFilterOnCanvasView,
        ShowImageInCanvas
    }

    public void activateXONImageFilterView(int mainMenu, int imgFltrGrpResId,
                                           XONImageHolder xonImgHldr)
    {
        m_ImageFilterView.clear(); m_MainMenuResId = mainMenu;
        m_ImageFilterGroupResId = imgFltrGrpResId;
        XONPropertyInfo.activateProgressBar(true);
        m_XONImage = xonImgHldr; m_XONImage.resetActiveImageFilter();
        String imgFltGrp = m_MainActivity.getResources().getString(imgFltrGrpResId);
        Log.i(TAG, "ImgFltGrp: "+imgFltGrp);
        m_ImageFilterGrouping = XONUtil.replaceSpace(imgFltGrp, "");
        TemplateFilterHolder basicTemplFltr = null, userTemplFltr = null;
        if (m_MainMenuResId == R.id.quick_effects_btn &&
                imgFltrGrpResId == R.string.popular_effects) {
            basicTemplFltr = TemplateFilterHolder.getInstance(ImageFilter.
                    PopularTemplateFilter);
            m_ImageFilters = basicTemplFltr.getImageFilters();
            m_ImageFiltersDispName = m_ImageFilters;
        } else if (m_MainMenuResId == R.id.quick_effects_btn &&
                imgFltrGrpResId == R.string.user_effects) {
            userTemplFltr = TemplateFilterHolder.getInstance(ImageFilter.
                    PersonalizedTemplateFilter);
            m_ImageFilters = userTemplFltr.getImageFilters();
            m_ImageFiltersDispName = m_ImageFilters;
            if (m_ImageFilters.length == 0)
                XONPropertyInfo.showAckMesg(R.string.NoSavedEffectsTitle,
                        R.string.NoSavedEffectsMesg, null);
        } else {
            int imgFltrResId = XONImageFilterDef.m_ImageFilterResId.get(imgFltrGrpResId);
            m_ImageFilters = m_MainActivity.getResources().getStringArray(imgFltrResId);
            imgFltrResId = XONImageFilterDef.m_ImageFilterDispResId.get(imgFltrGrpResId);
            m_ImageFiltersDispName = m_MainActivity.getResources().getStringArray(imgFltrResId);
            if (m_MainMenuResId == R.id.quick_effects_btn &&
                    imgFltrGrpResId == R.string.adv_effects) {
                if (m_ImageFilters.length == 0)
                    XONPropertyInfo.showAckMesg(R.string.FeatureCommingTitle,
                            XONPropertyInfo.getString(R.string.adv_effects)+" "+
                                    XONPropertyInfo.getString(R.string.FeatureCommingMesg), null);
            }
        }
        Log.i(TAG, "Image Fltr Grp: "+m_ImageFilterGrouping);
      //  if (m_ImageFilters.length > 0) m_XONImageFilterAdapter.reset();
      //  else
        resetView();

        m_XONCanvasView.setXONImage(m_XONImage);
        m_XONCanvasImageProcessHandler.setLoadingImage(m_XONImage.getDisplayImage());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("AsyncTaskReq", AsyncTaskReq.ShowImageInCanvas);
        m_XONCanvasImageProcessHandler.processImage(this, data, m_XONCanvasView);
        Map<String, Object> reqData = new HashMap<String, Object>();
        ThreadCreator.getInstance().createThread(this, reqData, true);
    }

    public void resetView()
    {
        m_ImageFilterView.clear();
       // m_XONImageFilterAdapter.reset();
      //  m_XONImageFilterAdapter.m_XONHorzScrollView.invalidate();
    }

    public void resetCache()
    {
        m_MainActivity = null;
        if (m_XONCanvasView != null) m_XONCanvasView.resetCache(); m_XONCanvasView = null;
        if (m_ImageFilterView != null) m_ImageFilterView.clear(); m_ImageFilterView = null;
        m_XONImageProcessHandler = null; m_XONCanvasImageProcessHandler = null;
    }

    public static void reset()
    {
        m_XONImageFilterView = null;
    }

    public static XONImageFilterView getInstance(XON_IM_UI act, XONCanvasView canvas,
                                                 XONHorzScrollView imgFltrListView)
    {
        if (m_XONImageFilterView != null) m_XONImageFilterView.resetCache(); reset();
        if (m_XONImageFilterView != null) {
            m_XONImageFilterView.m_XONImageFilterAdapter = null;
        } else { canvas = new XONCanvasView(act);
            m_XONImageFilterView = new XONImageFilterView(act, canvas); }
        XONHorzListAdapter adapter = new XONHorzListAdapter(m_XONImageFilterView,
                imgFltrListView);
        m_XONImageFilterView.m_XONImageFilterAdapter = adapter;
        return m_XONImageFilterView;
    }
}
