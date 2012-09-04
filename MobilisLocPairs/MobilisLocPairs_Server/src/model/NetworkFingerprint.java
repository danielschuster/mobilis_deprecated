package model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class NetworkFingerprint {
	private GeoPoint geoPoint;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	public GeoPoint getGeoPoint() {
		return geoPoint;
	}
	
	public void setFamily(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}
}
