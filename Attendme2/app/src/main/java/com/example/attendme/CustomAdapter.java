package com.example.attendme;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CustomAdapter extends ArrayAdapter<Student> {
    private ArrayList<Student> dataSet;
    private ArrayList<String> checkedIDs = new ArrayList<>();
    private ArrayList<String> allIDs = new ArrayList<>();
    Context mContext;

    public ArrayList<String> getAllIDs() {
        return allIDs;
    }


    private static class ViewHolder {
        TextView txtName;
        CheckBox checkBox;
    }

    public CustomAdapter(ArrayList<Student> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public Student getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
            viewHolder.txtName = convertView.findViewById(R.id.txtName);
            viewHolder.checkBox = convertView.findViewById(R.id.checkBox);
            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        final Student item = getItem(position);
        viewHolder.txtName.setText(item.getStdName());
        viewHolder.checkBox.setChecked(item.getAssigned());
        viewHolder.checkBox.setTag(position); // Set the position as the tag for the checkbox
        viewHolder.txtName.setTag(position);

        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int checkboxPosition = (int) buttonView.getTag();
                allIDs.add(dataSet.get(checkboxPosition).getStdID());
                if (isChecked) {
                    // Checkbox is checked
                    String checkboxId = String.valueOf(checkboxPosition);
                    String txtID = dataSet.get(checkboxPosition).getStdID();
                    checkedIDs.add(txtID);
                    //System.out.println(checkboxId+"------>"+ txtName);
                } else {
                    // Checkbox is unchecked
                    String checkboxId = String.valueOf(checkboxPosition);
                    String txtID = dataSet.get(checkboxPosition).getStdID();
                    checkedIDs.remove(txtID);
                    //System.out.println(checkboxId+"------>"+ txtName);
                }
            }
        });

        return result;
    }

    public ArrayList<String> getCheckedIDs() {
        return checkedIDs;
    }
}
