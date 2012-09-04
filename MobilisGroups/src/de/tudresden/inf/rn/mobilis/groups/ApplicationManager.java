/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.groups;

import de.tudresden.inf.rn.mobilis.groups.activities.MainActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * @author Robert Lübke
 *
 */
public class ApplicationManager {
	
	/** The TAG for the Log. */
	private final static String TAG = "ApplicationManager";
	
	private static ApplicationManager instance;
	private MainActivity mainActivity;
	
	// Acquire a reference to the system Location Manager
	LocationManager locationManager;
	Location lastLocation=null;
		
	public static ApplicationManager getInstance() {
		if (instance==null){
			instance = new ApplicationManager();
		}
		return instance;
	}
	
	private ApplicationManager() {
		 		
	}	
		
	public MainActivity getMainActivity() {
		return mainActivity;
	}
	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120000, 200, locationListener);     
	}
	
	public Location getLastKnownLocation() {
		if (lastLocation==null)
			lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastLocation!=null)
			Log.v(TAG, "lastKnownLocation: "+lastLocation.getLatitude()+" / "+lastLocation.getLongitude());
		else
			Log.v(TAG, "lastKnownLocation: null");
		return lastLocation;
	}
	
	// Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {        	
        	// Called when a new location is found by the network location provider.
        	Log.v(TAG,"locationListener --> onLocationChanged()");
        	lastLocation=location;
        	mainActivity.refreshPositionOverlay(location);        	        	
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
      };
      
	
}
