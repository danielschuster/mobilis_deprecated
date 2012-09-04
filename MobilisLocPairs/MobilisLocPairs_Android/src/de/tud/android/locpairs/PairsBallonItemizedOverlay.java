package de.tud.android.locpairs;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import com.google.android.maps.MapView;

/**
 * An abstract extension of BallonItemizedOverlay for displaying the picture information
 * of a scanned card depending on the card position.
 * 
 */
public class PairsBallonItemizedOverlay extends BalloonItemizedOverlay<CardMarkerOverlay> {

	/** The m_overlays. */
	private ArrayList<CardMarkerOverlay> m_overlays = new ArrayList<CardMarkerOverlay>();
	
	/** The c. */
	private Context c;
	
	/** The timer handler. */
	private Handler timerHandler = new Handler();
	
	/**
	 * Instantiates a new pairs ballon itemized overlay.
	 * 
	 * @param defaultMarker
	 *            the default marker
	 * @param mapView
	 *            the map view
	 */
	public PairsBallonItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		c = mapView.getContext();
	}

	/**
	 * Adds the overlay.
	 * 
	 * @param overlay
	 *            the overlay
	 */
	public void addOverlay(CardMarkerOverlay overlay) {
	    m_overlays.add(overlay);
	    populate();
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#createItem(int)
	 */
	@Override
	protected CardMarkerOverlay createItem(int i) {
		return m_overlays.get(i);
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#size()
	 */
	@Override
	public int size() {
		return m_overlays.size();
	}

	/* (non-Javadoc)
	 * @see de.tud.android.locpairs.BalloonItemizedOverlay#onBalloonTap(int)
	 */
	@Override
	protected boolean onBalloonTap(int index) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.tud.android.locpairs.BalloonItemizedOverlay#onTap(int)
	 */
	public boolean onTap(int index){
		return false;		
	}
	
	/**
	 * Opens the picture scanned for 10 seconds aufter the scan.
	 * 
	 * @param overlay
	 *            the overlay
	 */
	public void openBalloon(CardMarkerOverlay overlay){
			super.openBalloon(m_overlays.indexOf(overlay));
			timerHandler.postDelayed(timerRunnable, 10000);
	}
	
	/**
	 * Close balloon.
	 * 
	 * @param overlay
	 *            the overlay
	 */
	public void closeBalloon(CardMarkerOverlay overlay){
		super.closeBalloon();
	}
	
	// Timer Handler for waiting to start game
	/** The timer runnable. */
	private Runnable timerRunnable = new Runnable() {
		public void run() {
			closeBalloon();
		}
	};
}
