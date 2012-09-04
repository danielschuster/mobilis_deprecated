package de.tudresden.inf.rn.mobilis.android.services;

import java.util.HashMap;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;

import se.su.it.smack.pubsub.PubSub;
import se.su.it.smack.pubsub.elements.CreateElement;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.xmpp.beans.FeedEntry;
import de.tudresden.inf.rn.mobilis.xmpp.packet.PubSubPacketExtension;
import de.tudresden.inf.rn.mobilis.xmpp.provider.PubSubPacketExtensionProvider;
import de.tudresden.inf.rn.mobilis.xmpp.pubsub.elements.ConfigureElement;

public class PlacesManagementService implements PacketListener {

    private static final String TAG = "PlacesManagementService";
	private HashMap<String, String> mPubSubNodes;
	private XMPPConnection mConnection;
	private BroadcastReceiver ir;

	public void initIntentReceiver() {
	    ir = new IntentReceiver();
	    Context context = SessionService.getInstance().getContext();
	    context.registerReceiver(ir, new IntentFilter(Const.INTENT_PREFIX + "servicecall.createplace"));
	}

	public void unregisterIntentReceiver() {
	    SessionService.getInstance().getContext().unregisterReceiver(ir);
	}

	private class IntentReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (action
	                .equals(Const.INTENT_PREFIX + "servicecall.createplace")) {
	            Location loc = (Location) intent
	                    .getParcelableExtra(Const.INTENT_PREFIX + "servicecall.createplace.location");
	            String title = intent.getStringExtra(Const.INTENT_PREFIX + "servicecall.createplace.title");
	            String address = (String) intent.getStringExtra(Const.INTENT_PREFIX + "servicecall.createplace.address");
	            String notes = (String) intent.getStringExtra(Const.INTENT_PREFIX + "servicecall.createplace.notes");
	            publishPlace(loc, title, address, notes);
	        }
	    }
	}

	/**
	 * Creates a new FeedEntry object and hands it over to the BasicService.
	 * 
	 * @param loc
	 * @param title
	 * @param address
	 * @param notes
	 */
	public void publishPlace(Location loc, String title, String address,
			String notes) {
		FeedEntry fe = new FeedEntry(title, notes);
		fe.setGmlPoint(loc.getLatitude() + " " + loc.getLongitude());

		callPublishNewPlace(fe);
	}

	/**
	 * Constructs an XMPP packet and publishes the newly created place to a
	 * newly created PubSub node that has the group's pubsub node as parent
	 * element.
	 * 
	 * @param place
	 *            A FeedEntry object that contains the item to publish.
	 */
	private void callPublishNewPlace(FeedEntry place) {
		// TODO check if this position already exists in database
		PubSub pubSub = new PubSub();
		pubSub.setCustomNamespace("http://jabber.org/protocol/pubsub");
		pubSub.setFrom(mConnection.getUser());
		pubSub.setTo(SessionService.mPubSubServer);
		pubSub.setType(IQ.Type.SET);
		// Create element without node to let id be generated on server.
		CreateElement createParentElem = new CreateElement();
		pubSub.appendChild(createParentElem);
		// create configure element in order to set type to collection
		ConfigureElement confElem = new ConfigureElement("leaf");
		confElem.setParentNode(mPubSubNodes.get(SessionService.getInstance().getGroupManagement()
				.getGroupName()));
		pubSub.appendChild(confElem);

		// PubSubPacketExtension pspe = new PubSubPacketExtension();
		// pspe.setChild(confElem);
		// pspe.setCreateNode(true);
		//		
		// pubSub.addExtension(pspe);

		PacketFilter pf = new PacketExtensionFilter("pubsub",
				"http://jabber.org/protocol/pubsub");
		PacketCollector response = mConnection.createPacketCollector(pf);

		mConnection.sendPacket(pubSub);
//		Log.i(TAG, pubSub.toXML());
//
//		// wait for notification of successful node creation and get the newly
//		// generated id
//		Packet p = response.nextResult(SmackConfiguration
//				.getPacketReplyTimeout());
//		Log.i(TAG, p.toXML());
//		p = response.nextResult(SmackConfiguration.getPacketReplyTimeout());
//		Log.i(TAG, p.toXML());
//		PubSub ps = (PubSub) p;
//		response.cancel();
//		if (ps != null) {
//			CreateElement createElem = (CreateElement) ps.getChildren().get(0);
//			String newNode = createElem.getNode();
//			Log.i(TAG, newNode + " created");
//
//			// now publish item
//			PubSub pubSubItem = new PubSub();
//			pubSub.setCustomNamespace("http://jabber.org/protocol/pubsub");
//			pubSub.setFrom(mConnection.getUser());
//			pubSub.setTo(SessionService.mPubSubServer);
//			pubSub.setType(IQ.Type.SET);
//			PublishElement pe = new PublishElement(newNode);
//			ItemElement ie = new ItemElement(null, place.toXml());
//			pe.addChild(ie);
//			pubSubItem.appendChild(pe);
//
//			mConnection.sendPacket(pubSubItem);
//		}
	}

	/**
	 * Handles affiliation message packets and adds the node to the node
	 * management list.
	 */
	@Override
	public void processPacket(Packet packet) {
		if (packet.getExtension(PubSubPacketExtension.elementName,
				PubSubPacketExtension.namespace) != null) {
			PubSubPacketExtension pspe = (PubSubPacketExtension) packet
					.getExtension(PubSubPacketExtension.elementName,
							PubSubPacketExtension.namespace);
			String saJid = StringUtils.parseBareAddress(packet.getFrom());
			String node = pspe.getNode();
			String groupName = SessionService.getInstance()
					.getGroupManagement().getGroupOfAgent(saJid);
			mPubSubNodes.put(groupName, node);
		}
	}

	public void initialize(XMPPConnection connection) {
		mPubSubNodes = new HashMap<String, String>();
		
		// provide custom provider
		ProviderManager pm = ProviderManager.getInstance();
		PubSubPacketExtensionProvider pspep = new PubSubPacketExtensionProvider();
		// BUG in Smack that let's me get "jabber:client" namespace instead of
		// the right one.
		pm.addExtensionProvider("pubsub", "jabber:client", pspep);
		pm.addExtensionProvider("pubsub", "http://jabber.org/protocol/pubsub",
				pspep);
		
		// provide custom listeners
		mConnection = connection;
//		PacketExtensionFilter pef = new PacketExtensionFilter(
//				PubSubPacketExtension.elementName,
//				PubSubPacketExtension.namespace);
//		connection.addPacketListener(this, pef);
	}
}
