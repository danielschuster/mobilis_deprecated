
package de.tud.android.locpairs;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import de.tud.android.locpairs.controller.GamingClient;
import de.tud.android.locpairs.controller.LocPairsController;
import de.tud.android.locpairs.model.Game;
import de.tud.android.locpairs.model.Player;
import de.tud.android.locpairs.model.Team;
import de.tud.android.mapbiq.R;
import de.tud.android.mapbiq.locator.GpsPositioningService;
import de.tud.android.mapbiq.locator.WifiPositioningService;

/**
 * Lobby. Here players meet each other and see the different teams. 
 * Game will start automatically.
 * 
 * @author Stefan Wagner
 */
public class LobbyActivity extends Activity {

	/** Contains the list of lobby users. */
	protected ArrayList<Player> m_arrLobbyUserList;

	/**
	 * Adapter used to bind an AdapterView to list of users.
	 */
	protected LobbyListAdapter m_lobbyUserAdapter;

	/**
	 * ViewGroup used for maintaining a list of Views that each display
	 * LobbyUsers.
	 **/
	protected ListView m_vwLobbyUserLayout;


	/** The lp controller. */
	private LocPairsController lpController;
	
	/** The lp controller intent. */
	private Intent lpControllerIntent;

	/** Dialog Stuff. */
	private static final int PROGRESS = 0x1;
	
	/** The m progress. */
	private ProgressBar mProgress;
	
	/** The m progress status. */
	private int mProgressStatus = 0;
	
	/** The m handler. */
	private Handler mHandler = new Handler();
	
	/** The dialog. */
	private Dialog dialog;
	
	/** The dialog thread. */
	private Thread dialogThread;

	/**
	 * Called when the activity is first created. In a background thread the map
	 * is loaded. The LocPairsController is registered.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// register LocPairsController
		lpControllerIntent = new Intent(this, LocPairsController.class);
		bindService(lpControllerIntent, onService, BIND_AUTO_CREATE);

		initLayout();
		initListeners();
		this.getResources();

		bindLobbyUsersToList();
		
		if (!Game.getInstance().isConnected()) {

			// create a Dialog to let the User know that we're waiting for the server
			dialog = ProgressDialog.show(LobbyActivity.this, "Please wait...", "Connecting to server...", true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface arg0) {
					finish();
				}
			});
			
			// takes long so we add a progress bar
			mProgress = (ProgressBar) findViewById(R.id.progressbar);
			mProgress.setVisibility(View.VISIBLE);
			mProgress.setIndeterminate(true);

			// Start lengthy operation in a background thread
			dialogThread = new Thread(new Runnable() {
				public void run() {
//					Looper.prepare();
					while (!Game.getInstance().isConnected()) {
						// Wait till first playerupdatebean
						try {
							Thread.sleep(1000);
							
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
//					Looper.loop();
					
					// Update the progress bar
					mHandler.post(new Runnable() {
						public void run() {
							// dismiss the Progress Dialog
							try{
								dialog.dismiss();
							} catch(Exception e) {
								
							}
							mProgress.setVisibility(View.GONE);
						}
					});

				}
			});
			dialogThread.start();
		}
		
		getApplicationContext().startService(new Intent(LobbyActivity.this, WifiPositioningService.class));
		getApplicationContext().startService(new Intent(LobbyActivity.this, GpsPositioningService.class));

	}

	/**
	 * bind arraylist of users to lobby.
	 */
	private void bindLobbyUsersToList() {
		m_arrLobbyUserList = new ArrayList<Player>();
		m_lobbyUserAdapter = new LobbyListAdapter(this.getBaseContext(), m_arrLobbyUserList);
		m_vwLobbyUserLayout.setAdapter(m_lobbyUserAdapter);

		for (Team team : Game.getInstance().getTeams().values()) {
			for (Player player : team.getPlayers().values()) {
				this.addLobbyUser(player);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		try {
		registerReceiver(refreshLobby, new IntentFilter(GamingClient.REFRESH_GAME_ACTION));
		registerReceiver(refreshLobby, new IntentFilter(GamingClient.REFRESH_PLAYERS_GAME_ACTION));
		registerReceiver(startGame, new IntentFilter(GamingClient.START_GAME_ACTION));
		registerReceiver(quitGame, new IntentFilter(GamingClient.QUIT_GAME_ACTION));
		} catch (Exception e) {
			Log.e("lobbyactivity", "register receiver failed");
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshLobby);
		unregisterReceiver(startGame);
		unregisterReceiver(quitGame);
		unbindService(onService);
		getApplicationContext().stopService(new Intent(LobbyActivity.this, WifiPositioningService.class));
		getApplicationContext().stopService(new Intent(LobbyActivity.this, GpsPositioningService.class));
		try{
			dialogThread.stop();
			dialogThread = null;
		} catch (Exception e) {
			
		}
	}

	/**
	 * Method is used to encapsulate the code that initializes and sets the
	 * Layout for this Activity.
	 */
	protected void initLayout() {
		// set view to layout main.xml
		setContentView(R.layout.locpairs_lobby);

		m_vwLobbyUserLayout = (ListView) findViewById(R.id.LobbyPlayersList);

	}

	/**
	 * Method is used to encapsulate the code that initializes and sets the
	 * Event Listeners.
	 */
	protected void initListeners() {

	}

	/**
	 * Method used for encapsulating the logic necessary to properly add a new
	 * LobbyUser to m_arrLobbyUserList, and display it on screen.
	 * 
	 * @param lobbyUser
	 *            The LobbyUser to add to list of LobbyUsers.
	 */
	protected void addLobbyUser(Player lobbyUser) {
		m_arrLobbyUserList.add(lobbyUser);
		m_lobbyUserAdapter.notifyDataSetChanged();
		LobbyUserView txt = new LobbyUserView(this.getBaseContext(), lobbyUser);
	}
	
	/** the lobby is refreshed if the broadcastReceiver gets a message. */
	private BroadcastReceiver refreshLobby = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			refreshLobby();
		}
	};
	
	/** the game is quitted if the broadcastReceiver gets a message. */
	private BroadcastReceiver quitGame = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};
	
	/** the game is started if the broadcastReceiver gets a message. */
	private BroadcastReceiver startGame = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			// sends ready command to server
			Intent playingIntent = new Intent(getBaseContext(), InRoundPlayingActivity.class);
			startActivity(playingIntent);
		}
	};

	/**
	 * updates the list of players.
	 */
	private void refreshLobby() {
		m_arrLobbyUserList.clear();
		m_lobbyUserAdapter.notifyDataSetChanged();
		for (Player player:  Game.getInstance().getPlayers().values()){
			this.addLobbyUser(player);
		}

	}

	/** The on service. */
	private ServiceConnection onService = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			lpController = ((LocPairsController.LocalBinder) rawBinder).getService();
			Log.i("Service", "connected");
			
			// tell LP controller the new player
			if (lpController != null){
				lpController.sendDiscoveryRequest();
				//lpController.sendJoinGameMessage(Game.getInstance().getClientPlayer());	
			}
				
		}

		public void onServiceDisconnected(ComponentName className) {
			lpController = null;
			Log.i("Service", "disConnected");
		}
	};
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		lpController.sendQuitMessage(Game.getInstance().getClientPlayer());	
		super.onBackPressed();
	}
	
	
	
	

	
}