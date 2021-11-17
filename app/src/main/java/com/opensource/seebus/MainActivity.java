package com.opensource.seebus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.opensource.seebus.history.HistoryActivity;
import com.opensource.seebus.sendDeviceInfo.SendDeviceInfoRequestDto;
import com.opensource.seebus.sendDeviceInfo.SendDeviceInfoResponseDto;
import com.opensource.seebus.sendDeviceInfo.SendDeviceInfoService;
import com.opensource.seebus.singletonRetrofit.SingletonRetrofit;
import com.opensource.seebus.startingPoint.StartingPointActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private TextView textViewGPS;
    private double longitude;
    private double latitude;
    private double accuracy;
    LocationManager lm;

    Button startingPointButton;
    Button historyButton;

    private boolean onlySelfTestValue;

    String firebaseToken;
    String androidId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startingPointButton = findViewById(R.id.startingPointButton);
        historyButton=findViewById(R.id.historyButton);
        textViewGPS = findViewById(R.id.textViewGPS);

        onlySelfTestValue = onlySelfTest();

        if (onlySelfTestValue == false) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    200);
        } else {
            //최초실행이 아니고 퍼미션이 트루라면 앱을 켜자마자 현재위치 찾기
            whenSelfTestValueIsTrue();
        }

        startingPointButton.setOnClickListener(view -> {
            if (onlySelfTest() == false) {
                showToast("위치 권한 설정이 필요합니다.\n어플리케이션을 재실행하거나 재설치해주세요.");
            } else if (longitude != 0.0 && latitude != 0.0) {
                // 최초실행이 아닐때 이미 위에서 현재위치를 가져왔으므로 반복해서 위치를 얻지 않는다.
                goStartingPointActivity();
            } else {
                // 최초실행하고 퍼미션이 트루라면 onCreate()의 if else 문이 작동하지 않으므로 해당 함수를 호출해준다.
                // 물론 캐시에 저장된 위치를 사용해서 정확도가 떨어지지만 다시 홈화면으로 돌아올 경우 위치는 갱신되어있으므로
                // 조금? 좋아진거같다.
                whenSelfTestValueIsTrue();
                goStartingPointActivity();
            }
        });

        historyButton.setOnClickListener(view -> {
            Intent historyIntent= new Intent(this, HistoryActivity.class);
            startActivity(historyIntent);
        });

        // 안드로이드 아이디
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        //푸시알림
        //파이어베이스 토큰
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            showToast("토큰을 발급받는데 문제가 생겼습니다. 재설치 해주세요.");
                            return;
                        }

                        // 토큰 가져오기
                        firebaseToken = task.getResult();

                        String firebaseToken_msg = getString(R.string.msg_token_fmt, firebaseToken);
                        String androidId_msg=getString(R.string.android_id,androidId);
                        showToast(firebaseToken_msg+"\n"+androidId_msg);
                        sendDeviceInfo(SingletonRetrofit.getInstance(getApplicationContext()));
                    }
                });
    }
  
    private void goStartingPointActivity() {
        Intent startingPointIntent = new Intent(this, StartingPointActivity.class);
        startingPointIntent.putExtra("longitude", longitude);
        startingPointIntent.putExtra("latitude", latitude);
        startActivity(startingPointIntent);
    }

    private boolean onlySelfTest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
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
        accuracy = location.getAccuracy();
        textViewGPS.setText(
                "위도 : " + longitude + "\n" +
                        "경도 : " + latitude + "\n" +
                        "Accuracy : " + accuracy + "\n"
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            accuracy = location.getAccuracy();
            textViewGPS.setText(
                    "위도 : " + longitude + "\n" +
                            "경도 : " + latitude + "\n" +
                            "Accuracy : " + accuracy + "\n"
            );
        }
    };

    private void sendDeviceInfo(Retrofit retrofit) {
        SendDeviceInfoService sendDeviceInfoService=retrofit.create(SendDeviceInfoService.class);
        Call<SendDeviceInfoResponseDto> call=sendDeviceInfoService.requestSendDevice(new SendDeviceInfoRequestDto(androidId,firebaseToken));

        call.enqueue(new Callback<SendDeviceInfoResponseDto>() {

            @Override
            public void onResponse(Call<SendDeviceInfoResponseDto> call, Response<SendDeviceInfoResponseDto> response) {

            }

            @Override
            public void onFailure(Call<SendDeviceInfoResponseDto> call, Throwable t) {
                Log.d("DEVELOP", t.toString());
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("알림!");
                dialog.setMessage("통신에 실패했습니다.");
                dialog.show();
            }
        });
    }

    private void showToast(String string) {
        Toast toast = Toast.makeText(this,string,Toast.LENGTH_SHORT);
        toast.show();
    }
}

