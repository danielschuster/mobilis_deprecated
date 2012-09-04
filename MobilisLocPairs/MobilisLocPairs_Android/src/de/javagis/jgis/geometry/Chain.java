/**
 * Chain.java
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
 * Chain 
 * 
 * Funktion: Geometrie-Darstellung einer Kette, d.h. einer geschlossenen Linie.
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class Chain extends Line {
    
     /**
     * Konstruktor: Eine Chain stellt eine geschlossene Linie aus mind. 3 Punkten dar.
     * Wirft Fehlermeldung, falls Punkte < 3
     * @param points St�tzpunkte. Ist die Liste der Punkte nicht geschlossen,
     * wird sie geschlossen
     * @throws Exception im Fehlerfall
     */
    public Chain(Point[] p) throws Exception {
        super(p);

        if (p.length < 3) {
            throw new Exception("St�tzpunkte < 3 !");
        }

        // Schliesse Kette, falls Start- und Endpunkt unterschiedlich
       Point ap = getStartPoint();
       Point ep = getEndPoint();
       if (!ap.equals(ep)) {
           addSegment(ep, ap);
       }
    }
    
    Chain(double[][] xy) {
           super(xy);
       }
}
