package com.opensource.seebus.subService;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import com.opensource.seebus.MainActivity;

import androidx.core.app.ActivityCompat;

public class Gps {

    public static double longitude;
    public static double latitude;
    public static double accuracy;
    public static String gpsKinds;

    public static boolean permissionCheck;

    private static LocationManager lm;

    //Gps 정보 불러와 줌
    public static void getGps(Context context) {
        lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        // 퍼미션 체크용 (컴파일 하려면 있어야 함.. 의미는 없음)

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location==null)
            lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location.getAccuracy()<=25) {
            Gps.longitude = location.getLongitude();
            Gps.latitude = location.getLatitude();
            Gps.accuracy = location.getAccuracy();
            Gps.gpsKinds = location.getProvider();

        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0,
                0,
                gpsLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0,
                0,
                gpsLocationListener);
    }

    static final LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Gps.longitude = location.getLongitude();
            Gps.latitude = location.getLatitude();
            Gps.accuracy = location.getAccuracy();
            Gps.gpsKinds = location.getProvider();

            Log.d("User_GPS","Update");

            //개발용 위도 체크
            ((MainActivity)MainActivity.mContext).textViewGPS.setText(
                    "위도 : " + Gps.latitude + "\n" +
                            "경도 : " + Gps.longitude + "\n" +
                            "Accuracy : " + Gps.accuracy + "\n" +
                            "Provider : " + Gps.gpsKinds + "\n"
            );
        }
    };

}
