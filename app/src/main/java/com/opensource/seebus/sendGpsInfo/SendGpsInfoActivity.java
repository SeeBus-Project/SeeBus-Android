package com.opensource.seebus.sendGpsInfo;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;
import com.opensource.seebus.sendGuideExit.SendGuideExitRequestDto;
import com.opensource.seebus.sendGuideExit.SendGuideExitService;
import com.opensource.seebus.singleton.SingletonRetrofit;
import com.opensource.seebus.singleton.SingletonTimer;
import com.opensource.seebus.subService.Gps;

import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.opensource.seebus.subService.Gps.latitude;
import static com.opensource.seebus.subService.Gps.longitude;

public class SendGpsInfoActivity extends AppCompatActivity {
    private String androidId;

    private TextView tv_Gps;

    Button bt_quitSendGpsInfo;

    TextView sendGpsNextStationTextView;
    TextView sendGpsRemainingStationCountTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_gps_info);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tv_Gps = findViewById(R.id.tv_Gps);
        bt_quitSendGpsInfo = findViewById(R.id.bt_quitSendGpsInfo);

        sendGpsNextStationTextView=findViewById(R.id.sendGpsNextStationTextView);
        sendGpsRemainingStationCountTextView=findViewById(R.id.sendGpsRemainingStationCountTextView);

        // androidId, longitude, latitude 값 할당
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Gps.getGps(getApplicationContext());

        tv_Gps.setText(
                "위도 : " + Gps.latitude + "\n" +
                        "경도 : " + Gps.longitude + "\n" +
                        "Accuracy : " + Gps.accuracy + "\n" +
                        "Provider : " + Gps.gpsKinds + "\n"
        );
        // 5초마다 서버에 GpsInfo 보내기
        Intent sendGpsInfoIntent=getIntent();
        String isReboot=sendGpsInfoIntent.getStringExtra("isReboot");
        Timer timer = SingletonTimer.getInstance(getApplicationContext());
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                SendGpsInfo(SingletonRetrofit.getInstance(getApplicationContext()),timer);
            }
        };
        if(isReboot.equals("No")) {
            timer.schedule(timerTask, 0, 5000); // 5초
        } else if(isReboot.equals("Yes")) {
            timer.cancel();
            SingletonTimer.singletonTimer=new Timer();
            Timer newTimer = SingletonTimer.getInstance(getApplicationContext());
            newTimer.schedule(timerTask, 0, 5000);
        }

        // "안내 종료" 버튼 누르면 서버 전송 종료 후 홈화면으로 돌아가기
        bt_quitSendGpsInfo.setOnClickListener(view -> {
            sendGuideExit(SingletonRetrofit.getInstance(getApplicationContext()),SingletonTimer.getInstance(getApplicationContext()));
        });
    }

    private void SendGpsInfo(Retrofit retrofit,Timer timer) {
        SendGpsInfoService sendGpsInfoService = retrofit.create(SendGpsInfoService.class);
        Call<SendGpsInfoResponseDto> call = sendGpsInfoService.requestSendGps(new SendGpsInfoRequestDto(androidId, latitude, longitude));

        call.enqueue(new Callback<SendGpsInfoResponseDto>() {
            @Override
            public void onResponse(Call<SendGpsInfoResponseDto> call, Response<SendGpsInfoResponseDto> response) {
                if (response.isSuccessful()) { // 정상적으로 통신 성공
                    SendGpsInfoResponseDto gpsInfo = response.body();
                    if(gpsInfo.isArrived==true) {
                        timer.cancel();
                        SingletonTimer.singletonTimer=new Timer(); //새롭게 생성
                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                        startActivity(mainIntent);
                    }
                    sendGpsNextStationTextView.setText("다음정거장 : " + gpsInfo.nextStationName);
                    sendGpsRemainingStationCountTextView.setText("남은정거장 개수 : "+gpsInfo.remainingStationCount);
                    tv_Gps.setText(
                            "위도 : " + Gps.latitude + "\n" +
                                    "경도 : " + Gps.longitude + "\n" +
                                    "Accuracy : " + Gps.accuracy + "\n" +
                                    "Provider : " + Gps.gpsKinds + "\n"
                    );
                    Toast.makeText(getApplicationContext(), "성공", Toast.LENGTH_SHORT).show();
                } else { // 통신 실패(응답 코드로 판단)
                    // 운행종료시 400 BAD_REQUEST이면 타이머 종료후 메인으로 이동
                    if(response.code()==400) {
                        timer.cancel();
                        SingletonTimer.singletonTimer=new Timer(); //새롭게 생성
                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                        startActivity(mainIntent);
                        Toast.makeText(getApplicationContext(), "실패(응답 코드)", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<SendGpsInfoResponseDto> call, Throwable t) {
                // 확인용 toast
                Toast.makeText(getApplicationContext(), "데이터를 켜주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendGuideExit(Retrofit retrofit, Timer timer) {
        SendGuideExitService sendGpsInfoService = retrofit.create(SendGuideExitService.class);
        Call<Void> call = sendGpsInfoService.requestSendGps(new SendGuideExitRequestDto(androidId));

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) { // 정상적으로 통신 성공
                    // 확인용 toast - 나중에 삭제 예정
                    Toast.makeText(getApplicationContext(), "성공", Toast.LENGTH_SHORT).show();
                    // 서버 전송 종료
                    timer.cancel(); //타이머객체 없애주고
                    SingletonTimer.singletonTimer=new Timer(); //새롭게 생성
                    //이렇게 하면 timer객체는 단 한번만 생성된다.

                    // 홈화면으로 돌아가기
                    Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                    startActivity(mainIntent);
                } else { // 통신 실패(응답 코드로 판단)
                    // 확인용 toast - 나중에 삭제 예정
                    Toast.makeText(getApplicationContext(), "실패(응답 코드)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 확인용 toast
                Toast.makeText(getApplicationContext(), "데이터를 켜주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}