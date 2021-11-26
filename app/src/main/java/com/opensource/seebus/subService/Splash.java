package com.opensource.seebus.subService;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;


public class Splash extends AppCompatActivity {

    Activity activity;
    public static Context mContext;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        activity = Splash.this;
        mContext = getApplicationContext();


        if(!checkPermission()){
            requestSapPermissions();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    private void requestSapPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        if(permissionCheck == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            Log.d("User_위치권한","Fine");
        }

        if(permissionCheck2 == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            Log.d("User_위치권한","COARSE");
        }

        if(permissionCheck3 == PackageManager.PERMISSION_DENIED){


            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("알림");
            builder.setMessage("위치 권한 설정을 항상 허용으로 체크해주세요.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 0);
                    Log.d("User_위치권한","BACKGROUND");
                }
            });

            alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.YELLOW);
                }
            });
            alertDialog.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(checkPermission()) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Log.d("User_위치권한","Permission 모두 완료!");
                    final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    Gps.permissionCheck = true;
                    Gps.getGps(mContext);
                    startActivity(intent);
                    Splash.this.finish();
                }
            }, 3000);
        } else {
            Toast toast = Toast.makeText(this,"위치 권한 설정이 필요합니다.\n어플리케이션을 재실행하거나 재설치해주세요.",Toast.LENGTH_LONG);
            toast.show();

            Log.d("User_위치권한","Permission 실패..");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
/*public class Splash extends AppCompatActivity {


    private boolean onlySelfTestValue;


    private Gps gps;

    public static Context mContext;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceStare) {
        super.onCreate(savedInstanceStare);
        setContentView(R.layout.activity_splash);

        mContext = getApplicationContext();

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

    }


    // permission 갱신
    protected void isPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=PackageManager.PERMISSION_GRANTED) {
            Gps.permissionCheck = true;
        } else {
            Gps.permissionCheck = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        gps = new Gps();

        isPermissionCheck();

        if (Gps.permissionCheck == false) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    200);
        }

        if (Gps.permissionCheck==true) {
            gps.getGps(mContext);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mContext,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            },3000);

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("알림");
            builder.setMessage("위치 권한 설정이 필요합니다.\n어플리케이션을 재실행하거나 재설치해주세요.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton("NO", null);

            alertDialog = builder.create();
            alertDialog.show();

        }


    }
}*/