package de.tud.server.model;


/**
 * This class represents a Coordinate.
 *
 * @author Michael Ameling
 * @version 1.0
 */

public class Coordinate {

	public static final int DD_MM = 0;
	public static final int DD_MM_SS = 1;

	private String id;
	private double latitude=0;
	private double longitude=0;
	private float altitude;
	private double accuracy;
	private long time;


    public Coordinate(){
	}

	public Coordinate(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
	}

	public Coordinate(double latitude, double longitude, float altitude){
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
	}

	/**
	 * @return the altitude
	 */
	public float getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * @param accuracy the accuracy to set
	 */
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
}
