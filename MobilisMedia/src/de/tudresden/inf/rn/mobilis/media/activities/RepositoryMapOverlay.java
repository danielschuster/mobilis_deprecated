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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;

public class RepositoryMapOverlay extends Overlay {

	private static class RepositoryItemIcon extends Point {
		public RepositoryItemParcel parcel;
		public RepositoryItemIcon(RepositoryItemParcel parcel) {
			this.parcel = parcel;
		}
	}
	private RepositoryMapActivity owner;
	private RepositoryItemIcon[] icons;	
	private Bitmap iconBitmap;
	private Paint iconPaint;
	private int currentZoomLevel = -1;
	
	public RepositoryMapOverlay(RepositoryMapActivity owner) {
		this.owner = owner;
		this.icons = new RepositoryItemIcon[0];
		this.iconBitmap = BitmapFactory.decodeResource( owner.getResources(), R.drawable.repository_item_icon );
		this.iconPaint = new Paint();
		this.iconPaint.setTextSize(16);
		this.iconPaint.setAntiAlias(true);
	}
	
	public void onOutdateRepositoryItems(MapView mapView) {	
		GeoPoint center = mapView.getMapCenter();
		int widthE6  = mapView.getLongitudeSpan();
		int heightE6 = mapView.getLatitudeSpan();
		int latStartE6 = center.getLatitudeE6()  - widthE6/2;
		int lonStartE6 = center.getLongitudeE6() - heightE6/2;
		int latEndE6   = center.getLatitudeE6()  + widthE6/2;
		int lonEndE6   = center.getLongitudeE6() + heightE6/2;
		this.owner.onViewChanged(latStartE6, latEndE6, lonStartE6, lonEndE6);
	}
	
	public void onUpdateRepositoryItems(RepositoryItemParcel[] items) {
		this.icons = new RepositoryItemIcon[items.length];
		for (int i = 0; i < items.length; i++) { 
			this.icons[i] = new RepositoryItemIcon(items[i]);
		}
		this.owner.getMapView().invalidate();
	}
	
	private void updateRepositoryItemPosition(Projection projection) {
		for (RepositoryItemIcon icon: this.icons) {
			RepositoryItemParcel item = icon.parcel;
			int latitude  = Integer.parseInt(item.slices.get(ConstMMedia.database.SLICE_LATITUDE_E6));
			int longitude = Integer.parseInt(item.slices.get(ConstMMedia.database.SLICE_LONGITUDE_E6));
			projection.toPixels(new GeoPoint(latitude, longitude), icon);
		}
	}
	
	private void drawRepositoryItemPosition(Canvas canvas) {
		final Bitmap b = this.iconBitmap;
		final Paint  p = this.iconPaint;
		for (RepositoryItemIcon icon: this.icons) {
			int x = icon.x - b.getWidth()/2;
			int y = icon.y - b.getHeight();
			canvas.drawBitmap(this.iconBitmap, x, y, p);
		}
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		if (this.currentZoomLevel < mapView.getZoomLevel())
			this.onOutdateRepositoryItems(mapView);
		this.currentZoomLevel = mapView.getZoomLevel();
		this.updateRepositoryItemPosition(mapView.getProjection());
		this.drawRepositoryItemPosition(canvas);
	}
	
	private RepositoryItemIcon checkTappedItems(int x, int y, MapView mapView) {
		int w = this.iconBitmap.getWidth();
		int h = this.iconBitmap.getHeight();
		for (RepositoryItemIcon icon: this.icons)
			if (x > icon.x-w/2 && x < icon.x+w/2
					&& y > icon.y-h && y < icon.y)
				return icon;
		return null; 
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		if (super.onTouchEvent(e, mapView)) return true;
		switch (e.getAction()) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_OUTSIDE:
			RepositoryItemIcon icon = this.checkTappedItems((int)e.getX(), (int)e.getY(), mapView);
			if (icon != null) owner.onItemDisplay(icon.parcel);
			this.onOutdateRepositoryItems(mapView);
			return true;
		default:
			return false;
		}
	}
	
}
