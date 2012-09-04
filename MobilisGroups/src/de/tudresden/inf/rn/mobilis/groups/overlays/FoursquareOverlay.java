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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.ISocialNetworkService;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare.PrimaryCategory;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare.Venue;
import de.tudresden.inf.rn.mobilis.groups.ApplicationManager;
import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.groups.activities.GroupCreateActivity;
import de.tudresden.inf.rn.mobilis.groups.activities.MainActivity;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;

/**
 * 
 * @author Robert Lübke
 *
 */
public class FoursquareOverlay extends ItemizedOverlay<OverlayItem> {

	/** The TAG for the Log. */
	private final static String TAG = "FoursquareOverlay";
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private List<Venue> foursquareVenues;
	public boolean success=false;
	
	public FoursquareOverlay(Context context, GeoPoint point) {
		super(boundCenterBottom(context.getResources().getDrawable(R.drawable.foursquare_marker_35)));		
		this.mContext = context;
				
		this.updateAllOverlayItems(point);
		
	}
		
	
	public boolean updateAllOverlayItems(GeoPoint point) {	

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
		foursquareVenues=null;
		ISocialNetworkService sns=null;		
		try {
			sns = ApplicationManager.getInstance().getMainActivity().getISNS();			
			foursquareVenues = sns.getFoursquareNearbyVenues(
					point.getLongitudeE6()/1E6,
					point.getLatitudeE6()/1E6,
					Integer.parseInt(pref.getString(mContext.getString(R.string.pref_overlays_foursquare_limit_key), "10")));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		success = foursquareVenues!=null;
		
		if (success) {
			mOverlays.clear();
			for (Venue v : foursquareVenues) {
				int lat_e6 = (int) (v.getGeolat()*1E6);
				int lon_e6 = (int) (v.getGeolong()*1E6);
				OverlayItem oi = new OverlayItem(
						new GeoPoint( lat_e6, lon_e6),
						v.getName(),
						"FoursquareID="+v.getId());
				this.addOverlay(oi);
			}
		}
		return success;
	}
	
	public void addOverlay(OverlayItem overlay) {
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
	
	@Override
	protected boolean onTap(int index) {
		Venue v = foursquareVenues.get(index);
		
		PrimaryCategory primaryCategory = v.getPrimaryCategory();
		String category="";
		String iconurl="";
		if (primaryCategory!=null) {
			category = primaryCategory.getFullpathname();
			iconurl = primaryCategory.getIconurl();
		}
		
		String address="";
		if (!v.getAdress().equals(""))
			address+= v.getAdress()+", ";
		if (!v.getCity().equals(""))
			address+= v.getCity()+", ";
		if (!v.getState().equals(""))
			address+= v.getState()+", ";
		  
		if (address.length()>=2)
			address = address.substring(0, address.length()-2);	
		
		MainActivity mainActivity = ApplicationManager.getInstance().getMainActivity();
		LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.foursquare_details_dialog,
		                               (ViewGroup) mainActivity.findViewById(R.id.foursquare_details_layout_root));

		TextView text = (TextView) layout.findViewById(R.id.foursquare_details_text);
		text.setText(
				Html.fromHtml("<b>Category:</b> "+category+
						"<br><b>Latitude:</b> "+v.getGeolat()+
						"<br><b>Longitude:</b> "+v.getGeolong()+
						"<br><b>Distance:</b> "+ v.getDistance()+"m"+
						"<br><b>Address:</b> "+ address));		
		ImageView image = (ImageView) layout.findViewById(R.id.foursquare_details_image);
		image.setImageResource(R.drawable.ic_contact_picture);
		
		// Download the foursquare icon image and display it:
		if (!iconurl.equals("")) {			
			URL myFileUrl = null;          
	          try {
	               myFileUrl = new URL(iconurl);
	          } catch (MalformedURLException e) {
	               // TODO Auto-generated catch block
	               e.printStackTrace();
	          }
	          try {
	               HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
	               conn.setDoInput(true);
	               conn.connect();
	               InputStream is = conn.getInputStream();	               
	               Bitmap bmImg = BitmapFactory.decodeStream(is);
	               image.setImageBitmap(bmImg);
	          } catch (IOException e) {
	               // TODO Auto-generated catch block
	               e.printStackTrace();
	          }
		}		
	  
		//Prepare the Intent for creating a group at this foursquare venue
		final Intent i = new Intent(mContext.getApplicationContext(), GroupCreateActivity.class);
      	i.putExtra("group_name", v.getName());
      	i.putExtra("group_description", category);
      	i.putExtra("group_type", "Group");
      	i.putExtra("group_address", address);
      	i.putExtra("group_latitude", (int) (v.getGeolat()*1E6));
      	i.putExtra("group_longitude", (int) (v.getGeolong()*1E6));
      	i.putExtra("group_radius", 0);
      	i.putExtra("group_privacy", "Open for everybody");
      	i.putExtra("group_link", "www");
		
	  AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	  builder.setTitle(v.getName())
	  			.setView(layout)
	  			.setIcon(R.drawable.foursquare_marker_24)
	  			.setCancelable(true)
	  			.setPositiveButton("Create Group here", new DialogInterface.OnClickListener() {
		  				public void onClick(DialogInterface dialog, int id) {		  					
		  		        	mContext.startActivity(i);
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
