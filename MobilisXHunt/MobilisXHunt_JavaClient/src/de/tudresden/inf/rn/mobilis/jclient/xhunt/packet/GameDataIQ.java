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
package de.tudresden.inf.rn.mobilis.jclient.xhunt.packet;

import org.jivesoftware.smack.packet.IQ;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.RouteManagement;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.Route;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.Station;


public class GameDataIQ extends IQ {

	public static final String elementName = "query";
    public static final String namespace = "mobilisxhunt:iq:gamedata";
	private RouteManagement rm;
    
	public GameDataIQ(RouteManagement rm) {
		super();
		this.setType(IQ.Type.SET);
		this.rm=rm;
	}
	
	// Setter & Getter
		
	
	@Override
	public String getChildElementXML() {
		StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	//Convert Routes into XML-Structure 
    	for (Route r : rm.getRoutes()) {
    		buf.append("<Route id=\""+r.getId()+"\" type=\""+r.getType()+"\" start=\""+r.getStart()+"\" end=\""+r.getEnd()+"\">");    		
    		for (int i :r.getStations().keySet()) {
    			String stationId = r.getStations().get(i);
    			buf.append("<stop nr=\""+i+"\">"+stationId+"</stop>");    			
    		}    		
    		buf.append("</Route>");    		
    	}
    	
    	//Convert Stations into XML-Structure 
    	for (Station s : rm.getStations()) {    		
    		buf.append("<Station id=\""+s.getId()+"\" name=\""+s.getName()+"\" longitude=\""+s.getLongitudeInMicroDegrees()+"\" latitude=\""+s.getLatitudeInMicroDegrees()+"\"></Station>"); 	
    	}    	
    	    	    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
	}

}
