package com.bridgelabz.docscanner.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.bridgelabz.docscanner.preference.SaveSharedPreference;

import java.text.DateFormat;
import java.util.Date;


/**
 * Created by Nadimuddin on 18/10/16.
 */

public class DatabaseUtil extends SQLiteOpenHelper
{
    private static final String TAG = "DatabaseUtil";
    private static final String DATABASE_NAME = "cam_scanner_database.db";

    SaveSharedPreference mPref;
    Context mContext;

    private static String COL1;
    private static String COL2;
    private static String COL3;
    private static String COL4;
    private static String COL5;
    private static String COL6;

    public DatabaseUtil(Context context, String tableName) {
        super(context, DATABASE_NAME, null, 1);

        mContext = context;
        mPref = new SaveSharedPreference(context);

        String createTableStatement = null;

        if (tableName == "Documents") {
            COL1 = "document_id";
            COL2 = "d_name";
            COL3 = "cover_image_uri";
            COL4 = "date_time";
            COL5 = "number_of_images";

            createTableStatement = "create table " + tableName + "(" +
                    COL1 + " integer primary key autoincrement," +
                    COL2 + " text," +
                    COL3 + " text," +
                    COL4 + " text," +
                    COL5 + " integer)";
        }

        else if (tableName == "Images")
        {
            COL1 = "image_id";
            COL2 = "i_name";
            COL3 = "org_image_uri";
            COL4 = "crp_image_uri";
            COL5 = "fltr_image_uri";
            COL6 = "d_id";

            createTableStatement = "create table " + tableName + "(" +
                    COL1 + " integer primary key autoincrement," +
                    COL2 + " text," +
                    COL3 + " text,"+
                    COL4 + " text,"+
                    COL5 + " text,"+
                    COL6 + " integer)";
        }

        String getPref = mPref.getPreference(tableName+"_table");

        if(getPref == null || !getPref.equals("table created"))
            createTable(tableName, createTableStatement);
    }

    private void createTable(String tableName, String createTableStatement)
    {
        SQLiteDatabase sqLite = getWritableDatabase();

        sqLite.execSQL(createTableStatement);
        mPref.setPreferences(tableName+"_table", "table created");
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

    public long insertData(String tableName, String col2Data, String col3Data, String col4Data, int col5Data)
    {
        SQLiteDatabase sqLite = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL2, col2Data);
        values.put(COL3, col3Data);
        values.put(COL4, col4Data);
        values.put(COL5, col5Data);
        long result = sqLite.insert(tableName, null, values);
        return result;
    }

    public long insertData(String tableName, String col2Data, String col3Data, int col4Data)
    {
        SQLiteDatabase sqLite = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL2, col2Data);
        values.put(COL3, col3Data);
        values.put(COL6, col4Data);

        long result = sqLite.insert(tableName, null, values);
        return result;
    }

    public Cursor retrieveData(String query)
    {
        SQLiteDatabase sqLite = getReadableDatabase();

        //execute query & return result in Cursor
        Cursor cursor = sqLite.rawQuery(query, null);

        return cursor;
    }

    public int updateData(String tableName, ContentValues values, String whereKey, String whereValue)
    {
        int rowsAffected;

        //get writable database
        SQLiteDatabase sqLite = getWritableDatabase();

        rowsAffected = sqLite.update(tableName, values, whereKey+" = \'" + whereValue +"\'", null);

        return rowsAffected;
    }

    public int deleteData(String tableName, String keyColumn, int keyValue)
    {
        //to get how many rows are deleted
        int rowsDeleted;

        //object to get writable database
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        /*
        * call delete method SQLiteDatabase class
        * it returns no. of rows deleted
         */
        rowsDeleted = sqLiteDatabase.delete(tableName, keyColumn+" = ?", new String[] {Integer.toString(keyValue)});

        return rowsDeleted;
    }

    public boolean isThereAnyRow(String tableName)
    {
        boolean check;

        SQLiteDatabase sqLite = getReadableDatabase();
        Cursor result = sqLite.rawQuery("select * from "+tableName, null);

        Log.i(TAG, "isThereAnyRow: "+result);

        if(result.moveToNext())
            check = true;
        else
            check = false;
        //result.close();
        return check;
    }

    public int getLastID(String tableName)
    {
        int lastID = 0;

        if(isThereAnyRow(tableName))
        {
            SQLiteDatabase sqLite = getReadableDatabase();
            Cursor cursor = sqLite.rawQuery("select * from " + tableName, null);

            //move cursor to last row
            cursor.moveToLast();

            //get ID
            lastID = cursor.getInt(0);
        }

        return lastID;
    }

    public void prepareDataForInsertion(String tableName, String imageUri, int docId)
    {
        long result = 0;
        //Cursor cursor;

        switch (tableName)
        {
            case "Documents":
                String documentName = "Document"+(getLastID(tableName)+1);

                String dateTime = DateFormat.getDateTimeInstance().format(new Date());

                result = insertData(tableName, documentName, imageUri, dateTime, 1);
                break;
            case "Images" :
                String imageName = imageUri.substring(imageUri.lastIndexOf('/')+1);

                /*cursor = retrieveData("select * from Documents");
                cursor.moveToLast();*/

                result = insertData(tableName, imageName, imageUri, docId);
                break;
        }
        if(result < 0)
            Toast.makeText(mContext, "Data not inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext, "Data inserted into "+tableName+" with row ID "+result, Toast.LENGTH_SHORT).show();

    }
}