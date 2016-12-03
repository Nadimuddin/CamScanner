package com.bridgelabz.docscanner.model;

/**
 * Created by bridgelabz1 on 17/11/16.
 */

public class FilterModel
{
    private int mFilterIconIdBlack;
    private int mFilterIconIdWhite;
    private String mFilterName;

    public FilterModel(int filterIconIdBlack, int filterIconIdWhite, String filterName)
    {
        mFilterIconIdBlack = filterIconIdBlack;
        mFilterIconIdWhite = filterIconIdWhite;
        mFilterName = filterName;
    }

    public int getFilterIconIdBlack()
    {
        return mFilterIconIdBlack;
    }

    public int getFilterIconIdWhite()
    {
        return mFilterIconIdWhite;
    }

    public String getFilterName()
    {
        return mFilterName;
    }
}