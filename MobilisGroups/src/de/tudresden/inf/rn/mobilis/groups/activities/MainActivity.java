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
package de.tudresden.inf.rn.mobilis.groups.activities;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.ISocialNetworkService;
import de.tudresden.inf.rn.mobilis.groups.ApplicationManager;
import de.tudresden.inf.rn.mobilis.groups.ConstMGroups;
import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.groups.XMPPManager;
import de.tudresden.inf.rn.mobilis.groups.overlays.FoursquareOverlay;
import de.tudresden.inf.rn.mobilis.groups.overlays.GroupsOverlay;
import de.tudresden.inf.rn.mobilis.groups.overlays.PositionOverlay;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupItemInfo;

/**
 * 
 * @author Robert Lübke
 *
 */
public class MainActivity extends MapActivity implements ServiceConnection {
	
	/** The TAG for the Log. */
	private final static String TAG = "MainActivity";
	
	private AlertDialog.Builder builder;
	private AlertDialog alert;
	private MapView mapView;
	
	private boolean foursquareLayerChecked, satelliteLayerChecked;
//	private boolean	gowallaLayerChecked, osmLayerChecked;
	private boolean firstRun;
	private ArrayList<GroupItemInfo> groupItemsToShowOnMap = null;
	private FoursquareOverlay foursquareOverlay=null;
	private GroupsOverlay groupsOverlay=null;
	private PositionOverlay positionOverlay=null;
	
	private ISocialNetworkService iSNS = null;
	
	//XMPP
	private static XMPPManager xmppManager;
	
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {            
            // activity got recreated from a former state
            Log.v(TAG, "Recreating MainActivity");
            firstRun = false;
            
        } else {            
            // activity must have been started through a new intent
            Log.v(TAG, "Creating new MainActivity");
            firstRun = true;
            foursquareLayerChecked=false;
//            gowallaLayerChecked=false;
            satelliteLayerChecked=false;
//            osmLayerChecked=false;
        }
                  
        ApplicationManager.getInstance().setMainActivity(this);
        
        this.setTitle(R.string.app_name);
        setContentView(R.layout.main);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);                 
        
        mapView.setSatellite(satelliteLayerChecked);
        
        mapView.getOverlays().add(longClickOverlay); 
                
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Layers");
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				//Dialog is canceled via back button
				//Overlays have to be refreshed
				refreshAllOverlays();				
			}
		});
        
        Intent i = new Intent(ISocialNetworkService.class.getName());
        this.startService(i);
		this.bindService(i, this, 0);         
       
    }  
    
    
    public void onSplashScreenStop() {
    	//XMPP
        xmppManager = XMPPManager.getInstance();
        xmppManager.connectToMXA();
        
        refreshAllOverlays();        
    }
       
    /** Called when the activity will start interacting with the user. */
    public void onResume(){
    	super.onResume();   	
    	
    	updateLayersDialog(); 
        
    	if (firstRun)
    		firstRun=false;
    	else {
	        // Referesh all activated Overlays
	        this.refreshAllOverlays();
    	}
    } 
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "Starting MainView Activity");
        if (firstRun) {
            //firstRun = false;            
            new SplashScreen(this).show();
        }
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;
        // Handle item selection
    	switch (item.getItemId()) {
    	case R.id.menu_layers:
    		alert.show();
    		return true;
        case R.id.menu_pref:
      	  	//Toast.makeText(getApplicationContext(), "Settings ausgewählt", Toast.LENGTH_SHORT).show();
            this.startActivity(new Intent(this.getApplicationContext(), PrefActivity.class));
            return true;
        case R.id.menu_pref_xmpp:
      	  	//Toast.makeText(getApplicationContext(), "XMPP Settings ausgewählt", Toast.LENGTH_SHORT).show();
        	i = new Intent(ConstMGroups.INTENT_PREF_XMPP);
        	this.startActivity(Intent.createChooser(i, "MXA not found. Please install."));
      	  	return true;
        case R.id.menu_refresh:
      	  	makeToast("Refreshing...");
      	  	this.refreshAllOverlays();
      	  	return true;
        case R.id.menu_friends:
        	//makeToast("Friends selected...");
        	this.startActivity(new Intent(this.getApplicationContext(), FriendsActivity.class));
        	return true;
        case R.id.menu_groups:
        	//makeToast("Groups selected...");
        	this.startActivity(new Intent(this.getApplicationContext(), GroupsActivity.class));
        	return true;     
//        case R.id.menu_discover:
//        	//makeToast("Discovery selected...");
//        	xmppManager.sendServiceDiscoveryIQ();
//        	return true;   
        default:
        	return super.onOptionsItemSelected(item);
    	}
    }    
    
    /**
     * Renews the layers dialog. Called when preferences could have been changed and
     * when the checked-status of one layer element was changed from the code and
     * not the dialog itself
     */
    private void updateLayersDialog() {
		// 
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());    	        
	    final ArrayList<CharSequence> itemsList = new ArrayList<CharSequence>();
	    final ArrayList<Boolean> checkedList = new ArrayList<Boolean>();
	    itemsList.add(this.getString(R.string.layers_satellite));
	    checkedList.add(satelliteLayerChecked);
	    if (pref.contains(this.getString(R.string.pref_overlays_foursquare_key))
	    		&& pref.getBoolean(this.getString(R.string.pref_overlays_foursquare_key), false)) {
	    	itemsList.add(this.getString(R.string.layers_foursquare));
	    	checkedList.add(foursquareLayerChecked);
	    }
//	    if (pref.contains(this.getString(R.string.pref_overlays_gowalla_key))
//	    		&& pref.getBoolean(this.getString(R.string.pref_overlays_gowalla_key), false)) {
//	    	itemsList.add(this.getString(R.string.layers_gowalla));
//	    	checkedList.add(gowallaLayerChecked);
//	    }
//	    itemsList.add(this.getString(R.string.layers_osm));
//	    checkedList.add(osmLayerChecked);                
	    boolean[] checkedItems = toArrayBool(checkedList);           
	    builder.setMultiChoiceItems(toArrayChar(itemsList), checkedItems, new DialogInterface.OnMultiChoiceClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {				
				if (itemsList.get(which).equals(getString(R.string.layers_satellite))) {
					// "Satellite" was changed
					satelliteLayerChecked=isChecked;
					mapView.setSatellite(satelliteLayerChecked);					
				} else if (itemsList.get(which).equals(getString(R.string.layers_foursquare))) {
					// "Foursquare" was changed	
					//Toast.makeText(getApplicationContext(), "Foursquare: "+isChecked, Toast.LENGTH_SHORT).show();
					foursquareLayerChecked = isChecked;
//				} else if (itemsList.get(which).equals(getString(R.string.layers_gowalla))) {
//					// "Gowalla" was changed	
//					//Toast.makeText(getApplicationContext(), "Gowalla: "+isChecked, Toast.LENGTH_SHORT).show();
//					gowallaLayerChecked = isChecked;
//				} else if (itemsList.get(which).equals(getString(R.string.layers_osm))) {
//					// "OSM" was changed	
//					//Toast.makeText(getApplicationContext(), "OSM: "+isChecked, Toast.LENGTH_SHORT).show();
//					osmLayerChecked = isChecked;
				} else {
					Toast.makeText(getApplicationContext(), "Error handling the layers", Toast.LENGTH_SHORT).show();	
				}
				
			}
		});
	    alert = builder.create();  
	}
    
    
    private void onMapWasMoved() {
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());   
    	//Auto-refresh the GroupsOverlay
    	if(pref.getBoolean(this.getString(R.string.pref_communication_autorefresh_key), false)) {
    		// Send a GroupQueryBean to the server, to update the GroupsOverlay
        	xmppManager.sendGroupQueryBeanGet(this.mapView);        	
    	} 
    	//Auto-refresh the FoursquareOverlay
		if(foursquareLayerChecked && pref.getBoolean(this.getString(R.string.pref_overlays_foursquare_autorefresh_key), false))
			refreshFoursquareOverlay();
    }
    
    /**
     * At first deletes all old overlays, then creates the activated overlays and adds them to the map.
     */
    public void refreshAllOverlays() {
    	
    	if (MXAController.get().getXMPPService()!=null && xmppManager.getGroupingService()!=null)
    		// Send a GroupQueryBean to the server, to update the GroupsOverlay    	
    		xmppManager.sendGroupQueryBeanGet(this.mapView);
    	
    	refreshGroupsOverlay();
    	
    	refreshFoursquareOverlay();
        
//      	if (gowallaLayerChecked) {
      		//make new Gowalla Overlay
      		//todo: Gowalla Overlay
//      	}     	
        mapView.invalidate();
    }    
    
    public void refreshGroupsOverlay() {
    	Log.i(TAG, "refreshGroupsOverlay()");
    	   	
    	if (groupsOverlay==null) {
    		Log.i(TAG, "refreshGroupsOverlay() --> groupsOverlay==null");
    		if (groupItemsToShowOnMap!=null) {
    			// Create the GroupOverlay. Should only be executed once, when the first GroupQueryBean came in.
    			groupsOverlay = new GroupsOverlay(this, groupItemsToShowOnMap);
            	mapView.getOverlays().add(groupsOverlay);
    		}   		
    	} else {
    		if (groupItemsToShowOnMap!=null) {
    			groupsOverlay.updateAllOverlayItems(groupItemsToShowOnMap);
    		}
    	}    	
    	mapView.invalidate();
    }
    
    private void refreshFoursquareOverlay() {
    	Log.i(TAG, "refreshFoursquareOverlay()");    	
    	if (foursquareLayerChecked) {    		
	    	if (foursquareOverlay==null) {
	    		Log.i(TAG, "refreshFoursquareOverlay() --> foursquareOverlay==null");	    		
    			// Create the FoursquareOverlay. Should only be executed once.
    			foursquareOverlay = new FoursquareOverlay(this, mapView.getMapCenter());
    			if (foursquareOverlay.success) {
    				mapView.getOverlays().add(foursquareOverlay);
    			} else {
    				makeToast("Error at Foursquare-API. Try again later.");
	    			foursquareLayerChecked=false;
	    			updateLayersDialog();
    			}    		
	    	} else {	    		
	    		if (!foursquareOverlay.updateAllOverlayItems(mapView.getMapCenter())) {
	    			makeToast("Error at Foursquare-API. Try again later.");	    			
	    			foursquareLayerChecked=false;
	    			updateLayersDialog();
	    		}
	    		if (!mapView.getOverlays().contains(foursquareOverlay))
	    			mapView.getOverlays().add(foursquareOverlay);    			
	    	}    	
	    	mapView.invalidate();
    	} else {
    		mapView.getOverlays().remove(foursquareOverlay);
    	}
    }
    
    public void refreshPositionOverlay(Location location) {
    	//TODO
    	Log.i(TAG, "refreshPositionOverlay()");
    	    	
    	if (positionOverlay==null) {
    		positionOverlay = new PositionOverlay(this);
    		mapView.getOverlays().add(positionOverlay);
    	}
    	OverlayItem oi = new OverlayItem(new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6)), "", "");    	
    	positionOverlay.addOverlay(oi);
    }
    
    private boolean wasMoved=false;
    private Overlay longClickOverlay = new Overlay() {
    		private float startX, startY;     		
	  		private Timer timer;  
	  		
	  		public boolean onTouchEvent(final MotionEvent ev, final MapView view) {
	  			if(ev.getAction() == MotionEvent.ACTION_DOWN) {
	  				wasMoved=false;
	  				timer=new Timer();
	  				TimerTask task = new TimerTask() {
	  					public void run() {
	  						Log.i(TAG,"long click");      						
	  						Message msg = new Message();
	  						GeoPoint gp = view.getProjection().fromPixels((int)ev.getX(), (int) ev.getY());      						
	  						msg.arg1=gp.getLatitudeE6();
	  						msg.arg2=gp.getLongitudeE6();      							
	  						createGroupHereHandler.sendMessage(msg);
	  					}
	  				};
	  				timer.schedule(task , 1000);
	  				startX=ev.getX();
	  				startY=ev.getY();
	  		    } else if(ev.getAction() == MotionEvent.ACTION_UP) {
	  		    	timer.cancel();
	  		    	if (wasMoved) {
	  		    		Log.i(TAG,"map was moved.");
	  		    		onMapWasMoved();
	  		    	}
	  		    } else if(ev.getAction() == MotionEvent.ACTION_MOVE &&
	  		    		(Math.abs(ev.getX()-startX)>10 || Math.abs(ev.getY()-startY)>10)) {
	  		    	timer.cancel();
	  		    	wasMoved=true;
	  		    }		  			
	  			return false;      		
	  		}	  		
		};
       
    public Handler createGroupHereHandler = new Handler() {
    	@Override
		public void handleMessage(Message msg) {			
			openCreateGroupHereDialog(msg.arg1, msg.arg2);
		}
    };
    
    private void openCreateGroupHereDialog(final int latitudeE6, final int longitudeE6) {
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setTitle("Create new Group")
  	  			.setMessage("Latitude: "+ latitudeE6/1E6 + "\nLongitude: "+longitudeE6/1E6)
  	  			.setIcon(R.drawable.add_new_group)
  	  			.setCancelable(true)
  	  			.setPositiveButton("Create Group here", new DialogInterface.OnClickListener() {
  		  				public void onClick(DialogInterface dialog, int id) {
	  		  				Intent i = new Intent(getApplicationContext(), GroupCreateActivity.class);
		  		  	      	i.putExtra("group_latitude", latitudeE6);
		  		  	      	i.putExtra("group_longitude", longitudeE6);
  		  		        	startActivity(i);
  		  				}
  		  			})
  		  		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
  			  			public void onClick(DialogInterface dialog, int id) {
  			  				dialog.cancel();
  			  			}
  		  			});
		AlertDialog ad;
		ad = b.create();
		Vibrator vibr = (Vibrator) getSystemService(MainActivity.VIBRATOR_SERVICE);
		vibr.vibrate(100);  
		ad.show();
    }
    
    
    /** Utility method for converting ArrayList to Array */
    private CharSequence[] toArrayChar(ArrayList<CharSequence> list) {
    	CharSequence[] array = new CharSequence[list.size()];
    	for (int i=0; i<list.size(); i++)
    		array[i]=list.get(i);   
    	return array;
    }
    /** Utility method for converting ArrayList to Array */
    private boolean[] toArrayBool(ArrayList<Boolean> list) {
    	boolean[] array = new boolean[list.size()];
    	for (int i=0; i<list.size(); i++)
    		array[i]=list.get(i);   
    	return array;
    }
    
    /** Shows a short Toast message on the map */
    public void makeToast(String text) {
    	Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
    
    /** Getter and Setter */
    public boolean getFoursquareLayerChecked() {
    	return foursquareLayerChecked;
    }
//    public boolean getGowallaLayerChecked() {
//    	return gowallaLayerChecked;
//    }
    public void setFoursquareLayerChecked(boolean checked) {
    	foursquareLayerChecked=checked;
    }
//    public void setGowallaLayerChecked(boolean checked) {
//    	gowallaLayerChecked=checked;
//    }
    public void setGroupItemsToShowOnMap(ArrayList<GroupItemInfo> groupItemsToShowOnMap) {
    	this.groupItemsToShowOnMap=groupItemsToShowOnMap; 
    	Log.i(TAG,"setGroupItemsToShowOnMap");
    	refreshOverlaysHandler.sendEmptyMessage(0);
    }
    public MapView getMapView() {
    	return this.mapView;
    }
    
    Handler refreshOverlaysHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {    		
    		refreshGroupsOverlay();
    	}
    };
    
    @Override
	public void onServiceConnected(ComponentName name, IBinder binder) {
		Log.v(TAG, "SNS connected");
		this.iSNS = ISocialNetworkService.Stub.asInterface(binder);	
		//Toast.makeText(this, "SNS connected", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.v(TAG, "SNS disconnected");
		iSNS = null;
		Toast.makeText(this, "SNS disconnected", Toast.LENGTH_SHORT).show();
	}
	
	public ISocialNetworkService getISNS() {
		return iSNS;
	}

    
}
