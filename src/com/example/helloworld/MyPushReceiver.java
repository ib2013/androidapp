package com.example.helloworld;


import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.webkit.WebView.FindListener;
import android.widget.Toast;

import com.infobip.push.AbstractPushReceiver;
import com.infobip.push.PushNotification;
import com.infobip.push.PushNotificationBuilder;



public class MyPushReceiver extends AbstractPushReceiver {
	private Context context;
	@Override
    public void onRegistered(Context context) {
        Toast.makeText(context, "Successfully registered.", Toast.LENGTH_SHORT).show();
       
    }
    @Override
    protected void onRegistrationRefreshed(Context context) {
        Toast.makeText(context, "Registration is refreshed.", Toast.LENGTH_SHORT).show();
    }
    
    NotificationCompat.Builder mBuilder;
    
    @Override
    public void onNotificationReceived(PushNotification notification, Context context) {
    	
    	        mBuilder = new NotificationCompat.Builder(context)
    	                .setSmallIcon(R.drawable.feed2push_notification_icon)
    	                .setContentTitle(notification.getTitle().toString())
    	                .setContentText(notification.getMessage());
    	        
    	        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
    	        notificationIntent.setData(Uri.parse(notification.getUrl().toString()));
    	        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);   
    	        mBuilder.setContentIntent(pendingIntent);
    	        mBuilder.setAutoCancel(true);
    	        
    	        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	        if (Conf.soundControl == PushNotificationBuilder.ENABLED) {
    	         mBuilder.setSound(soundUri);
    	        }
    	        long[] vibraPattern = {0, 500, 250, 500 };
    	        if (Conf.vibrateControl == PushNotificationBuilder.ENABLED) {
    	         mBuilder.setVibrate(vibraPattern);
    	        }
    	             
    	        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	        //possible additional changes -- adding timestamp to getId().toString()
    	        String system = "";
    	        String source = "";
    	        String title = notification.getTitle().toString();
    	        system = "" + title.charAt(0) + title.charAt(1) + title.charAt(2) + title.charAt(3) + title.charAt(4) + title.charAt(5);
    	        source = "" + title.charAt(0) + title.charAt(1) + title.charAt(2);
    	        if (!system.equals("SYSTEM")) {
    	         if (source.equals("YTB")) {
    	          mBuilder.setSmallIcon(R.drawable.ytb);
    	         } else if (source.equals("TPB")) {
    	          mBuilder.setSmallIcon(R.drawable.tpb);
    	         }
    	        // mBuilder.setContentTitle(system);
    	         Date d = new Date();
    	         //long t = d.getTime();
    	            mNotificationManager.notify(notification.getId().toString()+d.getTime(), notification.getNotificationId(), mBuilder.build());
    	        } else {
    	        	//TODO provjeriti ovo!!!
    	        	if (Conf.mainActivityContext != null) {
    	        		((MainActivity) Conf.mainActivityContext).RefreshChannelList();
    	        	}
    	        }

    }
    @Override
    protected void onNotificationOpened(PushNotification notification, Context context) {
        //Toast.makeText(context, "Notification opened.", Toast.LENGTH_LONG).show();
        //otvaranje URL linka ako postoji u notifikaciji
    	if (notification.getMimeType().equals("text/html")) {
    		   if (notification.getUrl() != null) {
    		    Intent notificationInt = new Intent(Intent.ACTION_VIEW);
    		    notificationInt.setData(Uri.parse(notification.getUrl()));
    		    notificationInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		    context.startActivity(notificationInt);
    		   } else
    		    return;
    		  } else
    		   return;
        
    }
    @Override
    public void onUnregistered(Context context) {
        Toast.makeText(context, "Successfully unregistered.", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onError(int reason, Context context) {
        Toast.makeText(context, "Error occurred." + reason, Toast.LENGTH_SHORT).show();
	}

}
