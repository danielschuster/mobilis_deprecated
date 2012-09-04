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
package de.tudresden.inf.rn.mobilis.jclient.xhunt.packet;

import org.jivesoftware.smack.packet.IQ;

public class InitGameIQ extends IQ {

	public static final String elementName = "query";
    public static final String namespace = "mobilisxhunt:iq:initgame";
	
	private String name;
	
	public InitGameIQ() {
		super();
		this.setType(IQ.Type.SET);	
	}
	
	// Setter & Getter
	public void setName(String name) {
		this.name=name;
	}
	public String getName() {
		return name;
	}
	
	
	@Override
	public String getChildElementXML() {
		StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	buf.append("<name>").append(this.name).append("</name>\n");
    	    	    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
	}

}
