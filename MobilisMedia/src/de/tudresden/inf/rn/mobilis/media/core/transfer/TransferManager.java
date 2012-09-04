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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.os.RemoteException;
import android.util.SparseArray;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;
import de.tudresden.inf.rn.mobilis.media.services.XMPPConsumerService;
import de.tudresden.inf.rn.mobilis.mxa.services.callbacks.IFileAcceptCallback;
import de.tudresden.inf.rn.mobilis.mxa.services.callbacks.IFileCallback;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;

public class TransferManager extends IFileCallback.Stub {
	
	private SparseArray<OutgoingTransfer> outgoingTransfers;
	private SparseArray<IncomingTransfer> incomingTransfers;
	private List<TransferRequestObserver> fileTransferRequestObservers;
	private int currentTransferId;
	private XMPPConsumerService service;
	
	public TransferManager(XMPPConsumerService service) {
		this.service = service;
		this.outgoingTransfers = new SparseArray<OutgoingTransfer>();
		this.incomingTransfers = new SparseArray<IncomingTransfer>();
		this.fileTransferRequestObservers = Collections.synchronizedList(
				new LinkedList<TransferRequestObserver>() );
		this.currentTransferId = 0;
	}
	
	public XMPPConsumerService getService() {
		return this.service;
	}
	
	public OutgoingTransfer addOutgoingTransfer(FileTransfer file) {
		OutgoingTransfer ot = new OutgoingTransfer(
				this,
				this.currentTransferId,
				file);
		this.outgoingTransfers.append(this.currentTransferId, ot);
		this.currentTransferId++;
		return ot;
	}
	
	public OutgoingTransfer addOutgoingTransfer(String repository,
			RepositoryItemParcel item, FileTransfer file) {
		OutgoingTransfer ot = new RepositoryTransfer(
				this,
				this.currentTransferId,
				repository,
				item,
				file);
		this.outgoingTransfers.append(this.currentTransferId, ot);
		this.currentTransferId++;
		return ot;
	}

	
	public void removeFileTransfer(Transfer mt) {
		int id = mt.getId();
		SparseArray<OutgoingTransfer> ots = this.outgoingTransfers;
		SparseArray<IncomingTransfer> its = this.incomingTransfers;
		if (ots.get(id) != null) ots.remove(id);
		else if (its.get(id) != null) its.remove(id);
	}
	
	public void registerTransferRequestObserver(TransferRequestObserver ftro) {
		this.fileTransferRequestObservers.add(ftro);
	}
	
	public void unregisterTransferRequestObserver(TransferRequestObserver ftro) {
		this.fileTransferRequestObservers.remove(ftro);
	}
	
	public void notifyRequested(Transfer fileTransfer) {
		for (TransferRequestObserver ftro: this.fileTransferRequestObservers)
			ftro.requested(fileTransfer);
	}
	
	public int getNumberOfTransfers() {
		return this.outgoingTransfers.size();
	}
	
	private int[] getFileTransferIDs(final SparseArray<?> transfers) {
		final int size = transfers.size();
		int result[] = new int[size];
		for (int i = 0; i < size; i++)
			result[i] = transfers.keyAt(i);
		return result;
	}
	
	public int[] getOutgoingMediaTransferIDs() {
		return getFileTransferIDs(this.outgoingTransfers);
	}
	
	public int[] getIncomingMediaTransferIDs() {
		return getFileTransferIDs(this.incomingTransfers);
	}
	
	public Transfer getMediaTransferByID(int id) {
		Transfer mt = this.outgoingTransfers.get(id);
		if (mt != null) return mt;
		else return this.incomingTransfers.get(id);
	}
	
	public int getCurrentTransferId() {
		return this.currentTransferId;
	}
	
	public void processFile(int requestKey, FileTransfer file)
			throws RemoteException {
		
	}

	public void processFile(IFileAcceptCallback acceptCallback,
			FileTransfer file, String streamId) throws RemoteException {
		TransferManager owner = TransferManager.this;
		IncomingTransfer it = new IncomingTransfer(
				owner,
				owner.currentTransferId,
				streamId,
				acceptCallback,
				file);
		owner.incomingTransfers.append(it.getId(), it);
		owner.currentTransferId++;
		owner.notifyRequested(it);
	}

}
