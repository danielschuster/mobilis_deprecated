/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tud.server.model;

import de.javagis.jgis.geometry.Point;

/**
 *
 * @author Jan
 */
public class Route extends LocatableItem{

    private Point[] routePoints;
    private double routeDistance;

    public Route(Point[] points, double distance){
        routePoints = points;
        routeDistance = distance;
    }


    public double getRouteDistance(){
        return routeDistance;
    }

    public Point[] getRoutePoints(){
        return routePoints;
    }
}
