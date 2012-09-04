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
package de.tudresden.inf.rn.mobilis.jclient.xhunt.services;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.MainView;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.Settings;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.XHuntPlayer;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.GameDataIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.GameOverIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.InitGameIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.PlayerExitGameIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.RoundStatusIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.StartRoundIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.StatusGameIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.TargetIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.UpdatePlayerIQ;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.packet.XHuntLocationIQ;

public class IQService implements PacketListener{
		
	private MainView mw;
	private Settings set;
	
	/**
	 * Constructor
	 * @param mw Main frame
	 * @param set Settings
	 */
	public IQService(MainView mw, Settings set){
		this.mw = mw;
		this.set = set;
	}
	

	@Override
	public void processPacket(Packet packet) {
		
		//GameDataIQ
		if (packet instanceof GameDataIQ){
			if(((GameDataIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("--> GameDataIQ received");
				mw.refreshTargets();
			}	
		}
		
		
		//GameOverIQ
		if (packet instanceof GameOverIQ){
			if(((GameOverIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("--> GameOverIQ received");
				GameOverIQ goIQ = ((GameOverIQ) packet);
				mw.addStatusMessage("Game over: "+ goIQ.getGameOverReason());
			}	
		}
		
		//InitGameIQ
		if (packet instanceof InitGameIQ){
			if(((InitGameIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("--> InitGameIQ received");
			}
		}
		
		//PlayerExitGameIQ
		if (packet instanceof PlayerExitGameIQ){
			if(((PlayerExitGameIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("<-- PlayerExitGameIQ received");
				PlayerExitGameIQ pegIQ = ((PlayerExitGameIQ) packet);
				if(pegIQ.getJid().equals(set.getJid())){
					mw.addStatusMessage("You left the game...Shuting down");
					try {
						Thread.sleep(4000);
						System.exit(0);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		//RoundStatusIQ
		if (packet instanceof RoundStatusIQ){
			if(((RoundStatusIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("<-- RoundStatusIQ received");
			}
		}
		
		//StartRoundIQ
		if (packet instanceof StartRoundIQ){
			if(((StartRoundIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("<-- StartRoundIQ received");
				StartRoundIQ strIQ = ((StartRoundIQ) packet);
				mw.setRound(strIQ.getRound());
			}
		}
		
		//StatusGameIQ
		if (packet instanceof StatusGameIQ){
			if(((StatusGameIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("--> StatusGameIQ received");
				StatusGameIQ sgIQ = ((StatusGameIQ) packet);
				for (XHuntPlayer player : sgIQ.getPlayers()){
					if (player.getJid().equals(set.getJid())){
						set.setMisterX(player.getMrX());
						set.setModerator(player.getModerator());
						set.setReady(player.getReady());
					}
				}
			}
		}
		
		//TargetIQ
		if (packet instanceof TargetIQ){
			if(((TargetIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("--> TargetIQ received");
				TargetIQ tarIQ = ((TargetIQ) packet);
				if (tarIQ.getJid().equals(set.getJid())){
					mw.setLastTarget(tarIQ.getTarget());
				}
			}
		}
		
		//UpdatePlayerIQ
		if (packet instanceof UpdatePlayerIQ){
			if(((UpdatePlayerIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("--> UpdatePlayerIQ received");
				UpdatePlayerIQ upIQ = ((UpdatePlayerIQ) packet);
				if (upIQ.getJid().equals(set.getJid())){
					set.setMisterX(upIQ.getIsMrX());
					set.setModerator(upIQ.getIsModerator());
					set.setReady(upIQ.getIsReady());
				}	
			}
		}
		
		//XHuntLocationIQ
		if (packet instanceof XHuntLocationIQ){	
			if(((XHuntLocationIQ) packet).getType() != IQ.Type.ERROR){
				mw.addStatusMessage("--> XHuntLocationIQ received");
			}
		}

	}

}
