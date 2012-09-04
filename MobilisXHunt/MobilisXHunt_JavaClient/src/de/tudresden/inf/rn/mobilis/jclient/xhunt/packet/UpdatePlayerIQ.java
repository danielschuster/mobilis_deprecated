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

public class UpdatePlayerIQ extends IQ {

	public static final String elementName = "query";
    public static final String namespace = "mobilisxhunt:iq:updateplayer";
	
	private String jid,name;
	private boolean moderator, mrx, ready;
	
    
	public UpdatePlayerIQ() {
		super();
		this.setType(IQ.Type.SET);
	}
	
	// Setter & Getter
	public void setJid(String jid) {
		this.jid=jid;
	}
	public String getJid() {
		return jid;
	}	
	public void setName(String name) {
		this.name=name;
	}
	public String getName() {
		return name;
	}	
	public void setIsModerator(boolean moderator) {
		this.moderator=moderator;
	}
	public boolean getIsModerator() {
		return moderator;
	}	
	public void setIsMrX(boolean mrx) {
		this.mrx=mrx;
	}
	public boolean getIsMrX() {
		return mrx;
	}	
	public void setIsReady(boolean ready) {
		this.ready=ready;
	}
	public boolean getIsReady() {
		return ready;
	}	
	
	
	@Override
	public String getChildElementXML() {
		StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
   		buf.append("<jid>"+jid+"</jid>\n");
        buf.append("<name>"+name+"</name>\n");
   		
   		buf.append("<status>");
   		
   		buf.append("<moderator>");
   		if (moderator) buf.append("true"); else buf.append("false");
    	buf.append("</moderator>\n");
    	
    	buf.append("<mrx>");
   		if (mrx) buf.append("true"); else buf.append("false");
    	buf.append("</mrx>\n");
    	
    	buf.append("<ready>");
   		if (ready) buf.append("true"); else buf.append("false");
    	buf.append("</ready>\n");
    	
    	buf.append("</status>");
    	    	    	    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
	}

}
