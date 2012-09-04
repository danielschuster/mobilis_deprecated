
package de.tud.android.locpairs.model;

import android.text.format.Time;

/**
 * The Class Round.
 */
public class Round {
	
	//true if player is active in this round
	/** The is active team. */
	private boolean isActiveTeam = false;
	
	/** The start time. */
	private Time startTime;
	
	/** The end time. */
	private Time endTime;
	
	/** The id. */
	private int id;
	
	/**
	 * Instantiates a new round.
	 *
	 * @param id the id
	 * @param active the active
	 * @param startTime the start time
	 * @param endTime the end time
	 */
	public Round(int id, Boolean active, Time startTime, Time endTime){
		this.id = id;
		this.isActiveTeam = active;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * Checks if is active team.
	 *
	 * @return true, if is active team
	 */
	public boolean isActiveTeam() {
		return isActiveTeam;
	}

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public Time getStartTime() {
		return startTime;
	}

	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 */
	public Time getEndTime() {
		return endTime;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("--- Runde ---").append("\n");
		sb.append("RundenID: ").append(id).append("\n");
		sb.append("Startzeit: ").append(startTime).append("\n");
		sb.append("Endzeit: ").append(endTime).append("\n");
		sb.append("Aktiv? ").append(isActiveTeam).append("\n");
		return sb.toString();
	}
	
}
