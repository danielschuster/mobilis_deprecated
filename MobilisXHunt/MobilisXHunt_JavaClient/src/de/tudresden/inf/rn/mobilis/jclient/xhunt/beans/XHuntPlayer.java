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

package de.tudresden.inf.rn.mobilis.jclient.xhunt.beans;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.TicketManagement;

public class XHuntPlayer {
	private String jid,name;
	private boolean moderator,mrx,ready;
	private TicketManagement tm;
	
	public XHuntPlayer(String jid, String name, boolean moderator,boolean mrx,boolean ready) {
		this.jid=jid;
		this.name=name;
		this.moderator=moderator;
		this.mrx=mrx;
		this.ready=ready;
		this.tm = new TicketManagement();
	}
	
	//Getter & Setter
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
	public void setModerator(boolean moderator) {
		this.moderator=moderator;
	}
	public boolean getModerator() {
		return moderator;
	}
	public void setMrX(boolean mrx) {
		this.mrx=mrx;
	}
	public boolean getMrX() {
		return mrx;
	}
	public void setReady(boolean ready) {
		this.ready=ready;
	}
	public boolean getReady() {
		return ready;
	}
	public TicketManagement getTicketManagement(){
		return tm;
	}
	
}
