package de.tudresden.inf.rn.mobilis.android.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.ContactsListener;
import de.tudresden.inf.rn.mobilis.android.R;
import de.tudresden.inf.rn.mobilis.android.XMPPUtil;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.android.util.Util;
import de.tudresden.inf.rn.mobilis.xmpp.packet.BuddylistIQ;

public class BuddyListService {
    
    private static final String TAG = "BuddyListService";
    private static final String ADDRESSBOOK_INTEGRATION_SERVICE = "addressbook";
    private static final String ROSTER_INTEGRATION_SERVICE = "roster";
    private SessionService sessionService;
    private XMPPConnection connection;
    private long timeout;
    private Collection<String> buddies;
    
	public void initialize(XMPPConnection connection) {
        this.connection = connection;
        this.timeout = SessionService.getInstance().getPreferences().getLong(
                "pref_xmpp_timeout", 20000);
        this.sessionService = SessionService.getInstance();
        this.buddies = new HashSet<String>();
    }
    
    protected XMPPConnection getConnection() {
        return this.connection;
    }

    public void publishPhoneContacts() {
        sessionService.getInfoViewer().showProgress(sessionService.getContext().getResources().
                getString(R.string.infobar_publishphonecontacts));
        Collection<String> phoneContacts = fetchPhoneContacts();
        Log.v(TAG, "Fetched phone contacts:" + phoneContacts.toString());
        sendBuddyList(phoneContacts, ADDRESSBOOK_INTEGRATION_SERVICE);
        sessionService.getInfoViewer().showInfo(sessionService.getContext().getResources().
                getString(R.string.infobar_publishedphonecontacts));

        // testing: set contacts on roster
        /*try {
            Roster roster = connection.getRoster();
            for (String contact : phoneContacts) {
                roster.createEntry(contact, contact, null);
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }*/
    }

    public void publishRosterContacts() {
    	Thread t = new Thread(new Runnable() {
    		public void run() {
    			sessionService.getInfoViewer().showProgress(sessionService.getContext().getResources().
    					getString(R.string.infobar_publishrostercontacts));
    			Collection<String> rosterContacts = fetchRosterContacts();
    			Log.v(TAG, "Fetched roster contacts:" + rosterContacts.toString());
    			sendBuddyList(rosterContacts, ROSTER_INTEGRATION_SERVICE);
    			sessionService.getInfoViewer().showInfo(sessionService.getContext().getResources().
    					getString(R.string.infobar_publishedrostercontacts));
    		}
    	});
    	t.setName("PublishRosterThread");
    	t.start();
    }
    
    private Collection<String> fetchPhoneContacts() {
        Cursor contactCursor = queryContacts();
        return getContactSet(contactCursor);
    }

    private Cursor queryContacts() {
        // An array specifying which columns to return. 
        String[] projection = new String[] {
            People._ID,
            People.NAME,
        };

        // Get the base URI for People table in Contacts content provider.
        // i.e. content://contacts/people/
        Uri mContacts = People.CONTENT_URI;  
               
        // Best way to retrieve a query; returns a managed query. 
        Cursor managedCursor = SessionService.getInstance().getCurrentActivity().managedQuery( 
                                mContacts,
                                projection, // Which columns to return
                                null,       // WHERE clause; which rows to return (all rows)
                                null,       // WHERE clause selection arguments (none)
                                People.NAME + " ASC"); // Order-by clause (ascending by name)
        return managedCursor;
    }

    private Collection<String> getContactSet(Cursor contactCursor) {
        Collection<String> contacts = new HashSet<String>();
        if (contactCursor.moveToFirst()) {
            
        	// FIXME read out phone numbers from contacts in phone book as identities
            String name;
            int nameColumn = contactCursor.getColumnIndex(People.NAME); 
            String service = SessionService.getInstance().getPreferences().getString(
                    "pref_service", null);
            
            // walk through the contacts and build a set of their jabber-IDs
            do {
                // Get the field values
                name = contactCursor.getString(nameColumn).toLowerCase().replace(" ", "");
                contacts.add(name);
            } while (contactCursor.moveToNext());
        }
        return contacts;
    }
    
    private Collection<String> fetchRosterContacts() {
        Roster roster = connection.getRoster();
        Collection<String> rosterContacts = new HashSet<String>();
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            rosterContacts.add(entry.getUser());
        }
        return rosterContacts;
    }
    
    private void sendBuddyList(Collection<String> contactIdentities, String integrationService) {
        BuddylistIQ bIQ = new BuddylistIQ();
        bIQ.setFrom(connection.getUser());
        bIQ.setTo(SessionService.getInstance().getAndroidbuddyAgent());
        bIQ.setNetwork(integrationService);
        // FIXME read out own phone number as identity!
        bIQ.setIdentity(XMPPUtil.jidWithoutRessource(sessionService.getConnection().getUser()));
        bIQ.setType(IQ.Type.SET);
        bIQ.setBuddies(contactIdentities);
        connection.sendPacket(bIQ);
        Log.d(TAG, "Sent packet: BuddylistIQ (SET)");
    }
    
    public void fetchContactsFromServer(final ContactsListener listener) {
        if (SessionService.getInstance().getSocialNetworkManagementService().
                isAuthenticated(Const.MOBILIS)) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    BuddylistIQ bIQ = new BuddylistIQ();
                    bIQ.setFrom(connection.getUser());
                    bIQ.setTo(SessionService.getInstance().getAndroidbuddyAgent());
                    //bIQ.setNetwork(integrationService);
                    bIQ.setType(IQ.Type.GET);
                    // create PacketCollector for accepting the response 
                    PacketCollector packetCollector = connection.createPacketCollector(new PacketFilter() {
                        public boolean accept(Packet p) {
                            if (! (p instanceof BuddylistIQ) )                return false;
                            if (! ((IQ)p).getType().equals(IQ.Type.RESULT) )  return false;
                            return true;
                        }
                    }
                    );
                    Log.d(TAG, "Sent packet: BuddylistIQ (GET)");
                    sessionService.getInfoViewer().showProgress(sessionService.getContext().getResources().
                            getString(R.string.infobar_fetchbuddylist));
                    connection.sendPacket(bIQ);
                    BuddylistIQ buddyListIQ = (BuddylistIQ) packetCollector.nextResult(timeout);
                    // wait for response
                    if (buddyListIQ == null) {
                        Log.w(TAG, "Timeout waiting for packet: BuddylistIQ (RESULT)");
                        sessionService.getInfoViewer().showWarning(sessionService.getContext().getResources().
                                getString(R.string.infobar_fetchbuddylist_failed));
                    } else {
                        Log.d(TAG, "Received packet: BuddylistIQ (RESULT)");
                        sessionService.getInfoViewer().showInfo(sessionService.getContext().getResources().
                                getString(R.string.infobar_fetchbuddylist_ok));
                        buddies = buddyListIQ.getBuddies();
                        listener.onContactsUpdated();
                    }
                    // tell PacketCollector that we're done here.
                    packetCollector.cancel();
                    Log.v(TAG, "Fetched contacts from server:" + buddies.toString());
                }
            });
            t.setName("FetchContactsThread");
            t.start();
        }
    }

    public Collection<String> getBuddies() {
		return buddies;
	}
    
    public ArrayList<String> getBuddyList() {
    	return new ArrayList<String>(buddies);
    }
    
    /**
     * Returns the buddies, which are at the provided list and on the user's roster.
     * @return ArrayList<String>
     */
    public ArrayList<String> getBuddiesOnRosterAndList(List<String> list) {
        Roster roster = connection.getRoster();
        ArrayList<String> matches = new ArrayList<String>();
        for (String user : list) {           
            String buddyWithoutRessource = user.replaceAll("/.*", "");
            final boolean hasRessource = roster.contains(user);
            final boolean hasBuddy     = roster.contains(buddyWithoutRessource);
            if (hasRessource || hasBuddy)  matches.add(user);
        }
        return matches;
    }
    
    public void addToRoster(String buddy){
    	Roster roster = connection.getRoster();
    	//Roster r = SessionService.getInstance().getConnection().getRoster();
    	if (!roster.contains(buddy)){
    	try {
			roster.createEntry(buddy, buddy, null);
			sendBuddyList(fetchRosterContacts(), ROSTER_INTEGRATION_SERVICE);
			Log.v(TAG, "Succesfully added buddy "+ buddy +" to roster");
			
    	} catch (XMPPException e) {
			// TODO Auto-generated catch block
			Log.w(TAG, "Adding buddy " + buddy + " to roster failed!");
			e.printStackTrace();
		}
    	}else {
    		Log.w(TAG, "Buddy "+buddy+" is already on the roster!");
    	}
    }
    
    public void deleteFromRoster(String buddy){
    	Roster roster = connection.getRoster();
        String buddyWithoutRessource = buddy.replaceAll("/.*", "");
        final boolean hasRessource = roster.contains(buddy);
        final boolean hasBuddy     = roster.contains(buddyWithoutRessource);   	
    	try {
            if (hasRessource) roster.removeEntry(roster.getEntry(buddy));
            if (hasBuddy) roster.removeEntry(roster.getEntry(buddyWithoutRessource));
			sendBuddyList(fetchRosterContacts(), ROSTER_INTEGRATION_SERVICE);
			Log.v(TAG, "Succesfully removed buddy "+buddy+" from Roster");
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			Log.w(TAG, "Removing buddy " + buddy + " from Roster failed!");
			e.printStackTrace();
		}
    }
}
