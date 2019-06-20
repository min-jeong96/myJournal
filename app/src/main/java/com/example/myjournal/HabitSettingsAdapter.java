package com.example.myjournal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HabitSettingsAdapter extends RecyclerView.Adapter<HabitSettingsAdapter.ViewHolder> {
    private ArrayList<String> mTitle = null;
    private ArrayList<String> mDescription = null;
    private ArrayList<String> mPreferencesNum = null;
    private Context mContext;

    dailyDBHelper dailyHelper;
    SQLiteDatabase dailyDB;

    public static final String PREFS_HABIT_SETTINGS = "MyHabitTracker";

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView habitTitle;
        TextView habitDescription;

        ViewHolder(View itemView) {
            super(itemView);
            habitTitle          = itemView.findViewById(R.id.habit_title_settings);
            habitDescription    = itemView.findViewById(R.id.habit_description_settings);

            itemView.setOnCreateContextMenuListener(this);
        }

        private final MenuItem.OnMenuItemClickListener onMenuItemClickListener
                = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_habit_edit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        View editHabitDialog = LayoutInflater.from(mContext).inflate
                                (R.layout.dialog_edit_habit_settings, null, false);
                        builder.setView(editHabitDialog);

                        Button confirm_edit_habit    = (Button) editHabitDialog.findViewById(R.id.btn_confirm_edit_habit);
                        Button cancel_edit_habit     = (Button) editHabitDialog.findViewById(R.id.btn_cancel_edit_habit);
                        final EditText edit_habit_title          = (EditText) editHabitDialog.findViewById(R.id.edit_habit_title);
                        final EditText edit_habit_description    = (EditText) editHabitDialog.findViewById(R.id.edit_habit_description);

                        edit_habit_title.setText(mTitle.get(getAdapterPosition()));
                        edit_habit_description.setText(mDescription.get(getAdapterPosition()));

                        final AlertDialog dialog = builder.create();
                        confirm_edit_habit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String new_habit_title  = edit_habit_title.getText().toString();
                                String new_habit_desc   = edit_habit_description.getText().toString();

                                SharedPreferences settings = mContext.getSharedPreferences(PREFS_HABIT_SETTINGS, 0);
                                SharedPreferences.Editor editor = settings.edit();

                                String habitTitleReference  = "TITLE" + mPreferencesNum.get(getAdapterPosition());
                                String habitDescReference   = "DESC" + mPreferencesNum.get(getAdapterPosition());
                                editor.putString(habitTitleReference, new_habit_title);

                                if(new_habit_desc.length() > 0) {
                                    editor.putString(habitDescReference, new_habit_desc);
                                } else {
                                    editor.putString(habitDescReference, " ");
                                }
                                editor.commit();

                                mTitle.set(getAdapterPosition(), new_habit_title);
                                mDescription.set(getAdapterPosition(), new_habit_desc);
                                notifyItemChanged(getAdapterPosition());

                                dialog.dismiss();
                            }
                        });
                        cancel_edit_habit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(mContext, "수정이 취소되었습니다",
                                        Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                        break;

                    case R.id.menu_habit_delete:
                        Log.d("TAG", "DELETE the habit");
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                        alertDialogBuilder.setMessage("이 습관의 기록이 함께 삭제됩니다.\n삭제하시겠습니까?");
                        alertDialogBuilder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences settings = mContext.getSharedPreferences(PREFS_HABIT_SETTINGS, 0);
                                SharedPreferences.Editor editor = settings.edit();

                                String habitTitle   = "TITLE" + mPreferencesNum.get(getAdapterPosition());
                                String habitValue   = "VALUE" + mPreferencesNum.get(getAdapterPosition());
                                String habitTitleReference  = "TITLE" + mPreferencesNum.get(getAdapterPosition());
                                String habitDescReference   = "DESC" + mPreferencesNum.get(getAdapterPosition());
                                editor.remove(habitTitleReference);
                                editor.remove(habitDescReference);
                                editor.commit();

                                mTitle.remove(getAdapterPosition());
                                mDescription.remove(getAdapterPosition());
                                mPreferencesNum.remove(getAdapterPosition());
                                notifyDataSetChanged();

                                dailyHelper = new dailyDBHelper(mContext);
                                dailyDB = dailyHelper.getWritableDatabase();
                                dailyHelper.deleteColumn(dailyDB, habitTitle, habitValue);
                                dailyDB.close();

                                Toast.makeText(mContext, "해당 습관과 기록은 삭제되었습니다",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialogBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mContext, "삭제가 취소되었습니다",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        break;
                }
                return false;
            }
        };

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem Edit   = menu.add(Menu.NONE, R.id.menu_habit_edit, 0, "수정");
            MenuItem Delete = menu.add(Menu.NONE, R.id.menu_habit_delete, 1, "삭제");
            Edit.setOnMenuItemClickListener(onMenuItemClickListener);
            Delete.setOnMenuItemClickListener(onMenuItemClickListener);
        }
    }

    HabitSettingsAdapter(Context context, ArrayList<String> titles, ArrayList<String> descriptions,
                         ArrayList<String> preferencesNum) {
        mTitle = titles;
        mDescription = descriptions;
        mPreferencesNum = preferencesNum;
        mContext = context;
    }

    @NonNull
    @Override
    public HabitSettingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.listitem_habit_settings, parent, false);
        HabitSettingsAdapter.ViewHolder vh = new HabitSettingsAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull HabitSettingsAdapter.ViewHolder viewHolder, int position) {
        viewHolder.habitTitle.setText(mTitle.get(position));
        viewHolder.habitDescription.setText(mDescription.get(position));
    }

    @Override
    public int getItemCount() {
        if (mTitle == null)
            return 0;
        else
            return mTitle.size();
    }
}