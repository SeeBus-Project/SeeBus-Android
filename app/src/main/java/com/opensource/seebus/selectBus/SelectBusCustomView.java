package com.opensource.seebus.selectBus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.opensource.seebus.R;

import java.util.ArrayList;

public class SelectBusCustomView extends BaseAdapter {
    LayoutInflater layoutInflater = null;
    private ArrayList<SelectBusListData> listViewData = null;
    private int count = 0;

    public SelectBusCustomView(ArrayList<SelectBusListData> listData)
    {
        listViewData = listData;
        count = listViewData.size();
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (layoutInflater == null)
            {
                layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = layoutInflater.inflate(R.layout.custom_select_bus, parent, false);
        }

        TextView busNumber = convertView.findViewById(R.id.busNumber);
        TextView nextStation = convertView.findViewById(R.id.nextStation);
        TextView endStation = convertView.findViewById(R.id.endStation);
        TextView message1 = convertView.findViewById(R.id.message1);
        TextView message2 = convertView.findViewById(R.id.message2);

        busNumber.setText(listViewData.get(position).busNumber);
        nextStation.setText(listViewData.get(position).nextStation);
        endStation.setText(listViewData.get(position).endStation);
        message1.setText(listViewData.get(position).message1);
        message2.setText(listViewData.get(position).message2);

        return convertView;
    }
}
