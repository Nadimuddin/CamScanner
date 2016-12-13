package com.bridgelabz.docscanner.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.adapter.ImagesAdapter;
import com.bridgelabz.docscanner.utility.DatabaseUtil;
import com.bridgelabz.docscanner.utility.ImageUtil;
import com.bridgelabz.docscanner.utility.StorageUtil;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    File mImage;
    private static boolean FROM_CAMERA = false;

    private static final int CAMERA_REQUEST = 1;
    private static boolean UPDATE_COVER_IMAGE = false;

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
                Uri imageUri = mUris.get(mViewPager.getCurrentItem());
                processImage(imageUri);
                break;

            case R.id.retake:
                StorageUtil storage = new StorageUtil(this);
                Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                //mImage=null;
                try {
                    mImage = storage.createTemporaryFile();
                }
                catch (Exception e) {
                    //Log.i(TAG, "can't create file to take picture! "+e.toString());
                    Toast.makeText(this, "Please check SD card! Image shot is impossible",
                            Toast.LENGTH_SHORT).show();
                }
                Uri uri = Uri.fromFile(mImage);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                break;

            /*case R.id.pageName:
                break;*/

            case R.id.delete:
                /*DatabaseUtil database = new DatabaseUtil(this, "Images");
                Uri currentImageUri = mUris.get(mViewPager.getCurrentItem());
                Cursor cursor = database.retrieveData("select d_id from Images where fltr_image_uri = \""
                        +currentImageUri.getPath()+"\"");
                cursor.moveToNext();
                int docId = cursor.getInt(0);

                deletePage(currentImageUri);*/

                /*mUris.clear();
                mUris = retrieveUriFromDatabase(docId);
                mAdapter = new ImagesAdapter(getSupportFragmentManager(), mUris);
                //mAdapter.notifyDataSetChanged();
                mViewPager.setAdapter(mAdapter);*/

                openAlert();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openAlert()
    {
        DialogInterface.OnClickListener dialog = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        DatabaseUtil database = new DatabaseUtil(getBaseContext(), "Images");

                        Uri currentImageUri = mUris.get(mViewPager.getCurrentItem());
                        Cursor cursor = database.retrieveData("select d_id from Images where fltr_image_uri = \""
                                +currentImageUri.getPath()+"\"");
                        cursor.moveToNext();
                        int docId = cursor.getInt(0);
                        deletePage(currentImageUri);

                        if(mUris.size() == 1)
                        {
                            deleteDocument(docId);
                            Toast.makeText(getBaseContext(), "All pages are deleted from Document so unable" +
                                    "to open this Document", Toast.LENGTH_LONG).show();
                            Intent intent = new  Intent(getBaseContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        else
                        {
                            setImages(docId);
                            if (mViewPager.getCurrentItem() == 0)
                                UPDATE_COVER_IMAGE = true;
                            updateDocumentTable(docId);
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this page?")
                .setPositiveButton("Yes", dialog).setNegativeButton("No", dialog).show();
    }

    private void setImages(int docId)
    {
        mUris.clear();
        mUris = retrieveUriFromDatabase(docId);
        mAdapter = new ImagesAdapter(getSupportFragmentManager(), mUris);
        //mAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mAdapter);
    }

    private void deletePage(Uri uri)
    {
        DatabaseUtil database = new DatabaseUtil(this, "Images");
        StorageUtil storage = new StorageUtil(this);

        storage.deleteImage(uri.getPath());

        String imageName = uri.getPath().substring(uri.getPath().lastIndexOf('/')+1);
        storage.deleteImage(storage.getDirectoryForCroppedImage()+"/"+imageName);
        storage.deleteImage(storage.getDirectoryForOriginalImage()+"/"+imageName);

        database.deleteData("Images", "fltr_image_uri", uri.getPath());
    }

    private void deleteDocument(int docId)
    {
        DatabaseUtil database = new DatabaseUtil(this, "Documents");

        database.deleteData("Documents", "document_id", docId);
    }

    private void updateDocumentTable(int docId)
    {
        DatabaseUtil database = new DatabaseUtil(this, "Documents");
        Cursor cursor = database.retrieveData("select * from Images where d_id = "+docId);
        int numberOfImages = cursor.getCount();
        cursor.close();

        //get current date & time
        String dateTime = DateFormat.getDateTimeInstance().format(new Date());

        ContentValues values = new ContentValues();

        if(UPDATE_COVER_IMAGE)
            values.put("cover_image_uri", mUris.get(0).getPath());
        values.put("date_time", dateTime);
        values.put("number_of_images", numberOfImages);

        database.updateData("Documents", values, "document_id", docId);
    }

    private ArrayList<Uri> retrieveUriFromDatabase(int docId)
    {
        ArrayList<Uri> arrayList = new ArrayList<>();
        DatabaseUtil db = new DatabaseUtil(this, "Images");
        String uriString;

        //get Uri from Images table
        Cursor cursor = db.retrieveData("select fltr_image_uri from Images where d_id = "+docId);
        while (cursor.moveToNext())
        {
            uriString = cursor.getString(0);
            //Log.i(TAG, "retrieveUriFromDatabase: "+uriString);
            arrayList.add(Uri.fromFile(new File(uriString)));
        }
        cursor.close();
        return arrayList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result)
    {
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
        {
            ImageUtil imageUtil = new ImageUtil(this);
            StorageUtil storage = new StorageUtil(this);

            Bitmap bitmap = imageUtil.compressImage(mImage);
            //int imageId = imageUtil.nextImageID("Images");
            String currentImagePath = mUris.get(mViewPager.getCurrentItem()).getPath();
            String currentImageName = currentImagePath.substring(currentImagePath.lastIndexOf('/')+1);
            String directory = storage.getDirectoryForFilteredImage();

            Uri imageUri = storage.storeImage(bitmap, directory, currentImageName);
            FROM_CAMERA = true;
            processImage(imageUri);
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }

    private void processImage(Uri imageUri)
    {
        //IntentUtil.processIntent(this, ImageCropping.class, imageUri);
        DatabaseUtil database = new DatabaseUtil(this, "Images");
        Cursor cursor = database.retrieveData("select org_image_uri from Images " +
                "where fltr_image_uri = \""+imageUri.getPath()+"\"");
        cursor.moveToNext();
        String uriString = cursor.getString(0);
        cursor.close();
        Uri uri;

        if(FROM_CAMERA)
            uri = imageUri;
        else
            uri = Uri.fromFile(new File(uriString));

        Intent intent = new Intent(this, ImageCropping.class);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra("from", "ImageViewer");
        startActivity(intent);
    }
}
