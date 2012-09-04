package de.tud.server.model;


//import javax.persistence.*;


/**
 * The class to describe a device.
 * 
 * @author Michael Ameling, Kathrin Saemann
 *
 */
//@Entity
//@PrimaryKeyJoinColumn(name="ITEM_ID")
public class Device extends LocatableItem {
	
	private Person person;
	
	//@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	//@JoinColumn(name="PERSON_ID")
	public Person getPerson(){
		return person;
	}
	
	
	public void setPerson(Person person) {
		this.person = person;
	}
}
	

