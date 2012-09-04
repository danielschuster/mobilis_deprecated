package de.tud.server.model;


//import javax.persistence.*;


/**
 * The class to describe a locateable item.
 * 
 * @author Michael Ameling, Kathrin Saemann
 *
 */

//@Entity
//@PrimaryKeyJoinColumn(name="ITEM_ID")
public class LocatableItem extends Item {
	
	private Location location;
	
	
		
	/**
	 * @return location
	 */
	//@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER, targetEntity=Location.class)
	//@JoinColumn(name="LOCATION_ID")
	public Location getLocation(){
		return location;
	};
	
	public void setLocation(Location location){
		this.location = location;
	}

}
