/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.jclient.xhunt.beans;


public class LocationInfo {
	
	//private double altitude = 0;
	private double latitude = 0;
	private double longitude = 0;
	//private float speed = 0;
	//private Date timestamp;
	private String jid;
	
	/*public LocationInfo() {
		this.timestamp = new Date();
	}
	
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	
	public double getAltitude() {
		return altitude;
	}*/
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	/*public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}*/

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getJid() {
		return jid;
	}
}
