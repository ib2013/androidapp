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
	MyCustomAdapter dataAdapter = null;   	// data adapter za popunjavanje ListViewa
	private PushNotificationManager manager;// infobip manager
	final ArrayList<ChannelItem> channelList = new ArrayList<ChannelItem>();  // lista kanala za prikaz u listView
	ArrayList<String> channels;  // lista procitanih kanala
	CheckBox checkBoxSelectAll;  // cb za selekciju svih
	private ProgressDialog pDialog;  // progressdialog za ucitavanje liste kanala

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// inicijalizira stvari za Push Notification:
		manager = new PushNotificationManager(getApplicationContext());
		manager.initialize(Conf.senderID, Conf.appID, Conf.appSec);

		if (!manager.isRegistered()) {
			manager.register();
		}

		// poziv asinhronog zahteva da ucita kanale
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
				Toast.makeText(getApplicationContext(), "SUBSCRIBED ON: \n" + toastText,
						Toast.LENGTH_LONG).show();
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

		// Util.setDebugModeEnabled(false);
	}

	// metoda za prikazivanje liste:
	private void displayListView(ArrayList<ChannelItem> channelList) {
		// kreiraj ArrayAdaptar iz String Array
		dataAdapter = new MyCustomAdapter(this, R.layout.list_channel,
				channelList);
		ListView listView = (ListView) findViewById(R.id.listView1);
		// dodeli adapter u ListView
		listView.setAdapter(dataAdapter);
	}

	// klasa za definisanje naseg custom adaptera
	private class MyCustomAdapter extends ArrayAdapter<ChannelItem> {
		// lista kanala
		private ArrayList<ChannelItem> channelList;

		public MyCustomAdapter(Context context, int textViewResourceId,
				ArrayList<ChannelItem> countryList) {
			super(context, textViewResourceId, countryList);
			this.channelList = new ArrayList<ChannelItem>();
			this.channelList.addAll(countryList);
		}

		// klasa za jedan red liste kanala
		private class ViewHolder {
			TextView channelName;
			CheckBox checkbox;
		}

		//popuni pojedinacne retke u listViewu
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			Log.v("ConvertView", String.valueOf(position));
			
			//napravi view za list_channel
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_channel, null);

				holder = new ViewHolder();
				holder.channelName = (TextView) convertView
						.findViewById(R.id.textView1);
				holder.checkbox = (CheckBox) convertView
						.findViewById(R.id.checkBox1);
				convertView.setTag(holder);
				
				//napravi listener za checkpoint
				holder.checkbox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						ChannelItem channelItem = (ChannelItem) cb.getTag();
						channelItem.setSelected(cb.isChecked());
						if (!cb.isChecked())
							//ako se checkpoint u listi deselectira onda se deselectira i checkBoxSelectAll
							checkBoxSelectAll.setChecked(false);
					}
				});
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			//doda konkretne podatke u novi contentView
			ChannelItem channelItem = channelList.get(position);
			holder.channelName.setText(channelItem.getName());
			holder.checkbox.setChecked(channelItem.getSelected());
			holder.checkbox.setTag(channelItem);
			//holder.name.setText(channelItem.getName());
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

		return true;
	}

	// metoda za izbor selektovanog dugmeta - REFRESH dugme
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.refresh:
			new LoadAllChannels().execute();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// metoda koja updatuje UI na osnovu ucitane liste kanala
	private void addItemsOnListView() {
		// Toast.makeText(this, "USPESNO POKUPLJENI KANALI." + channels,
		// Toast.LENGTH_LONG).show();
		channelList.clear();
		for (String str : channels)
			channelList.add(new ChannelItem(str, false));

		// provjera koji su channeli vec subscribani i cekiraj ih:
		manager.getRegisteredChannels(new ChannelObtainListener() {

			@Override
			public void onChannelsObtained(String[] channels) {
				for (ChannelItem channelItem : channelList)
					for (String str : channels)
						if (channelItem.getName().equals(str))
							channelItem.setSelected(true);
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
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

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
				}
			} catch (Exception e) {
				Log.d("GRESKA PRILIKOM UCITAVANJA KANALA: ",
						"PROBLEM U DOHVATANJU");
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
					addItemsOnListView();
				}
			});
		}

	}

}
