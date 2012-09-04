package de.tud.server.model;


//import javax.persistence.*;


/**
 * The class to describe two-dimensinal locations.
 * 
 * @author Michael Ameling, Kathrin Saemann
 */
//@Entity
//@PrimaryKeyJoinColumn(name="ITEM_ID")
public class TwoDLocation extends Location {


	//public TwoDLocation(String id, String type) {
	//	super(id, type);
	//}

	private double latitude;
	private double longitude;
	
	
	/**
	 * Gets the long value of the altitude.
	 * 
	 * @return latitude
	 */
	public double getLatitude(){
		return latitude;
	};
	
	/**
	 * Gets the long value of the longitude.
	 * 
	 * @return longitude
	 */
	public double getLongitude(){
		return longitude;
	};
	
	/**
	 * Sets the geodetic latitude for this location.
	 * 
	 * @param latitude - the latitude of the locaton
	 */
	public void setLatitude(double latitude){
		this.latitude = latitude;
	};
	
	/**
	 * Sets the geodetic longitude for this location.
	 * 
	 * @param longitude - the longitude of the locaton
	 */
	public void setLongitude(double longitude){
		this.longitude = longitude;
	};
	
	
}
