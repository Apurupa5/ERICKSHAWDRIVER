package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by NB VENKATESHWARULU on 11/17/2016.
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
         String email=remoteMessage.getData().get("user");
        String pickup=remoteMessage.getData().get("pickup");
        String dest=remoteMessage.getData().get("destination");

        showNotification(remoteMessage.getData().get("message"),email,pickup,dest);


    }

  private void showNotification(String message,String email,String pickup,String dest) {

        Intent i = new Intent(this, NotificationActivity.class);
        i.putExtra("message",message);
        i.putExtra("user",email);
        i.putExtra("pickup",pickup);
        i.putExtra("destination",dest);

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("REQUEST")
                .setContentText(message)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());
    }
}
