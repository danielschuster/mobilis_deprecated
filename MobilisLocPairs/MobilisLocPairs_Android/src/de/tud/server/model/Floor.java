/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: de.tud.server.model
 */
package de.tud.server.model;


import de.javagis.jgis.geometry.Polygon;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Class: Floor.java
 * Function: floor representation
 *
 * @author Jan Scholze
 * @version 1.24
 */
public class Floor extends BuildingPart{

    private Building building;
    private Collection<Room> rooms;

    /**
     * constructor: create new floor
     *
     * @param Polygon geo - geometry (polygon) of the floor
     * @param ArrayList<ItemAttribute> attr - list with floor/polygon attributes
     */
    public Floor(Polygon geo, ArrayList<ItemAttribute> attr){
        super(geo, attr);
    }

    /**
     * add room
     *
     * @param Room room - room to add
     */
    public void addRoom(Room room){
        rooms.add(room);
    }

    /**
     * get building the floor belongs to
     *
     * @return Building - building the floor belongs to
     */
    public Building getBuilding(){
        return building;
    }

    /**
     * get all rooms of the floor
     *
     * @return Collection<Room> - collection with rooms of the floor
     */
    public Collection<Room> getRooms(){
        return rooms;
    }
}
