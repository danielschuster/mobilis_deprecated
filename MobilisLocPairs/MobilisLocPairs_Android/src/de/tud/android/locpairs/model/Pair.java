package de.tud.android.locpairs.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;

/**
 * The Class Pair.
 */
public class Pair {
	
	/** The cards. */
	private HashMap<String,Card> cards;
	
	/** The state. */
	private boolean state = false;
	
	/** The bmp pair. */
	private Bitmap bmpPair;
	
	/** The address. */
	private String address;


	/**
	 * Instantiates a new pair.
	 *
	 * @param address the address
	 * @param bmp the bmp
	 */
	public Pair(String address, Bitmap bmp){
		bmpPair = bmp;
		this.address = address;
		this.cards = new HashMap<String,Card>();
	}
	
	/**
	 * Check for pair.
	 *
	 * @return true, if successful
	 */
	public boolean checkForPair(){
		if(state) return true;
		for(Card card: cards.values()){
			if(!card.isState()) return false;
		}
		state =true;
		return true;
	}
	
	/**
	 * Gets the cards.
	 *
	 * @return the cards
	 */
	public HashMap<String,Card> getCards() {
		return cards;
	}

	/**
	 * Sets the cards.
	 *
	 * @param cards the cards
	 */
	public void setCards(HashMap<String,Card> cards) {
		this.cards = cards;
	}

	/**
	 * Adds the card.
	 *
	 * @param card the card
	 */
	public void addCard(Card card){
		this.cards.put(card.getCardID(),card);
		card.setPair(this);
	}
	
	/**
	 * Removes the card.
	 *
	 * @param card the card
	 */
	public void removeCard(Card card){
		this.cards.remove(card);
		card.setPair(null);
	}
	
	/**
	 * Checks if is state.
	 *
	 * @return true, if is state
	 */
	public boolean isState(){
		return state;
	}
	
	/**
	 * Gets the bitmap.
	 *
	 * @return the bitmap
	 */
	public Bitmap getBitmap() {
		return bmpPair;
	}
	
	/**
	 * Gets the pair id.
	 *
	 * @return the pair id
	 */
	public String getPairID(){
		return address;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("--- Paar ---").append("\n");
		sb.append("Bildadresse: ").append(address).append("\n");
		
		for (Card card:cards.values()){
			sb.append("    ").append(card).append("\n").append("\n");
		}
		
		return sb.toString();
	}
}
