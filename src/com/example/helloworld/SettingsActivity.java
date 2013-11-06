package com.example.helloworld;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

public class SettingsActivity extends PreferenceActivity{

	SwitchPreference sound;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		PreferenceManager.setDefaultValues(this, R.xml.settings, true);
		
		sound = (SwitchPreference) findPreference("soundUpdates");
	}

}
