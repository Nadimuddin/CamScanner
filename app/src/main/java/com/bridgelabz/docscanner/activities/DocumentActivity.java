package com.bridgelabz.docscanner.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
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
import com.bridgelabz.docscanner.dialog.RenameDialogBox;
import com.bridgelabz.docscanner.interfaces.ChangeDocumentName;
import com.bridgelabz.docscanner.utility.DatabaseUtil;
import com.bridgelabz.docscanner.utility.ImageUtil;
import com.bridgelabz.docscanner.utility.IntentUtil;
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
        AdapterView.OnItemClickListener, View.OnDragListener, AdapterView.OnItemLongClickListener
{
    private GridView mGridView;
    private FloatingActionButton mFloatingButton;
    private ImageButton mBackButton;
    private TextView mDocTitle;
    private ImageView croppedImage;
    private String mDocumentName;
    private ArrayList<Uri> mArrayList;
    File mImage;
    Uri mImageUri;
    int mPosition;
    private boolean mSelection = false;
    private boolean mSelected[];
    private int mSelectedItemCount = 0;

    private static final String TAG = "DocumentActivity";
    private static final int CAMERA_REQUEST = 1;
    private static final int SELECT_PICTURE = 2;
    private static final int CROP_FROM_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document);

        //get document name
        mDocumentName = getIntent().getExtras().getString("document_name");

        /* get XML objects
         * */
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        mGridView = (GridView)findViewById(R.id.gridView);
        mFloatingButton = (FloatingActionButton)findViewById(R.id.fabDocument);
        mBackButton = (ImageButton)findViewById(R.id.backButton);
        mDocTitle = (TextView)findViewById(R.id.docTitle);
        croppedImage = (ImageView)findViewById(R.id.croppedImage);

        //set Toolbar for activity
        setSupportActionBar(toolbar);

        //set blank title to remove default title
        getSupportActionBar().setTitle("");

        //set document title
        mDocTitle.setText(mDocumentName);

        //set onClickListener on views
        mFloatingButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mDocTitle.setOnClickListener(this);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);

        //set corresponding images in document
        setImages();

        //get number of images in a document
        int num = mArrayList.size();

        //boolean to get status of image whether it is selected or unselected
        mSelected = new boolean[num];


        /*ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        int sizeStack =  am.getRunningTasks(2).size();
        for(int i = 0;i < sizeStack;i++)
        {
            ComponentName cn = am.getRunningTasks(2).get(i).topActivity;
            Log.d(TAG, cn.getClassName());
        }*/
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

        else if(v == mFloatingButton)
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
            Bitmap bitmap = imageUtil.grabImage(mImage);

            /* get directory
             * i.e. /data/data/com.bridgelabz.docscanner/images     */
            String directory = storage.getDirectoryForOriginalImage();


            int imageId = imageUtil.nextImageID("Images");

            //store image in directory
            Uri imageUri = storage.storeImage(bitmap, directory, "CamScannerImage"+imageId);

            //insert image detail in database
            insertImageRecord(imageUri);

            updateDocumentTable();

            /*Intent intent = new Intent(this, ImageCropping.class);
            intent.putExtra("image_uri", imageUri.toString());
            startActivity(intent);*/
            processImage(imageUri);
        }

        else if(requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null)
        {
            //get uri of selected image
            Uri selectedImageUri = data.getData();

            Uri storedImageUri = null;

            try
            {
                //convert uri to bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

                //store image in local directory
                storedImageUri = storeImage(bitmap);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            //insert image details in database
            insertImageRecord(storedImageUri);

            updateDocumentTable();

            /*Intent intent = new Intent(this, ImageCropping.class);
            intent.putExtra("image_uri", storedImageUri.toString());
            startActivity(intent);*/
            processImage(storedImageUri);
        }
        else if(requestCode == CROP_FROM_CAMERA)
        {
            Bundle extras = data.getExtras();
            if (extras != null)
            {
                Bitmap mBitmap = extras.getParcelable("data");
                croppedImage.setImageBitmap(mBitmap);

            }
        }
    }

    private void processImage(Uri imageUri)
    {
        //IntentUtil.processIntent(this, ImageCropping.class, imageUri);

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

    private ArrayList<Uri> retrieveUriFromDatabase(String tableName)
    {
        ArrayList<Uri> arrayList = new ArrayList<>();
        DatabaseUtil db = new DatabaseUtil(this, tableName);
        String uriString;

        //get Uri from Images table
        Cursor cursor = db.retrieveData("select fltr_image_uri from "+tableName+" where d_id = " +
                "(select document_id from Documents where d_name = \""+mDocumentName+"\" )");
        while (cursor.moveToNext())
        {
            uriString = cursor.getString(0).substring(7);
            Log.i(TAG, "retrieveUriFromDatabase: "+uriString);
            arrayList.add(Uri.fromFile(new File(uriString)));
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
        else
            Toast.makeText(this, "nothing to show", Toast.LENGTH_SHORT).show();
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
        db.prepareDataForInsertion("Images", imageUri.toString(), docId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.document_option_menu, menu);
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
        //return false;
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
            if(!mSelected[position])
                view.setBackgroundResource(setPageSelected(true, position));
            else
                view.setBackgroundResource(setPageSelected(false, position));
            if(mSelectedItemCount == 0)
                mSelection = false;

        }
    }

    @Override
    public boolean onDrag(View v, DragEvent event)
    {
        boolean result = true;
        Bitmap bitmap;
        Drawable drawable = null;
        int action = event.getAction();

        Uri imageUri = mArrayList.get(mPosition);
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
                v.setBackgroundResource(R.drawable.ic_arrow_back_white_36dp);
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
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.i(TAG, "onItemLongClick: ");
        //view.setBackgroundResource(R.drawable.shape_for_selected);
        int count = parent .getChildCount();
        for (int i = 0; i < count; i++) {
            View current = parent.getChildAt(i);
            current.setOnDragListener(this);
        }
        Log.i(TAG, "onItemLongClick: OnDragListener activated");
        
        view.setBackgroundResource(setPageSelected(true, position));
        mSelection = true;

        return true;
    }

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
            }
            mSelection = false;
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
            mSelected[position] = true;
            mSelectedItemCount++;
            resId = R.drawable.shape_for_selected;
        }
        else
        {
            mSelected[position] = false;
            mSelectedItemCount--;
            resId = R.drawable.shape_for_unselected;
        }
        return resId;
    }

    private void renameDocument()
    {
        RenameDialogBox rename = new RenameDialogBox(this, mDocumentName, new ChangeDocumentName(){
            @Override
            public void updateDocumentName(String newName)
            {
                mDocTitle.setText(newName);
            }
        });
        rename.show();
    }
}