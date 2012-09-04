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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SettingsView implements ActionListener{
	
	//SettingsFrame
	private JFrame settingsFrame;
	private GridBagConstraints constraints;
	private JLabel header;
	
	//Labels
	private JLabel loginLab;
	private JLabel pwLab;
	private JLabel serverLab;
	private JLabel portLab;
	private JLabel serverJIDLab;
	private JLabel nameLab;
	
	//TextFields
	private JTextField loginTF;
	private JTextField pwTF;
	private JTextField serverTF;
	private JTextField portTF;
	private JTextField serverJIDTF;
	private JTextField nameTF;
	
	//Buttons
	private JButton ok;
	private JButton back;
	
	private Settings set;
	
	/**
	 * Initializes the user interface to change the settings 
	 * and adds all components to it
	 * @param set Given Settings
	 */
	public SettingsView(Settings set){
		
		this.set = set;
		
		//SettingsFrame
		settingsFrame = new JFrame("Settings");
		settingsFrame.setResizable(false);
		constraints = new GridBagConstraints();
		settingsFrame.setLayout(new GridBagLayout());
		
		//Header
	    constraints.gridx = 0;
	    constraints.gridy = 0;
	    constraints.gridwidth = GridBagConstraints.REMAINDER;
	    constraints.gridheight = 2;
	    constraints.insets = new Insets(0, 5, 0, 5);
	    header = new JLabel("Settings");
	    header.setFont(new Font("Helvetica", Font.BOLD, 14 ));
	    settingsFrame.add(header, constraints);
	    
	    //Labels
	    constraints.gridx = 0;
	    constraints.gridy = 2;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    loginLab = new JLabel("Login:");
	    settingsFrame.add(loginLab, constraints);
	    
	    constraints.gridx = 0;
	    constraints.gridy = 3;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    pwLab = new JLabel("Password:");
	    settingsFrame.add(pwLab, constraints);
	    
	    constraints.gridx = 0;
	    constraints.gridy = 4;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    serverLab = new JLabel("Server-IP:");
	    settingsFrame.add(serverLab, constraints);
	    
	    constraints.gridx = 0;
	    constraints.gridy = 5;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    portLab = new JLabel("Server-Port:");
	    settingsFrame.add(portLab, constraints);
	    
	    constraints.gridx = 0;
	    constraints.gridy = 6;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    serverJIDLab = new JLabel("XHunt-Server:");
	    settingsFrame.add(serverJIDLab, constraints);
	    
	    constraints.gridx = 0;
	    constraints.gridy = 7;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    nameLab = new JLabel("Nickname:");
	    settingsFrame.add(nameLab, constraints);
	    
	    
	    //TextFields
	    constraints.gridx = 1;
	    constraints.gridy = 2;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    loginTF = new JTextField(set.getLogin(), 15);
	    settingsFrame.add(loginTF, constraints);
	    
	    constraints.gridx = 1;
	    constraints.gridy = 3;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    pwTF = new JTextField(set.getPassword(), 15);
	    settingsFrame.add(pwTF, constraints);
	    
	    constraints.gridx = 1;
	    constraints.gridy = 4;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    serverTF = new JTextField(set.getServer(), 15);
	    settingsFrame.add(serverTF, constraints);
	    
	    constraints.gridx = 1;
	    constraints.gridy = 5;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    portTF = new JTextField(set.getPort(), 15);
	    settingsFrame.add(portTF, constraints);
	    
	    constraints.gridx = 1;
	    constraints.gridy = 6;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    serverJIDTF = new JTextField(set.getServerJid(), 15);
	    settingsFrame.add(serverJIDTF, constraints);
	    
	    constraints.gridx = 1;
	    constraints.gridy = 7;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    nameTF = new JTextField(set.getName(), 15);
	    settingsFrame.add(nameTF, constraints);
	    
	    //Buttons
	    constraints.gridx = 0;
		constraints.gridy = 8;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		back = new JButton("Back");
		back.addActionListener(this);
		back.setPreferredSize(new Dimension(130, 20));
		settingsFrame.add(back, constraints);
		
		constraints.gridx = 1;
		constraints.gridy = 8;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		ok = new JButton("Ok");
		ok.addActionListener(this);
		ok.setPreferredSize(new Dimension(130, 20));
		settingsFrame.add(ok, constraints);
		
	}
	
	/**
	 * Activates the frame
	 */
	public void showSettingsFrame(){
		settingsFrame.pack();
		settingsFrame.setVisible(true);
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Back")){
			settingsFrame.setVisible(false);
		}else{
			if(e.getActionCommand().equals("Ok")){
				set.setLogin(loginTF.getText());
				set.setPassword(pwTF.getText());
				set.setServer(serverTF.getText());
				set.setPort(portTF.getText());
				set.setServerJid(serverJIDTF.getText());
				set.setName(nameTF.getText());
				settingsFrame.setVisible(false);
				
			}
		}
		
	}

}
