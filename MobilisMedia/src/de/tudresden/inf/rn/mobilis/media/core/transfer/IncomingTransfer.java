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
package de.tudresden.inf.rn.mobilis.media.core.transfer;

import android.os.Messenger;
import android.os.RemoteException;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.mxa.services.callbacks.IFileAcceptCallback;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;

public class IncomingTransfer extends Transfer {

	private boolean initiated; 
	private String streamId;
	private String path;
	private int blockSize;
	private IFileAcceptCallback acceptCallback;
	
	public IncomingTransfer(TransferManager fileTransferManager, int id, String streamId,
			IFileAcceptCallback acceptCallback, FileTransfer xmppFile) {
		super(fileTransferManager, id, xmppFile);
		this.state = ConstMMedia.enumeration.STATE_REQUESTED;
		this.streamId = streamId;
		this.acceptCallback = acceptCallback;
		this.xmppFile = xmppFile;
		this.initiated = false;
	}
	
	@Override
	public long getTotalSize() {
		return this.xmppFile.size;
	}

	public boolean initiate(String path, int blockSize) {
		this.path = path;
		this.blockSize = blockSize;
		return this.initiate();
	}
		
	public boolean onReady() {
		final Messenger xmppMessenger = this.manager.getService().getXmppMessenger();
		try {
			this.acceptCallback.acceptFile(xmppMessenger, this.id, this.streamId, this.path, this.blockSize);
		} catch (RemoteException e) {
			this.notifyFailed(this, -2, "Could not initiate file transfer.");
		}
		this.initiated = true;
		return true;
	}


	@Override
	public boolean terminate() {
		final Messenger xmppMessenger = this.manager.getService().getXmppMessenger();
		if (this.initiated) {
			// TODO To be implemented
			return false;
		} else {
			try {
				this.acceptCallback.denyFileTransferRequest(xmppMessenger, this.id, this.streamId);
				this.notifyFailed(this, -4, "Transfer canceled by user.");
				return true;
			} catch (RemoteException e) {
				return false;
			}
		}
	}

	@Override
	public int getBlockSize() {
		return this.blockSize;
	}

}
