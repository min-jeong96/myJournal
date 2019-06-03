package com.example.myjournal;

import android.content.Context;
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
                "SLEEPINGtime INTEGER, MOOD INTEGER)");
        db.execSQL("CREATE TABLE habit( _id INTEGER PRIMARY KEY AUTOINCREMENT, DATE String)");
        db.execSQL("CREATE TABLE tasks( _id INTEGER PRIMARY KEY AUTOINCREMENT, TITLE0 String," +
                "VALUE0 INTEGER, TITLE1 String, VALUE1 INTEGER, TITLE2 String, VALUE2 INTEGER," +
                "TITLE3 String, VALUE3 INTEGER, TITLE4 String, VALUE4 INTEGER, TITLE5 String," +
                "VALUE5 INTEGER, TITLE6 String, VALUE6 INTEGER, TITLE7 String, VALUE7 INTEGER," +
                "TITLE8 String, VALUE8 INTEGER, TITLE9 String, VALUE9 INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void onUpgrade(SQLiteDatabase db, String COLUMN_TITLE, String COLUMN_DESC) {
        db.execSQL("ALTER TABLE habit ADD '"+ COLUMN_TITLE +"' String");
        db.execSQL("ALTER TABLE habit ADD '"+ COLUMN_DESC +"' INTEGER");
    }
}