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
package de.tudresden.inf.rn.mobilis.jclient.xhunt.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.RouteManagement;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.Route;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.Station;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.GameDataIQ;


public class GameDataIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		RouteManagement rm = RouteManagement.getInstance();
		ArrayList<Route> routes = new ArrayList<Route>();
		ArrayList<Station> stations = new ArrayList<Station>();
		
		int eventType = xpp.getEventType();
		boolean done = false;
		do {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("Route")) {
					
					//(xpp.nextText()) );
					Route r = new Route(xpp.getAttributeValue(0),
										xpp.getAttributeValue(1),
										xpp.getAttributeValue(2),
										xpp.getAttributeValue(3));
										
					Map<Integer, String> stationsMap = new HashMap<Integer, String>();		
					
					do {
						xpp.next();
						if (xpp.getEventType() == XmlPullParser.END_TAG) {
							//zeigt auf END-Tag von Route
							break; // beende die Schleife
						}
						if (xpp.getName().equals("stop")) {
							int stopId = Integer.parseInt(xpp.getAttributeValue(0));
							String s = xpp.nextText(); // zeigt auf END_TAG, wenn erfolgreich
							stationsMap.put(stopId, s);
						}
					} while(true);
						
					r.setStations(stationsMap);
					routes.add(r);
				} else if (xpp.getName().equals("Station")) {					
					Station s = new Station(xpp.getAttributeValue(0),
											xpp.getAttributeValue(1),
											Integer.parseInt(xpp.getAttributeValue(2)),
											Integer.parseInt(xpp.getAttributeValue(3)));
					stations.add(s);										
				}
				eventType = xpp.next();				
			} else if (eventType == XmlPullParser.END_TAG &&
					   xpp.getName().equals(GameDataIQ.elementName)) {
                //End-Tag found
				done = true;
			} else {
				eventType = xpp.next();				
			}
		} while (!done);
		
		rm.setRoutes(routes);
		rm.setStations(stations);
		rm.updateStationsWithRoutes();
		
		GameDataIQ iq = new GameDataIQ(rm);
		
		return iq;
	}
}
