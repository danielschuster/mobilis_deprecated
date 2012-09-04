package de.tudresden.inf.rn.mobilis.android.services;

import org.jivesoftware.smack.XMPPConnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.android.util.Util;

public abstract class AbstractAuthService {

    protected boolean authenticated;
	protected XMPPConnection connection;
	/**
	 * Time value in milliseconds waiting for a packet.
	 */
	protected long timeout;
	protected String connectedUserId;
	private BroadcastReceiver ir;
	
	public void initIntentReceiver() {
	    ir = new IntentReceiver();
	    Context context = SessionService.getInstance().getContext();
	    context.registerReceiver(ir, new IntentFilter(Const.INTENT_PREFIX + "login"));
	    context.registerReceiver(ir, new IntentFilter(Const.INTENT_PREFIX + "logout"));
	}

	public void unregisterIntentReceiver() {
	    SessionService.getInstance().getContext().unregisterReceiver(ir);
	}
	
	private class IntentReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        
	        String threadName = "Thread";
	        boolean startProcessing = false;
	        if (intent.getAction().equals(Const.INTENT_PREFIX + "login")) {
	            threadName = "LoginThread";
	            startProcessing = true;
	        } else if (intent.getAction().equals(Const.INTENT_PREFIX + "logout")) {
	            threadName = "LogoutThread";
	            startProcessing = true;
	        }

	        if (startProcessing) {
	            // construct a new thread for background login process
	            final Intent i = intent;
	            final AbstractAuthService caller = AbstractAuthService.this;
	            final Context ctx = context;
	            Thread t = new Thread(new Runnable() {
	                public void run() {

	                    boolean success = false;
	                    String callbackString = "";
	                    if (i.getAction().equals(Const.INTENT_PREFIX + "login")
	                            && i.getStringExtra(Const.INTENT_PREFIX + "login.network")
	                            .equals(caller.getNetworkName())) {
	                        success = caller.login(i.getExtras());
	                        callbackString = "callback.login";
	                    } else if (i.getAction().equals(Const.INTENT_PREFIX + "logout")
	                            && i.getStringExtra(Const.INTENT_PREFIX + "logout.network")
	                            .equals(caller.getNetworkName())) {
	                        success = caller.logout(i.getExtras());
	                        callbackString = "callback.logout";
	                    }
	                    
	                    // send back the intent to the LoginLoop (GUI)
	                    boolean singleLogin = i.getExtras().getBoolean(Const.INTENT_PREFIX + "login.single");
	                    Intent intent = new Intent(Const.INTENT_PREFIX + callbackString);
	                    intent.putExtra(Const.INTENT_PREFIX + callbackString + ".network", 
	                            caller.getNetworkName());
	                    intent.putExtra(Const.INTENT_PREFIX + callbackString + ".success", 
	                            success);
	                    if (callbackString.equals("callback.login")) {
	                        intent.putExtra(Const.INTENT_PREFIX + "callback.login.single", singleLogin);
	                    }
	                    ctx.sendOrderedBroadcast(intent, null);
	                }
	            });
	            t.setName(threadName);
	            t.start();
	        }

	    }
	}
	
	public final boolean isAuthenticated() {
		return this.authenticated;
	}
	
	synchronized public final boolean login(Bundle extras) {
		if (this.authenticated)
			if (!this.logout(null))
				return false;
		this.authenticated = this.performLogin(extras);
		if (authenticated) {
		    connectedUserId = getConnectedUserId();
		}
		return this.authenticated;
	}
	
	public final boolean logout(Bundle extras) {
		if (this.performLogout(extras)) {
			this.authenticated = false;
			return true;
		} else 
			return false;
	}
	
	public void initialize(XMPPConnection connection) {
		this.connection = connection;
		this.timeout = SessionService.getInstance().getPreferences().getLong(
		        "pref_xmpp_timeout", 10000);
	}
		
	protected XMPPConnection getConnection() {
		return this.connection;
	}
	
    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
	
    public String getConnectedUserId() {
        if (isAuthenticated()) {
            return connectedUserId;
        } else {
            return "";
        }
    }
    
    @Override
    public String toString() {
        return Util.getDisplayedNetworkName(getNetworkName()) + " - " + getConnectedUserId();
    }
        
	public abstract boolean performLogin(Bundle extras);
	public abstract boolean performLogout(Bundle extras);
	public abstract String getNetworkName();

}
