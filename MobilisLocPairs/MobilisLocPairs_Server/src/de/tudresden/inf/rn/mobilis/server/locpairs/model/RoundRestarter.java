package de.tudresden.inf.rn.mobilis.server.locpairs.model;

import java.util.TimerTask;

import de.tudresden.inf.rn.mobilis.server.locpairs.model.Game;

/**
 * The Class RoundRestarter. It's an extension of the class TimerTask
 * to start a round.
 * 
 * @author Reik Mueller
 */
public class RoundRestarter extends TimerTask {

	private Game game = null;
	
	/**
	 * Instantiates a new round restarter.
	 *
	 * @param game the game
	 */
	public RoundRestarter(Game game) {
		this.game = game;
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		System.out.println("Roundstarter.run()");
		game.startRound();
		this.cancel();
	}
}
