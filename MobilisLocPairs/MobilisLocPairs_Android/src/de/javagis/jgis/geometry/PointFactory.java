/**
 * PointFactory.java
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


/**
 * 
 * PointFactory
 * 
 * Funktion: Factory f�r Punktobjekte
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class PointFactory {
  /**
   * Konstante f�r Integer-Genauigkeit
   */
    public static final int PRECISION_INTEGER = 1;

  /**
   * Konstante f�r Double-Genauigkeit
   */
    public static final int PRECISION_DOUBLE = 2;

  /**
   * Nicht-sichtbarer Konstruktor, da diese Klasse nur Hilfsmethoden (statische
   * Methoden) anbietet.
   */
    private PointFactory() {
    }

  /**
   * Erzeugt einen Punkt mit Integer-Genauigkeit
   * @param x X-Koordinate
   * @param y Y-Koordinate
   * @return PointInt-Objekt
   */
    static public Point createPoint(int x, int y) {
        PointInt p = new PointInt(x, y);
        p.setExtent(new Extent(x, y, x, y));
        return p;
    }

  /**
   * Erzeugt einen Punkt mit Double-Genauigkeit
   * @param x X-Koordinate
   * @param y Y-Koordinate
   * @return PointDouble-Objekt
   */
    static public Point createPoint(double x, double y) {
        PointDouble p = new PointDouble(x, y);
        p.setExtent(new Extent(x, y, x, y));
        return p;
    }

  /**
   * Gibt den Typ des Punkts an
   * @param p zu pr�fender Punkt
   * @return int-Konstante der Genauigkeit
   */
    static public int getType(Point p) {
        if (p instanceof PointInt) {
            return PRECISION_INTEGER;
        }
        return PRECISION_DOUBLE;
    }
}
