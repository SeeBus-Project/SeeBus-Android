package com.opensource.seebus.history;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.opensource.seebus.R;

import java.util.ArrayList;

public class HistoryRvCustomAdaptor extends RecyclerView.Adapter<HistoryRvCustomAdaptor.ViewHolder> {
    private ArrayList<HistoryItem> mHistoryItems;
    private Context mContext;
    private DBHelper mDBHelper;

    public HistoryRvCustomAdaptor(ArrayList<HistoryItem> mHistoryItems, Context mContext) {
        this.mHistoryItems = mHistoryItems;
        this.mContext = mContext;
        mDBHelper = new DBHelper(mContext);
    }

    @NonNull
    @Override
    public HistoryRvCustomAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_history, parent, false);
        return new ViewHolder(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryRvCustomAdaptor.ViewHolder holder, int position) {
        holder.tv_busRouteId.setText(mHistoryItems.get(position).getBusRouteId());
        holder.tv_departureNm.setText(mHistoryItems.get(position).getDepartureNm());
        holder.tv_destinationNm.setText(mHistoryItems.get(position).getDestinationNm());
    }

    @Override
    public int getItemCount() {
        return mHistoryItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_busRouteId;
        private TextView tv_departureNm;
        private TextView tv_destinationNm;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_busRouteId = itemView.findViewById((R.id.tv_busRouteId));
            tv_departureNm = itemView.findViewById((R.id.tv_departureNm));
            tv_destinationNm = itemView.findViewById((R.id.tv_destinationNm));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currentPosition = getAdapterPosition(); // 현재 리스트 클릭한 아이템 위치
                    HistoryItem historyItem = mHistoryItems.get(currentPosition);

                    String[] strChoiceItems = {"안내시작", "삭제하기"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setItems(strChoiceItems, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0) { // 안내시작
                                // 안내시작 액티비티로 값 넘기고, ui도 넘어가기
                                dialog.dismiss();
                            }
                            else if (position == 1) { // 삭제하기
                                // delete table
                                int itemId = historyItem.getId();
                                mDBHelper.deleteHistory(itemId);

                                // delete UI
                                mHistoryItems.remove(currentPosition);
                                notifyItemRemoved(currentPosition);
                                dialog.dismiss();
                                Toast.makeText(mContext, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.show();
                }
            });
        }
    }

    // 액티비티에서 호출되는 함수이며, 현재 어댑터에 새로운 히스토리를 전달받아 추가하는 목적이다.
    public void addItem(HistoryItem _item) {
        mHistoryItems.add(0, _item); // 최신 데이터가 가장 위로 오도록
        notifyItemInserted(0);
    }

}
