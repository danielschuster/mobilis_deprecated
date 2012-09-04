package de.tudresden.inf.rn.mobilis.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;

import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.android.util.Util;

/**
 * Class responsible for MapView and main menu display.
 * @author Istvan, Dirk
 */
public class LocationMapActivity extends MapActivity implements OnClickListener {

	// views
	private MapView locationMap;
	private Menu mMenu;
	private MenuItem mMenuMapMode;
	private MenuItem mMenuSatelliteMode;
	private ImageView mPlaceReticule;
	private ProgressDialog mProgressDialog;
	
	// request codes
	private static final int ACTIVITY_CREATE_PLACE = 0;

	// fields
	private PresenceOverlay mPresenceOverlay;
	private Handler mHandler;
	private BroadcastReceiver cir;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.mapactivity);
		initComponents();
	}

	@Override
	public void onStart() {
	    super.onStart();
	    initIntentReceiver();
	}

	@Override
	public void onStop() {
	    super.onStop();
	    unregisterReceiver(cir);
	}
	
	private void initComponents() {
		locationMap = (MapView) findViewById(R.id.map_map);
		mPlaceReticule = (ImageView) findViewById(R.id.map_reticule_image);

		mPlaceReticule.setOnClickListener(this);
		mPlaceReticule.setClickable(true);

		// add zoom controls
		LinearLayout zoomLayout = (LinearLayout) findViewById(R.id.map_zoom);
		zoomLayout.addView(locationMap.getZoomControls(),
				new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));

		setMapLocation();
	}

	/**
	 * Ideally, all presence updates come in via intents
	 */
	private void initIntentReceiver() {
		cir = new CallbackIntentReceiver();
		registerReceiver(cir, new IntentFilter(
				Const.INTENT_PREFIX + "presence"));
		registerReceiver(cir, new IntentFilter(
				Const.INTENT_PREFIX + "location"));
		registerReceiver(cir, new IntentFilter(
				Const.INTENT_PREFIX + "location_buddy"));
	}

	/**
	 * Create main menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;

		// Inflate the menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_map, menu);

		mMenuMapMode = menu.findItem(R.id.menu_map_mode_map);
		mMenuSatelliteMode = menu.findItem(R.id.menu_map_mode_satellite);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (locationMap.isSatellite())
			mMenuSatelliteMode.setChecked(true);
		else
			mMenuMapMode.setChecked(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_accounts:
	        handleAccountsMenuSelection();
	        return true;
	    case R.id.menu_connections:
            handleConnectionsMenuSelection();
            return true;
	    case R.id.menu_chat_groups:
	        handleChatGroupsMenuSelection();
	        return true;
	    case R.id.menu_new_place:
	        toggleReticule();
	        return true;
	    case R.id.menu_map_mode_map:
	        locationMap.setSatellite(false);
	        return true;
	    case R.id.menu_map_mode_satellite:
	        locationMap.setSatellite(true);
	        return true;
	    case R.id.menu_zoom:
	        locationMap.displayZoomControls(true);
	        return true;
	    case R.id.menu_pref:
	        Intent i = new Intent(this, PreferencesClient.class);
	        startActivity(i);
	    }
	    return false;
	}

    private void setMapLocation() {
		MapController mc = locationMap.getController();
		
		// check if current location can be retrieved; if yes, animate to it
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationProvider lp = lm.getProvider(LocationManager.GPS_PROVIDER);
		Location loc = lm.getLastKnownLocation(lp.getName());
		if (loc != null) {
			GeoPoint p = new GeoPoint((int) (loc.getLatitude() * 1000000),
					(int) (loc.getLongitude() * 1000000));
			mc.animateTo(p);
		}
		mc.setZoom(16);

		// create map overlay
		mPresenceOverlay = new PresenceOverlay(locationMap, this);
		locationMap.getOverlays().add(mPresenceOverlay);
	}

	/**
	 * Updates the overlay's internal members representation and redraws it.
	 */
	private void handlePresenceUpdate() {
		// SessionService sessionServ = SessionService.getInstance();
		// ArrayList<GroupMember> gm = sessionServ.getGroupMemberService()
		// .getGroupMembers();
		// mPresenceOverlay.initializeLayout(gm);
		// locationMap.invalidate();
	}

	public void handleLocationCallback(String jid, Location loc) {
		mPresenceOverlay.updateLocation(jid, loc, true);
	}

	public void handleProximityCallback(String jid, Location loc, boolean proximity, boolean alert) {
		
		// allocate messages
		SessionService ss  = SessionService.getInstance();
		Context c   = ss.getContext();
		Resources r = c.getResources();
		String statusText    = String.format(r.getString(R.string.infobar_buddylocation), Util.getNameFromJabberID(jid));
		String notifyTicker  = String.format(r.getString(R.string.notification_proximity_ticker), jid);
		String notifyTitle   = r.getString(R.string.notification_proximity_title);
		String notifyContent = jid;
		
		// show text in status bar
		ss.getInfoViewer().showInfo(statusText);
			
		// add proximity hit
		int buddyHandle = mPresenceOverlay.addOrUpdateBuddy(jid, loc, proximity, alert);
		
		if (proximity && alert) {
			// add notification
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification = new Notification(
					R.drawable.flag_red,  // the icon to show in the notification bar
					notifyTicker,    // the text to show in the notification bar
					loc.getTime()    // the time the notification was created
				);
			PendingIntent contentIntent = PendingIntent.getBroadcast( // the intent to be called if the user clicks the notification
					this, 0, // this context and a result code of 0 (not used)
					new Intent(Const.INTENT_PREFIX + "lookup_buddy") // the intent
						.putExtra(Const.INTENT_PREFIX + "lookup_buddy.handle", buddyHandle), // we add a reference to the handle
					0 // flags
				);
			notification.setLatestEventInfo(
					SessionService.getInstance().getContext(),
					notifyTitle, // title to be shown in the notification pane
					notifyContent, // text to be shown in the notification pane
					contentIntent // the pending intent created above
				);
			nm.notify(buddyHandle, notification);
		}
		
	}

	/**
	 * Listens to Intents with action
	 * Const.INTENT_PREFIX + "presence". They occur whenever
	 * the presence or location of a group member has been updated.
	 * 
	 * @author sealpuppy
	 * 
	 */
	private class CallbackIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action
					.equals(Const.INTENT_PREFIX + "location")) {
				String jid = intent
						.getStringExtra(Const.INTENT_PREFIX + "location.jid");
				Location loc = (Location) intent
						.getParcelableExtra(Const.INTENT_PREFIX + "location.location");
				LocationMapActivity.this.handleLocationCallback(jid, loc);
			}
			if (action
					.equals(Const.INTENT_PREFIX + "location_buddy")) {
				
				// TODO Modify this body for displaying marker on map and for showing a notification
				String   id  =            intent.getStringExtra(Const.INTENT_PREFIX + "location_buddy.identity");
				Location loc = (Location) intent.getParcelableExtra(Const.INTENT_PREFIX + "location_buddy.location");
				boolean  proximity =      intent.getBooleanExtra(Const.INTENT_PREFIX + "location_buddy.proximity", false);
				boolean  alert =          intent.getBooleanExtra(Const.INTENT_PREFIX + "location_buddy.alert", false);
				LocationMapActivity.this.handleProximityCallback(id, loc, proximity, alert);
			}
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * Toggles the new place reticule and the underlying overlay whether they
	 * are clickable or not.
	 */
	private void toggleReticule() {
		if (mPlaceReticule.getVisibility() == View.GONE) {
			// enable reticule
			Toast.makeText(this,
					"Click the reticule to enter information about the place.",
					Toast.LENGTH_SHORT);
			mPresenceOverlay.setClickable(false);
			mPlaceReticule.setVisibility(View.VISIBLE);
		} else {
			// disable reticule
			mPlaceReticule.setVisibility(View.GONE);
			mPresenceOverlay.setClickable(true);
		}
	}

	/**
	 * Opens a new activity to enable creating a new place.
	 */
	private void createNewPlace() {
		Intent i = new Intent(this, EditPlaceActivity.class);
		GeoPoint gp = locationMap.getMapCenter();
		Location loc = new Location("gps");
		loc.setLatitude(gp.getLatitudeE6() / 1000000);
		loc.setLongitude(gp.getLongitudeE6() / 1000000);
		Log.i("lat", Integer.toString(gp.getLatitudeE6()));
		Log.i("lon", Integer.toString(gp.getLongitudeE6()));
		i.putExtra(Const.INTENT_PREFIX + "createplace.location", loc);

		startActivityForResult(i, ACTIVITY_CREATE_PLACE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		switch (requestCode) {
		case ACTIVITY_CREATE_PLACE:
			toggleReticule();
			// Frauenkirche
			Location loc = new Location("gps");
			loc.setLatitude(Double.valueOf(51051850 / 1E6));
			loc.setLongitude(Double.valueOf(13741372 / 1E6));
			mPresenceOverlay.updateLocation("Frauenkirche", loc, false);
			break;
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.map_reticule_image:
			createNewPlace();
		}
	}

    /**
     * Handles a click on the Chat Groups menu item: Sends a groups query
     * intent in order to get all available groups.
     */
    private void handleChatGroupsMenuSelection() {
        // mProgressDialog = ProgressDialog.show(this, null,
        // "Please wait while retrieving groups ...");
        if (SessionService.getInstance().getSocialNetworkManagementService().
                isAuthenticated(Const.MOBILIS)) {
            Intent i = new Intent(
                    Const.INTENT_PREFIX + "servicecall.groupsquery");
            sendBroadcast(i);
        }
    }

    /**
     * Handles a click on the Networks menu item: Sends an intent for opening the 
     * network connections activity.
     */
    private void handleConnectionsMenuSelection() {
        Intent i = new Intent(this, ConnectionsActivity.class);
        startActivity(i);
    }
    
    /**
     * Handles a click on the Accounts menu item: Sends an intent for opening the 
     * accounts activity.
     */
    private void handleAccountsMenuSelection() {
        Intent i = new Intent(this, AccountsActivity.class);
        startActivity(i);
    }
}
