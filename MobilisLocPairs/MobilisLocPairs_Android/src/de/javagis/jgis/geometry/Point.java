/**
 * Point.java
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
import java.text.*;


/**
 * 
 * Point
 * 
 * Funktion: Basisklasse eines Punkt-Objekts.
 * 
 * @author Bjoern Koos, Michael Herter
 */
public abstract class Point extends GeoObject {
  /**
   * Erzeugen einer identischen Objekt-Kopie
   * @throws Exception im Fehlerfall
   * @return Kopie
   */
    abstract public Object copy() throws Exception;

  /**
   * Formatierungshilfe bei Ausgabe von Zahlen
   */
    static private DecimalFormat df = new DecimalFormat("0.00");

  /**
   * Gebe die Koordinaten der Lage
   * @return X-Koordinate
   */
    abstract public double getX();

  /**
   * Gebe die Koordinaten der Lage
   * @return Y-Koordinate
   */
    abstract public double getY();

  /**
   * Setze die Koordinaten der Lage
   * @param xwert neuer Wert der X-Koordinate
   */
    abstract public void setX(double xwert);

  /**
   * Setze die Koordinaten der Lage
   * @param ywert neuer Wert der Y-Koordinate
   */
    abstract public void setY(double ywert);

  /**
   * Gibt die Eigenschaften des Punktes in einem String an
   * @return Formatierter String
   */
    public String toString() {
	return toString("/", df);	
    }
	
  /**
   * Gibt die Eigenschaften des Punktes in einem String an
   * @param separator Separator, der als Trennzeichen der X- und Y-Koordinate
   * verwendet wird
   * @param nf NumberFormat, welches die Formatierung der Koordinaten-Werte
   * �bernimmt
   * @return Formatierter String
   */
    public String toString(String separator, NumberFormat nf) {
        return nf.format(getX()) + separator + nf.format(getY());
    }

  /**
   * Vergleich mit Punkt
   * @param p Vergleichspunkt
   * @return true, wenn Punkte die gleiche Lage haben
   */
    public boolean equals(Point p) {
        return ((p != null) && (this.getX() == p.getX()) && (this.getY() == p.getY()));
    }

  /**
   * Vergleich mit Objekten
   * @param obj Vergleichsobjekt
   * @return true, wenn obj ein Punkt ist und dieser equals erf�llt
   */
    public boolean equals(Object p) {
        if (p!=null && p instanceof Point) {
            return this.equals((Point)p);
        }
	return false;
    }

  /**
   * Liefert St�tzpunkte
   * @return Array mit St�tzpunkten (nur dieser Punkt selber)
   */
    public Point[] asPoints() {
        Point[] pl = new Point[1];
        pl[0] = this;
        return pl;
    }
  /**
   * Schnitt-Test mit anderem GeoObject
   * @param obj Testobjekt
   * @return true, wenn Punkt auf der Geometrie des anderen GeoObject liegt
   */
    public boolean intersects(GeoObject obj) {
        return (GeometryHelper.intersects(obj, this));
    }

    public boolean isWithin(Extent ext) throws Exception {
        return GeometryHelper.isWithin(ext, this);
    }
   
  /**
   * Liefert Segmente des GeoObjects
   * @return null, da ein Punkt keine Segmente besitzt
   */
    public Segment[] getSegments() {
        return null;
    }
}
