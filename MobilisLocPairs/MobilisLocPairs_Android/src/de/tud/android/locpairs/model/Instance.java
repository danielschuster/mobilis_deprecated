package de.tud.android.locpairs.model;

import java.util.Collection;

/**
 * The Class Instance. Holds Informations about running mobilis instances of LocPairs
 */
public class Instance {
	
	/** The instance jid. */
	private String instanceJID;
	
	/** The name of the instance */
	private String name;
	
	/** The name of the player that created the instance. */
	private String opener;
	
	/** The players. */
	private Collection<String> players;
	
	/** The max member count. */
	private int maxMemberCount;
	
	/**
	 * Instantiates a new instance.
	 *
	 * @param instanceJID the instance jid
	 */
	public Instance(String instanceJID){
		this.instanceJID = instanceJID;
	}

	/**
	 * Gets the instance jid.
	 *
	 * @return the instance jid
	 */
	public String getInstanceJID() {
		return instanceJID;
	}

	/**
	 * Sets the instance jid.
	 *
	 * @param instanceJID the new instance jid
	 */
	public void setInstanceJID(String instanceJID) {
		this.instanceJID = instanceJID;
	}

	/**
	 * Gets the opener.
	 *
	 * @return the opener
	 */
	public String getOpener() {
		return opener;
	}

	/**
	 * Sets the opener.
	 *
	 * @param opener the new opener
	 */
	public void setOpener(String opener) {
		this.opener = opener;
	}

	/**
	 * Gets the players.
	 *
	 * @return the players
	 */
	public Collection<String> getPlayers() {
		return players;
	}

	/**
	 * Sets the players.
	 *
	 * @param players the new players
	 */
	public void setPlayers(Collection<String> players) {
		this.players = players;
	}

	/**
	 * Gets the max member count.
	 *
	 * @return the max member count
	 */
	public int getMaxMemberCount() {
		return maxMemberCount;
	}

	/**
	 * Sets the max member count.
	 *
	 * @param maxMemberCount the new max member count
	 */
	public void setMaxMemberCount(int maxMemberCount) {
		this.maxMemberCount = maxMemberCount;
	}

	/**
	 * Gets the instance name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the instance name
	 *
	 * @param name the Name of the instance
	 */
	public void setName(String name) {
		this.name = name;
	}
}
