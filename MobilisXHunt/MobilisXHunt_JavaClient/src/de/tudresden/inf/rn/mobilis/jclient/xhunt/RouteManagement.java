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
package de.tudresden.inf.rn.mobilis.jclient.xhunt;

import java.util.ArrayList;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.Route;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.Station;



public class RouteManagement {
	private ArrayList<Route> routes;
	private ArrayList<Station> stations;
	private GeoPosition mapCenter;
	
	private static RouteManagement instance;
	
	private RouteManagement(){
		routes = new ArrayList<Route>();
		stations = new ArrayList<Station>();
		mapCenter=null;
	}
	
	public static RouteManagement getInstance(){
		if (instance == null){
			instance = new RouteManagement();
		}
		return instance;
	}
	
	
	public ArrayList<Station> getStations() {
		return stations;
	}

	public void setStations(ArrayList<Station> stations) {
		this.stations = stations;
	}
	
	public void addStation(Station station){
		stations.add(station);
	}
	
	public void getStationsOfRoute(Route route){
		route.getStations();
	}

	public ArrayList<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(ArrayList<Route> routes) {
		this.routes = routes;
	}

	public void addRoute(Route route){
		routes.add(route);
	}
	
	public ArrayList<Route> getRoutesForStation(Station station){
		ArrayList<Route> result = new ArrayList<Route>();
		int pos = -1;
		for (Route r : routes){
			pos = r.getPositionOfStation(station);
			if (pos != -1){
				result.add(r);
			}
		}
		return result;
	}
	
	public Station getStation(String id){
		for (Station stop : stations){
			if (stop.getId().equals(id)) return stop;
		}
		return null;
	}
	
	public void updateStationsWithRoutes(){
		for (Station updateStation : getStations()){
        	ArrayList<Route> availableRoutes = getRoutesForStation(updateStation);
        	updateStation.setRoutes(availableRoutes);
        }
	}
	
	public GeoPosition getMapCenter() {
		if (mapCenter!=null)
			return mapCenter;
		
		//Calculate the Center of the map
		double maxLat=-91, maxLon=-181, minLat=91, minLon=181;
		double lat, lon;
		for (Station s : stations) {
			lat = s.getGeoPositionInDegree().getLatitude();
			lon = s.getGeoPositionInDegree().getLongitude();
			if (lat>maxLat) maxLat=lat;
			if (lat<minLat) minLat=lat;
			if (lon>maxLon) maxLon=lon;
			if (lon<minLon) minLon=lon;			
		}				
		mapCenter = new GeoPosition((maxLat+minLat)/2, (maxLon+minLon)/2);
		return mapCenter;
	}
	
	
}
