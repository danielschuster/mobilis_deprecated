package de.tudresden.inf.rn.mobilis.media.services;

import android.os.Messenger;
import de.tudresden.inf.rn.mobilis.media.parcelables.ConditionParcel;

interface IRepositoryService {

	void query(in String repository, in ConditionParcel condition, in Messenger resultMessenger, in int resultCode);
	void delete(in String repository, in String[] uids, in Messenger resultMessenger, in int resultCode);
	void discover(in String serverJid, in Messenger resultMessenger, in int resultCode);
	void transfer(in String repository, in String content, in String uid, in Messenger resultMessenger, in int resultCode);

}