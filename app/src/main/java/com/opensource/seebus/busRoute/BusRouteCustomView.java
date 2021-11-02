package com.opensource.seebus.busRoute;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.opensource.seebus.R;

import java.util.ArrayList;

public class BusRouteCustomView extends BaseAdapter {
    LayoutInflater layoutInflater = null;
    private ArrayList<BusRouteListData> listViewData = null;
    private int count = 0;

    public BusRouteCustomView(ArrayList<BusRouteListData> listData) {
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
            convertView = layoutInflater.inflate(R.layout.custom_bus_route, parent, false);
        }

        TextView busRouteStation = convertView.findViewById(R.id.busRouteStation);
        TextView busRouteStationNumber = convertView.findViewById(R.id.busRouteStationNumber);

        busRouteStation.setText(listViewData.get(position).busRouteStation);
        busRouteStationNumber.setText(listViewData.get(position).busRouteStationNumber);

        return convertView;
    }
}