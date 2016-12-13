package com.bridgelabz.docscanner.model;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Nadimuddin on 13/12/16.
 */

public class PageDetail implements Serializable
{
    private String  mImageUriString;
    private String mPageName;

    public PageDetail(String imageUriString, String pageName) {
        mImageUriString = imageUriString;
        mPageName = pageName;
    }

    public String getImageUri()
    {
        return mImageUriString;
    }

    public String getPageName()
    {
        return mPageName;
    }
}
