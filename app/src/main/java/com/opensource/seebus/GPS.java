package com.opensource.seebus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class GPS extends AppCompatActivity implements LocationListener {

    private TextView txtResult;
    Location location;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 *1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    double longitude;
    double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        txtResult = (TextView)findViewById(R.id.txtResult);

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( GPS.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }
        else{


            if (location == null) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            } else {

            }

            txtResult.setText(
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

            txtResult.setText(
                    "위도 : " + longitude + "\n" +
                            "경도 : " + latitude + "\n"
            );
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}