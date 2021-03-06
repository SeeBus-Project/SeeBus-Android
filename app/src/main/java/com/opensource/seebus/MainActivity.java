package com.opensource.seebus;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.opensource.seebus.dialog.CustomDialogClickListener;
import com.opensource.seebus.dialog.NetworkDialog;
import com.opensource.seebus.help.HelpActivity;
import com.opensource.seebus.history.FavoriteActivity;
import com.opensource.seebus.history.HistoryActivity;
import com.opensource.seebus.sendDeviceInfo.SendDeviceInfoRequestDto;
import com.opensource.seebus.sendDeviceInfo.SendDeviceInfoResponseDto;
import com.opensource.seebus.sendDeviceInfo.SendDeviceInfoService;
import com.opensource.seebus.sendGpsInfo.SendGpsInfoActivity;
import com.opensource.seebus.singleton.SingletonRetrofit;
import com.opensource.seebus.startingPoint.StartingPointActivity;
import com.opensource.seebus.subService.Gps;
import com.opensource.seebus.util.MakeToast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

//    public TextView textViewGPS;

    Button startingPointButton;
    Button historyButton;
    Button favoriteButton;

    String firebaseToken;
    String androidId;

    public static Context mContext;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id== R.id.actionHelpText) {
            Intent helpIntent = new Intent(this, HelpActivity.class);
            startActivity(helpIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startingPointButton = findViewById(R.id.startingPointButton);
        historyButton=findViewById(R.id.historyButton);
        favoriteButton=findViewById(R.id.favoriteButton);
//        textViewGPS = findViewById(R.id.textViewGPS);

        mContext = this;

        startingPointButton.setOnClickListener(view -> {
            if (Gps.permissionCheck == false) {
                showToast("위치 권한 설정이 필요합니다.\n어플리케이션을 재실행하거나 재설치해주세요.");
            } else if (Gps.longitude != 0.0 && Gps.latitude != 0.0) {
                // 최초실행이 아닐때 이미 위에서 현재위치를 가져왔으므로 반복해서 위치를 얻지 않는다.
                goStartingPointActivity();
            } else {
                // 최초실행하고 퍼미션이 트루라면 onCreate()의 if else 문이 작동하지 않으므로 해당 함수를 호출해준다.
                // 물론 캐시에 저장된 위치를 사용해서 정확도가 떨어지지만 다시 홈화면으로 돌아올 경우 위치는 갱신되어있으므로
                // 조금? 좋아진거같다.
                Gps.getGps(getApplicationContext());
                goStartingPointActivity();
            }
        });

//        textViewGPS.setText(
//                "위도 : " + Gps.latitude + "\n" +
//                        "경도 : " + Gps.longitude + "\n" +
//                        "Accuracy : " + Gps.accuracy + "\n" +
//                        "Provider : " + Gps.gpsKinds + "\n"
//        );

        historyButton.setOnClickListener(view -> {
            Intent historyIntent= new Intent(this, HistoryActivity.class);
            startActivity(historyIntent);
        });

        favoriteButton.setOnClickListener(view -> {
            Intent favoriteIntent= new Intent(this, FavoriteActivity.class);
            startActivity(favoriteIntent);
        });



        // 안드로이드 아이디
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
//                        showToast(firebaseToken_msg+"\n"+androidId_msg);
                        sendDeviceInfo(SingletonRetrofit.getInstance(getApplicationContext()));
                    }
                });
    }

    private void goStartingPointActivity() {
        if(Gps.longitude!=0.0&&Gps.latitude!=0.0) {
            Intent startingPointIntent = new Intent(this, StartingPointActivity.class);
            startActivity(startingPointIntent);
        } else {
            MakeToast.makeToast(getApplicationContext(),"위치를 받아오는 중입니다.").show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void sendDeviceInfo(Retrofit retrofit) {
        SendDeviceInfoService sendDeviceInfoService=retrofit.create(SendDeviceInfoService.class);
        Call<SendDeviceInfoResponseDto> call=sendDeviceInfoService.requestSendDevice(new SendDeviceInfoRequestDto(androidId,firebaseToken));

        call.enqueue(new Callback<SendDeviceInfoResponseDto>() {

            @Override
            public void onResponse(Call<SendDeviceInfoResponseDto> call, Response<SendDeviceInfoResponseDto> response) {
                SendDeviceInfoResponseDto device = response.body();
                if(device.isArrived==false) {
                    Intent gpsIntent = new Intent(getApplicationContext(), SendGpsInfoActivity.class);
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                    gpsIntent.putExtra("isReboot","Yes");
                    startActivity(gpsIntent);
                }
            }

            @Override
            public void onFailure(Call<SendDeviceInfoResponseDto> call, Throwable t) {
                Log.d("DEVELOP", t.toString());
                NetworkDialog networkDialog=new NetworkDialog(MainActivity.this, new CustomDialogClickListener() {
                    @Override
                    public void onPositiveClick() {
                        sendDeviceInfo(SingletonRetrofit.getInstance(getApplicationContext()));
                    }

                    @Override
                    public void onNegativeClick() {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
                networkDialog.setCanceledOnTouchOutside(false);
                networkDialog.setCancelable(false);
                networkDialog.show();
            }
        });
    }

    private void showToast(String string) {
        Toast toast = Toast.makeText(this,string,Toast.LENGTH_SHORT);
        toast.show();
    }
}

