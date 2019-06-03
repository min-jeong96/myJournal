package com.example.myjournal;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class StatisticsActivity extends AppCompatActivity {
    dailyDBHelper dailyHelper;
    SQLiteDatabase dailyDB;

    boolean hasAnyData = false;
    final static int ERROR_DEVIATION = 60;

    int userSelectedYear, userSelectedMonth;
    int previousSelectedYear, previousSelectedMonth;
    String beginOfMonth, endOfMonth;

    EditText statisticsMonth;
    TextView avgSleepTimeText, avgWakeTimeText, avgSleepingTimeText;
    TextView sleepTimeStatisticsText, wakeTimeStatisticsText, sleepingTimeStatisticsText;

    ImageView sleepTimeImage, wakeTimeImage, sleepingTimeImage;
    LinearLayout sleepAnalysisLayout, sleepAnalysisImageLayout;

    int sumOfSleepHour, sumOfSleepMinute, sumOfWakeHour, sumOfWakeMinute, sumOfSleepingTime;
    int avgOfSleepHour, avgOfSleepMinute, avgOfWakeHour, avgOfWakeMinute, avgOfSleepingTime;
    int numOfSleepHour, numOfSleepMinute, numOfWakeHour, numOfWakeMinute, numOfSleepingTime;
    int setOfSleepHour, setOfSleepMinute, setOfWakeHour, setOfWakeMinute, setOfSleepingTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        dailyHelper = new dailyDBHelper(this);

        final Calendar c = Calendar.getInstance();
        previousSelectedMonth   = userSelectedMonth = c.get(Calendar.MONTH)+1;
        previousSelectedYear    = userSelectedYear  = c.get(Calendar.YEAR);
        beginOfMonth    = String.format("%4d%02d00", previousSelectedYear, previousSelectedMonth);
        endOfMonth      = String.format("%4d%02d32", previousSelectedYear, previousSelectedMonth);

        sleepTimeImage      = (ImageView)findViewById(R.id.image_sleepTime_statistics);
        wakeTimeImage       = (ImageView)findViewById(R.id.image_wakeTime_statistics);
        sleepingTimeImage   = (ImageView)findViewById(R.id.image_sleepingTime_statistics);
        avgSleepTimeText    = (TextView)findViewById(R.id.avg_sleep_time);
        avgWakeTimeText     = (TextView)findViewById(R.id.avg_wake_time);
        avgSleepingTimeText = (TextView)findViewById(R.id.avg_sleeping_time);
        sleepTimeStatisticsText     = (TextView)findViewById(R.id.text_sleepTime_statistics);
        wakeTimeStatisticsText      = (TextView)findViewById(R.id.text_wakeTime_statistics);
        sleepingTimeStatisticsText  = (TextView)findViewById(R.id.text_sleepingTime_statistics);

        sleepAnalysisLayout         = (LinearLayout)findViewById(R.id.layout_sleep_analysis);
        sleepAnalysisImageLayout    = (LinearLayout)findViewById(R.id.layout_image_sleep_analysis);

        statisticsMonth = (EditText)findViewById(R.id.statisticsMonth);
        statisticsMonth.setInputType(0);
        statisticsMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Custom dialog: year and month picker
                final Dialog monthPicker = new Dialog(StatisticsActivity.this);
                monthPicker.setContentView(R.layout.dialog_month_picker);
                monthPicker.setTitle("month picker");

                Button btnConfirm   = (Button)monthPicker.findViewById(R.id.btn_confirm);
                Button btnCancel    = (Button)monthPicker.findViewById(R.id.btn_cancel);

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        previousSelectedMonth = userSelectedMonth;
                        previousSelectedYear = userSelectedYear;
                        statisticsMonth.setText(previousSelectedYear + "년 " + previousSelectedMonth + "월");
                        sumOfSleepHour = sumOfSleepMinute = sumOfWakeHour = sumOfWakeMinute = sumOfSleepingTime = 0;
                        numOfSleepHour = numOfSleepMinute = numOfWakeHour = numOfWakeMinute = numOfSleepingTime = 0;

                        beginOfMonth    = String.format("%4d%02d00", previousSelectedYear, previousSelectedMonth);
                        endOfMonth      = String.format("%4d%02d32", previousSelectedYear, previousSelectedMonth);

                        statisticsDailyFromDB();
                        monthPicker.dismiss();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        monthPicker.dismiss();
                    }
                });

                NumberPicker pickerYear     = (NumberPicker)monthPicker.findViewById(R.id.picker_year);
                NumberPicker pickerMonth    = (NumberPicker)monthPicker.findViewById(R.id.picker_month);

                pickerMonth.setMinValue(1);
                pickerMonth.setMaxValue(12);
                pickerMonth.setValue(previousSelectedMonth);
                pickerMonth.setWrapSelectorWheel(false);
                pickerMonth.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        userSelectedMonth = newVal;
                    }
                });

                pickerYear.setMinValue(2000);
                pickerYear.setMaxValue(2099);
                pickerYear.setValue(previousSelectedYear);
                pickerYear.setWrapSelectorWheel(false);
                pickerYear.setOnLongPressUpdateInterval(5);
                pickerYear.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        userSelectedYear = newVal;
                    }
                });

                monthPicker.show();
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

    public void statisticsDailyFromDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                dailyDB = dailyHelper.getWritableDatabase();
                String[] whereArgs = new String[] {beginOfMonth, endOfMonth};
                Cursor cursor = dailyDB.rawQuery("SELECT * FROM daily WHERE DATE > ? AND date < ?", whereArgs);

                if ((cursor != null) && (cursor.getCount() > 0)) {
                    sleepAnalysisLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            sleepAnalysisLayout.setVisibility(View.VISIBLE);
                        }
                    });
                    sleepAnalysisImageLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            sleepAnalysisImageLayout.setVisibility(View.VISIBLE);
                        }
                    });

                    if (cursor.moveToFirst()) {
                        do {
                            int tmpSleepHour = cursor.getInt(cursor.getColumnIndex("SLEEPhour"));
                            if ((11 < tmpSleepHour) && (tmpSleepHour < 24)) {
                                sumOfSleepHour += tmpSleepHour;
                                numOfSleepHour++;
                            }
                            else {
                                sumOfSleepHour += tmpSleepHour + 24;
                                numOfSleepHour++;
                            }

                            int tmpSleepMinute = cursor.getInt(cursor.getColumnIndex("SLEEPminute"));
                            sumOfSleepMinute += tmpSleepMinute;
                            numOfSleepMinute++;

                            int tmpWakeHour = cursor.getInt(cursor.getColumnIndex("WAKEhour"));
                            sumOfWakeHour += tmpWakeHour;
                            numOfWakeHour++;

                            int tmpWakeMinute = cursor.getInt(cursor.getColumnIndex("WAKEminute"));
                            sumOfWakeMinute += tmpWakeMinute;
                            numOfWakeMinute++;

                            int tmpSleepingTime = cursor.getInt(cursor.getColumnIndex("SLEEPINGtime"));
                            sumOfSleepingTime += tmpSleepingTime;
                            numOfSleepingTime++;

                        } while (cursor.moveToNext());
                        hasAnyData = true;
                    }
                }
                else {
                    hasAnyData = false;
                }

                if (hasAnyData) {
                    avgOfSleepHour      = (sumOfSleepHour * 60 + sumOfSleepMinute) / numOfSleepHour / 60;
                    if(avgOfSleepHour>23) avgOfSleepHour -= 24;
                    avgOfSleepMinute    = (sumOfSleepHour * 60 + sumOfSleepMinute) / numOfSleepMinute % 60;
                    avgSleepTimeText.post(new Runnable() {
                        @Override
                        public void run() {
                            String Hour     = String.format("%02d", avgOfSleepHour);
                            String Minute   = String.format("%02d", avgOfSleepMinute);
                            avgSleepTimeText.setText(Hour + ":" + Minute);
                        }
                    });

                    avgOfWakeHour   = (sumOfWakeHour * 60 + sumOfWakeMinute) / numOfWakeHour / 60;
                    avgOfWakeMinute = (sumOfWakeHour * 60 + sumOfWakeMinute) / numOfWakeMinute % 60;
                    avgWakeTimeText.post(new Runnable() {
                        @Override
                        public void run() {
                            String Hour     = String.format("%02d", avgOfWakeHour);
                            String Minute   = String.format("%02d", avgOfWakeMinute);
                            avgWakeTimeText.setText(Hour + ":" + Minute);
                        }
                    });

                    avgOfSleepingTime = sumOfSleepingTime / numOfSleepingTime;
                    avgSleepingTimeText.post(new Runnable() {
                        @Override
                        public void run() {
                            int avgSleepingHour     = avgOfSleepingTime / 60;
                            int avgSleepingMinute   = avgOfSleepingTime % 60;
                            avgSleepingTimeText.setText(avgSleepingHour + "시간 " + avgSleepingMinute + "분");
                        }
                    });

                    Cursor cursor2 = dailyDB.rawQuery("SELECT * FROM daily WHERE DATE = 'SETTINGS';", null);
                    if ((cursor2 != null) && (cursor.getCount() > 0)) {
                        cursor2.moveToFirst();
                        setOfSleepHour       = cursor2.getInt(cursor.getColumnIndex("SLEEPhour"));
                        setOfSleepMinute     = cursor2.getInt(cursor.getColumnIndex("SLEEPminute"));
                        setOfWakeHour        = cursor2.getInt(cursor.getColumnIndex("WAKEhour"));
                        setOfWakeMinute      = cursor2.getInt(cursor.getColumnIndex("WAKEminute"));
                        setOfSleepingTime    = cursor2.getInt(cursor.getColumnIndex("SLEEPINGtime"));

                        int cmpSleepTime;
                        if ((avgOfSleepHour > setOfSleepHour) && (setOfSleepHour <= 12))
                            cmpSleepTime = ((24 + setOfSleepHour) - avgOfSleepHour) * 60 + (setOfSleepMinute - avgOfSleepMinute);
                        else
                            cmpSleepTime = (setOfSleepHour - avgOfSleepHour) * 60 + (setOfSleepMinute - avgOfSleepMinute);

                        if (cmpSleepTime >= ERROR_DEVIATION) {
                            sleepTimeImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepTimeImage.setImageResource(R.drawable.sleep_time_bad);
                                }
                            });
                            sleepTimeStatisticsText.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepTimeStatisticsText.setText("계획보다 훨씬 늦게 잠들고");
                                }
                            });
                        }
                        else {
                            sleepTimeImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepTimeImage.setImageResource(R.drawable.sleep_time_good);
                                }
                            });
                            sleepTimeStatisticsText.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepTimeStatisticsText.setText("계획했던 시간에 잠들고");
                                }
                            });
                        }

                        int cmpWakeTime = (setOfWakeHour - avgOfWakeHour) * 60 + (setOfWakeMinute - avgOfWakeMinute);
                        if ((cmpWakeTime >= ERROR_DEVIATION) || (cmpWakeTime <= -ERROR_DEVIATION)) {
                            wakeTimeImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    wakeTimeImage.setImageResource(R.drawable.wake_time_bad);
                                }
                            });
                            wakeTimeStatisticsText.post(new Runnable() {
                                @Override
                                public void run() {
                                    wakeTimeStatisticsText.setText("계획보다 훨씬 늦게 일어나고");
                                }
                            });
                        }
                        else {
                            wakeTimeImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    wakeTimeImage.setImageResource(R.drawable.wake_time_good);
                                }
                            });
                            wakeTimeStatisticsText.post(new Runnable() {
                                @Override
                                public void run() {
                                    wakeTimeStatisticsText.setText("계획했던 시간에 일어나고");
                                }
                            });
                        }

                        int cmpSleepingTime = setOfSleepingTime - avgOfSleepingTime;
                        if (cmpSleepingTime >= ERROR_DEVIATION) {
                            sleepingTimeImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepingTimeImage.setImageResource(R.drawable.sleeping_time_bad);
                                }
                            });
                            sleepingTimeStatisticsText.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepingTimeStatisticsText.setText("계획보다 훨씬 많은 잠을 잤습니다");
                                }
                            });
                        }
                        else if (cmpSleepingTime <= -ERROR_DEVIATION) {
                            sleepingTimeImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepingTimeImage.setImageResource(R.drawable.sleeping_time_bad);
                                }
                            });
                            sleepingTimeStatisticsText.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepingTimeStatisticsText.setText("계획보다 훨씬 적은 잠을 잤습니다");
                                }
                            });
                        }
                        else {
                            sleepingTimeImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepingTimeImage.setImageResource(R.drawable.sleeping_time_good);
                                }
                            });
                            sleepingTimeStatisticsText.post(new Runnable() {
                                @Override
                                public void run() {
                                    sleepingTimeStatisticsText.setText("계획했던 시간만큼 잠을 잤습니다.");
                                }
                            });
                        }

                    }
                    else {
                        // SETTINGS 없을 때
                        sleepTimeStatisticsText.post(new Runnable() {
                            @Override
                            public void run() {
                                sleepTimeStatisticsText.setText("목표 취침/기상 시간이 기록되어 있지 않아");
                            }
                        });
                        wakeTimeStatisticsText.post(new Runnable() {
                            @Override
                            public void run() {
                                wakeTimeStatisticsText.setText("평균 시간을 평가할 수 없습니다.");
                            }
                        });
                        sleepingTimeStatisticsText.post(new Runnable() {
                            @Override
                            public void run() {
                                sleepingTimeStatisticsText.setText("목표 수면 시간을 설정해주세요.");
                            }
                        });
                        sleepAnalysisImageLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                sleepAnalysisImageLayout.setVisibility(View.GONE);
                            }
                        });
                    }

                    cursor2.close();
                }
                else {
                    //invisible layouts
                    sleepAnalysisLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            sleepAnalysisLayout.setVisibility(View.GONE);
                        }
                    });
                }

                cursor.close();
                dailyDB.close();
            }
        }).start();
    }
}
