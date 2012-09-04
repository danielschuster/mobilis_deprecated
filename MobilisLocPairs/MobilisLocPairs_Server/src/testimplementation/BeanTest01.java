package testimplementation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.GoThereBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.StartGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.StartRoundBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;
public class BeanTest01 {

	private Calendar testzeit = Calendar.getInstance();
	private static Collection<String> barcodes = new ArrayList<String>();
	private static Collection<String> imageURLs = new ArrayList<String>();

	private static Map<String, GeoPosition> barcodePositions = new HashMap<String, GeoPosition>();
	private static Map<String, String> pairs = new HashMap<String, String>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		barcodes.add("inf01");
		barcodes.add("inf02");
		barcodes.add("inf03");
		barcodes.add("inf04");
		barcodes.add("inf06");
		barcodes.add("inf07");
		barcodes.add("inf08");
		for(String s : barcodes){
			 barcodePositions.put(s, new GeoPosition(0, 0, 0));
		}
		imageURLs.add("http://141.30.203.90/locpairs/pictures/img01");
		imageURLs.add("http://141.30.203.90/locpairs/pictures/img02");
		imageURLs.add("http://141.30.203.90/locpairs/pictures/img03");
		imageURLs.add("http://141.30.203.90/locpairs/pictures/img04");
		pairs.put("inf01", "http://141.30.203.90/locpairs/pictures/img01");
		pairs.put("inf02", "http://141.30.203.90/locpairs/pictures/img01");
		pairs.put("inf03", "http://141.30.203.90/locpairs/pictures/img02");
		pairs.put("inf04", "http://141.30.203.90/locpairs/pictures/img02");
		pairs.put("inf05", "http://141.30.203.90/locpairs/pictures/img03");
		pairs.put("inf06", "http://141.30.203.90/locpairs/pictures/img03");
		pairs.put("inf07", "http://141.30.203.90/locpairs/pictures/img04");
		pairs.put("inf08", "http://141.30.203.90/locpairs/pictures/img04");
		
		
		JoinGameBean joinGameBean = new JoinGameBean(new GeoPosition(2, 3, 4), "horst", "playerName");
		System.out.println(joinGameBean.toXML());
		StartRoundBean testbean = new StartRoundBean(true, StartRoundBean.sdf.format(Calendar.getInstance().getTime()), 300, null, "hydf");
		System.out.println(testbean.toXML());
		StartGameBean startgameBean = new StartGameBean("gameID", barcodePositions, pairs);
		System.out.println(startgameBean.toXML());
	}
}
