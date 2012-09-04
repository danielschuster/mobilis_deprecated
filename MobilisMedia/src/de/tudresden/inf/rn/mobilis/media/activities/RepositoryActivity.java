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
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.core.ApplicationManager;
import de.tudresden.inf.rn.mobilis.media.parcelables.ConditionParcel;
import de.tudresden.inf.rn.mobilis.media.services.IRepositoryService;

public abstract class RepositoryActivity extends ActivityGroup
		implements ServiceConnection, OnTabChangeListener, DialogInterface.OnClickListener {
	
	/** The TAG for the Log. */
	private final static String TAG = "RepositoryActivity";
	
	private IRepositoryService service;
	private String[] repositories = new String[0];
	private String selectedRepository = null;
	private Messenger messenger = null;
	private List<RepositoryActivity.SubActivity> views = new LinkedList<RepositoryActivity.SubActivity>();
	private String currentTag = null;
	private SharedPreferences preferences = null;
	
	/* activity lifecycle */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tab_host);
		this.messenger = new Messenger(
				new RepositoryActivity.ActivityHandler() );
		this.registerSubActivities();
		this.onSubActivitiesRegistered();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
    	ApplicationManager.setApplicationContext(this.getApplicationContext());
    	ApplicationManager.getInstance().initialize();
		this.preferences = ApplicationManager.getInstance().getPreferences();
		this.bindService(new Intent(IRepositoryService.class.getName()),
				this, Service.BIND_AUTO_CREATE);
	}
	
	protected void onSubActivitiesRegistered() {
		this.createTabs();
		if (this.views.size() == 0)
			throw new IllegalStateException("There has to be at least one registered view!");
		this.currentTag = "0";
	}
	
	public void onServiceConnected(ComponentName component, IBinder binder) {
		this.service = IRepositoryService.Stub.asInterface(binder);
		if (this.selectedRepository == null) this.discoverRepositories();
	}
	
	public void discoverRepositories() {
		try {
			String mobilisServer = this.preferences.getString("server", "");
			Log.i(TAG, "discoverRepositories(). server="+mobilisServer);
			this.service.discover(mobilisServer, this.messenger, 0);
		} catch (RemoteException e) { };
	}
	
	protected static final int DIALOG_SELECT_REPOSITORY = 0;
	protected void onRepositoriesDiscovered(String[] repositories) {
		this.repositories = repositories;
		this.selectRepository();
	}
	
	public void selectRepository() {
		this.showDialog(RepositoryActivity.DIALOG_SELECT_REPOSITORY);
	}
		
	protected void onRepositorySelected(String repository) {
		this.selectedRepository = repository;
		for (SubActivity a: this.views)
			this.onSubActivityOutdate(a);
	}
	
	public void onServiceDisconnected(ComponentName component) {
		this.service = null;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		this.unbindService(this);
	}
	
	/* menu management */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.repository, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			this.onSubActivityOutdate(this.views.get(Integer.parseInt(this.currentTag)));
			return true;
		case R.id.menu_selectrep:
			this.discoverRepositories();
			return true;
		case R.id.menu_upload:
			Intent i = new Intent(ConstMMedia.intent.SEND);
			i.putExtra(ConstMMedia.intent.extra.STR_REPOSITORY, this.selectedRepository);
			this.startActivity(i);
			return true;
		case R.id.menu_pref:
			this.startActivity( new Intent(this, PreferencesActivity.class) );
			return true;
		case R.id.menu_pref_xmpp:			
			this.startActivity( Intent.createChooser(new Intent(ConstMMedia.intent.PREF_XMPP), getString(R.string.mxa_not_found)) );
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/* dialog management */
	
	@Override
	protected Dialog onCreateDialog(int id) {
		final Resources r = this.getResources();
		if (id == RepositoryActivity.DIALOG_SELECT_REPOSITORY)
			return new AlertDialog.Builder(this)
					.setTitle(r.getString(R.string.pick_rep_title))
					/*.setMessage(r.getString(R.string.pick_rep_label))*/
					.setCancelable(true)
					.setNegativeButton(r.getString(R.string.pick_rep_cancel), this)
					.setAdapter( new ArrayAdapter<String>( 
							this, android.R.layout.select_dialog_item, 
							android.R.id.text1, this.repositories ), this )
					.create();
		else
			return super.onCreateDialog(id);
	}	
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if (id == RepositoryActivity.DIALOG_SELECT_REPOSITORY) {
			AlertDialog ad = (AlertDialog) dialog;
			ad.getListView().setAdapter(new ArrayAdapter<String>(
					this, android.R.layout.select_dialog_item,
					android.R.id.text1, this.repositories
				));
		}
	}
	
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_NEGATIVE:
			dialog.cancel();
			break;
		default:
			this.onRepositorySelected(this.repositories[which]);
			dialog.dismiss();
		}
	}
	
	/* activity management */
	
	public static class SubActivity {
		Class<? extends Activity> activity;
		int tabUnselectedDrawable;
		int tabSelectedDrawable;
		String tabCaption;
		Messenger outMessenger;
		
		public SubActivity(Class<? extends Activity> activity,
				int tabUnselectedDrawable, int tabSelectedDrawable, String tabCaption) {
			this.activity = activity;
			this.tabSelectedDrawable = tabSelectedDrawable;
			this.tabUnselectedDrawable = tabUnselectedDrawable;
			this.tabCaption = tabCaption;
		}
		
		public void trySendMessage(Message m) {
			try {
				if (this.outMessenger != null) this.outMessenger.send(m);
			} catch (RemoteException e) { }
		}
	}
	
	protected abstract void registerSubActivities();
	
	protected void registerSubActivity(SubActivity view) {
		if (currentTag != null)
			throw new IllegalStateException("You can't register new views after calling viewsRegistered()");
		this.views.add(view);
	}
	
	private void createTabs() {
		Resources r  = this.getResources();
		TabHost   th = (TabHost) this.findViewById(android.R.id.tabhost);
		th.setup(this.getLocalActivityManager());
		final int selected = android.R.attr.state_selected;
		for (int i = 0; i < this.views.size(); i++) {
			RepositoryActivity.SubActivity va = this.views.get(i);
			TabHost.TabSpec   ts = th.newTabSpec(String.valueOf(i));
			StateListDrawable td = new StateListDrawable();
			td.addState(new int[] { selected }, r.getDrawable(va.tabSelectedDrawable) );
			td.addState(new int[] { },          r.getDrawable(va.tabUnselectedDrawable) );
			ts.setIndicator(va.tabCaption, td);
			Intent tc = new Intent(this, va.activity);
			tc.putExtra(ConstMMedia.intent.extra.PAR_PARENTMESSENGER, this.messenger);
			tc.putExtra(ConstMMedia.intent.extra.INT_CHILDARG2,       i);
			ts.setContent(tc);
			th.addTab(ts);
		}
		th.setOnTabChangedListener(this);
		th.setCurrentTab(0);
	}
	
	public void onTabChanged(String tag) {
		String currentTag = this.currentTag;
		if (!currentTag.equals(tag)) {
			this.onSubActivityHide(this.views.get(Integer.parseInt(currentTag)));
			this.onSubActivityShow(this.views.get(Integer.parseInt(tag)));
			this.currentTag = tag;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("repository", this.selectedRepository);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String repository = savedInstanceState.getString("repository");
		if (repository != null) this.selectedRepository = repository;
	}
	
	protected void onSubActivityHide(RepositoryActivity.SubActivity a) {
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_SUBACTIVITY_HIDE;
		m.arg1 = ConstMMedia.message.ARG1_INFO;
		a.trySendMessage(m);
	}
	
	protected void onSubActivityShow(RepositoryActivity.SubActivity a) {
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_SUBACTIVITY_HIDE;
		m.arg1 = ConstMMedia.message.ARG1_INFO;
		a.trySendMessage(m);
	}
	
//	protected void onAllSubActivitiesOutdate() {
//		for (RepositoryActivity.SubActivity a: this.views)
//			this.onSubActivityOutdate(a);
//	}
	
	protected void onSubActivityOutdate(RepositoryActivity.SubActivity a) {
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_SUBACTIVITY_OUTDATE;
		m.arg1 = ConstMMedia.message.ARG1_INFO;
		a.trySendMessage(m);
	}
	
	protected void onSubActivityUpdate(RepositoryActivity.SubActivity a, boolean success, Parcelable[] parcels) {
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_SUBACTIVITY_UPDATE;
		m.arg1 = (success ? ConstMMedia.message.ARG1_SUCCESS : ConstMMedia.message.ARG1_ERROR);
		m.getData().putParcelableArray(ConstMMedia.message.data.PARA_REPOSITORYITEM, parcels);
		a.trySendMessage(m);
	}
	
	protected class ActivityHandler extends Handler {
		@Override
		public void handleMessage(Message m) {
			super.handleMessage(m);
			final RepositoryActivity owner = RepositoryActivity.this; 
			switch (m.what) {
			case ConstMMedia.message.WHAT_REPOSITORY_QUERY:
				owner.onSubActivityUpdate(
						owner.views.get(m.arg2),
						m.arg1 == ConstMMedia.message.ARG1_SUCCESS,
						m.getData().getParcelableArray( ConstMMedia.message.data.PARA_REPOSITORYITEM )
					);
				break;
			case ConstMMedia.message.WHAT_REPOSITORY_DISCOVER:
				Log.i(TAG, "message.WHAT_REPOSITORY_DISCOVER");
				if (m.arg1 == ConstMMedia.message.ARG1_SUCCESS) {
					ArrayList<String> repositories
							= m.getData().getStringArrayList(ConstMMedia.message.data.STRL_REPOSITORIES);
					String[] rs = new String[repositories.size()];
					int i = 0;
					for (String r: repositories) {
						rs[i] = r;
						Log.i(TAG,"RepositoryDiscover: "+r);
					}
					owner.onRepositoriesDiscovered(rs);
				}
				break;
			case ConstMMedia.message.WHAT_SUBACTIVITY_REGISTER:
				owner.views.get(m.arg2).outMessenger
						= (Messenger) m.getData().getParcelable(ConstMMedia.message.data.PAR_CHILDMESSENGER);
				break;
			case ConstMMedia.message.WHAT_SUBACTIVITY_UNREGISTER:
				owner.views.get(m.arg2).outMessenger = null;
				break;
			case ConstMMedia.message.WHAT_SUBACTIVITY_UPDATE:
				if (owner.service != null && owner.selectedRepository != null)
					try {
						owner.service.query(
								owner.selectedRepository,
								(ConditionParcel)
									m.getData().getParcelable(ConstMMedia.message.data.PAR_CONDITION),
								owner.messenger,
								m.arg2
							);
					} catch (RemoteException e) { }
				break;
			case ConstMMedia.message.WHAT_SUBACTIVITY_DISPLAY:
				Intent i = new Intent(ConstMMedia.intent.DISPLAY_REPOSITORYITEM);
				i.putExtra(ConstMMedia.intent.extra.PAR_REPOSITORYITEM,
						m.getData().getParcelable(ConstMMedia.message.data.PAR_REPOSITORYITEM));
				i.putExtra(ConstMMedia.intent.extra.STR_REPOSITORY,
						owner.selectedRepository);
				owner.startActivity(i);
				break;
			}
		}
	}
		
}
