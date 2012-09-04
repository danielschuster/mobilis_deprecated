package de.tud.android.locpairs.model;

import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;

/**
 * The Class Card.
 */
public class Card {
	
	/** The Constant CARDSTATE_UNCOVERED. */
	public static final boolean CARDSTATE_UNCOVERED = true;
	
	/** The Constant CARDSTATE_COVERED. */
	public static final boolean CARDSTATE_COVERED = false;

	/** The barcode. */
	private String barcode;
	
	/** The pair id. */
	private String pairID;
	
	/** The position. */
	private GeoPosition position;
	
	/** The state. */
	private boolean state = false;
	
	/** The pair. */
	private Pair pair;
	
	/**
	 * Instantiates a new card.
	 *
	 * @param barcode the barcode
	 * @param address the address
	 * @param position the position
	 */
	public Card(String barcode, String address, GeoPosition position){
		this.pairID = address;
		this.barcode = barcode;
		this.position = position;
		this.state = CARDSTATE_COVERED;
	}
	
	/**
	 * Flip card.
	 */
	public void flipCard(){
		this.state = !this.state;
		pair.checkForPair();
	}
	
	/**
	 * Uncover card.
	 */
	public void uncoverCard(){
		this.state = true;
		pair.checkForPair();
	}
	
	/**
	 * Cover card.
	 */
	public void coverCard(){
		this.state = false;
	}
	
	/**
	 * Gets the barcode.
	 *
	 * @return the barcode
	 */
	public String getBarcode() {
		return barcode;
	}

	/**
	 * Sets the barcode.
	 *
	 * @param barcode the new barcode
	 */
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	/**
	 * Gets the pair id.
	 *
	 * @return the pair id
	 */
	public String getPairID() {
		return pairID;
	}

	/**
	 * Sets the pair id.
	 *
	 * @param address the new pair id
	 */
	public void setPairID(String address) {
		this.pairID = address;
	}

	/**
	 * Gets the position.
	 *
	 * @return the position
	 */
	public GeoPosition getPosition() {
		return position;
	}

	/**
	 * Sets the position.
	 *
	 * @param position the new position
	 */
	public void setPosition(GeoPosition position) {
		this.position = position;
	}

	/**
	 * Gets the pair.
	 *
	 * @return the pair
	 */
	public Pair getPair() {
		return pair;
	}

	/**
	 * Sets the pair.
	 *
	 * @param pair the new pair
	 */
	public void setPair(Pair pair) {
		this.pair = pair;
	}
	
	/**
	 * Checks if is state.
	 *
	 * @return true, if is state
	 */
	public boolean isState() {
		return state;
	}
	
	/**
	 * Gets the card id.
	 *
	 * @return the card id
	 */
	public String getCardID() {
		return barcode;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("--- Karte ---").append("\n");
		sb.append("Barcode: ").append(barcode).append("\n");
		sb.append("Adresse: ").append(pairID).append("\n");
		sb.append("Position: ").append(position).append("\n");
		if (pair != null) sb.append("PaarID: ").append(pair.getPairID()).append("\n");
		return sb.toString();
	}
}
