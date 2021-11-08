package com.opensource.seebus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.opensource.seebus.startingPoint.StartingPointActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    private TextView textViewGPS;
    private double longitude;
    private double latitude;
    Location location;

    //퍼미션 여부
    boolean locationFindPermission;
    boolean locationCoursePermission;

    Button locationTest;
    Button startingPointButton;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startingPointButton = findViewById(R.id.startingPointButton);
        locationTest = findViewById(R.id.locationTest);

        startingPointButton.setOnClickListener(this);
        locationTest.setOnClickListener(this);

        textViewGPS = findViewById(R.id.textViewGPS);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationFindPermission = true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationCoursePermission = true;
        }

        if (!locationFindPermission || !locationCoursePermission) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    200);
        }
    }

    @Override
    public void onClick(View v) {
        if (locationFindPermission == true && locationCoursePermission == true) {
            final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (location == null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }

                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            if (v == locationTest) {
                textViewGPS.setText(
                        "위도 : " + longitude + "\n" +
                                "경도 : " + latitude + "\n"
                );

                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000,
                        1,
                        gpsLocationListener);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        1000,
                        1,
                        gpsLocationListener);

            } else if (v==startingPointButton) {
                Intent startingPointIntent = new Intent(this, StartingPointActivity.class);
                startingPointIntent.putExtra("longitude", longitude);
                startingPointIntent.putExtra("latitude", latitude);
                startActivity(startingPointIntent);
            }
        } else {
            showToast("위치 권한 설정이 필요합니다.\n어플리케이션을 재실행하거나 재설치해주세요.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 200 && grantResults.length >0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                locationFindPermission = true;
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED)
                locationCoursePermission = true;
        }
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            textViewGPS.setText(
                    "위도 : " + longitude + "\n" +
                            "경도 : " + latitude + "\n"
            );
        }
    };

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    private void showToast(String string) {
        Toast toast = Toast.makeText(this,string,Toast.LENGTH_SHORT);
        toast.show();
    }
}

