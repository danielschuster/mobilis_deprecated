package de.tud.server.model;


//import javax.persistence.*;


/**
 * The class to describe a beacon.
 * 
 * @author Michael Ameling, Kathrin Saemann
 * 
 */

//@Entity
//@PrimaryKeyJoinColumn(name="ITEM_ID")
public class Beacon extends Device{
	
	private float signal;
		
	/**
	 * @return the signal
	 */
	public float getSignal(){
		return signal;
	}
	
	public void setSignal(float signal){
		this.signal = signal;
	}

	
}
