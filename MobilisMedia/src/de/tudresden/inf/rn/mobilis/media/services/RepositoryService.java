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

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import de.tudresden.inf.rn.mobilis.media.core.repository.DeleteTask;
import de.tudresden.inf.rn.mobilis.media.core.repository.DiscoverTask;
import de.tudresden.inf.rn.mobilis.media.core.repository.QueryTask;
import de.tudresden.inf.rn.mobilis.media.core.repository.Task;
import de.tudresden.inf.rn.mobilis.media.core.repository.TransferTask;
import de.tudresden.inf.rn.mobilis.media.parcelables.ConditionParcel;

public class RepositoryService extends XMPPConsumerService {

	/** The TAG for the Log. */
	private final static String TAG = "RepositoryService";
	
	private SparseArray<Task> tasks = new SparseArray<Task>();
	private SparseArray<Task> waitingTasks = new SparseArray<Task>();
	private int taskId = 1;
	
	@Override
	protected IBinder newBinder() {
		return new RepositoryService.ServiceBinder();
	}
	
	@Override
	protected Handler newHandler() {
		return new RepositoryService.ServiceHandler();
	}
	
	@Override
	protected void onExecute() {
		super.onExecute();
		SparseArray<Task> waitingTasks = this.waitingTasks;
		for (int index = 0; index < waitingTasks.size(); index++) {
			int key = waitingTasks.keyAt(index);
			Task task = waitingTasks.get(key);
			this.tasks.put(key, task); 
			task.onRun();
		}
		waitingTasks.clear();
	}
	
	@Override
	protected void onExecute(Intent i) { }
	
	private void addTask(Task t, Messenger resultMessenger, int resultCode) {
		t.initialize(this, this.taskId, resultMessenger, resultCode);
		if (this.xmppService != null && this.xmppConnected) {
			this.tasks.put(this.taskId, t);
			t.onRun();
		} else 
			this.waitingTasks.put(this.taskId, t);
		this.taskId++;
	}
	
	public void removeTask(Task t) {
		this.tasks.remove(this.tasks.indexOfValue(t));
	}
	
	public void removeTask(int id) {
		this.tasks.remove(id);
	}
	
	public Task getTask(int id) {
		return this.tasks.get(id);
	}

	protected class ServiceHandler extends XMPPConsumerService.ServiceHandler {
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.arg2 >= 0) {
				Task t = RepositoryService.this.getTask(msg.arg2);
				if (t != null)
					t.handleMessage(msg);
			}	
		}
		
	}
	
	protected class ServiceBinder extends IRepositoryService.Stub {
		
		public void query(String repository, ConditionParcel condition,
				Messenger resultMessenger, int resultCode) {
			RepositoryService.this.addTask(new QueryTask(repository, condition), resultMessenger, resultCode);	
		}
		
		public void delete(String repository, String[] uids,
				Messenger resultMessenger, int resultCode) {
			RepositoryService.this.addTask(new DeleteTask(repository, uids), resultMessenger, resultCode);	
		}
		
		public void discover(String serverJid,
				Messenger resultMessenger, int resultCode) {
			Log.i(TAG, "discover(). serverJid="+serverJid);
			RepositoryService.this.addTask(new DiscoverTask(serverJid), resultMessenger, resultCode);	
		}

		public void transfer(String repository, String content, String uid,
				Messenger resultMessenger, int resultCode)
				throws RemoteException {
			RepositoryService.this.addTask(new TransferTask(content, repository, uid), resultMessenger, resultCode);
		}
		
	}

}
