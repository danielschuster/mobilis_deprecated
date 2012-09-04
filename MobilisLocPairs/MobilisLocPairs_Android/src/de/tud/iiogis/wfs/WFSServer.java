/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: wfs
 */
package de.tud.iiogis.wfs;


import de.javagis.jgis.geometry.Extent;
import de.javagis.jgis.geometry.Point;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Class: WFSServer.java
 * Function: object to represent a wfs server
 *
 * @author Jan Scholze
 * @version 1.34
 */
public class WFSServer {

    private String wfsTitle;
    private String wfsName;
    private String wfsUrl;
    private ArrayList<WFSLayer> wfsLayers;
    private ArrayList<WFSServer> nearbyWfs;
    private Extent wfsExtent;
    private Point wfsCoordinate;
    private boolean isConnected;
    private boolean geometryIntegrated;

    /**
     * constructor: create new wfs server
     *
     * @param String name - name of the wfs server
     * @param String url - url of the wfs server
     * @param String title - title of the wfs server
     */
    public WFSServer(String name, String url, String title){
        wfsName = name;
        wfsTitle = title;
        wfsUrl = url;
        isConnected = false;
        geometryIntegrated = false;
        nearbyWfs = new ArrayList<WFSServer>();
    }

    /**
     * get name of the wfs server
     *
     * @return String - name of the wfs server
     */
    public String getWfsName(){
        return wfsName;
    }

//    /**
//     * get title of the wfs server
//     *
//     * @return String - title of the wfs server
//     */
//    public String getWfsTitle(){
//        return wfsTitle;
//    }

    /**
     * get basic url of the wfs server
     *
     * @return String - basic url of the wfs server
     */
    public String getWfsUrl(){
        return wfsUrl;
    }

    /**
     * get all nearby wfs server
     *
     * @return ArrayList<WFSServer> - arraylist with all nearby servers of the wfs server
     */
    public ArrayList<WFSServer> getNearbyWfs(){
        return nearbyWfs;
    }

    /**
     * get all layers of the wfs server
     *
     * @return ArrayList<WFSLayer> - arraylist with all layers of the wfs server
     */
    public ArrayList<WFSLayer> getWfsLayers(){
        return wfsLayers;
    }

    /**
     * get only polygon layers from all layers of the wfs server
     *
     * @return ArrayList<WFSLayer> - arraylist with all polygon layers of the wfs server
     */
    public ArrayList<WFSLayer> getWfsPolygonLayers(){
        ArrayList<WFSLayer> wfsPolygonLayers = new ArrayList<WFSLayer>();
        Iterator layerIt = wfsLayers.iterator();
        while (layerIt.hasNext()){
            WFSLayer layer = (WFSLayer) layerIt.next();
            // only add polygon layers to arraylist
            if (layer.getLayerType().equalsIgnoreCase("Polygon")){
                wfsPolygonLayers.add(layer);
            }
        }
        return wfsPolygonLayers;
    }

    /**
     * get only point layers from all layers of the wfs server
     *
     * @return ArrayList<WFSLayer> - arraylist with all point layers of the wfs server
     */
    public ArrayList<WFSLayer> getWfsPointLayers(){
        ArrayList<WFSLayer> wfsPointLayers = new ArrayList<WFSLayer>();
        Iterator layerIt = wfsLayers.iterator();
        while (layerIt.hasNext()){
            WFSLayer layer = (WFSLayer) layerIt.next();
            // only add point layers to arraylist
//            if ((layer.getLayerType().equalsIgnoreCase("Point")) && (!layer.getLayerName().endsWith("_" + Settings.getWfsLayerSuffix()))){
            if ((layer.getLayerType().equalsIgnoreCase("Point")) && (!layer.getLayerName().endsWith("_" + "WFS"))){
                wfsPointLayers.add(layer);
            }
        }
        return wfsPointLayers;
    }

    /**
     * get extent of the wfs server (server extent = merging all layer extents)
     *
     * @return Extent - extent/bounding box of the wfs server
     */
    public Extent getWfsExtent(){
        return wfsExtent;
    }

    /**
     * get (center) coordinate of the wfs server
     *
     * @return Point - point with the coordinate values (latitude, longitude) of the wfs server
     */
    public Point getWfsCoordinate(){
        return wfsCoordinate;
    }

    /**
     * get connection status of the wfs server
     *
     * @return boolean - true: wfs server status is "connected", false: wfs server status is "not connected"
     */
    public boolean isConnected(){
        return isConnected;
    }

    /**
     * get geometry status of the wfs server (geometry intergrated into location model or not)
     *
     * @return boolean - true: wfs server status is "geometry integrated", false: wfs server status is "geometry not integrated"
     */
    public boolean geometryIntegrated(){
        return geometryIntegrated;
    }

    /**
     * add nearby wfs server
     *
     * @param WFSServer server - nearby wfs server to add
     * @return boolean - true: nearby wfs server added, false: nearby wfs server not added
     */
    public boolean addNearbyWfs(WFSServer server){
        return nearbyWfs.add(server);
    }


    /**
     * set title of the wfs server
     *
     * @param String title - title to set for the wfs server
     */
    public void setWfsTitle(String title){
        wfsTitle = title;
    }

    /**
     * set coordinate of the wfs server
     *
     * @param Point coord - (center) coordinate to set for the wfs server
     */
    public void setWfsCoordinate(Point coord){
        wfsCoordinate = coord;
    }

    /**
     * set layers of the wfs server
     *
     * @param ArrayList<WFSLayer> layers - arraylist of layers to set for the wfs server
     */
    public void setWfsLayers(ArrayList<WFSLayer> layers){
        // set layers
        wfsLayers = layers;

        // get server extent by merging layer extents
        wfsExtent = layers.get(0).getLayerExtent();
        for (int i = 1; i < layers.size(); i++){
            wfsExtent.mergeExtent(layers.get(i).getLayerExtent());
        }
    }

    /**
     * set connection status of the wfs server
     *
     * @param boolean connected - true: wfs server status is "connected", false: wfs server status is "not connected"
     */
    public void setConnected(boolean connected){
        isConnected = connected;
    }

    /**
     * set geometry status of the wfs server (geometry intergrated into location model or not)
     *
     * @param boolean integrated - true: wfs server status is "geometry integrated", false: wfs server status is "geometry not integrated"
     */
    public void setIntegrated(boolean integrated){
        geometryIntegrated = integrated;
    }

    /**
     * get string representation of the wfs server
     *
     * @return String - string representation of the wfs server
     */
    @Override
    public String toString(){
        return wfsName;
    }
    
}
