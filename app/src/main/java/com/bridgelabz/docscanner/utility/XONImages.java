package com.bridgelabz.docscanner.utility;

import android.util.Log;

import java.util.Vector;

/**
 * Created by bridgeit on 26/11/16.
 */

public class XONImages {

    public int m_FileCounter = 0;
    public Vector<String> m_XONImages, m_XONThumbImages;

    public static final String TAG = "XONImages";

    public int nextCounter()
    {
        m_FileCounter++; return m_FileCounter;
    }

    public int getCount() { return m_XONImages.size(); }

    public String generatePath(String origPath, String origSplitter, String newSplitter)
    {
        String [] imgFileParts = origPath.split(origSplitter);
        String newPath = imgFileParts[0]+newSplitter+imgFileParts[1];
        Log.i(TAG, "Orig Path: "+origPath+" New Path: "+newPath);
        return newPath;
    }

    public void addXONImageData(String uri, String thumbUri)
    {
        m_XONImages.addElement(uri); m_XONThumbImages.addElement(thumbUri);
        Log.i(TAG, "Saving Full Image: "+m_XONImages.lastElement()+
                "\nThumb Image: "+m_XONThumbImages.lastElement());
    }
}
