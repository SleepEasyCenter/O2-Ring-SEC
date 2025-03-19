package com.sleepeasycenter.o2ring_app.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lepu.blepro.objs.Bluetooth;
import com.sleepeasycenter.o2ring_app.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class DeviceFileListViewAdapter extends RecyclerView.Adapter<DeviceFileListViewAdapter.ViewHolder> {


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        public String item = null;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.device_file_item_name);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String filename);
    }

    public ArrayList<String> items;
    public OnItemClickListener listener = null;

    public DeviceFileListViewAdapter(ArrayList<String> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_file_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = items.get(position);
        holder.item =item;

        TextView textView = holder.getTextView();
        textView.setText(formatFilename(item));
        textView.setOnClickListener(view -> this.onItemClick(item));
    }

    public void onItemClick(final String filename) {
        if (listener != null) listener.onItemClick(filename);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatFilename(String filename) {
        try {
            // attempt to parse filename
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime dateTime = LocalDateTime.parse(filename, inputFormatter);

            // format parsed filename
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM HH:mm:ss");
            return dateTime.format(outputFormatter);
        } catch (DateTimeParseException e) {
            Log.d("FI", "failed formatting");
            // if parsing fails, return the original filename
            return filename;
        }
    }

}
