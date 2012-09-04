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
package de.tudresden.inf.rn.mobilis.groups.overlays;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.gson.Gson;

import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.groups.R.drawable;
import de.tudresden.inf.rn.mobilis.groups.R.id;
import de.tudresden.inf.rn.mobilis.groups.R.layout;
import de.tudresden.inf.rn.mobilis.groups.activities.GroupCreateActivity;
import de.tudresden.inf.rn.mobilis.groups.activities.GroupInfoActivity;
import de.tudresden.inf.rn.mobilis.groups.activities.MainActivity;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupItemInfo;

/**
 * 
 * @author Robert Lübke
 *
 */
public class GroupsOverlay extends ItemizedOverlay<OverlayItem> {

	/** The TAG for the Log. */
	private final static String TAG = "GroupsOverlay";
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private ArrayList<GroupItemInfo> groupItemInfos;
	private MainActivity mainActivity;
	public boolean success=false;
	
	public GroupsOverlay(MainActivity mainActivity, ArrayList<GroupItemInfo> groupItemsToShowOnMap) {
		super(boundCenterBottom(mainActivity.getResources().getDrawable(R.drawable.group_marker_35)));
		Log.i(TAG, "Konstruktor GroupsOverlay");
		this.mainActivity = mainActivity;		
		this.updateAllOverlayItems(groupItemsToShowOnMap);	
	}
		
	public void addOverlayItem(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	    
    public void updateAllOverlayItems(ArrayList<GroupItemInfo> items) {        
    	if (items!=null) { 
    		this.groupItemInfos = items;
	        mOverlays.clear();
	        ArrayList<OverlayItem> newItems = new ArrayList<OverlayItem>();     	
	        
	        for (GroupItemInfo gii: items) {
	        	OverlayItem oi = new OverlayItem(
						new GeoPoint( gii.latitudeE6, gii.longitudeE6),
						gii.name,
						"groupID="+gii.groupId);
				newItems.add(oi);        	
	        }
	                
	        mOverlays=(ArrayList<OverlayItem>) newItems.clone();	        
	        
	        //Log.i(TAG,"updateAllOverlayItems(). new size:"+mOverlays.size());
	        setLastFocusedIndex(-1);
	        populate();	        
        }
    } 
    
	
	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		GroupItemInfo gii = groupItemInfos.get(index);
				
		LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(mainActivity.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.groups_details_dialog,
		                               (ViewGroup) mainActivity.findViewById(R.id.groups_details_layout_root));

		TextView text = (TextView) layout.findViewById(R.id.groups_details_text);
		text.setText(
				Html.fromHtml("<b>Latitude:</b> "+gii.latitudeE6/1E6+
						"<br><b>Longitude:</b> "+gii.longitudeE6/1E6+
						"<br><b>Number of Members:</b> "+ gii.memberCount));
		
		//Prepare the Intent for creating a group at this foursquare venue
		final Intent i = new Intent(mainActivity.getApplicationContext(), GroupInfoActivity.class);
		i.putExtra("group_id", gii.groupId);   
		
		String title=gii.name;
		if (title==null || title.equals("")) title=" ";
		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setTitle(title)
	  			.setView(layout)
	  			.setIcon(R.drawable.group_marker_24)
	  			.setCancelable(true)
	  			.setPositiveButton("Details", new DialogInterface.OnClickListener() {
		  				public void onClick(DialogInterface dialog, int id) {		  					   	
		  		        	mainActivity.startActivity(i);
		  				}
		  			})
		  		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  			public void onClick(DialogInterface dialog, int id) {
			  				dialog.cancel();
			  			}
		  			});
		builder.show();
		return true;
	}	
	
}
