package model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class GeoPoint {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToMany(mappedBy = "GeoPoint")
	private final List<NetworkFingerprint> fingerprints = new ArrayList<NetworkFingerprint>();
	
	private int longitude;
	private int latitude;
	
	public GeoPoint(){}
	
	public GeoPoint(int lat, int longi){
		longitude = longi;
		latitude = lat;
	}
	
	
	
	public List<NetworkFingerprint> getFingerprints() {
		return fingerprints;
	}

	public int getLongitude() {
		return longitude;
	}

	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}

	public int getLatitude() {
		return latitude;
	}

	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}
	
	@Override
	public String toString(){
		return new String ("Longitude: " + longitude + " --- Latitude: " + latitude);
	}

}
