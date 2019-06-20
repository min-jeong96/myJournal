package com.example.myjournal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class HabitTrackerAdapter extends RecyclerView.Adapter<HabitTrackerAdapter.ViewHolder> {
    private ArrayList<String> mTitle = null;
    private ArrayList<Integer> mDone = null;
    private ArrayList<String> mColumnDB = null;
    private String TODAY_DATE;
    private Context mContext;

    dailyDBHelper dailyHelper;
    SQLiteDatabase dailyDB;

    public static final String PREFS_HABIT_TRACKER= "MyHabitTracker";

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView habitTitle;
        Switch habitSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            habitTitle    = itemView.findViewById(R.id.habit_title);
            habitSwitch   = itemView.findViewById(R.id.btn_habit_tracker);

            habitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(habitSwitch.isChecked())
                        mDone.set(getAdapterPosition(), 1);
                    else
                        mDone.set(getAdapterPosition(), 0);

                    dailyHelper = new dailyDBHelper(HabitTrackerAdapter.this.mContext);
                    dailyDB = dailyHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();

                    for(int i = 0; i < mColumnDB.size(); i++) {
                        String Position = mColumnDB.get(i);
                        values.put(Position, mDone.get(i));

                        Log.d("TAG", "column: " + Position);
                        Log.d("TAG", "value: " + mDone.get(i));
                    }

                    Cursor cursor = dailyDB.rawQuery("SELECT * FROM habit WHERE DATE='" + TODAY_DATE + "';", null);
                    if ((cursor != null) && (cursor.getCount() > 0)) {
                        String[] whereArgs = new String[] {TODAY_DATE};
                        dailyDB.update("habit", values, "DATE = ?", whereArgs);
                        Log.d("TAG", "DB UPGRADE");
                    }
                    else {
                        values.put("DATE", TODAY_DATE);
                        dailyDB.insert("habit", null, values);
                        Log.d("TAG", "DB INSERT");
                    }

                    dailyDB.close();
                }
            });
        }
    }

    HabitTrackerAdapter(Context context, ArrayList<String> titles, ArrayList<String> columnDB, ArrayList<Integer> done) {
        mTitle = titles;
        mDone = done;
        mContext = context;
        mColumnDB = columnDB;
    }

    HabitTrackerAdapter(Context context, ArrayList<String> titles, ArrayList<String> columnDB, ArrayList<Integer> done, String DATE) {
        mTitle = titles;
        mDone = done;
        mContext = context;
        mColumnDB = columnDB;
        TODAY_DATE = DATE;
    }

    @NonNull
    @Override
    public HabitTrackerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.listitem_habit_tracker, parent, false);
        HabitTrackerAdapter.ViewHolder vh = new HabitTrackerAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.habitTitle.setText(mTitle.get(position));

        if (mDone.get(position) == 0)
            viewHolder.habitSwitch.setChecked(false);
        else
            viewHolder.habitSwitch.setChecked(true);
    }

    @Override
    public int getItemCount() {
        if (mTitle == null)
            return 0;
        else
            return mTitle.size();
    }
}
