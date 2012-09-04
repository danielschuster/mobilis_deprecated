/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tud.server.model;


import de.javagis.jgis.geometry.Polygon;
import java.util.ArrayList;


/**
 *
 * @author Jan
 */
public class Room extends BuildingPart{

    private Floor floor;

    public Room(Polygon geo, ArrayList<ItemAttribute> attr){
        super(geo, attr);
    }

    public Floor getFloor(){
        return floor;
    }
}
