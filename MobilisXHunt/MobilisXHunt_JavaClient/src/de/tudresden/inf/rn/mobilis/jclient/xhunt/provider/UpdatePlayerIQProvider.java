/*******************************************************************************
 * Copyright (C) 2010 Technische Universitšt Dresden
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

import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.UpdatePlayerIQ;

public class UpdatePlayerIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		UpdatePlayerIQ iq = new UpdatePlayerIQ();
		int eventType = xpp.getEventType();
		boolean done = false;
		do {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("jid")) {
					iq.setJid( xpp.nextText() );
				} else if (xpp.getName().equals("name")) {
					iq.setName( xpp.nextText() );
				} else if (xpp.getName().equals("moderator")) {
					iq.setIsModerator( Boolean.parseBoolean(xpp.nextText()) );
				} else if (xpp.getName().equals("mrx")) {
					iq.setIsMrX( Boolean.parseBoolean(xpp.nextText()) );
				} else if (xpp.getName().equals("ready")) {
					iq.setIsReady( Boolean.parseBoolean(xpp.nextText()) );
				}
				eventType = xpp.next();				
			} else if (eventType == XmlPullParser.END_TAG &&
					   xpp.getName().equals(UpdatePlayerIQ.elementName)) {
                //End-Tag found
				done = true;
			} else {
				eventType = xpp.next();				
			}
		} while (!done);

		return iq;
	}

}
