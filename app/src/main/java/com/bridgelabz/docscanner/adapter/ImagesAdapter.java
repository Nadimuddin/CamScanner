package com.bridgelabz.docscanner.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.bridgelabz.docscanner.fragment.ImageFragment;
import com.bridgelabz.docscanner.model.PageDetail;

import java.util.ArrayList;

/**
 * Created by Nadimuddin on 25/11/16.
 */

public class ImagesAdapter extends FragmentStatePagerAdapter
{
    private ArrayList<Uri> mUris;
    private ArrayList<PageDetail> mPageDetails;
    private TextView mPageName;
    private ViewPager mViewPager;
    public ImagesAdapter(FragmentManager fragment, ArrayList<PageDetail> pageDetails, TextView pageName,
                         ViewPager viewPager)
    {
        super(fragment);
        mPageDetails = pageDetails;
        mPageName = pageName;
        mViewPager = viewPager;
    }

    @Override
    public int getCount()
    {
        return mPageDetails.size();
    }

    @Override
    public Fragment getItem(int position)
    {
        Fragment fragment = new ImageFragment();
        Bundle bundle = new Bundle();
        PageDetail pageDetail = mPageDetails.get(position);
        bundle.putSerializable("page_details", pageDetail);
        fragment.setArguments(bundle);
        return fragment;
    }
}