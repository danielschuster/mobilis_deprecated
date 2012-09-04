/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: de.tud.server.model
 */
package de.tud.server.model;


import de.javagis.jgis.geometry.GeoObject;
import java.util.ArrayList;


/**
 * Class: BuildingPart.java
 * Function: abstract class for building parts (buildings, floors, rooms)
 *
 * @author Jan Scholze
 * @version 1.17
 */
public class BuildingPart extends LocatableItem{

    protected GeoObject geometry;
    protected ArrayList<ItemAttribute> attributes;
    private boolean selected = false;

    /**
     * constructor: create new building part
     *
     * @param GeoObject geo - geometry of the building part (polygon or point)
     * @param ArrayList<ItemAttribute> attr - list with attributes
     */
    public BuildingPart(GeoObject geo, ArrayList<ItemAttribute> attr){
        this.geometry = geo;
        this.attributes = attr;
    }

    /**
     * get geometry of the building part
     *
     * @return GeoObject - polygon or point of the building part
     */
    public GeoObject getGeometry(){
        return this.geometry;
    }

    /**
     * get all attributes of the building part
     *
     * @return ArrayList<ItemAttribute> - attributes of the building part
     */
    public ArrayList<ItemAttribute> getAttributes(){
        return this.attributes;
    }

    /**
     * get selection status of the building part
     *
     * @return boolean - selection status of the building part
     */
    public boolean isSelected(){
        return selected;
    }

    /**
     * set selection status of the building part
     *
     * @param boolean sel - selection status to set for the building part
     */
    public void setSelected(boolean sel){
        selected = sel;
    }
}
