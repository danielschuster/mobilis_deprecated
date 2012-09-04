/**
 * MultiPoint.java
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
 * MultiPoint
 * 
 * Funktion: MultiPoint kann mehrere Point-Objekte enthalten.
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class MultiPoint extends MultiGeoObject {
  /**
   * Liste der St�tzpunkte
   */
  protected List punkte = null;

  /** Konstruktor */
  public MultiPoint() {
      punkte = new ArrayList();
  }
   
  /**
   * F�gt Einzelpunkt hinzu
   * @param p neuer Point
   */
  public void addPoint(Point p) {
    punkte.add(p);
    
    updateExtent(p);
  }

  /**
   * F�gt Punktliste hinzu
   * @param points neue Punkte
   * @throws Exception im Fehlerfall
   */
  public void addPoints(Point[] points) throws Exception {

    for (int i = 0; i < points.length; i++) {
      addPoint(points[i]);
    }
  }

  /**
   * @see de.javagis.jgis.geometry.MultiGeoObject#getElements()
   */
  public GeoObject[] getElements() {
    return asPoints();
  }
  
  /**
   * Liefert MultiPunkt als Punktliste, vererbt aus GeoObject
   * @return Enthaltene Punkte
   */
    public Point[] asPoints() {
        return (Point[]) punkte.toArray(new Point[punkte.size()]);
    }

  /**
   * Liefert true, wenn GeoObject mit MultiPunkt schneidet
   * @param obj Testobjekt
   * @return true, wenn ein Punkt des Multiobjektes auf der Geometrie des
   * Testobjektes liegt
   */
  public boolean intersects(GeoObject obj) {
    int size = punkte.size();

    for (int i = 0; i < size; i++) {
      if (GeometryHelper.intersects(obj, ((Point) punkte.get(i)))) {
                return true;
            }
        }

    return false;
    }

  /**
   * Liefert MultiPunkt als Segment-Liste
   * @return null, da ein MultiPoint niemals Segmente hat
   */
    public Segment[] getSegments() {
        return null;
  }
    public boolean isWithin(Extent queryExt) throws Exception {
        for (int i = 0; i < punkte.size(); i++) {
            if (((Point) punkte.get(i)).isWithin(queryExt)) {
                return true;
            }
        }

        return false;
    }
}
