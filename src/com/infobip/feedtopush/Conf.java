package com.infobip.feedtopush;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.infobip.push.PushNotificationBuilder;

public class Conf {
	public static final String senderID = "233288288285";
	public static final String appID = "9cabf301d3db";
	public static final String appSec = "e376d84c89be";

	public static final String url_all_channels = "https://pushapi.infobip.com/1/application/9cabf301d3db/channels";
	public static final String headerName = "Authorization";
	public static final String headerValue = "Basic cHVzaGRlbW86cHVzaGRlbW8=";
	
	public static final String filename = "channellist.txt";
	
	static int soundControl = PushNotificationBuilder.ENABLED;
	static int vibrateControl = PushNotificationBuilder.ENABLED;
	static int quietControl = PushNotificationBuilder.ENABLED;
	static String startQuiet = "";
	static String endQuiet = "";
	
	static String sourcenot = "";
	
	static public Context mainActivityContext = null;
}
