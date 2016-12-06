package com.bridgelabz.docscanner.fragment;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.activities.DocumentActivity;
import com.bridgelabz.docscanner.adapter.FilterAdapter;
import com.bridgelabz.docscanner.imagefilter.BitmapFilter;
import com.bridgelabz.docscanner.interfaces.FilterImage;
import com.bridgelabz.docscanner.model.FilterModel;
import com.bridgelabz.docscanner.recyclerview_divider.DividerItemDecoration;
import com.bridgelabz.docscanner.recyclerview_divider.HorizontalSpaceItemDecoration;
import com.bridgelabz.docscanner.utility.DatabaseUtil;
import com.bridgelabz.docscanner.utility.StorageUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Nadimuddin on 14/11/16.
 */

public class ImageFiltering extends Fragment implements View.OnClickListener, FilterImage
{
    View mView;
    RecyclerView mRecyclerView;
    ArrayList<FilterModel> mArrayList;
    ImageView mImageView;
    Uri mUri;
    Bitmap mOriginalBitmap = null;
    Bitmap mChangeBitmap[];
    private static final int  ORIGINAL = 2;
    private static final String TAG = "ImageFiltering";
    private static final int HORIZONTAL_ITEM_SPACE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.image_filtering, container, false);

        String uriString = getArguments().getString("image_uri").substring(7);
        mUri = Uri.fromFile(new File(uriString));

        mArrayList = new ArrayList<>();

        mRecyclerView = (RecyclerView)mView.findViewById(R.id.filterList);
        mImageView = (ImageView)mView.findViewById(R.id.image_view);

        ImageButton backButton = (ImageButton)mView.findViewById(R.id.backButton);
        ImageButton rotateLeft = (ImageButton)mView.findViewById(R.id.rotate_left);
        ImageButton rotateRight = (ImageButton)mView.findViewById(R.id.rotate_right);
        ImageButton modify = (ImageButton)mView.findViewById(R.id.modify);
        ImageButton done = (ImageButton)mView.findViewById(R.id.done);

        mArrayList.add(new FilterModel(R.drawable.ic_wb_auto_black_24dp,
                R.drawable.ic_wb_auto_white_24dp, "Auto"));
        mArrayList.add(new FilterModel(R.drawable.ic_photo_black_24dp,
                R.drawable.ic_photo_white_24dp, "Original"));
        mArrayList.add(new FilterModel(R.drawable.ic_brightness_5_black_24dp,
                R.drawable.ic_brightness_5_white_24dp, "Brighter"));
        mArrayList.add(new FilterModel(R.drawable.ic_opacity_black_24dp,
                R.drawable.ic_opacity_white_24dp, "Contrast"));
        mArrayList.add(new FilterModel(R.drawable.ic_wb_cloudy_black_24dp,
                R.drawable.ic_wb_cloudy_white_24dp, "Grey Mode"));
        mArrayList.add(new FilterModel(R.drawable.ic_tonality_black_24dp,
                R.drawable.ic_tonality_white_24dp, "B & W"));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        FilterAdapter adapter = new FilterAdapter(mArrayList, new FilterImage() {
            @Override
            public void applyChangesToImage(int style)
            {
                Log.i(TAG, "applyChangesToImage: style "+style+1);
                try {
                    applyStyle(style);
                }
                catch (Exception e) {
                    Toast.makeText(getActivity(), "Filter error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(HORIZONTAL_ITEM_SPACE));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.divider));
        mRecyclerView.setAdapter(adapter);

        backButton.setOnClickListener(this);
        rotateLeft.setOnClickListener(this);
        rotateRight.setOnClickListener(this);
        modify.setOnClickListener(this);
        done.setOnClickListener(this);

        mImageView.setImageURI(mUri);

        BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
        mOriginalBitmap = drawable.getBitmap();
        return mView;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.backButton:
                //Toast.makeText(mView.getContext(), "back button is not implemented yet", Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
                //getActivity().finish();
                break;

            case R.id.rotate_left:
                Toast.makeText(mView.getContext(), "you clicked on 'Rotate Left'", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.rotate_right:
                Toast.makeText(mView.getContext(), "you clicked on 'Rotate Right'", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.modify:
                Toast.makeText(mView.getContext(), "you clicked on 'Modify'", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.done:
                storeFilteredImage();

                openDocumentActivity();
                //getActivity().finish();
                break;
        }
    }

    private void storeFilteredImage()
    {
        StorageUtil storage = new StorageUtil(getActivity());
        Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        String directory = storage.getDirectoryForFilteredImage();
        String uriString = mUri.toString();
        String  imageName = uriString.substring(uriString.lastIndexOf('/')+1);
        Uri uri = storage.storeImage(bitmap, directory, imageName);

        updateFilteredImageUri(uri, imageName);
        Log.i(TAG, "onClick: "+imageName);
    }

    private void updateFilteredImageUri(Uri uri, String imageName)
    {
        DatabaseUtil database = new DatabaseUtil(getActivity(), "Images");
        ContentValues values = new ContentValues();
        values.put("fltr_image_uri", uri.toString());
        int updatedColumns = database.updateData("Images", values, "i_name", imageName);
        Toast.makeText(getActivity(), updatedColumns+" column updated", Toast.LENGTH_SHORT).show();
    }

    private void openDocumentActivity()
    {
        Intent intent = new Intent(getActivity(), DocumentActivity.class);
        DatabaseUtil database = new DatabaseUtil(getActivity(), "Documents");

        String uriString = mUri.toString();
        String imageName = uriString.substring(uriString.lastIndexOf('/')+1);

        Cursor cursor = database.retrieveData("select d_name from Documents where document_id =" +
                "(select d_id from Images where crp_image_uri = \""+mUri.toString()+"\")");
        cursor.moveToNext();
        String docName = cursor.getString(0);
        cursor.close();
        intent.putExtra("document_name", docName);

        getActivity().finish();
        startActivity(intent);
    }

    private void applyStyle(int styleNo)
    {
        switch (styleNo)
        {
            case BitmapFilter.AUTOFIX_ENHANCE_STYLE:
                mChangeBitmap = BitmapFilter.changeStyle(mOriginalBitmap, BitmapFilter.AUTOFIX_ENHANCE_STYLE);
                Toast.makeText(mView.getContext(), "AUTOFIX_ENHANCE_STYLE", Toast.LENGTH_SHORT).show();
                break;

            case ORIGINAL:
                mChangeBitmap = new Bitmap[]{mOriginalBitmap};
                Toast.makeText(mView.getContext(), "ORIGINAL IMAGE", Toast.LENGTH_SHORT).show();
                break;

            case BitmapFilter.BRIGHTNESS_ENHANCE_STYLE:
                mChangeBitmap = BitmapFilter.changeStyle(mOriginalBitmap, BitmapFilter.BRIGHTNESS_ENHANCE_STYLE);
                Toast.makeText(mView.getContext(), "BRIGHTNESS_ENHANCE_STYLE", Toast.LENGTH_SHORT).show();

                break;
            case BitmapFilter.CONTRAST_ENHANCE_STYLE:
                mChangeBitmap = BitmapFilter.changeStyle(mOriginalBitmap, BitmapFilter.CONTRAST_ENHANCE_STYLE);
                Toast.makeText(mView.getContext(), "CONTRAST_ENHANCE_STYLE", Toast.LENGTH_SHORT).show();
                break;

            case BitmapFilter.GREY_COLOR_STYLE:
                mChangeBitmap = BitmapFilter.changeStyle(mOriginalBitmap, BitmapFilter.GREY_COLOR_STYLE);
                Toast.makeText(mView.getContext(), "GREY_COLOR_STYLE", Toast.LENGTH_SHORT).show();
                break;

            case BitmapFilter.BW_STYLE:
                mChangeBitmap = BitmapFilter.changeStyle(mOriginalBitmap, BitmapFilter.BW_STYLE);
                Toast.makeText(mView.getContext(), "B&W_STYLE", Toast.LENGTH_SHORT).show();
                break;

            default:
                mChangeBitmap = BitmapFilter.changeStyle(mOriginalBitmap, styleNo);
                break;
        }
        mImageView.setImageBitmap(mChangeBitmap[0]);
    }

    @Override
    public void applyChangesToImage(int style) {

    }
}
