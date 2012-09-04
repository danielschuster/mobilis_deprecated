/**
 * Segment.java
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
 * Segment
 * 
 * Funktion: Segment zur Abbildung einer Strecke zwischen zwei Punkten
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class Segment extends GeoObject {
    private Point startpunkt = null;
   private Point endpunkt = null;

     /**
     * Konstruktor:
     * @param Point Startpunkt, Point Endpunkt
     */
    public Segment(Point start, Point end) {
        if ((start == null) || (end == null)) {
            throw new NullPointerException("Fehlende Segmentpunkte");
        }

        startpunkt = start;
        endpunkt = end;
        updateExtent(start);
        updateExtent(end);
    }

     /**
     * Liefert den Schnittpunkt zweier Segmente
     * @param Segment seg
      *@return Point
     */
    public Point returnIntersectionPoint(Segment seg) {
	    return GeometryHelper.returnSegmentIntersection(this, seg);
    }
    /**
     * Liefert true, wenn sich Segment mit anderem Segment seg kreuzt
     * @param Segment seg
      *@return boolean
     */
    public boolean crosses(Segment seg) {
        return GeometryHelper.crosses(this, seg);
  }
   
    /**
     * Liefert den Startpunkt des Segments
     * @param
      *@return Point
     */
    public Point getStartpunkt() {
        return startpunkt;
    }

     /**
     * Liefert den Endpunkt des Segments
     * @param
      *@return Point
     */
    public Point getEndpunkt() {
        return endpunkt;
    }

     /**
     * Liefert das Segment als Liste von Punkten (Start- und Endpunkt)
     * @param
      *@return Point
     */
    public Point[] asPoints() {
        Point[] pl = new Point[2];
        pl[0] = this.getStartpunkt();
        pl[1] = this.getEndpunkt();

        return pl;
    }

     /**
     * Liefert true, wenn sich Segment mit einem anderen Geoobject schneidet
      * Schneiden bedeutet: Ber�hren oder kreuzen
     * @param GeoObject o
      *@return boolean
     */
    public boolean intersects(GeoObject o) {
        return (GeometryHelper.intersects(o, this));
    }

     /**
     * Liefert das Segment als Liste von Segmenten = sich selbst als Liste
     * @param
      *@return Segment[] 
     */
    public Segment[] getSegments() {
        return new Segment[] { this };
    }
    
     /**
     * Liefert die L�nge des Segments.
     * @param
      *@return double 
     */
    public double returnLength() {
        double a = ( (endpunkt.getX() - startpunkt.getX() ) * (endpunkt.getX() - startpunkt.getX() ) );
        double b = ( (endpunkt.getY() - startpunkt.getY() ) * (endpunkt.getY() - startpunkt.getY() ) );
        return Math.sqrt(a + b);
    }
    
    public String toString() {
    	return "Segment "+this.getId()+" Start "+this.getStartpunkt()+" Ende "+this.getEndpunkt() ;
    }
}
