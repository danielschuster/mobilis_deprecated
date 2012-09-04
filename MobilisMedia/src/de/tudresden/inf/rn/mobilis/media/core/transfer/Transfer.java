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

import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.os.Message;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;


public abstract class Transfer {

	protected int state;
	protected int blocksTransferred;
	protected boolean isInitiated;
	protected long bytesTransferred;
	protected final int id;
	protected FileTransfer xmppFile;
	protected TransferManager manager;
	
	private Set<TransferObserver> fileTransferObservers = new HashSet<TransferObserver>();
		
	public Transfer(TransferManager manager, int id, FileTransfer xmppFile) {
		this.manager = manager;
		this.xmppFile = xmppFile;
		this.id = id;
		this.blocksTransferred = -1;
		this.state = ConstMMedia.enumeration.STATE_STANDBY;
	}
		
	/*
	 * Observer-Pattern
	 */
	
	protected void notifyInitiated(Transfer sender) {
		this.state = ConstMMedia.enumeration.STATE_INITIATED;
		this.blocksTransferred = 0;
		for (TransferObserver fto: this.fileTransferObservers)
			fto.initiated(sender);
	}
	
	protected void notifyNegotiation(Transfer sender) {
		this.state = ConstMMedia.enumeration.STATE_NEGOTIATED;
		this.blocksTransferred = 0;
		for (TransferObserver fto: this.fileTransferObservers)
			fto.negotiation(sender);
	}
	
	protected void notifyBlockTransferred(Transfer sender, int block, long bytes) {
		this.state = ConstMMedia.enumeration.STATE_INPROGRESS;
		this.blocksTransferred = block;
		this.bytesTransferred += bytes;
		for (TransferObserver fto: this.fileTransferObservers)
			fto.blockTransferred(sender, block);
	}
	
	protected void notifyFinished(Transfer sender) {
		this.state = ConstMMedia.enumeration.STATE_FINISHED;
		for (TransferObserver fto: this.fileTransferObservers)
			fto.finished(sender);
		this.manager.removeFileTransfer(this);
	}
	
	protected void notifyFailed(Transfer sender, int reason, String message) {
		this.state = ConstMMedia.enumeration.STATE_FAILED;
		for (TransferObserver fto: this.fileTransferObservers)
			fto.failed(sender, reason, message);
		this.manager.removeFileTransfer(this);
	}

	public void registerMediaTransferObserver(TransferObserver fto) {
		this.fileTransferObservers.add(fto);
	}
	
	public void unregisterMediaTransferObserver(TransferObserver fto) {
		this.fileTransferObservers.remove(fto);
	}
	
	/*
	 * Abstract Methods
	 */
	
	abstract public long getTotalSize();
	abstract public int getBlockSize();
	abstract public boolean terminate();

	public boolean initiate() {
		return this.onReady();
	}
	
	protected abstract boolean onReady();
		
	protected void onError(int error, String message) {
		this.notifyFailed(this, error, message);
	}
		
	protected void onNegotiation() {
		this.notifyNegotiation(this);
	}

	protected void onBlocksTransferred(int blocksTransferred, long bytesTransferred) {
		this.notifyBlockTransferred(this, blocksTransferred, bytesTransferred);
	}
		
	protected void onFinnished() {
		this.notifyFinished(this);
	}
		
	public void handleMessage(Message msg) {
		if (msg.what == ConstMXA.MSG_SEND_FILE) {
			if (msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS) {
				Bundle b = msg.getData();
				int blocksTransferred = b.getInt("BLOCKSTRANSFERRED");
				long bytesTransferred = b.getLong("BYTESTRANSFERRED");
				if (blocksTransferred == -1)
					this.onNegotiation();
				else
					this.onBlocksTransferred(blocksTransferred, bytesTransferred);
			} else if (msg.arg1 == ConstMXA.MSG_STATUS_DELIVERED) {
				this.onFinnished();
			} else if (msg.arg1 == ConstMXA.MSG_STATUS_ERROR) {
				Bundle b = msg.getData();
				String message = b.getString("ERRORMESSAGE");
				int reason = b.getInt("ERRORCODE");
				this.onError(reason, message);
			}
		}
	}	
	
	
	
	/*
	 * Methods derived from abstract Methods
	 */
	
	public int getId() {
		return this.id;
	}

	public double getPercentage() {
		double percentage = (double)this.bytesTransferred / this.getTotalSize() * 100.0;
		if (percentage > 100.0)
			return 100.0;
		else if (percentage < 0.0)
			return 0.0;
		else
			return percentage;
	}
	
	/*
	 * Getters and Setters
	 */
	
	public int getState() {
		return state;
	}
	
	public FileTransfer getXmppFile() {
		return xmppFile;
	}
	
	public int getBlocksTransferred() {
		return this.blocksTransferred;
	}

	public long getBytesTransferred() {
		return this.bytesTransferred;
	}

	public void setBlocksTransferred(int blocksTransferred) {
		this.blocksTransferred = blocksTransferred;
	}
	
	public void setBytesTransferred(long bytesTransferred) {
		this.bytesTransferred = bytesTransferred;
	}
	
}
