package com.example.helloworld;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;



public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		//PreferenceManager.setDefaultValues(this, R.xml.settings, true);

		ActionBar actionBar = getActionBar();
		actionBar.setIcon(R.drawable.ic_action_settings);
		
	}
	
}
