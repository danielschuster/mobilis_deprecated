package de.tud.server.model;


import java.io.Serializable;
//import javax.persistence.*;
//import de.tud.model.help.IdGenerator;


//@Entity
//@Inheritance(strategy=InheritanceType.JOINED)
public class Item implements Serializable{
	
	protected String itemId;
	private String name;
	private String type;

	/**
	 * Constructor
	 *
	 */
	public Item(){
		//setItemId(IdGenerator.generateId());
	}
	
	/**
	 * @return the id
	 */
	//@Id
	public String getItemId() {
		return itemId;
	}

	/**
	 * @param id the id to set
	 */
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the name
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param name the name to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}

