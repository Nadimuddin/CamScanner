package com.bridgelabz.docscanner.model;


/**
 * Created by Nadimuddin on 19/10/16.
 */
public class DocumentDetails
{
    private String mDocumentName;
    private String mCoverImageUri;
    private String mDateTime;
    private int mImageCount;

    public DocumentDetails(String documentName, String imageUri, String dateTime, int imageCount)
    {
        mDocumentName = documentName;
        mCoverImageUri = imageUri;
        mDateTime = dateTime;
        mImageCount = imageCount;
    }

    public String getDocumentName()
    {
        return mDocumentName;
    }

    public String getImageUri()
    {
        return mCoverImageUri;
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
