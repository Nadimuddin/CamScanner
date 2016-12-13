package com.bridgelabz.docscanner.dialog;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
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
    private int mDocId;

    public PageNameDialog(Context context,int docId, SetPageName setPageName)
    {
        super(context);
        mContext = context;
        mSetPageName = setPageName;
        mDocId = docId;
        setTitle(TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rename);

        mEditText = (EditText)findViewById(R.id.new_name);
        mOK = (Button)findViewById(R.id.ok);
        mCancel = (Button)findViewById(R.id.cancel);

        mEditText.setHint("Please enter page name");
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
                dismiss();
                break;

            case R.id.cancel:
                dismiss();
                break;
        }

    }

    private void setPageName(String pageName)
    {
        DatabaseUtil database = new DatabaseUtil(mContext, "Images");

        ContentValues values = new ContentValues();
        values.put("page_name", pageName);

        database.updateData("Images", values, "image_id", mDocId);

    }
}
