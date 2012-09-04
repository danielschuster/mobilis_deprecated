package de.tud.android.mapbiq.locator;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.placelab.core.Coordinate;
import org.placelab.core.TwoDCoordinate;
import org.placelab.core.Types;
import org.placelab.mapper.AbstractMapper;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.WiFiBeacon;

import de.javagis.jgis.geometry.Point;
import de.javagis.jgis.geometry.PointFactory;
import de.tud.server.model.LocationModelAPI;
import de.tud.server.model.WLANAccessPoint;

/**
 * A Mapper that uses IIOGIS to store Beacons.
 * 
 */
public class IIOGISMapper extends AbstractMapper {
	protected boolean bulkPuts;
	protected int putCount;
	
		
	/**
	 * Create a new IIOGISMapper.  
	 * @param shouldCache whether or not to cache access Beacons in memory
	 */
	public IIOGISMapper(boolean shouldCache) {
		super(shouldCache);
	}
	
	/**
	 * Gets beacon by given id, return null if no beacon can be found.
	 * 
	 * @param id - id from beacon
	 */
	protected Vector findBeaconsImpl(String id) {
		
		WLANAccessPoint ap = LocationModelAPI.getAccessPoint(id);
		//if no beacon is found
		if(ap == null){
			return null;
		}
		WiFiBeacon wb = new WiFiBeacon();
		wb.setId(ap.getItemId());		
		TwoDCoordinate cor = new TwoDCoordinate(((Point) ap.getGeometry()).getY(), ((Point) ap.getGeometry()).getX());
		wb.setPosition(cor);
		Vector list = new Vector();
		list.add(wb);
		
		return list;
	}

	
	protected boolean putBeaconsImpl(String id, Vector beacons) {
		Enumeration iter = beacons.elements();
		while (iter.hasMoreElements()) {
			Beacon b = (Beacon)(iter.nextElement());
			Hashtable map = b.toHashMap();
						
			de.tud.server.model.Coordinate coord = new de.tud.server.model.Coordinate();
			coord.setLatitude(Double.parseDouble((String) map.get(Types.LATITUDE)));
			coord.setLongitude(Double.parseDouble((String)map.get(Types.LONGITUDE)));
			
			Point point = PointFactory.createPoint(coord.getLongitude(), coord.getLatitude());
			WLANAccessPoint ap = new WLANAccessPoint(point, null);
			ap.setItemId(id);
			LocationModelAPI.addAccessPoint(ap);
		}
		
		return true;
	}
	

	public Enumeration query(Coordinate c1, Coordinate c2) {
		double lat1,  lon1,  lat2,  lon2;
		double td;
		if ((!(c1 instanceof TwoDCoordinate)) || (!(c2 instanceof TwoDCoordinate)))  {
			return new Hashtable().keys(); // empty
		}
		lat1 = ((TwoDCoordinate)c1).getLatitude();
		lon1 = ((TwoDCoordinate)c1).getLongitude();
		lat2 = ((TwoDCoordinate)c2).getLatitude();
		lon2 = ((TwoDCoordinate)c2).getLongitude();
		// make sure lat lon in right orders
		if (lat1 > lat2) {
		  td = lat1;
		  lat1 = lat2;
		  lat2 = td;
		}
		if (lon1 > lon2) {
		  td = lon1;
		  lon1 = lon2;
		  lon2 = td;
		}
		
		//TODO
		/*
		try {
			final Statement s = connection.createStatement();
			String q = "select storage from " + TABLE_NAME + " where "
					+ Types.LATITUDE + " < " + lat2 + " AND " + Types.LATITUDE
					+ " > " + lat1 + " AND " + Types.LONGITUDE + " < " + lon2
					+ " AND " + Types.LONGITUDE + " > " + lon1 + " order by " + Types.ID;
			System.out.println("Executing query");
			final ResultSet rs = s.executeQuery(q);
			final ResultSetMetaData meta = rs.getMetaData();
			Enumeration it = new Enumeration() {
				boolean closed = false;
				Beacon nextBeacon = loadNext();
				
				private void close() throws SQLException {
					if (!closed) {
						closed = true;
						rs.close();
						s.close();
					}
				}
				
				public boolean hasMoreElements() {
					return nextBeacon != null;
				}
				
				public Object nextElement() {
					Beacon rv = nextBeacon;
					nextBeacon = loadNext();
					return rv;
				}

				public Beacon loadNext() {
					try {
						if (rs.next()) {
							return createBeacon(rs.getString(1));
						} else {
							close();
							return null;
						}
					} catch (SQLException ex) {
						ex.printStackTrace();
						return null;
					}
				}

				public void remove() {
					throw new UnsupportedOperationException("remove() not supported for iterators");
				}
			};
			
			return it;
		} catch (SQLException e) {
			System.err.println("failed " + e.getMessage());
			return new Hashtable().keys(); // empty
		}
		*/
		return null;
	}

	public boolean deleteAll() {
		return false;
	}

	public boolean isOpened() {
		return false;
	}

	public void startBulkPuts() {
		bulkPuts = true;
	}

	public void endBulkPuts() {
		
	}

	public boolean overrideOnPut() { return false; }

	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean open() {
		// TODO Auto-generated method stub
		return false;
	};
	
}
