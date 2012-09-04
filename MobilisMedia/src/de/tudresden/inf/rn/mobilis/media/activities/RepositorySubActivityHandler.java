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

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.parcelables.ConditionParcel;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;

public class RepositorySubActivityHandler extends Handler {
	
	private int arg2;
	private Messenger toParentMessenger;
	private Messenger fromParentMessenger;
	private SubActivityListener subActivityListener;

	public RepositorySubActivityHandler(Intent startingIntent) {
		this.fromParentMessenger = new Messenger( this ); 
		this.toParentMessenger   = (Messenger) startingIntent.getParcelableExtra(
				ConstMMedia.intent.extra.PAR_PARENTMESSENGER );
		this.arg2 = startingIntent.getIntExtra( ConstMMedia.intent.extra.INT_CHILDARG2, 0 );
	}
	
	public void setSubActivityListener(RepositorySubActivityHandler.SubActivityListener l) {
		this.subActivityListener = l;
	}
		
	protected void trySendToParent(Message m) {
		try {
			this.toParentMessenger.send(m);
		} catch (RemoteException e) { }
	}
	
	public void subActivityRegister() {
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_SUBACTIVITY_REGISTER;
		m.arg1 = ConstMMedia.message.ARG1_INFO;
		m.arg2 = this.arg2;
		m.getData().putParcelable(ConstMMedia.message.data.PAR_CHILDMESSENGER, this.fromParentMessenger);
		this.trySendToParent(m);
	}
	
	public void subActivityUnregister() {
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_SUBACTIVITY_UNREGISTER;
		m.arg1 = ConstMMedia.message.ARG1_INFO;
		m.arg2 = this.arg2;
		this.trySendToParent(m);
	}
	
	public void subActivityUpdate(ConditionParcel condition) {
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_SUBACTIVITY_UPDATE;
		m.arg1 = ConstMMedia.message.ARG1_INFO;
		m.arg2 = this.arg2;
		m.getData().putParcelable(ConstMMedia.message.data.PAR_CONDITION, condition);
		this.trySendToParent(m);
	}
	
	public void subActivityDisplay(RepositoryItemParcel repositoryItem) {
		Message m = Message.obtain();
		m.what = ConstMMedia.message.WHAT_SUBACTIVITY_DISPLAY;
		m.arg1 = ConstMMedia.message.ARG1_INFO;
		m.arg2 = this.arg2;
		m.getData().putParcelable(ConstMMedia.message.data.PAR_REPOSITORYITEM, repositoryItem);
		this.trySendToParent(m);
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (this.subActivityListener == null) return;
		switch (msg.what) {
		case ConstMMedia.message.WHAT_SUBACTIVITY_HIDE:
			this.subActivityListener.onSubActivityHide();
			break;
		case ConstMMedia.message.WHAT_SUBACTIVITY_SHOW:
			this.subActivityListener.onSubActivityShow();
			break;
		case ConstMMedia.message.WHAT_SUBACTIVITY_OUTDATE:
			this.subActivityListener.onSubActivityOutdate();
			break;
		case ConstMMedia.message.WHAT_SUBACTIVITY_UPDATE:
			if (msg.arg1 == ConstMMedia.message.ARG1_SUCCESS)
				this.subActivityListener.onSubActivityUpdate(
						(RepositoryItemParcel[])
							msg.getData().getParcelableArray(ConstMMedia.message.data.PARA_REPOSITORYITEM)
					);
			else
				this.subActivityListener.onSubActivityUpdateError();
		}
	}
	
	public static interface SubActivityListener {
		public void onSubActivityHide();
		public void onSubActivityShow();
		public void onSubActivityOutdate();
		public void onSubActivityUpdate(RepositoryItemParcel[] items);
		public void onSubActivityUpdateError();
	}
}
