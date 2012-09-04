/**
 * GeoObject.java
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
import de.tud.server.model.Shape;


/**
 * 
 * GeoObject
 * 
 * Funktion: Basisobjekt eines geometrischen Objekts im JGIS
 * 
 * @author Bjoern Koos, Michael Herter
 */
public abstract class GeoObject extends Shape {
  /**
   * Ausdehnung des Objekts
   */
    private Extent extent = null;
  /**
   * ID des Objekts
   */
    private String id = null;
    
    /**
     * Liefert den Extent des GeoObjekts
     * @param
     * @return Extent
     */
    public Extent getExtent() {
        return extent;
    }
    
    /**
     * Liefert die Id des GeoObjekts
     * @param
     * @return String
     */
    public String getId() {
        return id;
    }
    
    /**
     * Setzt den Extent eines GeoObjekts
     * @param Extent
     * @return void
     */
    public void setExtent(Extent ext) {
        this.extent = ext;
    }
    /**
     * Setzt die ID eines GeoObjekts
     * @param Id (String)
     * @return void
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Aktualisiert den Extent des GeoObjekts
     * @param GeoObject
     * @return void
     */
    protected void updateExtent(GeoObject o) {
        if ((o == null) || (o.getExtent() == null)) {
            return;
        }

        if (getExtent() == null) {
            setExtent(o.getExtent().copy());
        } else {
            getExtent().mergeExtent(o.getExtent());
        }
    }
    
    /**
     * Liefert den Mittelpunkt des Extents (= Fl�chenschwerpunkt)
     * @param GeoObject
   * @return Point Mittelpunkt
     */
    public Point returnCenter() throws Exception {
        Point center = PointFactory.createPoint(0, 0);

        try {
            double xcenter = this.extent.getXmin() + (this.extent.getWidth() / 2);
            double ycenter = this.extent.getYmin() + (this.extent.getHeight() / 2);
            center = PointFactory.createPoint(xcenter, ycenter);
        } catch (Exception e) {
        }

        return center;
    }
    
    /**
     * Liefert das GeoObjekt als Punkt-Liste
     * @return Point[]
     */
    abstract public Point[] asPoints();
    
    /**
     * Liefert die Segmente eines GeoObjekts
     * @return Segment[]
     */
    abstract public Segment[] getSegments();
    
    /**
     * Liefert true, wenn das GeoObjekt in dem Parameter-Objekt (obj) liegt.
     * @param GeoObject
     * @return boolean
     */
    public boolean isWithin(GeoObject obj) throws Exception {
        return (GeometryHelper.isWithin(this, obj));
    }
   
    public boolean isWithin(Extent queryExt) throws Exception {
        return GeometryHelper.isWithin(queryExt, this);
    }
     
     /**
     * Liefert true, wenn das GeoObjekt mit dem Parameter-Objekt (o) schneidet.
     * @param GeoObject
     * @return boolean
     */
    abstract public boolean intersects(GeoObject o) throws Exception;
    
     /**
     * Liefert true, wenn das GeoObjekt mit dem Parameter-Objekt (o) schneidet.
     * @param GeoObject
     * @return boolean
     */
   // abstract public boolean intersects(Extent ext);
    
    /**
     * Liefert true, wenn sich das GeoObjekt mit dem Parameter-Objekt (o) r�umlich �berlagert.
     * @param GeoObject
     * @return boolean
     */
    public boolean overlaps(GeoObject o) throws Exception {
    	if (o == null) {
    		return false;
    	}
    	
    	Extent oExt = o.getExtent();
    	
        boolean ov = this.intersects(o);
        
        if (!ov) {
            if (oExt.getHeight()+oExt.getWidth() > this.extent.getHeight()+this.extent.getWidth()) {
                ov = this.isWithin(o);
                if (!ov) {
                    ov = o.isWithin(this);
                }
            } else {
                ov = o.isWithin(this);
                if (!ov) {
                    ov = this.isWithin(o);
                }
            }
        }
        return ov;
    }
    
     /**
     * Liefert true, wenn sich das GeoObjekt mit dem Extent (o) r�umlich �berlagert.
     * @param GeoObject
     * @return boolean
     */
    public boolean overlaps(Extent oExt) throws Exception {
    	if (oExt == null) {
    		return false;
    	}
    	
        GeoObject queryPoly = GeometryHelper.Extent2Polygon(oExt);
        boolean ov = this.intersects(queryPoly);
        
        if (!ov) {
            if (oExt.getHeight()+oExt.getWidth() > this.extent.getHeight()+this.extent.getWidth()) {
                ov = this.isWithin(oExt);
                if (!ov) {
                    ov = queryPoly.isWithin(this);
                }
            } else {
                ov = queryPoly.isWithin(this);
                if (!ov) {
                    ov = this.isWithin(oExt);
                }
            }
        }
        return ov;
    }
    /**
     * Liefert true, wenn der Extent des GeoObjekts = Null ist
     * @param GeoObject
     * @return
     */
    public boolean isNull() {
        if (getExtent() == null) {
            return true;
        } else {
            return false;
        }
    }
}
