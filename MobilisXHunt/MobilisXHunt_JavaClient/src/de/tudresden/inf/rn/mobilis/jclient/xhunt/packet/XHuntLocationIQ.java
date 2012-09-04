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
/** 
 * TODO: ...
 * @author Robert
 */

package de.tudresden.inf.rn.mobilis.jclient.xhunt.packet;

import org.jivesoftware.smack.packet.IQ;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.LocationInfo;

public class XHuntLocationIQ extends IQ {
    
    public static final String elementName = "query";
    public static final String namespace = "mobilisxhunt:iq:location";
	
	//private double altitude;
	private double latitude;
	private double longitude;
	private String jid;
	//private Date timestamp;
    
    public LocationInfo getLocation() {
    	LocationInfo l = new LocationInfo();
    	//l.setAltitude(altitude);
    	l.setLatitude(latitude);
    	l.setLongitude(longitude);
    	//l.setTimestamp(timestamp);
        return l;
    }

    public void setLocation(LocationInfo location) {
    	//this.altitude = location.getAltitude();
    	this.latitude = location.getLatitude();
    	this.longitude = location.getLongitude();
    	//this.timestamp = location.getTimestamp();
    }
    
    /*public double getAltitude() {
    	return this.altitude;
    }
    
    public void setAltitude(double altitude) {
    	this.altitude = altitude;
    }*/
    
    public double getLatitude() {
    	return this.latitude;
    }
    
    public void setLatitude(double latitude) {
    	this.latitude = latitude;
    }
    
    public double getLongitude() {
    	return this.longitude;
    }
    
    public void setLongitude(double longitude) {
    	this.longitude = longitude;
    }
   
    /*public Date getTimestamp() {
    	return this.timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
    	this.timestamp = timestamp;
    }*/
    
    public String getJid() {
    	return this.jid;
    }
    
    public void setJid(String jid) {
    	this.jid = jid;
    }

    public XHuntLocationIQ() {
        super();
        this.setType(IQ.Type.SET);
    }
    
    @Override
	public String getChildElementXML() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	//buf.append("<altitude>").append(this.altitude).append("</altitude>\n");
    	buf.append("<latitude>").append(this.latitude).append("</latitude>\n");
    	buf.append("<longitude>").append(this.longitude).append("</longitude>\n");
    	buf.append("<jid>").append(this.jid).append("</jid>\n");
    	//if (this.timestamp != null)
    	//	buf.append("<timestamp>").append(this.timestamp.getTime()).append("</timestamp>\n");
    	    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
    }
}
