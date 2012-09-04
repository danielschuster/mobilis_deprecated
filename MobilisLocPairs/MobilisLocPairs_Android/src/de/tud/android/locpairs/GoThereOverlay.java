/*
 * 
 */
package de.tud.android.locpairs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.tud.android.locpairs.model.Game;
import de.tud.android.mapbiq.R;

/**
 * Shows other team player on the map where to go. A room is highlighted.
 * 
 * @author Stefan Wagner
 * 
 */
public class GoThereOverlay extends Overlay {

	/** The bmp. */
	private Bitmap bmp;
	
	/** The m_context. */
	private Context m_context;
	
	/** The point. */
	private Point point;
	
	/** The projection. */
	private Projection projection;
	
	/**
	 * Instantiates a new go there overlay.
	 * 
	 * @param context
	 *            the context
	 */
	public GoThereOverlay(Context context) {
		super();
		m_context = context;
		bmp = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.balloon_overlay_close);
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas, com.google.android.maps.MapView, boolean)
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		projection = mapView.getProjection();
		if (shadow == false && Game.getInstance().getGoThere()!=null) {
			point = new Point();
			projection.toPixels(new GeoPoint((int) (Game.getInstance().getGoThere().getLatitude()*1E6),(int) (Game.getInstance().getGoThere().getLongitude()*1E6)), point);
			Log.v("goThere", "goThere: " + Game.getInstance().getGoThere().getLatitude() + " " + Game.getInstance().getGoThere().getLongitude() + "    " + point.x + " " + point.y);
			canvas.drawBitmap(bmp, point.x-bmp.getWidth()/2, point.y-bmp.getHeight()/2, null);
		}
	}   
}