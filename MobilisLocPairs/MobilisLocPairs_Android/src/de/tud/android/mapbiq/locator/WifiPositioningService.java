/**
 * 
 */
package de.tud.android.mapbiq.locator;

import java.util.List;

import org.placelab.client.tracker.CentroidTracker;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.Tracker;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.WiFiReading;
import org.placelab.mapper.Mapper;

import de.tud.server.model.Coordinate;
import de.tud.server.model.LocationModelAPI;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

/**
 * This class is a service used for wifi positioning. The position is provided by
 * de.tud.android.mapbiq.locator.WIFIPOS_AVAILABLE broadcast having the attributed timestamp,
 * longitude and latitude.
 * @author Franz Josef Gr�neberger
 *
 */
public class WifiPositioningService extends Service {
	
	private Mapper mapper;
	private Tracker tracker;
	
	private WifiManager wifiMgr;
	private WifiBroadcastReceiver wifiBroadcastReceiver;
	
	
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
		
		//create WifiBroadcastReceiver that is called if scan results from wifi scan are available
		wifiBroadcastReceiver = new WifiBroadcastReceiver();
		
		//create new IIOGISMapper that maps data from the LocationModel
		//parameter true means information are cached; false=no caching
		mapper = new IIOGISMapper(false);
		tracker = new CentroidTracker(mapper);
		
		//get WifiManager
		wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		//register BroadcastReceiver to be called if wifi scan had been done
		registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		
		//start scanning
		wifiMgr.startScan();
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//unregister the WifiBroadcastReceiver that was called if wifi scan had been done
		unregisterReceiver(wifiBroadcastReceiver);
	}

	
	
	/**
	 * This class describes the BroadcastReceiver that handles the estimation of a wifi position after
	 * finishing a wifi scan of the environment.
	 * @author Franz Josef Gr�neberger
	 *
	 */
	private class WifiBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			//start the WifiThread that handels the processing for the last wifi scan
			WifiThread wifiThread = new WifiThread();
			wifiThread.start();
			
		}
		
	}
	
	
	/**
	 * This class is a thread class, so that the waiting time after processing one scan result does not influence the main thread
	 * of the program.
	 * @author Franz Josef Gr�neberger
	 *
	 */
	private class WifiThread extends Thread {
		
		@Override
		public void run() {
			super.run();
			
			//obtain ScanResults from the last WifiScan
			List<ScanResult> results = wifiMgr.getScanResults();
			
			//converting results in PlaceLab Measurement
			Measurement meas = convertToMeasurement(results);
			
			//check if tracker supports received measurement and if so update estimated position
			if(tracker.acceptableMeasurement(meas)) {
				tracker.updateEstimate(meas);
			}
			
			//get the new Estimate, store it to LocationModel and provide it via broadcast additionally
			Estimate estimate = tracker.getEstimate();
			double latitude = Double.parseDouble(estimate.getCoord().getLatitudeAsString());
			double longitude = Double.parseDouble(estimate.getCoord().getLongitudeAsString());		
			de.tud.server.model.Coordinate coordinate = new Coordinate(latitude,longitude);
			
			Log.d("MapBiquitous", "WifiPositioning done. time: "+estimate.getTimestamp()+" coord: "+coordinate.getLatitude()+","+coordinate.getLongitude());
			
			//store the position to the location model
			LocationModelAPI.setCurrentWifiPosition(estimate.getTimestamp(), coordinate);
			
			//send Broadcast with the new WifiPosition
			Intent wifiIntent = new Intent("de.tud.android.mapbiq.locator.WIFIPOS_AVAILABLE");
			wifiIntent.putExtra("latitude", coordinate.getLatitude());
			wifiIntent.putExtra("longitude", coordinate.getLongitude());
			wifiIntent.putExtra("timestamp", estimate.getTimestamp());
			sendBroadcast(wifiIntent);
			
			//wait 1000ms until new scan is started
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//start wifi scan again
			wifiMgr.startScan();
			
		}
		
		
		/**
		 * Method for converting List<ScanResult> to PlaceLab Measurement
		 * @param results the list that shall be converted
		 * @return PlaceLab Measurement object
		 */
		private Measurement convertToMeasurement(List<ScanResult> results) {
			BeaconMeasurement meas;
			
			if(results.size() == 0) {
				//no WifiScanResults available
				meas = new BeaconMeasurement(System.currentTimeMillis(),BeaconMeasurement.noWifiReadings);
			} else {
				//WifiScanResults available, so convert them
				meas = new BeaconMeasurement(System.currentTimeMillis());
				for(ScanResult sr : results) {
					WiFiReading wifi = new WiFiReading(sr.BSSID,sr.SSID,sr.level,true,true);
					meas.addReading(wifi);
				}
			}
			
			return meas;
		}
		
	}
	
}
