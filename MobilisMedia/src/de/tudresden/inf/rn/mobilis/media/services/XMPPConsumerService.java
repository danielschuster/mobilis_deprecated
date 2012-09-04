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
package de.tudresden.inf.rn.mobilis.media.services;

import java.util.LinkedList;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.media.core.ApplicationManager;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;

public abstract class XMPPConsumerService extends Service implements ServiceConnection {
	
	/** The TAG for the Log. */
	private final static String TAG = "XMPPConsumerService";

	protected SharedPreferences xmppPreferences = null;
	protected IXMPPService xmppService = null;
	protected Messenger xmppMessenger = null;
	protected IBinder binder = null;
	protected boolean xmppConnected = false;
	private List<Intent> intentQueue = new LinkedList<Intent>();
	
	@Override
	public void onCreate() {
		// the service is created -> bind the XMPP service
		super.onCreate();
		this.binder = this.newBinder();
		ApplicationManager.setApplicationContext(this.getApplicationContext());
		this.xmppPreferences = ApplicationManager.getInstance().getPreferences();
	}
	
	public void onServiceConnected(ComponentName name, IBinder binder) {
		// the XMPP service is bound -> initiate binder, connect to XMPP
		this.xmppService = IXMPPService.Stub.asInterface(binder);
		this.xmppMessenger = new Messenger( this.newHandler() );
		this.prepareXmppService();
	}
	
	public void onServiceDisconnected(ComponentName name) {
		// the XMPP service cannot be bound -> stop service
		this.xmppConnected = false;
		this.xmppService = null;
		this.stopSelf();
	}
	
	public void onXMPPConnect() {
		// the XMPP connection has been established -> process intents
		Log.i(TAG, "onXMPPConnected().");
		this.xmppConnected = true;
		this.onExecute();
	}

	public void onXMPPFailure() {
		// the XMPP connection cannot be established -> unbind MXA
		this.xmppConnected = false;
		this.unbindService(this);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		// a client requests to start the service
		super.onStart(intent, startId);
		this.intentQueue.add(intent);
		this.prepareXmppService();			
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unbindService(this);
	}
	
	protected void onExecute() {
		// the intent queue can be processed
		for (Intent i: this.intentQueue)
			this.onExecute(i);
		intentQueue.clear();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		this.prepareXmppService();
		return this.binder;
	}

	public IXMPPService getXmppService() {
		return this.xmppService;
	}
	
	public Messenger getXmppMessenger() {
		return this.xmppMessenger;
	}
	
	private void prepareXmppService() {
		if (this.xmppService == null)
			this.bindService(new Intent(IXMPPService.class.getName()),
					this, Context.BIND_AUTO_CREATE);
		else if (!this.xmppConnected)
			try {
				this.xmppService.connect(this.xmppMessenger);
			} catch (RemoteException e) {
				this.onXMPPFailure();
			}
		else 
			this.onXMPPConnect();
	}
	
	/* override this */
	
	protected abstract void onExecute(Intent i);
	
	protected Handler newHandler() {
		return new XMPPConsumerService.ServiceHandler();
	}
	
	protected abstract IBinder newBinder();
	
	protected class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			final XMPPConsumerService owner = XMPPConsumerService.this;
			super.handleMessage(msg);
			if (msg.what == ConstMXA.MSG_CONNECT) 
				if (msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS)
					owner.onXMPPConnect();
				else
					owner.onXMPPFailure();
		}
	}
}
