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

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import de.tudresden.inf.rn.mobilis.media.services.RepositoryService;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;

public abstract class Task extends IXMPPIQCallback.Stub {

	protected int id;
	protected int requestCode;
	protected Messenger resultMessenger = null;
	protected RepositoryService service = null; 
	
	public void initialize(RepositoryService service, int id,
			Messenger resultMessenger, int requestCode) {
		this.service = service;
		this.id = id;
		this.resultMessenger = resultMessenger;
		this.requestCode = requestCode;
	}
	
	public abstract void onRun();
	
	public void onResult() {
		this.service.removeTask(this.id);
	}
	
	public void onError() {
		this.service.removeTask(this.id);
	}
	
	public abstract void handleMessage(Message msg);
	public abstract void processIQ(XMPPIQ iq) throws RemoteException;
}
