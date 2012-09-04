package de.tudresden.inf.rn.mobilis.xmpp.packet;

import org.jivesoftware.smack.packet.IQ;

/**
 *
 * @author Istvan
 */
public class JoinGroupIQ extends IQ {
    
    public static final String elementName = "query";
    public static final String namespace = "mobilis:iq:joingroup";
    private String mGroup;
    
    public String getGroup() {
        return mGroup;
    }

    public void setGroup(String group) {
        mGroup = group;
    }
    
    public JoinGroupIQ() {
        super();
        this.setType(IQ.Type.GET);
    }
    
    @Override
	public String getChildElementXML() {
        return "<" + elementName + " xmlns=\"" + namespace + "\"></" + elementName + ">";
    }
}
