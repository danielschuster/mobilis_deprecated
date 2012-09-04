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

import java.util.HashMap;
import org.jivesoftware.smack.packet.IQ;

public class RoundStatusIQ extends IQ {

	public static final String elementName = "query";
    public static final String namespace = "mobilisxhunt:iq:roundstatus";
	
	private int round;
	private HashMap<String,Boolean> players;
	
	public RoundStatusIQ() {
		super();
		this.setType(IQ.Type.SET);
		players = new HashMap<String,Boolean>();
	}
	
	// Setter & Getter	
	public void setRound(int round) {
		this.round=round;
	}
	public int getRound() {
		return round;
	}
	public void setPlayers(HashMap<String,Boolean> players) {
		this.players=players;
	}
	public HashMap<String,Boolean> getPlayers() {
		return players;
	}
	public void addPlayer(String playerJid, Boolean arrivalStatus) {
		players.put(playerJid, arrivalStatus);
	}
	
	
	
	
	
	@Override
	public String getChildElementXML() {
		StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	buf.append("<round>").append(this.round).append("</round>\n");
    	boolean arrivalStatus;
    	for (String jid: players.keySet()) {
    		arrivalStatus=players.get(jid);
    		if (arrivalStatus)
    			buf.append("<Player jid=\""+jid+"\" arrived=\"true\"></Player>\n");
    		else
    			buf.append("<Player jid=\""+jid+"\" arrived=\"false\"></Player>\n");
    	}
    	    	    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
	}

}
