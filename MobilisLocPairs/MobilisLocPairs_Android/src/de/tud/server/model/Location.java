package de.tud.server.model;


import java.io.Serializable;
import java.util.Collection;
//import javax.persistence.*;


/**
 * This class represents any Location.
 * 
 * @author Michael Ameling
 * @version 1.0
 * */
//@Entity
//@PrimaryKeyJoinColumn(name="ITEM_ID")
public class Location extends Item implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * constructor
	 *
	 */
	public Location(){
		super();
	}
	
	private String extra;
	private Location location;
	private Coordinate coordinate;
	private Collection<Location> locations;
	private Collection<LocatableItem> locatableItems;
	private Shape shape;
	
	/**
	 * @return the extra
	 */
	public String getExtra() {
		return extra;
	}

	/**
	 * @param extra - the extra to set
	 */
	public void setExtra(String extra) {
		this.extra = extra;
	}

	/**
	 * @return the coordinate
	 */
	//@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	//@JoinColumn(name="COORDINATE_ID")
	public Coordinate getCoordinate() {
		return coordinate;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}
	
	/**
	 * @return the shape
	 */
	//@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	//@JoinColumn(name="SHAPE_ID")
	public Shape getShape() {
		return shape;
	}

	/**
	 * @param shape the shape to set
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	/**
	 * @return the location
	 */
	//@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	//@JoinColumn(name="LOCATION_ID")
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	
	/**
	 * Gets a Collection of Lacations that have reference to given Location.
	 * 
	 * @deprecated lazy fetch type - use findLocationsByReference(Location) in LocationDao instead
	 * 
	 * @return the location
	 */
	//@OneToMany(mappedBy="location", fetch=FetchType.LAZY)
	public Collection<Location> getLocations() {
		return locations;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocations(Collection<Location> locations) {
		this.locations = locations;
	}
	
	/**
	 * Gets a Collection of LocatableItems that have reference to given Location.
	 * 
	 * @deprecated lazy fetch type - use findLocatableItemsByReference(Location) in LocatableItemDao instead
	 * 
	 * @return the locatable items
	 */
	//@OneToMany(mappedBy="location", fetch=FetchType.LAZY)
	public Collection<LocatableItem> getLocatableItems() {
		return locatableItems;
	}
	
	/**
	 * @param collection the locatableItems to set
	 */
	public void setLocatableItems(Collection<LocatableItem> locatableItems) {
		this.locatableItems = locatableItems;
	}
}
