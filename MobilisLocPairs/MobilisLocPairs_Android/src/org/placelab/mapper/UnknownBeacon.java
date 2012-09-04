/*
 * Created on Aug 2, 2004
 *
 */
package org.placelab.mapper;

import java.util.Enumeration;
import java.util.Hashtable;

import org.placelab.core.Coordinate;
import org.placelab.core.Types;

/**
 * 
 */
public class UnknownBeacon extends Beacon {
	private Hashtable map;
	
	public void fromHashMap(Hashtable map) {
		this.map = map;
	}

	public Hashtable toHashMap() {
		Hashtable copy = new Hashtable();
		for(Enumeration e = map.keys(); e.hasMoreElements(); ) {
			Object key = e.nextElement();
			copy.put(key, map.get(key));
		}
		return copy;
	}

	public String getId() {
		return (String)map.get(Types.ID);
	}

	public String getType() {
		String type = (String)map.get(Types.TYPE);
		return (type != null ? type : Types.UNKNOWN);
	}

    public Coordinate getPosition() {
    	String lat = (String)map.get(Types.LATITUDE);
    	String lon = (String)map.get(Types.LONGITUDE);
    	return (lat != null && lon != null ? Types.newCoordinate(lat, lon) : null);
    }

    public int getMaximumRange() {
        return 0;
    }
}
