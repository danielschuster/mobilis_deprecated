package de.tudresden.inf.rn.mobilis.android.services;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.XMPPConnection;

import de.tudresden.inf.rn.mobilis.android.util.DBHelper;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Credential;

public class SocialNetworkManagementService {

	private List<AbstractAuthService> authServices;
	private BuddyListService buddyListService;
	
	public SocialNetworkManagementService() {
		this.authServices = new ArrayList<AbstractAuthService>(2);
		this.authServices.add(new MobilisAuthService());
		this.authServices.add(new FacebookAuthService());
		this.buddyListService = new BuddyListService();
	}
	
	public void initialize(XMPPConnection connection) {
		for (AbstractAuthService aas: this.authServices) {
			aas.initialize(connection);
		}
		buddyListService.initialize(connection);
	}
	
    public void initIntentReceivers() {
        for (AbstractAuthService aas: this.authServices) {
            aas.initIntentReceiver();
        }
    }
    
    public void unregisterIntentReceivers() {
        for (AbstractAuthService aas: this.authServices) {
            aas.unregisterIntentReceiver();
        }
    }
	
	public boolean isAnyAuthenticated() {
		for (AbstractAuthService aas: authServices)
			if (aas.isAuthenticated()) return true;
		return false;
	}
	
	public boolean isAuthenticated(String networkName) {
	    AbstractAuthService authService = getAuthService(networkName);
	    if ((authService != null) && (authService.isAuthenticated())) return true;
	    else return false;
	}
	
	public AbstractAuthService getAuthService(String networkName) {
	    for (AbstractAuthService aas: authServices) {
	        if (aas.getNetworkName().equals(networkName)) return aas;
	    }
	    return null;
	}

    public List<AbstractAuthService> getAuthServices() {
        return authServices;
    }
    
    public List<AbstractAuthService> getServices(boolean isAuthenticated) {
        List<AbstractAuthService> services = new ArrayList<AbstractAuthService>();
        for (AbstractAuthService aas: authServices) {
            if (aas.isAuthenticated() == isAuthenticated) services.add(aas);
        }
        return services;
    }
    
    public BuddyListService getBuddyListService() {
        return buddyListService;
    }

    public List<AbstractAuthService> getNotAuthenticatedServices() {
        return getServices(false);
    }
    
    public List<AbstractAuthService> getAuthenticatedServices() {
        return getServices(true);
    }
    
}
