package com.example.helloworld;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.infobip.push.ChannelObtainListener;
import com.infobip.push.ChannelRegistrationListener;
import com.infobip.push.PushNotificationBuilder;
import com.infobip.push.PushNotificationManager;
import com.infobip.push.lib.util.Util;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	
	MyCustomAdapter dataAdapter = null; // data adapter za popunjavanje ListViewa
	private PushNotificationManager manager;// infobip manager
	final ArrayList<ChannelItem> channelList = new ArrayList<ChannelItem>(); // lista kanala za prikaz u listView
	ArrayList<String> channels; // lista procitanih kanala
	CheckBox checkBoxSelectAll; // cb za selekciju svih
	private ProgressDialog pDialog; // progressdialog za ucitavanje liste kanala
	static PushNotificationBuilder builder;
	String filterZaListuKanala = ""; //mjenja se u "searchu" u sklopu listenera definiranog u onCreateOptionsMenu metodi
								//koristi se pri prikazivanju liste kanala

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// PreferenceManager.setDefaultValues(this, R.xml.settings, true);
		// inicijalizira stvari za Push Notification:
		manager = new PushNotificationManager(getApplicationContext());
		manager.initialize(Conf.senderID, Conf.appID, Conf.appSec);

		if (!manager.isRegistered()) {
			manager.register();
		}
	/*	try{
			channels = readChannelListFromFile();
			if (channels == null) {
				new LoadAllChannels().execute();				
			} else {
				addItemsOnListView();
			}
		}catch(Exception e) {
			new LoadAllChannels().execute();
		} */
		new LoadAllChannels().execute();
		
		// Subscribe button click:
		final Button subscribeButton = (Button) findViewById(R.id.button1);
		subscribeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String toastText = "";
				List<String> channels = new ArrayList<String>();
				for (ChannelItem chnl : channelList) {
					if (chnl.getSelected()) {
						channels.add(chnl.getName());
						toastText += chnl.getName();
						toastText += '\n';
					}
				}
				ChannelRegistrationListener channelRegistrationListener = null;
				manager.registerToChannels(channels, true,
						channelRegistrationListener);
				Toast.makeText(getApplicationContext(),
						"SUBSCRIBED ON: \n" + toastText, Toast.LENGTH_LONG)
						.show();
			}
		});

		// cb listener za selekciju svih kanala u listi
		checkBoxSelectAll = (CheckBox) findViewById(R.id.checkBoxSelectAll);
		checkBoxSelectAll
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							for (ChannelItem channelItem : channelList)
								channelItem.setSelected(true);
							// Generate list View from ArrayList
							displayListView(channelList);
						}
					}
				});

		//Util.setDebugModeEnabled(false);
		// Uljepsavanje notificationa
		// PushNotificationBuilder builder;
		notificationConfig();
	}

	protected void customizeNotificationParams() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean soundToggle = pref.getBoolean("soundUpdates", true);
		boolean vibrateToggle = pref.getBoolean("vibrateUpdates", true);
		boolean quietToggle = pref.getBoolean("quietUpdates", false);
		if (soundToggle) {
			Conf.soundControl = PushNotificationBuilder.ENABLED;
		} else {
			Conf.soundControl = PushNotificationBuilder.DISABLED;
		}
		if (vibrateToggle) {
			Conf.vibrateControl = PushNotificationBuilder.ENABLED;
		} else {
			Conf.vibrateControl = PushNotificationBuilder.DISABLED;
		}
		if (quietToggle) {
			Conf.quietControl = PushNotificationBuilder.ENABLED;
			String startTime = pref.getString("start", "");
			String stopTime = pref.getString("stop", "");

			int starthour, startminute;
			int stophour, stopminute;
			try {
				String sh = "" + startTime.charAt(0) + startTime.charAt(1);
				String sm = "" + startTime.charAt(3) + startTime.charAt(4);
				starthour = Integer.valueOf(sh);
				startminute = Integer.valueOf(sm);

				sh = "" + stopTime.charAt(0) + stopTime.charAt(1);
				sm = "" + stopTime.charAt(3) + stopTime.charAt(4);
				stophour = Integer.valueOf(sh);
				stopminute = Integer.valueOf(sm);
				builder.setQuietTimeEnabled(true);
				builder.setQuietTime(starthour, startminute, stophour,
						stopminute);
				Toast.makeText(
						this,
						"Start time: " + starthour + ":" + startminute
								+ Conf.endQuiet + "\nStop time: " + stophour
								+ ":" + stopminute, Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(this,"Error setting quiet mode. Bad time format.",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Conf.quietControl = PushNotificationBuilder.DISABLED;
			Conf.startQuiet = "";
			Conf.endQuiet = "";
			builder.setQuietTimeEnabled(false);
		}
	}
	
	
	public void writeChannelListToFile(ArrayList<String> data) {
	   try {
	        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config.txt", Context.MODE_PRIVATE));
	        for (String str : data) {
	        	outputStreamWriter.write(str+",");	        	
	        }
	        outputStreamWriter.close();
	    }
	    catch (IOException e) {
	        Log.e("Exception", "File write failed: " + e.toString());
	    }
	}
	
	
	/* Ne koristi se:
	//funkcija za  
	 
	private ArrayList<String> readChannelListFromFile() {
		 String ret = "";
	    try {
		        InputStream inputStream = openFileInput("config.txt");
		        if ( inputStream != null ) {
		            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		            String receiveString = "";
		            StringBuilder stringBuilder = new StringBuilder();
		            while ( (receiveString = bufferedReader.readLine()) != null ) {
		                stringBuilder.append(receiveString);
		            }
		            inputStream.close();
		            ret = stringBuilder.toString();
		        }		        
		    }
		    catch (FileNotFoundException e) {
		        Log.e("login activity", "File not found: " + e.toString());
		    } catch (IOException e) {
		        Log.e("login activity", "Can not read file: " + e.toString());
		    }
		    ArrayList<String> chanels = null;
		    
		    if (ret.equals("")) {
		    	return chanels;
		    }
		    else {
		    	chanels = new ArrayList<String>();
		    	String chlrow = "";
			    for (int i=0; i<ret.length(); i++) {
			    	if (ret.charAt(i) == ',') {
			    		chanels.add(chlrow);		    		
			    		chlrow = "";			    		
			    	} else {
			    		chlrow += ret.charAt(i);			    		
			    	}
			    }
			    return chanels;
		    }			    
	}
	*/


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		notificationConfig();
	}
	
	void notificationConfig() {
		builder = new PushNotificationBuilder(getApplicationContext());
		customizeNotificationParams();
		builder.setIconDrawableId(R.drawable.feed2push_white_icon);
		builder.setSound(Conf.soundControl);
		builder.setVibration(Conf.vibrateControl);
	}
	
	

	// metoda za prikazivanje liste:
	private void displayListView(ArrayList<ChannelItem> channelList) {
		// kreiraj ArrayAdaptar iz String Array		
		dataAdapter = new MyCustomAdapter(this, R.layout.list_channel, channelList);
		ListView listView = (ListView) findViewById(R.id.listView1);
		// dodeli adapter u ListView
		listView.setAdapter(dataAdapter);
	}

	// klasa za definisanje naseg custom adaptera
	private class MyCustomAdapter extends ArrayAdapter<ChannelItem> {
		// lista kanala
		private ArrayList<ChannelItem> channelList;

		public MyCustomAdapter(Context context, int textViewResourceId,	ArrayList<ChannelItem> countryList) {
			super(context, textViewResourceId, countryList);
			this.channelList = new ArrayList<ChannelItem>();
			this.channelList.addAll(countryList);
		}

		// klasa za jedan red liste kanala
		private class ViewHolder {
			TextView channelName;
			CheckBox checkbox;
		}

		// popuni pojedinacne retke u listViewu
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			Log.v("ConvertView", String.valueOf(position));

			// napravi view za list_channel
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_channel, null);

				holder = new ViewHolder();
				holder.channelName = (TextView) convertView.findViewById(R.id.textView1);
				holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkBox1);
				convertView.setTag(holder);

				// napravi listener za checkpoint
				holder.checkbox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						ChannelItem channelItem = (ChannelItem) cb.getTag();
						channelItem.setSelected(cb.isChecked());
						if (!cb.isChecked())
							// ako se checkpoint u listi deselectira onda se
							// deselectira i checkBoxSelectAll
							checkBoxSelectAll.setChecked(false);
					}
				});
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// doda konkretne podatke u novi contentView
			ChannelItem channelItem = channelList.get(position);
			holder.channelName.setText(channelItem.getName());
			holder.checkbox.setChecked(channelItem.getSelected());
			holder.checkbox.setTag(channelItem);
			holder.checkbox.setChecked(channelItem.getSelected());
			holder.checkbox.setTag(channelItem);
			return convertView;
		}

	}

	// metoda za generisanje menija - REFRESH dugme
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		
		//"search" (zapravo samo filtrira listu kanala):
		
		MenuItem searchItem = menu.findItem(R.id.action_search);
	    SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
	    
	    searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			public boolean onQueryTextSubmit(String arg0) {
				return false;
			}
			
			public boolean onQueryTextChange(String arg0) {
				filterZaListuKanala = arg0;
				addItemsOnListView();
				displayListView(channelList);
				return false;
			}
		});
	    
	    
	    // When using the support library, the setOnActionExpandListener() method is
	    // static and accepts the MenuItem object as an argument
	    MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
		
	    	@Override
	    	public boolean onMenuItemActionExpand(MenuItem arg0) {
	    		findViewById(R.id.button1).setVisibility(View.GONE);
	    		findViewById(R.id.layoutSaSellectAllCheckboxom).setVisibility(View.GONE);
	    		return true;
	    	}
		
	    	@Override
	    	public boolean onMenuItemActionCollapse(MenuItem arg0) {
	    		filterZaListuKanala = "";
	    		addItemsOnListView();
	    		displayListView(channelList);
	    		findViewById(R.id.button1).setVisibility(View.VISIBLE);
	    		findViewById(R.id.layoutSaSellectAllCheckboxom).setVisibility(View.VISIBLE);
	    		return true;
	    	}
	    });
	    
	    /*
	    searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				filterZaListuKanala = "";
				addItemsOnListView();
				displayListView(channelList);
				findViewById(R.id.button1).setVisibility(View.VISIBLE);
				findViewById(R.id.layoutSaSellectAllCheckboxom).setVisibility(View.VISIBLE);
			}
	    	
	    }); 
	    */
		
	    
		return true;
	}

	// metoda za izbor selektovanog dugmeta - REFRESH dugme
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.refresh:
			new LoadAllChannels().execute();
			//writeChannelListToFile(channels);
			break;
		case R.id.settings:
			Intent i = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(i);
			break;
		}
		return false;
	}

	// metoda koja updatuje UI na osnovu ucitane liste kanala
	private void addItemsOnListView() {
		// Toast.makeText(this, "USPESNO POKUPLJENI KANALI." + channels,
		// Toast.LENGTH_LONG).show();
		channelList.clear();
		for (String str : channels)	
			if (str.toLowerCase().contains(filterZaListuKanala.toLowerCase()))
				channelList.add(new ChannelItem(str, false));

		// provjera koji su channeli vec subscribani i cekiraj ih:
		manager.getRegisteredChannels(new ChannelObtainListener() {

			@Override
			public void onChannelsObtained(String[] channels) {
				for (ChannelItem channelItem : channelList)
					for (String str : channels)
						if (channelItem.getName().equals(str)) channelItem.setSelected(true);
				// Generate list View from ArrayList
				displayListView(channelList);
			}

			@Override
			public void onChannelObtainFailed(int reason) {
				// Generate list View from ArrayList
				displayListView(channelList);
			}
		});

	}

	// asinhroni zahteva za ucitavanje liste kanala
	class LoadAllChannels extends AsyncTask<String, String, String> {
		int errorCode = 0;
		String textView = "";

		// metoda koja postavi ProgressDialog
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Loading channels. Please wait....");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		// run metoda zahteva
		protected String doInBackground(String... args) {
			// UCITAVANJE KANALA
			channels = new ArrayList<String>();
			// httpGet request sa odgovarajuceg urla
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(Conf.url_all_channels);
				request.addHeader(Conf.headerName, Conf.headerValue);
				HttpResponse response = client.execute(request);
				// ucitavanje tog dobijenog odgovora u string format u JSON
				// obliku
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

				String line = "";
				while ((line = rd.readLine()) != null) {
					textView += line;
				}
				
				// parsiranje JSON formata u format ArrayList<String> channels
				try {
					JSONArray oneArray = new JSONArray(textView);
					for (int i = 0; i < oneArray.length(); i++) {
						JSONObject c = oneArray.getJSONObject(i);
						String name = c.getString("name");
						channels.add(name);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					errorCode = 1;
				}
			} catch (Exception e) {
				Log.d("ERROR LOADING CHANNELS: ", "FETCHING PROBLEM");
				errorCode = 1;
				e.printStackTrace();
			}
			return null;
		}

		// metoda koja ukloni ProgressDialog i osvezi UI. Osvezavanje UIa samo u
		// PostExecute metodi. Nikako u doInBackground()
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			runOnUiThread(new Runnable() {
				public void run() {
					if (errorCode == 0) {
						addItemsOnListView();
						writeChannelListToFile(channels);
					} else {
						Toast.makeText(MainActivity.this, "ERROR - CONNECTION PROBLEM", Toast.LENGTH_LONG).show();
					}
				}
			});
		}

	}

}
