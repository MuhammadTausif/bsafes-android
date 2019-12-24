package com.pro.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ReadingListAdapter extends ArrayAdapter<ReadingListModel> implements View.OnClickListener {
    private ArrayList<ReadingListModel> dataSet;
    Context mContext;

    public ReadingListAdapter(ArrayList<ReadingListModel> data, Context context) {
        super(context, R.layout.readlist_item, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object obj = getItem(position);
        ReadingListModel dataModel = (ReadingListModel)obj;


    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if(v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.readlist_item, null);

        }

        ImageView iv = (ImageView) v.findViewById(R.id.readlist_item_logo);
        TextView tv1 = (TextView) v.findViewById(R.id.readlist_item_title);
        TextView tv2 = (TextView) v.findViewById(R.id.readlist_item_url);

        tv1.setText(getItem(position).getTitle());
        tv2.setText(getItem(position).getUrl());

        return v;
    }
}
