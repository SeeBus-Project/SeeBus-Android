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

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


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
            Log.d("User_????????????","Fine");
        }

        if(permissionCheck2 == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            Log.d("User_????????????","COARSE");
        }

        if(permissionCheck3 == PackageManager.PERMISSION_DENIED){


            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("??????");
            builder.setMessage("?????? ?????? ????????? ?????? ???????????? ??????????????????.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 0);
                    Log.d("User_????????????","BACKGROUND");
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

                    Log.d("User_????????????","Permission ?????? ??????!");
                    final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    Gps.permissionCheck = true;
                    Gps.getGps(mContext);
                    startActivity(intent);
                    Splash.this.finish();
                }
            }, 3000);
        } else {
            Toast toast = Toast.makeText(this,"?????? ?????? ????????? ???????????????.\n????????????????????? ?????????????????? ?????????????????????.",Toast.LENGTH_LONG);
            toast.show();

            Log.d("User_????????????","Permission ??????..");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}