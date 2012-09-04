/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: model
 */
package de.tud.server.model;


import de.javagis.jgis.geometry.GeoObject;
import java.util.ArrayList;


/**
 * Class: WLANAccessPoint.java
 * Function: object to represent a wlan access point
 *
 * @author Jan Scholze
 * @version 1.16
 */
public class WLANAccessPoint extends Beacon{

    protected GeoObject geometry;
    protected ArrayList<ItemAttribute> attributes;
    private boolean selected = false;
    private String macAddress = "";

    /**
     * constructor: create new wlan access point
     *
     * @param GeoObject geo - geometrical representation of the wlan access point (point)
     * @param ArrayList<ItemAttribute> attr - arraylist with attributes of the wlan access point (mac, ssid, standard)
     */
    public WLANAccessPoint(GeoObject geo, ArrayList<ItemAttribute> attr){
        this.geometry = geo;
        this.attributes = attr;
    }

    /**
     * get geometrical representation of the wlan access point
     *
     * @return GeoObject - geometrical representation of the wlan access point (point)
     */
    public GeoObject getGeometry(){
        return this.geometry;
    }

    /**
     * get attributes of the wlan access point
     *
     * @return ArrayList<ItemAttribute> - arraylist with attributes of the wlan access point (mac, ssid, standard)
     */
    public ArrayList<ItemAttribute> getAttributes(){
        return this.attributes;
    }

    /**
     * get id of the item/wlan access point
     *
     * @return String - id of the item/wlan access point
     */
    @Override
    public String getItemId(){
        return macAddress;
    }

    /**
     * get selection status of the wlan access point
     *
     * @return boolean - true: access point selected and shown at the map, false: access point not selected and not shown
     */
    public boolean isSelected(){
        return selected;
    }

    /**
     * set selection status of the wlan access point
     *
     * @param boolean sel - true: access point selected and shown at the map, false: access point not selected and not shown
     */
    public void setSelected(boolean sel){
        selected = sel;
    }

    /**
     * get string representation of the item/wlan aceess point
     *
     * @return String - string representation of the item/wlan access point
     */
    @Override
    public void setItemId(String mac){
        macAddress = mac;
    }
    
}
