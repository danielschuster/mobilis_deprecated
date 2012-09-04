package de.tud.android.locpairs;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import de.tud.android.locpairs.controller.GamingClient;
import de.tud.android.locpairs.controller.LocPairsController;
import de.tud.android.locpairs.model.Game;
import de.tud.android.locpairs.model.Instance;
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
public class GameInstancesActivity extends Activity implements OnItemClickListener  {

	/** Contains the list of lobby users. */
	protected ArrayList<Instance> m_arrGamesList;

	/**
	 * Adapter used to bind an AdapterView to list of users.
	 */
	protected GameListAdapter m_gamesListAdapter;

	/**
	 * ViewGroup used for maintaining a list of Views that each display
	 * LobbyUsers.
	 **/
	protected ListView m_vwGameInstanceLayout;
	protected Instance instance;

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

	/** The TAG. */
	private String TAG = "GameInstanceActivity";
	
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
		m_vwGameInstanceLayout.setOnItemClickListener(this);
		this.getResources();

		bindInstancesToList();
		
		if (Game.getInstance() != null) {

			// create a Dialog to let the User know that we're waiting for the server
			dialog = ProgressDialog.show(GameInstancesActivity.this, "Please wait...", "Collecting Game Data...", true);
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
					while (Game.getInstance().getInstances().size() == 0) {
						// Wait till first instanceupdatebean
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
						}
					});

				}
			});
			dialogThread.start();
		}

	}

	/**
	 * bind arraylist of users to lobby.
	 */
	private void bindInstancesToList() {
		Game.getInstance().reset();
		m_arrGamesList = Game.getInstance().getInstances();
		m_gamesListAdapter = new GameListAdapter(this.getBaseContext(), Game.getInstance().getInstances());
		m_vwGameInstanceLayout.setAdapter(m_gamesListAdapter);
		
	}
	
	protected void addGameInstance(Instance instance) {
		m_arrGamesList.add(instance);
		m_gamesListAdapter.notifyDataSetChanged();
		GameInstanceView txt = new GameInstanceView(this.getBaseContext(), instance);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		try {
		registerReceiver(refreshInstances, new IntentFilter(GamingClient.REFRESH_INSTANCES_ACTION));
		registerReceiver(finishedLoading, new IntentFilter(GamingClient.FINISHED_LOADING_INSTANCES_ACTION));

		} catch (Exception e) {
			Log.e("GameInstanceActivity", "register receiver failed");
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshInstances);
		unregisterReceiver(finishedLoading);
		unbindService(onService);
		getApplicationContext().stopService(new Intent(GameInstancesActivity.this, WifiPositioningService.class));
		getApplicationContext().stopService(new Intent(GameInstancesActivity.this, GpsPositioningService.class));
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
		setContentView(R.layout.locpairs_games);
		m_vwGameInstanceLayout = (ListView) findViewById(R.id.GamesList);
		

	}

	

	/** The refresh instances. */
	private BroadcastReceiver refreshInstances = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			refreshInstances();
		}
	};
	
	/** The finished loading. */
	private BroadcastReceiver finishedLoading = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			dialog.dismiss();
			mProgress.setVisibility(View.INVISIBLE);
			m_gamesListAdapter.notifyDataSetChanged();
		}
	};
	
	

	/**
	 * Refresh instances.
	 */
	private void refreshInstances() {
		Log.v(TAG,"Method refreshInstances called.");
		m_gamesListAdapter.notifyDataSetChanged();
		for (Instance i: Game.getInstance().getInstances() ){
			Log.v(TAG, i.getInstanceJID());
		}

	}

	/** The on service. */
	private ServiceConnection onService = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			lpController = ((LocPairsController.LocalBinder) rawBinder).getService();
			Log.i("Service", "connected");
			
			// searching for games
			if (lpController != null){
				Game.getInstance().reset();
				lpController.sendDiscoveryRequest();
			}
				
		}

		public void onServiceDisconnected(ComponentName className) {
			lpController = null;
			Log.i("Service", "disConnected");
		}
	};
	
	/**
	 * On Game click Listener
	 */
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		instance = (Instance) m_gamesListAdapter.getItem((int) arg3);
		final AlertDialog joinDialog = new AlertDialog.Builder(this).create();
		joinDialog.setTitle(instance.getInstanceJID());
		joinDialog.setMessage("Host: " + instance.getOpener() + "\n" + "Players: "+ String.valueOf(instance.getPlayers().size())   + " of " + instance.getMaxMemberCount() + "\n JID: "+ instance.getInstanceJID());
		joinDialog.setIcon(R.drawable.icon);
		if(instance.getPlayers().size() < instance.getMaxMemberCount()){
			joinDialog.setButton("Join", joinListener );
			joinDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					joinDialog.dismiss();	
				}			
			});
		}else{
			joinDialog.setButton("Cancel", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					joinDialog.dismiss();	
				}			
			});
		}
		
		
		joinDialog.show();
		
	}
	
	
	/** The join listener. */
	private DialogInterface.OnClickListener joinListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			lpController.sendJoinGameMessage(Game.getInstance().getClientPlayer(), instance.getInstanceJID());
			Intent lobbyIntent = new Intent(getApplicationContext(), LobbyActivity.class);
			startActivity(lobbyIntent);
		}
	};
	
	public void onAddGameClick(View v) {
		lpController.sendCreateGameMessage();
		Intent lobbyIntent = new Intent(getApplicationContext(), LobbyActivity.class);
		startActivity(lobbyIntent);
	}
	
	public void onRefreshClick(View v) {
		Game.getInstance().reset();
		m_gamesListAdapter.notifyDataSetChanged();
		lpController.sendDiscoveryRequest();
		mProgress.setVisibility(View.VISIBLE);
		
	}

	
	
	
	

	
}