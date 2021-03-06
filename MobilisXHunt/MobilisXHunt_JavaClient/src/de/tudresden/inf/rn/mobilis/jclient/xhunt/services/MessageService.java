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
package de.tudresden.inf.rn.mobilis.jclient.xhunt.services;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.MainView;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.Settings;

public class MessageService implements PacketListener{

	private MainView mw;
	private Settings set;
	
	/**
	 * Constructor
	 * @param mw Main frame
	 * @param set Settings
	 */
	public MessageService(MainView mw, Settings set){
		this.mw = mw;
		this.set = set;
	}
	
	
	@Override
	public void processPacket(Packet packet) {
		
		if (packet instanceof Message){
			
			Message mes = (Message) packet;
			
			if (mes.getBody() != null){
				mw.addStatusMessage(packet.getFrom() + " - " + mes.getBody());
			}
				
		}
		
	}

}
