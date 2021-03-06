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
                showToast("?????? ?????? ????????? ???????????????.\n????????????????????? ?????????????????? ?????????????????????.");
            } else if (Gps.longitude != 0.0 && Gps.latitude != 0.0) {
                // ??????????????? ????????? ?????? ????????? ??????????????? ?????????????????? ???????????? ????????? ?????? ?????????.
                goStartingPointActivity();
            } else {
                // ?????????????????? ???????????? ???????????? onCreate()??? if else ?????? ???????????? ???????????? ?????? ????????? ???????????????.
                // ?????? ????????? ????????? ????????? ???????????? ???????????? ??????????????? ?????? ??????????????? ????????? ?????? ????????? ????????????????????????
                // ??????? ??????????????????.
                Gps.getGps(getApplicationContext());
                goStartingPointActivity();
            }
        });

//        textViewGPS.setText(
//                "?????? : " + Gps.latitude + "\n" +
//                        "?????? : " + Gps.longitude + "\n" +
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



        // ??????????????? ?????????
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //????????????
        //?????????????????? ??????
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            showToast("????????? ??????????????? ????????? ???????????????. ????????? ????????????.");
                            return;
                        }

                        // ?????? ????????????
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
            MakeToast.makeToast(getApplicationContext(),"????????? ???????????? ????????????.").show();
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
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // ????????? ???????????? ??????
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // ????????? ???????????? ??????
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

