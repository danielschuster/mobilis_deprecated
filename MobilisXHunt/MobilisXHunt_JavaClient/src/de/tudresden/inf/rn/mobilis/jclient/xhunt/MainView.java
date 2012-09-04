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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.JXMapKit.DefaultProviders;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.jdesktop.swingx.painter.CompoundPainter;

import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.LocationInfo;
import de.tudresden.inf.rn.mobilis.jclient.xhunt.beans.Station;

public class MainView implements ActionListener{
	
	private Settings set;
	private Connection con;
	private SettingsView settingsView;
				
	//Mainframe
	private JFrame mainFrame;
	private GridBagConstraints constraints;
	private JLabel header;
	
	//OSM
	private JXMapKit map;
	private GeoPosition actPosition;
	
	private Set<Waypoint> target;
	private Set<Waypoint> location;
	
	private WaypointPainter painterTarget;
	private WaypointPainter painterLocation;
	
	
	//Settings
	private JButton settings;
	private JButton connect;
	
	//Status
	private JTextArea statusTA;
	private JScrollPane statusSP;
	private JScrollBar statusVerticalScroll;
	
	//IQs
	private JButton sendInitIQ;
	private JButton sendUpdateIQ;
	private JButton sendTargetIQ;
	private JButton sendLocationIQ;
	private JButton sendExitIQ;
	
	//Coords
	private JLabel markedLatHeader;
	private JLabel markedLonHeader;
	private JLabel targetsHeader;
	private JTextField markedLat;
	private JTextField markedLon;
	private JComboBox targets;
	
	private int round;
	private ArrayList<Station> stations;
		
	/**
	 * Initializes the user interface and adds all components to it
	 * @param set Settings
	 * @param con Connection
	 */
	public MainView(Settings set, Connection con){
		
		this.set = set;
		this.con = con;
		actPosition = new GeoPosition(0, 0);
		
		round = 0;
		
		target = new HashSet<Waypoint>();
		location = new HashSet<Waypoint>();
		painterTarget = new WaypointPainter();
		painterLocation = new WaypointPainter();
		
		//Setting new symbols for waypoints
		painterLocation.setRenderer(new WaypointRenderer(){
			@Override
			public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
				g.setColor(Color.BLUE); 
				g.fillRoundRect(-10, -10, 10, 10, 10, 10);  
				return true;  
			}	
		});
		
		painterTarget.setRenderer(new WaypointRenderer(){
			@Override
			public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
				g.setColor(Color.BLUE);  
				g.fillRect(-5, -5, 5, 5);  
				return true;  
			}	
		});
		
		//Mainframe
		mainFrame = new JFrame("XHunt-Javaclient");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setResizable(false);
		constraints = new GridBagConstraints();
		mainFrame.setLayout(new GridBagLayout());
		
		//Header
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = 1;
		constraints.insets = new Insets(0, 5, 0, 5);
		header = new JLabel("XHunt Javaclient");
		header.setFont(new Font("Helvetica", Font.BOLD, 16));
		mainFrame.add(header, constraints);
		
		//OSM-Map
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.gridheight = 2;
		constraints.insets = new Insets(5, 5, 5, 5);
		map = new JXMapKit();
		map.setName("Übersichtskarte");
	    map.setPreferredSize(new Dimension(300, 300));
		map.setDefaultProvider(DefaultProviders.OpenStreetMaps);
		map.setCenterPosition(new GeoPosition(51.02537648, 13.72288942));
		map.setZoom(1);
		map.setMiniMapVisible(false);
		map.getMainMap().addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent ev){
				actPosition = map.getMainMap().convertPointToGeoPosition(ev.getPoint());
				while(actPosition.getLongitude() > 180){
					actPosition = new GeoPosition(actPosition.getLatitude(), actPosition.getLongitude()-180);
				}
				while(actPosition.getLongitude() < -180){
					actPosition = new GeoPosition(actPosition.getLatitude(), actPosition.getLongitude()+180);
				}
				markedLat.setText("" + actPosition.getLatitude());
				markedLon.setText("" + actPosition.getLongitude());
			}
		});
		mainFrame.add(map, constraints);
		
		//Settings
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		settings = new JButton("Settings");
		settings.addActionListener(this);
		settings.setPreferredSize(new Dimension(130, 20));
		mainFrame.add(settings, constraints);
		
		constraints.gridx = 4;
		constraints.gridy = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		connect = new JButton("Connect");
		connect.addActionListener(this);
		connect.setPreferredSize(new Dimension(130, 20));
		mainFrame.add(connect, constraints);
		
		//Status
		constraints.gridx = 2;
		constraints.gridy = 2;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = 1;
		constraints.insets = new Insets(0, 5, 0, 5);
		statusTA = new JTextArea(17, 25);
		statusTA.setEditable(false);
		statusSP = new JScrollPane(statusTA);
		statusSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		statusVerticalScroll = statusSP.getVerticalScrollBar();
	    mainFrame.add(statusSP, constraints);
		
		//IQs
		//InitIQ
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.CENTER; 
		sendInitIQ = new JButton("InitGameIQ");
		sendInitIQ.addActionListener(this);
		sendInitIQ.setPreferredSize(new Dimension(140, 20));
		sendInitIQ.setEnabled(false);
		mainFrame.add(sendInitIQ, constraints);
		
		//UpdateIQ
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.CENTER; 
		sendUpdateIQ = new JButton("UpdatePlayerIQ");
		sendUpdateIQ.addActionListener(this);
		sendUpdateIQ.setPreferredSize(new Dimension(140, 20));
		sendUpdateIQ.setEnabled(false);
		mainFrame.add(sendUpdateIQ, constraints);
		
		//TargetIQ
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.CENTER; 
		sendTargetIQ = new JButton("TargetIQ");
		sendTargetIQ.addActionListener(this);
		sendTargetIQ.setPreferredSize(new Dimension(140, 20));
		sendTargetIQ.setEnabled(false);
		mainFrame.add(sendTargetIQ, constraints);
		
		//LocationIQ
		constraints.gridx = 1;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.CENTER;
		sendLocationIQ = new JButton("XHuntLocationIQ");
		sendLocationIQ.addActionListener(this);
		sendLocationIQ.setPreferredSize(new Dimension(140, 20));
		sendLocationIQ.setEnabled(false);
		mainFrame.add(sendLocationIQ, constraints);
		
		//PlayerExitIQ
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.CENTER; 
		sendExitIQ = new JButton("PlayerExitGameIQ");
		sendExitIQ.addActionListener(this);
		sendExitIQ.setPreferredSize(new Dimension(140, 20));
		sendExitIQ.setEnabled(false);
		mainFrame.add(sendExitIQ, constraints);
		
		
		//Coords
		//Header Latitude
		constraints.gridx = 2;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		markedLatHeader = new JLabel("Lat:");
		mainFrame.add(markedLatHeader, constraints);
		
		//Textfield Latitude
		constraints.gridx = 3;
		constraints.gridy = 3;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = 1;
		markedLat = new JTextField();
		markedLat.setPreferredSize(new Dimension(200, 20));
		markedLat.setEditable(false);
		mainFrame.add(markedLat, constraints);
		
		//Header Longitude
		constraints.gridx = 2;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		markedLonHeader = new JLabel("Lon:");
		mainFrame.add(markedLonHeader, constraints);
		
		//Textfield Longitude
		constraints.gridx = 3;
		constraints.gridy = 4;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = 1;
		markedLon = new JTextField();
		markedLon.setPreferredSize(new Dimension(200, 20));
		markedLon.setEditable(false);
		mainFrame.add(markedLon, constraints);
		
		//Header Targets
		constraints.gridx = 2;
		constraints.gridy = 5;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		targetsHeader = new JLabel("Targets:");
		mainFrame.add(targetsHeader, constraints);
		
		//ComboBox Targets
		constraints.gridx = 3;
		constraints.gridy = 5;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = 1;
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		targets = new JComboBox(model);
		targets.setPreferredSize(new Dimension(200, 20));
		targets.addActionListener(new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	if(stations != null){
					for(Station sta : stations){
						if(targets.getSelectedItem().equals(sta.getName())){
							actPosition = new GeoPosition(sta.getLatitudeInMicroDegrees() / 1000000f, sta.getLongitudeInMicroDegrees() / 1000000f);
							markedLat.setText("" + actPosition.getLatitude());
							markedLon.setText("" + actPosition.getLongitude());
							break;
						}
					}
				}
		    }
	    });

		mainFrame.add(targets, constraints);

		mainFrame.pack();
		mainFrame.setVisible(true);
	}
	
	
	/**
	 * Adds a status message to the status textarea
	 * @param text text, which should be added
	 */
	public void addStatusMessage(String text) {
		SimpleDateFormat formatter = new SimpleDateFormat ("[HH:mm:ss] ");
		Date currentTime = new Date();
		statusTA.append(formatter.format(currentTime) + text + "\n");
		
		statusVerticalScroll.revalidate();
		statusVerticalScroll.setValue(statusVerticalScroll.getMaximum());
		statusVerticalScroll.repaint();
		
	}
	
	
	/**
	 * Updates the targets from GameDataIQ and sets the map center position
	 */
	public void refreshTargets(){
		//Refreshing targets
		Vector vec = new Vector();
		RouteManagement rm = RouteManagement.getInstance();
		stations = rm.getStations();
		
		Collections.sort(stations, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				String id1 = ((Station) o1).getName();
				String id2 = ((Station) o2).getName();
				return id1.compareTo(id2);
			}	
		});
		
		for (Station sta : stations){
			vec.addElement(sta.getName());
		}
		DefaultComboBoxModel model = new DefaultComboBoxModel(vec);
		targets.setModel(model);
			
		//Setting map center
		map.setCenterPosition(rm.getMapCenter());
		map.setZoom(5);

	}

	
	/**
	 * Draws the location and target of the player on the map
	 */
	public void drawLocations(){
		
		painterTarget.setWaypoints(target);
		painterLocation.setWaypoints(location);
						
		CompoundPainter compPainter = new CompoundPainter(painterTarget, painterLocation);
		
		map.getMainMap().setOverlayPainter(compPainter);
		
	}

	
	/**
	 * Updates the location of the player
	 * @param lastLocation location from XHuntPlayerIQ
	 */
	public void setLastLocation(GeoPosition lastLocation){
		location.clear();
		location.add(new Waypoint(lastLocation));
		drawLocations();
	}
	
	/**
	 * Updates the target of the player
	 * @param lastTarget target from TargetIQ
	 */
	public void setLastTarget(String lastTarget){
		target.clear();
		RouteManagement rm = RouteManagement.getInstance();
		LocationInfo li= new LocationInfo();
		li.setLatitude(rm.getStation(lastTarget).getLatitudeInMicroDegrees() / 1000000.f);
		li.setLongitude(rm.getStation(lastTarget).getLongitudeInMicroDegrees() / 1000000.f);
						
		target.add(new Waypoint(new GeoPosition(li.getLatitude(), li.getLongitude())));
		drawLocations();
	}
	
	
	public void setRound(int round) {
		this.round = round;
	}


	public int getRound() {
		return round;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Settings")){
			settingsView = new SettingsView(set);
			settingsView.showSettingsFrame();
		}else{
			if(e.getActionCommand().equals("Connect")){
				if (con.startConnection()){
					connect.setEnabled(false);
					settings.setEnabled(false);
					sendInitIQ.setEnabled(true);
					sendUpdateIQ.setEnabled(true);
					sendTargetIQ.setEnabled(true);
					sendLocationIQ.setEnabled(true);
					sendExitIQ.setEnabled(true);
					header.setText("XHunt Javaclient - " + set.getJid());
					addStatusMessage("Connection established");
				}
			}else{
				if(e.getActionCommand().equals("InitGameIQ")){
					con.sendInitIQ();
					addStatusMessage("<-- InitGameIQ send");
				}else{
					if(e.getActionCommand().equals("UpdatePlayerIQ")){
						con.sendXHuntLocationIQ(new GeoPosition(51.02537648, 13.72288942));		
						con.sendUpdatePlayerIQ();
						addStatusMessage("<-- UpdatePlayerIQ send");
					}else{
						if(e.getActionCommand().equals("TargetIQ")){
							if(stations != null){
								for(Station sta : stations){
									if(targets.getSelectedItem().equals(sta.getName())){
										con.sendTargetIQ(sta.getId());
										addStatusMessage("<-- TargetIQ send");
										break;
									}
								}
							}
							
							
						}else{
							if(e.getActionCommand().equals("XHuntLocationIQ")){
								con.sendXHuntLocationIQ(actPosition);
								addStatusMessage("<-- XHuntLocationIQ send");
								setLastLocation(actPosition);
							}else{
								if(e.getActionCommand().equals("PlayerExitGameIQ")){
									con.sendPlayerExitGameIQ();
									addStatusMessage("<-- PlayerExitGameIQ send");
								}
							}
						}
					}
				}
			}
		}
		
	}
	
}
