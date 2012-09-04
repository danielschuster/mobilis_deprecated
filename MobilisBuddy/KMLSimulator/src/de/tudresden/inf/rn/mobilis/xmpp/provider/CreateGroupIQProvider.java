package de.tudresden.inf.rn.mobilis.xmpp.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.CreateGroupIQ;

public class CreateGroupIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		CreateGroupIQ cgi = new CreateGroupIQ();
		cgi.setType(Type.RESULT);
		
		return cgi;
	}

}
