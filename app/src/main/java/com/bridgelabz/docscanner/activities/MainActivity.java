package com.bridgelabz.docscanner.activities;
/**
 * Created by Nadimuddin on 10/10/16.
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.adapter.MainAdapter;
import com.bridgelabz.docscanner.dialog.RenameDialog;
import com.bridgelabz.docscanner.interfaces.UpdateDocumentName;
import com.bridgelabz.docscanner.model.DocumentDetails;
import com.bridgelabz.docscanner.utility.DatabaseUtil;
import com.bridgelabz.docscanner.utility.ImageUtil;
import com.bridgelabz.docscanner.utility.StorageUtil;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, Toolbar.OnMenuItemClickListener
{
    private static final int CAMERA_REQUEST = 1;
    private static final int SELECT_PICTURE = 2;
    private static final String TAG = "MainActivity";
    private static final String SELECT_ALL = "Select All";
    private static final String DESELECT_ALL = "Deselect All";

    ListView mListView;
    FloatingActionButton mCameraButton;
    ArrayList<DocumentDetails> mArrayList;
    MainAdapter mAdapter;
    Toolbar mToolbar, mToolbarBottom;
    Menu mMenu;
    MenuItem mSelectAll;
    File mImage;
    Uri mImageUri;

    boolean mSelection;
    boolean mIsSelected[];
    ArrayList<Integer> mSelectedItems;
    boolean SELECTED = true;
    boolean DESELECTED = false;
    int mSelectedListCount=0;
    int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView)findViewById(R.id.listView);
        mCameraButton = (FloatingActionButton)findViewById(R.id.fab);
        mToolbar = (Toolbar)findViewById(R.id.toolbar_main);
        mToolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(R.string.title_name);

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mToolbarBottom.setOnMenuItemClickListener(this);

        mArrayList = new ArrayList<>();

        setDocumentList();

        mCameraButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume: ");
        super.onResume();
        setDocumentList();
    }

    @Override
    public void onBackPressed()
    {
        if(mSelection)
        {
            int count = mListView.getCount();
            for(int i=0; i<count; i++) {
                mListView.getChildAt(i).setBackgroundResource(setDocumentSelected(false, i));
            }

            mToolbar.setTitle(R.string.title_name);
            mSelectedListCount = 0;
            mSelection = false;
            mSelectedItems.clear();
            changeOptionMenu();
            setBottomToolbar(false);
        }
        else
            super.onBackPressed();
    }

    @Override
    public void onClick(View view)
    {
        if(view == mCameraButton) {
            StorageUtil storage = new StorageUtil(this);
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            //File photo;
            try {
                mImage = storage.createTemporaryFile();
            }
            catch (Exception e)
            {
                Log.i(TAG, "can't create file to take picture! "+e.toString());
                Toast.makeText(this, "Please check SD card! Image shot is impossible", Toast.LENGTH_SHORT).show();
                return;
            }
            mImageUri = Uri.fromFile(mImage);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if(mSelection)
        {
            int count = mListView.getCount();
            if(!mIsSelected[position])
                view.setBackgroundResource(setDocumentSelected(true, position));
            else
                view.setBackgroundResource(setDocumentSelected(false, position));
            getSupportActionBar().setTitle(mSelectedListCount+" Selected");
            if(mSelectedListCount == count)
                mSelectAll.setTitle(DESELECT_ALL);
            else if(mSelectedListCount < count)
                mSelectAll.setTitle(SELECT_ALL);
        }
        else
            openDocument(position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.i(TAG, "onItemLongClick: ");
        mSelection = true;
        view.setBackgroundResource(setDocumentSelected(true, position));
        changeOptionMenu();
        mToolbar.setTitle(mSelectedListCount+" Selected");
        getSupportActionBar().setTitle(mSelectedListCount+" Selected");
        setBottomToolbar(true);
        mPosition = position;
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result)
    {
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
        {
            setDocumentList();
            ImageUtil imageUtil = new ImageUtil(this);

            Bitmap bitmap = imageUtil.compressImage(mImage);
            Uri imageUri = storeImage(bitmap);
            insertDocRecord(imageUri);

            processImage(imageUri);
        }
        else if(requestCode == SELECT_PICTURE && resultCode == RESULT_OK && result != null)
        {
            Uri selectedImage = result.getData(), storedImageUri = null;
            ImageUtil imageUtil = new ImageUtil(this);

            /*try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);

                storedImageUri = storeImage(bitmap);
            }
            catch (IOException e) {
                e.printStackTrace();
            }*/
            String realUri = imageUtil.getRealPath(selectedImage);

            File file = new File(realUri);

            Bitmap bitmap = imageUtil.compressImage(file);
            storedImageUri = storeImage(bitmap);

            insertDocRecord(storedImageUri);

            processImage(storedImageUri);
        }
    }

    private void processImage(Uri imageUri)
    {
        //IntentUtil.processIntent(this, ImageCropping.class, imageUri);
        Intent intent = new Intent(this, ImageCropping.class);
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.putExtra("from", "MainActivity");
        startActivity(intent);
    }

    private ArrayList<DocumentDetails> retrieveFromDatabase(String tableName)
    {
        ArrayList<DocumentDetails> arrayList = new ArrayList<>();

        DatabaseUtil db = new DatabaseUtil(this, tableName);
        Cursor cursor = db.retrieveData("select * from "+tableName);
        while (cursor.moveToNext())
        {
            arrayList.add(new DocumentDetails(cursor.getString(1), Uri.fromFile(new File(cursor.getString(2))),
                    cursor.getString(3), cursor.getInt(4)));
        }
        cursor.close();
        return arrayList;
    }

    private void setDocumentList()
    {
        mArrayList.clear();
        mArrayList = retrieveFromDatabase("Documents");
        Log.i(TAG, "setDocumentList: Size oa arrayList "+mArrayList.size());
        int listSize = mArrayList.size();
        if(listSize > 0)
        {
            mAdapter = new MainAdapter(this, mArrayList);
            mListView.setAdapter(mAdapter);
        }
        else
        {
            mAdapter = new MainAdapter(this, mArrayList);
            mListView.setAdapter(mAdapter);
            Toast.makeText(this, "Nothing to show", Toast.LENGTH_SHORT).show();
        }
        mSelection = false;
        mIsSelected = new boolean[listSize];
        mSelectedItems = new ArrayList<>();
    }

    private Uri storeImage(Bitmap bitmap)
    {
        StorageUtil storage= new StorageUtil(this);
        ImageUtil imageUtil = new ImageUtil(this);

        /* get directory
         * i.e. /data/data/com.bridgelabz.camscannertrail/app_original_images */
        String directory = storage.getDirectoryForOriginalImage();

        int imageId = imageUtil.nextImageID("Images");

        //store image in directory
        Uri imageUri = storage.storeImage(bitmap, directory, "CamScannerImage"+imageId);

        return imageUri;
    }

    private void openDocument(int position)
    {
        Intent intent = new Intent(MainActivity.this, DocumentActivity.class);
        String documentName = mArrayList.get(position).getDocumentName();
        intent.putExtra("document_name", documentName);
        startActivity(intent);
    }

    private void insertDocRecord(Uri imageUri)
    {
        ImageUtil imageUtil = new ImageUtil(this);
        StorageUtil storage = new StorageUtil(this);

        int imageId = imageUtil.nextImageID("Images");
        String imagePath = storage.getDirectoryForFilteredImage()+"/CamScannerImage"+imageId;

        DatabaseUtil db = new DatabaseUtil(this, "Documents");

        //add record to Documents table
        db.prepareDataForInsertion("Documents", imagePath, 0);

        Cursor cursor = db.retrieveData("select document_id from Documents");
        cursor.moveToLast();
        int docId = cursor.getInt(0);
        cursor.close();

        db = new DatabaseUtil(this, "Images");

        //add record to Images table also
        db.prepareDataForInsertion("Images", imageUri.getPath(), docId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.main_option_menu, menu);
        //getSupportActionBar().setTitle(mSelectedListCount+" Selected");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.import_from_gallery:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_PICTURE);
                break;

            case R.id.select:
                if(mArrayList.size() > 0) {
                    mSelection = true;
                    changeOptionMenu();
                    setBottomToolbar(mSelection);
                }
                else
                    Toast.makeText(this, "Nothing is there to select", Toast.LENGTH_SHORT).show();
                break;

            case R.id.select_all:
                String menuTitle = (String )item.getTitle();
                if(menuTitle.equalsIgnoreCase(SELECT_ALL))
                {
                    setAllListItemBackground(SELECTED);
                    item.setTitle(DESELECT_ALL);
                }
                else if(menuTitle.equalsIgnoreCase(DESELECT_ALL))
                {
                    setAllListItemBackground(DESELECTED);
                    item.setTitle(SELECT_ALL);
                }
                mSelection = true;
                mToolbar.setTitle(mSelectedListCount+" Selected");
                break;

            /*case R.id.createNewFolder:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.gridView:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.sortBy:
                Toast.makeText(this, item.getTitle()+" is not implemented yet", Toast.LENGTH_SHORT).show();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAllListItemBackground(boolean select)
    {
        int count = mListView.getCount();
        for (int i = 0; i < count; i++) {
            View current = mListView.getChildAt(i);
            if (mIsSelected[i] != select)
                current.setBackgroundResource(setDocumentSelected(select, i));
        }
    }

    private int setDocumentSelected(boolean selected, int position)
    {
        int resId;
        if(selected)
        {
            mIsSelected[position] = true;
            mSelectedListCount++;
            mSelectedItems.add(position);
            resId = R.drawable.shape_for_selected;
        }
        else
        {
            mIsSelected[position] = false;
            mSelectedListCount--;
            Object obj = position;
            mSelectedItems.remove(obj);
            resId = 0;//R.drawable.shape_for_unselected;
        }
        return resId;
    }

    private void changeOptionMenu()
    {
        mToolbar.getMenu().clear();
        int menuRes = mSelection ? R.menu.select_all : R.menu.main_option_menu;
        getMenuInflater().inflate(menuRes, mMenu);
        mSelectAll = mMenu.findItem(R.id.select_all);
    }

    private void setBottomToolbar(boolean condition)
    {
        mToolbarBottom.getMenu().clear();
        if(condition) {
            mToolbarBottom.setVisibility(View.VISIBLE);
            mToolbarBottom.inflateMenu(R.menu.main_bottom_menu);
            mCameraButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            mToolbarBottom.setVisibility(View.INVISIBLE);
            mCameraButton.setVisibility(View.VISIBLE);
        }
    }

    private void deleteDocument(String docName)
    {
        DatabaseUtil database = new DatabaseUtil(this, "Documents");
        StorageUtil storage = new StorageUtil(this);
        Cursor cursor = database.retrieveData("select document_id from Documents where d_name  = \""+docName+"\"");
        cursor.moveToNext();
        int docId = cursor.getInt(0);
        cursor.close();

        int rowsDeleted = database.deleteData("Documents", "document_id", docId);

        Log.i(TAG, "deleteDocument: "+rowsDeleted+" rows deleted from Documents table");

        //Toast.makeText(this, rowsDeleted+" rows deleted from Documents table", Toast.LENGTH_SHORT).show();

        cursor = database.retrieveData("select org_image_uri from Images where d_id = "+docId);
        while (cursor.moveToNext())
        {
            String directory = cursor.getString(0);
            storage.deleteImage(directory);
        }
        cursor.close();
        rowsDeleted = database.deleteData("Images", "d_id", docId);
        Toast.makeText(this, rowsDeleted+" rows deleted from Documents table", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        if(item.getItemId() == R.id.delete) {
            if (mSelectedItems.size() > 0)
                showAlert();
            else
                Toast.makeText(this, "Nothing is selected", Toast.LENGTH_SHORT).show();
        }

        else if(item.getItemId() == R.id.rename)
        {
            String oldName = mArrayList.get(mSelectedItems.get(0)).getDocumentName();
            RenameDialog rename = new RenameDialog(this, oldName, new UpdateDocumentName() {
                @Override
                public void updateDocumentName(String newName)
                {
                    setDocumentList();
                    changeOptionMenu();
                    setBottomToolbar(false);
                }
            });
            rename.show();
        }

        return true;
    }

    private void showAlert()
    {
        new AlertDialog.Builder(this).setTitle("Delete Document")
                .setMessage("Are you sure you want to delete selected document")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        for(int i=0; i<mSelectedItems.size(); i++)
                        {
                            String docName = mArrayList.get(mSelectedItems.get(i)).getDocumentName();
                            deleteDocument(docName);
                        }
                        //deleteDocument(mArrayList.get(mPosition).getDocumentName());
                        setDocumentList();
                        setBottomToolbar(false);
                        mSelection = false;
                        changeOptionMenu();
                        mToolbar.setTitle(R.string.title_name);
                        mSelectedListCount = 0;
                        mIsSelected = new boolean[mArrayList.size()];
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                }).show();
    }
}

            /*Uri uriInput = Uri.fromFile(fileInput);

            File fileOutput = new File(directory, "CROPPED_IMAGE.png");
            Uri uriOutput = Uri.fromFile(fileOutput);*/

            /*ByteArrayOutputStream bs = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
            Intent intent = new Intent(this, ImageEditing.class);
            intent.putExtra("byteArray", bs.toByteArray());
            startActivity(intent);*/

            /*Intent intent = new Intent(this, ImageEditing.class);
            intent.putExtra("image_uri", imageUri.toString());
            startActivity(intent);*/