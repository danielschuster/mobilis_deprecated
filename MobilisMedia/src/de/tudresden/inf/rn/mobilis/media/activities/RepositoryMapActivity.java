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
package de.tudresden.inf.rn.mobilis.media.activities;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.parcelables.ConditionParcel;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;

public class RepositoryMapActivity extends MapActivity
		implements RepositorySubActivityHandler.SubActivityListener {

	private RepositorySubActivityHandler subActivityHandler;
	private RepositoryMapOverlay mapOverlay;
	private MapView mapView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.repository_map);
		this.mapOverlay = new RepositoryMapOverlay(this);
		this.setupMapView();
		this.setupController();
	}
	
	private void setupMapView() {
		MapView mapView = (MapView) this.findViewById(R.id.map);
		this.mapView = mapView;
		mapView.setBuiltInZoomControls(true);
		mapView.setClickable(true);
		mapView.getOverlays().add(this.mapOverlay);
	}
	
	private void setupController() {
		MapController mapController = this.mapView.getController();
		Criteria lc = new Criteria();
		lc.setAccuracy(Criteria.ACCURACY_COARSE);
		lc.setAltitudeRequired(false);
		lc.setBearingRequired(false);
		lc.setCostAllowed(true);
		lc.setSpeedRequired(false);
		LocationManager  lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		String lp = lm.getBestProvider(lc, true);
		if (lp != null) {
			Location l = lm.getLastKnownLocation( lp );
			if (l != null) {
				GeoPoint g = new GeoPoint(
							(int) l.getLatitude() * 1000000,
							(int) l.getLongitude() * 1000000
						);
				mapController.animateTo(g);
			}
			mapController.setZoom(16);
		}
	}
	
	public MapView getMapView() {
		return this.mapView;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		this.subActivityHandler = new RepositorySubActivityHandler(this.getIntent());
		this.subActivityHandler.setSubActivityListener(this);
		this.subActivityHandler.subActivityRegister();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		this.subActivityHandler.subActivityUnregister();
	}
	
	public void onSubActivityShow() {
		this.mapOverlay.onOutdateRepositoryItems(this.mapView);
	}

	public void onSubActivityHide() { }

	public void onSubActivityOutdate() {
		this.mapOverlay.onOutdateRepositoryItems(this.mapView);
	}

	public void onSubActivityUpdate(RepositoryItemParcel[] items) {
		this.mapOverlay.onUpdateRepositoryItems(items);
	}

	public void onSubActivityUpdateError() { }

	public void onViewChanged(int latStartE6, int latEndE6, int lonStartE6, int lonEndE6) {
		ConditionParcel   c  = new ConditionParcel();
		ConditionParcel[] cc = new ConditionParcel[4];
		cc[0] = new ConditionParcel();
		cc[0].key   = ConstMMedia.database.SLICE_LONGITUDE_E6;
		cc[0].op    = ConditionParcel.OP_GE;
		cc[0].value = String.valueOf(lonStartE6);
		cc[1] = new ConditionParcel();
		cc[1].key   = ConstMMedia.database.SLICE_LONGITUDE_E6;
		cc[1].op    = ConditionParcel.OP_LE;
		cc[1].value = String.valueOf(lonEndE6);
		cc[2] = new ConditionParcel();
		cc[2].key   = ConstMMedia.database.SLICE_LATITUDE_E6;
		cc[2].op    = ConditionParcel.OP_GE;
		cc[2].value = String.valueOf(latStartE6);
		cc[3] = new ConditionParcel();
		cc[3].key   = ConstMMedia.database.SLICE_LATITUDE_E6;
		cc[3].op    = ConditionParcel.OP_LE;
		cc[3].value = String.valueOf(latEndE6);
		c.conditions = cc;
		c.op = ConditionParcel.OP_AND;
		this.subActivityHandler.subActivityUpdate(c);
	}
	
	public void onItemDisplay(RepositoryItemParcel item) {
		this.subActivityHandler.subActivityDisplay(item);
	}
	
	
}
