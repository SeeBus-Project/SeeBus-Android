package com.opensource.seebus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.opensource.seebus.startingPoint.StartingPointActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView textViewGPS;
    private double longitude;
    private double latitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startingPointButton=findViewById(R.id.startingPointButton);
        startingPointButton.setOnClickListener(view -> {
            Intent startingPointIntent= new Intent(this, StartingPointActivity.class);
            startingPointIntent.putExtra("longitude",longitude);
            startingPointIntent.putExtra("latitude",latitude);
            startActivity(startingPointIntent);
        });


        //TODO GPS최초실행시 동의 누르고 반영이 바로 안되는 현상
        textViewGPS=findViewById(R.id.textViewGPS);
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }
        else{
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();

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
}