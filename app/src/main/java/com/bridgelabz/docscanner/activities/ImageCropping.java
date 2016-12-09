package com.bridgelabz.docscanner.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bridgelabz.docscanner.BuildConfig;
import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.controller.XONImageHolder;
import com.bridgelabz.docscanner.utility.DatabaseUtil;
import com.bridgelabz.docscanner.utility.Dimension;
import com.bridgelabz.docscanner.utility.ImageUtil;
import com.bridgelabz.docscanner.utility.IntentUtil;
import com.bridgelabz.docscanner.utility.ScalingFactor;
import com.bridgelabz.docscanner.utility.StorageUtil;
import com.bridgelabz.docscanner.utility.XONObjectCache;
import com.bridgelabz.docscanner.utility.XONPropertyInfo;

import java.io.File;

/**
 * Created by Nadimuddin on 13/11/16.
 */

public class ImageCropping extends AppCompatActivity implements View.OnClickListener
{
    ImageView mImageView;
    Toolbar mBottomToolbar;
    ImageButton backButton, rotateRight, rotateLeft, cropUncrop, done;

    // Size to display the image in the Canvas View
    Rect mLayoutRect; Dimension mLayoutSize;

    XONCropView mXONCropView;

    // Image URI and Image Holder Object
    Uri mImageUri; XONImageHolder mXONImage;

    File mImage;

    DialogInterface.OnClickListener mDialog;

    private String mFrom;
    private static final String MAIN_ACTIVITY = "MainActivity", DOCUMENT_ACTIVITY = "DocumentActivity",
            IMAGE_VIEWER = "ImageViewer", IMAGE_CROPPING = "ImageCropping";

    ProgressDialog mProgressDialog;

    private static final String TAG = "ImageCropping";
    private static final int CAMERA_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_cropping);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Cropping Image...");
        mProgressDialog.show();

        XONPropertyInfo.populateResources(this, BuildConfig.DEBUG, false);
        mImageUri = IntentUtil.getImageIntent(this);

        mFrom = getIntent().getStringExtra("from");
        Log.i(TAG, "onCreate: Previous activity: "+mFrom);

        if (mImageUri == null) {
            if (XONObjectCache.getObjectForKey("XONImage") != null) {
                mXONImage = (XONImageHolder) XONObjectCache.getObjectForKey("XONImage");
                mImageUri = mXONImage.m_Uri;
                Log.i(TAG, "XON View Type: "+mXONImage.m_ViewType);
            } else {
                //IntentUtil.processIntent(this, ImageCropping.class);
                return;
            }
        } else XONPropertyInfo.setSuperLongToastMessage(R.string.ShowCropMessage);

        Log.i(TAG, "Image Uri: "+mImageUri+" Uri Path: "+mImageUri.getPath());

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.xon_graphics_holder);
        mXONCropView = new XONCropView(this); frameLayout.addView(mXONCropView);

        // The view tree observer is used to get notifications when global events, like layout,
        // happens. Listener is attached to get the final width of the GridView and then calc the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mXONCropView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener()
                {
                    // Register a callback to be invoked when the global layout state or the visibility of
                    // views within the view tree changes
                    @Override
                    public void onGlobalLayout()
                    {
                        calcFrameLayoutSize();
                        Log.i(TAG, "CropView Layout Size " + mLayoutSize);
                        mProgressDialog.dismiss();
                    }
                });

        mImageView = (ImageView)findViewById(R.id.image_view);
        mBottomToolbar = (Toolbar)findViewById(R.id.toolbar_bottom_i_e);
        backButton = (ImageButton)findViewById(R.id.backButton);
        rotateLeft = (ImageButton)findViewById(R.id.rotate_left);
        rotateRight = (ImageButton)findViewById(R.id.rotate_right);
        cropUncrop = (ImageButton)findViewById(R.id.crop_uncrop);
        done = (ImageButton)findViewById(R.id.done);

        //setSupportActionBar(mBottomToolbar);
        //mBottomToolbar.setTitle("Bottom Toolbar");

        backButton.setOnClickListener(this);
        rotateLeft.setOnClickListener(this);
        rotateRight.setOnClickListener(this);
        cropUncrop.setOnClickListener(this);
        done.setOnClickListener(this);

    }

    /*private void saveInPreference(Uri uri)
    {
        SaveSharedPreference sharedPreference = new SaveSharedPreference(this);
        String uriString = uri.toString();
        sharedPreference.setPreferences(HANDLING_BACK_PRESS_KEY, uriString);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    private void goToXON_IM_UI()
    {
        IntentUtil.processIntent(this, XON_IM_UI.class);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        openAlert();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.backButton:
                //onBackPressed();
                openAlert();
                break;

            case R.id.rotate_left:
                Toast.makeText(this, "Rotate Left is not implemented yet", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.rotate_right:
                Toast.makeText(this, "Rotate Right is not implemented yet", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.crop_uncrop:
                Toast.makeText(this, "Crop is not implemented yet", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.done:
                goToXON_IM_UI();
                break;
        }
    }

    private void openAlert()
    {
        mDialog = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        if(mFrom.equals(MAIN_ACTIVITY) || mFrom.equals(DOCUMENT_ACTIVITY) ||
                                mFrom.equals(IMAGE_CROPPING))
                        {
                            deleteRecord();
                            openCamera();
                        }
                        else if(mFrom.equals(IMAGE_VIEWER))
                            finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to discard this image?")
                .setPositiveButton("Discard!", mDialog).setNegativeButton("Cancel", mDialog).show();
    }

    private void deleteRecord()
    {
        DatabaseUtil database = new DatabaseUtil(this, "Images");

        Log.i(TAG, "deleteRecord: "+mImageUri.getPath());

        Cursor cursor = database.retrieveData("select d_id from Images where " +
                "org_image_uri = \""+mImageUri.toString()+"\"");
        cursor.moveToNext();
        int docId = cursor.getInt(0);

        int deletedRows = database.deleteData("Images", "org_image_uri", mImageUri.toString());
        //Toast.makeText(this, deletedRows+" rows deleted from Images", Toast.LENGTH_SHORT).show();

        int numberOfPages = database.retrieveData("select * from Images where d_id = "+docId).getCount();

        ContentValues values = new ContentValues();
        values.put("number_of_images", numberOfPages);
        database.updateData("Documents", values, "document_id", docId);

        int deletedRow = database.deleteData("Documents", "cover_image_uri", mImageUri.toString());
        //Toast.makeText(this, deletedRow+" rows deleted from Documents", Toast.LENGTH_SHORT).show();
    }

    private void openCamera()
    {
            StorageUtil storage = new StorageUtil(this);
            Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");

            try {
                mImage = storage.createTemporaryFile();
            } catch (Exception e) {
                Log.i(TAG, "can't create file to take picture! " + e.toString());
                Toast.makeText(this, "Please check SD card! Image shot is impossible", Toast.LENGTH_SHORT).show();
                return;
            }
            mImageUri = Uri.fromFile(mImage);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
        {
            ImageUtil imageUtil = new ImageUtil(this);

            Bitmap bitmap = imageUtil.grabImage(mImage);

            StorageUtil storage= new StorageUtil(this);

            String directory = storage.getDirectoryForOriginalImage();
            int imageId = imageUtil.nextImageID("Images")-1;

            Uri imageUri = storage.storeImage(bitmap, directory, "CamScannerImage"+imageId);

            Intent intent = new Intent(this, ImageCropping.class);
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.putExtra("from", IMAGE_CROPPING);
            startActivity(intent);
            finish();
        }

        else if(resultCode == RESULT_CANCELED)
        {
            Log.i(TAG, "onActivityResult: ResultCanceled");
            //if(mFrom.equals(MAIN_ACTIVITY)) {
            //}

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mXONImage.cancelCropView();
            //goToXON_IM_UI();
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private synchronized void calcFrameLayoutSize()
    {
        if (mLayoutSize != null && mLayoutRect != null) return;
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.xon_graphics_holder);
        int left = 0, right = 0, top = 0, bot = 0, layoutWt = 0, layoutHt = 0;
        if (frameLayout != null) {
            left = frameLayout.getLeft(); top = frameLayout.getTop();
            right = frameLayout.getRight(); bot = frameLayout.getBottom();
            layoutWt = frameLayout.getWidth(); layoutHt = frameLayout.getHeight();
        }

        if (layoutWt <= 0 || layoutHt <= 0) return;

        mLayoutSize = new Dimension(layoutWt, layoutHt);
        mLayoutRect = new Rect(); mLayoutRect.set(left, top, right, bot);
        Log.i(TAG, "Layout Wt: "+layoutWt+" Ht: "+layoutHt+
                " Layout Rect: "+mLayoutRect);
        Log.i(TAG, "Rect Wt: "+mLayoutRect.width()+" Ht: "+
                mLayoutRect.height());
        mXONCropView.m_LayoutRect = mLayoutRect;
        mXONCropView.m_LayoutSize = mLayoutSize;
        if (mXONImage == null && mImageUri != null) {
            Log.i(TAG, "Creating XONImage for: "+mImageUri);
            Dimension thumbSize = XONPropertyInfo.getThumbSize();
            mXONImage = new XONImageHolder(mImageUri, XONImageHolder.ViewType.CropView, mLayoutSize,
                    thumbSize.width, thumbSize.height);
            mXONImage.deriveDeviceCTM(0, null, ScalingFactor.FillLogic.FIT_IMAGE_IN_BBOX);
            mXONCropView.setXONImage(mXONImage); mXONCropView.invalidate();
        } else mXONImage.setPanelSize(mLayoutSize, XONImageHolder.ViewType.CropView);
    }
}
