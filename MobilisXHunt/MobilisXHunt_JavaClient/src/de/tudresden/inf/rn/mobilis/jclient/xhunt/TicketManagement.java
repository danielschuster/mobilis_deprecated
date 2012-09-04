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
package de.tudresden.inf.rn.mobilis.jclient.xhunt;

/**
 * For every player TicketManagement holds the tickets.
 * Depending of the player type (Mr.X or Agent) the initialization differs.
 * The possible types of tickets are: 
 * busTicket - for busses
 * tramTicket - for tram
 * railwayTicket - for railway (S-Bahn)
 * blackTicket - just for Mr. X to hide the used ticket type
 * doubleTicket - just for Mr. X to go two stops instead of just one per round
 * waintingTicket - if you have to wait very long for the next railway, you can play this ticket to stay on this stop.
 * @author fanny
 *
 */
public class TicketManagement {
	private int busTicket;
	private int tramTicket;
	private int railwayTicket;
	private int blackTicket;
	private int doubleTicket;
	private int waitingTicket;
	private boolean mrx;
	
	/**
	 * Returns the current number of available busTickets.
	 * @return int - busTicket
	 */
	public int getBusTicketNumber() {
		return busTicket;
	}

	/**
	 * Returns the current number of available tramTickets.
	 * @return int - tramTicket
	 */
	public int getTramTicketNumber() {
		return tramTicket;
	}

	/**
	 * Returns the current number of available railwayTickets.
	 * @return int - railwayTicket
	 */
	public int getRailwayTicketNumber() {
		return railwayTicket;
	}

	/**
	 * Returns the current number of available blackTickets.
	 * @return int - blackTicket
	 */
	public int getBlackTicketNumber() {
		return blackTicket;
	}

	/**
	 * Returns the current number of available doubleTickets.
	 * @return int - doubleTicket
	 */
	public int getDoubleTicketNumber() {
		return doubleTicket;
	}

	/**
	 * Returns the current number of available waitingTickets.
	 * @return int - waitingTicket
	 */
	public int getWaitingTicketNumber() {
		return waitingTicket;
	}

	/**
	 * Constructor
	 */	
	public TicketManagement(int busTicket, int tramTicket, int railwayTicket, int blackTicket, int doubleTicket, int waitingTicket){
		this.busTicket = busTicket;
		this.tramTicket = tramTicket;
		this.railwayTicket = railwayTicket;
		this.blackTicket = blackTicket;
		this.doubleTicket = doubleTicket;
		this.waitingTicket = waitingTicket;
	}
	
	public TicketManagement(){}
	
	
	public void setTickets(int busTicket, int tramTicket, int railwayTicket, int blackTicket, int doubleTicket, int waitingTicket){
		this.busTicket = busTicket;
		this.tramTicket = tramTicket;
		this.railwayTicket = railwayTicket;
		this.blackTicket = blackTicket;
		this.doubleTicket = doubleTicket;
		this.waitingTicket = waitingTicket;
	}
	
	/**
	 * Initializes tickets of Mr. X.
	 */
	public void initMrXTickets(){
		busTicket = 4;
		tramTicket = 3;
		railwayTicket = 3;
		blackTicket = 2;
		doubleTicket = 0;
		waitingTicket = 1;
	}
	
	/**
	 * Initializes tickets of the agents.
	 */
	public void initAgentTickets(){
		busTicket = 5;
		tramTicket = 5;
		railwayTicket = 2;
		blackTicket = 0;
		doubleTicket = 0;
		waitingTicket = 1;
	}
	
	/**
	 * This method returns an true if there is a busTicket available and false if it is not. 
	 * @return boolean - true if ticket available, false if not
	 */
	public boolean useBusTicket(){
		if (busTicket > 0){
			busTicket--;
			return true;
		}
		else return false;
	}
	
	/**
	 * This method returns an true if there is a tramTicket available and false if it is not. 
	 * @return boolean - true if ticket available, false if not
	 */
	public boolean useTramTicket(){
		if (tramTicket > 0){
			tramTicket--;
			return true;
		}
		else return false;
	}
	
	/**
	 * This method returns an true if there is a blackTicket available and false if it is not. 
	 * @return boolean - true if ticket available, false if not
	 */
	public boolean useBlackTicket(){
		if (blackTicket > 0){
			blackTicket--;;
			return true;
		}
		else return false;
	}
	
	/**
	 * This method returns an true if there is a railwayTicket available and false if it is not. 
	 * @return boolean - true if ticket available, false if not
	 */
	public boolean useRailwayTicket(){
		if (railwayTicket > 0){
			railwayTicket--;;
			return true;
		}
		else return false;
	}
	
	/**
	 * This method returns an true if there is a doubleTicket available and false if it is not. 
	 * @return boolean - true if ticket available, false if not
	 */
	public boolean useDoubleTicket(){
		if (doubleTicket > 0){
			doubleTicket--;;
			return true;
		}
		else return false;
	}
	
	/**
	 * This method returns an true if there is a waitingTicket available and false if it is not. 
	 * @return boolean - true if ticket available, false if not
	 */
	public boolean useWaitingTicket(){
		if (waitingTicket > 0){
			waitingTicket--;;
			return true;
		}
		else return false;
	}
	
	/**
	 * This method returns an true if the player is Mr. X and he got a new busTicket. If the player is an
	 * agent, it is not possible to add new tickets during the game, so false is returned to show, that 
	 * this operation failed. 
	 * @return boolean - if Mr.X true, else false
	 */
	public boolean addBusTicket(){
		if (mrx){
			busTicket++;
			return true;
		}
		else return false;
	}
	
	/**
	 * This method returns an true if the player is Mr. X and he got a new tramTicket. If the player is an
	 * agent, it is not possible to add new tickets during the game, so false is returned to show, that 
	 * this operation failed. 
	 * @return boolean - if Mr.X true, else false
	 */
	public boolean addTramTicket(){
		if (mrx){
			tramTicket++;
			return true;
		}
		else return false;
	}
	
	/**
	 * This method returns an true if the player is Mr. X and he got a new railwayTicket. If the player is an
	 * agent, it is not possible to add new tickets during the game, so false is returned to show, that 
	 * this operation failed. 
	 * @return boolean - if Mr.X true, else false
	 */
	public boolean addRailwayTicket(){
		if (mrx){
			railwayTicket++;
			return true;
		}
		else return false;
	}	
}
