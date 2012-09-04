/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: model
 */
package de.tud.server.model;


/**
 * Class: ItemAttribute.java
 * Function: attribute of an item
 *
 * @author Jan Scholze
 * @version 1.26
 */
public class ItemAttribute {

    private String attributeName;
    private String attributeValue;

    /**
     * constructor: create new attribute for an item
     *
     * @param String attrName - name of the item attribute
     * @param String attrValue - value of the item attribute
     */
    public ItemAttribute(String attrName, String attrValue){
        this.attributeName = attrName;
        this.attributeValue = attrValue;
    }

    /**
     * get name of the item attribute
     *
     * @return String - name of the item attribute
     */
    public String getAttributeName(){
        return this.attributeName;
    }

    /**
     * get value of the item attribute
     *
     * @return String - value of the item attribute
     */
    public String getAttributeValue(){
        return this.attributeValue;
    }

    /**
     * set name of the item attribute
     *
     * @param String attrName - name of the item attribute to set
     */
    public void setAttributeName(String attrName){
        this.attributeName = attrName;
    }

    /**
     * set value of the item attribute
     *
     * @param String attrValue - value of the item attribute to set
     */
    public void setAttributeValue(String attrValue){
        this.attributeValue = attrValue;
    }

}
