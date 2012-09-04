
package de.tud.android.locpairs.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;

/**
 * The central Game Class - stores game informations, implemented as Singleton.
 */

public class Game {
	
	/** The instance. */
	private static Game instance;
	
	/** The game instances currently avaible on mobilis server. */
	private ArrayList<Instance> instances;
	
	/** The teams in current game. */
	private HashMap<Integer, Team> teams;
	
	/** The players in current game. */
	private HashMap<String,Player> players;
	
	/** The pairs of cards in current game. */
	private HashMap<String,Pair> pairs;

	/** The currently active round. */
	private Round activeRound;
	
	/** The round that starts next. */
	private Round nextRound;
	
	/** The currently active team. */
	private Team activeTeam;
	
	/** The start time of the current game. */
	private String startTime;
	
	/** The end time of the current game. */
	private String endTime;
	
	/** The game xmpp id. */
	private String gameID;
	
	/** The points for current game. */
	private Map<Integer,Long> points;
	
	/** The all time highscore. */
	private Map<String,Long> highscore;
	
	/** The model representation of the client player. */
	private Player clientPlayer;
	
	/** Last recieved "go there" position */
	private GeoPosition goThere;

	/** Flag that indicates if game is running. */
	private boolean isRunning;
	
	/** Flag that indicates if client is connected */
	private boolean isConnected;
	
	/** Flag that indicates if model is completely downloaded. */
	private boolean isModelDownloaded;
	
	/** The is re connect. */
	private boolean isReConnect;
	
	/**
	 * Instantiates a new game.
	 */
	private Game(){
		reset();
		clientPlayer = new Player("Alpha",null);
		isModelDownloaded=false;
	}
	
	/**
	 * Gets the single instance of Game.
	 *
	 * @return single instance of Game
	 */
	public static Game getInstance(){
		if(instance == null){
			instance = new Game();
		}
		return instance;
	}
	
	/**
	 * Gets the client player.
	 *
	 * @return the client player
	 */
	public Player getClientPlayer() {
		return clientPlayer;
	}
	
	/**
	 * Adds the player.
	 *
	 * @param player the player
	 */
	public void addPlayer(Player player){
		players.put(player.getPlayerID(), player);
	}
	
	/**
	 * Adds the team.
	 *
	 * @param team the team
	 */
	public void addTeam(Team team){
		teams.put(team.getTeamID(),team);
	}

	/**
	 * Adds the pair.
	 *
	 * @param pair the pair
	 */
	public void addPair(Pair pair){
		pairs.put(pair.getPairID(),pair);
	}
	
	/**
	 * Removes the team.
	 *
	 * @param team the team
	 */
	public void removeTeam(Team team){
		teams.remove(team.getTeamID());
	}
	
	/**
	 * Removes the pair.
	 *
	 * @param pair the pair
	 */
	public void removePair(Pair pair){
		pairs.remove(pair.getPairID());
	}
	
	/**
	 * Gets the teams.
	 *
	 * @return the teams
	 */
	public HashMap<Integer, Team> getTeams() {
		return teams;
	}

	/**
	 * Sets the teams.
	 *
	 * @param teams the teams
	 */
	public void setTeams(HashMap<Integer, Team> teams) {
		this.teams = teams;
	}
	
	/**
	 * Removes the player.
	 *
	 * @param player the player
	 */
	public void removePlayer(Player player){
		players.remove(player.getPlayerID());
	}
	
	/**
	 * Gets the pairs.
	 *
	 * @return the pairs
	 */
	public HashMap<String,Pair> getPairs() {
		return pairs;
	}

	/**
	 * Sets the pairs.
	 *
	 * @param pairs the pairs
	 */
	public void setPairs(HashMap<String,Pair> pairs) {
		this.pairs = pairs;
	}

	/**
	 * Gets the active round.
	 *
	 * @return the active round
	 */
	public Round getActiveRound() {
		return activeRound;
	}

	/**
	 * Sets the active round.
	 *
	 * @param activeRound the new active round
	 */
	public void setActiveRound(Round activeRound) {
		this.activeRound = activeRound;
	}
	
	/**
	 * Gets the next round.
	 *
	 * @return the next round
	 */
	public Round getNextRound() {
		return nextRound;
	}

	/**
	 * Sets the next round.
	 *
	 * @param activeRound the new next round
	 */
	public void setNextRound(Round activeRound) {
		this.nextRound = activeRound;
	}

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time.
	 *
	 * @param startTime the new start time
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time.
	 *
	 * @param endTime the new end time
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets the points.
	 *
	 * @return the points
	 */
	public Map<Integer, Long> getPoints() {
		return points;
	}

	/**
	 * Sets the points.
	 *
	 * @param points the points
	 */
	public void setPoints(Map<Integer, Long> points) {
		this.points = points;
	}

	/**
	 * Gets the highscore.
	 *
	 * @return the highscore
	 */
	public Map<String, Long> getHighscore() {
		return highscore;
	}

	/**
	 * Sets the highscore.
	 *
	 * @param highscore the highscore
	 */
	public void setHighscore(Map<String, Long> highscore) {
		this.highscore = highscore;
	}
	
	/**
	 * Reset.
	 */
	public void reset() {
		if(instances == null) instances = new ArrayList<Instance>();
			else instances.clear();
		if(players == null)players = new HashMap<String,Player>();
			else players.clear();
		if (teams == null) teams = new HashMap<Integer,Team>();
			else teams.clear();
		if (pairs == null) pairs = new HashMap<String,Pair>();
			else pairs.clear();
		if (points == null) points = new HashMap<Integer,Long>();
			else points.clear();
		if (highscore == null) highscore = new HashMap<String,Long>();
			else highscore.clear();
		
		activeRound = null;
		nextRound = null;
		isRunning =false;
		isConnected=false;
		isReConnect=false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("--- Spiel ---").append("\n");
		sb.append("GameID: ").append(gameID).append("\n");
		if(activeRound != null && activeRound.isActiveTeam()) sb.append("Eigenes Team ist aktiv.").append("\n");
		if(activeRound != null) sb.append("Aktive Runde: ").append(activeRound.getStartTime()).append("\n");
		sb.append("Startzeit: ").append(startTime).append("\n");
		sb.append("Endzeit: ").append(endTime).append("\n");
		
		for(Player player: players.values()){
			sb.append(player).append("\n").append("\n");
		}
		
		for(Pair pair: pairs.values()){
			sb.append(pair).append("\n").append("\n");
		}

//		for(Round round: rounds){
//			sb.append("    ").append(round).append("\n").append("\n");
//		}
		
		sb.append("--- Punkte ---").append("\n");
		for(int teamId: points.keySet()){
			if(teams.get(teamId) != null){
				sb.append(teamId).append(" : ").append(points.get(teamId)).append("\n");
			}
		}
		
		sb.append("--- Highscores ---").append("\n");
		for(String team: highscore.keySet()){
			if(highscore.get(team)!= null){
				sb.append(team).append(" : ").append(highscore.get(team)).append("\n");
			}
		}
		
		return sb.toString();
	}

	/**
	 * Gets the players.
	 *
	 * @return the players
	 */
	public HashMap<String, Player> getPlayers() {
		return players;
	}

	/**
	 * Sets the players.
	 *
	 * @param players the players
	 */
	public void setPlayers(HashMap<String, Player> players) {
		this.players = players;
	}

	/**
	 * Gets the cards.
	 *
	 * @return the cards
	 */
	public Map<String,Card> getCards() {
		HashMap<String,Card> cards = new HashMap<String, Card>();
		for(Pair pair:getPairs().values()) {
			for(Card card:pair.getCards().values()) {
				cards.put(card.getBarcode(), card);
			}
		}		
		return cards;
	}

	/**
	 * Checks if is running.
	 *
	 * @return true, if is running
	 */
	public boolean isRunning() {
		Log.v("Game", "isRunning? :" + isRunning);
		return isRunning;
	}
	
	/**
	 * Checks if is connected.
	 *
	 * @return true, if is connected
	 */
	public boolean isConnected() {
		Log.v("Server", "isConnected? :" + isConnected);
		return isConnected;
	}
	
	/**
	 * Sets the connected.
	 *
	 * @param connected the new connected
	 */
	public void setConnected(boolean connected) {
		this.isConnected = connected;
	}
	
	/**
	 * Start game.
	 */
	public void startGame() {
		isRunning = true;
		Log.v("Game", "isRunning set to true");		
	}
	
	/**
	 * Stop game.
	 */
	public void stopGame() {
		isRunning = false;
	}

	/**
	 * Sets the model downloaded.
	 *
	 * @param isModelDownloaded the new model downloaded
	 */
	public void setModelDownloaded(boolean isModelDownloaded) {
		this.isModelDownloaded = isModelDownloaded;
	}
	
	/**
	 * Checks if is model downloaded.
	 *
	 * @return true, if is model downloaded
	 */
	public boolean isModelDownloaded() {
		Log.v("Model", "isModelDownloaded? :" + isConnected);
		return isConnected;
	}
	
	/**
	 * Checks if is re connect.
	 *
	 * @return true, if is re connect
	 */
	public boolean isReConnect() {
		return isReConnect;
	}

	/**
	 * Sets the re connect.
	 *
	 * @param isReConnect the new re connect
	 */
	public void setReConnect(boolean isReConnect) {
		this.isReConnect = isReConnect;
	}
	
	/**
	 * Gets the active team.
	 *
	 * @return the active team
	 */
	public Team getActiveTeam() {
		return activeTeam;
	}

	/**
	 * Sets the active team.
	 *
	 * @param activeTeam the new active team
	 */
	public void setActiveTeam(Team activeTeam) {
		this.activeTeam = activeTeam;
	}
	
	/**
	 * Gets the game id.
	 *
	 * @return the game id
	 */
	public String getGameID() {
		return gameID;
	}

	/**
	 * Sets the game id.
	 *
	 * @param gameID the new game id
	 */
	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	//this is the player that is represented by your own device
	/**
	 * Sets the client player.
	 *
	 * @param clientPlayer the new client player
	 */
	public void setClientPlayer(Player clientPlayer) {
		this.clientPlayer = clientPlayer;
	}
	
	/**
	 * Gets the go there.
	 *
	 * @return the go there
	 */
	public GeoPosition getGoThere() {
		return goThere;
	}

	/**
	 * Sets the go there.
	 *
	 * @param goThere the new go there
	 */
	public void setGoThere(GeoPosition goThere) {
		this.goThere = goThere;
	}

	/**
	 * Gets the instances.
	 *
	 * @return the instances
	 */
	public ArrayList<Instance> getInstances() {
		return instances;
	}

	/**
	 * Sets the instances.
	 *
	 * @param instances the instances
	 */
	public void setInstances(ArrayList<Instance> instances) {
		this.instances = instances;
	}
}