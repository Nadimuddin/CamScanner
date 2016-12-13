package com.bridgelabz.docscanner.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.adapter.DocumentAdapter;
import com.bridgelabz.docscanner.dialog.RenameDialog;
import com.bridgelabz.docscanner.interfaces.ChangeDocumentName;
import com.bridgelabz.docscanner.model.PageDetail;
import com.bridgelabz.docscanner.utility.DatabaseUtil;
import com.bridgelabz.docscanner.utility.ImageUtil;
import com.bridgelabz.docscanner.utility.StorageUtil;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Nadimuddin on 13/10/16.
 */

public class DocumentActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, View.OnDragListener, AdapterView.OnItemLongClickListener,
        Toolbar.OnMenuItemClickListener
{
    private GridView mGridView;
    Toolbar mToolbar,mBottomToolbar;
    private FloatingActionButton mCameraButton;
    private ImageButton mBackButton;
    private TextView mDocTitle;
    private ImageView croppedImage;
    private Menu mMenu;
    private MenuItem mSelectAll;
    private String mDocumentName;
    private ArrayList<Uri> mUris = new ArrayList<>();
    private ArrayList<PageDetail> mArrayList;
    File mImage;
    Uri mImageUri;
    int mPosition;
    private boolean mSelection = false;
    private boolean mIsSelected[];
    private int mSelectedItemCount = 0;
    private ArrayList<Integer> mSelectedItems;

    private static final String TAG = "DocumentActivity";
    private static final int CAMERA_REQUEST = 1;
    private static final int SELECT_PICTURE = 2;
    private static final String SELECT_ALL = "Select All";
    private static final String DESELECT_ALL = "Deselect_All";
    private static final boolean SELECTED = true;
    private static final boolean DESELECTED = false;
    //private static boolean UPDATE_COVER_IMAGE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document);

        //get document name
        mDocumentName = getIntent().getExtras().getString("document_name");

        /* get XML objects
         * */
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mBottomToolbar = (Toolbar)findViewById(R.id.toolbar_bottom);
        mGridView = (GridView)findViewById(R.id.gridView);
        mCameraButton = (FloatingActionButton)findViewById(R.id.fabDocument);
        mBackButton = (ImageButton)findViewById(R.id.backButton);
        mDocTitle = (TextView)findViewById(R.id.docTitle);
        croppedImage = (ImageView)findViewById(R.id.croppedImage);

        //set Toolbar for activity
        setSupportActionBar(mToolbar);

        //set blank title to remove default title
        getSupportActionBar().setTitle("");

        //set document title
        mDocTitle.setText(mDocumentName);

        //set onClickListener on views
        mCameraButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mDocTitle.setOnClickListener(this);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        mBottomToolbar.setOnMenuItemClickListener(this);
        Log.i(TAG, "onCreate: setOnMenuItemClickListener activated");

        //set corresponding images in document
        setImages();

        //get number of images in a document
        int num = mArrayList.size();

        for(int i=0; i<num; i++) {
            String uriString = mArrayList.get(i).getImageUri();
            mUris.add(Uri.fromFile(new File(uriString)));
        }

        //boolean to get status of image whether it is selected or unselected
        mIsSelected = new boolean[num];

        mSelectedItems = new ArrayList<>();


        /*ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        int sizeStack =  am.getRunningTasks(2).size();
        for(int i = 0;i < sizeStack;i++)
        {
            ComponentName cn = am.getRunningTasks(2).get(i).topActivity;
            Log.d(TAG, cn.getClassName());
        }*/
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart: ");
        super.onRestart();
        setImages();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    public void onClick(View v)
    {
        if(v == mDocTitle)
        {
            /* when click on a name of document
             * it will ask to rename it  */
            renameDocument();
        }

        else if(v == mCameraButton)
        {
            StorageUtil storage = new StorageUtil(this);

            //calling camera intent
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            try
            {
                //create temporary file to store camera image
                mImage = storage.createTemporaryFile();
            }
            catch (Exception e)
            {
                Log.i(TAG, "can't create file to take picture! "+e.toString());
                Toast.makeText(this, "Please check SD card! Image shot is impossible", Toast.LENGTH_SHORT).show();
                return;
            }

            //uri of temporary file
            mImageUri = Uri.fromFile(mImage);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(intent, CAMERA_REQUEST);
        }

        else if(v == mBackButton)
            onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        ImageUtil imageUtil = new ImageUtil(this);
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
        {
            StorageUtil storage= new StorageUtil(this);

            //get bitmap of camera image
            Bitmap bitmap = imageUtil.compressImage(mImage);

            /* get directory
             * i.e. /data/data/com.bridgelabz.docscanner/images     */
            String directory = storage.getDirectoryForOriginalImage();


            int imageId = imageUtil.nextImageID("Images");

            //store image in directory
            Uri imageUri = storage.storeImage(bitmap, directory, "CamScannerImage"+imageId);

            //insert image detail in database
            insertImageRecord(imageUri);

            updateDocumentTable();

            processImage(imageUri);
        }

        else if(requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null)
        {
            //get uri of selected image
            Uri selectedImageUri = data.getData();

            String path = imageUtil.getRealPath(selectedImageUri);

            File file = new File(path);

            Bitmap bitmap = imageUtil.compressImage(file);

            Uri storedImageUri = storeImage(bitmap);

            //insert image details in database
            insertImageRecord(storedImageUri);

            updateDocumentTable();

            processImage(storedImageUri);
        }
        /*else if(requestCode == CROP_FROM_CAMERA)
        {
            Bundle extras = data.getExtras();
            if (extras != null)
            {
                Bitmap mBitmap = extras.getParcelable("data");
                croppedImage.setImageBitmap(mBitmap);
            }
        }*/
    }

    private void processImage(Uri imageUri)
    {
        //IntentUtil.processIntent(this, ImageCropping.class, imageUri);

        finish();

        Intent intent = new Intent(this, ImageCropping.class);
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.putExtra("from", "DocumentActivity");
        startActivity(intent);
    }

    private void updateDocumentTable()
    {
        DatabaseUtil database = new DatabaseUtil(this, "Documents");
        Cursor cursor = database.retrieveData("select * from Images where d_id = (select document_id from Documents where d_name = \""+mDocumentName+"\")");
        int numberOfImages = cursor.getCount();
        cursor.close();

        //get current date & time
        String dateTime = DateFormat.getDateTimeInstance().format(new Date());

        ContentValues values = new ContentValues();

        //if(UPDATE_COVER_IMAGE)
        //String uriString = mArrayList.get(0).getImageUri();
        values.put("cover_image_uri", mArrayList.get(0).getImageUri());
        values.put("date_time", dateTime);
        values.put("number_of_images", numberOfImages);

        database.updateData("Documents", values, "d_name", mDocumentName);
    }

    private Uri storeImage(Bitmap bitmap)
    {
        StorageUtil storage= new StorageUtil(this);
        ImageUtil imageUtil = new ImageUtil(this);

        /* get directory
         * i.e. /data/data/com.bridgelabz.camscannertrail/app_images */
        String directory = storage.getDirectoryForOriginalImage();

        int imageId = imageUtil.nextImageID("Images");

        //store image in directory
        Uri imageUri = storage.storeImage(bitmap, directory, "CamScannerImage"+imageId);

        return imageUri;
    }

    private ArrayList<PageDetail> retrieveUriFromDatabase(String tableName)
    {
        ArrayList<PageDetail> arrayList = new ArrayList<>();
        DatabaseUtil db = new DatabaseUtil(this, tableName);
        String uriString, pageName;

        //get Uri from Images table
        Cursor cursor = db.retrieveData("select fltr_image_uri, page_name from "+tableName+" where d_id = " +
                "(select document_id from Documents where d_name = \""+mDocumentName+"\" )");
        while (cursor.moveToNext())
        {
            uriString = cursor.getString(0);
            pageName = cursor.getString(1);
            Log.i(TAG, "retrieveUriFromDatabase: "+uriString);
            //arrayList.add(Uri.fromFile(new File(uriString)), pageName);
            arrayList.add(new PageDetail(uriString, pageName));
        }
        cursor.close();
        return arrayList;
    }

    //set images to gridView of activity layout
    private void setImages()
    {
        mArrayList = retrieveUriFromDatabase("Images");
        if(mArrayList.size() > 0)
        {
            DocumentAdapter adapter = new DocumentAdapter(this, mArrayList);

            mGridView.setAdapter(adapter);
        }
        /*else {
            Toast.makeText(this, "nothing to show in "+mDocumentName, Toast.LENGTH_SHORT).show();
            mGridView.setAdapter(null);
        }*/
    }

    private void insertImageRecord(Uri imageUri)
    {
        DatabaseUtil db = new DatabaseUtil(this, "Images");

        //get specific record from Documents table for using it further
        Cursor cursor = db.retrieveData("select document_id from Documents where d_name = \'"+mDocumentName+"\'");

        //point cursor to next record
        cursor.moveToNext();

        // get document ID
        int  docId = cursor.getInt(0);

        cursor.close();

        //add record to database
        db.prepareDataForInsertion("Images", imageUri.getPath(), docId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.document_option_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {

            case R.id.import_from_gallery:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_PICTURE);
                break;

            case R.id.rename:
                renameDocument();
                break;

            case R.id.select_all:
                String menuTitle = (String)item.getTitle();
                if(menuTitle.equalsIgnoreCase(SELECT_ALL))
                {
                    setAllPageBackground(SELECTED);
                    item.setTitle(DESELECT_ALL);
                }
                else if(menuTitle.equalsIgnoreCase(DESELECT_ALL))
                {
                    setAllPageBackground(DESELECTED);
                    item.setTitle(SELECT_ALL);
                }
                mSelection = true;
                mDocTitle.setText(mSelectedItemCount+" Selected");
                break;

            case R.id.delete:
                /*if(mSelectedItems.size() > 0)
                    showAlert();
                else
                    Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();*/
                break;

            /*case R.id.pdfSetting:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.invite:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.comment:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.emailTo:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.setTag:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.manualSorting:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.viewBy:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.collage:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.select:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        if(item.getItemId() == R.id.delete)
        {
            if(mSelectedItems.size() > 0) {
                showAlert();
            }
            else
                Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void showAlert()
    {
        DialogInterface.OnClickListener dialog = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        for(int i=0; i<mSelectedItems.size(); i++)
                        {
                            PageDetail pageDetail = mArrayList.get(mSelectedItems.get(i));
                            String uriString = /*mArrayList.get(mSelectedItems.get(i))*/ pageDetail.getImageUri();
                            deletePage(uriString);/*
                            if(mSelectedItems.get(i) == 0)
                                UPDATE_COVER_IMAGE = true;*/
                        }
                        mSelection = false;
                        if(mSelectedItems.size() == mGridView.getCount())
                        {
                            Toast.makeText(getBaseContext(), "All pages in "+mDocumentName+" are deleted, unable to open document",
                                    Toast.LENGTH_LONG).show();
                            //mGridView.setAdapter(null);
                            deleteDocument(mDocumentName);
                            onBackPressed();
                        }
                        else {
                            setImages();
                            setBottomToolbar(false);
                            changeOptionMenu();
                            mDocTitle.setText(mDocumentName);
                            mSelectedItemCount = 0;
                            mIsSelected = new boolean[mArrayList.size()];
                            updateDocumentTable();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete these pages?")
                .setPositiveButton("Yes", dialog).setNegativeButton("No", dialog).show();
    }

    private void deletePage(String uriString)
    {
        DatabaseUtil database = new DatabaseUtil(this, "Images");
        StorageUtil storage = new StorageUtil(this);

        storage.deleteImage(uriString);

        String imageName = uriString.substring(uriString.lastIndexOf('/')+1);
        storage.deleteImage(storage.getDirectoryForCroppedImage()+"/"+imageName);
        storage.deleteImage(storage.getDirectoryForOriginalImage()+"/"+imageName);

        database.deleteData("Images", "fltr_image_uri", uriString);

    }

    private void deleteDocument(String documentName)
    {
        DatabaseUtil database = new DatabaseUtil(this, "Documents");

        database.deleteData("Documents", "d_name", documentName);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if(!mSelection)
        {
            Intent intent = new Intent(this, ImageViewer.class);
            intent.putExtra("list_of_images", mArrayList);
            intent.putExtra("current_image", position);
            startActivity(intent);
        }
        else
        {
            if(!mIsSelected[position])
                view.setBackgroundResource(setPageSelected(true, position));
            else
                view.setBackgroundResource(setPageSelected(false, position));
            mDocTitle.setText(mSelectedItemCount+" selected");

            int count = mGridView.getCount();
            if(mSelectedItemCount == count)
                mSelectAll.setTitle(DESELECT_ALL);
            else if(mSelectedItemCount < count)
                mSelectAll.setTitle(SELECT_ALL);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.i(TAG, "onItemLongClick: ");
        mSelection = true;
        //int count = parent.getChildCount();
        /*for(int i = 0; i < count; i++)
        {
            View current = parent.getChildAt(i);
            current.setOnDragListener(this);
        }*/
        Log.i(TAG, "onItemLongClick: OnDragListener activated0123");
        
        view.setBackgroundResource(setPageSelected(true, position));
        mDocTitle.setText(mSelectedItemCount+" selected");
        changeOptionMenu();
        setBottomToolbar(true);

        return true;
    }

    private void setAllPageBackground(boolean select)
    {
        int count = mGridView.getCount();
        for (int i = 0; i < count; i++)
        {
            View current = mGridView.getChildAt(i);
            if (mIsSelected[i] != select)
                current.setBackgroundResource(setPageSelected(select, i));
        }
    }

    private void changeOptionMenu()
    {
        mToolbar.getMenu().clear();
        int menuRes = mSelection ? R.menu.select_all : R.menu.document_option_menu;
        getMenuInflater().inflate(menuRes, mMenu);
        mSelectAll = mMenu.findItem(R.id.select_all);
    }

    private void setBottomToolbar(boolean condition)
    {
        mBottomToolbar.getMenu().clear();
        if(condition)
        {
            mBottomToolbar.setVisibility(View.VISIBLE);
            mBottomToolbar.inflateMenu(R.menu.document_bottom_menu);
            mCameraButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            mBottomToolbar.setVisibility(View.INVISIBLE);
            mCameraButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onDrag(View v, DragEvent event)
    {
        boolean result = true;
        Bitmap bitmap;
        Drawable drawable = null;
        int action = event.getAction();

        String uriString = mArrayList.get(mPosition).getImageUri();
        Uri imageUri = Uri.fromFile(new File(uriString));
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            drawable = new BitmapDrawable(getResources(), bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (action)
        {
            case DragEvent.ACTION_DRAG_ENTERED:
                //v.setBackground(drawable);
                v.setBackgroundResource(R.drawable.back_white_36dp);
                Log.i(TAG, "onDrag: Entered");
                break;

            case DragEvent.ACTION_DRAG_EXITED:
                //v.setBackgroundResource(R.drawable.crop__selectable_background);
                Log.i(TAG, "onDrag: Exited");
                break;
        }
        return result;
    }

    /*@Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            GridView parent = (GridView)v;

            int x = (int) event.getX();
            int y = (int) event.getY();

            mPosition = parent.pointToPosition(x, y);
            Log.i(TAG, "onTouch:  position of page onTouch: "+mPosition);
            if(mPosition > AdapterView.INVALID_POSITION)
            {

                int count = parent.getChildCount();
                for (int i = 0; i < count; i++) {
                    View current = parent.getChildAt(i);
                    current.setOnDragListener(this);
                }
            }
        }
        return true;
    }*/

    @Override
    public void onBackPressed()
    {
        Log.i(TAG, "onBackPressed: ");
        if(mSelection)
        {
            int count = mGridView.getChildCount();
            for(int i=0; i < count; i++)
            {
                View current = mGridView.getChildAt(i);
                current.setBackgroundResource(0);
                mIsSelected[i] = false;
            }
            mSelection = false;
            mSelectedItemCount = 0;
            mSelectedItems.clear();
            changeOptionMenu();
            setBottomToolbar(false);
            mDocTitle.setText(mDocumentName);
        }
        else
        {
            super.onBackPressed();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

    }

    private int setPageSelected(boolean selected, int position)
    {
        int resId;
        if(selected)
        {
            mIsSelected[position] = true;
            mSelectedItemCount++;
            mSelectedItems.add(position);
            resId = R.drawable.shape_for_selected;
        }
        else
        {
            mIsSelected[position] = false;
            mSelectedItemCount--;
            Object obj = position;
            mSelectedItems.remove(obj);
            resId = 0;
        }
        return resId;
    }

    private void renameDocument()
    {
        RenameDialog rename = new RenameDialog(this, mDocumentName, new ChangeDocumentName(){
            @Override
            public void updateDocumentName(String newName)
            {
                mDocTitle.setText(newName);
                mDocumentName = newName;
            }
        });
        rename.show();
        rename.setCancelable(false);
    }
}