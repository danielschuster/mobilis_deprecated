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
package de.tudresden.inf.rn.mobilis.media.core.repository;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.core.ApplicationManager;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.DiscoverItem;
import de.tudresden.inf.rn.mobilis.mxa.services.servicediscovery.IServiceDiscoveryService;

public class DiscoverTask extends Task {
	
	/** The TAG for the Log. */
	private final static String TAG = "DiscoverTask";

	protected String serverJid;
	protected int expectedBrokers = 0;
	protected ArrayList<String> repositoryBrokers = new ArrayList<String>();
	protected IServiceDiscoveryService serviceDiscoveryService;
	
	public DiscoverTask(String serverJid) {
		this.serverJid = serverJid;
	}

	@Override
	public void onRun() {
		final Messenger xmppMessenger  = this.service.getXmppMessenger();
		final IXMPPService xmppService = this.service.getXmppService(); 
		try {
			xmppService.getServiceDiscoveryService().discoverItem(
					xmppMessenger, xmppMessenger, this.id, this.serverJid, null);
			Log.i(TAG, "onRun(). DiscoverItem send");
		} catch (RemoteException e) {
			this.onError();
		}
	}
	
	public void onRun(String broker) {
		final Messenger xmppMessenger  = this.service.getXmppMessenger();
		final IXMPPService xmppService = this.service.getXmppService(); 
		try {
			xmppService.getServiceDiscoveryService().discoverItem(
					xmppMessenger, xmppMessenger, this.id, broker, ConstMMedia.namespace.SERVICES);
			Log.i(TAG, "onRun(String broker). DiscoverItem send");
		} catch (RemoteException e) {
			this.onError();
		}
	}

	@Override
	public void handleMessage(Message msg) {
		if (msg.what == ConstMXA.MSG_DISCOVER_ITEMS)
			if (msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS) {
				Bundle d = msg.getData();
				d.setClassLoader(ApplicationManager.getInstance().getClassLoader());
				String                  jid   = d.getString("JID"); 
				ArrayList<DiscoverItem> items = d.getParcelableArrayList("DISCOVER_ITEMS");
				if (jid.equals(serverJid)) {
					this.repositoryBrokers = new ArrayList<String>(items.size());
					int counter = 0;
					for (DiscoverItem item: items)
						if (!item.jid.equals(serverJid)) {
							this.onRun(item.jid);
							counter++;
						}
					this.expectedBrokers = counter;
					if (this.expectedBrokers == 0) this.onResult();
				} else {
					final String repositoryService = ConstMMedia.namespace.REPOSITORY_SERVICE;
					for (DiscoverItem item: items)
						if (item.node.equals(repositoryService)) {
							this.repositoryBrokers.add(jid);
							break;
						}
					this.expectedBrokers--;
					if (this.expectedBrokers <= 0)
						this.onResult();
				}
			} else if (msg.arg1 != ConstMXA.MSG_STATUS_DELIVERED)
				this.onError();
	}


	@Override
	public void processIQ(XMPPIQ iq) throws RemoteException {}
	
	@Override
	public void onResult() {
		super.onResult();
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_REPOSITORY_DISCOVER;
		m.arg1 = ConstMMedia.message.ARG1_SUCCESS;
		m.arg2 = this.requestCode;
		m.getData().putStringArrayList(
				ConstMMedia.message.data.STRL_REPOSITORIES,
				this.repositoryBrokers
			);
		try { this.resultMessenger.send(m); } catch (RemoteException e) {}
	}
	
	@Override
	public void onError() {
		super.onError();
		Log.e(TAG, "onError().");
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_REPOSITORY_DISCOVER;
		m.arg1 = ConstMMedia.message.ARG1_ERROR;
		m.arg2 = this.requestCode;
		try { this.resultMessenger.send(m); } catch (RemoteException e) {}
	}

}
