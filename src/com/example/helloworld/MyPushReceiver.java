package com.example.helloworld;


import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import com.infobip.push.AbstractPushReceiver;
import com.infobip.push.PushNotification;

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
    @Override
    public void onNotificationReceived(PushNotification notification, Context context) {
    	//Toast.makeText(context, "Received notification: " + notification.toString(), 
          //   Toast.LENGTH_SHORT).show();
    	//podesavanje ringtonea za notifikaciju
    	//if (Conf.soundControl) 
    	/*try {
            Uri not = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, not);
            r.play();
        } catch (Exception e) {}*/
    	Toast.makeText(context, "" + notification.getId(), Toast.LENGTH_SHORT).show();
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
