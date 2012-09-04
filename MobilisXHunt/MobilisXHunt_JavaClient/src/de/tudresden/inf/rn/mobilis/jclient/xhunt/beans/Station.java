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

import java.util.ArrayList;

import org.jdesktop.swingx.mapviewer.GeoPosition;


public class Station {
	private GeoPosition geoPositionInDegree;
	private String id;
	private String name;
	private int longitudeMicroDegrees;
	private int latitudeMicroDegrees;
	private ArrayList<Route> routes;
	
	public Station(String id, String name, int longitudeMicroDegrees, int latitudeMicroDegrees){
		this.id = id;
		this.name = name;
		this.longitudeMicroDegrees = longitudeMicroDegrees;
		this.latitudeMicroDegrees = latitudeMicroDegrees;
		this.routes = new ArrayList<Route>();
		this.geoPositionInDegree = new GeoPosition(latitudeMicroDegrees / 1000000.f, longitudeMicroDegrees / 1000000.f);
	}

	public ArrayList<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(ArrayList<Route> routes) {
		this.routes = routes;
	}
	
	public boolean addRoutes(Route newRoute){
		boolean success = false;
		if (!routes.contains(newRoute)){
			success = routes.add(newRoute);
		}
		else return true;
		return success;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	
	public int getLongitudeInMicroDegrees(){
		return longitudeMicroDegrees;
	}

	
	public int getLatitudeInMicroDegrees(){
		return latitudeMicroDegrees;
	}

	public void setGeoPositionInDegree(GeoPosition geoPositionInDegree) {
		this.geoPositionInDegree = geoPositionInDegree;
	}

	public GeoPosition getGeoPositionInDegree() {
		return geoPositionInDegree;
	}
	
	
}
