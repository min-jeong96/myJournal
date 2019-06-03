package com.example.myjournal;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
    boolean isSelectedDate = false;
    int userYear, userMonth, userDay;

    String todayDate;
    int userSleepHour = 0, userSleepMinute = 0, userWakeHour = 0, userWakeMinute = 0, userSleepingTime = 0;
    int userMood = -1;

    Button sleepTimeButton, wakeTimeButton;
    EditText dailyDate;
    SeekBar moodTracker;

    ImageView moodFaceEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dailyHelper = new dailyDBHelper(this);

        final Calendar c = Calendar.getInstance();
        userYear    = c.get(Calendar.YEAR);
        userMonth   = c.get(Calendar.MONTH) + 1;
        userDay     = c.get(Calendar.DAY_OF_MONTH);
        userSleepHour = c.get(Calendar.HOUR_OF_DAY);
        userSleepMinute = c.get(Calendar.MINUTE);
        userWakeHour = c.get(Calendar.HOUR_OF_DAY);
        userWakeMinute = c.get(Calendar.MINUTE);

        dailyDate = (EditText) findViewById(R.id.dailyDate);
        dailyDate.setInputType(0);
        dailyDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                userYear = year;
                                userMonth = month+1;
                                userDay = dayOfMonth;
                                todayDate = String.format("%4d%02d%02d", userYear, userMonth, userDay);
                                dailyDate.setText(year + "년 " + (month+1) + "월 " + dayOfMonth + "일");
                                isSelectedDate = true;

                                initializationDailyFromDB();
                            }
                        }, userYear, userMonth-1, userDay);
                datePickerDialog.show();
            }
        });

        sleepTimeButton = (Button) findViewById(R.id.sleepTimeButton);
        sleepTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                        if (isSelectedDate)
                            writeData();
                        else
                            Toast.makeText(getApplicationContext(), "기록할 날짜를 선택하지 않았습니다", Toast.LENGTH_SHORT).show();
                    }
                }, userSleepHour, userSleepMinute, true);
                timePickerDialog.show();
            }
        });

        wakeTimeButton = (Button) findViewById(R.id.wakeTimeButton);
        wakeTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                        if (isSelectedDate)
                            writeData();
                        else
                            Toast.makeText(getApplicationContext(), "기록할 날짜를 선택하지 않았습니다", Toast.LENGTH_SHORT).show();
                    }
                }, userWakeHour, userWakeMinute, true);
                timePickerDialog.show();
            }
        });

        moodFaceEmoji = (ImageView)findViewById(R.id.moodFaceEmoji);
        moodTracker = (SeekBar)findViewById(R.id.moodTrackerInput);
        moodTracker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                userMood = seekBar.getProgress();

                if (isSelectedDate)
                    writeData();
                else
                    Toast.makeText(getApplicationContext(), "기록할 날짜를 선택하지 않았습니다", Toast.LENGTH_SHORT).show();

                switch (userMood) {
                    case 0:
                        moodFaceEmoji.setImageResource(R.drawable.loudly_crying_face_emoji);
                        break;
                    case 1:
                        moodFaceEmoji.setImageResource(R.drawable.crying_face_emoji);
                        break;
                    case 2:
                        moodFaceEmoji.setImageResource(R.drawable.neutral_face_emoji);
                        break;
                    case 3:
                        moodFaceEmoji.setImageResource(R.drawable.slightly_smiling_face_emoji);
                        break;
                    case 4:
                        moodFaceEmoji.setImageResource(R.drawable.smiling_emoji_with_smiling_eyes);
                        break;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // end of onCreate()
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.daily_journal:
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.statistics:
                intent = new Intent(getApplicationContext(), StatisticsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.setting:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSleepingTime() {
        userSleepingTime = 0;

        if (userSleepHour > userWakeHour)
            userSleepingTime += ((24 - userSleepHour) + userWakeHour) * 60 + (userWakeMinute - userSleepMinute);
        else
            userSleepingTime += (userWakeHour - userSleepHour) * 60 + (userWakeMinute - userSleepMinute);
    }

    public void writeData() {
        dailyDB = dailyHelper.getWritableDatabase();

        if (isAlreadyMade) {
            // data update
            ContentValues values = new ContentValues();
            values.put("SLEEPhour", userSleepHour);
            values.put("SLEEPminute", userSleepMinute);
            values.put("WAKEhour", userWakeHour);
            values.put("WAKEminute", userWakeMinute);
            values.put("SLEEPINGtime", userSleepingTime);
            values.put("MOOD", userMood);

            String[] whereArgs = new String[] {todayDate};
            dailyDB.update("daily", values, "DATE = ?", whereArgs);
        }
        else {
            // data insert(new)
            dailyDB.execSQL("INSERT INTO daily VALUES(null, '"+ todayDate +"'," +
                    "'"+ userSleepHour +"', '"+ userSleepMinute +"', '"+ userWakeHour +"'," +
                    "'"+ userWakeMinute +"', '"+ userSleepingTime +"', '"+ userMood +"');");
            isAlreadyMade = true;
        }

        dailyDB.close();
    }

    public void initializationDailyFromDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                dailyDB = dailyHelper.getReadableDatabase();
                Cursor cursor = dailyDB.rawQuery("SELECT * FROM daily WHERE DATE='" + todayDate + "';", null);

                if ((cursor != null) && (cursor.getCount() > 0)) {
                    cursor.moveToFirst();
                    userSleepHour       = cursor.getInt(cursor.getColumnIndex("SLEEPhour"));
                    userSleepMinute     = cursor.getInt(cursor.getColumnIndex("SLEEPminute"));
                    userWakeHour        = cursor.getInt(cursor.getColumnIndex("WAKEhour"));
                    userWakeMinute      = cursor.getInt(cursor.getColumnIndex("WAKEminute"));
                    userSleepingTime    = cursor.getInt(cursor.getColumnIndex("SLEEPINGtime"));
                    userMood             = cursor.getInt(cursor.getColumnIndex("MOOD"));

                    sleepTimeButton.post(new Runnable() {
                        @Override
                        public void run() {
                            String Hour     = String.format("%02d", userSleepHour);
                            String Minute   = String.format("%02d", userSleepMinute);
                            sleepTimeButton.setText(Hour + ":" + Minute);
                        }
                    });

                    wakeTimeButton.post(new Runnable() {
                        @Override
                        public void run() {
                            String Hour     = String.format("%02d", userWakeHour);
                            String Minute   = String.format("%02d", userWakeMinute);
                            wakeTimeButton.setText(Hour + ":" + Minute);
                        }
                    });

                    if (userMood < 0) {
                        moodFaceEmoji.post(new Runnable() {
                            @Override
                            public void run() { moodFaceEmoji.setImageResource(R.drawable.expressionless_emoji); }
                        });
                    }
                    else {
                        moodTracker.post(new Runnable() {
                            @Override
                            public void run() { moodTracker.setProgress(userMood); }
                        });
                    }

                    isAlreadyMade = true;
                }
                else {
                    userSleepHour = userSleepMinute = userWakeHour = userWakeMinute = userSleepingTime = 0;
                    userMood = -1;

                    sleepTimeButton.post(new Runnable() {
                        @Override
                        public void run() { sleepTimeButton.setText("00:00"); }
                    });

                    wakeTimeButton.post(new Runnable() {
                        @Override
                        public void run() { wakeTimeButton.setText("00:00"); }
                    });

                    moodFaceEmoji.post(new Runnable() {
                        @Override
                        public void run() { moodFaceEmoji.setImageResource(R.drawable.expressionless_emoji); }
                    });

                    isAlreadyMade = false;
                }

                cursor.close();
                dailyDB.close();

            }
        }).start();
    }
}
