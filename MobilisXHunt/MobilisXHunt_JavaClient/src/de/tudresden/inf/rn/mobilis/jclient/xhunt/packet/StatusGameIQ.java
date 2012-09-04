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

import java.util.ArrayList;
import org.jivesoftware.smack.packet.IQ;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.XHuntPlayer;

public class StatusGameIQ extends IQ {

	public static final String elementName = "query";
    public static final String namespace = "mobilisxhunt:iq:statusgame";
	
	
    private int round;
    private String gameId;
    private String chatId, chatPassword;
	private ArrayList<XHuntPlayer> players;
	
	
	public StatusGameIQ() {
		super();
		this.setType(IQ.Type.SET);
		players = new ArrayList<XHuntPlayer>();
	}
	
	// Setter & Getter
	public void setGameId(String gameid) {
		this.gameId=gameid;
	}
	public String getGameId() {
		return gameId;
	}	
	public void setRound(int round) {
		this.round=round;
	}
	public int getRound() {
		return round;
	}
	public void setChatId(String chatId) {
		this.chatId=chatId;
	}
	public String getChatId() {
		return chatId;
	}	
	public void setChatPassword(String chatPassword) {
		this.chatPassword=chatPassword;
	}
	public String getChatPassword() {
		return chatPassword;
	}	
	public void setPlayers(ArrayList<XHuntPlayer> players) {
		this.players=players;
	}
	public ArrayList<XHuntPlayer> getPlayers() {
		return players;
	}
	public void addPlayer(XHuntPlayer player) {
		players.add(player);
	}
	
		
	@Override
	public String getChildElementXML() {
		StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	buf.append("<gameid>").append(this.gameId).append("</gameid>\n");
    	buf.append("<round>").append(this.round).append("</round>\n");
    	buf.append("<chatid>").append(this.chatId).append("</chatid>\n");
    	buf.append("<chatpassword>").append(this.chatPassword).append("</chatpassword>\n");
    	    	
    	for (XHuntPlayer p: players) {
    		buf.append("<Player jid=\""+p.getJid()+"\" name=\""+p.getName()+"\" moderator=\""+p.getModerator()+"\" mrx=\""+p.getMrX()+"\" ready=\""+p.getReady()+"\"></Player>\n");    		
    	}
    	    	    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
	}


}
