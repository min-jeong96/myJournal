package com.example.myjournal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class HabitSettingsAdapter extends RecyclerView.Adapter<HabitSettingsAdapter.ViewHolder> {
    private ArrayList<String> mTitle = null;
    private ArrayList<String> mDescription = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView habitTitle;
        TextView habitDescription;

        ViewHolder(View itemView) {
            super(itemView);

            habitTitle          = itemView.findViewById(R.id.habit_title_settings);
            habitDescription    = itemView.findViewById(R.id.habit_description_settings);
        }
    }

    HabitSettingsAdapter(ArrayList<String> titles, ArrayList<String> descriptions) {
        mTitle = titles;
        mDescription = descriptions;
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