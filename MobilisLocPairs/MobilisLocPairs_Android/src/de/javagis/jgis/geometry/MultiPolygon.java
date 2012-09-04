/**
 * MultiPolygon.java
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
 * MultiPolygon
 * 
 * Funktion: MultiGeoObject, welches Polygone als Teilobjekte enth�lt.
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class MultiPolygon extends MultiGeoObject {
  /**
   * Verzeichnis der Donuts, Key = Polygon
   */
    private Hashtable donuts = new Hashtable();
  /**
   * Liste aller Polygone (Positiv-Fl�chen)
   */
    private List polys = new ArrayList();
  /**
   * Liste aller Holes (Negativ-Fl�chen)
   */
    private List holes = new ArrayList();
  /**
   * Liste aller Polygone und Holes
   */
    private List flaechen = new ArrayList();

  /**
   * isCleaned = true ==> Chains sind aufgel�st in Polygone und Holes
   */
  private boolean isCleaned = false;

  /**
   * Konstruktor
   */
    public MultiPolygon() {
    }

  /** 
   * Es wird hier im ersten Schritt angenommen, dass keine falschen Polygone vorhanden sind! 
   * Ein verfeinerter LineSweep-Algorithmus k�nnte eingesetzt werden, um 
   * eine schnelle Schnittpr�fung umzusetzen.
   */
	public void addPoints(Point[] points) throws Exception {
		add(points, true);
	}
	
    public void add(Point[] points, boolean alreadyClean) throws Exception {
        if (alreadyClean) {
        	addCleanRing( points );
                isCleaned = true;
        } else {
        	add(new Chain(points));
        }
    }

    /**
     * unter der vereinfachenden Annahme, dass der Ring ein g�ltiges Polygon ist.
     */
    private void addCleanRing(Point[] rp) throws Exception {
        
        Ring r = new Ring(rp, true);
        
        polys.add( new Polygon(rp, true) );
        flaechen.add( r );
        
        this.updateExtent( r );
    }
    
    private void add(Chain c) throws Exception {
        Ring[] ringe = GeometryHelper.dissolve(c);

        if (ringe != null) {
            for (int i = 0; i < ringe.length; i++) {
                add(ringe[i]);
            }
        }
    }

  /**
   * F�gt Ring dem MultiPolygon hinzu
   * @param r neuer Ring
   * @throws Exception im Fehlerfall
   */
  private void add(Ring r) throws Exception {
        updateExtent(r);

        // Drehsinn pr�fen
        if (r.getDrehsinn() > 0) {
            Polygon p = (r.getInternal()!=null ? new Polygon(r.getInternal()) : new Polygon(r));
            Donut d = new Donut(p);

            donuts.put(p, d);
            polys.add(p);
        } else {
            Hole h = new Hole(r);
            holes.add(h);

            Polygon poly;

            // Donut suchen, in das Hole faellt... 
            for (int i = 0; i < polys.size(); i++) {
                poly = (Polygon) polys.get(i);

                if (h.isWithin(poly)) {
                    Donut d = (Donut) donuts.get(poly);
                    d.addHole(h);
                }
            }
        }

        flaechen.add(r);
    }
  /**
   * Bereinigt MultiPolygon, d.h. Chain-Liste wird in Polygone und Holes
   * umgewandelt
   * @throws Exception im Fehlerfall
   */
  public void clean() throws Exception {

    //Deklariere Chain
    Chain c;
    // Durchlaufe alle Chains
    for (int i = 0; i < flaechen.size(); i++) {
      c = (Chain) flaechen.get(i);
      // L�se Chain in Ringe auf
      Ring[] ringe = GeometryHelper.dissolve(c);
      // F�ge Ringe MultiPolygon hinzu
      if (ringe != null) {
        for (int j = 0; j < ringe.length; j++) {
          add(ringe[j]);
        }
      }
    }
    // Leere Liste der Chains
    flaechen.clear();
    // Holes zu Polygonen �ber Donut zuordnen
    if (this.hasUncleanedHoles()) {
      cleanHoles();
    }

    // Setze isCleaned = true
    isCleaned = true;
  }

  public boolean isClean() {
    return isCleaned;
  }
  
  private void cleanHoles() throws Exception {
    // Deklariere Hole
    Hole h;
    // Durchlaufe Liste aller Holes
    for (int i = 0; i < holes.size(); i++) {
      h = (Hole) holes.get(i);
      // Deklariere Polygon
      Polygon poly;
      // Durchlaufe Liste aller Polygone
      for (int j = 0; j < polys.size(); j++) {
        poly = (Polygon) polys.get(j);
        // Pr�fe, ob Hole in Polygon f�llt ...
        if (h.isWithin(poly)) {
          // ... wenn ja, dann f�ge Hole in Donut des Polygons ein
          Donut d = (Donut) donuts.get(poly);
          d.addHole(h);
        }
      }

    }
    holes.clear();

  }

    public Ring[] getRings() {
        return (Ring[]) flaechen.toArray(new Ring[flaechen.size()]);
    }

  /**
   * @see de.javagis.jgis.geometry.MultiGeoObject#getElements()
   */
  public GeoObject[] getElements() {
    List alleElemente = new ArrayList();
    if (polys.size()>0 || holes.size()>0) {
        alleElemente.addAll( polys );
        alleElemente.addAll( holes );
    } else {
        alleElemente.addAll( flaechen );
    }
    return (GeoObject[]) alleElemente.toArray(new GeoObject[alleElemente.size()]);
  }
  
  /**
   * Liefert alle Segmente eines MultiPolygons als Liste von Segmenten zur�ck
   * @return Array der Segmente
   */
    public Segment[] getSegments() {
        ArrayList segments = new ArrayList();
        Ring[] rings = this.getRings();

        for (int i = 0; i < rings.length; i++) {
            Segment[] segs = rings[i].getSegments();

            segments.addAll( Arrays.asList(segs) );
        }

        return (Segment[]) segments.toArray(new Segment[segments.size()]);
    }

  /**
   * Liefert alle Polygone eines MultiPolygons als Liste von Polygonen zur�ck
   * @param
   * @return Polygon[]
   */
    public Polygon[] getPolygons() {
        return (Polygon[]) polys.toArray(new Polygon[polys.size()]);
    }

  /**
   * Liefert alle Holes(L�cher) eines MultiPolygons, die noch nicht einem
   * Umgebungspolygon zugewiesen wurden (=uncleanedHoles)
   * @param
   * @return Hole[]
   */
  public Hole[] getUncleanedHoles() {
    return (Hole[]) holes.toArray(new Hole[holes.size()]);
    }

  /**
   * Gibt an, ob Holes vorhanden sind
   * @return true, wenn Holes vorhanden
   */
  public boolean hasUncleanedHoles() {
    return (holes.size() > 0);
  }

  /**
   * Liefert MultiPolygon als Array von Punkten zur�ck
   * @return Array mit St�tzpunkten
   */
    public Point[] asPoints() {
        ArrayList points = new ArrayList();

        for (int i = 0; i < flaechen.size(); i++) {
            Line line = (Line) flaechen.get(i);
            points.addAll(line.asPointList());
        }

        return (Point[]) points.toArray(new Point[points.size()]);
    }

  /**
   * Schnitt-Test f�r MultiPolygon
   * @param obj Testobjekt
   * @return true, wenn MultiPolygon von obj geschnitten wird
   */
    public boolean intersects(GeoObject o) {

        for (int i = 0; i < flaechen.size(); i++) {
           Line line = (Line) flaechen.get(i);
           if (line.intersects(o)) {
           	  return true;
           }
        }

        return false;
    }

  /**
   * Gibt die Holes eines Polygons zur�ck
   * @param poly Polygon
   * @return Array mit Holes des Polygons, null, wenn MultiPolygon nicht
   * bereinigt ist
   */
  public Hole[] getPolygonHoles(Polygon poly) {

    if (!isCleaned) {
      return null;
    }
    Donut d = (Donut) donuts.get(poly);
    return d.getHoles();
  }
  /**
   * Pr�fung, ob in Polygon Holes liegen
   * @param poly Polygon, f�r das die Pr�fung erfolgt
   * @return true, wenn Holes vorhanden
   */
  public boolean hasPolygonHoles(Polygon poly) throws Exception {
      if (!isCleaned) { // Vereinfachung, eigentlich m�sste zuerst gecleaned werden
          return false;
      }
      
      Donut d = (Donut) donuts.get(poly);

      return d != null && d.hasHoles();
  }

  /**
   * Abfrage, ob MultiPolygon Holes (L�cher) kennt
   * @return true, wenn Holes vorhanden sind
   */
  public boolean hasHoles() {
    return (getUncleanedHoles().length > 0);
  }

    public boolean hasDonuts() {
        return (donuts.size() != 0);
    }
  /**
   * Gibt die Fl�che des MultiPolygons an.
   * @return Fl�che des MultiPolygons -1, wenn MultiPolygon nicht bereinigt ist
   */
  public double returnArea() {

    if (!isCleaned) {
      return -1;
    }

    double area = 0.0;
    for (int i = 0; i < polys.size(); i++) {
      Polygon poly = (Polygon) polys.get(i);
      Donut d = (Donut) donuts.get(poly);
      area = area + d.returnArea();
    }
    return area;
  }

  /**
   * Donut: Interne Klasse zur Speicherung von Polygonen + Holes Donut besteht
   * immer genau aus 1 Polygon und n-Holes; Holes werden in der Funktion clean()
   * einem Donut hinzugef�gt
   */
    private class Donut {
    /**
     * L�cher im Donut
     */
    private List holesInDonut = new ArrayList();

    /**
     * Referenz auf Hauptpolygon
     */
    private Polygon p = null;

    /**
     * Erzeugt einen Donut
     * @param p Hauptpolygon
     */
        private Donut(Polygon p) {
            this.p = p;
        }

    /**
     * Liefert R�ckreferenz auf das Polygon
     * @return Hauptpolygon
     */
        public Polygon getPolygon() {
            return p;
        }

    /**
     * F�gt Hole zu Donut hinzu
     * @param h Hole
     */
        public void addHole(Hole h) {
      holesInDonut.add(h);
        }

    /**
     * Liefert alle Holes des Donuts
     * @return Array mit Holes
     */
    public Hole[] getHoles() {
      return (Hole[]) holesInDonut.toArray(new Hole[holesInDonut.size()]);
    }
    /**
     * Abfrage, ob L�cher vorhanden sind
     * @return true, wenn L�cher im Donut
     */
    public boolean hasHoles() {
      return holesInDonut.size() > 0;
    }

    /**
     * Fl�chenberechnung des Donuts
     * @return Fl�che des Donuts (abz�glich der Fl�chen der L�cher)
     */
    public double returnArea() {
      double area = p.returnArea();

      for (int i = 0; i < holesInDonut.size(); i++) {
        area += (((Hole) holesInDonut.get(i)).returnArea());

      }
      return area;
    }

  }

    public boolean isWithin(Extent queryExt) throws Exception {
        for (int i = 0; i < flaechen.size(); i++) {
            if (((Ring) flaechen.get(i)).isWithin(queryExt)) {
                return true;
            }
        }

        return false;
    }
}
