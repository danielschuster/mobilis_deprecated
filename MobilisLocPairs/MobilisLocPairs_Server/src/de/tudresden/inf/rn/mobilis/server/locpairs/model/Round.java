package de.tudresden.inf.rn.mobilis.server.locpairs.model;

import java.util.Date;

import de.tudresden.inf.rn.mobilis.server.locpairs.model.Game;
import de.tudresden.inf.rn.mobilis.server.locpairs.model.LocPairsServerTime;
import de.tudresden.inf.rn.mobilis.server.locpairs.model.Player;

/**
 * The Class Round represents a round in the game locpairs 
 * with its start time, the roundnumber and the uncovered cards 
 * and the player uncovered these cards
 * 
 *  @author Reik Mueller
 */
public class Round {

	private Date startTime = null;
	private int duration = 30000;
	private int number = 0;
	private String uncoveredCard1 = null;
	private Player player1 = null;
	private String uncoveredCard2 = null;
	private Player player2 = null;
	private Team activeTeam = null;

	public Team getActiveTeam() {
		return activeTeam;
	}

	public void setActiveTeam(Team activeTeam) {
		this.activeTeam = activeTeam;
	}

	/**
	 * Instantiates a new round.
	 *
	 * @param observer the observer
	 */
	public Round(Game observer) {
	}
	
	/**
	 * Clear. Sets the uncovered cards to null.
	 */
	public void clear() {
		uncoveredCard1 = null;
		uncoveredCard2 = null;
	}

	/**
	 * Increase number. increases the round number
	 */
	public void increaseNumber() {
		number++;
	}

	/**
	 * Gets the number.
	 *
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Sets the number.
	 *
	 * @param number the new number
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Gets the uncovered card1.
	 *
	 * @return the uncovered card1
	 */
	public String getUncoveredCard1() {
		return uncoveredCard1;
	}

	/**
	 * Gets the uncovered card2.
	 *
	 * @return the uncovered card2
	 */
	public String getUncoveredCard2() {
		return uncoveredCard2;
	}

	/**
	 * Uncover card.
	 *
	 * @param uncoveredCard the uncovered card
	 */
	public void uncoverCard(String uncoveredCard, Player player) {
		System.out.println("Round.uncoverCard");
		if (uncoveredCard1 == null) {
			uncoveredCard1 = uncoveredCard;
			player1 = player;
			player.setActive(false);
		} else {
			uncoveredCard2 = uncoveredCard;
			player2 = player;
			player.setActive(false);
			
		}
	}
	
	/**
	 * Compare cards.
	 *
	 * @return true, if successful
	 */
	public boolean compareCards(){
		if(uncoveredCard1 != null){
			if(uncoveredCard2 != null){
				System.out.println("Round.compareCards(" + uncoveredCard1 + " - " + uncoveredCard2 + ")");
				return uncoveredCard1.equals(uncoveredCard2);
			}
		}
		return false;
	}


	/**
	 * Sets the start time.
	 *
	 * @param startTime the new start time
	 */
	public void setStartTime() {
		this.startTime = LocPairsServerTime.getTime();
	}

	/**
	 * Sets the duration.
	 *
	 * @param duration the new duration
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * Gets the scores.
	 *
	 * @return the scores
	 */

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Gets the duration.
	 *
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}
	
	/**
	 * Checks if the round is finished. A round ended when two Cards were uncovered.
	 *
	 * @return true, if is finished
	 */
	public boolean isFinished(){
		if(uncoveredCard1 != null && uncoveredCard2 != null)return true;
		return false;
	}
	/**
	 * Gets the player that uncovered the first card.
	 *
	 * @return the player1
	 */
	public Player getPlayer1() {
		return player1;
	}
	/**
	 * Gets the player that uncovered the second card.
	 *
	 * @return the player2
	 */
	public Player getPlayer2() {
		return player2;
	}
	
}
