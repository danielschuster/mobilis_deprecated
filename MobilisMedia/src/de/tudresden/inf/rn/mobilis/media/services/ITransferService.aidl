package de.tudresden.inf.rn.mobilis.media.services;

import android.os.Messenger;
import de.tudresden.inf.rn.mobilis.media.parcelables.TransferParcel;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;
import de.tudresden.inf.rn.mobilis.media.parcelables.ConditionParcel;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPMessage;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPPresence;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;



interface ITransferService {

	int startTransferToJid(in FileTransfer file);
	int startTransferToRep(in String repository, in RepositoryItemParcel item, in FileTransfer file);
	
	boolean acceptTransferFromJid(in String filename, in int id);
	boolean denyTransferFromJid(in int id);
	
	int[] getIds(in int direction);
	TransferParcel getTransferParcel(in int id);
	void registerMediaTransferMessenger(in Messenger messenger, in int direction);
	void unregisterMediaTransferMessenger(in Messenger messenger, in int direction);
	
}