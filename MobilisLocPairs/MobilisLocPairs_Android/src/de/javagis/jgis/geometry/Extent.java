/**
 * Extent.java
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


import java.text.*;


/**
 * Extent
 * 
 * Funktion: Datenstruktur zur Abbildung einer raeumlichen Ausdehnung
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class Extent {
  /**
   * Zur Formatierung von Zahlenausgaben
   */
  private static DecimalFormat nf = new DecimalFormat("0.000000");

  /**
   * Minimum der Ausdehnung auf X-Achse
   */
    private double xmin = 0.0;

  /**
   * Minimum der Ausdehnung auf Y-Achse
   */
    private double ymin = 0.0;

  /**
   * Maximum der Ausdehnung auf X-Achse
   */
    private double xmax = 0.0;

  /**
   * Maximum der Ausdehnung auf Y-Achse
   */
    private double ymax = 0.0;

  /**
   * Initialisierungskonstruktor
   * @param xmin Minimale X-Koordinate
   * @param ymin Minimale Y-Koordinate
   * @param xmax Maximale X-Koordinate
   * @param ymax Maximale Y-Koordinate
   */
    public Extent(double xmin, double ymin, double xmax, double ymax) {
        setExtent(xmin, ymin, xmax, ymax);
    }

  /**
   * Erzeugt eine Objektkopie des aktuellen Extents
   * @return Extent als komplette Kopie
   */
    public Extent copy() {
        return new Extent(xmin, ymin, xmax, ymax);
    }
   /**
    * Verschmelzt die neue Koordinate mit der derzeitigen Ausdehnung
   * @param x x-Koordinate, die abgedeckt werden soll
   * @param y y-Koordinate, die abgedeckt werden soll
   */
  public void merge(double x, double y) {

    if (xmin > x) {
      xmin = x;
    }
    if (xmax < x) {
      xmax = x;
    }
    if (ymin > y) {
      ymin = y;
    }
    if (ymax < y) {
      ymax = y;
    }
  }
  /**
   * Vereinigt den aktuellen Extent mit dem angegebenen.
   * @param ext Anderer Extent
   */
    public Extent mergeExtent(Extent ext) {
        if (ext.getXmax() > xmax) {
            xmax = ext.getXmax();
        }

        if (ext.getYmax() > ymax) {
            ymax = ext.getYmax();
        }

        if (ext.getXmin() < xmin) {
            xmin = ext.getXmin();
        }

        if (ext.getYmin() < ymin) {
            ymin = ext.getYmin();
        }
        
        return this;
    }

  /**
   * Setzt den Extent auf die angegebenen Werte. Die Werte werden sortiert und
   * so - auch bei falscher Sortierung innerhalb einer Ausdehnungsrichtung (je X
   * bzw. Y) - den richtigen Attributen zugewiesen
   * @param xmin Minimale X-Koordinate
   * @param ymin Minimale Y-Koordinate
   * @param xmax Maximale X-Koordinate
   * @param ymax Maximale Y-Koordinate
   */
    public void setExtent(double xmin, double ymin, double xmax, double ymax) {
        if (xmin > xmax) {
            this.xmin = xmax;
            this.xmax = xmin;
        } else {
            this.xmin = xmin;
            this.xmax = xmax;
        }

        if (ymin > ymax) {
            this.ymin = ymax;
            this.ymax = ymin;
        } else {
            this.ymin = ymin;
            this.ymax = ymax;
        }
    }

    public double getXmin() {
        return xmin;
    }

    public double getYmin() {
        return ymin;
    }

    public double getXmax() {
        return xmax;
    }

    public double getYmax() {
        return ymax;
    }

    public double getWidth() {
        return Math.abs(xmax - xmin);
    }

    public double getHeight() {
        return Math.abs(ymax - ymin);
    }

    public String toString() {
	 return "Min=(" + nf.format(xmin) + ", " + nf.format(ymin) + ") Max=(" + nf.format(xmax) + ", " + nf.format(ymax) + ")";
    }
}
