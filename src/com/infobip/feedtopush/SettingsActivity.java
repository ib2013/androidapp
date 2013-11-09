package com.infobip.feedtopush;

import com.example.helloworld.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;



public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		//PreferenceManager.setDefaultValues(this, R.xml.settings, true);
	}

}
