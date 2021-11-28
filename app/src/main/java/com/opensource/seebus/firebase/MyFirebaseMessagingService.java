package com.opensource.seebus.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;

import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
//    private static final String TAG = "MyFirebaseMsgService";

    //푸시 알림 설정
    private String title ="";
    private String body ="";
//    private String color ="teal_700";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //Notification 사용했을때 data 가져오기
        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getColor());
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getIcon());
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getTitle());
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            title=remoteMessage.getNotification().getTitle();
            body=remoteMessage.getNotification().getBody();
        }
        sendNotification(title, body);
    }

    //TODO 푸시알림 heads-up으로 만들기
    //TODO 푸시알림 색 변경
    private void sendNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "1234";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
//                        .setSmallIcon(R.mipmap.ic_firebase)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
//                        .setColor(Integer.parseInt(String.valueOf(R.color.teal_200)))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "1번채널",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}