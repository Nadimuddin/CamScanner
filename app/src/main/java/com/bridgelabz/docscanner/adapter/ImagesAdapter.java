package com.bridgelabz.docscanner.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bridgelabz.docscanner.fragment.ImageFragment;

import java.util.ArrayList;

/**
 * Created by Nadimuddin on 25/11/16.
 */

public class ImagesAdapter extends FragmentStatePagerAdapter
{
    private ArrayList<Uri> mUris;
    public ImagesAdapter(FragmentManager fragment, ArrayList<Uri> uris)
    {
        super(fragment);
        mUris = uris;
    }

    @Override
    public int getCount()
    {
        return mUris.size();
    }

    @Override
    public Fragment getItem(int position)
    {
        Fragment fragment = new ImageFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("image_uri", mUris.get(position));
        fragment.setArguments(bundle);
        return fragment;
    }
}
