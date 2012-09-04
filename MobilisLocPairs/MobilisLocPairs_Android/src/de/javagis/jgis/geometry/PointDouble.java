
 /**
 * PointDouble.java
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
 * PointDouble
 * 
 * Funktion: Konkrete Punktklasse, die die Koordinaten in Double-Genauigkeit
 * speichert
 * 
 * @author Bjoern Koos, Michael Herter
 */
class PointDouble extends Point {

  /**
   * X-Koordinate
   */
  private double x = 0;

  /**
   * Y-Koordinate
   */
  private double y = 0;

  /**
   * Copy-Konstruktor f�r Punkt-Objekt
   * @param punkt Punkt, dessen Eigenschaften �bernommen werden sollen
   * @throws Exception im Fehlerfall
   */
    public PointDouble(Point punkt) throws Exception {
        x = punkt.getX();
        y = punkt.getY();
        updateExtent(this);
    }

  /**
   * Copy-Konstruktor f�r Punkt-Objekt
   * @param x Initiale X-Koordinate
   * @param y Initiale Y-Koordinate
   */
    public PointDouble(double x, double y) {
        this.x = x;
        this.y = y;
        updateExtent(this);
    }

  /**
   * Gibt eine Kopie des Punkts zur�ck
   * @return Kopie
   * @throws Exception im Fehlerfall
   */
    public Object copy() throws Exception {
        return new PointDouble(this);
    }

    /** Verhalten des Punktes ------------**/
  /**
   * Gebe die Koordinaten der Lage
   * @return X-Koordinate
   */
    public double getX() {
        return x;
    }

  /**
   * Gebe die Koordinaten der Lage
   * @return Y-Koordinate
   */
    public double getY() {
        return y;
    }

  /**
   * Setze die x-Koordinate der Lage
   * @param xwert Wert f�r X-Koordinate
   */
    public void setX(double xwert) {
        x = xwert;
    }

  /**
   * Setze die y-Koordinate der Lage
   * @param ywert Wert f�r Y-Koordinate
   */
    public void setY(double ywert) {
        y = ywert;
    }

    public Point[] asPoints() {
        Point[] pl = new Point[1];
        pl[0] = this;

        return pl;
    }

    public boolean intersects(GeoObject o) {
        return (GeometryHelper.intersects(o, this));
    }
}
