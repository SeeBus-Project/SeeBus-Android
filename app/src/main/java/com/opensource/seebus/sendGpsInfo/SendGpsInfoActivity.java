package com.opensource.seebus.sendGpsInfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;
import com.opensource.seebus.sendGuideExit.SendGuideExitRequestDto;
import com.opensource.seebus.sendGuideExit.SendGuideExitService;
import com.opensource.seebus.singleton.SingletonRetrofit;
import com.opensource.seebus.singleton.SingletonTimer;

import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SendGpsInfoActivity extends AppCompatActivity {
    private String androidId;

    private TextView tv_Gps;
    private double longitude;
    private double latitude;
    LocationManager lm;

    Button bt_quitSendGpsInfo;

    TextView sendGpsNextStationTextView;
    TextView sendGpsRemainingStationCountTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_gps_info);

        tv_Gps = findViewById(R.id.tv_Gps);
        bt_quitSendGpsInfo = findViewById(R.id.bt_quitSendGpsInfo);

        sendGpsNextStationTextView=findViewById(R.id.sendGpsNextStationTextView);
        sendGpsRemainingStationCountTextView=findViewById(R.id.sendGpsRemainingStationCountTextView);

        // androidId, longitude, latitude 값 할당
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        whenSelfTestValueIsTrue(); // GPS 값 얻어오기

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

    private void whenSelfTestValueIsTrue() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 퍼미션 체크용 (컴파일 하려면 있어야 함.. 의미는 없음)

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //GPS_PROVIDER이 null일때 오류 발생해서 NETWORK_PROVIDER를 사용
        if(location==null) {
            location=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        tv_Gps.setText(
                "위도 : " + latitude + "\n" +
                        "경도 : " + longitude + "\n"
        );

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0,
                0,
                gpsLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0,
                0,
                gpsLocationListener);
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            tv_Gps.setText(
                    "위도 : " + latitude + "\n" +
                            "경도 : " + longitude + "\n"
            );
        }
    };

    private void SendGpsInfo(Retrofit retrofit,Timer timer) {
        SendGpsInfoService sendGpsInfoService = retrofit.create(SendGpsInfoService.class);
        Call<SendGpsInfoResponseDto> call = sendGpsInfoService.requestSendGps(new SendGpsInfoRequestDto(androidId, latitude, longitude));

        call.enqueue(new Callback<SendGpsInfoResponseDto>() {
            @Override
            public void onResponse(Call<SendGpsInfoResponseDto> call, Response<SendGpsInfoResponseDto> response) {
                if (response.isSuccessful()) { // 정상적으로 통신 성공
                    //TODO 다음 정류장 표시해주기
                    SendGpsInfoResponseDto gpsInfo = response.body();
                    if(gpsInfo.isArrived==true) {
                        timer.cancel();
                        SingletonTimer.singletonTimer=new Timer(); //새롭게 생성
                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                        startActivity(mainIntent);
                    }
                    Log.d("TAEWOON",gpsInfo.nextStationName);
                    Log.d("TAEWOON", String.valueOf(gpsInfo.remainingStationCount));
                    sendGpsNextStationTextView.setText("다음정거장 : " + gpsInfo.nextStationName);
                    sendGpsRemainingStationCountTextView.setText("남은정거장 개수 : "+gpsInfo.remainingStationCount);
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