package com.bridgelabz.docscanner.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.adapter.ImagesAdapter;
import com.bridgelabz.docscanner.utility.DatabaseUtil;
import com.bridgelabz.docscanner.utility.IntentUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Nadimuddin on 25/11/16.
 */

public class ImageViewer extends AppCompatActivity implements View.OnClickListener
{
    Toolbar mToolbar;
    ImageButton mBackButton;
    ViewPager mViewPager;
    ImagesAdapter mAdapter;
    ArrayList<Uri> mUris;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);

        mUris = (ArrayList<Uri>) getIntent().getSerializableExtra("list_of_images");

        int currentItem = getIntent().getIntExtra("current_image", 0);
        mToolbar = (Toolbar)findViewById(R.id.imageViewerToolbar);
        mBackButton = (ImageButton)findViewById(R.id.backButton);
        mViewPager = (ViewPager)findViewById(R.id.view_pager);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");

        mAdapter = new ImagesAdapter(getSupportFragmentManager(), mUris);

        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(currentItem);
        mBackButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.backButton:
                onBackPressed();
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.image_viewer_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.crop:
                /*Intent intent = new Intent(this, ImageCropping.class);
                intent.putExtra("image_uri", mUris.get(mViewPager.getCurrentItem()).toString());
                startActivity(intent);*/
                Uri imageUri = mUris.get(mViewPager.getCurrentItem());
                processImage(imageUri);
                break;

            /*case R.id.retake:
                break;

            case R.id.pageName:
                break;

            case R.id.delete:
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    private void processImage(Uri imageUri)
    {
        //IntentUtil.processIntent(this, ImageCropping.class, imageUri);
        DatabaseUtil database = new DatabaseUtil(this, "Images");
        Cursor cursor = database.retrieveData("select org_image_uri from Images " +
                "where fltr_image_uri = \""+imageUri.toString()+"\"");
        cursor.moveToNext();
        String uriString = cursor.getString(0);
        cursor.close();

        Uri uri = Uri.fromFile(new File(uriString.substring(7)));

        Intent intent = new Intent(this, ImageCropping.class);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra("from", "ImageViewer");
        startActivity(intent);
    }
}
