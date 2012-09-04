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
package de.tudresden.inf.rn.mobilis.groups;

import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;

/**
 * 
 * @author Robert Lübke
 *
 */
public final class ConstMGroups {
		
	//Intents
	public static final String INTENT_PREF_XMPP      = ConstMXA.INTENT_PREFERENCES;
		
	//Namespace
	public static final String NAMESPACE_SERVICES = Mobilis.NAMESPACE + "#services";
	public static final String NAMESPACE_GROUPING_SERVICE = NAMESPACE_SERVICES + "/GroupingService";
	
	public static final String XMPP_ADD_ROSTERITEM_IQ_CHILD = "query";
	public static final String XMPP_ADD_ROSTERITEM_IQ_NAMESPACE = "jabber:iq:roster";
	
	//Social Networks	
	public static final String FOURSQUARE_VENUES_URL		= "http://api.foursquare.com/v1/venues.json?";
	public static final String FOURSQUARE_VENUES_LATITUDE	= "geolat=";
	public static final String FOURSQUARE_VENUES_LONGITUDE	= "geolong=";

	
	//Activity Request Codes
	public static final int REQUEST_CODE_UPDATE			= 1;
	public static final int REQUEST_CODE_MEMBERINFO		= 2;
	public static final int REQUEST_CODE_GROUPINFO		= 3;
	public static final int REQUEST_CODE_MUC			= 4;
}
