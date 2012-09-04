/*
 * Created on Jun 29, 2004
 *
 */
package org.placelab.mapper;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.placelab.core.Coordinate;
import org.placelab.core.Types;
import org.placelab.util.StringUtil;

/**
 * Provides convenience methods for packing and unpacking Beacons
 * in a Mapper, for caching Beacons in memory when they are used,
 * and for storing Beacons of multiple types.  Only Mapper implementers
 * should be concerned with this class.
 */
public abstract class AbstractMapper implements Mapper {
	/** A storage space for Beacons that have already been parsed.
	 */
	private Hashtable cache=null;
	private Hashtable beaconTypeToClassMap = null;
	
	public static Class UNKNOWN_BEACON_CLASS;
	
	static{
		try {
			UNKNOWN_BEACON_CLASS = Class.forName("org.placelab.mapper.UnknownBeacon");
		} catch(ClassNotFoundException e) {
			throw new RuntimeException("Could not load class in AbstractMapper");
		}
	}
	
	public AbstractMapper(boolean shouldCache) {
		if (shouldCache) {
			cache = new Hashtable();
		} else {
			cache = null;
		}
		
		addBeaconClass(new WiFiBeacon());
		addBeaconClass(new BluetoothBeacon());
		addBeaconClass(new GSMBeacon());
	}
	
	public void addBeaconClass(String type, String className) {
		if (beaconTypeToClassMap == null) {
			beaconTypeToClassMap = new Hashtable();
		}
		beaconTypeToClassMap.put(type, className);
	}
	public void addBeaconClass(Beacon beacon) {
		addBeaconClass(beacon.getType(), beacon.getClass().getName());
	}
	
	public Beacon findBeacon(String id) {
		Vector b = findBeacons(id);
		if (b==null || b.isEmpty()) return null;
		return ((Beacon)((Vector)b).elementAt(0));
	}
	
	public Vector findBeacons(String id) {
		if (id == null) {
			return null;
		}

		if (cache != null) {
			Object cached = cache.get(id);
			if (cached != null) {
				if (cached instanceof String && ((String)cached).equals("null")) {
					return null;
				} else {
					return (Vector)cached;
				}
			}
		}
		
		Vector beacons = findBeaconsImpl(id);
		if (cache != null) {
			if (beacons == null) {
				cache.put(id, "null");
			} else {
				cache.put(id, beacons);
			}
		}
		return beacons;		
	}
	
	/* The following methods are used by MapLoaders to create new Mapper databases */
	public boolean putBeacon(String id, Beacon beacon) {
		/* invalidate the cache entry for this beacon */
		if(cache != null) cache.remove(id);
		Vector beacons = new Vector();
		beacons.addElement(beacon);
		return putBeaconsImpl(id, beacons);
	}
	
	public boolean putBeacons(String id, Vector beacons) {
		/* invalidate the cache entry for this beacon */
		if(cache != null) cache.remove(id);
		return putBeaconsImpl(id, beacons);
	}

	
//	protected String getBeaconType(Beacon b) {
//		if (beaconClassToTypeMap==null) {
//			beaconClassToTypeMap = new HashMap();
//		}
//		if (beaconTypeToClassMap==null) {
//			beaconTypeToClassMap = new HashMap();
//		}
//		String type = (String) beaconClassToTypeMap.get(b.getClass());
//		if (type == null) {
//			lastBeaconTypeId++;
//			type = ""+lastBeaconTypeId;
//			beaconClassToTypeMap.put(b.getClass(), type);
//			beaconTypeToClassMap.put(type, b.getClass().getName());
//		}
//		return type;
//	}
	private Class getBeaconClass(String type) {
		if (beaconTypeToClassMap==null || type==null) {
			return UNKNOWN_BEACON_CLASS;
		}
		String className = (String) beaconTypeToClassMap.get(type);
//		System.out.println("Hey!!!" + type);
		if (className == null) return UNKNOWN_BEACON_CLASS;
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return UNKNOWN_BEACON_CLASS;
		}
	}

	public Beacon createBeacon(String storageString) {
		/* assume we have key/value pairs
		 */
	    if(storageString == null) return null;
		Hashtable map = StringUtil.storageStringToHashMap(storageString);
		if (map==null || map.isEmpty()) {
		    // try looking for old format
			String[] sarr = StringUtil.split(storageString);
			if (sarr != null && sarr.length == 4) {
				/* this is the old format of the mapper db */
				return new WiFiBeacon(sarr);
			}
			return null;
		}
		String type = (String)map.get(Types.TYPE);
		return Beacon.create(getBeaconClass(type), map);
	}
	
	/**
	 * Unpacks a Vector of Beacons using {@link Mapper#createBeacon(String)}
	 * stored separated by newlines.
	 */
	protected Vector getBeaconsFromStorageString(String storage) {
		if (storage == null) return null;
		// jws - redundant?
//		if (storage.indexOf('\n') < 0) {
//			Beacon b = createBeacon(storage);
//			if (b==null) return null;
//			ArrayList l = new ArrayList();
//			l.add(b);
//			return l;
//		}
		String[] lines = StringUtil.split(storage, '\n');
		Vector list = new Vector();
		for (int i=0; i < lines.length; i++) {
			Beacon b = createBeacon(lines[i]);
			if (b == null) return null;
			list.addElement(b);
		}
		return list;
	}

	public abstract Enumeration query(Coordinate c1, Coordinate c2);
	
	protected abstract Vector findBeaconsImpl(String id);
	protected abstract boolean putBeaconsImpl(String id, Vector beacons);
	public boolean overrideOnPut() { return true; }
}
