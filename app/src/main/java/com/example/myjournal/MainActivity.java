package com.example.myjournal;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    dailyDBHelper dailyHelper;
    SQLiteDatabase dailyDB;

    SharedPreferences settings;
    SharedPreferences.Editor editor;
    public static final String PREFS_HABIT_TRACKER = "MyHabitTracker";
    public static final String PREFS_TODO_LIST = "MyToDoList";

    boolean isAlreadyMade = false;
    boolean isSelectedDate = false;
    int userYear, userMonth, userDay;

    String todayDate;
    int userSleepHour = 0, userSleepMinute = 0, userWakeHour = 0, userWakeMinute = 0, userSleepingTime = 0;
    int userMood = 2;

    Button sleepTimeButton, wakeTimeButton;
    EditText dailyDate;
    SeekBar moodTracker;
    ImageView moodFaceEmoji;

    EditText dailyJournal;
    String userJournal = null;

    int habitNum;

    HabitTrackerAdapter habitTrackerAdapter;
    ArrayList<String> mHabitTitle = null;
    ArrayList<Integer> mDoneHabit = null;
    ArrayList<String> mColumnDB = null;
    RecyclerView recyclerView_habit;

    ToDoListAdapter toDoListAdapter;
    ArrayList<String> mTaskTitle = null;
    ArrayList<Integer> mTaskState = null;
    RecyclerView recyclerView_task;

    Button addTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView_task = (RecyclerView) findViewById(R.id.list_task);
        recyclerView_task.setLayoutManager(new LinearLayoutManager(this));
        mTaskTitle  = new ArrayList<String>();
        mTaskState  = new ArrayList<Integer>();

        recyclerView_habit = (RecyclerView) findViewById(R.id.list_habit_tracker);
        recyclerView_habit.setLayoutManager(new LinearLayoutManager(this));
        mHabitTitle     = new ArrayList<String>();
        mColumnDB       = new ArrayList<String>();
        mDoneHabit      = new ArrayList<Integer>();

        dailyHelper = new dailyDBHelper(this);
        dailyDB = dailyHelper.getWritableDatabase();
        Cursor cursor = dailyDB.rawQuery("SELECT * FROM habit WHERE DATE = 'SETTINGS';",null);
        if ((cursor != null) && (cursor.getCount() > 0))
            cursor.moveToFirst();

        settings = getSharedPreferences(PREFS_HABIT_TRACKER, 0);
        habitNum = settings.getInt("USED MAX HABIT NUMBER", 0);

        for (int i = 0; i < habitNum; i++) {
            String habitTitleReference = "TITLE" + Integer.toString(i);
            String habitValueReference = "VALUE" + Integer.toString(i);
            String tmp_title = settings.getString(habitTitleReference, " ");

            String getDBColumn = cursor.getString(cursor.getColumnIndex(habitTitleReference));

            if (!(getDBColumn.equals("DELETED"))) {
                if (!(tmp_title.equals(" "))) {
                    mHabitTitle.add(settings.getString(habitTitleReference, " "));
                    mColumnDB.add(habitValueReference);
                    mDoneHabit.add(0);

                    Log.d("TAG", "mHabitTitle.add " + mHabitTitle.get(i));
                    Log.d("TAG", "mColumnDB.add " + mColumnDB.get(i));
                    Log.d("TAG", "mDoneHabit.add " + mDoneHabit.get(i).toString());
                }
            }
        }
        cursor.close();
        dailyDB.close();
        habitTrackerAdapter = new HabitTrackerAdapter(this, mHabitTitle, mColumnDB, mDoneHabit);
        recyclerView_habit.setAdapter(habitTrackerAdapter);

        final Calendar c = Calendar.getInstance();
        userYear    = c.get(Calendar.YEAR);
        userMonth   = c.get(Calendar.MONTH) + 1;
        userDay     = c.get(Calendar.DAY_OF_MONTH);
        userSleepHour = c.get(Calendar.HOUR_OF_DAY);
        userSleepMinute = c.get(Calendar.MINUTE);
        userWakeHour = c.get(Calendar.HOUR_OF_DAY);
        userWakeMinute = c.get(Calendar.MINUTE);

        addTaskButton = findViewById(R.id.btn_add_Task);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTaskTitle.size() > 9) {
                    Toast.makeText(getApplicationContext(), "10개의 할 일을 모두 등록하였습니다",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                final Dialog addTaskDialog = new Dialog(MainActivity.this);
                addTaskDialog.setContentView(R.layout.dialog_add_task);

                Button confirm_add_task = (Button)addTaskDialog.findViewById(R.id.btn_confirm_add_task);
                Button cancel_add_task  = (Button)addTaskDialog.findViewById(R.id.btn_cancel_add_task);

                final EditText add_task_title = (EditText)addTaskDialog.findViewById(R.id.add_task_title);

                confirm_add_task.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (add_task_title.getText().toString().trim().length() > 0) {
                            mTaskTitle.add(add_task_title.getText().toString());
                            mTaskState.add(0);

                            dailyDB = dailyHelper.getWritableDatabase();
                            Cursor cursor = dailyDB.rawQuery("SELECT * FROM tasks WHERE DATE='" + todayDate + "';", null);

                            if ((cursor != null) && (cursor.getCount() > 0)) {
                                cursor.moveToFirst();
                                int taskNum = cursor.getInt(cursor.getColumnIndex("NUM_OF_TASK"));
                                String columnTitle = "TITLE" + Integer.toString(taskNum);
                                String columnValue = "VALUE" + Integer.toString(taskNum);

                                ContentValues values = new ContentValues();
                                values.put("NUM_OF_TASK", (taskNum+1));
                                values.put(columnTitle, add_task_title.getText().toString());
                                values.put(columnValue, 0);

                                String[] whereArgs = {todayDate};
                                dailyDB.update("tasks", values, "DATE = ?", whereArgs);
                            }
                            else {
                                ContentValues values = new ContentValues();
                                values.put("DATE", todayDate);
                                values.put("NUM_OF_TASK", 1);
                                values.put("TITLE0", add_task_title.getText().toString());
                                values.put("VALUE0", 0);
                                dailyDB.insert("tasks", null, values);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toDoListAdapter = new ToDoListAdapter(MainActivity.this,
                                            mTaskTitle, mTaskState, todayDate);
                                    recyclerView_task.setAdapter(toDoListAdapter);
                                }
                            });

                            dailyDB.close();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "할 일을 입력해주세요",
                                    Toast.LENGTH_SHORT).show();
                        }

                        Toast.makeText(getApplicationContext(), "할 일이 등록되었습니다",
                                Toast.LENGTH_SHORT).show();
                        addTaskDialog.dismiss();
                    }
                });

                cancel_add_task.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addTaskDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "등록이 취소되었습니다",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                addTaskDialog.show();
            }
        });

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

                                dailyDB = dailyHelper.getWritableDatabase();
                                Cursor cursor = dailyDB.rawQuery("SELECT * FROM habit WHERE DATE='" + todayDate + "';", null);

                                settings = getSharedPreferences(PREFS_HABIT_TRACKER, 0);
                                habitNum = settings.getInt("USED MAX HABIT NUMBER", 0);

                                Cursor tmpCursor = dailyDB.rawQuery("SELECT * FROM habit WHERE DATE = 'SETTINGS';",null);
                                if ((tmpCursor != null) && (tmpCursor.getCount() > 0))
                                    tmpCursor.moveToFirst();

                                Boolean isDBdata = false;
                                if ((cursor != null) && (cursor.getCount() > 0)) {
                                    cursor.moveToFirst();
                                    isDBdata = true;
                                    Log.d("TAG", "HAVE DATA FROM DB");
                                }

                                mDoneHabit  = new ArrayList<Integer>();
                                for (int i = 0; i < habitNum; i++) {
                                    String habitTitleReference = "TITLE" + Integer.toString(i);
                                    String habitValueReference = "VALUE" + Integer.toString(i);
                                    String getDBColumn = tmpCursor.getString(tmpCursor.getColumnIndex(habitTitleReference));
                                    if (!(getDBColumn.equals("DELETED"))) {
                                        if (isDBdata) {
                                            int tmp = cursor.getInt(cursor.getColumnIndex(habitValueReference));
                                            mDoneHabit.add(tmp);
                                        }
                                        else
                                            mDoneHabit.add(0);
                                        Log.d("TAG", "title: " + settings.getString(habitTitleReference, " "));
                                        Log.d("TAG", "value: " + mDoneHabit.get(i).toString());
                                    }
                                }

                                habitTrackerAdapter = new HabitTrackerAdapter(MainActivity.this,
                                        mHabitTitle, mColumnDB, mDoneHabit, todayDate);
                                recyclerView_habit.setAdapter(habitTrackerAdapter);

                                mTaskTitle  = new ArrayList<String>();
                                mTaskState  = new ArrayList<Integer>();

                                cursor = dailyDB.rawQuery("SELECT * FROM tasks WHERE DATE='" + todayDate + "';", null);
                                if ((cursor != null) && (cursor.getCount() > 0)) {
                                    cursor.moveToFirst();
                                    int numOfTask = cursor.getInt(cursor.getColumnIndex("NUM_OF_TASK"));

                                    for(int i = 0; i < numOfTask; i++) {
                                        String titleColumn = "TITLE" + Integer.toString(i);
                                        String taskTitle = cursor.getString(cursor.getColumnIndex(titleColumn));

                                        String valueColumn = "VALUE" + Integer.toString(i);
                                        int taskValue = cursor.getInt(cursor.getColumnIndex(valueColumn));

                                        mTaskTitle.add(taskTitle);
                                        mTaskState.add(taskValue);

                                        Log.d("TAG", "mTaskTitle.add " + mTaskTitle.get(i));
                                        Log.d("TAG", "mTaskState.add " + mTaskState.get(i).toString());
                                    }
                                }

                                toDoListAdapter = new ToDoListAdapter(MainActivity.this,
                                        mTaskTitle, mTaskState, todayDate);
                                recyclerView_task.setAdapter(toDoListAdapter);

                                tmpCursor.close();
                                cursor.close();
                                dailyDB.close();

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

        dailyJournal = (EditText) findViewById(R.id.text_dailyJournal);
        dailyJournal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                dailyDB = dailyHelper.getWritableDatabase();
                userJournal = dailyJournal.getText().toString();
                writeData();
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
        } //FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP
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
            values.put("JOURNAL", userJournal);

            String[] whereArgs = new String[] {todayDate};
            dailyDB.update("daily", values, "DATE = ?", whereArgs);
        }
        else {
            // data insert(new)
            dailyDB.execSQL("INSERT INTO daily VALUES(null, '"+ todayDate +"'," +
                    "'"+ userSleepHour +"', '"+ userSleepMinute +"', '"+ userWakeHour +"'," +
                    "'"+ userWakeMinute +"', '"+ userSleepingTime +"', '"+ userMood +"', '"+userJournal+"');");
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
                    userJournal         = cursor.getString(cursor.getColumnIndex("JOURNAL"));

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

                    moodTracker.post(new Runnable() {
                        @Override
                        public void run() { moodTracker.setProgress(userMood); }
                    });
                    /*
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
                    */

                    dailyJournal.post(new Runnable() {
                        @Override
                        public void run() { dailyJournal.setText(userJournal); }
                    });

                    isAlreadyMade = true;
                }
                else {
                    userSleepHour = userSleepMinute = userWakeHour = userWakeMinute = userSleepingTime = 0;
                    userMood = 2;

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

                    dailyJournal.post(new Runnable() {
                        @Override
                        public void run() { dailyJournal.setText(null); }
                    });

                    isAlreadyMade = false;
                }

                dailyDB.close();

            }
        }).start();
    }
}
