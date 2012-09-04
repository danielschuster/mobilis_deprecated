/**
 * Ring.java
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
import de.javagis.jgis.util.Logger;


/**
 * 
 * Ring
 * 
 * Funktion: Ring-Objekt. Darstellung einer geschlossenen Linie (Chain) ohne
 * �berschneidungen der Segmente
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class Ring extends Chain {
  /**
   * Erzeugt Ring aus Punktliste. Ring wird auf selfIntersecting gepr�ft
   * @param points St�tzpunkte
   * @throws Exception im Fehlerfall
   */
    public Ring(Point[] p) throws Exception {
        this(p,true);
    }
    
    Ring(double[][] xy) {
        super(xy);
    }
    
    public Ring(Point[] p, boolean isClean) throws Exception {
        super(p);
        
        if (!isClean && this.isSelfIntersecting()) {
            Logger.log("FEHLER IN RING...", Logger.ERROR);
            
            for (int i=0; i<p.length; i++) {
                Logger.log("P"+i+" = "+p[i].getX()+" , "+p[i].getY(), Logger.ERROR );
            }
            
            throw new Exception("Ring is selfintersecting");
        }
    }

  /**
   * Liefert den Drehsinn des Rings
   * @return int Konstante f�r Drehrichtung
   */
    public int getDrehsinn() {
        return GeometryHelper.getDrehsinn(this.asPoints());
    }
}
