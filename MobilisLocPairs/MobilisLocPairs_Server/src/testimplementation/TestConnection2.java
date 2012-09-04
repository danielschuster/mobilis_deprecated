package testimplementation;

import java.sql.Date;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.PrivateDataManager;



import de.tudresden.inf.rn.mobilis.server.locpairs.model.NetworkFingerprintDAO;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.GoThereBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.KeepAliveBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.StartRoundBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.UncoverCardBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.NetworkFingerPrint;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanFilterAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;


/**
 * Ich verwende die Pidgin-Konsole um IQs, Precenses und Messages zu 
 * senden. Außer den IQs lösen alle die processPacket() Methode des 
 * PacketListeners aus.
 * @author Reik Mueller
 **/

public class TestConnection2 {
	
	public static XMPPConnection connection;
	public static void main(String[] args) {
		XMPPConnection.DEBUG_ENABLED = true;
		
		// Der TimerTask ist dazu da die Anwendung am laufen zu halten
		// ohne wird sie einfach beendet
		// sollte nicht eigentlich der Einsatz eines Listeners dafür
		// sorgen, dass die Anwendung von allein weiter läuft?
		
/*		Timer t = new Timer();
		TimerTask tt = new TimerTask() {
			
			@Override
			public void run() {
				System.out.println("Hallo!");
				
			}
		};
		t.schedule(tt, 5);
*/		
		// Verbinden mit dem JABBER-Server + Anmeldung
		XMPPConnection connection = new XMPPConnection("127.0.0.1");
		try {
			connection.connect();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		try {
			connection.login("server", "7Dj3S", "Smack");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		connection.DEBUG_ENABLED = true;
		// Den PacketListener anlegen
		// Alle gesendeten Nachrichten (IQs, Messages, Presences) sind 
		// SubClasses von Packet und müssen also processPacket() Methode 
		// durchlaufen
		
		XMPPBean gotThereBeanPrototype = new GoThereBean();
		XMPPBean joinGameBeanPrototype = new JoinGameBean();
		XMPPBean startRoundBeanPrototype = new StartRoundBean();
		XMPPBean uncoverCardBeanPrototype = new UncoverCardBean();
		XMPPBean keepAliveBeanPrototype = new KeepAliveBean();
 
		(new BeanProviderAdapter(gotThereBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(joinGameBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(startRoundBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(uncoverCardBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(keepAliveBeanPrototype)).addToProviderManager();
		
		
		PacketListener myListener = new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				NetworkFingerprintDAO dao = new NetworkFingerprintDAO();
				System.out.println("Packet empfangen: " + packet.toString());
				try{
				if(packet instanceof BeanIQAdapter){
					XMPPBean b = ((BeanIQAdapter)packet).getBean();
					
					if(b instanceof GoThereBean){
						GoThereBean bean = (GoThereBean)b;
						if(bean.getType() == XMPPBean.TYPE_SET){
							System.out.println("GoThereBean vom typ SET empfangen");
							// TODO
						}else if (bean.getType() == XMPPBean.TYPE_RESULT){
							System.out.println("GoThereBean vom typ GET empfangen");
							// TODO
						}else if (bean.getType() == XMPPBean.TYPE_ERROR){
							// TODO
						}
					}else if(b instanceof StartRoundBean){
						StartRoundBean bean = (StartRoundBean)b;
						if(bean.getType() == XMPPBean.TYPE_SET){
							System.out.println("StartRoundBean vom typ SET empfangen");
							// TODO
						}else if (bean.getType() == XMPPBean.TYPE_RESULT){
							System.out.println("StartRoundBean vom typ RESULT empfangen");
							// TODO
						}else if (bean.getType() == XMPPBean.TYPE_ERROR){
							System.out.println("StartRoundBean vom typ ERROR empfangen");
							// TODO
						}
					}else if(b instanceof UncoverCardBean){
						UncoverCardBean bean = (UncoverCardBean)b;
						System.out.println("UncoverCardBean empfangen");
						if(bean.getType() == XMPPBean.TYPE_SET){
							System.out.println("UncoverCardBean vom typ SET empfangen");
							NetworkFingerPrint fp = bean.getNetworkFingerPrint();
							dao.addFingerprint(fp);
							dao.close();
						}else if (bean.getType() == XMPPBean.TYPE_RESULT){
							System.out.println("UncoverCardBean vom typ GET empfangen");
							// TODO
						}else if (bean.getType() == XMPPBean.TYPE_ERROR){
							System.out.println("UncoverCardBean vom typ ERROR empfangen");
						}
					}else if(b instanceof JoinGameBean){
						JoinGameBean bean = (JoinGameBean)b;
						if(bean.getType() == XMPPBean.TYPE_SET){
							System.out.println("JoinGameBean vom typ SET empfangen");
							System.out.println(bean.toXML());
						}else if (bean.getType() == XMPPBean.TYPE_RESULT){
							System.out.println("JoinGameBean vom typ GET empfangen");
							// TODO
						}else if (bean.getType() == XMPPBean.TYPE_ERROR){
							// TODO
						}
					}else if(b instanceof KeepAliveBean){
					KeepAliveBean bean = (KeepAliveBean)b;
					if(bean.getType() == XMPPBean.TYPE_SET){
						System.out.println("KAB vom typ SET empfangen");
						System.out.println(bean.toXML());
					}else if (bean.getType() == XMPPBean.TYPE_RESULT){
						System.out.println("KAB vom typ GET empfangen");
						// TODO
					}else if (bean.getType() == XMPPBean.TYPE_ERROR){
						// TODO
					}
					}
				}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		
		connection.addPacketListener(myListener, 	
				new OrFilter(	new BeanFilterAdapter(gotThereBeanPrototype),
				new OrFilter(	new BeanFilterAdapter(uncoverCardBeanPrototype),
				new OrFilter(	new BeanFilterAdapter(startRoundBeanPrototype),
								new BeanFilterAdapter(joinGameBeanPrototype)))));
		
//		connection.addPacketListener(myListener, new BeanFilterAdapter(uncoverCardBeanPrototype));  
		
		try{ java.util.Date date = Calendar.getInstance().getTime();
		NetworkFingerPrint fp = new NetworkFingerPrint();
		fp.setPosition(new GeoPosition(121, 231, 4));
		Map<String, Integer> fps = new HashMap<String, Integer>();
		fps.put("id01", -10);
		fps.put("id02", -20);
		fps.put("id03", -30);
		fps.put("id04", -40);
		fp.setNetworkFingerPrint(fps);
	    UncoverCardBean ucb = new UncoverCardBean("awe", fp, StartRoundBean.sdf.format(date), "hugo");
	    ucb.setTo("alpha@141.30.203.90/MXA");
	    ucb.setTo("server@141.30.203.90/Smack");
//	    ucb.setTo("reik@141.30.203.90/Client");
	    ucb.setFrom(connection.getUser());
//	    testbean1.setType(XMPPBean.TYPE_SET);
	    ucb.setType(XMPPBean.TYPE_SET);
	    System.out.println("HAARHH " + ucb.toXML());
	    connection.sendPacket(new BeanIQAdapter(ucb));
	   }catch(Exception e){
		   e.printStackTrace();
	   }
		
		JoinGameBean resultBean = new JoinGameBean();
		resultBean.setTo("reik@141.30.203.90/Client");
	    resultBean.setFrom("Server");
	    resultBean.setType(XMPPBean.TYPE_RESULT);
//	    connection.sendPacket(new BeanIQAdapter(resultBean));
	    System.out.println("JoinGameBean vom typ RESULT an " + "alpha@141.30.203.90/MXA" + " gesendet");
	    
		
	    System.out.println("Senden der Testbeans:");
	    System.out.println("StartRoundBean gesendet");
	    Map<Integer, Long> scores = new HashMap<Integer, Long>();
	    scores.put(new Integer(1), new Long(1));
	    StartRoundBean testbean1 = new StartRoundBean(true, StartRoundBean.sdf.format(Calendar.getInstance().getTime()), 3000, scores, "yadsjfa");
	    //StartRoundBean testbean = new StartRoundBean(true, StartRoundBean.sdf.format(Calendar.getInstance().getTime()), 300, null);
	    testbean1.setTo("alpha@141.30.203.90/MXA");
	    testbean1.setTo("server@141.30.203.90/Smack");
	    testbean1.setFrom(connection.getUser());
	    testbean1.setType(XMPPBean.TYPE_SET);
	    System.out.println("StartRoundBean: "+testbean1.toXML());
	    System.out.println("ID: " + testbean1.getId());
	    connection.sendPacket(new BeanIQAdapter(testbean1));
//	    connection.sendPacket(new BeanIQAdapter(testbean1));
	    
	    KeepAliveBean kab = new KeepAliveBean("ssdfad", new GeoPosition(23, 234, 5));
	    kab.setTo("server@141.30.203.90/Smack");
	    kab.setType(XMPPBean.TYPE_SET);
	    connection.sendPacket(new BeanIQAdapter(kab));
	    
	    java.util.Date date = Calendar.getInstance().getTime();
	    NetworkFingerPrint nfp = new NetworkFingerPrint();
	    nfp.addFingerPrint("asfd", 123);
	    UncoverCardBean ucb = new UncoverCardBean("awe", nfp, StartRoundBean.sdf.format(date), "hugo");
	    ucb.setTo("alpha@141.30.203.90/MXA");
	    ucb.setTo("server@141.30.203.90/Smack");
	    ucb.setFrom(connection.getUser());
//	    testbean1.setType(XMPPBean.TYPE_SET);
	    ucb.setType(XMPPBean.TYPE_SET);
	    System.out.println(ucb.toXML());
//	    connection.sendPacket(new BeanIQAdapter(ucb));
	    
/*	    GoThereBean testbean2 = new GoThereBean("nerdigkeitinperson", 1231, 3453);
	    testbean2.setTo("alpha@141.30.203.90/MXA");
	    testbean2.setFrom("server@141.30.203.90/Smack");
	    testbean2.setType(XMPPBean.TYPE_SET);
	    System.out.println("GoThereBean gesendet");
//	    connection.sendPacket(new BeanIQAdapter(testbean2));
*/
/*	    
	    Map<String, Integer> players= new HashMap<String, Integer>();
	    players.put("testnerd@testserverSmack", 1);
	    players.put("guru@testserverSmack", 1);
	    players.put("heinDoof@testserverSmack", 2);
	    players.put("joerg@testserverSmack", 2);
	    Map<String, Boolean> states= new HashMap<String, Boolean>();
	    states.put("testnerd@testserverSmack", true);
	    states.put("guru@testserverSmack", true);
	    states.put("heinDoof@testserverSmack", false);
	    states.put("joerg@testserverSmack", true);
	    Map<String, String> names= new HashMap<String, String>();
	    names.put("testnerd@testserverSmack", "argon");
	    names.put("guru@testserverSmack", "xenon");
	    names.put("heinDoof@testserverSmack", "helium");
	    names.put("joerg@testserverSmack", "radon");
	    
	    JoinGameBean testbean3 = new JoinGameBean(new GeoPosition(12, 12, 12),"mafia", players, names, states);
	    testbean3.setTo("alpha@141.30.203.90/MXA");
	    testbean3.setFrom("server@141.30.203.90");
	    testbean3.setType(XMPPBean.TYPE_RESULT);
	    connection.sendPacket(new BeanIQAdapter(testbean3));
	    System.out.println("JoinGameBean gesendet");
 */
 
	}	
}
