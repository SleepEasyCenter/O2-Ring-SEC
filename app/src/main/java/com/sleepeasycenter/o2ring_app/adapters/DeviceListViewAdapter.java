package com.sleepeasycenter.o2ring_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sleepeasycenter.o2ring_app.R;
import com.lepu.blepro.objs.Bluetooth;

import java.util.ArrayList;

public class DeviceListViewAdapter extends RecyclerView.Adapter<DeviceListViewAdapter.ViewHolder> {


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        public Bluetooth device = null;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.device_item_name);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DeviceListViewAdapter.ViewHolder item);
    }

    private ArrayList<Bluetooth> devices_list;
    public OnItemClickListener listener = null;

    public DeviceListViewAdapter(ArrayList<Bluetooth> devicesList) {
        devices_list = devicesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_item, parent, false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bluetooth device = devices_list.get(position);
        holder.device=device;
        TextView textView = holder.getTextView();
        textView.setText(device.getName() + " | " + device.getMacAddr());
        textView.setOnClickListener(view -> this.onItemClick(holder));
    }

    public void onItemClick(final DeviceListViewAdapter.ViewHolder item) {
        if (listener != null) listener.onItemClick(item);
    }

    @Override
    public int getItemCount() {
        return devices_list.size();
    }


}
