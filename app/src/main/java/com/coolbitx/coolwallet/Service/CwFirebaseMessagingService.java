package com.coolbitx.coolwallet.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.ui.Fragment.FragMainActivity;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.snscity.egdwlib.utils.LogUtil;

import io.fabric.sdk.android.Fabric;

public class CwFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        LogUtil.e("Notification=" + remoteMessage.getData());
        String ExchangeMessage="";
        String ExchangeData="";
        try {

            ExchangeMessage = remoteMessage.getNotification().getBody();
            ExchangeData = remoteMessage.getData().toString();
            LogUtil.d("From: " + remoteMessage.getFrom() + ";Notification Message Body= " + ExchangeMessage + "\n" + "data= " + ExchangeData);

        } catch (Exception e) {

            if(Fabric.isInitialized()){
                Crashlytics.log(e.toString());
            }
            return;
        }

        final Intent intent = new Intent(BTConfig.XCHS_NOTIFICATION);
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        intent.putExtra("ExchangeMessage", ExchangeMessage);
        intent.putExtra("ExchangeData", ExchangeData);
        broadcastManager.sendBroadcast(intent);

        sendNotification(ExchangeMessage);
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        LogUtil.d("sendNotification=" + messageBody);
        Intent intent = new Intent(this, FragMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_cw)
//                .setContentTitle("CoolWallet Exchange Message")
//                .setContentText(messageBody)
                .setStyle(new Notification.InboxStyle()
                        .setBigContentTitle("CoolWallet Exchange Message")
                        .addLine(messageBody.substring(0, messageBody.indexOf(".") + 1))
                        .addLine(messageBody.substring(messageBody.indexOf(".") + 1)))
//                        .addLine(messageBody.substring(messageBody.indexOf(".") + 1, messageBody.indexOf(",") + 1))
//                        .addLine(messageBody.substring(messageBody.indexOf(",") + 1, messageBody.lastIndexOf(",") + 1))
//                        .addLine(messageBody.substring(messageBody.lastIndexOf(",") + 1, messageBody.lastIndexOf(".") + 1)))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
    }

}
