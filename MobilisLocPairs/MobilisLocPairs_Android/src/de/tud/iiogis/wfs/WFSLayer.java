/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: wfs
 */
package de.tud.iiogis.wfs;


import de.javagis.jgis.geometry.Extent;
import de.tud.server.model.LocatableItem;
import java.util.ArrayList;


/**
 * Class: WFSLayer.java
 * Function: object to represent a single layer of a wfs server
 *
 * @author Jan Scholze
 * @version 1.24
 */
public class WFSLayer {

    private String layerName;
    private String layerTitle;
    private String layerType;
    private String layerProjection;
    private Extent layerExtent;
    private ArrayList<WFSLayerAttribute> layerAttributes;
    private ArrayList<LocatableItem> items;
    private boolean active;

    /**
     * constructor: create new layer of a wfs server
     *
     * @param String name - layer name
     * @param String title - layer title
     * @param String type - layer type (point/line/polygon)
     * @param String projection - layer projection (epsg code of the layer's coordinate system)
     * @param Extent extent - extent/bounding box of the layer
     */
    public WFSLayer(String name, String title, String type, String projection, Extent extent){
        layerName = name;
        layerTitle = title;
        layerType = type;
        layerProjection = projection;
        layerExtent = extent;
        items = new ArrayList<LocatableItem>();
        active = false;
    }

    /**
     * get name of the layer
     *
     * @return String - name of the layer
     */
    public String getLayerName(){
        return layerName;
    }

    /**
     * get title of the layer
     *
     * @return String - title of the layer
     */
    public String getLayerTitle(){
        return layerTitle;
    }

    /**
     * get type of the layer
     *
     * @return String - type of the layer (point/line/polygon)
     */
    public String getLayerType(){
        return layerType;
    }

    /**
     * get projection of the layer
     *
     * @return String - projection of the layer (epsg code)
     */
    public String getLayerProjection(){
        return layerProjection;
    }

    /**
     * get extent of the layer
     *
     * @return Extent - extent/bounding box of the layer
     */
    public Extent getLayerExtent(){
        return layerExtent;
    }

    /**
     * get items of the layer
     *
     * @return ArrayList<LocatableItem> - arraylist with all items of the layer
     */
    public ArrayList<LocatableItem> getItems(){
        return items;
    }

    /**
     * get number of items of the layer
     *
     * @return int - number of items of the layer
     */
    public int getNumberOfItems(){
        return items.size();
    }

    /**
     * get attributes of the layer
     *
     * @return ArrayList<WFSLayerAttribute> - arraylist with all attributes of the layer
     */
    public ArrayList<WFSLayerAttribute> getLayerAttributes(){
        return layerAttributes;
    }

    /**
     * get selection status of the layer
     *
     * @return boolean - true: layer is active and shown at the map, false: layer is inactive and not shown
     */
    public boolean isActive(){
        return active;
    }

    /**
     * set extent for the layer
     *
     * @param Extentextent - extent/bounding box to set for the layer
     */
    public void setLayerExtent(Extent extent){
        layerExtent = extent;
    }

    /**
     * set type for the layer
     *
     * @param String type - type to set for the layer (point/line/polygon)
     */
    public void setLayerType(String type){
        layerType = type;
    }

    /**
     * set attributes for the layer
     *
     * @param ArrayList<WFSLayerAttribute> attributes - arraylist with all attributes to set for the layer
     */
    public void setLayerAttributes(ArrayList<WFSLayerAttribute> attributes){
        layerAttributes = attributes;
    }

    /**
     * add single item to the layer
     *
     * @param LocatableItem item - single item to add to the layer
     */
    public boolean addItem(LocatableItem item){
        return items.add(item);
    }

    /**
     * set selection status of the layer
     *
     * @param boolean act - true: layer is active and will be shown at the map, false: layer is inactive and will not be shown
     */
    public void setActive(boolean act){
        active = act;
    }

    /**
     * get string representation of the wfs layer
     *
     * @return String - string representation of the wfs layer
     */
    @Override
    public String toString(){
        //return "Name: " + layerName + ", Title: " + layerTitle + ", Type: " + layerType + ", Projection: " + layerProjection;
        return layerTitle;
    }
    
}
