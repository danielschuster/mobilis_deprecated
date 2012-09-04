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

import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.TargetIQ;


public class TargetIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		TargetIQ iq = new TargetIQ();
		int eventType = xpp.getEventType();
		boolean done = false;
		do {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("target")) {
					iq.setTarget( xpp.nextText() );
				} else if (xpp.getName().equals("finalDecision")) {
					iq.setFinalDecision( Boolean.parseBoolean(xpp.nextText()) );
				} else if (xpp.getName().equals("showMe")) {
					iq.setShowMe( Boolean.parseBoolean(xpp.nextText()) );
				} else if (xpp.getName().equals("jid")) {
					iq.setJid( xpp.nextText() );
				} else if (xpp.getName().equals("tickettype")) {
					iq.setTicketType( xpp.nextText() );
				} else if (xpp.getName().equals("round")) {
					iq.setRound( Integer.parseInt( xpp.nextText()) );
				}
				eventType = xpp.next();				
			} else if (eventType == XmlPullParser.END_TAG &&
					   xpp.getName().equals(TargetIQ.elementName)) {
                //End-Tag found
				done = true;
			} else {
				eventType = xpp.next();				
			}
		} while (!done);

		return iq;
	}

}
