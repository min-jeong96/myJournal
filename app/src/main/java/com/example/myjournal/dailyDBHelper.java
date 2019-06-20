package com.example.myjournal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dailyDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "myjournal.db";
    private static final int DATABASE_VERSION = 2;

    public dailyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE daily( _id INTEGER PRIMARY KEY AUTOINCREMENT, DATE String," +
                "SLEEPhour INTEGER, SLEEPminute INTEGER, WAKEhour INTEGER, WAKEminute INTEGER," +
                "SLEEPINGtime INTEGER, MOOD INTEGER, JOURNAL String)");
        db.execSQL("CREATE TABLE habit(DATE String)");
        db.execSQL("CREATE TABLE tasks(DATE String, NUM_OF_TASK INTEGER, TITLE0 String," +
                "VALUE0 INTEGER, TITLE1 String, VALUE1 INTEGER, TITLE2 String, VALUE2 INTEGER," +
                "TITLE3 String, VALUE3 INTEGER, TITLE4 String, VALUE4 INTEGER, TITLE5 String," +
                "VALUE5 INTEGER, TITLE6 String, VALUE6 INTEGER, TITLE7 String, VALUE7 INTEGER," +
                "TITLE8 String, VALUE8 INTEGER, TITLE9 String, VALUE9 INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addColumn(SQLiteDatabase db, String COLUMN_TITLE, String COLUMN_DESC) {
        db.execSQL("ALTER TABLE habit ADD '"+ COLUMN_TITLE +"' String");
        db.execSQL("ALTER TABLE habit ADD '"+ COLUMN_DESC +"' INTEGER");
        return true;
    }

    public void deleteColumn(SQLiteDatabase db, String COLUMN_TITLE, String COLUMN_DESC) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, "DELETED");
        values.put(COLUMN_DESC, -1);

        String[] whereArgs = {"SETTINGS"};
        db.update("habit", values, "DATE = ?", whereArgs);
    }
}