package com.bridgelabz.docscanner.dialog;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.activities.DocumentActivity;
import com.bridgelabz.docscanner.activities.MainActivity;
import com.bridgelabz.docscanner.interfaces.ChangeDocumentName;
import com.bridgelabz.docscanner.interfaces.UpdateDocumentName;
import com.bridgelabz.docscanner.utility.DatabaseUtil;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by bridgelabz1 on 27/10/16.
 */

public class RenameDialog extends Dialog implements View.OnClickListener
{
    private Context mContext;
    private EditText mEditText;
    private Button mOk, mCancel;
    private String mOldName;
    private ChangeDocumentName mChange;
    private UpdateDocumentName mUpdateName;
    private InputMethodManager mInputMethod;

    private static final String DIALOG_TITLE = "Rename Document";
    private static final String TAG = "RenameDialog";
    public RenameDialog(Context context, String oldName, ChangeDocumentName change)
    {
        super(context);
        setTitle(DIALOG_TITLE);
        mContext = context;
        mOldName = oldName;
        mChange = change;
    }

    public RenameDialog(Context context, String oldName, UpdateDocumentName updateName)
    {
        super(context);
        setTitle(DIALOG_TITLE);
        mContext = context;
        mOldName = oldName;
        mUpdateName = updateName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rename);

        mEditText = (EditText)findViewById(R.id.new_name);
        mOk = (Button)findViewById(R.id.ok);
        mCancel = (Button)findViewById(R.id.cancel);

        mInputMethod = (InputMethodManager)mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
        mInputMethod.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        mEditText.setText(mOldName);

        mOk.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if (v == mOk)
        {
            int result = 0;
            String newName = mEditText.getText().toString();
            if(nameAlreadyExist(newName)) {
                Toast.makeText(mContext, "Name already exist", Toast.LENGTH_SHORT).show();
                mEditText.setText(mOldName);
            }
            else if(newName.equals("")) {
                Toast.makeText(mContext, "Name should not be blank", Toast.LENGTH_SHORT).show();
                mEditText.setText(mOldName);
            }
            else {
                result = updateName(newName);
                mInputMethod.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                dismiss();
            }

            if(result <= 0 )
            {
                Toast.makeText(mContext, "Rename failed, data not updated", Toast.LENGTH_SHORT).show();
            }
        }
        else if(v == mCancel) {
            dismiss();
            mInputMethod.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    private int updateName(String newName)
    {
        DatabaseUtil db = new DatabaseUtil(mContext, "Documents");

        String dateTime = DateFormat.getDateTimeInstance().format(new Date());

        //String values[] = {newName, dateTime};

        ContentValues values = new ContentValues();
        values.put("d_name", newName);
        values.put("date_time", dateTime);

        //int result = db.updateData("Documents", values, mOldName);
        int result = db.updateData("Documents", values, "d_name", mOldName);
        if(result > 0) {
            if(mContext instanceof DocumentActivity)
                mChange.updateDocumentName(newName);
            else if(mContext instanceof MainActivity)
                mUpdateName.updateDocumentName(newName);
        }
        return result;
    }

    private boolean nameAlreadyExist(String userInput)
    {
        DatabaseUtil database = new DatabaseUtil(mContext, "Documents");
        Cursor cursor = database.retrieveData("select d_name from Documents where d_name <> \'"+mOldName+"\'");
        while (cursor.moveToNext())
        {
            Log.i(TAG, "nameAlreadyExist: document name: "+cursor.getString(0));
            if (userInput.equals(cursor.getString(0)))
                return true;
        }
        cursor.close();
        return false;
    }
}
