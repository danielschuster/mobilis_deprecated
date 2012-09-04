/**
 * 
 */
package de.tud.android.mapbiq.locator;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import de.tud.server.model.Coordinate;
import de.tud.server.model.LocationModelAPI;

/**
 * This class is a service used for gps positioning. The position is provided by
 * de.tud.android.mapbiq.locator.GPSPOS_AVAILABLE broadcast having the attributed timestamp,
 * longitude and latitude.
 * @author Franz
 *
 */
public class GpsPositioningService extends Service {

	private GpsListener gpsListener;
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//initialize GpsListener to be used later
		gpsListener = new GpsListener();
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		//register GpsListener to handle gps updates
		LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
		
		System.out.println("STARTEN");
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//unregister GpsListener
		LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mgr.removeUpdates(gpsListener);
		
		System.out.println("BEENDEN");
	}
	
	/**
	 * This class represents the listener that can be registered for handling
	 * gps position updates. The new position data is provided via LocationModelAPI
	 * and a broadcast de.tud.android.mapbiq.locator.GPSPOS_AVAILABLE is sent.
	 * @author Franz
	 *
	 */
	private class GpsListener implements LocationListener {
		
		public void onLocationChanged(Location location) {
			//store the new location in the location model
			Coordinate coord = new Coordinate(location.getLatitude(), location.getLongitude());
			LocationModelAPI.setCurrentGpsPosition(location.getTime(), coord);
			
			//send broadcast
			Intent intent = new Intent("de.tud.android.mapbiq.locator.GPSPOS_AVAILABLE");
			intent.putExtra("timestamp", location.getTime());
			intent.putExtra("latitude", location.getLatitude());
			intent.putExtra("longitude", location.getLongitude());
			sendBroadcast(intent);
			
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
