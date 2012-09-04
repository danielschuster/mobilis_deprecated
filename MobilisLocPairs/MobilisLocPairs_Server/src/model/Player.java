package model;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.PacketFilter;


public class Player {
	
	private String name;
	private myListener listener;
	private PacketFilter filter;
	private XMPPConnection connection;
	
	public Player (String name, XMPPConnection connection){
		this.name = name;
		this.connection = connection;
		this.filter = new FromContainsFilter(this.name);
//		this.filter = new AndFilter(new IQTypeFilter(IQ.Type.GET), new FromContainsFilter(this.name));
		this.listener = new myListener(this);
//		this.connection.addPacketListener(listener, this.filter);
		this.connection.addConnectionListener(listener);
	}
	
	public boolean processPackage(String xmlString){
		// TODO here the player rules are implemented
		return true;
	}
}
