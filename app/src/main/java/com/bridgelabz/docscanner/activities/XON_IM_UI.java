package com.bridgelabz.docscanner.activities;

/**
 * Created by bridgeit on 25/11/16.
 */

import com.bridgelabz.docscanner.XONHorzScrollView;
import com.bridgelabz.docscanner.XONImageFilterView;
import com.bridgelabz.docscanner.controller.TemplateFilterHandlerImpl;
import com.bridgelabz.docscanner.controller.TemplateFilterHolder;
import com.bridgelabz.docscanner.controller.XONImageHolder;
import com.bridgelabz.docscanner.controller.XONImageProcessHandler;
import com.bridgelabz.docscanner.fragment.ImageFiltering;
import com.bridgelabz.docscanner.interfaces.ImageFilterHandler;
import com.bridgelabz.docscanner.interfaces.ThreadInvokerMethod;
import com.bridgelabz.docscanner.preference.SaveSharedPreference;
import com.bridgelabz.docscanner.utility.BuildConfig;
import com.bridgelabz.docscanner.utility.Dimension;
import com.bridgelabz.docscanner.utility.IntentUtil;
import com.bridgelabz.docscanner.utility.RawCacheManager;
import com.bridgelabz.docscanner.utility.ScalingFactor;
import com.bridgelabz.docscanner.utility.StorageUtil;
import com.bridgelabz.docscanner.utility.ThreadCreator;
import com.bridgelabz.docscanner.utility.UIUtil;
import com.bridgelabz.docscanner.utility.XONImageManager;
import com.bridgelabz.docscanner.utility.XONObjectCache;
import com.bridgelabz.docscanner.utility.XONPropertyInfo;
import com.bridgelabz.docscanner.utility.XONUtil;
import com.bridgelabz.docscanner.controller.XONImageHolder.ViewType;
import com.bridgelabz.docscanner.utility.XONImageFilterDef.ImageFilter;
import com.bridgelabz.docscanner.utility.XONPropertyInfo.ImageOrientation;
import com.bridgelabz.docscanner.R;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;

public class XON_IM_UI extends Activity implements ThreadInvokerMethod
{
    public static final String TAG = "XON_IM_UI";

    public Map<Integer, Button> m_ImageProcessButtons;
    public Map<Integer, Button> m_ImageSubProcessButtons;
    public Map<Integer, Button> m_ImageActionButtons;
    public Map<Integer, ScrollView> m_ScrollMenu;
    public int m_ActiveMainMenu = -1, m_ActiveSubMenu = -1;
    public ScrollView m_ActiveScrollView;

    // Basic and Advanced View
    public enum XONIMViewType { Basic, Advanced }
    public XONIMViewType m_XONIMViewType;

    // Clean up type local or complete. In Local only the cache of UI Elements are
    // destroyed while in Complete, the complete cache is destroyed
    private enum XONCleanType { Local, Complete }
    private XONCleanType m_XONCleanType = XONCleanType.Complete;

    private static final int[]
            m_BasicButtonResIds = { R.id.basic_popular_effects_btn };

    private static final int[]
            m_AdvButtonResIds = {R.id.quick_effects_btn};

    // Size to display the image in the Canvas View
    Rect m_LayoutRect; Dimension m_LayoutSize;

    // Image URI and Image Holder Object
    Uri m_ImageUri; XONImageHolder m_XONImage;

    // Canvas View to view Image and Scroll View of type XONImageFilterView to view
    // and apply Image Filters
    XONCanvasView m_XONCanvasView;
    XONImageFilterView m_XONImageFilterView;

    // This is a singleton object used for saving and viewing XON Images
    XONImageManager m_XONImageManager;

    // This is set to true when this Activity is completly loaded and Image Filters can
    // be applied. This is used to perform button click as soon as the Activity is loaded
    private boolean m_ImageFilterActivated = false;

    // Indicates if Scroll Menu is On/Off
    private boolean m_ScrollMenuOn = false;

    public static Vector<Integer> m_MenuAction, m_SubMenuAction;
    static {
        m_MenuAction = new Vector<>();
        m_SubMenuAction = new Vector<>();
    }

    @SuppressLint({ "UseSparseArrays", "UseSparseArrays" })
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_xon__im__ui);

        m_LayoutRect = null; m_LayoutSize = null; m_XONImage = null;
        setXONIMViewType(false);
        m_ImageUri = IntentUtil.getImageIntent(this);
        if (m_ImageUri == null)
        {
            if (XONObjectCache.getObjectForKey("XONImage") != null) {
                m_XONImage = (XONImageHolder) XONObjectCache.getObjectForKey("XONImage");
                m_ImageUri = m_XONImage.m_Uri;
                Log.i(TAG, "XON View Type: "+m_XONImage.m_ViewType);
            } else {
                IntentUtil.processIntent(XON_IM_UI.this, XON_Main_UI.class);
                finish();
                return;
            }
        }

        Log.i(TAG, "Image Uri: "+m_ImageUri+" Uri Path: "+m_ImageUri.getPath());

        m_XONImageFilterView = null; m_ImageFilterActivated = false;
        m_XONCleanType = XONCleanType.Complete;
        m_ScrollMenu = new HashMap<>();
        m_ImageProcessButtons = new HashMap<>();
        m_ImageSubProcessButtons = new HashMap<>();
        m_ImageActionButtons = new HashMap<>();

        XONPropertyInfo.populateResources(this, BuildConfig.DEBUG, true);

        m_XONImageManager = XONImageManager.getInstance();
        initXONIMButtons();

        XONHorzScrollView listview = (XONHorzScrollView) findViewById(R.id.filter_listview);
        // Reg XONImageFilterView object as an XON Adapter to process call backs from XONHorzScroll
        m_XONImageFilterView =  XONImageFilterView.getInstance(this, m_XONCanvasView, listview);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.xon_graphics_holder);
        m_XONCanvasView = m_XONImageFilterView.getCanvasView();
        frameLayout.addView(m_XONCanvasView);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("Request", "CheckFilterActivatedReq");
        ThreadCreator.getInstance().createThread(this, data, true);

    }

    public void showFilters(Activity act) {
        try {
            ImageFiltering fragment = new ImageFiltering();
            Bundle arg = new Bundle();

            SaveSharedPreference sharedPreference = new SaveSharedPreference(this);
            String uriString = sharedPreference.getPreference("cropped_image_uri");

            Uri uri = Uri.fromFile(new File(uriString.substring(7)));
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            StorageUtil storage = new StorageUtil(this);
            uriString = m_ImageUri.toString();
            String imageName = uriString.substring(uriString.lastIndexOf('/')+1);
            String directory = storage.getMyDirectory();
            uri = storage.storeImage(bitmap, directory, imageName);
            arg.putString("image_uri", uri.toString());
            fragment.setArguments(arg);

            FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
            transaction.replace(R.id.layout_root, fragment);
            transaction.addToBackStack(null).commit();
            sharedPreference.setPreferences("cropped_image_uri", "nothing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "onBackPressed: ");
    }

    @Override
    public void onLowMemory()
    {
        Log.i(TAG, "System is running low in Memory");
    }

    // This is called when the overall system is running low on memory, and would like
    // actively running process to try to tighten their belt. From interface
    // android.content.ComponentCallbacks2
    @Override
    public void onTrimMemory(int level)
    {
        Log.i(TAG, "System is running low in Memory and will like to trim level: "+
                level);
    }

    @Override
    public void onDestroy()
    {
        try {
            Log.i(TAG, "This Activity is getting Destroyed....");
            if (m_XONCleanType.equals(XONCleanType.Local)) localCleanUp();
            else cleanUp();
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @SuppressWarnings("rawtypes")
    public void localCleanUp()
    {
        XONUtil.clear((HashMap) m_ScrollMenu);
        XONUtil.clear((HashMap) m_ImageProcessButtons);
        XONUtil.clear((HashMap) m_ImageSubProcessButtons);
        XONUtil.clear((HashMap) m_ImageActionButtons);
    }

    public void cleanUp()
    {
        localCleanUp(); XONImageFilterView.reset();
        if (m_XONImage != null) m_XONImage.resetMemoryCache();
        m_XONImageFilterView = null; m_XONCanvasView = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (m_XONIMViewType != null) {
                if (m_XONIMViewType.equals(XONIMViewType.Advanced)) {
                    resetXONIMViewType();
                    IntentUtil.processIntent(this, XON_IM_UI.class);
                    m_XONCleanType = XONCleanType.Local; finish();
                    return true;
                }
            }
            resetXONIMViewType();
            IntentUtil.processIntent(this, XON_Main_UI.class);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void resetXONIMViewType()
    {
        XONObjectCache.removePersistentObject("XONIMViewType");
    }

    private void setButtonVisibility(int[] resIds, int visibility)
    {
        if (m_ImageProcessButtons == null) return;
        for (int i = 0; i < resIds.length; i++)
        {
            Button button = m_ImageProcessButtons.get(resIds[i]);
            if (button != null) button.setVisibility(visibility);
        }
    }

    private void setXONIMViewType(boolean showAdvView)
    {
        if (showAdvView) {
            m_XONIMViewType = XONIMViewType.Advanced;
            XONObjectCache.addObject("XONIMViewType", m_XONIMViewType, true);
            setButtonVisibility(m_BasicButtonResIds, View.INVISIBLE);
            setButtonVisibility(m_AdvButtonResIds, View.VISIBLE);
            return;
        }

        if (XONObjectCache.getObjectForKey("XONIMViewType") != null) {
            m_XONIMViewType = (XONIMViewType) XONObjectCache.
                    getObjectForKey("XONIMViewType");
            return;
        }

        m_XONIMViewType = XONIMViewType.Basic;
        Log.i(TAG, "Set XON View Type: "+m_XONIMViewType);
        setButtonVisibility(m_AdvButtonResIds, View.INVISIBLE);
        setButtonVisibility(m_BasicButtonResIds, View.VISIBLE);
        XONObjectCache.addObject("XONIMViewType", m_XONIMViewType, true);
    }

    private void initXONIMButtons()
    {
        initXONActions();
        initXONIMBasicButtons();
    }

    private void initXONActions()
    {
//      final String selTip = (String)  getString(R.string.selected_txt);
        Button button = (Button) findViewById(R.id.home_btn);
        m_ImageProcessButtons.put(R.id.home_btn, button);
        ScrollView scrollView = (ScrollView) findViewById(R.id.home_btn_scroll);
        m_ScrollMenu.put(R.id.home_btn, scrollView);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean showScrollMenu = true;
                if (m_ActiveMainMenu == R.id.home_btn && m_ScrollMenuOn) showScrollMenu = false;
                else { m_ActiveMainMenu = R.id.home_btn; m_ActiveSubMenu = -1; }
                highlightButton(showScrollMenu);
            }
        });

        button = (Button) findViewById(R.id.xon_main_btn);
        m_ImageSubProcessButtons.put(R.id.xon_main_btn, button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                m_ActiveMainMenu = R.id.home_btn;
                m_ActiveSubMenu = R.id.xon_main_btn; highlightButton(false);
                resetXONIMViewType(); finish();
                IntentUtil.processIntent(XON_IM_UI.this, XON_Main_UI.class);
            }
        });

        button = (Button) findViewById(R.id.xon_orig_btn);
        m_ImageSubProcessButtons.put(R.id.xon_orig_btn, button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                m_ActiveMainMenu = R.id.home_btn;
                m_ActiveSubMenu = R.id.xon_orig_btn; highlightButton(false);
                XON_IM_UI.this.m_XONCanvasView.showOriginalImage();
            }
        });

        button = (Button) findViewById(R.id.xon_rot_btn);
        m_ImageSubProcessButtons.put(R.id.xon_rot_btn, button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                m_ActiveMainMenu = R.id.home_btn;
                m_ActiveSubMenu = R.id.xon_rot_btn; highlightButton(false);
            }
        });

        button = (Button) findViewById(R.id.xon_save_btn);
        m_ImageSubProcessButtons.put(R.id.xon_save_btn, button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                m_ActiveMainMenu = R.id.home_btn;
                m_ActiveSubMenu = R.id.xon_save_btn; highlightButton(false);

            }
        });
        button = (Button) findViewById(R.id.xon_crop_btn);
        m_ImageSubProcessButtons.put(R.id.xon_crop_btn, button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                m_ActiveMainMenu = R.id.home_btn;
                m_ActiveSubMenu = R.id.xon_crop_btn; highlightButton(false);
                Log.i(TAG, "Pressed Crop View");
                IntentUtil.processIntent(XON_IM_UI.this, XONImageCropActivity.class);
                m_XONCleanType = XONCleanType.Local; finish();
            }
        });
    }

    private void initXONIMBasicButtons()
    {
        final String selTip = (String) getString(R.string.selected_txt);
        Button button = (Button) findViewById(R.id.basic_popular_effects_btn);
        m_ImageProcessButtons.put(R.id.basic_popular_effects_btn, button);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                m_ActiveMainMenu = R.id.basic_popular_effects_btn; m_ActiveSubMenu = -1;
                highlightButton(false);
                UIUtil.showShortMessage(XON_IM_UI.this,
                        selTip+" "+getString(R.string.popular_effects));
                activateXONImageView(R.id.quick_effects_btn, R.string.popular_effects);
            }
        });

    }

    public void highlightButton(boolean showScrollMenu)
    {
        int bgSelResId = R.color.XONTitleSelBackground;

        m_ScrollMenuOn = showScrollMenu;
        Collection<Button> buttons = m_ImageProcessButtons.values();
        Iterator<Button> buttonIter = buttons.iterator();
        while (buttonIter.hasNext()) buttonIter.next().setBackgroundColor(Color.TRANSPARENT);
        Button homeBtn = m_ImageProcessButtons.get(R.id.home_btn);
        homeBtn.setBackgroundResource(R.color.XONHomeButtonBG);

        buttons = m_ImageSubProcessButtons.values();
        buttonIter = buttons.iterator();
        while (buttonIter.hasNext()) buttonIter.next().setBackgroundColor(Color.TRANSPARENT);

        Collection<ScrollView> scrollMenus = m_ScrollMenu.values();
        Iterator<ScrollView> scrollIter = scrollMenus.iterator();
        while (scrollIter.hasNext()) scrollIter.next().setVisibility(View.INVISIBLE);

        Button button = m_ImageProcessButtons.get(m_ActiveMainMenu);
        if (m_ActiveMainMenu == R.id.home_btn) homeBtn.setTextColor(Color.BLACK);
        else homeBtn.setTextColor(Color.WHITE);
        if (button != null) button.setBackgroundColor(
                Color.parseColor(XONPropertyInfo.getString(bgSelResId)));

        button = m_ImageSubProcessButtons.get(m_ActiveSubMenu);
        if (button != null) button.setBackgroundColor(
                Color.parseColor(XONPropertyInfo.getString(bgSelResId)));

        ScrollView scrollMenu = null;
        if (showScrollMenu) {
            scrollMenu = m_ScrollMenu.get(m_ActiveMainMenu);
            if (scrollMenu != null) {
                scrollMenu.bringToFront();
                scrollMenu.setVisibility(View.VISIBLE);
            }
        }
        m_ActiveScrollView = scrollMenu;
        if (m_XONImage != null) m_XONImage.resetActiveImageFilter();
    }


    private void activateXONImageView(int mainMenu, int imgFltrResId)
    {
        calcFrameLayoutSize();
        if (m_LayoutSize == null && m_LayoutRect == null) {
            Log.i(TAG, "Unable Apply Image "+XONPropertyInfo.getString(imgFltrResId));
            return;
        }
        Log.i(TAG, "Apply Image "+XONPropertyInfo.getString(imgFltrResId));
        if (m_XONImage == null) {
            Log.i(TAG, "Creating XONImage for: "+m_ImageUri);
            Dimension thumbSize = XONPropertyInfo.getThumbSize();
            m_XONImage = new XONImageHolder(m_ImageUri, ViewType.CanvasView, m_LayoutSize,
                    thumbSize.width, thumbSize.height);
            m_XONImage.deriveDeviceCTM(0, null, ScalingFactor.FillLogic.FIT_IMAGE_IN_BBOX);
        } else {
            Log.i(TAG, "View Type: "+m_XONImage.m_ViewType);
            if (!m_XONImage.m_ViewType.equals(ViewType.CanvasView))
                m_XONImage.setPanelSize(m_LayoutSize, ViewType.CanvasView);
        }

        if (m_XONImageFilterView == null) {
            XONHorzScrollView listview = (XONHorzScrollView) findViewById(R.id.filter_listview);
            m_XONImageFilterView =  XONImageFilterView.getInstance(this, m_XONCanvasView, listview);
        }
        m_XONImage.resetDisplayImage(false);
        m_XONImageFilterView.activateXONImageFilterView(mainMenu, imgFltrResId, m_XONImage);

        // Building up the Template Filters in Background Thread
        if (!m_ImageFilterActivated ) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("Request", "BuildTemplateFiltersReq");
            ThreadCreator.getInstance().createThread(this, data, true);
        }
        m_ImageFilterActivated = true;

        SaveSharedPreference sharedPreference = new SaveSharedPreference(this);
        String uriString = sharedPreference.getPreference("cropped_image_uri");
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Cropping image....");
        progress.show();
        if(uriString.contains("/"))
        {
            showFilters(this);
            progress.dismiss();
        }
    }

    private void calcFrameLayoutSize()
    {
        if (m_LayoutSize != null && m_LayoutRect != null) return;
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.xon_graphics_holder);
        int left = 0, right = 0, top = 0, bot = 0, layoutWt = 0, layoutHt = 0;
        if (frameLayout != null) {
            left = frameLayout.getLeft(); top = frameLayout.getTop();
            right = frameLayout.getRight(); bot = frameLayout.getBottom();
            layoutWt = frameLayout.getWidth(); layoutHt = frameLayout.getHeight();
        }

        if (layoutWt <= 0 || layoutHt <= 0) return;

        m_LayoutSize = new Dimension(layoutWt, layoutHt);
        m_LayoutRect = new Rect(); m_LayoutRect.set(left, top, right, bot);
        Log.i(TAG, "Layout Wt: "+layoutWt+" Ht: "+layoutHt+
                " Layout Rect: "+m_LayoutRect);
        Log.i(TAG, "Rect Wt: "+m_LayoutRect.width()+" Ht: "+
                m_LayoutRect.height());
        m_XONCanvasView.m_LayoutRect = m_LayoutRect;
    }

    @Override
    public void processThreadRequest(Map<String, Object> data)
    {
        String req = (String) data.get("Request");
        Log.i(TAG, "Req is: "+req);
        if (req.equals("BuildTemplateFiltersReq")) {
            RawCacheManager.getInstance();
            if (m_XONIMViewType.equals(XONIMViewType.Basic)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button button = (Button) findViewById(
                                R.id.basic_popular_effects_btn);
                        button.performClick();
                    }
                });
            }
            m_XONImageManager.buildTemplateFilters();
        } else if (req.equals("CheckFilterActivatedReq")) {
            while(!m_ImageFilterActivated) {
                XONUtil.put2Sleep();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button button = (Button) findViewById(R.id.basic_popular_effects_btn);
                        if (m_XONIMViewType.equals(XONIMViewType.Advanced))
                            button = (Button) findViewById(R.id.image_enhance_btn);
                        button.performClick();
                    }
                });
            }
        }
    }
}