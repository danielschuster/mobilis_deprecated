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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Route {
	private String id;
	private String type;
	private String start;
	private String end;
	private Map<Integer, String> stations;
	
	public Route(String id, String type, String start, String end){
		this.id = id;
		this.type = type;
		this.start = start;
		this.end = end;
		this.stations = new HashMap<Integer, String>();
	}

	public boolean addStation(int position, Station station){
		return (station.getId().equals(stations.put(position, station.getId())));
	}
	public boolean addStation(int position, String stationId){
		return (stationId.equals(stations.put(position, stationId)));
	}
	
	public Integer getPositionOfStation(Station station){
		for (Integer position : stations.keySet()){
			if (stations.get(position).equals(station.getId())) return position;
		}
		return -1;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public Map<Integer, String> getStations() {
		return stations;
	}

	public void setStations(Map<Integer, String> stops) {
		this.stations = stops;
	}
}
