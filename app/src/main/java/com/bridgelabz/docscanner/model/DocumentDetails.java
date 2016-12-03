package com.bridgelabz.docscanner.model;


/**
 * Created by Nadimuddin on 19/10/16.
 */
public class DocumentDetails
{
    private String mDocumentName;
    private String mImageUri;
    private String mDateTime;
    private int mImageCount;

    public DocumentDetails(String documentName, String imageUri, String dateTime, int imageCount)
    {
        mDocumentName = documentName;
        mImageUri = imageUri;
        mDateTime = dateTime;
        mImageCount = imageCount;
    }

    public String getDocumentName()
    {
        return mDocumentName;
    }

    public String getImageUri()
    {
        return mImageUri;
    }

    public String getDateTime() {
        return mDateTime;
    }

    public int getImageCount()
    {
        return mImageCount;
    }

    public void setDocumentName(String documentName)
    {
        mDocumentName = documentName;
    }
}
