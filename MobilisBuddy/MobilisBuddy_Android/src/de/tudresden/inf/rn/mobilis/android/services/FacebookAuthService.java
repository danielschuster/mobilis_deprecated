package de.tudresden.inf.rn.mobilis.android.services;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import android.os.Bundle;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.xmpp.packet.NetworkIQ;
import edu.bu.CS893.FacebookMockLoginBrowser;

public class FacebookAuthService extends AbstractAuthService {
    
    private static final String TAG = "FacebookAuthService";
    
	@Override
	public boolean performLogin(Bundle b) {
		String email     = b.getString(Const.INTENT_PREFIX + "login.userid");
		String password  = b.getString(Const.INTENT_PREFIX + "login.password"); 
		boolean success  = false;
		// get FB AuthToken
		NetworkIQ fbatIQ = this.getFacebookAuthToken();
		if (fbatIQ != null) {
		    String fbApiKey    = fbatIQ.getParams().get("apikey");
		    String fbAuthToken = fbatIQ.getParams().get("authtoken");
		    // login
		    Log.i(TAG, "Logging in to Facebook ...");
		    Log.i(TAG, "... with ApiKey: " + fbApiKey);
		    Log.i(TAG, "... with AuthToken: " + fbAuthToken);
		    success = FacebookMockLoginBrowser.login(fbApiKey, fbAuthToken, email, password);
		    if (success) {
		    	// send that we're logged in
			    NetworkIQ fbliIQ = this.sendFacebookLoggedIn();
			    if (fbliIQ != null) {
			        success = (fbliIQ.getType() == IQ.Type.RESULT);
			        if (success) connectedUserId = email;
			    } else {
			    	success = false;
			    }
		    }
		} else success = false;
        return success;
	}
		
	protected NetworkIQ getFacebookAuthToken() {
		XMPPConnection c = this.getConnection();
		// request facebook auth token IQ from AndroidBuddy agent
		NetworkIQ authTokenGetIQ = new NetworkIQ();
		authTokenGetIQ.setType(IQ.Type.GET);
		authTokenGetIQ.setFrom(c.getUser());
		authTokenGetIQ.setTo(SessionService.getInstance().getAndroidbuddyAgent());
		authTokenGetIQ.setNetwork(this.getNetworkName());
		authTokenGetIQ.setAction("getauthtoken");
		// create PacketCollector for accepting the response 
		PacketCollector authTokenResultIQCollector = c.createPacketCollector(new PacketFilter() {
				public boolean accept(Packet p) {
					return (p instanceof NetworkIQ)
					   && ((IQ)p).getType().equals(IQ.Type.RESULT)
					   && ((NetworkIQ)p).getNetwork().equals(getNetworkName())
					   && ((NetworkIQ)p).getAction().equals("getauthtoken");
				}
			}
		);
		// wait for response
		c.sendPacket(authTokenGetIQ);
		Log.d(TAG, "Sent packet: NetworkIQ (GET FacebookAuthToken)");
		NetworkIQ authTokenResult = (NetworkIQ) authTokenResultIQCollector.nextResult(timeout);
	    if (authTokenResult == null) {
	        Log.w(TAG, "Timeout waiting for packet: NetworkIQ (RESULT FacebookAuthToken)");
	    } else {
	        Log.d(TAG, "Received packet: NetworkIQ (RESULT FacebookAuthToken)");
	    }
		// tell PacketCollector that we're done here.
		authTokenResultIQCollector.cancel();
		return authTokenResult;
	}
	
	protected NetworkIQ sendFacebookLoggedIn() {
		XMPPConnection c = this.getConnection();
		// send facebook logged in notification to AndroidBuddy agent
		NetworkIQ loggedInSetIQ = new NetworkIQ();
		loggedInSetIQ.setType(IQ.Type.SET);
		loggedInSetIQ.setFrom(c.getUser());
		loggedInSetIQ.setTo(SessionService.getInstance().getAndroidbuddyAgent());
		loggedInSetIQ.setNetwork(this.getNetworkName());
		loggedInSetIQ.setAction("login");
		c.sendPacket(loggedInSetIQ);
		Log.d(TAG, "Sent packet: NetworkIQ (SET FacebookLogin)");
		// create PacketCollector for accepting the response 
		PacketCollector loggedInResultIQCollector = c.createPacketCollector(
			new PacketFilter() {
				public boolean accept(Packet p) {
					return (p instanceof NetworkIQ)
						   && ((IQ)p).getType().equals(IQ.Type.RESULT)
						   && ((NetworkIQ)p).getNetwork().equals(getNetworkName())
						   && ((NetworkIQ)p).getAction().equals("login");
				}
			}
		);
				
		// wait for response
		NetworkIQ loggedInResultIQ = (NetworkIQ) loggedInResultIQCollector.nextResult(/*timeout*/);
		if (loggedInResultIQ == null) {
		    Log.w(TAG, "Timeout waiting for packet: NetworkIQ (RESULT FacebookLogin)");
		} else {
		    Log.d(TAG, "Received packet: NetworkIQ (RESULT FacebookLogin)");
		}
		// tell PacketCollector that we're done here.
		loggedInResultIQCollector.cancel();
		return loggedInResultIQ;
	}

	@Override
	public String getNetworkName() {
		return Const.FACEBOOK;
	}

	@Override
	public boolean performLogout(Bundle extras) {
		// does nothing. don't worry about it!
	    Log.i(TAG, "Logging out of Facebook");
		return true;
	}
		
}
