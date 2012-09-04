package org.placelab.core;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.placelab.spotter.NMEASentence;
import org.placelab.util.FixedPointLong;
import org.placelab.util.FixedPointLongException;
import org.placelab.util.StringUtil;



/**
 * A Measurement based on a reading from a GPS unit.  GPSMeasurements
 * are different from most Measurements in that they have a Coordinate
 * included with them that is read from the GPS.
 */
public class GPSMeasurement extends Measurement implements PositionMeasurement {
	private Hashtable entries;
	private Coordinate loc;

	public static final String[] gpsTypes = { Types.TIMEOFFIX,Types.STATUS,Types.LATITUDE,Types.LONGITUDE,
			  Types.SPEEDOVERGROUND,Types.COURSEOVERGROUND,Types.DATEOFFIX,
			  Types.MAGNETICVARIATION,Types.MAGNETICVARIATIONDIRECTION,
			  Types.MODE,Types.GPSQUALITY,Types.NUMOFSATELLITES,Types.HORIZONTALDILUTIONOFPRECISION,
			  Types.ANTENNAHEIGHT,Types.GEOIDALHEIGHT,Types.DIFFERENTIALGPSDATAAGE,
			  Types.DIFFERENTIALREFERENCESTATIONID };


	public Coordinate getPosition() {
		return loc;
	}

	public GPSMeasurement(long timestampInMillis, Coordinate loc, Hashtable fields) {
		super(timestampInMillis);
		this.loc = loc;
		this.entries = fields;
		this.entries.put(Types.LATITUDE, loc.getLatitudeAsString());
		this.entries.put(Types.LONGITUDE, loc.getLongitudeAsString());
	}
	
	public GPSMeasurement(long timestampInMillis, Coordinate loc) {
		super(timestampInMillis);
		this.loc = loc;
		this.entries = new Hashtable();
        entries.put(Types.LATITUDE, loc.getLatitudeAsString());
        entries.put(Types.LONGITUDE, loc.getLongitudeAsString());
	}
	
	/**
	 * Gets whether or not the fix data is valid for this GPSMeasurement.
	 * GPSMeasurements without valid fix data should not be relied upon
	 * for positioning.
	 */
	public boolean isValid() {
		String status = getField(Types.STATUS);
		if(status == null) return false;
		return StringUtil.equalsIgnoreCase(status,"A") && !loc.isNull();
	}
	
	/**
	 * Uses a heuristic based on number of satellites and satellite angles
	 * to predict if reading is likely to be accurate.
	 */
	public boolean isLikelyAccurate() {
		try {
			long hdopX10 = FixedPointLong.intValue(FixedPointLong.mult(
					           FixedPointLong.stringToFlong(getField(Types.HORIZONTALDILUTIONOFPRECISION)),
					           FixedPointLong.intToFlong(10)));
			int numSat = Integer.parseInt(getField(Types.NUMOFSATELLITES));
			if (numSat < 4) {
//				System.out.println("reject! " + numSat);
				return false;
			}
			if (hdopX10 > 80) {
//				System.out.println("rejectHD! " + hdopX10);
				return false;
			}
			return isValid();
		} catch (Exception ex) {
			System.out.println("Bad NEMA sentence at: " + this.getTimestamp() + " HDOP=" + getField(Types.HORIZONTALDILUTIONOFPRECISION) + " NUMSAT=" + getField(Types.NUMOFSATELLITES));
			return false;
		}
	}
	
	/**
	 * @param fieldName GPS field names from NMEASentence
	 * @return the field value
	 */
	public String getField(String fieldName) {
	    String entry = (String)entries.get(fieldName);
	    if(entry == null) return "";
	    return entry;
	}
	
	public String getType() {
	    return Types.GPS;
	}

    public String toLogString() {
        StringBuffer sb = new StringBuffer();
        sb.append(Types.TYPE+"="+Types.GPS+"|"+Types.TIME+"=" + this.getTimestamp() + "|");
        Enumeration it = entries.keys();
        while(it.hasMoreElements()) {
        	String key = (String)it.nextElement();
            String entryName = StringUtil.percentEscape(key);
            String entry = StringUtil.percentEscape((String)entries.get(key));
            sb.append(entryName + "=" + entry);
            if(it.hasMoreElements()) sb.append("|");
        }
        return sb.toString();
    }
 
    public String toShortString() {
    	String lat = (String) entries.get(Types.LATITUDE);
    	String lon = (String) entries.get(Types.LONGITUDE);
    	if(lat == null || lat.equals(""))
    		lat = "unknown";
    	
    	if(lon == null || lon.equals(""))
    		lon = "unknown";
     	return "Lat: " + lat+ "\nLon: " + lon;
    }
 
    // JWS - compressing estimates helps save RMS space on phones
    public byte[] toCompressedBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
    	try {
    		// write the type string first
    		dos.writeUTF(Types.GPS);
    		dos.writeLong(this.getTimestamp());
    		// JWS - using flongs to compress
    		dos.writeLong(FixedPointLong.stringToFlong(loc.getLatitudeAsString()));
    		dos.writeLong(FixedPointLong.stringToFlong(loc.getLongitudeAsString()));
    		for(int i=0;i<gpsTypes.length;i++) {
    			String s = (String) entries.get(gpsTypes[i]);
    			dos.writeUTF(s);
    		}
    	
        	dos.close();
    	} catch(IOException ioe) {
    		//ioe.printStackTrace();
    		return null; 
    	} catch(FixedPointLongException fple) {
    	    return null;
    	}
    	return baos.toByteArray();
    }
    
    public GPSMeasurement(long time, DataInputStream dis) throws IOException {
    	super(time);
    	
		Hashtable combinedEntry = new Hashtable();
		long lat = dis.readLong();
		long lon = dis.readLong();
		String value;
		for (int i = 0; i < gpsTypes.length; i++) {
			value = dis.readUTF();
			combinedEntry.put(gpsTypes[i], value);
		}

		try {
		    loc = Types.newCoordinate(FixedPointLong.flongToString(lat),
				FixedPointLong.flongToString(lon));
		} catch(FixedPointLongException fple) {
		    loc=Types.newCoordinate();
		}
		entries = combinedEntry;
    }
    
    // some state constants
    public static final int HAVE_A_LOCK = 0;
    public static final int DONT_HAVE_A_LOCK = 1;
    public static final int NO_INFO_RE_A_LOCK = 2;

	/**
	 * Roughly the same as isValid, but also returns whether there
	 * was no info about a lock (which can be assumed to be no lock)
	 * @see #HAVE_A_LOCK
	 * @see #DONT_HAVE_A_LOCK
	 * @see #NO_INFO_RE_A_LOCK
	 */
	public int haveALock() {
		String status = getField(NMEASentence.STATUS);
		if (status==null) return NO_INFO_RE_A_LOCK;
		if ( StringUtil.equalsIgnoreCase(status,"V") ) {
			return DONT_HAVE_A_LOCK;
		}
		else if ( StringUtil.equalsIgnoreCase(status,"A") ) {
			return HAVE_A_LOCK;
		}
		return NO_INFO_RE_A_LOCK;
	}
}
