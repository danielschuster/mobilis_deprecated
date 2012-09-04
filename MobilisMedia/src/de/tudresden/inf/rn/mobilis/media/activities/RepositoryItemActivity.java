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

import java.util.Calendar;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import de.tudresden.inf.rn.mobilis.commons.views.AggregatedAdapter;
import de.tudresden.inf.rn.mobilis.commons.views.Command;
import de.tudresden.inf.rn.mobilis.commons.views.CommandListAdapter;
import de.tudresden.inf.rn.mobilis.commons.views.PairListAdapter;
import de.tudresden.inf.rn.mobilis.commons.views.TitledAdapter;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;
import de.tudresden.inf.rn.mobilis.media.services.IRepositoryService;
import de.tudresden.inf.rn.mobilis.media.views.RepositoryItemDeleteCommand;
import de.tudresden.inf.rn.mobilis.media.views.RepositoryItemDownloadCommand;
import de.tudresden.inf.rn.mobilis.media.views.RepositoryItemReplaceCommand;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPUtil;

public class RepositoryItemActivity extends ListActivity implements ServiceConnection, OnItemClickListener {

	private IRepositoryService repositoryService;
	private IXMPPService xmppService;
	private Messenger resultMessenger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.repository_item_list);
		this.resultMessenger = new Messenger(new Handler());
		this.getListView().setOnItemClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		this.bindService(new Intent(IRepositoryService.class.getName()), this, Context.BIND_AUTO_CREATE);
		this.bindService(new Intent(IXMPPService.class.getName()), this, Context.BIND_AUTO_CREATE);
	}
	
	public void onServiceConnected(ComponentName name, IBinder service) {
		String interfaceName;
		try {
			interfaceName = service.getInterfaceDescriptor();
			if (interfaceName.equals(IXMPPService.class.getName()))
				this.xmppService = IXMPPService.Stub.asInterface(service);
			else if (interfaceName.equals(IRepositoryService.class.getName()))
				this.repositoryService = IRepositoryService.Stub.asInterface(service);
		} catch (RemoteException e) {}
		if (this.xmppService != null && this.repositoryService != null)
			this.setupListAdapter();
	}

	public void onServiceDisconnected(ComponentName name) {
		this.repositoryService = null;
	}

	
	private void setupListAdapter() {
		final Resources r = this.getResources();
		AggregatedAdapter aggregatedAdapter    = new AggregatedAdapter();
		TitledAdapter     slicesTitleAdapter   = new TitledAdapter(this, r.getString(R.string.slices)); 
		TitledAdapter     commandsTitleAdapter = new TitledAdapter(this, r.getString(R.string.commands));
		PairListAdapter<String, String> slicesAdapter = new PairListAdapter<String, String>(this);
		CommandListAdapter            commandsAdapter = new CommandListAdapter(this);
		// get item information
		RepositoryItemParcel item = this.getRepositoryItem();
		String repository = this.getRepository();
		if (item != null && repository != null) {
			// add slices
			for (String slice: item.slices.keySet()) {
				String one = slice;
				String two = item.slices.get(slice);
				if (slice.equals(ConstMMedia.database.SLICE_LATITUDE_E6)) {
					float f = Float.parseFloat(two) / 1000000;
					one = r.getString(R.string.slice_latitude_e6);
					two = String.format("%.6f °%s", Math.abs(f), (f>0 ? "N" : "S"));
				} else if (slice.equals(ConstMMedia.database.SLICE_LONGITUDE_E6)) {
					float f = Float.parseFloat(two) / 1000000;
					one = r.getString(R.string.slice_longitude_e6);
					two = String.format("%.6f °%s", Math.abs(f), (f>0 ? "E" : "W"));
				} else if (slice.equals(ConstMMedia.database.SLICE_TAKEN)) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(Long.parseLong(two));
					one = r.getString(R.string.slice_taken);
					two = String.format("%02d.%02d.%02d %02d:%02d",
							c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH)+1, c.get(Calendar.YEAR)%100,
							c.get(Calendar.HOUR), c.get(Calendar.MINUTE)
						);
				} else
					if (slice.equals(ConstMMedia.database.SLICE_OWNER))
						one = r.getString(R.string.slice_owner);
					else if (slice.equals(ConstMMedia.database.SLICE_DESCRIPTION))
						one = r.getString(R.string.slice_description);
					else if (slice.equals(ConstMMedia.database.SLICE_TITLE))
						one = r.getString(R.string.slice_title);
				slicesAdapter.addPair(one, two);
			}
			// add commands
			commandsAdapter.addCommand(new RepositoryItemDownloadCommand(
					repository, item.uid, item.content, this.repositoryService, this.resultMessenger, 0));  
			try {
				if (XMPPUtil.similarJid(item.owner, this.xmppService.getUsername())) { 
					commandsAdapter.addCommand(new RepositoryItemReplaceCommand(
							repository, item.uid, this));
					commandsAdapter.addCommand(new RepositoryItemDeleteCommand(
							repository, item.uid, this.repositoryService, this.resultMessenger, 0));
				}
			} catch (RemoteException e) { }
		}
		// tie adapters together
		slicesTitleAdapter.setContentAdapter(slicesAdapter);
		commandsTitleAdapter.setContentAdapter(commandsAdapter);
		aggregatedAdapter.addAdapter(slicesTitleAdapter);
		aggregatedAdapter.addAdapter(commandsTitleAdapter);
		this.setListAdapter(aggregatedAdapter);
	}
	
	private String getRepository() {
		return this.getIntent().getStringExtra(ConstMMedia.intent.extra.STR_REPOSITORY);
	}
	
	private RepositoryItemParcel getRepositoryItem() {
		return this.getIntent().getParcelableExtra(ConstMMedia.intent.extra.PAR_REPOSITORYITEM);
	}	
	
	public void onItemClick(AdapterView<?> view, View parentView, int position, long id) {
		Object item = this.getListView().getItemAtPosition(position);
		if (item instanceof Command) {
			((Command)item).doCommand();
			this.finish();
		}
	}
	
}
