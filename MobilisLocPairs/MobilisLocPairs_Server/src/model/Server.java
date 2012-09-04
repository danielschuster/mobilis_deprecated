package model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class Server {
	Collection<RosterEntry> entries;
//	ChatManager chatManager;
	HashSet<Player> playerList = new HashSet<Player>();
//	XMPPConnection connection;
	
	public void run(){
		XMPPConnection.DEBUG_ENABLED = true;
		XMPPConnection connection = new XMPPConnection("127.0.0.1");//141.30.203.90
		try {
			connection.connect();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		try {
			connection.login("server", "7Dj3S");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
//		chatManager = connection.getChatManager();
		
		Roster roster = connection.getRoster();
//		System.out.println(roster.getSubscriptionMode().toString());
		roster.setSubscriptionMode(SubscriptionMode.accept_all);
		entries = roster.getEntries();
		System.out.println("Roster: ");
		for (RosterEntry entry : entries) {
			System.out.println("RosterEntry.getUser(): " + entry.getUser());
			Player player = new Player(entry.getUser(), connection);
			playerList.add(player);
//		    System.out.println(roster.getPresence(entry.getUser()));
//		    System.out.println(roster.getPresenceResource(entry.getUser()));
		}
		
	}
}
