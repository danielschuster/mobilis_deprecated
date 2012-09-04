package de.tudresden.inf.rn.mobilis.android.services;

import java.util.Date;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.xmlpull.v1.XmlPullParser;

import se.su.it.smack.packet.XMPPElement;
import se.su.it.smack.pubsub.PubSub;
import se.su.it.smack.pubsub.elements.PublishElement;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.xmpp.beans.LocationInfo;
import de.tudresden.inf.rn.mobilis.xmpp.packet.LocationIQ;

public class ContextManagementService implements LocationListener, PacketListener {

    private static final String TAG = "ContextManagementService";
	private LocationManager mLM;
	private LocationProvider mLocationProvider;
	private XMPPConnection mConnection;
	private BroadcastReceiver ir;

	public void initIntentReceiver() {
	    ir = new IntentReceiver();
	    Context context = SessionService.getInstance().getContext();
	    context.registerReceiver(ir, new IntentFilter(Const.INTENT_PREFIX + "locationmanager.update"));
	}

	public void unregisterIntentReceiver() {
	    SessionService.getInstance().getContext().unregisterReceiver(ir);
	}

	private class IntentReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (action
	                .equals(Const.INTENT_PREFIX + "locationmanager.update")) {
	            Location loc = getCurrentLocation();
	            updateLocation(loc);
	        }
	    }
	}
	    
	/**
	 * Sends an intent to the view that our location has changed.
	 * 
	 * @param geoloc
	 */
	public void handleLocationCallback(Location geoloc) {
		Intent i = new Intent(Const.INTENT_PREFIX + "location");
		i.putExtra(Const.INTENT_PREFIX + "location.jid","me");
		i.putExtra(Const.INTENT_PREFIX + "location.location",geoloc);
		SessionService.getInstance().getContext().sendBroadcast(i);
	}

	/**
	 * Informs the view and distributes the new location.
	 * 
	 * @param loc
	 *            The current location.
	 */
	public void updateLocation(Location loc) {
		
		handleLocationCallback(loc);
		callUpdateLocation(loc);
		//send an info to the status/info bar that the location has changed
		InfoViewer infoBar = SessionService.getInstance().getInfoViewer();
		infoBar.showInfo("Own location has changed");
	}

	/**
	 * Sends out an XEP-0080 PubSub stanza to those subscribed.
	 */
	private void callUpdateLocation(Location loc) {
		if ((mConnection != null) && mConnection.isConnected()) {
			PubSub ps = new PubSub();
			ps.setFrom(mConnection.getUser());
			ps.setType(IQ.Type.SET);
			PublishElement pub = new PublishElement(
					"http://jabber.org/protocol/geoloc");
			ps.appendChild(pub);
			StringBuffer buf = new StringBuffer();
			buf.append("<geoloc xmlns=\"http://jabber.org/protocol/geoloc\" xml:lang=\"en\">\n");
			buf.append("<lat>" + loc.getLatitude() + "</lat>\n");
			buf.append("<lon>" + loc.getLongitude() + "</lon>\n");
			buf.append("<alt>" + loc.getAltitude() + "</alt>\n");
			buf.append("</geoloc>");
			String itemText = buf.toString();
			CustomItemElement item = new CustomItemElement(itemText);
			pub.addChild(item);

			mConnection.sendPacket(ps);
			
			if (SessionService.getInstance().getSocialNetworkManagementService().isAnyAuthenticated()) {
				LocationIQ luIQ = new LocationIQ();
				luIQ.setFrom(mConnection.getUser());
				luIQ.setTo(SessionService.getInstance().getAndroidbuddyAgent());
				luIQ.setLatitude(loc.getLatitude());
				luIQ.setLongitude(loc.getLongitude());
				luIQ.setAltitude(loc.getAltitude());
				luIQ.setType(IQ.Type.SET);
				luIQ.setTimestamp(new Date());
				luIQ.setIdentity(mConnection.getUser());
				mConnection.sendPacket(luIQ);
			}
		}
	}

	public Location getCurrentLocation() {
		Location loc = mLM.getLastKnownLocation(mLocationProvider.getName());
		return loc;
	}

	public void initialize(XMPPConnection conn) {
		mConnection = conn;
		Context c = SessionService.getInstance().getContext();
		mLM = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
		// mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
		// LocationProvider.AVAILABLE, null, System.currentTimeMillis());
		// mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER,
		// true);
		mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this, SessionService.getInstance().getContext().getMainLooper());
//		LocationProvider lp = mLM.getProvider(LocationManager.GPS_PROVIDER);
//		mLocationProvider = mLM.getProvider(LocationManager.GPS_PROVIDER);
//		Location loc = getCurrentLocation();
//		handleLocationCallback(loc);
		// requestUpdates doesn't work in Android m5-rc15
		// mHandler = new Handler();
		// mLocationManager
		// .requestUpdates(
		// mLocationProvider,
		// 10000,
		// 100,
		// new Intent(
		// Const.INTENT_PREFIX + "locationprovider.update"));
		mConnection.addPacketListener(this, new AndFilter(
				new IQTypeFilter(IQ.Type.SET),
				new PacketTypeFilter(LocationIQ.class)
			));
		
		// inizialize location update sending and receiving
	}

	/**
	 * WORKAROUND class for different w3c dom compatibility on Android
	 * 
	 * @author Istvan
	 * 
	 */
	private class CustomItemElement implements XMPPElement {
		private String mContent;

		public CustomItemElement(String content) {
			mContent = content;
		}

		@Override
		public void addChild(Object obj) throws IllegalArgumentException {
			return;
		}

		@Override
		public void parse(XmlPullParser xmlpullparser) throws Exception {
			return;
		}

		@Override
		public String toXML() {
			StringBuffer buf = new StringBuffer();
			buf.append("<item>\n");
			buf.append(mContent);
			buf.append("</item>");
			return buf.toString();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.v(TAG, "Own location changed");
		updateLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPacket(Packet packet) {
		if (packet instanceof LocationIQ && ((IQ)packet).getType() == IQ.Type.SET) {
			// extract information from IQ
			LocationIQ locIQ = (LocationIQ) packet;
			LocationInfo loc = locIQ.getLocation();
			// generate an Android Location object from the received location info 
			Location androidLocation = generateLocation(loc);
			String identity = locIQ.getIdentity();
			// throw intent
			Intent intent = new Intent(Const.INTENT_PREFIX + "location_buddy");
			intent.putExtra(Const.INTENT_PREFIX + "location_buddy.identity",  identity);
			intent.putExtra(Const.INTENT_PREFIX + "location_buddy.location",  androidLocation);
			intent.putExtra(Const.INTENT_PREFIX + "location_buddy.alert",     locIQ.getAlert());
			intent.putExtra(Const.INTENT_PREFIX + "location_buddy.proximity", locIQ.isProximity());				
			SessionService.getInstance().getContext().sendBroadcast(intent);
			// respond
			locIQ.setType(IQ.Type.RESULT);
			locIQ.setFrom(this.mConnection.getUser());
			locIQ.setTo(SessionService.getInstance().getAndroidbuddyAgent());
			this.mConnection.sendPacket(locIQ);
		}
	}
	
	private Location generateLocation(LocationInfo locationInfo) {
	    Location loc = new Location(Const.MOBILIS_LOCATION_PROVIDER);
	    loc.setAltitude(locationInfo.getAltitude());
	    loc.setLatitude(locationInfo.getLatitude());
	    loc.setLongitude(locationInfo.getLongitude());
	    loc.setSpeed(locationInfo.getSpeed());
	    loc.setTime(locationInfo.getTimestamp().getTime());
	    return loc;
	}
}
