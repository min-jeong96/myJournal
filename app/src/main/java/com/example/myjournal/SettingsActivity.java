package com.example.myjournal;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import static java.security.AccessController.getContext;

public class SettingsActivity extends AppCompatActivity {
    dailyDBHelper dailyHelper;
    SQLiteDatabase dailyDB;

    SharedPreferences settings;
    SharedPreferences.Editor editor;
    public static final String PREFS_HABIT_SETTINGS = "MyHabitTracker";
    public static final String PREFS_NOTIFICATION_SETTINGS = "MyJournalSettings";

    boolean isAlreadySet = false;
    int setSleepHour, setSleepMinute, setWakeHour, setWakeMinute, setSleepingTime;

    HabitSettingsAdapter habitSettingsAdapter;
    ArrayList<String> titles, descriptions, preferencesNum;
    int habitNum = 0;

    RecyclerView recyclerView;
    Button setSleepTimeButton, setWakeTimeButton, addHabitButton;
    TextView setSleepingTimeText;

    Switch switchNotification;
    boolean userChangedNotiSwitch = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        recyclerView = findViewById(R.id.list_habit_tracker_settings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setSleepTimeButton = (Button)findViewById(R.id.btn_set_bedtime);
        setWakeTimeButton = (Button)findViewById(R.id.btn_set_wakeTime);
        setSleepingTimeText = (TextView)findViewById(R.id.text_set_sleeping_time);

        setSleepTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingsActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setSleepHour = hourOfDay;
                        setSleepMinute = minute;
                        setSleepingTime();

                        String Hour     = String.format("%02d", setSleepHour);
                        String Minute   = String.format("%02d", setSleepMinute);
                        setSleepTimeButton.setText(Hour + ":" + Minute);

                        int setSleepingHour     = setSleepingTime / 60;
                        int setSleepingMinute   = setSleepingTime % 60;
                        setSleepingTimeText.setText(setSleepingHour + "시간 " + setSleepingMinute + "분");

                        writeSettingData();
                    }
                }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        });

        setWakeTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingsActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setWakeHour = hourOfDay;
                        setWakeMinute = minute;
                        setSleepingTime();

                        String Hour     = String.format("%02d", setWakeHour);
                        String Minute   = String.format("%02d", setWakeMinute);
                        setWakeTimeButton.setText(Hour + ":" + Minute);

                        int setSleepingHour     = setSleepingTime / 60;
                        int setSleepingMinute   = setSleepingTime % 60;
                        setSleepingTimeText.setText(setSleepingHour + "시간 " + setSleepingMinute + "분");

                        writeSettingData();
                    }
                }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        });

        switchNotification = (Switch) findViewById(R.id.switch_notification);
        switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings = getSharedPreferences(PREFS_NOTIFICATION_SETTINGS, 0);
                    editor = settings.edit();
                    editor.putBoolean("NOTIFICATION", true);
                    editor.commit();

                    Intent intent = new Intent(SettingsActivity.this, NotificationService.class);
                    intent.putExtra("SLEEP HOUR", setSleepHour);
                    intent.putExtra("SLEEP MINUTE", setSleepMinute);
                    intent.putExtra("WAKE HOUR", setWakeHour);
                    intent.putExtra("WAKE MINUTE", setWakeMinute);
                    startService(intent);

                    if(userChangedNotiSwitch) {
                        Toast.makeText(getApplicationContext(), "알람이 설정되었습니다",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if(userChangedNotiSwitch) {
                        settings = getSharedPreferences(PREFS_NOTIFICATION_SETTINGS, 0);
                        editor = settings.edit();
                        editor.putBoolean("NOTIFICATION", false);
                        editor.commit();

                        Intent intent = new Intent(SettingsActivity.this, NotificationService.class);
                        stopService(intent);

                        Toast.makeText(getApplicationContext(), "알람이 취소되었습니다",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                userChangedNotiSwitch = true;
            }
        });

        dailyHelper = new dailyDBHelper(this);
        initializationSettingsFromDB();

        titles          = new ArrayList<String>();
        descriptions    = new ArrayList<String>();
        preferencesNum  = new ArrayList<String>();

        dailyDB = dailyHelper.getWritableDatabase();
        Cursor cursor = dailyDB.rawQuery("SELECT * FROM habit WHERE DATE = 'SETTINGS';",null);
        if ((cursor != null) && (cursor.getCount() > 0))
            cursor.moveToFirst();

        settings = getSharedPreferences(PREFS_HABIT_SETTINGS, 0);
        habitNum = settings.getInt("USED MAX HABIT NUMBER", 0);

        for (int i = 0; i < habitNum; i++) {
            String habitTitleReference = "TITLE" + Integer.toString(i);
            String habitDescReference = "DESC" + Integer.toString(i);
            String habitPreferenceNum = Integer.toString(i);
            String tmp_title = settings.getString(habitTitleReference, " ");

            String getDBColumn = cursor.getString(cursor.getColumnIndex(habitTitleReference));

            if (!(getDBColumn.equals("DELETED"))) {
                if (!(tmp_title.equals(" "))) {
                    titles.add(settings.getString(habitTitleReference, " "));
                    descriptions.add(settings.getString(habitDescReference, " "));
                    preferencesNum.add(habitPreferenceNum);
                }
            }
        }

        habitSettingsAdapter = new HabitSettingsAdapter(this, titles, descriptions, preferencesNum);
        recyclerView.setAdapter(habitSettingsAdapter);

        addHabitButton = findViewById(R.id.btn_add_habit);
        addHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog addHabitDialog = new Dialog(SettingsActivity.this);
                addHabitDialog.setContentView(R.layout.dialog_add_habit_settings);

                Button confirm_add_habit    = (Button) addHabitDialog.findViewById(R.id.btn_confirm_add_habit);
                Button cancel_add_habit     = (Button) addHabitDialog.findViewById(R.id.btn_cancel_add_habit);

                final EditText add_habit_title          = (EditText) addHabitDialog.findViewById(R.id.add_habit_title);
                final EditText add_habit_description    = (EditText) addHabitDialog.findViewById(R.id.add_habit_description);

                confirm_add_habit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (add_habit_title.getText().toString().trim().length() > 0) {
                            titles.add(add_habit_title.getText().toString());

                            if(add_habit_description.getText().toString().trim().length() > 0) {
                                descriptions.add(add_habit_description.getText().toString());
                            } else {
                                descriptions.add(" ");
                            }

                            String newPreferencesNum= Integer.toString(habitNum);
                            preferencesNum.add(newPreferencesNum);

                            dailyDB = dailyHelper.getWritableDatabase();

                            String columnTitle = "TITLE" + Integer.toString(habitNum);
                            String columnValue = "VALUE" + Integer.toString(habitNum);

                            if(dailyHelper.addColumn(dailyDB, columnTitle, columnValue))
                                Log.d("TAG", "ADD COLUMN: " + columnTitle);
                            dailyDB.close();

                            dailyDB = dailyHelper.getWritableDatabase();
                            Cursor cursor = dailyDB.rawQuery("SELECT * FROM habit WHERE DATE = 'SETTINGS';",null);
                            if ((cursor != null) && (cursor.getCount() > 0)) {
                                // data update
                                ContentValues values = new ContentValues();
                                values.put(columnTitle, columnTitle);
                                values.put(columnValue, 0);

                                String[] whereArgs = {"SETTINGS"};
                                dailyDB.update("habit", values, "DATE = ?", whereArgs);
                            }
                            else
                                dailyDB.execSQL("INSERT INTO habit VALUES('SETTINGS', '"+columnTitle+"', 0);");

                            dailyDB.close();

                            settings = getSharedPreferences(PREFS_HABIT_SETTINGS, 0);
                            editor = settings.edit();

                            String habitTitleReference  = "TITLE" + Integer.toString(habitNum);
                            String habitDescReference   = "DESC" + Integer.toString(habitNum);
                            editor.putString(habitTitleReference, add_habit_title.getText().toString());

                            if(add_habit_description.getText().toString().trim().length() > 0) {
                                editor.putString(habitDescReference, add_habit_description.getText().toString());
                            } else {
                                editor.putString(habitDescReference, " ");
                            }

                            habitNum++;
                            editor.putInt("USED MAX HABIT NUMBER", habitNum);
                            editor.commit();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    habitSettingsAdapter = new HabitSettingsAdapter(SettingsActivity.this, titles, descriptions, preferencesNum);
                                    recyclerView.setAdapter(habitSettingsAdapter);
                                }
                            });

                            Toast.makeText(getApplicationContext(), "새로운 습관이 등록되었습니다",
                                    Toast.LENGTH_SHORT).show();
                            addHabitDialog.dismiss();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "습관에 대해 작성해주세요",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                cancel_add_habit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addHabitDialog.dismiss();

                        Toast.makeText(getApplicationContext(), "습관 등록이 취소되었습니다",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                addHabitDialog.show();
            }
        });
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
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.statistics:
                intent = new Intent(getApplicationContext(), StatisticsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.setting:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSleepingTime() {
        setSleepingTime = 0;

        if (setSleepHour > setWakeHour)
            setSleepingTime += ((23 - setSleepHour) + setWakeHour) * 60;
        else
            setSleepingTime += (setWakeHour - setSleepHour) * 60;

        if (setSleepMinute > setWakeMinute)
            setSleepingTime += (60 - setSleepMinute) + setWakeMinute;
        else
            setSleepingTime += 60 + (setWakeMinute - setSleepMinute);
    }

    public void initializationSettingsFromDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                settings = getSharedPreferences(PREFS_NOTIFICATION_SETTINGS, 0);
                if(settings.getBoolean("NOTIFICATION", false)) {
                    switchNotification.post(new Runnable() {
                        @Override
                        public void run() {
                            switchNotification.setChecked(true);
                        }
                    });
                }
                else {
                    switchNotification.post(new Runnable() {
                        @Override
                        public void run() {
                            switchNotification.setChecked(false);
                            userChangedNotiSwitch = false;
                        }
                    });
                }

                dailyDB = dailyHelper.getReadableDatabase();
                Cursor cursor = dailyDB.rawQuery("SELECT * FROM daily WHERE DATE = 'SETTINGS';",null);

                if ((cursor != null) && (cursor.getCount() > 0)) {
                    cursor.moveToFirst();
                    setSleepHour       = cursor.getInt(cursor.getColumnIndex("SLEEPhour"));
                    setSleepMinute     = cursor.getInt(cursor.getColumnIndex("SLEEPminute"));
                    setWakeHour        = cursor.getInt(cursor.getColumnIndex("WAKEhour"));
                    setWakeMinute      = cursor.getInt(cursor.getColumnIndex("WAKEminute"));
                    setSleepingTime    = cursor.getInt(cursor.getColumnIndex("SLEEPINGtime"));

                    setSleepTimeButton.post(new Runnable() {
                        @Override
                        public void run() {
                            String Hour     = String.format("%02d", setSleepHour);
                            String Minute   = String.format("%02d", setSleepMinute);
                            setSleepTimeButton.setText(Hour + ":" + Minute);
                        }
                    });

                    setWakeTimeButton.post(new Runnable() {
                        @Override
                        public void run() {
                            String Hour     = String.format("%02d", setWakeHour);
                            String Minute   = String.format("%02d", setWakeMinute);
                            setWakeTimeButton.setText(Hour + ":" + Minute);
                        }
                    });

                    setSleepingTimeText.post(new Runnable() {
                        @Override
                        public void run() {
                            int setSleepingHour     = setSleepingTime / 60;
                            int setSleepingMinute   = setSleepingTime % 60;
                            setSleepingTimeText.setText(setSleepingHour + "시간 " + setSleepingMinute + "분");
                        }
                    });

                    isAlreadySet = true;
                }
                else {
                    setSleepHour = setSleepMinute = setWakeHour = setWakeMinute = setSleepingTime = 0;

                    setSleepTimeButton.post(new Runnable() {
                        @Override
                        public void run() { setSleepTimeButton.setText("00:00"); }
                    });

                    setWakeTimeButton.post(new Runnable() {
                        @Override
                        public void run() { setWakeTimeButton.setText("00:00"); }
                    });

                    setSleepingTimeText.post(new Runnable() {
                        @Override
                        public void run() {
                            setSleepingTimeText.setText("설정되지 않았습니다.");
                        }
                    });

                    isAlreadySet = false;
                }
                cursor.close();
                dailyDB.close();
            }
        }).start();
    }

    public void writeSettingData() {
        dailyDB = dailyHelper.getWritableDatabase();

        if (isAlreadySet) {
            // data update
            ContentValues values = new ContentValues();
            values.put("SLEEPhour", setSleepHour);
            values.put("SLEEPminute", setSleepMinute);
            values.put("WAKEhour", setWakeHour);
            values.put("WAKEminute", setWakeMinute);
            values.put("SLEEPINGtime", setSleepingTime);
            values.put("MOOD", 2);

            String[] whereArgs = {"SETTINGS"};
            dailyDB.update("daily", values, "DATE = ?", whereArgs);
        }
        else {
            // data insert(new)
            int setMood = 2;
            dailyDB.execSQL("INSERT INTO daily VALUES(null, 'SETTINGS'," +
                    "'"+ setSleepHour +"', '"+ setSleepMinute +"', '"+ setWakeHour +"'," +
                    "'"+ setWakeMinute +"', '"+ setSleepingTime +"', '"+ setMood +"', null);");
            isAlreadySet = true;
        }

        dailyDB.close();
    }
}
