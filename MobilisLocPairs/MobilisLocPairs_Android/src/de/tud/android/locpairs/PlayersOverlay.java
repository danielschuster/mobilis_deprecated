package de.tud.android.locpairs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.tud.android.locpairs.model.Game;
import de.tud.android.locpairs.model.Player;
import de.tud.android.mapbiq.R;

/**
 * Shows other players on map with different colors depending on their team.
 * 
 * @author Stefan Wagner
 * 
 */
public class PlayersOverlay extends Overlay {

	/** The bmp player. */
	private Bitmap bmpPlayer;
	
	/** The m_context. */
	private Context m_context;

	/**
	 * Instantiates a new players overlay.
	 * 
	 * @param context
	 *            the context
	 */
	public PlayersOverlay(Context context) {
		super();
		m_context = context;
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas, com.google.android.maps.MapView, boolean)
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		Projection projection = mapView.getProjection();
		if (shadow == false) {
			for (Player player : Game.getInstance().getPlayers().values()) {
				Point myPoint = new Point();
				GeoPoint position = new GeoPoint((int) (player.getPosition().getLatitude()*1E6),(int) (player.getPosition().getLongitude()*1E6));
				projection.toPixels(position, myPoint);
				
				if (player.getTeam().getTeamID() == 1){
					bmpPlayer = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.ingame_droid_green);
				}
				if (player.getTeam().getTeamID() == 2){
					bmpPlayer = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.ingame_droid_orange);
				}
				if (player.getTeam().getTeamID() == 3){
					bmpPlayer = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.ingame_droid_blue);
				}
				if (player.getTeam().getTeamID() == -1){
					bmpPlayer = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.ingame_droid_red);
				}
				
				canvas.drawBitmap(bmpPlayer, myPoint.x-bmpPlayer.getWidth()/2, myPoint.y-bmpPlayer.getHeight()/2, null);
			}
		}
	}
}