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

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.core.transfer.IncomingTransfer;
import de.tudresden.inf.rn.mobilis.media.core.transfer.OutgoingTransfer;
import de.tudresden.inf.rn.mobilis.media.core.transfer.RepositoryTransfer;
import de.tudresden.inf.rn.mobilis.media.core.transfer.Transfer;
import de.tudresden.inf.rn.mobilis.media.core.transfer.TransferManager;
import de.tudresden.inf.rn.mobilis.media.core.transfer.TransferObserver;
import de.tudresden.inf.rn.mobilis.media.core.transfer.TransferRequestObserver;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;
import de.tudresden.inf.rn.mobilis.media.parcelables.TransferParcel;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;

public class TransferService extends XMPPConsumerService  {

	public static int PREF_TIMEOUT = 60;
	public static int PREF_BLOCKSIZE = 1*1024;
	public static String PREF_DIRECTORY = "/sdcard/download"; 
	
	private TransferManager transferManager = null;
	
	@Override
	public void onCreate() {
		this.transferManager = new TransferManager(this);
		super.onCreate();
	}
	
	@Override
	public void onXMPPConnect() {
		super.onXMPPConnect();
		try {
			this.xmppService.getFileTransferService().registerFileCallback(this.transferManager);
		} catch (RemoteException e) {
			Log.e(TransferManager.class.getName(), "No connection to XMPP service");
		}
	}

	@Override
	protected Handler newHandler() {
		return new TransferService.ServiceHandler();
	}
	
	@Override
	protected IBinder newBinder() {
		TransferService.ServiceBinder binder = new TransferService.ServiceBinder();
		this.transferManager.registerTransferRequestObserver(binder);
		return binder;
	}

	@Override
	protected void onExecute(Intent intent) {
		if (intent == null || intent.getAction() == null) return;
		String a = intent.getAction(); 
		if ( a.equals(ConstMMedia.intent.SEND_TO_JID)
				|| a.equals(ConstMMedia.intent.SEND_TO_REP) ) {
			Log.i(TransferService.class.getName(), "Initializing Media Transfer");
			String[] paths  = intent.getStringArrayExtra(ConstMMedia.intent.extra.STRA_PATHS);
			Parcelable[] slices = null;
			String[] uids = null;
			if (a.equals(ConstMMedia.intent.SEND_TO_REP)) {
				slices = intent.getParcelableArrayExtra(ConstMMedia.intent.extra.BDLA_SLICES);
				uids = intent.getStringArrayExtra(ConstMMedia.intent.extra.STRA_REPOSITORYITEMS_UIDS);
			}
			String to = intent.getStringExtra(ConstMMedia.intent.extra.STR_TO);
			String description = intent.getStringExtra(ConstMMedia.intent.extra.STR_DESCRIPTION);
			for (int i = 0; i < paths.length; i++) {
				String mimeType = this.getContentResolver().getType( Uri.fromFile(new File(paths[i])) );
				int blockSize = Integer.parseInt(
						this.xmppPreferences.getString("transfer_blocksize",
								String.valueOf(TransferService.PREF_BLOCKSIZE))
					);
				FileTransfer file = new FileTransfer("", to, description, paths[i], mimeType, blockSize, -1); // TODO: from & size is missing?
				try {
					if (a.equals(ConstMMedia.intent.SEND_TO_JID))
						((TransferService.ServiceBinder)this.binder).startTransferToJid(file);
					else if (a.equals(ConstMMedia.intent.SEND_TO_REP)) {
						RepositoryItemParcel item = new RepositoryItemParcel();
						Bundle s = (Bundle)(slices[i]);
						for (String key: s.keySet())
							item.slices.put(key, s.getString(key));
						if (uids != null) item.uid = uids[i];
						((TransferService.ServiceBinder)this.binder).startTransferToRep(to, item, file);
					}
				} catch (RemoteException e) {
					// TODO add message
				}
			}
		}
	}
	
	protected class ServiceHandler extends XMPPConsumerService.ServiceHandler {
		@Override
		public void handleMessage(Message msg) {
			final TransferService owner = TransferService.this;
			super.handleMessage(msg);
			if (msg.arg2 >= 0) {
				Transfer t = owner.transferManager.getMediaTransferByID(msg.arg2);
				if (t != null) t.handleMessage(msg);
			}
		}
	}
	
	private class ServiceBinder extends ITransferService.Stub
			implements TransferObserver, TransferRequestObserver {
		
		private TransferService.ServiceNotifier transferNotifier;
		private Collection<Messenger> incomingMediaTransferMessengers;
		private Collection<Messenger> outgoingMediaTransferMessengers;
		
		public ServiceBinder() {
			this.transferNotifier = new TransferService.ServiceNotifier();
			this.incomingMediaTransferMessengers = new HashSet<Messenger>();
			this.outgoingMediaTransferMessengers = new HashSet<Messenger>();
		}

		public int startTransferToJid(FileTransfer file)
				throws RemoteException {
			final TransferService owner = TransferService.this;
			OutgoingTransfer transfer = owner.transferManager.addOutgoingTransfer(file);
			transfer.registerMediaTransferObserver(this);
			transfer.initiate();
			return transfer.getId();
		}
		
		public int startTransferToRep(String repository, RepositoryItemParcel item, FileTransfer file)
				throws RemoteException {
			final TransferService owner = TransferService.this;
			OutgoingTransfer transfer = owner.transferManager.addOutgoingTransfer(repository, item, file);
			transfer.registerMediaTransferObserver(this);
			transfer.initiate();
			return transfer.getId();
		}

		public TransferParcel getTransferParcel(int id)
				throws RemoteException {
			final TransferService owner = TransferService.this;
			Transfer t = owner.transferManager.getMediaTransferByID(id);
			if (t == null) return null;
			FileTransfer ftp = t.getXmppFile(); 
			TransferParcel tp = new TransferParcel(
					id,
					t.getState(),
					((t instanceof OutgoingTransfer) || (t instanceof RepositoryTransfer) ?
							ConstMMedia.enumeration.DIRECTION_OUT :
							ConstMMedia.enumeration.DIRECTION_IN), 
					t.getBlocksTransferred(),
					t.getBytesTransferred());
			tp.xmppFile = new FileTransfer(
					ftp.from, ftp.to, ftp.description, ftp.path, ftp.mimeType,
					t.getBlockSize(),
					t.getTotalSize()
				);
			return tp;
		}
		
		public int[] getIds(int direction) throws RemoteException {
			final TransferService owner = TransferService.this;
			if (direction == ConstMMedia.enumeration.DIRECTION_IN)
				return owner.transferManager.getIncomingMediaTransferIDs();
			else if (direction == ConstMMedia.enumeration.DIRECTION_OUT)
				return owner.transferManager.getOutgoingMediaTransferIDs();
			else
				return null;
		}
		
		public boolean acceptTransferFromJid(String filename, int id) throws RemoteException {
			final TransferService owner = TransferService.this;
			Transfer t = owner.transferManager.getMediaTransferByID(id);
			if (t == null || !(t instanceof IncomingTransfer))
				return false;
			else {
				SharedPreferences ps = TransferService.this.xmppPreferences; 
				int blockSize    = Integer.parseInt(
						ps.getString("transfer_blocksize", String.valueOf(TransferService.PREF_BLOCKSIZE)
					));
				String directory = ps.getString("transfer_directory", TransferService.PREF_DIRECTORY);
				((IncomingTransfer)t).initiate(directory + "/" + filename, blockSize);
				return true;
			}
		}

		public boolean denyTransferFromJid(int id) throws RemoteException {
			final TransferService owner = TransferService.this;
			Transfer t = owner.transferManager.getMediaTransferByID(id);
			if (t == null || !(t instanceof IncomingTransfer))
				return false;
			else {
				((IncomingTransfer)t).terminate();
				return true;
			}
		}
		
		public void registerMediaTransferMessenger(Messenger messenger, int direction)
				throws RemoteException {
			if (messenger!=null && (direction & ConstMMedia.enumeration.DIRECTION_IN) > 0)
				this.incomingMediaTransferMessengers.add(messenger);
			if (messenger!=null && (direction & ConstMMedia.enumeration.DIRECTION_OUT) > 0)
				this.outgoingMediaTransferMessengers.add(messenger);
		
		}

		public void unregisterMediaTransferMessenger(Messenger messenger, int direction)
				throws RemoteException {
			if (messenger!=null && (direction & ConstMMedia.enumeration.DIRECTION_IN) > 0)
				this.incomingMediaTransferMessengers.remove(messenger);
			if (messenger!=null && (direction & ConstMMedia.enumeration.DIRECTION_OUT) > 0)
				this.outgoingMediaTransferMessengers.remove(messenger);
		}
		
		public void requested(Transfer sender) {
			this.notifyMediaTransferMessengers(sender);
			final Resources resources = TransferService.this.getResources();
			String from = sender.getXmppFile().from;
			String name = sender.getXmppFile().path;
			sender.registerMediaTransferObserver(this);
			this.transferNotifier.addEventNotification(
					resources.getString(R.string.notification_transfer_incoming_title),
					String.format(
						resources.getString(R.string.notification_transfer_incoming_content),
						from),
					String.format(
						resources.getString(R.string.notification_transfer_incoming_ticker),
						from, name),
					sender.getId(),
					true);
		}
		
		public void initiated(Transfer sender) {
			Log.i("MMedia measurement", "MMedia\tinitiated\t"+System.currentTimeMillis());			
			this.notifyMediaTransferMessengers(sender);
			Log.d("MediaTransferBinder", "Transfer initiated: " + String.valueOf(sender.getId()));
		}
		
		public void negotiation(Transfer sender) {
			Log.i("MMedia measurement", "MMedia\tnegotiated\t"+System.currentTimeMillis());			
			final TransferService owner = TransferService.this;
			this.notifyMediaTransferMessengers(sender);
			Log.d("MediaTransferBinder", "Transfer negotiated: " + String.valueOf(sender.getId()));
			final Resources resources = TransferService.this.getResources();
			String contentText = "";
			double progressBarState = 0;
			int number = owner.transferManager.getNumberOfTransfers();
			if (number > 1) {
				contentText = String.format(
						resources.getString(R.string.notification_transfer_ongoing_more),
						number);
				progressBarState = ServiceNotifier.PROGRESS_BAR_HIDDEN; 
			} else {
				contentText = "";
				progressBarState = ServiceNotifier.PROGRESS_BAR_INDETERMINATE;
			}
			this.transferNotifier.updateServiceNotification(
					resources.getString(R.string.notification_transfer_ongoing_title),
					contentText,
					String.format(resources.getString(R.string.notification_transfer_ongoing_ticker),
							sender.getXmppFile().path,
							sender.getXmppFile().to),
					progressBarState);
		}

		public void blockTransferred(Transfer sender, long block) {
			Log.i("MMedia measurement", "MMedia\t"+String.format("%05d", block)+"\t"+System.currentTimeMillis());			
			final TransferService owner = TransferService.this;
			this.notifyMediaTransferMessengers(sender);
			Log.d( "MediaTransferBinder", "Transfer block transferred: " + String.valueOf(sender.getId()) + " - " + String.valueOf(sender.getPercentage()) + "%");
			final Resources resources = TransferService.this.getResources();
			String contentText = "";
			double progressBarState = 0;
			int number = owner.transferManager.getNumberOfTransfers();
			if (number > 1) {
				contentText = String.format(
						resources.getString(R.string.notification_transfer_ongoing_more),
						number);
				progressBarState = ServiceNotifier.PROGRESS_BAR_HIDDEN; 
			} else {
				contentText = resources.getString(R.string.notification_transfer_ongoing_one);
				progressBarState = sender.getPercentage();
			}
			this.transferNotifier.updateServiceNotification(
					resources.getString(R.string.notification_transfer_ongoing_title),
					contentText,
					null,
					progressBarState);

		}
		
		public void failed(Transfer sender, int reason, String message) {
			this.notifyMediaTransferMessengers(sender);
			Log.d("MediaTransferBinder", "Transfer failed: " + String.valueOf(sender.getId()));
			final Resources resources = TransferService.this.getResources();
			this.transferNotifier.addEventNotification(
					resources.getString(R.string.notification_transfer_failed_title),
					resources.getString(R.string.notification_transfer_failed_status),
					resources.getString(R.string.notification_transfer_failed_ticker),
					sender.getId(),
					true);
			this.completed(false);
		}

		public void finished(Transfer sender) {
			Log.i("MMedia measurement", "MMedia\tdone\t"+System.currentTimeMillis());			
			final TransferService owner = TransferService.this;
			this.notifyMediaTransferMessengers(sender);
			Log.d("MediaTransferBinder", "Transfer finished: " + String.valueOf(sender.getId()));
			final Resources resources = TransferService.this.getResources();
			if (owner.transferManager.getNumberOfTransfers() <= 1) 
				this.transferNotifier.addEventNotification(
						resources.getString(R.string.notification_transfer_finished_title),
						resources.getString(R.string.notification_transfer_finished_status),
						resources.getString(R.string.notification_transfer_finished_ticker),
						sender.getId(),
						false);
			this.completed(true);
		}
		
		private void completed(boolean successful) {
			final TransferService owner = TransferService.this;
			final Resources resources = owner.getResources();
			String contentText = "";
			String titleText = "";
			int number = owner.transferManager.getNumberOfTransfers();
			if (number > 1) {
				titleText = resources.getString(R.string.notification_transfer_ongoing_title);
				contentText = String.format(
						resources.getString(R.string.notification_transfer_ongoing_more),
						number-1);
				this.transferNotifier.updateServiceNotification(titleText, contentText, "",
						ServiceNotifier.PROGRESS_BAR_HIDDEN);
			} else 
				this.transferNotifier.cancelServiceNotification();
		}

		public void notifyMediaTransferMessengers(Transfer sender) {
			Collection<Messenger> messengers = null;
			if ((sender instanceof OutgoingTransfer) || (sender instanceof RepositoryTransfer))
				messengers = this.outgoingMediaTransferMessengers;
			else if (sender instanceof IncomingTransfer)
				messengers = this.incomingMediaTransferMessengers;
			// prepare message
			int id = sender.getId();
			Message msg = Message.obtain();
			msg.what = ConstMMedia.message.WHAT_TRANSFERSTATECHANGED;
			msg.arg1 = id;
			try {
				msg.getData().putParcelable(
						ConstMMedia.message.data.PAR_TRANSFER,
						this.getTransferParcel(id));
			} catch (RemoteException e) {}
			// send out message to receivers
			for (Messenger m: messengers) {
				try {
					m.send(Message.obtain(msg));
				} catch (RemoteException e) {}
			}
		}

	}
	
	private class ServiceNotifier {
		
		private final static double PROGRESS_BAR_HIDDEN = -2.0;
		private final static double PROGRESS_BAR_INDETERMINATE = -1.0;
		private final static int SERVICE_NOTIFICATION_ID = -1;
		private NotificationManager notificationManager;
		private Notification        serviceNotification;
		private PendingIntent       serviceNotificationIntent;
		private RemoteViews         serviceNotificationView;

		public ServiceNotifier() {
			TransferService owner = TransferService.this;
			Resources resources = owner.getResources();
			// Remote View
			RemoteViews notificationView = new RemoteViews(owner.getPackageName(), R.layout.media_transfer_notification);
			notificationView.setImageViewResource(R.id.icon, R.drawable.notification_xmpp_service);
			// Notification
			Notification notification = new Notification(
					R.drawable.notification_xmpp_service,
					resources.getString(R.string.notification_transfer_ongoing_ticker),
					(new Date()).getTime()
				);
			notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			notification.contentView = notificationView;
			// Pending Intent
			PendingIntent notificationIntent = PendingIntent.getActivity(
					owner,
					TransferService.ServiceNotifier.SERVICE_NOTIFICATION_ID,
					new Intent(ConstMMedia.intent.CHECK_TRANSFER),
					0);
			this.notificationManager = (NotificationManager) owner.getSystemService(NOTIFICATION_SERVICE);
			this.serviceNotification = notification;
			this.serviceNotificationView   = notificationView;
			this.serviceNotificationIntent = notificationIntent;
		}
		
		public void cancelServiceNotification() {
			this.notificationManager.cancel(TransferService.ServiceNotifier.SERVICE_NOTIFICATION_ID);
			
		}

		public void updateServiceNotification(String titleText, String contentText, String tickerText, double progress) {
			Notification  n = this.serviceNotification;
			RemoteViews   v = this.serviceNotificationView;
			PendingIntent i = this.serviceNotificationIntent;
			if (progress == TransferService.ServiceNotifier.PROGRESS_BAR_HIDDEN) {
				n.contentView = null;
				n.setLatestEventInfo(TransferService.this, titleText, contentText, i);
			} else {
				v.setProgressBar(R.id.progress, 100, (int)progress,
						(progress==TransferService.ServiceNotifier.PROGRESS_BAR_INDETERMINATE));
				v.setTextViewText(R.id.percentage,
						(progress>TransferService.ServiceNotifier.PROGRESS_BAR_INDETERMINATE ? 
								String.valueOf((int)progress)+"%" : "") );
				v.setTextViewText(R.id.title, titleText);
				n.contentView = v;
				n.contentIntent = i;
			}
			n.tickerText = tickerText;
			this.notificationManager.notify(-1, n);
			
		}
		
		public void addEventNotification(String titleText, String contentText, String tickerText, int requestCode, boolean important) {
			
			TransferService owner = TransferService.this;
			Notification notification = new Notification(
					(important ? R.drawable.notification_xmpp_event : R.drawable.notification_xmpp_service),
					tickerText,
					(new Date()).getTime());
			notification.defaults = Notification.DEFAULT_SOUND;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			PendingIntent intent = PendingIntent.getActivity(
					owner,
					requestCode,
					new Intent(ConstMMedia.intent.CHECK_TRANSFER),
					0);
			notification.setLatestEventInfo(owner, titleText, contentText, intent);
			this.notificationManager.notify(requestCode, notification);
			
		}
		
	}

}
