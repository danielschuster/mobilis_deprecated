
package de.tud.android.locpairs.model;

import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;

/**
 * The Class Player.
 */
public class Player {
	
	/** The playername. */
	private String playername;
	
	/** The player id. */
	private String playerID;
	
	/** The position. */
	private GeoPosition position;
	
	/** The team. */
	private Team team;
	//zeigt ob Spieler bereit ist
	/** The state. */
	private boolean state;
	
	/**
	 * Instantiates a new player.
	 *
	 * @param playername the playername
	 * @param playerID the player id
	 * @param position the position
	 */
	public Player(String playername, String playerID, GeoPosition position){
		this.playername = playername;
		this.playerID = playerID;
		this.position = position;
	}
	
	/**
	 * Checks if is state.
	 *
	 * @return true, if is state
	 */
	public boolean isState() {
		return state;
	}

	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(boolean state) {
		this.state = state;
	}

	/**
	 * Instantiates a new player.
	 *
	 * @param playername the playername
	 * @param playerID the player id
	 */
	public Player(String playername, String playerID){
		this.playername = playername;
		this.playerID = playerID;
		this.position = new GeoPosition(0,0,0);
	}

	/**
	 * Gets the playername.
	 *
	 * @return the playername
	 */
	public String getPlayername() {
		return playername;
	}

	/**
	 * Sets the playername.
	 *
	 * @param playername the new playername
	 */
	public void setPlayername(String playername) {
		this.playername = playername;
	}

	/**
	 * Gets the player id.
	 *
	 * @return the player id
	 */
	public String getPlayerID() {
		return playerID;
	}

	/**
	 * Sets the player id.
	 *
	 * @param playerID the new player id
	 */
	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	/**
	 * Gets the position.
	 *
	 * @return the position
	 */
	public GeoPosition getPosition() {
		return position;
	}

	/**
	 * Sets the position.
	 *
	 * @param position the new position
	 */
	public void setPosition(GeoPosition position) {
		this.position = position;
	}

	/**
	 * Gets the team.
	 *
	 * @return the team
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * Sets the team.
	 *
	 * @param team the new team
	 */
	public void setTeam(Team team) {
		if(this.team != null) this.team.removePlayer(this);
		team.addPlayer(this);
		this.team = team;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("--- Spieler ---").append("\n");
		sb.append("Playername: ").append(playername).append("\n");
		sb.append("PlayerID: ").append(playerID).append("\n");
		sb.append("Position: ").append(position).append("\n");
		if(team != null) sb.append("TeamID: ").append(team.getTeamID()).append("\n");
		return sb.toString();
	}
}
