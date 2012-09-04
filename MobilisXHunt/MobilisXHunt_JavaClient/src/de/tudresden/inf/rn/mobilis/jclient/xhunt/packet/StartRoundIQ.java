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

import de.tudresden.inf.rn.mobilis.jclient.xhunt.TicketManagement;

public class StartRoundIQ extends IQ {

	public static final String elementName = "query";
    public static final String namespace = "mobilisxhunt:iq:startround";
	
	private int round;
	private HashMap<String, TicketManagement> players;
	
	public StartRoundIQ() {
		super();
		this.setType(IQ.Type.SET);	
		players = new HashMap<String, TicketManagement>();
	}
	
	// Setter & Getter
	public void setRound(int round) {
		this.round=round;
	}
	public int getRound() {
		return round;
	}
	public void setPlayers(HashMap<String, TicketManagement> players) {		
		this.players = players;
	}
	public HashMap<String, TicketManagement> getPlayers() {
		return players;
	}
	public void addPlayer(String jid, TicketManagement tm) {
		players.put(jid, tm);
	}
	
	
	@Override
	public String getChildElementXML() {
		StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	buf.append("<round>").append(this.round).append("</round>\n");
    	
    	for (String jid : players.keySet()) {
    		buf.append("<Player jid=\""+jid+"\" busTicket=\""+players.get(jid).getBusTicketNumber()+"\" tramTicket=\""+players.get(jid).getTramTicketNumber()
    				+"\" railwayTicket=\""+players.get(jid).getRailwayTicketNumber()+"\" blackTicket=\""+players.get(jid).getBlackTicketNumber()
    				+"\" doubleTicket=\""+players.get(jid).getDoubleTicketNumber()+"\" waitingTicket=\""+players.get(jid).getWaitingTicketNumber()+"\"></Player>\n");    		
    	}
    	    	    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
	}
	
}
