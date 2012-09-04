package de.tud.android.locpairs.controller;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import de.tud.android.locpairs.model.Card;
import de.tud.android.locpairs.model.Game;
import de.tud.android.locpairs.model.Pair;
import de.tud.android.locpairs.model.Round;

/**
 * The GamingClient handles the synchronization of the application view and the server.
 * Sends broadcasts to activities of the game logic.
 */
public class GamingClient extends Service {
	
	/** The Constant NOTIF_ID. */
	public static final int NOTIF_ID = 1337;

	/* Strings used for Broadcasts*/
	/** The Constant REFRESH_GAME_ACTION. */
	public static final String REFRESH_GAME_ACTION = "de.tudresden.inf.rn.mobilis.android.locpairs.refreshgame";
	
	/** The Constant NEXT_ROUND_GAME_ACTION. */
	public static final String NEXT_ROUND_GAME_ACTION = "de.tudresden.inf.rn.mobilis.android.locpairs.nextround";
	
	/** The Constant REFRESH_PLAYERS_GAME_ACTION. */
	public static final String REFRESH_PLAYERS_GAME_ACTION = "de.tudresden.inf.rn.mobilis.android.locpairs.refreshplayers";
	
	/** The Constant START_GAME_ACTION. */
	public static final String START_GAME_ACTION = "de.tudresden.inf.rn.mobilis.android.locpairs.startgame";
	
	/** The Constant QUIT_GAME_ACTION. */
	public static final String QUIT_GAME_ACTION = "de.tudresden.inf.rn.mobilis.android.locpairs.quitgame";
	
	/** The Constant REFRESH_INSTANCES_ACTION. */
	public static final String REFRESH_INSTANCES_ACTION = "de.tudresden.inf.rn.mobilis.android.locpairs.refreshinstances";
	
	/** The Constant NO_INSTANCES_ACTION. */
	public static final String NO_INSTANCES_ACTION = "de.tudresden.inf.rn.mobilis.android.locpairs.noinstances";
	
	/** The Constant FINISHED_LOADING_INSTANCES_ACTION. */
	public static final String FINISHED_LOADING_INSTANCES_ACTION = "de.tudresden.inf.rn.mobilis.android.locpairs.finishedloadinginstances";
	
	/* Broadcast Intents */
	/** The refresh game broadcast. */
	private Intent refreshGameBroadcast = new Intent(REFRESH_GAME_ACTION);
	
	/** The next round broadcast. */
	private Intent nextRoundBroadcast = new Intent(NEXT_ROUND_GAME_ACTION);
	
	/** The refresh players broadcast. */
	private Intent refreshPlayersBroadcast = new Intent(
			REFRESH_PLAYERS_GAME_ACTION);
	
	/** The start game broadcast. */
	private Intent startGameBroadcast = new Intent(START_GAME_ACTION);
	
	/** The quit game broadcast. */
	private Intent quitGameBroadcast = new Intent(QUIT_GAME_ACTION);
	
	/** The refresh instances broadcast. */
	private Intent refreshInstancesBroadcast = new Intent(REFRESH_INSTANCES_ACTION);
	
	/** The finished loading instances broadcast. */
	private Intent finishedLoadingInstancesBroadcast = new Intent(FINISHED_LOADING_INSTANCES_ACTION);

	/** The tag. */
	private String tag = "GamingClient";

	/** The binder. */
	private final Binder binder = new LocalBinder();

	/** The timer handler. */
	private Handler timerHandler = new Handler();

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		startService();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return (binder);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		timerHandler.removeCallbacks(timerRunnable);
		super.onDestroy();
	}

	/**
	 * Start service.
	 */
	public void startService() {
	}

	/**
	 * Called by LocPairsController when game state has changed.
	 */
	public void refreshGame() {
		sendBroadcast(refreshGameBroadcast);
	}
	
	/**
	 * Called by LocPairsController when new information about an game instance
	 * arrived.
	 */
	public void refreshInstances(){
		sendBroadcast(refreshInstancesBroadcast);
	}

	/**
	 * Called by LocPairsController when all information about current game
	 * instances has been retrieved.
	 */
	public void finishedLoadingInstances(){
		sendBroadcast(finishedLoadingInstancesBroadcast);
	}
	
	/**
	 * Start next Round.
	 */
	public void startNextRound() {
		Game.getInstance().setActiveRound(Game.getInstance().getNextRound());
		// Game.getInstance().setNextRound(null);
		for (Pair pair : Game.getInstance().getPairs().values()) {
			pair.checkForPair();
			if (pair.isState()) {
				Boolean status = false;
				for (Card card : pair.getCards().values()) {
					status = status && card.isState();
				}
				if (status)
					Game.getInstance().removePair(pair);
			}
		}
		for (Card card : Game.getInstance().getCards().values()) {
			if (card.isState())
				card.flipCard();
		}

		sendBroadcast(nextRoundBroadcast);
	}

	/**
	 * Prepare next Round.
	 */
	public void initializeNextRound() {
		Log.v(tag, "initialize Round");

		Round nextRound = Game.getInstance().getNextRound();
		if (nextRound != null) {

			Log.v(tag, "start Time = " + nextRound.getStartTime().toString());

			long time = nextRound.getStartTime().toMillis(false)
					- System.currentTimeMillis();
			Log.v(tag,
					"NextRoundTime: "
							+ nextRound.getStartTime().toMillis(false));
			Log.v(tag, "NextRoundTime ignore distance: "
					+ nextRound.getStartTime().toMillis(true));
			Log.v(tag, "SystemTime: " + System.currentTimeMillis());
			Log.v(tag, "Offset: " + time);
			Log.v(tag, "Uptime: " + SystemClock.uptimeMillis());
			timerHandler.postAtTime(timerRunnable, SystemClock.uptimeMillis()
					+ time);

		}

	}

	/**
	 * Refresh players.
	 */
	public void refreshPlayers() {
		sendBroadcast(refreshPlayersBroadcast);
	}

	/**
	 * Start game.
	 */
	public void startGame() {
		sendBroadcast(startGameBroadcast);
	}

	/**
	 * Quit game.
	 */
	public void quitGame() {
		sendBroadcast(quitGameBroadcast);
	}

	/**
	 * The Class LocalBinder.
	 */
	public class LocalBinder extends Binder {
		GamingClient getService() {
			return (GamingClient.this);
		}
	}

	// Timer Handler for starting next round
	/** The timer runnable. */
	private Runnable timerRunnable = new Runnable() {
		public void run() {
			startNextRound();
		}
	};

}
