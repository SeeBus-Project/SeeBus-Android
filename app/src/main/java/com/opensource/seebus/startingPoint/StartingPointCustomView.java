package com.opensource.seebus.startingPoint;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.opensource.seebus.R;

import java.util.ArrayList;

public class StartingPointCustomView extends BaseAdapter {
    LayoutInflater layoutInflater = null;
    private ArrayList<StartingPointListData> listViewData = null;
    private int count = 0;

    public StartingPointCustomView(ArrayList<StartingPointListData> listData)
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
            convertView = layoutInflater.inflate(R.layout.custom_starting_point, parent, false);
        }

        TextView station = convertView.findViewById(R.id.station);
        TextView distAndStationNumber = convertView.findViewById(R.id.distAndStationNumber);

        station.setText(listViewData.get(position).station);
        distAndStationNumber.setText(listViewData.get(position).distAndStationNumberAndNextStationName);

        return convertView;
    }
}