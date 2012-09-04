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

package de.tudresden.inf.rn.mobilis.jclient.xhunt.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.StartRoundIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.TicketManagement;

public class StartRoundIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		StartRoundIQ iq = new StartRoundIQ();
		int eventType = xpp.getEventType();
		boolean done = false;
		do {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("round")) {
					iq.setRound( Integer.parseInt( xpp.nextText()) );
				} else if (xpp.getName().equals("Player")) {
					iq.addPlayer(xpp.getAttributeValue(0), new TicketManagement(
							Integer.parseInt(xpp.getAttributeValue(1)),
							Integer.parseInt(xpp.getAttributeValue(2)),
							Integer.parseInt(xpp.getAttributeValue(3)),
							Integer.parseInt(xpp.getAttributeValue(4)),
							Integer.parseInt(xpp.getAttributeValue(5)),
							Integer.parseInt(xpp.getAttributeValue(6)) )
					);
				}
				eventType = xpp.next();				
			} else if (eventType == XmlPullParser.END_TAG &&
					   xpp.getName().equals(StartRoundIQ.elementName)) {
                //End-Tag found
				done = true;
			} else {
				eventType = xpp.next();				
			}
		} while (!done);

		return iq;
	}

}
