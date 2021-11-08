package com.opensource.seebus.history;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.opensource.seebus.MainActivity;
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
        holder.tv_busNm.setText(mHistoryItems.get(position).getBusNm() + "번 버스");
        holder.tv_departureNm.setText(mHistoryItems.get(position).getDepartureNm() + " 출발");
        holder.tv_destinationNm.setText(mHistoryItems.get(position).getDestinationNm() + " 도착");
    }

    @Override
    public int getItemCount() {
        return mHistoryItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_busNm;
        private TextView tv_departureNm;
        private TextView tv_destinationNm;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_busNm = itemView.findViewById((R.id.tv_busNm));
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
                                //dialog.dismiss();

                                Intent mainIntent= new Intent(view.getContext(), MainActivity.class);

                                // 출발지
                                mainIntent.putExtra("departure",historyItem.getDepartureNm());
                                //도착지
                                mainIntent.putExtra("destination",historyItem.getDestinationNm());

                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                                mContext.startActivity(mainIntent);

                                Toast.makeText(mContext, "안내를 시작합니다.", Toast.LENGTH_SHORT).show();
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
