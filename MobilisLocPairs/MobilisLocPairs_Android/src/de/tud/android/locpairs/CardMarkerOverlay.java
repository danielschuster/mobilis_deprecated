/*
 * 
 */
package de.tud.android.locpairs;

import android.graphics.Bitmap;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * Show markers for each room that is used as scan point in a game.
 * Has information about cardId, bmp, geoposition and roomname.
 */
 class CardMarkerOverlay extends OverlayItem {
	
	/** The Constant UNOPENABLE. */
	public static final boolean UNOPENABLE = false;
	
	/** The Constant OPENABLE. */
	public static final boolean OPENABLE = true;
	
	/** The status. */
	private boolean status = false;
	
	/** The card id. */
	private String cardId;
	
	/** The bmp. */
	private Bitmap bmp;

	/**
	 * Instantiates a new card marker overlay.
	 * 
	 * @param cardId
	 *            the card id
	 * @param bmp
	 *            the bmp
	 * @param point
	 *            the point
	 * @param title
	 *            the title
	 * @param snippet
	 *            the snippet
	 */
	public CardMarkerOverlay(String cardId,Bitmap bmp,GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		this.bmp = bmp;
		this.cardId = cardId;
	}

	/**
	 * Gets the bitmap.
	 * 
	 * @return the bitmap
	 */
	public Bitmap getBitmap() {
		return bmp;
	}

	/**
	 * Checks if is status.
	 * 
	 * @return true, if is status
	 */
	public boolean isStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	/**
	 * Gets the card id.
	 * 
	 * @return the card id
	 */
	public String getCardId(){
		return cardId;
	}
	
}
