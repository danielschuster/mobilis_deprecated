package test;

import java.util.HashMap;
import java.util.Map;

import de.tudresden.inf.rn.mobilis.server.locpairs.model.NetworkFingerprintDAO;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.NetworkFingerPrint;

public class NetworkFingerprintDAOTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NetworkFingerprintDAO dao = new NetworkFingerprintDAO();
		NetworkFingerPrint fp = new NetworkFingerPrint();
		fp.setPosition(new GeoPosition(121, 231, 4));
		Map<String, Integer> fps = new HashMap<String, Integer>();
		fps.put("id01", -10);
		fps.put("id02", -20);
		fps.put("id03", -30);
		fps.put("id04", -40);
		fp.setNetworkFingerPrint(fps);
		dao.addFingerprint(fp);
		dao.close();
	}

}
