/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: de.tud.server.model
 */
package de.tud.server.model;


import de.javagis.jgis.geometry.Polygon;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Class: Building.java
 * Function: building representation
 *
 * @author Jan Scholze
 * @version 1.22
 */
public class Building extends BuildingPart{

    private Collection<Floor> floors;

    /**
     * constructor: create new building
     *
     * @param Polygon geo - geometry (polygon) of the building
     * @param ArrayList<ItemAttribute> attr - list with building/polygon attributes
     */
    public Building(Polygon geo, ArrayList<ItemAttribute> attr){
        super(geo, attr);
    }

    /**
     * add floor
     *
     * @param Floor floor - floor to add
     */
    public void addFloor(Floor floor){
        floors.add(floor);
    }

    /**
     * get all floor of the building
     *
     * @return Collection<Floor> - collection with floors of the building
     */
    public Collection<Floor> getFloors(){
        return floors;
    }
}
