package model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * The Class PersistentGameId. It is used to be stored in a data base to provide the 
 * auto-generated String id.
 * 
 * @author Reik Mueller
 */
@Entity
public class PersistentGameId {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int id;

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return String.valueOf(id);
	}
}
