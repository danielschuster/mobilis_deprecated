package de.tudresden.inf.rn.mobilis.example;

import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.gson.Gson;

import de.tudresden.inf.rn.mobilis.clientservices.AuthRemoteException;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.ISocialNetworkService;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare.Venue;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.qype.Place;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.qype.QypeManager;

/**
 * Example which demonstrates the usage of the Social Network Service.
 * @author Robert Lübke
 */
public class MainActivity extends MapActivity implements ServiceConnection {
    	
	/** The TAG for the Log. */
	private final static String TAG = "MainActivity";
	
	private ISocialNetworkService iSNS = null;
	private MapView mapView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
                
        Intent i = new Intent(ISocialNetworkService.class.getName());
        this.startService(i);
		this.bindService(i, this, 0);	
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder binder) {
		Log.i(TAG, "SNS connected");
		this.iSNS = ISocialNetworkService.Stub.asInterface(binder);	
		Toast.makeText(this, "SNS connected", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.i(TAG, "SNS disconnected");
		iSNS = null;
		Toast.makeText(this, "SNS disconnected", Toast.LENGTH_SHORT).show();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	switch (item.getItemId()) {
    	case R.id.menu_settings:      	  	
            this.startActivity(new Intent(this.getApplicationContext(), PrefActivity.class));
            return true;
//    	case R.id.menu_test:
//    		if (iSNS != null) {
//    			mapView.getOverlays().clear();
//    			mapView.invalidate();
//    			
//				try {
//					iSNS.testLoginOAuth2();
//				} catch (RemoteException e) {
//					if (e instanceof AuthRemoteException) {					
//						Log.e(TAG, "AuthRemoteException");
//						e.printStackTrace();
//						Toast.makeText(this, "Error during OAuth-Login. Check username and password in the settings.", Toast.LENGTH_SHORT).show();
//						return true;
//					}
//					Log.e(TAG, "RemoteException: Error @ twitter-API.");
//					e.printStackTrace();
//					Toast.makeText(this, "Error @ twitter-API", Toast.LENGTH_SHORT).show();
//					return true;
//				}
//				//...
//				
//    		} else {
//    			Toast.makeText(this, "SNS is not bound", Toast.LENGTH_SHORT).show();
//    		}
//    		return true;
    	case R.id.menu_foursquare_nearby:
    		if (iSNS != null) {
    			mapView.getOverlays().clear();
    			mapView.invalidate();
    			FoursquareOverlay foursquareOverlay = new FoursquareOverlay(this);
    			List<Venue> venues = null;
				try {
					venues = iSNS.getFoursquareNearbyVenues(
							mapView.getMapCenter().getLongitudeE6()/1E6,
							mapView.getMapCenter().getLatitudeE6()/1E6,
							5);
				} catch (RemoteException e) {
					if (e instanceof AuthRemoteException) {					
						Log.e(TAG, "AuthRemoteException: Error during OAuthLogin");
						e.printStackTrace();
						Toast.makeText(this, "Error during OAuth-Login. Check username and password in the settings.", Toast.LENGTH_SHORT).show();
						return true;
					}
					Log.e(TAG, "RemoteException: Error @ Foursquare-API.");
					e.printStackTrace();
					Toast.makeText(this, "Error @ Foursquare-API", Toast.LENGTH_SHORT).show();
					return true;
				}
				String snippet, title;
				GeoPoint gp;
				OverlayItem oi;
				if (venues!=null) {
	    			for (Venue v : venues) {
	    				gp = new GeoPoint(
	    						(int)(v.getGeolat()*1E6),
	    						(int)(v.getGeolong()*1E6));
	    				title = v.getName();
	    				snippet = "";
	    				if (v.getPrimaryCategory()!=null)
	    					snippet = v.getPrimaryCategory().getFullpathname();
	    				
	    				oi = new OverlayItem(gp, title, snippet);
	    				foursquareOverlay.addOverlay(oi);
	    			}   			
	    			
	    			mapView.getOverlays().add(foursquareOverlay);
	    			mapView.invalidate();
				}
    		} else {
    			Toast.makeText(this, "SNS is not bound", Toast.LENGTH_SHORT).show();
    		}
    		return true;
        case R.id.menu_foursquare_userhistory:
        	if (iSNS != null) {    			
        		mapView.getOverlays().clear();
        		mapView.invalidate();
        		FoursquareOverlay foursquareOverlay = new FoursquareOverlay(this);
    			List<Venue> venues = null;
				try {
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					iSNS.setFoursquareUserCredentials(
							pref.getString("foursquare_username", ""),
							pref.getString("foursquare_password", ""));
					venues = iSNS.getFoursquareCheckinHistory(10);
				} catch (RemoteException e) {					
					if (e instanceof AuthRemoteException) {					
						Log.e(TAG, "AuthRemoteException: Error during OAuthLogin");
						e.printStackTrace();
						Toast.makeText(this, "Error during OAuth-Login. Check username and password in the settings.", Toast.LENGTH_SHORT).show();
						return true;
					}
					Log.e(TAG, "RemoteException: Error @ Foursquare-API.");
					e.printStackTrace();
					Toast.makeText(this, "Error @ Foursquare-API", Toast.LENGTH_SHORT).show();
					return true;
				}
				String snippet, title;
				GeoPoint gp;
				OverlayItem oi;
				if (venues!=null) {
	    			for (Venue v : venues) {
	    				gp = new GeoPoint(
	    						(int)(v.getGeolat()*1E6),
	    						(int)(v.getGeolong()*1E6));
	    				title = v.getName();
	    				snippet = "";
	    				if (v.getPrimaryCategory()!=null)
	    					snippet = v.getPrimaryCategory().getFullpathname();
	    				
	    				oi = new OverlayItem(gp, title, snippet);
	    				foursquareOverlay.addOverlay(oi);
	    			}   			
	    			mapView.getOverlays().add(foursquareOverlay);
	    			mapView.invalidate();
				}
    		} else {
    			Toast.makeText(this, "SNS is not bound", Toast.LENGTH_SHORT).show();
    		}
            return true; 
        case R.id.menu_qype_nearby:       	
        	
    		if (iSNS != null) {
    			mapView.getOverlays().clear();
    			mapView.invalidate();
    			QypeOverlay qypeOverlay = new QypeOverlay(this);
    			List<Place> places = null;
				
    			GeoPoint center = mapView.getMapCenter();
    			int widthE6  = mapView.getLongitudeSpan();
    			int heightE6 = mapView.getLatitudeSpan();
    			double latitudeSW = (center.getLatitudeE6()  - widthE6/2)/1E6;
    			double longitudeSW = (center.getLongitudeE6() - heightE6/2)/1E6;
    			double latitudeNE   = (center.getLatitudeE6()  + widthE6/2)/1E6;
    			double longitudeNE   = (center.getLongitudeE6() + heightE6/2)/1E6;
    			
    			try {
					places = iSNS.getQypeAllPlacesInBoundingBox(longitudeSW, latitudeSW, longitudeNE, latitudeNE, 0, null);					
				} catch (RemoteException e) {
					if (e instanceof AuthRemoteException) {					
						Log.e(TAG, "AuthRemoteException: Error during OAuthLogin");
						e.printStackTrace();
						Toast.makeText(this, "Error during OAuth-Login. Check username and password in the settings.", Toast.LENGTH_SHORT).show();
						return true;
					}
					Log.e(TAG, "RemoteException: Error @ Qype-API.");
					e.printStackTrace();
					Toast.makeText(this, "Error @ Qype-API", Toast.LENGTH_SHORT).show();
					return true;
				}
				String snippet, title;
				GeoPoint gp;
				OverlayItem oi;
				if (places!=null) {
	    			for (Place p : places) {
	    				gp = new GeoPoint(
	    						(int)(p.getLatitutde()*1E6),
	    						(int)(p.getLongitude()*1E6));
	    				title = p.getTitle();
	    				snippet = "ID: "+p.getId()+" CREATED: "+p.getCreated()+" POINT: "+p.getPoint();
	    				
	    				oi = new OverlayItem(gp, title, snippet);
	    				qypeOverlay.addOverlay(oi);
	    			}   			
	    			
	    			mapView.getOverlays().add(qypeOverlay);
	    			mapView.invalidate();
				}
    		} else {
    			Toast.makeText(this, "SNS is not bound", Toast.LENGTH_SHORT).show();
    		}
    		return true;
        default:
        	return super.onOptionsItemSelected(item);
    	}
    }    
	
}