package com.example.myjournal;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    dailyDBHelper dailyHelper;
    SQLiteDatabase dailyDB;

    boolean isAlreadyMade = false;
    int userYear, userMonth, userDay;

    String todayDate;
    int userSleepHour = 0, userSleepMinute = 0, userWakeHour = 0, userWakeMinute = 0, userSleepingTime = 0;

    Button sleepTimeButton, wakeTimeButton;
    EditText dailyDate;
    SeekBar moodTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dailyHelper = new dailyDBHelper(this);
        dailyDB = dailyHelper.getWritableDatabase();
        dailyDB.close();

        dailyDate = (EditText) findViewById(R.id.dailyDate);
        dailyDate.setInputType(0);
        dailyDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                userYear = year;
                                userMonth = month+1;
                                userDay = dayOfMonth;
                                todayDate = Integer.toString(userYear) + Integer.toString(userMonth) + Integer.toString(userDay);
                                dailyDate.setText(year + "년 " + (month+1) + "월 " + dayOfMonth + "일");

                                dailyDB = dailyHelper.getReadableDatabase();
                                Cursor cursor = dailyDB.rawQuery("SELECT * FROM daily WHERE date='" + todayDate + "';", null);

                                if ((cursor != null) && (cursor.getCount() > 0)) {
                                    // data load
                                    cursor.moveToFirst();
                                    userSleepHour   = cursor.getInt(cursor.getColumnIndex("sleepHour"));
                                    userSleepMinute = cursor.getInt(cursor.getColumnIndex("sleepMinute"));
                                    userWakeHour    = cursor.getInt(cursor.getColumnIndex("wakeHour"));
                                    userWakeMinute  = cursor.getInt(cursor.getColumnIndex("wakeMinute"));
                                    userSleepingTime = cursor.getInt(cursor.getColumnIndex("sleepingTime"));

                                    String Hour     = String.format("%02d", userSleepHour);
                                    String Minute   = String.format("%02d", userSleepMinute);
                                    sleepTimeButton.setText(Hour + ":" + Minute);

                                    Hour     = String.format("%02d", userWakeHour);
                                    Minute   = String.format("%02d", userWakeMinute);
                                    wakeTimeButton.setText(Hour + ":" + Minute);
                                    isAlreadyMade = true;
                                }
                                else {
                                    sleepTimeButton.setText("00:00");
                                    wakeTimeButton.setText("00:00");
                                    isAlreadyMade = false;
                                }

                                cursor.close();
                                dailyDB.close();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        sleepTimeButton = (Button) findViewById(R.id.sleepTimeButton);
        sleepTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        userSleepHour = hourOfDay;
                        userSleepMinute = minute;
                        setSleepingTime();

                        String Hour     = String.format("%02d", userSleepHour);
                        String Minute   = String.format("%02d", userSleepMinute);
                        sleepTimeButton.setText(Hour + ":" + Minute);

                        dailyDB = dailyHelper.getWritableDatabase();

                        if (isAlreadyMade) {
                            // data update
                            ContentValues values = new ContentValues();
                            values.put("sleepHour", userSleepHour);
                            values.put("sleepMinute", userSleepMinute);
                            values.put("wakeHour", userWakeHour);
                            values.put("wakeMinute", userWakeMinute);
                            values.put("sleepingTime", userSleepingTime);

                            String[] whereArgs = new String[] {todayDate};
                            dailyDB.update("daily", values, "date = ?", whereArgs);
                        }
                        else {
                            // data insert(new)
                            dailyDB.execSQL("INSERT INTO daily VALUES(null, '"+ todayDate +"'," +
                                    "'"+ userSleepHour +"', '"+ userSleepMinute +"', '"+ userWakeHour +"'," +
                                    "'"+ userWakeMinute +"', '"+ userSleepingTime +"');");
                        }

                        dailyDB.close();
                    }
                }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        });

        wakeTimeButton = (Button) findViewById(R.id.wakeTimeButton);
        wakeTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        userWakeHour = hourOfDay;
                        userWakeMinute = minute;
                        setSleepingTime();

                        String Hour     = String.format("%02d", userWakeHour);
                        String Minute   = String.format("%02d", userWakeMinute);
                        wakeTimeButton.setText(Hour + ":" + Minute);

                        dailyDB = dailyHelper.getWritableDatabase();

                        if (isAlreadyMade) {
                            // data update
                            ContentValues values = new ContentValues();
                            values.put("sleepHour", userSleepHour);
                            values.put("sleepMinute", userSleepMinute);
                            values.put("wakeHour", userWakeHour);
                            values.put("wakeMinute", userWakeMinute);
                            values.put("sleepingTime", userSleepingTime);

                            String[] whereArgs = new String[] {todayDate};
                            dailyDB.update("daily", values, "date = ?", whereArgs);
                            /*
                            dailyDB.execSQL("UPDATE daily SET sleepHour = userSleepHour," +
                                    "sleepMinute = userSleepMinute, wakeHour = userWakeHour," +
                                    "wakeMinute = userWakeMinute, sleepingTime = userSleepingTime WHERE date='"+ todayDate +"';");
                                    */
                        }
                        else {
                            // data insert(new)
                            dailyDB.execSQL("INSERT INTO daily VALUES(null, '"+ todayDate +"'," +
                                    "'"+ userSleepHour +"', '"+ userSleepMinute +"', '"+ userWakeHour +"'," +
                                    "'"+ userWakeMinute +"', '"+ userSleepingTime +"');");
                        }

                        dailyDB.close();
                    }
                }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        });

        moodTracker = (SeekBar)findViewById(R.id.moodTrackerInput);
        moodTracker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //what
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //what
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //what
            }
        });

        // end of onCreate()
    }

    private void setSleepingTime() {
        userSleepingTime = 0;

        if (userSleepHour > userWakeHour)
            userSleepingTime += ((23 - userSleepHour) + userWakeHour) * 60;
        else
            userSleepingTime += (userWakeHour - userSleepHour) * 60;

        if (userSleepMinute > userWakeMinute)
            userSleepingTime += (60 - userSleepMinute) + userWakeMinute;
        else
            userSleepingTime += 60 + (userWakeMinute - userSleepMinute);
    }

    public void insert(View target) {
        dailyDB.execSQL("INSERT INTO daily VALUES (null, '"+userSleepHour+"', '"+userSleepMinute
                +"', '"+userWakeHour+"', '"+userWakeMinute+"', '"+userSleepingTime+"')");
        Toast.makeText(getApplicationContext(), "성공적으로 추가되었음", Toast.LENGTH_SHORT).show();
    }

    public void search(View target) {
        Cursor cursor;
        cursor = dailyDB.rawQuery("SELECT date, sleepHour, sleepMinute, wakeHour, wakeMinute," +
                "sleepingTime FROM daily WHERE date='"+todayDate+"';", null);
    }
}
