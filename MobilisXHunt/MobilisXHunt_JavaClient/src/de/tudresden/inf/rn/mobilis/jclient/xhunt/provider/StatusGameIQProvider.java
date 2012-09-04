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

import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.XHuntPlayer;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.StatusGameIQ;


public class StatusGameIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		StatusGameIQ iq = new StatusGameIQ();
		int eventType = xpp.getEventType();
		boolean done = false;
		do {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("gameid")) {
					iq.setGameId( xpp.nextText() );			
				} else if (xpp.getName().equals("round")) {
					iq.setRound( Integer.parseInt( xpp.nextText()) );
				} else if (xpp.getName().equals("chatid")) {
					iq.setChatId( xpp.nextText() );
				} else if (xpp.getName().equals("chatpassword")) {
					iq.setChatPassword( xpp.nextText() );
				} else if (xpp.getName().equals("Player")) {
					iq.addPlayer( new XHuntPlayer(
										xpp.getAttributeValue(0),
										xpp.getAttributeValue(1),
										Boolean.parseBoolean(xpp.getAttributeValue(2)),
										Boolean.parseBoolean(xpp.getAttributeValue(3)),
										Boolean.parseBoolean(xpp.getAttributeValue(4))
										) );
				}
				eventType = xpp.next();				
			} else if (eventType == XmlPullParser.END_TAG &&
					   xpp.getName().equals(StatusGameIQ.elementName)) {
                //End-Tag found
				done = true;
			} else {
				eventType = xpp.next();				
			}
		} while (!done);

		return iq;
	}

}
