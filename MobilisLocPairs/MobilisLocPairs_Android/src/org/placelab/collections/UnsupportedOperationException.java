package org.placelab.collections;

/**
 * This doesn't exist in midp, so I'm making my own.
 */
public class UnsupportedOperationException extends RuntimeException {

	public UnsupportedOperationException() { super(); }
	public UnsupportedOperationException(String msg) { super(msg); }
	
}
