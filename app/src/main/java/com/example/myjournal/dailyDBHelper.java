package com.example.myjournal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dailyDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mydaily.db";
    private static final int DATABASE_VERSION = 2;

    public dailyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE daily( _id INTEGER PRIMARY KEY AUTOINCREMENT, date String," +
                "sleepHour INTEGER, sleepMinute INTEGER, wakeHour INTEGER, wakeMinute INTEGER," +
                "sleepingTime INTEGER, mood INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS daily");
        onCreate(db);
    }
}