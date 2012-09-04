/**
 * Hole.java
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
 * Hole
 * 
 * Funktion: Darstellung eines Loches, d.h. eines Polygons mit negativer
 * Innenfl�che
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class Hole extends Polygon {

  /**
   * Konstruktor: Hole kann entweder �ber Punktliste oder Ring erzeugt werden.
   * @param p St�tzpunkte
   * @throws Exception im Fehlerfall
   */
    public Hole(Point[] p) throws Exception {
        super(p);
    }

  /**
   * Konstruktor: Hole kann entweder �ber Punktliste oder Ring erzeugt werden.
   * @param r Ring
   * @throws Exception im Fehlerfall
   */
    public Hole(Ring r) throws Exception {
        super(r.asPoints());
    }
  /**
   * Abfrage der Negativfl�che eines Holes.
   * @return double (negativer Wert)
   */
  public double returnArea() {
    return -GeometryHelper.calculateArea(this);
  }
}
