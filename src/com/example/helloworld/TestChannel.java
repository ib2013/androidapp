package com.example.helloworld;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.infobip.push.PushNotificationManager;
import com.infobip.push.RegistrationData;

public class TestChannel extends Activity {

	private PushNotificationManager manager;
	Button bSubscribe, bUnsubscribe;
	TextView tvTest1, tvTest2, tvTest3, tvTest4;

	// Campus:
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testchannel);

		bSubscribe = (Button) findViewById(R.id.bSubscribe);
		bUnsubscribe = (Button) findViewById(R.id.bUnsubscribe);

		manager = new PushNotificationManager(getApplicationContext());
		manager.initialize(Conf.senderID, Conf.appID, Conf.appSec);

		bSubscribe.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				/*if (manager.isRegistered()) {
					return;
				}*/
				
				List<String> channels = new ArrayList<String>();
				channels.add("Test channel");
				channels.add("Test channel2");

				// Perform registration
				RegistrationData registrationData = new RegistrationData();
				//registrationData.setUserId("mlmilan");
				registrationData.setChannels(channels);
				manager.register(registrationData);

			}
		});

		bUnsubscribe.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				/*if (!manager.isRegistered()) {
					return;
				}*/
				manager.unregister();
			}
		});

	}

}
