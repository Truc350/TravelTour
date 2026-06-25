package com.example.myapplication.service;

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        // Save token locally
        getSharedPreferences("UserSession", MODE_PRIVATE)
                .edit()
                .putString("fcm_token", token)
                .apply();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = null;
        String body = null;
        String action = null;
        String voucherCode = null;

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            action = remoteMessage.getData().get("action");
            voucherCode = remoteMessage.getData().get("voucher_code");
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("message");
        }

        // Fallback to notification payload
        if (remoteMessage.getNotification() != null) {
            if (title == null) title = remoteMessage.getNotification().getTitle();
            if (body == null) body = remoteMessage.getNotification().getBody();
        }

        if (body != null) {
            sendNotification(title, body, action, voucherCode);
        }
    }

    private void sendNotification(String messageTitle, String messageBody, String action, String voucherCode) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (action != null) {
            intent.putExtra("action", action);
        }
        if (voucherCode != null) {
            intent.putExtra("voucher_code", voucherCode);
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(messageTitle != null ? messageTitle : "Tour Notification")
                        .setContentText(messageBody)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}
