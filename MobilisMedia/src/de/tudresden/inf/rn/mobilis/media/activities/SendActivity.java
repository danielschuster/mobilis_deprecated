/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.media.activities;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.StateListDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.core.ApplicationManager;
import de.tudresden.inf.rn.mobilis.media.services.IRepositoryService;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;

public class SendActivity extends TabActivity implements ServiceConnection {
	
	public static String SEND_TO_JID = "send_to_jid";
	public static String SEND_TO_REP = "send_to_rep";
	private String[] paths;
	private String[] uids;
	private Bundle[] slices;
	private ActivityViewManager viewManager;
	private IXMPPService xmppService;
	private IRepositoryService repService;
	private SharedPreferences preferences;
	private Messenger messenger;
	private String repository;
	private boolean imageSelected;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	this.messenger = new Messenger( new SendActivity.ActivityHandler() );
    	this.imageSelected = false;
        this.setContentView(R.layout.tab_host);
        this.createTabs();
    }
    
    private void createTabs() {
		Resources resources = this.getResources(); 
		TabHost tabHost = this.getTabHost();
		tabHost.setup();
		// creating tabs
		TabHost.TabSpec tabJid = tabHost.newTabSpec(SendActivity.SEND_TO_JID);
		TabHost.TabSpec tabRep = tabHost.newTabSpec(SendActivity.SEND_TO_REP);
		// defining tab icons
		final int selected = android.R.attr.state_selected;
		StateListDrawable drawableJid = new StateListDrawable();
		drawableJid.addState(new int[]{selected}, resources.getDrawable(R.drawable.tab_send_to_jid_dark));
		drawableJid.addState(new int[]{},         resources.getDrawable(R.drawable.tab_send_to_jid_light));
		tabJid.setIndicator(resources.getString(R.string.tab_send_to_jid), drawableJid);
		StateListDrawable drawableRep = new StateListDrawable();
		drawableRep.addState(new int[]{selected}, resources.getDrawable(R.drawable.tab_send_to_rep_dark));
		drawableRep.addState(new int[]{},         resources.getDrawable(R.drawable.tab_send_to_rep_light));
		tabRep.setIndicator(resources.getString(R.string.tab_send_to_rep), drawableRep);
		// defining tab contents
		SendActivity.ActivityViewManager vm = new SendActivity.ActivityViewManager();;
		this.viewManager = vm;
		tabJid.setContent(vm);
		tabRep.setContent(vm);
		tabHost.setOnTabChangedListener(vm);
		// adding tabs
		tabHost.addTab(tabJid);
		tabHost.addTab(tabRep);
		tabHost.setCurrentTab(0);
	}
    
    @Override
    protected void onStart() {
    	super.onStart();
    	ApplicationManager.setApplicationContext(this.getApplicationContext());
    	ApplicationManager.getInstance().initialize();
    	this.preferences = ApplicationManager.getInstance().getPreferences();
    	this.bindService(new Intent(IXMPPService.class.getName()),
				this, Context.BIND_AUTO_CREATE);
		this.bindService(new Intent(IRepositoryService.class.getName()),
				this, Context.BIND_AUTO_CREATE);
        this.processIntent();
    }
    
	public void onServiceConnected(ComponentName name, IBinder binder) {
		try {
			String interfaceName = binder.getInterfaceDescriptor();
			if (interfaceName.equals(IXMPPService.class.getName()))
				this.xmppService = IXMPPService.Stub.asInterface(binder);
			else if (interfaceName.equals(IRepositoryService.class.getName()))
				this.repService = IRepositoryService.Stub.asInterface(binder);
			if (this.xmppService != null && this.repService != null)
				this.onServicesConnected();
		} catch (RemoteException e) { }
	}
	
	private void onServicesConnected() {
		try {
			this.xmppService.connect(this.messenger);
		} catch (RemoteException e) {}
	}
    
    private void processIntent() {
    	Intent i = this.getIntent();
    	if (i.getAction().equals("android.intent.action.SEND")) {
    		Uri uri = (Uri)this.getIntent().getExtras().get("android.intent.extra.STREAM");
    		this.repository = null;
    		this.imageSelected = true;
    		this.onImageSelected(uri);
    	} else if (i.getAction().equals(ConstMMedia.intent.SEND)) {
    		if (this.imageSelected) {
    			this.imageSelected = false;
    		} else {
	    		Intent imageChooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
	    		imageChooserIntent.setType("image/*");
	    		this.repository = i.getStringExtra(ConstMMedia.intent.extra.STR_REPOSITORY);
	    		this.uids = new String[] { i.getStringExtra(ConstMMedia.intent.extra.STR_REPOSITORYITEM_UID) };
	    		this.imageSelected = false;
	    		this.startActivityForResult(imageChooserIntent, 0);
    		}
    	}
    	
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	this.onImageSelected(data.getData());
    	this.imageSelected = true;
    }
    
    private void onImageSelected(Uri uri) {
		Cursor c = this.managedQuery(uri,
				new String[]{
					MediaStore.Images.ImageColumns.DATA,
					MediaStore.Images.ImageColumns.TITLE,
					MediaStore.Images.ImageColumns.DESCRIPTION,
					MediaStore.Images.ImageColumns.DATE_TAKEN,
					MediaStore.Images.ImageColumns.LATITUDE,
					MediaStore.Images.ImageColumns.LONGITUDE,
					MediaStore.Images.ImageColumns.SIZE,
				}, null, null, null);

		String path=null, title=null, description=null;
		long datetaken=-1;
		double latitude=0.0, longitude=0.0;
		
		if (c != null && c.getCount() > 0) {
		    c.moveToFirst(); 
			path  = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
			title       = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.TITLE));
			description = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DESCRIPTION));
			datetaken   = c.getLong(c.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
			latitude    = c.getDouble(c.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE));
			longitude   = c.getDouble(c.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE));
		}		
		if (title       == null) title = "";
		if (description == null) description = "";
		if (datetaken == -1)     datetaken = System.currentTimeMillis();		
		// If chosen picture has no information about latitude or longitude,
		// then take phones last known location
		if (latitude == 0.0 || longitude == 0.0) {
			Criteria lc = new Criteria();
			lc.setAltitudeRequired(false);
			lc.setBearingRequired(false);
			lc.setCostAllowed(true);
			lc.setSpeedRequired(false);
			lc.setAccuracy(Criteria.ACCURACY_COARSE);
			lc.setPowerRequirement(Criteria.NO_REQUIREMENT);
			LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			String lp = lm.getBestProvider(lc, true);
			if (lp != null) {
				Location l = lm.getLastKnownLocation(lp);
				if (l != null) {
					latitude  = l.getLatitude();
					longitude = l.getLongitude();
				}
			}
		}
		
		Bundle bundle = new Bundle();
		bundle.putString(ConstMMedia.database.SLICE_TITLE,        title);
		bundle.putString(ConstMMedia.database.SLICE_DESCRIPTION,  description);
		bundle.putString(ConstMMedia.database.SLICE_TAKEN,        String.valueOf(datetaken));
		bundle.putString(ConstMMedia.database.SLICE_LATITUDE_E6,  String.valueOf((int)Math.ceil(latitude*1000000)));
		bundle.putString(ConstMMedia.database.SLICE_LONGITUDE_E6, String.valueOf((int)Math.ceil(longitude*1000000)));
		this.paths = new String[] { path };
		this.slices = new Bundle[] { bundle };
		this.viewManager.updateEnabledState();
    }

    
    @Override
    protected void onResume() {
    	super.onResume();
    	this.viewManager.updateEnabledState();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.send_media, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
		case R.id.menu_pref:
			i = new Intent(this, PreferencesActivity.class);
			break;
		case R.id.menu_pref_xmpp:
			i = new Intent(ConstMMedia.intent.PREF_XMPP);
			break;
		}
		this.startActivity(i);
		return false;
	}
    
	private class ActivityHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			SendActivity owner = SendActivity.this;
			super.handleMessage(msg);
			if (msg.what == ConstMXA.MSG_CONNECT
					&& msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS)
				owner.viewManager.updateViewGroup(null);
			else if (msg.what == ConstMMedia.message.WHAT_REPOSITORY_DISCOVER
					&& msg.arg1 == ConstMMedia.message.ARG1_SUCCESS
					&& owner.viewManager.currentTab.equals(SendActivity.SEND_TO_REP)) {
				ArrayList<String> targetList = msg.getData().getStringArrayList(ConstMMedia.message.data.STRL_REPOSITORIES);
				String[] targetArray = new String[targetList.size()];
				int i = 0; for (String t: targetList) targetArray[i++] = t;
				owner.viewManager.setTargets(targetArray);
			}
		}
	}
	
	public void onServiceDisconnected(ComponentName name) {
		this.xmppService = null;
	}
    
	@Override
	protected void onStop() {
		super.onStop();
		this.unbindService(this);
	}	
	
	private class ActivityViewManager
			implements TabContentFactory, OnClickListener, OnItemClickListener, OnTabChangeListener {

		ViewGroup viewGroup;
		EditText descriptionEdit;
		ListView targetList;
		TextView targetText;
		Button sendButton;
		String currentTab;
		String[] targets;
		
		public ActivityViewManager() {
			final SendActivity owner = SendActivity.this; 
			ViewGroup parent = (ViewGroup) owner.findViewById(android.R.id.tabcontent); 
			ViewGroup vg = (ViewGroup) View.inflate(owner, R.layout.send_media, parent);
			this.viewGroup = vg;
			this.descriptionEdit = (EditText)vg.findViewById(R.id.description_edit);
			this.targetText      = (TextView)vg.findViewById(R.id.target_text);
			ListView tl = this.targetList   = (ListView)vg.findViewById(R.id.target_list);
			Button   sb = this.sendButton   = (Button)vg.findViewById(R.id.send_button);
			Button   cb =                     (Button)vg.findViewById(R.id.cancel_button);
			this.currentTab = SendActivity.SEND_TO_JID;
			tl.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			tl.setOnItemClickListener(this);
			sb.setOnClickListener(this);
			cb.setOnClickListener(this);
		}
		
		public void updateEnabledState() {
			final SendActivity owner = SendActivity.this; 
			this.sendButton.setEnabled(
					this.targetList.getCheckedItemPosition() != -1
					&& owner.xmppService != null
					&& owner.paths != null
					&& owner.slices != null
				);
			if (owner.repository != null) {
				owner.getTabHost().setEnabled(false);
				owner.getTabHost().setCurrentTabByTag(SendActivity.SEND_TO_REP);
			}
		}

		private void updateViewGroup(String newCurrentTab) {
			if (newCurrentTab != null)
				this.currentTab = newCurrentTab;
			else
				newCurrentTab = this.currentTab; 
			Resources resources = SendActivity.this.getResources();
			if (newCurrentTab.equals(SendActivity.SEND_TO_JID)) {
				this.retrieveJidTargets();
				this.targetText.setText(resources.getString(R.string.pick_jid_label));
			} else if (newCurrentTab.equals(SendActivity.SEND_TO_REP)) {
				this.retrieveRepTargets();
				this.targetText.setText(resources.getString(R.string.pick_rep_label));
			}
			this.updateEnabledState();
		}
		
		private void retrieveJidTargets() {
			ContentResolver cr = SendActivity.this.getContentResolver();
			Cursor c = cr.query(ConstMXA.RosterItems.CONTENT_URI,
					new String[] { ConstMXA.RosterItems.XMPP_ID },
					null, null, null);
			String[] r = new String[c.getCount()];
			if (c.moveToFirst()) do
				r[c.getPosition()] = c.getString(c.getColumnIndex(ConstMXA.RosterItems.XMPP_ID));
			while (c.moveToNext());
			this.setTargets(r);
		}
		
	    private void retrieveRepTargets() {
	    	final SendActivity owner = SendActivity.this;
	    	final String serverJid = owner.preferences.getString("server", "");
	    	if (owner.repository != null) {
	    		this.setTargets(new String[] { owner.repository });
	    		this.targetList.setItemChecked(0, true);
	    	} else {
		    	if (owner.repService != null) {
		    		try {
						owner.repService.discover(serverJid, owner.messenger, 0);
					} catch (RemoteException e) { }
		    	}
		    	this.setTargets(new String[0]);
	    	}
	    }
	    
	    public void setTargets(String[] targets) {
	    	this.targets = targets;
			this.targetList.setAdapter( new ArrayAdapter<String>(
					SendActivity.this,
					android.R.layout.simple_list_item_single_choice,
					this.targets) );
	    }
	    
		/* TabContentFactory Role */
		
		public View createTabContent(String tag) {
			this.updateViewGroup(tag);
			return this.viewGroup;
		}
		
		/* OnTabChangedListener Role */

		public void onTabChanged(String tag) {
			this.updateViewGroup(tag);			
		}
		
		/* OnClick Role, OnItemClickListener Role */
		
		public void onClick(View v) {
			final SendActivity owner = SendActivity.this; 
			ListView targetList = this.targetList;
			if (v.equals(this.sendButton)) {
				String target = this.targets[targetList.getCheckedItemPosition()];
				String description = descriptionEdit.getText().toString();
				// prepare intent
				Intent i = null;
				if (this.currentTab.equals(SendActivity.SEND_TO_JID))
					i = new Intent(ConstMMedia.intent.SEND_TO_JID);
				else if (this.currentTab.equals(SendActivity.SEND_TO_REP)) {
					i = new Intent(ConstMMedia.intent.SEND_TO_REP);
					i.putExtra(ConstMMedia.intent.extra.BDLA_SLICES, owner.slices);
					if (owner.uids != null)
						i.putExtra(ConstMMedia.intent.extra.STRA_REPOSITORYITEMS_UIDS,
								owner.uids);
				}
				i.putExtra(ConstMMedia.intent.extra.STR_TO, target);
				i.putExtra(ConstMMedia.intent.extra.STR_DESCRIPTION, description);
				i.putExtra(ConstMMedia.intent.extra.STRA_PATHS, SendActivity.this.paths);
				owner.startService(i);
			}
			owner.finish(); 
		}

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			this.updateEnabledState();
		}
	
	
	}
	
}
