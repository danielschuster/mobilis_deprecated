package de.tud.android.mapbiq.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import de.tud.server.model.LocationModelAPI;

/**
 * This class represents the wifi position overlay that can be added.
 */

public class WifiPositionOverlay extends Overlay {
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		
		//draw only if position data is available
		if(LocationModelAPI.getLastWifiCoordinateUpdate() == -1) {
			return;
		}
		
		//convert latitude and longitude of the current position to coordinates on screen
		double lat = LocationModelAPI.getWifiCoordinate().getLatitude();
		double lon = LocationModelAPI.getWifiCoordinate().getLongitude();
		GeoPoint geoPoint = new GeoPoint((int)(lat*1000000), (int)(lon*1000000));

		//transform current position to screen coordinates
		Point screenCoordinates = new Point();
		mapView.getProjection().toPixels(geoPoint, screenCoordinates);
		
		
		//draw green triangle at wifi position
		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.FILL);
		
		//calculate size of the triangle 
		float maxSize = 20;
		float currentLevel = mapView.getZoomLevel();
		float maxLevel = mapView.getMaxZoomLevel();
		float size = (maxSize/(maxLevel*maxLevel))*(3/2)*currentLevel*currentLevel-maxSize/maxLevel;
		
		//draw it finally
		Path path = new Path();
		path.moveTo(screenCoordinates.x, screenCoordinates.y);
		path.lineTo(screenCoordinates.x+size/2, screenCoordinates.y-size*3/2);
		path.lineTo(screenCoordinates.x-size/2, screenCoordinates.y-size*3/2);
		canvas.drawPath(path, paint);
		
		}
	
}