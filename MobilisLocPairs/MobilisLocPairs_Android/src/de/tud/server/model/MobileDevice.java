package de.tud.server.model;


//import javax.persistence.*;


/**
 * The class to describe a mobile device.
 * 
 * @author Michael Ameling, Kathrin Saemann
 *
 */
//@Entity
//@PrimaryKeyJoinColumn(name="ITEM_ID")
public class MobileDevice extends Device{
		
	private String[] sourceTypes;
		
	/**
	 * @return the source type
	 */
	public String[] getSourceTypes(){
		return sourceTypes;
	}

	/**
	 * @param sourceTypes - the sourceTypes to set
	 */
	public void setSourceTypes(String[] sourceTypes) {
		this.sourceTypes = sourceTypes;
	}
	
	
}
