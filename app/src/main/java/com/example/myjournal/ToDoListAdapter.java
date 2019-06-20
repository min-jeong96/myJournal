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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ViewHolder> {
    private ArrayList<String> mTaskTitle;
    private ArrayList<Integer> mTaskState;
    private Context mContext;
    private String TODAY_DATE;

    dailyDBHelper dailyHelper;
    SQLiteDatabase dailyDB;

    public static final String PREFS_TODO_LIST = "MyToDoList";

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        ImageButton taskButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle       = itemView.findViewById(R.id.task_title);
            taskButton      = itemView.findViewById(R.id.btn_task_item);

            taskButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("TAG", "CLICKED TASK BUTTON");
                    final PopupMenu popupMenu = new PopupMenu(mContext, v);
                    popupMenu.inflate(R.menu.task_state_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int selectedState = 0;
                            switch (item.getItemId()) {
                                case R.id.initialized_task:
                                    selectedState = 0;
                                    break;
                                case R.id.continue_task:
                                    selectedState = 1;
                                    break;
                                case R.id.finished_task:
                                    selectedState = 2;
                                    break;
                                case R.id.delay_task:
                                    selectedState = 3;
                                    break;
                                case R.id.cancel_task:
                                    selectedState = 4;
                                    break;
                            }
                            mTaskState.set(getAdapterPosition(), selectedState);

                            dailyDBHelper dailyHelper = new dailyDBHelper(mContext);
                            SQLiteDatabase dailyDB = dailyHelper.getWritableDatabase();

                            ContentValues values = new ContentValues();

                            for(int i = 0; i < mTaskState.size(); i++) {
                                String Position = "VALUE" + Integer.toString(i);
                                values.put(Position, mTaskState.get(i));

                                Log.d("TAG", "column: " + Position);
                                Log.d("TAG", "value: " + mTaskState.get(i));
                            }

                            Cursor cursor = dailyDB.rawQuery("SELECT * FROM tasks WHERE DATE='" + TODAY_DATE + "';", null);
                            if ((cursor != null) && (cursor.getCount() > 0)) {
                                String[] whereArgs = new String[] {TODAY_DATE};
                                dailyDB.update("tasks", values, "DATE = ?", whereArgs);
                                Log.d("TAG", "DB UPGRADE");
                            }
                            else {
                                values.put("DATE", TODAY_DATE);
                                dailyDB.insert("tasks", null, values);
                                Log.d("TAG", "DB INSERT");
                            }

                            dailyDB.close();

                            switch (item.getItemId()) {
                                case R.id.initialized_task:
                                    taskButton.setImageResource(android.R.color.transparent);
                                    popupMenu.dismiss();
                                    break;
                                case R.id.continue_task:
                                    taskButton.setImageResource(R.drawable.task_continue_arrow);
                                    popupMenu.dismiss();
                                    break;
                                case R.id.finished_task:
                                    taskButton.setImageResource(R.drawable.task_finished_check);
                                    popupMenu.dismiss();
                                    break;
                                case R.id.delay_task:
                                    taskButton.setImageResource(R.drawable.task_delay_arrow);
                                    popupMenu.dismiss();
                                    break;
                                case R.id.cancel_task:
                                    taskButton.setImageResource(R.drawable.task_cancle);
                                    popupMenu.dismiss();
                                    break;
                            }
                            return false;
                        }
                    });

                    popupMenu.show();
                }
            });
        }
    }

    ToDoListAdapter(Context context, ArrayList<String> taskTitle, ArrayList<Integer> taskState, String date) {
        mTaskTitle          = taskTitle;
        mTaskState          = taskState;
        TODAY_DATE          = date;
        mContext            = context;
    }

    @NonNull
    @Override
    public ToDoListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.listitem_task, parent, false);
        ToDoListAdapter.ViewHolder vh = new ToDoListAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.taskTitle.setText(mTaskTitle.get(position));

        switch (mTaskState.get(position)) {
            case 0: // unfinished and non-started task
                viewHolder.taskButton.setImageResource(android.R.color.transparent);
                break;
            case 1: // continue task
                viewHolder.taskButton.setImageResource(R.drawable.task_continue_arrow);
                break;
            case 2: // finished task
                viewHolder.taskButton.setImageResource(R.drawable.task_finished_check);
                break;
            case 3: // delay task
                viewHolder.taskButton.setImageResource(R.drawable.task_delay_arrow);
                break;
            case 4: // canceled task
                viewHolder.taskButton.setImageResource(R.drawable.task_cancle);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mTaskTitle == null)
            return 0;
        else
            return mTaskTitle.size();
    }

}
