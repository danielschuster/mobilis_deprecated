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

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;
import de.tudresden.inf.rn.mobilis.xmpp.android.Parceller;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentTransferBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.RepositoryItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.RepositoryQueryBean;

public class RepositoryTransfer extends OutgoingTransfer {

	private String repository;
	private RepositoryItemParcel item;
	private String queryPacketId;
	private String expectedContentBroker;
	private String uid;
	
	private IXMPPIQCallback iqCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			RepositoryTransfer.this.handleBean(Parceller.getInstance().convertXMPPIQToBean(iq));
		};
	};

	public RepositoryTransfer(TransferManager manager, int id, 
			String repository, RepositoryItemParcel item, FileTransfer file) {
		super(manager, id, file);
		this.repository = repository;
		this.item = item;
		this.item.owner = null;
		this.item.content = null;
	}
	
	@Override
	protected boolean onReady() {
		final IXMPPService xmppService = this.manager.getService().getXmppService();
		final Messenger xmppMessenger  = this.manager.getService().getXmppMessenger();
		RepositoryQueryBean queryBean = new RepositoryQueryBean();
		queryBean.setType(XMPPBean.TYPE_SET);
		queryBean.setTo(this.repository);
		queryBean.getItems().add(
				Parceller.getInstance().convertRepositoryItemParcelToInfo(this.item)
			);
		ContentTransferBean transferBean = new ContentTransferBean();
		this.queryPacketId = queryBean.getId();
		try {
			Parceller.getInstance().registerXMPPBean(queryBean);
			Parceller.getInstance().registerXMPPBean(transferBean);
			xmppService.registerIQCallback(this.iqCallback, queryBean.getChildElement(), queryBean.getNamespace());
			xmppService.registerIQCallback(this.iqCallback, transferBean.getChildElement(), transferBean.getNamespace());
			xmppService.sendIQ(xmppMessenger, null, this.id, 
					Parceller.getInstance().convertXMPPBeanToIQ(queryBean, true) );
		} catch (RemoteException e) {
			this.onError(-101, "Error asking the repository to store the file (internal error).");
		}
		return true;
	}
	
	public void onIQSent() { /* nothing to do yet */ }
	
	public void onIQAnswered(RepositoryItemInfo rii) {
		// wait for content broker to request transfer initiation
		this.uid = rii.getUid();
		this.expectedContentBroker = rii.getContent();
	}
	
	public boolean onTransferRequested(String uid, String requester) {
		// send out this file to the repository
		boolean success;
		if (this.uid.equals(uid) && this.expectedContentBroker.equals(requester)) {
			this.onItemAnnounced(uid, requester);
			success = true;
		} else {
			this.onError(-105, "Security error when content broker asked to initiate transfer");
			success = false;
		}
		return success;
	}
	
	public void onItemAnnounced(String uid, String contentBroker) {
		ContentItemInfo cii = new ContentItemInfo();
		cii.setDescription(this.xmppFile.description);
		cii.setRepository(this.repository);
		cii.setUid(uid);
		this.xmppFile.description = cii.toXML();
		super.onResourceDiscovered(contentBroker);
	}
	
	@Override
	public void handleMessage(Message msg) {
		if (msg.what == ConstMXA.MSG_SEND_IQ) {
			if (msg.arg1 == ConstMXA.MSG_STATUS_DELIVERED)
				this.onIQSent();
		} else
			super.handleMessage(msg);
	}
	
	public void handleBean(XMPPBean b) throws RemoteException {
		final IXMPPService xmppService = this.manager.getService().getXmppService();
		final Messenger xmppMessenger  = this.manager.getService().getXmppMessenger();
		if ( b instanceof RepositoryQueryBean
				&& b.getId().equals(this.queryPacketId) ) {
			RepositoryQueryBean bb = (RepositoryQueryBean) b;
			if (bb.getType() == XMPPBean.TYPE_RESULT && bb.getItems().size() == 1) {
				this.onIQAnswered(bb.getItems().get(0));
				xmppService.unregisterIQCallback(this.iqCallback, bb.getChildElement(), bb.getNamespace() );
			} else
				this.onError(-104, "Error asking the repository to store the file (repository side error).");
		} else if ( b instanceof ContentTransferBean && b.getType() == XMPPBean.TYPE_GET ) {
			ContentTransferBean bb = (ContentTransferBean) b;
			boolean authorized = this.onTransferRequested(bb.getUid(), bb.getSendTo());
			ContentTransferBean bbAnswer = bb.clone();
			String from = bb.getFrom(); String to = bb.getTo(); 
			bbAnswer.setFrom(to); bbAnswer.setTo(from);
			bbAnswer.setType(XMPPBean.TYPE_RESULT);
			if (authorized)
				try {
					if (this.expectedContentBroker.equals(bb.getSendTo()))
						xmppService.unregisterIQCallback(
								this.iqCallback, bb.getChildElement(), bb.getNamespace() );
					xmppService.sendIQ(
							xmppMessenger, null, this.id,
							Parceller.getInstance().convertXMPPBeanToIQ(bbAnswer, true) );
				} catch (RemoteException e) {
					this.onError(-104, "Error while sending the answer for the file transfer initiation request..");
				}
		}
	}

}
