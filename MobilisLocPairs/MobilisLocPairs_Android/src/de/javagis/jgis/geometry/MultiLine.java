/**
 * MultiLine.java 
 * ---------------------------------------------- 
 * Dieser Code ist Teil des Buches "Java & GIS". 
 * Autoren:
 * @author Bjoern Koos, Michael Herter
 * ----------------------------------------------
 * 
 * Hinweis: Die Software wird bereitgestellt, ohne dass damit irgendeine direkte
 * oder indirekte Gewaehr fuer die Korrektheit und Funktionsfaehigkeit
 * uebernommen wird. Ebenso wird keinerlei Haftung fuer Schaeden, fuer den
 * Verlust von Daten etc. uebernommen, die durch den Einsatz dieser Software
 * entstehen.
 */
package de.javagis.jgis.geometry;


import java.util.*;


/**
 * 
 * MultiLine
 * 
 * Funktion: Geometrie einer MultiLinie
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class MultiLine extends MultiGeoObject {
  /**
   * Linien werden in Liste gespeichert
   */
  protected List linien = new ArrayList();

  /** Konstruktor */
  public MultiLine() {
  }
   
  /**
   * F�gt Punktliste der MultiLine hinzu, erstellt daraus eine Linie
   * @param points zus�tzliche St�tzpunkte
   * @throws Exception im Fehlerfall
   */
  public void addPoints(Point[] points) throws Exception {
    Line l = new Line(points);
    addLine(l);
  }

  /**
   * F�gt Linie der MultiLinie hinzu
   * @param line hinzuzuf�gende Linie
   */
  public void addLine(Line line) {
    updateExtent(line);
    linien.add(line);
  }

  /**
   * @see de.javagis.jgis.geometry.MultiGeoObject#getElements()
   */
  public GeoObject[] getElements() {
    return getLines();
  }
  /**
   * Liefert MultiLine als Line-Liste
   * @param
   * @return Line[]
   */
    public Line[] getLines() {
        return (Line[]) linien.toArray(new Line[linien.size()]);
    }

  /**
   * Liefert MultiLine als Punktliste
   * @param
   * @return Point[]
   */
    public Point[] asPoints() {
        ArrayList points = new ArrayList();
    int size = linien.size();
    
    for (int i = 0; i < size; i++) {
      Line l = (Line) linien.get(i);
      
      List linePoints = Arrays.asList(l.asPoints());
      
      points.addAll(linePoints);
        }

        return (Point[]) points.toArray(new Point[points.size()]);
    }
  /**
   * Liefert MultiLine als Segment-Liste
   * @return Array der Segmente
   */
    public Segment[] getSegments() {
        ArrayList segments = new ArrayList();

    int size = linien.size();
    for (int i = 0; i < size; i++) {
            Segment[] segs = ((Line) linien.get(i)).getSegments();

            for (int s = 0; s < segs.length; s++) {
                segments.add(segs[s]);
            }
        }

        return (Segment[]) segments.toArray(new Segment[segments.size()]);
    }
  /**
   * Test, ob Objekte sich schneiden
   * @param obj Testobjekt
   * @return true, wenn Geoobjekt dieMultiLine schneidet
   */
    public boolean intersects(GeoObject o) {
        
        for (int l = 0; l < linien.size(); l++) {
           Line line = (Line) linien.get(l);
           if (line.intersects(o)) {
           		return true;
      }
    }

    return false;
  }
    public boolean isWithin(Extent queryExt) throws Exception {
        for (int i = 0; i < linien.size(); i++) {
            if (((Line) linien.get(i)).isWithin(queryExt)) {
                return true;
            }
        }

        return false;
    }
}
