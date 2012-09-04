/**
 * MultiGeoObject.java
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
 * MultiGeoObject
 * 
 * Funktion: Basisklasse eines Multiobjektes, d.h. eines Objektes, das mehrere
 * Objekte gleichen Typs aufnehmen kann. Der Typ der Teilobjekte wird durch die
 * jeweilige konkrete Unterklasse festgelegt.
 * Die Eigenschaften des MultiGeoObjects werden i.A. durch die
 * Eigenschaften seiner Bestandteile bestimmt, z.B. bei Geometrie-Operationen.  
 * 
 * @author Bjoern Koos, Michael Herter
 */
public abstract class MultiGeoObject extends GeoObject {

  /** Creates a new instance of MultiGeoObject */
  public MultiGeoObject() {
  }


  /**
   * F�gt Punktliste zu dem Multi-GeoObjekt hinzu
   * @param points St�tzpunkte eines neuen Objekts 
   * @throws Exception im Fehlerfall
   */
  public abstract void addPoints(Point[] points) throws Exception;

  /**
   * Liefert das GeoObjekt als Punkt-Liste
   * @return Array aller St�tzpunkte des Multiobjekts
   */
  public abstract Point[] asPoints();

  /**
   * Liefert alle Segmente des MultiGeoObjekts
   * @return Segmente aller Objekte
   */
  public abstract Segment[] getSegments();


  public abstract GeoObject[] getElements();
}