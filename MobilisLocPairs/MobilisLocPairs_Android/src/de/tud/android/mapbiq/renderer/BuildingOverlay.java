package de.tud.android.mapbiq.renderer;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import de.javagis.jgis.geometry.Polygon;
import de.tud.android.mapbiq.R;
import de.tud.iiogis.wfs.WFSLayer;
import de.tud.iiogis.wfs.WFSServer;
import de.tud.server.model.BuildingPart;
import de.tud.server.model.LocatableItem;
import de.tud.server.model.LocationModelAPI;

/**
 * This class represents an overlay for buildings that can be added.
 * 
 * @author Franz Josef Gr�neberger
 * 
 */

public class BuildingOverlay extends Overlay {

	private int floor = 0;

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		// buildings have no shadows; therefore return without drawing if shadow
		// == true
		if (shadow == true) {
			return;
		}
		
		try {
		
		// create a new painter
		Paint paint = new Paint();
		GeoPoint geoPoint = new GeoPoint((int) (1 * 1E6), (int) (1 * 1E6));
		Point screenCoordinates = new Point();
		Path path = new Path();
		
//		//draw background
//		// create a new painter
//		paint.setColor(R.color.mapBackground);
//		paint.setStyle(Paint.Style.FILL);
//		
//		geoPoint = new GeoPoint((int) (1 * 1E6), (int) (1 * 1E6));
//		screenCoordinates = new Point();
//		mapView.getProjection().toPixels(geoPoint, screenCoordinates);
//		path.moveTo(screenCoordinates.x, screenCoordinates.y);
//
//		geoPoint = new GeoPoint((int) (70 * 1E6), (int) (1 * 1E6));
//		screenCoordinates = new Point();
//		mapView.getProjection().toPixels(geoPoint, screenCoordinates);
//		path.lineTo(screenCoordinates.x, screenCoordinates.y);
//
//		geoPoint = new GeoPoint((int) (70* 1E6), (int) (70 * 1E6));
//		screenCoordinates = new Point();
//		mapView.getProjection().toPixels(geoPoint, screenCoordinates);
//		path.lineTo(screenCoordinates.x, screenCoordinates.y);
//		
//		geoPoint = new GeoPoint((int) (1 * 1E6), (int) (70 * 1E6));
//		screenCoordinates = new Point();
//		mapView.getProjection().toPixels(geoPoint, screenCoordinates);
//		path.lineTo(screenCoordinates.x, screenCoordinates.y);
//		
//		// draw the path on bitmapCanvas
//		canvas.drawPath(path, paint);
//		
		

		// draw line with antialiasing
        DrawFilter drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG);
        canvas.setDrawFilter(drawFilter);
		
		// create a new painter
		paint.setColor(R.color.building);
		paint.setStyle(Paint.Style.FILL);

		// get the layer information of TUD_INF_E0
		WFSServer wfsServer = LocationModelAPI.getSelectedBuilding();

		// check whether WFS data is available
		if (wfsServer == null) {
			// no information available --> nothing has to be drawn
			return;
		}
		ArrayList<WFSLayer> toDrawLayer = wfsServer.getWfsPolygonLayers();
		ArrayList<BuildingPart> toDraw = new ArrayList<BuildingPart>();

		ArrayList<LocatableItem> items = toDrawLayer.get(floor).getItems();
		for (int j = 0; j < items.size(); j++) {
			toDraw.add((BuildingPart) items.get(j));
		}

		// convert polygon points to map points
		Iterator<BuildingPart> toDrawIt = toDraw.iterator();
		while (toDrawIt.hasNext()) {
			// current item
			BuildingPart part = toDrawIt.next();
			// current polygon
			Polygon polygon = (Polygon) part.getGeometry();
			// corner points of this polygon
			de.javagis.jgis.geometry.Point[] points = polygon.asPoints();

			// create new path for every ground
			path = new Path();
			for (int i = 0; i < points.length; i++) {
				// get on of the ground vertices
				double lat = points[i].getY();
				double lon = points[i].getX();
				geoPoint = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));

				// transform current position to screen coordinates
				screenCoordinates = new Point();
				mapView.getProjection().toPixels(geoPoint, screenCoordinates);

				if (i == 0) {
					// path has to be started --> moveTo
					path.moveTo(screenCoordinates.x, screenCoordinates.y);
				} else {
					// path has to be resumed --> lineTo
					path.lineTo(screenCoordinates.x, screenCoordinates.y);
				}

			}

			// draw the path on bitmapCanvas
			canvas.drawPath(path, paint);

		}
		} catch (Exception e) {
			Log.e("BuildingOverlay", "needs more time to get buildings");
		}
		

	}

	public void setFloor(int i) {
		floor = i;
	}

}