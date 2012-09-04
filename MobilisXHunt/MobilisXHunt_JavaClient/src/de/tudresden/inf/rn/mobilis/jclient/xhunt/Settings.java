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

public class Settings {
	
	private String login;
	private String password;
	private String server;
	private String port;
	private String serverJid;
	private String name;
	
	//Own status
	private String jid;
	private boolean moderator;
	private boolean misterX;
	private boolean ready;
	
	/**
	 * Contructor
	 * Initializes the settings with predefined values
	 */
	public Settings(){
		login = "client";
		password = "client";
		server = "127.0.0.1";
		port = "5222";
		serverJid = "server@xhunt/Smack";
		name = "JClient";
		
		jid = "";
		moderator = false;
		misterX = false;
		ready = false;
	}

	public void setLogin(String username) {
		this.login = username;
	}

	public String getLogin() {
		return login;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getPort() {
		return port;
	}

	public void setServerJid(String serverJID) {
		this.serverJid = serverJID;
	}

	public String getServerJid() {
		return serverJid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getJid() {
		return jid;
	}

	public void setModerator(boolean moderator) {
		this.moderator = moderator;
	}

	public boolean isModerator() {
		return moderator;
	}

	public void setMisterX(boolean misterX) {
		this.misterX = misterX;
	}

	public boolean isMisterX() {
		return misterX;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isReady() {
		return ready;
	}

}
