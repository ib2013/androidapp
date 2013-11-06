package com.example.helloworld;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.infobip.push.ChannelObtainListener;
import com.infobip.push.ChannelRegistrationListener;
import com.infobip.push.PushNotificationManager;
import com.infobip.push.RegistrationData;
import com.infobip.push.lib.util.Util;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	MyCustomAdapter dataAdapter = null;
	
	private PushNotificationManager manager;
	
	final ArrayList<ChannelItem> channelList = new ArrayList<ChannelItem>();

	String[] chanals = new String[] {"Test Channel", "Test Channel2", "Test Channel3" };
	ArrayList<String> channels;

	RegistrationData rg;
	List<String> chl;
	
	ListView lvChannels;

	TextView tvTekst;
	
	CheckBox checkBoxSelectAll;
	
	private ProgressDialog pDialog;
	private static String url_all_channels = "http://get_all_channels_script.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//inicijalizira stvari za Push Notification:
		
		manager = new PushNotificationManager(getApplicationContext());
		manager.initialize(Conf.senderID, Conf.appID, Conf.appSec);
		
		if(!manager.isRegistered()) {
    		manager.register();
    	}
		
		
		new LoadAllChannels().execute();
			
		
		//Subscribe button click:
		final Button subscribeButton = (Button) findViewById(R.id.button1);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String toastText ="";
            	List<String> channels = new ArrayList<String>();
                for (ChannelItem chnl : channelList) {
                	if(chnl.getSelected()) {
                		channels.add(chnl.getName());
                		toastText += chnl.getName();
                		toastText += '\n';
                	}
                }
				ChannelRegistrationListener channelRegistrationListener = null;
				manager.registerToChannels(channels, true, channelRegistrationListener);
				Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
            }
        });
		
        checkBoxSelectAll = (CheckBox) findViewById(R.id.checkBoxSelectAll);
        
        checkBoxSelectAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					for (ChannelItem channelItem : channelList) 
						channelItem.setSelected(true);
					//Generate list View from ArrayList
					displayListView(channelList);
				}				
			}
		});
        
        
		Util.setDebugModeEnabled(false);
	  }
	
	//metoda za prikazivanje liste:
	
	private void displayListView(ArrayList<ChannelItem> channelList) { 
		
		
		//create an ArrayAdaptar from the String Array
		dataAdapter = new MyCustomAdapter(this, R.layout.list_channel, channelList);
		ListView listView = (ListView) findViewById(R.id.listView1);
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);
				
	}
	

	
	private class MyCustomAdapter extends ArrayAdapter<ChannelItem> {
		
		private ArrayList<ChannelItem> channelList;
		
		public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<ChannelItem> countryList) {
			   super(context, textViewResourceId, countryList);
			   this.channelList = new ArrayList<ChannelItem>();
			   this.channelList.addAll(countryList);
		}
		
		private class ViewHolder {
			   TextView code;
			   CheckBox name;
		}
			 
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			 
			ViewHolder holder = null;
			Log.v("ConvertView", String.valueOf(position));
			 
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_channel, null);
			 
				holder = new ViewHolder();
				holder.code = (TextView) convertView.findViewById(R.id.textView1);
				holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
				convertView.setTag(holder);
			 
				holder.name.setOnClickListener( new View.OnClickListener() {  
					public void onClick(View v) {  
						CheckBox cb = (CheckBox) v ;  
						ChannelItem channelItem = (ChannelItem) cb.getTag();  
						channelItem.setSelected(cb.isChecked());
						if (!cb.isChecked())
							checkBoxSelectAll.setChecked(false);
					}  
				});  
			} 
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			 
			ChannelItem channelItem = channelList.get(position);
			holder.code.setText(channelItem.getName());
			//holder.name.setText(channelItem.getName());
			holder.name.setChecked(channelItem.getSelected());
			holder.name.setTag(channelItem);
			 
			return convertView;
			 
		}	
		
	}
	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.refresh :
	        	new LoadAllChannels().execute();
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	
	
	
	
	
	
	private void addItemsOnListView() {
		Toast.makeText(this, "USPESNO POKUPLJENI KANALI." + channels,	Toast.LENGTH_LONG).show();
		channelList.clear();
		for (String str : channels) 
			channelList.add(new ChannelItem(str, false));
		
		//provjera koji su channeli vec subscribani:
		
		manager.getRegisteredChannels(new ChannelObtainListener() {

			@Override
			public void onChannelsObtained(String[] channels) {
				for (ChannelItem channelItem : channelList) 
					for (String str : channels)
						if (channelItem.getName().equals(str)) 
							channelItem.setSelected(true);	
				//Generate list View from ArrayList
				displayListView(channelList);
			}

			@Override
			public void onChannelObtainFailed(int reason) {
				//Generate list View from ArrayList
				displayListView(channelList);
			}
			
		});

	}
	
	
	class LoadAllChannels extends AsyncTask<String, String, String> {
		
		String textView = "";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Loading channels. Please wait....");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... args) {
			// UCITAVANJE KANALA
			channels = new ArrayList<String>();
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet("https://pushapi.infobip.com/1/application/9cabf301d3db/channels");
				request.addHeader("Authorization", "Basic cHVzaGRlbW86cHVzaGRlbW8=");
				HttpResponse response = client.execute(request);

				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				String line = "";
				while ((line = rd.readLine()) != null) {
					textView += line;
				}
				try {
					//result = "";
					JSONArray oneArray = new JSONArray(textView);
					for (int i = 0; i < oneArray.length(); i++) {
						JSONObject c = oneArray.getJSONObject(i);
						String name = c.getString("name");
						//result += name;
						channels.add(name);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				Log.d("GRESKA PRILIKOM UCITAVANJA KANALA: ", "PROBLEM U DOHVATANJU");
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			runOnUiThread(new Runnable() {
				public void run() {
					addItemsOnListView();
				}
			});
		}

	}

}
