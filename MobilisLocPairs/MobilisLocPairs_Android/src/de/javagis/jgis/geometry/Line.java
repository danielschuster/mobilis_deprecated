/**
 * Line.java
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
import java.util.*;


/**
 * 
 * Line
 * 
 * Funktion: Geometrieobjekt Linie
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class Line extends GeoObject {
    // Liste der Liniensegmente wird leer initialisiert
    ArrayList segmente = new ArrayList();
    ArrayList pointList = new ArrayList();
    private Point startPoint = null;
    private Point endPoint = null;
    
    private final boolean DIRECTCOORDS = true;
    private double xy[][];
    
    protected Point getStartPoint() {
        if (DIRECTCOORDS) {
            return PointFactory.createPoint(xy[0][0],xy[0][1]);
        } else {
            return startPoint;
        }
    }
    
    protected Point getEndPoint() {
        if (DIRECTCOORDS) {
            return PointFactory.createPoint(xy[xy.length-1][0],xy[xy.length-1][1]);
        } else {
            return endPoint;
        } 
    }
    
    Line(double[][] xy) {
        copyToInternal(xy);
    }
    
    /**
     * Konstruktor: Ein Line-Objekt wird durch �bergabe einer Punkt-Liste generiert,
     * die in einem internen Point-Array gespeichert wird.
     * @param Point[]
     */
    public Line(Point[] punkte) throws Exception {
        if ((punkte == null) || (punkte.length < 2)) {
            throw new Exception("Fehlende St�tzpunkte!");
        }
        if (DIRECTCOORDS) {
            copyToInternal(punkte);
        } else {
            pointList.addAll(Arrays.asList(punkte));
        
            startPoint = punkte[0];
            
            for (int i = 0; i < punkte.length; i++) {
                if (i > 0) {
                    addSegment(punkte[i - 1], punkte[i]);
                }
            }
        }
    }
    
    public double[][] getInternal() {
        return xy;
    }
       
    private void copyToInternal(double[][] xy) {
       this.xy = xy; /*new double[xy.length][2];
       for (int i=0; i<xy.length;i++) {
           this.xy[i][0] = xy[i][0];
           this.xy[i][1] = xy[i][1];
       }*/
       
       //updateExtent
       Extent ext = this.getExtent();
       double ext_xmin = (ext != null ? ext.getXmin() : xy[0][0]);
       double ext_ymin = (ext != null ? ext.getYmin() : xy[0][1]);
       double ext_xmax = (ext != null ? ext.getXmax() : xy[0][0]);
       double ext_ymax = (ext != null ? ext.getYmax() : xy[0][1]);
       
       for (int i=0; i<xy.length; i++) {
           if (xy[i][0] > ext_xmax) {
               ext_xmax = xy[i][0];
           }
           if (xy[i][1] > ext_ymax) {
               ext_ymax = xy[i][1];
           }
           if (xy[i][0] < ext_xmin) {
               ext_xmin = xy[i][0];
           }
           if (xy[i][1] < ext_ymin) {
               ext_ymin = xy[i][1];
           }
        }
        this.setExtent(new Extent(ext_xmin,ext_ymin,ext_xmax,ext_ymax));
    }

    private void copyToInternal(Point[] pkt) {
        xy = new double[pkt.length][2];
        
        for (int i=0; i<pkt.length; i++) {
            updateExtent(pkt[i]);
        
            xy[i][0] = pkt[i].getX();
            xy[i][1] = pkt[i].getY();
        }
    }
    
    private Point[] extractPoints() {
        Point[] pt = new Point[xy.length];
        
        for (int i=0; i<xy.length; i++) {
            pt[i] = PointFactory.createPoint(xy[i][0],xy[i][1]);
        }
        return pt;
    }
    protected void addSegment(Point start, Point end) throws Exception {
        Segment s = new Segment(start, end);
        segmente.add(s);
        updateExtent(s);
        endPoint = end;
    }
    
    /**
     * F�gt Linie Punkt hinzu, erg�nzt Segment und aktualsiert Endpunkt der Linie
     * @param Point p
     *@return
     */
    public void addPoint(Point p) throws Exception {
        Segment s = new Segment(endPoint, p);
        segmente.add(s);
        pointList.add(p);
        updateExtent(s);
        endPoint = p;
    }

    /**
     * Liefert Linie als Punkte im ArrayList.
     * @param
     *@return ArrayList
     */

    public List asPointList() {
        if (DIRECTCOORDS) {
            return Arrays.asList( extractPoints() );   
        } else {
            return pointList;
        }
    }
    /**
     * Liefert Linie als Segmentliste
     * @param
     *@return Segment[]
     */
    public Segment[] getSegments() {
        if (DIRECTCOORDS) {
            Point[] pt = extractPoints();
            List seg = new ArrayList();
            for (int i = 0; i < pt.length-1; i++) {
                seg.add( new Segment(pt[i],pt[i+1]) );
            }
            return (Segment[]) seg.toArray(new Segment[seg.size()]);
        } else {
            return (Segment[]) this.segmente.toArray(new Segment[segmente.size()]);
        }
    }
    
  /**
   * Liefert true, wenn sich Linie selbst kreuzt.
   * @return true, wenn sich Segmente untereinander schneiden
   */
    public boolean isSelfIntersecting() {
        Segment[] segmente = this.getSegments();
//    return GeometryHelper.intersectsInner(segmente);
    int size = segmente.length;

    for (int i = 0; i < size; i++) {
            for(int j=0;j < i; j++) {
                if(segmente[i].crosses( segmente[j] ) ) {
                    return true;
                }
            }
        }
        return false;        
    }
    
    /**
     * Liefert die Linie als Liste von Punkten.
     * @param
     *@return Point[]
     */
    public Point[] asPoints() {
        if (DIRECTCOORDS) {
            return extractPoints();
        } else {
            return (Point[]) pointList.toArray(new Point[pointList.size()]);
        }
    }
  public boolean isWithin(Extent ext) throws Exception {
      return GeometryHelper.isWithin(ext, this);
  }
    
    /**
     * Liefert true, wenn sich Geoobjekt o mit einem der Segmente schneidet.
     * @param GeoObject o
     *@return true
     */
    public boolean intersects(GeoObject o) {
        Segment[] segmente =  this.getSegments();
        
        for (int i = 0; i < segmente.length; i++) {
            if (segmente[i].intersects(o)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Liefert die L�nge der Linie.
   * @return L�nge
     */
    public double returnLength() {
       double l = 0.0;
       Segment[] segmente =  this.getSegments();
        
        for (int i = 0; i < segmente.length; i++) {
            l = l + segmente[i].returnLength();
        }
       return l;
       
    }
}
