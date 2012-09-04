package de.tud.server.model;


import java.io.Serializable;
//import javax.persistence.*;


//@Entity
//@Inheritance(strategy=InheritanceType.JOINED)
public class Shape implements Serializable{
	
	//@Id
	private String id;

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
	
}
