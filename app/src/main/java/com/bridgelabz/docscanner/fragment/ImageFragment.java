package com.bridgelabz.docscanner.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.model.PageDetail;

import java.io.File;

/**
 * Created by Nadimuddin on 25/11/16.
 */

public class ImageFragment extends Fragment
{
    ImageView mImageView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.images, container, false);

        Bundle arg = getArguments();
        PageDetail pageDetail = (PageDetail) arg.getSerializable("page_details");
        String uriString = pageDetail.getImageUri();
        Uri uri = Uri.fromFile(new File(uriString));

        mImageView = (ImageView)view.findViewById(R.id.images);
        mImageView.setImageURI(uri);

        return view;
    }
}
