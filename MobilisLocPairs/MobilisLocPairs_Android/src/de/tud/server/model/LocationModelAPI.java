/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: model
 */
package de.tud.server.model;


//import de.tud.iiogis.map.WayPoint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import de.tud.iiogis.wfs.WFSLayer;
import de.tud.iiogis.wfs.WFSServer;


/**
 * Class: LocationModelAPI.java
 * Function: api to control the location model
 *
 * @author Jan Scholze
 * @version 1.53
 */
public class LocationModelAPI {

    private static ArrayList<WFSServer> availableWFSServers = new ArrayList<WFSServer>();
    private static ArrayList<WFSServer> connectedWFSServers = new ArrayList<WFSServer>();
    private static WFSServer selectedBuildingServer;
    private static int nearbyWfsIndex;
    private static ArrayList<WFSLayer> activeFloorLayer = new ArrayList<WFSLayer>();
//    private static ArrayList<WFSLayer> activeAPLayer = new ArrayList<WFSLayer>();
    private static ArrayList<WFSLayer> activeBuildingLayer = new ArrayList<WFSLayer>();
//    private static ArrayList<WayPoint> waypoints = new ArrayList<WayPoint>();
//    private static ArrayList<Route> routes = new ArrayList<Route>();
//    private static WayPoint waypoint = null;
//    private static Route route = null;
    private static Map accessPoints = new LinkedHashMap<String, WLANAccessPoint>();
    private static Coordinate gpsCoordinate;
    private static long lastGpsCoordinateUpdate = -1;
    private static Coordinate wifiCoordinate;
    private static long lastWifiCoordinateUpdate = -1;
    private static BuildingPart highlightRoom;
       
    /**
	 * @param gpsCoordinate the gpsCoordinate to set
	 */
	public static void setGpsCoordinate(Coordinate gpsCoordinate) {
		LocationModelAPI.gpsCoordinate = gpsCoordinate;
	}

	/**
	 * @return the gpsCoordinate
	 */
	public static Coordinate getGpsCoordinate() {
		return gpsCoordinate;
	}

	/**
	 * @param lastGpsCoordinateUpdate the lastGpsCoordinateUpdate to set
	 */
	public static void setLastGpsCoordinateUpdate(long lastGpsCoordinateUpdate) {
		LocationModelAPI.lastGpsCoordinateUpdate = lastGpsCoordinateUpdate;
	}

	/**
	 * @return the lastGpsCoordinateUpdate
	 */
	public static long getLastGpsCoordinateUpdate() {
		return lastGpsCoordinateUpdate;
	}
	
	/**
	 * sets the current gps position
	 * @param timestamp the timestamp to set
	 * @param coord the coordinates to set
	 */
	public static void setCurrentGpsPosition(long timestamp, Coordinate coord) {
		setLastGpsCoordinateUpdate(timestamp);
		setGpsCoordinate(coord);
	}

	/**
	 * @return the wifiCoordinate
	 */
	public static Coordinate getWifiCoordinate() {
		return wifiCoordinate;
	}

	/**
	 * @param wifiCoordinate the wifiCoordinate to set
	 */
	public static void setWifiCoordinate(Coordinate wifiCoordinate) {
		LocationModelAPI.wifiCoordinate = wifiCoordinate;
	}

	/**
	 * @return the lastWifiCoordinateUpdate
	 */
	public static long getLastWifiCoordinateUpdate() {
		return lastWifiCoordinateUpdate;
	}

	/**
	 * @param lastWifiCoordinateUpdate the lastWifiCoordinateUpdate to set
	 */
	public static void setLastWifiCoordinateUpdate(long lastWifiCoordinateUpdate) {
		LocationModelAPI.lastWifiCoordinateUpdate = lastWifiCoordinateUpdate;
	}
	
	/**
	 * sets the current wifiPosition
	 * @param timestamp the timestamp to set
	 * @param wifiCoordinate the coordinate to set
	 */
	public static void setCurrentWifiPosition(long timestamp, Coordinate wifiCoordinate) {
		setLastWifiCoordinateUpdate(timestamp);
		setWifiCoordinate(wifiCoordinate);
	}

	/**
     * add given access point
     *
     * @param WLANAccessPoint ap - given access point to add
     */
    public static void addAccessPoint(WLANAccessPoint ap){
        // key = mac-address, value = access point object
        accessPoints.put(ap.getItemId(), ap);
    }

    /**
     * remove given access point
     *
     * @param WLANAccessPoint ap - given access point to remove
     */
    public static WLANAccessPoint removeAccessPoint(String key){
        return (WLANAccessPoint) accessPoints.remove(key);
    }

    /**
     * clear all access points
     */
    public static void clearAccessPoints(){
        accessPoints.clear();
    }

    /**
     * get access point by key
     *
     * @param String key - key to find an access point with
     * @return WLANAccessPoint ap - access point with the given key
     */
    public static WLANAccessPoint getAccessPoint(String key){
        return (WLANAccessPoint) accessPoints.get(key);
    }

    /**
     * get number of access points
     *
     * @return int - number of access points
     */
    public static int getNumberOfAccessPoints(){
        return accessPoints.size();
    }

    /**
     * test if access point map already contains access point key
     *
     * @param String key - key to search for
     * @return boolean - true: map already contains key, false: map not contains key
     */
    public static boolean containsAccessPointKey(String key){
        return accessPoints.containsKey(key);
    }

    /**
     * test if access point map already contains access point value
     *
     * @param WLANAccessPoint ap - access point to search for
     * @return boolean - true: map already contains access point, false: map not contains access point
     */
    public static boolean containsAccessPointValue(WLANAccessPoint ap){
        return accessPoints.containsValue(ap);
    }


    /**
     * update building overlay layer list
     */
    public static void updateBuildingOverlayLayerList(){
        // clear current list
        activeBuildingLayer.clear();
        Iterator wfsIt = connectedWFSServers.iterator();
        while(wfsIt.hasNext()){
            WFSServer server = (WFSServer) wfsIt.next();
            // add ground layer (= layer 0) if wfs is not selected
            if (server != selectedBuildingServer){
                activeBuildingLayer.add(server.getWfsLayers().get(0));
            }
        }
    }

    /**
     * get building overlay layer list
     *
     * @return ArrayList<WFSLayer> - arraylist with all active layers of the building
     */
    public static ArrayList<WFSLayer> getBuildingOverlayLayerList(){
        return activeBuildingLayer;
    }

    public static void setActiveBuildingLayer(WFSLayer layer){
    	activeBuildingLayer.clear();
    	activeBuildingLayer.add(layer);
    }

//    /**
//     * add waypoint
//     *
//     * @param WayPoint point - waypoint to add
//     * @return boolean - true: waypoint added, false: waypoint not added
//     */
//    public static boolean addWaypoint(WayPoint point){
//        if (!containsWaypoint(point)){
//            return waypoints.add(point);
//            //System.out.println("Waypoint: " + point.toString());
//        }else{
//            JOptionPane.showMessageDialog(null, "Waypoint is already existing!");
//            return false;
//        }
//    }

//    /**
//     * test if arraylist already contains waypoint
//     *
//     * @param WayPoint point - waypoint to test
//     * @return boolean - true: waypoint already exists, false: waypoint not exists
//     */
//    public static boolean containsWaypoint(Waypoint point){
//        for (int i = 0; i < waypoints.size(); i++){
//            if ((waypoints.get(i).getPosition().getLatitude() == point.getPosition().getLatitude()) &&
//                (waypoints.get(i).getPosition().getLongitude() == point.getPosition().getLongitude())){
//                return true;
//            }
//        }
//        return false;
//    }

//    /**
//     * remove waypoint
//     *
//     * @param WayPoint point - waypoint to remove
//     * @return boolean - true: waypoint removed, false: waypoint not removed
//     */
//    public static boolean removeWaypoint(WayPoint point){
//        return waypoints.remove(point);
//    }

//    /**
//     * get all waypoints
//     *
//     * @return ArrayList<WayPoint> - arraylist with all waypoints
//     */
//    public static ArrayList<WayPoint> getWaypoints(){
//        return waypoints;
//    }
    
//    /**
//     * get specific waypoint
//     *
//     * @param int index - index of the waypoint to get
//     * @return WayPoint - waypoint with the given index
//     */
//    public static WayPoint getWaypoint(int index){
//        return waypoints.get(index);
//    }

//    /**
//     * get active waypoint
//     *
//     * @return WayPoint - currently active waypoint
//     */
//    public static WayPoint getActiveWaypoint(){
//        return waypoint;
//    }

//    /**
//     * get index of waypoint
//     *
//     * @param WayPoint wayP - waypoint to get the index from
//     * @return int - index of the given waypoint
//     */
//    public static int getIndexOfWaypoint(WayPoint wayP){
//        return waypoints.indexOf(wayP);
//    }

//    /**
//     * get number of waypoint
//     *
//     * @return int - number of waypoints
//     */
//    public static int getNumberOfWaypoints(){
//        return waypoints.size();
//    }

//    /**
//     * update selected waypoint
//     *
//     * @param WayPoint point - waypoint to set as new selected waypoint
//     */
//    public static void updateWayPoint(WayPoint point){
//        waypoint = point;
//    }


//    /**
//     * get route
//     *
//     * @return Route - route
//     */
//    public static Route getRoute(){
//        return route;
//    }

//    /**
//     * set route
//     *
//     * @param Route newRoute - route to set
//     */
//    public static void setRoute(Route newRoute){
//        route = newRoute;
//    }

//    /**
//     * clear route
//     */
//    public static void clearRoute(){
//        route = null;
//    }


//    /**
//     * are wfs servers available
//     *
//     * @return boolean - true: wfs servers available, false: no wfs servers available
//     */
//    public static boolean areWFSServersAvailable(){
//        return (availableWFSServers.size() != 0);
//    }
    
//    /**
//     * are wfs servers connected
//     *
//     * @return boolean - true: wfs servers connected, false: no wfs servers connected
//     */
//    public static boolean areWFSServersConnected(){
//        return (connectedWFSServers.size() != 0);
//    }

    /**
     * add new wfs server to available wfs servers
     *
     * @param WFSServer server - wfs server to add
     * @return boolean - true: wfs servers added to available wfs servers, false: wfs servers not added to available wfs servers
     */
    public static boolean addWFSServer(WFSServer server){
        return availableWFSServers.add(server);
    }

    /**
     * remove wfs server from available wfs servers
     *
     * @param int index - index of the wfs server to remove
     * @return WFSServer - removed wfs server
     */
    public static WFSServer removeWFSServer(int index){
        return availableWFSServers.remove(index);
    }

    /**
     * connect an available wfs server
     *
     * @param WFSServer server - wfs server to connect
     * @return boolean - true: wfs servers connected, false: wfs servers not connected
     */
    public static boolean connectWFSServer(WFSServer server){
    	server.setConnected(true);
        return connectedWFSServers.add(server);
    }

    /**
     * disconnect a connected wfs server
     *
     * @param WFSServer server - wfs server to disconnect
     * @return boolean - true: wfs servers disconnected, false: wfs servers not disconnected
     */
    public static WFSServer disconnectWFSServer(int index){
        return connectedWFSServers.remove(index);
    }
    
    public static boolean disconnectWFSServer(WFSServer server){
    	server.setConnected(false);
    	server.setIntegrated(false);
        return connectedWFSServers.remove(server);
    }

    /**
     * get an arraylist with all available wfs servers
     *
     * @return ArrayList<WFSServer> - arraylist with all available wfs servers
     */
    public static ArrayList<WFSServer> getAvailableWFSServers(){
        return availableWFSServers;
    }

    /**
     * get an arraylist with all connected wfs servers
     *
     * @return ArrayList<WFSServer> - arraylist with all connected wfs servers
     */
    public static ArrayList<WFSServer> getConnectedWFSServers(){
        return connectedWFSServers;
    }


    /**
     * get an arraylist with all active floor layers
     *
     * @return ArrayList<WFSLayer> - arraylist with all active wfs floor layers (only floors, not access points)
     */
    public static ArrayList<WFSLayer> getActiveFloorLayers(){
        return activeFloorLayer;
    }

    /**
     * get number of active floor layers
     *
     * @return int - number of all active wfs floor layers (only floors, not access points)
     */
    public static int getNumberOfActiveFloorLayers(){
        return activeFloorLayer.size();
    }

    /**
     * get number of rooms in all active floor layers
     *
     * @return int - number of rooms in all active wfs floor layers
     */
    public static int getNumberOfRoomsInActiveFloorLayers(){
        int rooms = 0;
        Iterator layerIt = activeFloorLayer.iterator();
        while (layerIt.hasNext()){
            rooms = rooms + ((WFSLayer) layerIt.next()).getNumberOfItems();
        }        
        return rooms;
    }

    /**
     * clear list with active floors
     */
    public static void clearActiveFloorLayers(){
        activeFloorLayer.clear();
    }

    /**
     * set active floor layers
     *
     * @param ArrayList<WFSLayer> floors - arraylist with all active wfs floor layers to set (only floors, not access points)
     */
    public static void setActiveFloorLayers(ArrayList<WFSLayer> floors){
        activeFloorLayer = floors;
    }

//    /**
//     * get an arraylist with all active access point layers
//     *
//     * @return ArrayList<WFSLayer> - arraylist with all active access point layers (only access points, not floors)
//     */
//    public static ArrayList<WFSLayer> getActiveAPLayers(){
//        return activeAPLayer;
//    }

//    /**
//     * get number of active access point layers
//     *
//     * @return int - number of all active access point layers (only access points, not floors)
//     */
//    public static int getNumberOfActiveAPLayers(){
//        return activeAPLayer.size();
//    }

//    /**
//     * get number of beacons in all active access point layers
//     *
//     * @return int - number of beacons in all active access point layers
//     */
//    public static int getNumberOfBeaconsInActiveAPLayers(){
//        int aps = 0;
//        Iterator layerIt = activeAPLayer.iterator();
//        while (layerIt.hasNext()){
//            aps = aps + ((WFSLayer) layerIt.next()).getNumberOfItems();
//        }
//        return aps;
//    }

//    /**
//     * clear list with active access points
//     */
//    public static void clearActiveAPLayers(){
//        activeAPLayer.clear();
//    }

//    /**
//     * set active access points
//     *
//     * @param ArrayList<WFSLayer> aps - arraylist with all active access point layers to set (only access points, not floors)
//     */
//    public static void setActiveAPLayers(ArrayList<WFSLayer> aps){
//        activeAPLayer = aps;
//    }

    /**
     * get selected building server
     *
     * @return WFSServer - selected building server
     */
    public static WFSServer getSelectedBuilding(){
        return selectedBuildingServer;
    }

    /**
     * set selected building server
     *
     * @param WFSServer selBuildServ - selected building server to set
     */
    public static void setSelectedBuilding(WFSServer selBuildServ){
        selectedBuildingServer = selBuildServ;
    }

//    /**
//     * get index of selected nearby wfs server
//     *
//     * @return int - index of selected nearby wfs server
//     */
//    public static int getNearbyWfsIndex(){
//        return nearbyWfsIndex;
//    }

//    /**
//     * set index of selected nearby wfs server
//     *
//     * @param int nearWfsIndex - index of selected nearby wfs server to set
//     */
//    public static void setNearbyWfsIndex(int nearWfsIndex){
//        nearbyWfsIndex = nearWfsIndex;
//    }

    public static BuildingPart getHighlightRoom(){
    	return highlightRoom;
    }
    
    public static void setHighlightRoom(BuildingPart part){
    	highlightRoom = part;
    }
}
