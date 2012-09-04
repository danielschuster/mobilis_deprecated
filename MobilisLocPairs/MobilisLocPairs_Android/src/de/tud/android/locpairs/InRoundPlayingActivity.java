
package de.tud.android.locpairs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.tud.android.locpairs.controller.GamingClient;
import de.tud.android.locpairs.controller.LocPairsController;
import de.tud.android.locpairs.model.Card;
import de.tud.android.locpairs.model.Game;
import de.tud.android.locpairs.model.Pair;
import de.tud.android.locpairs.model.Round;
import de.tud.android.locpairs.model.Team;
import de.tud.android.mapbiq.R;
import de.tud.android.mapbiq.locator.GpsPositioningService;
import de.tud.android.mapbiq.locator.WifiPositioningService;
import de.tud.android.mapbiq.renderer.BuildingOverlay;
import de.tud.android.mapbiq.renderer.GpsPositionOverlay;
import de.tud.android.mapbiq.renderer.WifiPositionOverlay;
import de.tud.iiogis.wfs.WFSServer;
import de.tud.server.model.LocationModelAPI;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.NetworkFingerPrint;

/**
 * InRoundPlayingActivity handles the actions in the active game.
 * You see the players on the map, the card positions, the active team.
 * There is a count down, the points of your team and scanned cards will be displayed.
 *
 */
public class InRoundPlayingActivity extends MapActivity {

	/** The map view. */
	MapView mapView;
	
	/** The list. */
	List<Overlay> list;
	
	/** The zoom controls. */
	ZoomControls zoomControls;
	
	/** The mc. */
	MapController mc;
	
	/** The building view. */
	LinearLayout zoomView, settingsView, buildingView;
	
	/** The wifi overlay. */
	private WifiPositionOverlay wifiOverlay;
	
	/** The building overlay. */
	private BuildingOverlay buildingOverlay;
	
	/** The gps overlay. */
	private GpsPositionOverlay gpsOverlay;
	
	/** The go there overlay. */
	private GoThereOverlay goThereOverlay;
	
	/** The connected servers. */
	private ArrayList<WFSServer> connectedServers = LocationModelAPI.getConnectedWFSServers();
	
	/** The buildings array. */
	private ArrayList<String> buildingsArray = new ArrayList<String>();
	
	/** The room list view. */
	ListView buildingListView, floorListView, roomListView;

	/** The settings button. */
	Button settingsButton;
	
	/** The back button. */
	Button backButton;
	
	/** The timer handler. */
	private Handler timerHandler = new Handler();
	
	/** The timer. */
	private int timer = 0;
	
	/** The touch overlay. */
	private TouchOverlay touchOverlay;
	
	//New Round PopUp
	/** The popup timer handler. */
	private Handler popupTimerHandler = new Handler();
	
	/** The popup timer. */
	private int popupTimer = 5;
	
	/** The timestamp. */
	private long timestamp;
	
	/** The alert dialog. */
	private AlertDialog alertDialog;

	//LocPairsController Stuff
	/** The lp controller. */
	private LocPairsController lpController;
	
	/** The lp controller intent. */
	private Intent lpControllerIntent;

	// Game Data
	/** The teams. */
	private List<Team> teams;
	
	/** The pairs. */
	private List<Pair> pairs;

	// Balloon Stuff
	/** The pairs ballon overlay. */
	private PairsBallonItemizedOverlay pairsBallonOverlay;
	
	/** The marker open. */
	private Drawable markerOpen;
	
	/** The marker closed. */
	private Drawable markerClosed;
	
	/** The card markers. */
	private CopyOnWriteArrayList<CardMarkerOverlay> cardMarkers = new CopyOnWriteArrayList<CardMarkerOverlay>();
	
	/** The keep alive counter. */
	private int keepAliveCounter = 0;
	
	/** The GoThere Activator */
	private boolean goThere = false;
	
	/** The GoThere Button */
	ImageButton goThereButton;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.locpairs_playing);

		registerReceiver(refreshGameStates, new IntentFilter(GamingClient.REFRESH_GAME_ACTION));
		registerReceiver(refreshPlayerStates, new IntentFilter(GamingClient.REFRESH_PLAYERS_GAME_ACTION));
		registerReceiver(nextRound, new IntentFilter(GamingClient.NEXT_ROUND_GAME_ACTION));
		registerReceiver(quitGame, new IntentFilter(GamingClient.QUIT_GAME_ACTION));
		
		//register LocPairsController
		lpControllerIntent = new Intent(this, LocPairsController.class);
		bindService(lpControllerIntent, onService, BIND_AUTO_CREATE);
		
		//mapView is set
		mapView = (MapView) findViewById(R.id.mapPlaying);
		list = mapView.getOverlays();
		mc = mapView.getController();

		mapView.setSatellite(true);
		mapView.setBuiltInZoomControls(true);

		mc.setZoom(19);
		GeoPoint center = new GeoPoint((int) (51.025758 * 1E6), (int) (13.723348 * 1E6));
		mc.setCenter(center);

		initBuildings();
		initPositionService();

		// ballons overlay
		pairsBallonOverlay = new PairsBallonItemizedOverlay(getResources().getDrawable(R.drawable.marker), mapView);

		/**
		 * if a pair is found, delete the card positions out of the BallonOverlay
		 */
		for (Pair pair : Game.getInstance().getPairs().values()) {
			for (Card card : pair.getCards().values()) {
				GeoPoint point = new GeoPoint((int) ((card.getPosition().getLatitude() * 1E6)), (int) ((card.getPosition().getLongitude() * 1E6)));
				Log.v("CardOverlay", "lat: "+point.getLatitudeE6()+ " lon: "+point.getLongitudeE6());
				CardMarkerOverlay overlayItem = new CardMarkerOverlay(card.getCardID(), pair.getBitmap(), point, "", "");
				pairsBallonOverlay.addOverlay(overlayItem);
				cardMarkers.add(overlayItem);
			}
		}
		list.add(pairsBallonOverlay);

		// add postitions from players
		PlayersOverlay playersOverlay = new PlayersOverlay(this.getBaseContext());
		list.add(playersOverlay);		
		
		try {
		ImageView teamBmpView = (ImageView) findViewById(R.id.tv_team_id_bmp);
		if (Game.getInstance().getClientPlayer().getTeam().getTeamID() == 1){
			teamBmpView.setBackgroundDrawable(getResources().getDrawable( R.drawable.ingame_droid_green));
		}
		if (Game.getInstance().getClientPlayer().getTeam().getTeamID() == 2){
			teamBmpView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ingame_droid_orange));
		}
		if (Game.getInstance().getClientPlayer().getTeam().getTeamID() == 3){
			teamBmpView.setBackgroundDrawable(getResources().getDrawable( R.drawable.ingame_droid_blue));
		}
		if (Game.getInstance().getClientPlayer().getTeam().getTeamID() ==-1){
			teamBmpView.setBackgroundDrawable(getResources().getDrawable( R.drawable.ingame_droid_red));
		}	} catch (Exception e) {
			Log.e("EXCEPTION", "Client player ist null");
		}	
		
//		//TouchMarker
		goThereOverlay = new GoThereOverlay(getApplicationContext());
		list.add(goThereOverlay);
		
		touchOverlay = new TouchOverlay();
		list.add(touchOverlay);
		
//		itemizedoverlay = new PositionMarkerOverlay(getResources().getDrawable(R.drawable.marker2), this);
//		list.add(itemizedoverlay);
//		placeTouchMarker(new GeoPoint((int) (51.025258 * 1E6), (int) (13.723948 * 1E6))) ;
	
	}

	/** 
	 * Create WifiPositioningReceiver that handles the drawing of a position. 
	 * Determined by means of wifi. Initializes Position Overlays. 
	**/
	public void initPositionService() {
		wifiOverlay = new WifiPositionOverlay();
		gpsOverlay = new GpsPositionOverlay();
		list.add(wifiOverlay);
		list.add(gpsOverlay);
		getApplicationContext().startService(new Intent(InRoundPlayingActivity.this, WifiPositioningService.class));
		getApplicationContext().startService(new Intent(InRoundPlayingActivity.this, GpsPositioningService.class));
	}

	/**
	 * Initializes Building Overlays. 
	 */
	public void initBuildings() {
		buildingOverlay = new BuildingOverlay();
		list.add(buildingOverlay);

		// init faculty
		setBuildings();
		connectedServers = LocationModelAPI.getConnectedWFSServers();
		Iterator<WFSServer> wfsIt = connectedServers.iterator();
		while (wfsIt.hasNext()) {
			WFSServer server = wfsIt.next();
			String tmp1 = server.getWfsName();
			String tmp2 = buildingsArray.get(0).toString();
			LocationModelAPI.setSelectedBuilding(server);
			if (tmp1.equals(tmp2)) {
				for (int i = 0; i < server.getWfsPolygonLayers().size(); i++) {
					LocationModelAPI.getActiveFloorLayers().add(server.getWfsPolygonLayers().get(i));
				}
			}
		}
		buildingOverlay.setFloor(1);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();	
	}
	
	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onDestroy()
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		lpController.sendQuitMessage(Game.getInstance().getClientPlayer());
		unregisterReceiver(refreshGameStates);
		unregisterReceiver(refreshPlayerStates);
		unregisterReceiver(nextRound);
		unregisterReceiver(quitGame);
		unbindService(onService);
		popupTimerHandler.removeCallbacks(popupTimerRunnable);
		timerHandler.removeCallbacks(timerRunnable);
		getApplicationContext().stopService(new Intent(InRoundPlayingActivity.this, WifiPositioningService.class));
		getApplicationContext().stopService(new Intent(InRoundPlayingActivity.this, GpsPositioningService.class));
	}
	
	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	public void onResume() {
		Game.getInstance().startGame();
		Log.v("game", "ready");
		super.onResume();
		
		
		
	}
	
	/**
	 * Handles the touch event for the goThere action so that the room is highlighted. 
	 */
    class TouchOverlay extends com.google.android.maps.Overlay
    {       
        
        /* (non-Javadoc)
         * @see com.google.android.maps.Overlay#onTouchEvent(android.view.MotionEvent, com.google.android.maps.MapView)
         */
        @Override
        public boolean onTouchEvent(MotionEvent e, MapView mapView) 
        {   
            if ((e.getAction() == 1) && (goThere)) {                
    			GeoPoint touch = mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());
    			GeoPosition touchPos = new GeoPosition(touch.getLatitudeE6()/1E6, touch.getLongitudeE6()/1E6, 0);
    			lpController.sendGoThereMessage(null, touchPos);
    			mapView.postInvalidate();
    			goThere = false;
    			if(goThereButton != null )goThereButton.setBackgroundResource(R.drawable.title_button);
    			
            }                           
            return false;
        }        
    }

	/**
	 * adds all buildings to buildingsArray.
	 */
	private void setBuildings() {
		connectedServers = LocationModelAPI.getConnectedWFSServers();
		Iterator<WFSServer> wfsIt = connectedServers.iterator();
		while (wfsIt.hasNext()) {
			WFSServer server = wfsIt.next();
			String tmp = server.getWfsName();
			buildingsArray.add(tmp);
		}
	}

	/** called if model changes. */
	private BroadcastReceiver refreshGameStates = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshGameStates();
		}
	};

	/** called if new player positions are available. */
	private BroadcastReceiver refreshPlayerStates = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshPlayerStates();
		}
	};

	/** called if new round is started. */
	private BroadcastReceiver nextRound = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			nextRound();
		}
	};

	/** called if game ends. */
	private BroadcastReceiver quitGame = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "server quits game", Toast.LENGTH_LONG);
			Intent scoreIntent = new Intent(getApplicationContext(), ScoreActivity.class);
			startActivity(scoreIntent);
		}
	};

	
	/**
	 * checks changed model.
	 */
	private void refreshGameStates() {
		updateBalloonStates();
		mapView.postInvalidate();
	}

	/**
	 * refreshes player positions.
	 */
	private void refreshPlayerStates() {
		Log.v("Callback", "refresh player positions");
		mapView.postInvalidate();
	}

	/**
	 * In this method the team for the next round is set and the
	 * players are notified about a new round starting.
	 */
	private void nextRound() {
		
		if (Game.getInstance().getActiveTeam() != null) {
			// set current team information
			TextView teamView = (TextView) findViewById(R.id.tv_current_team_id);
			teamView.setText("Active Team " + Game.getInstance().getActiveTeam().getTeamID());

			ImageView teamBmpView = (ImageView) findViewById(R.id.tv_current_team_id_bmp);
			if (Game.getInstance().getActiveTeam().getTeamID() == 1) {
				teamBmpView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ingame_droid_green));
			}
			if (Game.getInstance().getActiveTeam().getTeamID() == 2) {
				teamBmpView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ingame_droid_orange));
			}
			if (Game.getInstance().getActiveTeam().getTeamID() == 3) {
				teamBmpView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ingame_droid_blue));
			}
			if (Game.getInstance().getActiveTeam().getTeamID() == -1) {
				teamBmpView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ingame_droid_red));
			}
		}
		
		//sets scan button
		Round activeRound = Game.getInstance().getActiveRound();
		if (activeRound.isActiveTeam()) {
			ImageButton scanButton = (ImageButton) findViewById(R.id.btn_title_scan);
			scanButton.setVisibility(View.VISIBLE);
		} else {
			ImageButton scanButton = (ImageButton) findViewById(R.id.btn_title_scan);
			scanButton.setVisibility(View.INVISIBLE);
		}
		
		// set points information
		TextView pointsView = (TextView) findViewById(R.id.tv_points_id);
		pointsView.setText("Points " + Game.getInstance().getPoints().get(Game.getInstance().getClientPlayer().getTeam().getTeamID()) + "");
		
		coverCards();
		
		//check if Pair is removed
		for(Pair pair : Game.getInstance().getPairs().values()){
			if(pair.checkForPair()){
				//vibrate if pair is found
				//if (activeRound.isActiveTeam()==true) {
					// Get instance of Vibrator from current Context
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); 
					// Vibrate for 300 milliseconds
					v.vibrate(300);					
				//}
				
				for(Card card:pair.getCards().values()){
					ArrayList<CardMarkerOverlay> deletedCardMarker = new ArrayList<CardMarkerOverlay>();
					for(CardMarkerOverlay marker:cardMarkers) {
						if (card.getCardID().equals(marker.getCardId())){
							Log.v("remove card", card.getCardID() + " ");
							deletedCardMarker.add(marker);
							marker.setMarker(getResources().getDrawable(R.drawable.marker2));
						}
					}
					cardMarkers.removeAll(deletedCardMarker);
				}
			}
		}
		Log.v("InRoundPlaying", "StartTime = " + activeRound.getStartTime().toMillis(false));
		Log.v("InRoundPlaying", "EndTime = " + activeRound.getEndTime().toMillis(false));
		long duration = activeRound.getEndTime().toMillis(false) - activeRound.getStartTime().toMillis(false);
		duration = duration / 1000;
		timerHandler.removeCallbacks(timerRunnable);
		timer = (int)duration;
		timerRunnable.run();
		
		try{
			// init Timer
			alertDialog.dismiss();
		} catch(Exception e) {}
		timestamp = SystemClock.uptimeMillis();
		popupTimerHandler.removeCallbacks(popupTimerRunnable);
		popupTimer = 5;
		popupTimerRunnable.run();
		
		// get the instance of the LayoutInflater
		alertDialog = new AlertDialog.Builder(this).create();
		if (activeRound.isActiveTeam()) {
			alertDialog.setTitle("New Round started its your turn!");
		} else {
			alertDialog.setTitle("New Round started please wait for the other players.");
		}
		alertDialog.setMessage("Starting in... \n 3");
		alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
		
		
	}

	/**
	 * update the positions of the cards in the game.
	 */
	private void updateBalloonStates() {
		for(Card card:Game.getInstance().getCards().values()) {
			if (card.isState() == Card.CARDSTATE_UNCOVERED) uncoverCard(card);
		}
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.locpairs_map_optionsmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.map_settings:
			Intent i = new Intent(getBaseContext(), MapSettingsActivity.class);
			startActivity(i);
			break;
		case R.id.nextRound:
			nextRound();
			break;

		}
		return true;
	}*/

	/** 
	 * Handle "refresh" action. 
	 * */
	public void onRefreshClick(View v) {
	}

	/**
	 * Handle "QR Scan" action. Toast if the required barcode scanner is not
	 * installed.
	 * 
	 * @param v
	 *            the v
	 */
	public void onScanClick(View v) {
		if (isIntentAvailable(this,"com.google.zxing.client.android.SCAN")){
			
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
		}else{
			Context context = getApplicationContext();
			CharSequence text = "Sorry, you have no Barcode Scanner installed. \n Please install one.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	/**
	 * QR Code result sends uncoverCardMessage with barcode to server.
	 * 
	 * @param requestCode
	 *            the request code
	 * @param resultCode
	 *            the result code
	 * @param intent
	 *            the intent
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				Context context = getApplicationContext();
				CharSequence text = "You scanned: " + contents;
				int duration = Toast.LENGTH_LONG;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				
				Card currentCard = null;
				for (Pair pair : Game.getInstance().getPairs().values()) {
					if(!pair.checkForPair()) {
						for (Card card : pair.getCards().values()) {
							if(card.getBarcode().equals(contents) && !card.isState()) {
								currentCard = card;
								uncoverCard(currentCard);
								ImageButton scanButton = (ImageButton) findViewById(R.id.btn_title_scan);
								scanButton.setVisibility(View.INVISIBLE);
								Log.v("PlayingActivity", "card uncovered: "+ currentCard );
							}
						}
					}
				}

				if (currentCard != null )lpController.sendUncoverCardMessage(currentCard, Game.getInstance().getClientPlayer(), createFootprint());

			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}

	/**
	 * Handle "chat" action.
	 * 
	 * @param v
	 *            the v
	 */
	public void onChatClick(View v) {
	}
	
	
	/**
	 * Handle "goThere" action.
	 * 
	 * @param v
	 *            the v
	 */
	public void onGoThereClick(View v) {
		goThere = !goThere;
		goThereButton = (ImageButton) findViewById(R.id.btn_title_gothere);
		if (goThere) goThereButton.setBackgroundColor(R.color.background2);
		else goThereButton.setBackgroundResource(R.drawable.title_button);
		
	}

	/** Timer Handler shows remaining time. */
	private Runnable timerRunnable = new Runnable() {

		public void run() {

			if (timer > 0)
				timer--;
			TextView timerView = (TextView) findViewById(R.id.tv_remaining_time);
			timerView.setText("Time: " + timer);


			if (timer == 0){
				ImageButton scanButton = (ImageButton) findViewById(R.id.btn_title_scan);
				scanButton.setVisibility(View.INVISIBLE);
			}
			
			//popup Timer beenden
			if(popupTimer < 0){
				popupTimerHandler.removeCallbacks(popupTimerRunnable);
				alertDialog.dismiss();
				
			}
			
//			// Balloon Test
//			if (timer == 20) {
//				cardMarkers.get(0).setStatus(CardMarkerOverlay.OPENABLE);
//				pairsBallonOverlay.openBalloon(cardMarkers.get(0));
//			}
//			if (timer == 10) {
//				cardMarkers.get(1).setStatus(CardMarkerOverlay.OPENABLE);
//				pairsBallonOverlay.openBalloon(cardMarkers.get(1));
//			}
//			
			
			
			
			/*
			 * Now register it for running next time
			 */
			timerHandler.postDelayed(this, 1000);
		}

	};
	
	/** Timer Handler shows new round Message as a popup. */
	private Runnable popupTimerRunnable = new Runnable() {
		public void run() {
			int offset = (int) ((SystemClock.uptimeMillis() - timestamp) /1000);
			popupTimer =  popupTimer - offset ;
			if (offset > 0) timestamp = SystemClock.uptimeMillis();
			 
			if (popupTimer > 1){
				if (alertDialog != null) alertDialog.setMessage("Starting in... \n "+ String.valueOf(popupTimer-1));
			}
			
			if(popupTimer == 1){
				if (alertDialog != null) alertDialog.setMessage("Go!");
			}

			if(popupTimer < 1){
				if (alertDialog != null) alertDialog.dismiss();	
			}
			
			popupTimerHandler.postDelayed(this, 1000);
		}
	};
	
	/**
	 * create Footprint for uncoverCard Message.
	 * 
	 * @return the network finger print
	 */
	private NetworkFingerPrint createFootprint(){
		NetworkFingerPrint fingerPrint = new NetworkFingerPrint();
		
		try{
			//start WiFi-Scan
			WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			manager.startScan();
			List<ScanResult> results = manager.getScanResults();
			for(ScanResult scanResult:results) {
				fingerPrint.addFingerPrint(scanResult.BSSID,scanResult.level);
			}
		} catch (Exception e) {
			//Wlan off?
			e.printStackTrace();
		}
		
		return fingerPrint;
	}
	
	/**
	 * checks if Intent is available.
	 * 
	 * @param context
	 *            the context
	 * @param action
	 *            the action
	 * @return true, if is intent available
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	/** binder for LocPairsController Service. */
    private ServiceConnection onService = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className,
				IBinder rawBinder) {
			lpController = ((LocPairsController.LocalBinder) rawBinder).getService();
			Log.i("Service", "connected");
			//ready for round
			lpController.acknowledgeStartGameBean();
		}

		public void onServiceDisconnected(ComponentName className) {
			lpController  = null;
			Log.i("Service", "disConnected");
		}
	};
	
	/**
	 * OnTap Marker.
	 * 
	 * @param point
	 *            the point
	 */
	public void placeTouchMarker(GeoPoint point){
		OverlayItem overlayitem = new OverlayItem(point, "", "");
	}
	
	/**
	 * Uncover card.
	 * 
	 * @param card
	 *            the card
	 */
	private void uncoverCard(Card card){
		for(CardMarkerOverlay marker:cardMarkers) {
			if(card.getCardID().equals(marker.getCardId())){
//				if(card.isState()) {
//					marker.setMarker(getResources().getDrawable(R.drawable.marker2));
					//marker.setStatus(CardMarkerOverlay.OPENABLE);
					pairsBallonOverlay.closeBalloon();
					pairsBallonOverlay.openBalloon(marker);
//				}
//			} else {
//				marker.setMarker(getResources().getDrawable(R.drawable.marker));
			}
		}
		mapView.postInvalidate();
	}
	
	/**
	 * Cover cards.
	 */
	private void coverCards(){
//		for(CardMarkerOverlay marker:cardMarkers) {
//			marker.setMarker(getResources().getDrawable(R.drawable.marker));
//			marker.setStatus(CardMarkerOverlay.UNOPENABLE);
//			pairsBallonOverlay.closeBalloon(marker);
//		}
//		mapView.postInvalidate();
	}
		
}
