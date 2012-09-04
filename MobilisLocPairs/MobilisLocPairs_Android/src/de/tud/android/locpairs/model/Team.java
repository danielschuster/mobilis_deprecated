
package de.tud.android.locpairs.model;

import java.util.HashMap;

/**
 * The Class Team.
 */
public class Team {
	
	/** The players. */
	private HashMap<String,Player> players;
	
	/** The team id. */
	private int teamID = 0;
	
	/**
	 * Instantiates a new team.
	 *
	 * @param teamID the team id
	 */
	public Team(int teamID){
		this.players = new HashMap<String,Player>();
		this.teamID = teamID;	
	}
	
	/**
	 * Gets the team id.
	 *
	 * @return the team id
	 */
	public int getTeamID() {
		return teamID;
	}

	/**
	 * Adds the player.
	 *
	 * @param player the player
	 */
	public void addPlayer(Player player){
		this.players.put(player.getPlayerID(),player);
	}
	
	/**
	 * Removes the player.
	 *
	 * @param player the player
	 */
	public void removePlayer(Player player){
		this.players.remove(player.getPlayerID());
	}
	
	/**
	 * Gets the players.
	 *
	 * @return the players
	 */
	public HashMap<String,Player> getPlayers() {
		return players;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("--- Team ---").append("\n");
		sb.append("TeamID: ").append(teamID).append("\n");
		
		for (Player player:players.values()){
			sb.append("    ").append(player).append("\n").append("\n");;
		}
		
		return sb.toString();
	}
}
