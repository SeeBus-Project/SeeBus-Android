package com.opensource.seebus.history;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;
import com.opensource.seebus.sendGpsInfo.SendGpsInfoActivity;
import com.opensource.seebus.sendRouteInfo.SendRouteInfoRequestDto;
import com.opensource.seebus.sendRouteInfo.SendRouteInfoService;
import com.opensource.seebus.singleton.SingletonRetrofit;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FavoriteRvCustomAdaptor extends RecyclerView.Adapter<FavoriteRvCustomAdaptor.ViewHolder> {
    private ArrayList<HistoryItem> mFavoriteItems;
    private Context mContext;
    private DBHelper mDBHelper;

    // 서버로 보낼 데이터
    private String mAndroidId;
    private String mDestinationArsId;
    private String mDestinationName;
    private String mRtNm;
    private String mStartArsId;

    public FavoriteRvCustomAdaptor(ArrayList<HistoryItem> mFavoriteItems, Context mContext) {
        this.mFavoriteItems = mFavoriteItems;
        this.mContext = mContext;
        mDBHelper = new DBHelper(mContext);
    }

    @NonNull
    @Override
    public FavoriteRvCustomAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 즐겨찾기와 최근기록의 아이템은 동일하게 생겨서 custom_history 그대로 가져옴.
        View holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_history, parent, false);
        return new FavoriteRvCustomAdaptor.ViewHolder(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteRvCustomAdaptor.ViewHolder holder, int position) {
        holder.tv_busNm.setText(mFavoriteItems.get(position).getBusNm() + " 버스");
        holder.tv_departureNm.setText(mFavoriteItems.get(position).getDepartureNm() + " 출발");
        holder.tv_destinationNm.setText(mFavoriteItems.get(position).getDestinationNm() + " 도착");
    }

    @Override
    public int getItemCount() { return mFavoriteItems.size(); }

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
                    HistoryItem favoriteItem = mFavoriteItems.get(currentPosition);

                    String[] strChoiceItems = {"안내시작", "삭제하기", "뒤로가기"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setItems(strChoiceItems, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0) { // 안내시작
                                Intent mainIntent= new Intent(view.getContext(), MainActivity.class);

                                // 출발지
                                mainIntent.putExtra("departure",favoriteItem.getDepartureNm());
                                //도착지
                                mainIntent.putExtra("destination",favoriteItem.getDestinationNm());

                                //EC2 신호전달(TCP)
                                ClientThread thread = new ClientThread();
                                thread.data[0] = "in";
                                thread.data[1] = favoriteItem.getDepartureNm();
                                thread.data[2] = favoriteItem.getDestinationNm();
                                thread.getPort = 5000;
                                thread.start();

                                // 데이터 할당
                                mAndroidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                                mDestinationArsId = favoriteItem.getDestinationNo();
                                mDestinationName = favoriteItem.getDestinationNm();
                                mRtNm = favoriteItem.getBusNm();
                                mStartArsId = favoriteItem.getDepartureNo();

                                // 서버에 데이터 전송
                                sendRouteInfo(SingletonRetrofit.getInstance(mContext.getApplicationContext()));
                            }
                            else if (position == 1) { // 삭제하기
                                // delete table
                                int itemId = favoriteItem.getId();
                                mDBHelper.deleteFavorite(itemId);

                                // delete UI
                                mFavoriteItems.remove(currentPosition);
                                notifyItemRemoved(currentPosition);
                                dialog.dismiss();
                                Toast.makeText(mContext, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                            else if (position == 2) { // 뒤로가기
                                dialog.dismiss();
                            }
                        }
                    });
                    builder.show();
                }
            });
        }
    }

    private void sendRouteInfo(Retrofit retrofit) {
        SendRouteInfoService sendRouteInfoService = retrofit.create(SendRouteInfoService.class);
        Call<Void> call = sendRouteInfoService.requestSendRoute(new SendRouteInfoRequestDto(mAndroidId, mDestinationArsId, mDestinationName, mRtNm, mStartArsId));

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) { // 정상적으로 통신 성공
                    // 확인용 toast
//                    Toast.makeText(mContext.getApplicationContext(), "안내를 시작합니다.", Toast.LENGTH_SHORT).show();

                    // SendGpsInfoActivity로 넘어가기
                    Intent gpsIntent = new Intent(mContext, SendGpsInfoActivity.class);
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                    gpsIntent.putExtra("isReboot","No");
                    mContext.startActivity(gpsIntent);
                } else { // 통신 실패(응답 코드로 판단)
                    // 확인용 toast
                    Toast.makeText(mContext.getApplicationContext(), "현재 서비스하지 않는 경로입니다.", Toast.LENGTH_SHORT).show();

                    // MainActivity로 돌아가기
                    Intent mainIntent = new Intent(mContext, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                    mContext.startActivity(mainIntent);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
//                Log.d("SENDROUTE", t.toString());

                // 확인용 toast
                Toast.makeText(mContext.getApplicationContext(), "데이터를 키고 다시 시도해주세요.", Toast.LENGTH_SHORT).show();

                // MainActivity로 돌아가기
                Intent mainIntent = new Intent(mContext, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                mContext.startActivity(mainIntent);
            }
        });
    }

    // TCP 쓰레드
    class ClientThread extends Thread {
        String data[] = new String[3];
        int getPort;
        @Override
        public void run() {
            String host2 = "183.101.12.31";
            String host = "ec2-3-35-208-56.ap-northeast-2.compute.amazonaws.com";
            try {
                Socket socket = new Socket(host, getPort);

                ObjectOutputStream outstream = new ObjectOutputStream(socket.getOutputStream()); //소켓의 출력 스트림 참조
                outstream.writeObject(data[0]); // 출력 스트림에 데이터 넣기
                outstream.flush(); // 출력

                // 출발정류장 전송
                outstream.writeObject(data[1]); // 출력 스트림에 데이터 넣기
                outstream.flush(); // 출력

                // 도착정류장 전송
                outstream.writeObject(data[2]); // 출력 스트림에 데이터 넣기
                outstream.flush(); // 출력
                //ObjectInputStream instream = new ObjectInputStream(socket.getInputStream());
                //String response = (String)instream.readObject();

                //response = (String)instream.readObject();

                outstream.close();
                //instream.close();
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

