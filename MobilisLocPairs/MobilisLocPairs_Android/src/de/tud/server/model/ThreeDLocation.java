package de.tud.server.model;


//import javax.persistence.*;


/**
 * The class to describe a three dimensional location.
 * 
 * @author Michael Ameling, Kathrin Saemann
 *
 */

//@Entity
//@PrimaryKeyJoinColumn(name="ITEM_ID")
public class ThreeDLocation extends TwoDLocation {
	
	//public ThreeDLocation(String id, String type) {
	//	super(id, type);
	//}

	private float altitude;
	
	/**
	 * Gets the long value of the longitude.
	 * 
	 * @return alitude
	 */
	public float getAltitude(){
		return altitude;
	};
	
	/**
	 * Sets the geodetic altitude for this location.
	 * 
	 * @param altitude - the altitude of the locaton
	 */
	public void setAltitude(float altitude){
		this.altitude = altitude;
	};

	
}
