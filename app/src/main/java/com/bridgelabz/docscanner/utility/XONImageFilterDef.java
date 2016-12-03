package com.bridgelabz.docscanner.utility;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.util.SparseIntArray;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.controller.ImageFilterHandlerImpl;
import com.bridgelabz.docscanner.controller.TemplateFilterHolder;
import com.bridgelabz.docscanner.interfaces.ImageFilterHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by bridgeit on 28/11/16.
 */

public class XONImageFilterDef {
    public static final String TAG = "XONImageFilterDef";

    public static SparseIntArray m_ImageFilterResId, m_ImageFilterDispResId;
    public static Map<String, Vector<String>> m_ImageFilterResources;
    public static Map<String, Vector<String>> m_ImageFilterRules;

    static {
        m_ImageFilterResources = new HashMap<String, Vector<String>>();
        // This map specifies the Image Filter Resource
        m_ImageFilterResId = new SparseIntArray();
        m_ImageFilterResId.put(R.string.image_enhance, R.array.ImageEnhance);
        m_ImageFilterDispResId = new SparseIntArray();
        m_ImageFilterDispResId.put(R.string.image_enhance, R.array.ImageEnhanceDispName);
    }


    public static enum ImageFilter {

        // Template Image Filter - This is either user defined or system defined
        PopularTemplateFilter, PopularTemplateFilter_On, PopularTemplateFilter_Off,
        AdvTemplateFilter, AdvTemplateFilter_On, AdvTemplateFilter_Off,
        PersonalizedTemplateFilter, PersonalizedTemplateFilter_On, PersonalizedTemplateFilter_Off,


        // Enhance Filters
        AutoFix1Enhance, AutoFix1Enhance_On, AutoFix1Enhance_Off,
        AutoFix2Enhance, AutoFix2Enhance_On, AutoFix2Enhance_Off,
        FixMthd2Enhance, FixMthd2Enhance_On, FixMthd2Enhance_Off,
        ContrastEnhance, ContrastEnhance_On, ContrastEnhance_Off,
        BrightnessEnhance, BrightnessEnhance_On, BrightnessEnhance_Off,
        ExposureEnhance, ExposureEnhance_On, ExposureEnhance_Off,
        GainEnhance, GainEnhance_On, GainEnhance_Off,
        GlowEnhance, GlowEnhance_On, GlowEnhance_Off,
        HueEnhance, HueEnhance_On, HueEnhance_Off,
        SaturationEnhance, SaturationEnhance_On, SaturationEnhance_Off,
        ColorBrightEnhance, ColorBrightEnhance_On, ColorBrightEnhance_Off,
        BiasEnhance, BiasEnhance_On, BiasEnhance_Off;

        static Vector<String> getImageFilterList()
        {
            String[] imgFltrsArr = XONUtil.objToStringArray(ImageFilter.values(), "_");
            Vector<String> imageFltrLists = XONUtil.createVector(imgFltrsArr);
            return imageFltrLists;
        }

        // Based on the Image Filter, this method creates and returns the Image Filter Handler
        public static ImageFilterHandler createImageFilterHandler(String imgFltr)
        {
            Log.i(TAG, "Creating Image Fltr Hdlr for: "+imgFltr);
            if (!imgFltr.contains("TemplateFilter"))
                return new ImageFilterHandlerImpl(imgFltr);
            return TemplateFilterHolder.getTemplateFilterImpl(imgFltr);
        }


        public static boolean showColorPickerDialog(String imgFltr)
        {
          /*  if (imgFltr.equals(ImageFilter.ColorFadeBorderFilters.toString()) ||
                    imgFltr.equals(ImageFilter.CrystalBorderFilters.toString()) ||
                    imgFltr.equals(ImageFilter.HexCrystalBorderFilters.toString()) ||
                    imgFltr.equals(ImageFilter.OctaCrystalBorderFilters.toString()) ||
                    imgFltr.equals(ImageFilter.CrystalBlurFilters.toString()) ||
                    imgFltr.equals(ImageFilter.HexCrystalBlurFilters.toString()) ||
                    imgFltr.equals(ImageFilter.OctCrystalBlurFilters.toString()) ||
                    imgFltr.equals(ImageFilter.ColorDotsColorEffects.toString()) ||
                    imgFltr.equals(ImageFilter.ColorFadeFadeEffects.toString()) ||
                    imgFltr.equals(ImageFilter.ColorCrystalFadeEffects.toString()))
            {
                Log.i(TAG, "Show Dialog for: "+imgFltr);
                return true;
            }*/

            String framesGrpName = XONPropertyInfo.getString(R.string.frame_effects, true);
            if (imgFltr.toString().contains(framesGrpName)) return true;
            return false;
        }
    }

    private static void populateFilterRules(Resources res)
    {
        String[] imgFltrRules = res.getStringArray(R.array.ImageFilterRules);
        for (int i = 0; i < imgFltrRules.length; i++)
        {
            String[] imgFltrRule = imgFltrRules[i].split("::");
            String usedFltr = (imgFltrRule[0].split(":="))[1];
            String negateFltr = (imgFltrRule[1].split(":="))[1];
            String[] negateFltrArr = negateFltr.split(":N:");
            Vector<String> negateFltrs = new Vector<String>();
            for (int iter = 0; iter < negateFltrArr.length; iter++)
                negateFltrs.add(negateFltrArr[iter]);
            m_ImageFilterRules.put(usedFltr, negateFltrs);
        }
        Log.i(TAG, "ImageFilterRules: "+m_ImageFilterRules);
    }

    public static void populateImageFilterResource(Activity act)
    {
        int keyResId, valResId; String imgFltrGrp = ""; String[] imgFltrs;
        String packName = act.getPackageName(); Resources res = act.getResources();
        m_ImageFilterRules = new HashMap<>();
        populateFilterRules(res);
        for(int index = 0; index < m_ImageFilterResId.size(); index++)
        {
            keyResId = m_ImageFilterResId.keyAt(index);
            valResId = m_ImageFilterResId.valueAt(index);
            imgFltrGrp = res.getString(keyResId); imgFltrs = res.getStringArray(valResId);
            for (int iter = 0; iter < imgFltrs.length; iter++)
            {
                String imgFltrOpt = getImageFilter(-1, imgFltrGrp, imgFltrs[iter]);
                if (imgFltrOpt == null) {
                    Log.i(TAG, "Image Filter not defined for: "+imgFltrs[iter]);
                    continue;
                }
                String imgFltr = imgFltrOpt.toString();
                int imgFltrResid = res.getIdentifier(imgFltr+"_Val", "array", packName);
                String[] imgFltrSetVals = res.getStringArray(imgFltrResid);
                Vector<String> imgFltrSetList = new Vector<>(Arrays.asList(imgFltrSetVals));
                m_ImageFilterResources.put(imgFltr, imgFltrSetList);
            }
        }
    }

    private static Vector<String> m_ImageFilterList;
    public static Vector<String> getImageFilterList()
    {
        if (m_ImageFilterList == null) m_ImageFilterList = ImageFilter.getImageFilterList();
        return m_ImageFilterList;
    }

    public static boolean containsImageFilter(String imgFltr)
    {
        if (m_ImageFilterList == null) getImageFilterList();
        return m_ImageFilterList.contains(imgFltr);
    }

    public static String getImageFilter(int mainMenuFltrRes, String imgFltrGrp,
                                        String imgFltrNm)
    {
        if (mainMenuFltrRes == R.id.quick_effects_btn) {
            if (imgFltrGrp.equals(XONUtil.replaceSpace(
                    XONPropertyInfo.getString(R.string.popular_effects), "")))
                return getImageFilter(ImageFilter.PopularTemplateFilter, imgFltrNm);
            if (imgFltrGrp.equals(XONUtil.replaceSpace(
                    XONPropertyInfo.getString(R.string.user_effects), "")))
                return getImageFilter(ImageFilter.PersonalizedTemplateFilter, imgFltrNm);
        }
        String imgFltr = XONUtil.replaceSpace(imgFltrNm, "")+
                XONUtil.replaceSpace(imgFltrGrp, "");
        if (!containsImageFilter(imgFltr)) return null;
        return imgFltr;
    }

    public static Vector<Map<String, String>> getImageFilterRes(String imgFltr)
    {

        Vector<String> imgFltrSetList = m_ImageFilterResources.get(imgFltr);
        if (imgFltrSetList == null) {
            Log.i(TAG, "ImageFilter Values not defn for: "+imgFltr);
        }
        Vector<Map<String, String>> imgFltrValMapList = new Vector<Map<String, String>>();
        for (int iter = 0; iter < imgFltrSetList.size(); iter++)
        {
            String imgFltrValStr = imgFltrSetList.elementAt(iter);
            if (!imgFltrValStr.contains("::")) continue;
            String[] imgFltrValues = imgFltrValStr.split("::");
            Map<String, String> imgFltrValMaps = new HashMap<String, String>();
            for (int i = 0; i < imgFltrValues.length; i++)
            {
                if (!imgFltrValues[i].contains(":=")) continue;
                String[] imgFltrKeyVal = imgFltrValues[i].split(":=");
                imgFltrValMaps.put(imgFltrKeyVal[0], imgFltrKeyVal[1]);
            }
            imgFltrValMapList.addElement(imgFltrValMaps);
        }
        return imgFltrValMapList;
    }

    public static String getImageFilter(ImageFilter templImageFltr, String imgFltrNm)
    {
        return templImageFltr.toString()+"::"+imgFltrNm;
    }
}
