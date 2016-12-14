package com.bridgelabz.docscanner.dialog;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.interfaces.SetPageName;
import com.bridgelabz.docscanner.utility.DatabaseUtil;

/**
 * Created by bridgeit on 13/12/16.
 */

public class PageNameDialog extends Dialog implements View.OnClickListener
{
    private static final String TITLE = "Page Name";
    private Context mContext;
    private EditText mEditText;
    private Button mOK, mCancel;
    private SetPageName mSetPageName;
    private int mImageId;
    private InputMethodManager mInputMethod;

    public PageNameDialog(Context context,int imageId, SetPageName setPageName)
    {
        super(context);
        mContext = context;
        mSetPageName = setPageName;
        mImageId = imageId;
        setTitle(TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rename);

        mEditText = (EditText)findViewById(R.id.new_name);
        mOK = (Button)findViewById(R.id.ok);
        mCancel = (Button)findViewById(R.id.cancel);

        mInputMethod = (InputMethodManager)mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
        mInputMethod.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        String currentPageName = getCurrentPageName();
        if(currentPageName.equals(""))
            mEditText.setHint("Please enter page name");
        else// if(currentPageName != null)
            mEditText.setText(currentPageName);
        mOK.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.ok:
                String pageName = mEditText.getText().toString();
                setPageName(pageName);
                mInputMethod.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                dismiss();
                break;

            case R.id.cancel:
                mInputMethod.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                dismiss();
                break;
        }

    }

    private void setPageName(String pageName)
    {
        DatabaseUtil database = new DatabaseUtil(mContext, "Images");

        ContentValues values = new ContentValues();
        values.put("page_name", pageName);

        database.updateData("Images", values, "image_id", mImageId);

    }

    private String getCurrentPageName()
    {
        DatabaseUtil database = new DatabaseUtil(mContext, "Images");
        Cursor cursor = database.retrieveData("select page_name from Images where image_id = "+mImageId);
        cursor.moveToNext();
        String currentPageName = cursor.getString(0);
        cursor.close();
        return currentPageName;
    }
}
