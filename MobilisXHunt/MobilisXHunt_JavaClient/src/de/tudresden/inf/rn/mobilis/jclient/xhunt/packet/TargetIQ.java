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

package de.tudresden.inf.rn.mobilis.jclient.xhunt.packet;

import org.jivesoftware.smack.packet.IQ;

public class TargetIQ extends IQ {

	public static final String elementName = "query";
    public static final String namespace = "mobilisxhunt:iq:target";
	
	private String target, jid, ticketType;
	private boolean finalDecision, showMe;
	private int round;
	
	public TargetIQ() {
		super();
		this.setType(IQ.Type.SET);	
	}
	
	// Setter & Getter
	public void setTarget(String target) {
		this.target=target;
	}
	public String getTarget() {
		return target;
	}
	public void setFinalDecision(boolean finalDecision) {
		this.finalDecision=finalDecision;
	}
	public boolean getFinalDecision() {
		return finalDecision;
	}
	public void setShowMe(boolean showMe) {
		this.showMe = showMe;
	}
	public boolean getShowMe() {
		return showMe;
	}
	public void setJid(String jid) {
		this.jid=jid;
	}
	public String getJid() {
		return jid;
	}
	public void setTicketType(String ticketType) {
		this.ticketType=ticketType;
	}
	public String getTicketType() {
		return ticketType;
	}
	public void setRound(int round) {
		this.round=round;
	}
	public int getRound() {
		return round;
	}
	
	
	
	@Override
	public String getChildElementXML() {
		StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	buf.append("<target>").append(this.target).append("</target>\n");
    	if (finalDecision)
    		buf.append("<finalDecision>true</finalDecision>\n");
    	else
    		buf.append("<finalDecision>false</finalDecision>\n");
    	if (showMe)
    		buf.append("<showMe>true</showMe>\n");
    	else
    		buf.append("<showMe>false</showMe>\n");
    	buf.append("<jid>").append(this.jid).append("</jid>\n");
    	buf.append("<tickettype>").append(this.ticketType).append("</tickettype>\n");
    	buf.append("<round>").append(this.round).append("</round>\n");
    	    	    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
	}

}
