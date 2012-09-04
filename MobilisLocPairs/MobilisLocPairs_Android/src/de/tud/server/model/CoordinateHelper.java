package de.tud.server.model;


import de.javagis.jgis.geometry.Point;
import de.javagis.jgis.geometry.PointFactory;


/**
 * Helper class to calculate Coordinates (WGS84) and distances.
 * 
 * @author Michael Ameling
 *
 */
public class CoordinateHelper {
	
	/**
	 * Gets Coordinate with max values. Only valid for Coordinats in
	 * stripe 5. (Germany)
	 * 
	 * @param coordinate
	 * @param radius in meters
	 * @return Coordinate - the Coordinate with max values.
	 */
	public static Coordinate getMaxCoordinate(Coordinate coordinate, double radius){
		Coordinate result = new Coordinate();
		//TODO 
		//x and y values are changed in geodetic systems
		//values in meters
		double x = getMetersOfLatitude(coordinate.getLatitude());
		//only for 5. stripe
		double y = getMetersOfLongitude(coordinate.getLongitude());
		//add radius
		x += radius;
		y += radius;
		
		result.setLatitude(getLatitudeOfMeters(x));
		result.setLongitude(getLongtitudeOfMeters(y));
		//TODO altitude value...
		result.setAltitude(0);
		
		return result;
	}
	
	/**
	 * Gets the Coordinate with min values.
	 * 
	 * @param coordinate
	 * @param radius
	 * @return Coordinate - the Coordinate with min values
	 */
	public static Coordinate getMinCoordinate(Coordinate coordinate, double radius){
		Coordinate result = new Coordinate();
		//TODO 
		//x and y values are changed in geodetic systems
		//values in meters
		double x = getMetersOfLatitude(coordinate.getLatitude());
		//only for 5. stripe
		double y = getMetersOfLongitude(coordinate.getLongitude());
		//add radius
		x -= radius;
		y -= radius;
		
		result.setLatitude(getLatitudeOfMeters(x));
		result.setLongitude(getLongtitudeOfMeters(y));
		//TODO altitude value...
		result.setAltitude(0);
		
		return result;
	}
	
	/**
	 * Gets double value in meters for latitude.
	 * 
	 * @param latitude
	 * @return double - double value in meters for 5. stripe
	 */
	public static double getMetersOfLatitude(double latitude){
		 return latitude * 110000;
	}
	
	/**
	 * Gets double value in meters of longitude.
	 * 
	 * @param longitude
	 * @return double - double value in meters for 5. stripe
	 */
	public static double getMetersOfLongitude(double longitude){
		 return 5500000 + ((longitude - 15) * 70000);
	}
	
	/**
	 * Gets double value as latitude of value in meters.
	 * 
	 * @param x
	 * @return double - double value of latitude in degrees
	 */
	public static double getLatitudeOfMeters(double x){
		return x / 110000;
	}
	
	/**
	 * Gets double value as longitude of value in meters.
	 * 
	 * @param x
	 * @return double - double value of longitude in degrees
	 */
	public static double getLongtitudeOfMeters(double y){
		return ((y - 5500000) / 70000) + 15;
	}
	
	public static Point getPointOfCoordinate(Coordinate coordinate){
		Point point = PointFactory.createPoint(
				getMetersOfLatitude(coordinate.getLatitude()), 
				getMetersOfLongitude(coordinate.getLongitude()));
		return point;
	}
}
