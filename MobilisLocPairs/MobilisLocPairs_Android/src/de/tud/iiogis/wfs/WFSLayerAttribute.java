/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: wfs
 */
package de.tud.iiogis.wfs;


/**
 * Class: WFSLayerAttribute.java
 * Function: object to represent a single layer attribute of a wfs layer
 *
 * @author Jan Scholze
 * @version 1.14
 */
public class WFSLayerAttribute {

    private String attributeName;
    private String attributeType;

    /**
     * constructor: create new layer of a wfs server
     *
     * @param String attrName - name of the layer attribute
     * @param String attrType - type of the layer attribute (string/number)
     */
    public WFSLayerAttribute(String attrName, String attrType){
        attributeName = attrName;
        attributeType = attrType;
    }

    /**
     * get name of the layer attribute
     *
     * @return String - name of the layer attribute
     */
    public String getAttributeName(){
        return attributeName;
    }

    /**
     * get type of the layer attribute
     *
     * @param String - type of the layer attribute
     */
    public String getAttributeType(){
        return attributeType;
    }
    
}
