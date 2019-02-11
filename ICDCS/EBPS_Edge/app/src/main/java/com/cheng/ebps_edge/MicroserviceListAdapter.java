package com.cheng.ebps_edge;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cheng.ebps_edge.Models.Microservice;

import java.util.Collection;
import java.util.List;

public class MicroserviceListAdapter extends ArrayAdapter<Microservice> {

    private int layout;
    private Context mContext;
    public List<Microservice> microserviceList;

    public MicroserviceListAdapter(@NonNull Context context, int resource, @NonNull List<Microservice> microservices) {
        super(context, resource, microservices);
        this.layout = resource;
        this.mContext = context;
        this.microserviceList = microservices;

        Log.d("list size", "" + microserviceList.size());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(layout, null);
        }

        Microservice microservice = microserviceList.get(position);

//        Log.i("Adapter", "" + microserviceList.size());

        if (microservice != null) {
            TextView task = v.findViewById(R.id.task);
            TextView microserviceText = v.findViewById(R.id.microservice);

            if (task != null) {
                task.setText(microservice.taskName);
            }

            if (microserviceText != null) {
                microserviceText.setText(microservice.microserviceName);
            }
        }

        return v;
    }
}