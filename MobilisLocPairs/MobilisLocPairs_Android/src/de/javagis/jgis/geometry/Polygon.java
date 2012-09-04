/**
 * Polygon.java
 * ----------------------------------------------
 * Dieser Code ist Teil des Buches "Java & GIS". 
 * Autoren:
 * @author Bjoern Koos, Michael Herter
 * ----------------------------------------------
 *
 * Hinweis:
 * Die Software wird bereitgestellt, ohne dass
 * damit irgendeine direkte oder indirekte Gewaehr
 * fuer die Korrektheit und Funktionsfaehigkeit
 * uebernommen wird. Ebenso wird keinerlei Haftung
 * fuer Schaeden, fuer den Verlust von Daten etc.
 * uebernommen, die durch den Einsatz dieser Software
 * entstehen.
 */
package de.javagis.jgis.geometry;


import de.javagis.jgis.util.GeometryHelper;


/**
 * 
 * Polygon 
 * 
 * Funktion: Polygon-Objekt
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class Polygon extends Ring {
    
    /**
     * Erzeugt Polygon �ber St�tzpunkte
     * @param points St�tzpunkte
     * @throws Exception im Fehlerfall
     */
    public Polygon(Point[] points) throws Exception {
        super(points);
    }

    public Polygon(Point[] points, boolean clean) throws Exception {
        super(points, clean);
    }
    
    Polygon(double[][] xy) throws Exception {
        super(xy);
    }
    
    /**
     * Erzeugt Polygon aus Ring
     * @param r Ring
     * @throws Exception im Fehlerfall
     */
    public Polygon(Ring r) throws Exception {
        super(r.asPoints());
    }

    /**
     * Abfrage der Polygonfl�che
     * @return Fl�che
     */
    public double returnArea() {
       return GeometryHelper.calculateArea(this);   
    }
}
