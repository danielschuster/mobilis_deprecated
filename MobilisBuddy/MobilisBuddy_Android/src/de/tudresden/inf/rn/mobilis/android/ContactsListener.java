package de.tudresden.inf.rn.mobilis.android;

/**
 * Object, which wants to be notified, if a fresh contact list got received from the mobilis server.
 * @author Dirk
 */
public interface ContactsListener {

	public void onContactsUpdated();
	
}
