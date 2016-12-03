package com.bridgelabz.docscanner.utility;

/**
 * Created by bridgeit on 28/11/16.
 */

import android.util.Log;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.controller.TemplateFilterHolder;
import com.bridgelabz.docscanner.utility.XONImageFilterDef.ImageFilter;

import java.lang.reflect.Field;

public class RawCacheManager
{
    public static final String TAG = "RawCacheManager";
    public static String IMAGE_FILTER_FILES = "filter";
    public static String SHAPE_FILES = "shapes";

    private static RawCacheManager m_RawCacheManager;


    private RawCacheManager()
    {
        buildRawCache();
    }

    private void buildRawCache()
    {
        StringBuffer rawFileNames = new StringBuffer("\nPopular Image Filter Files are: ");
        Field[] fields = R.raw.class.getFields();
        TemplateFilterHolder templFltrHldr = null;
        templFltrHldr = TemplateFilterHolder.getInstance(ImageFilter.PopularTemplateFilter);
        for(int count=0; count < fields.length; count++){
            try {
                String name = fields[count].getName(); int resId = fields[count].getInt(null);
                Log.i(TAG, "Raw Asset Name: "+name+" Value: "+resId);
                if (name.contains(IMAGE_FILTER_FILES)) {
                    int posId = templFltrHldr.buildTemplateFilters(name, resId);
                    rawFileNames.append(name+":"+posId+" ");
                }
            } catch (Exception ex) {}
        }
        Log.i(TAG, rawFileNames.toString());
    }

    public static synchronized RawCacheManager getInstance()
    {
        if (m_RawCacheManager == null) m_RawCacheManager = new RawCacheManager();
        return m_RawCacheManager;
    }
}

