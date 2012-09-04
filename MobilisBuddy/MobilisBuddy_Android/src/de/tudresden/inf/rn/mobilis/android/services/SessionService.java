package de.tudresden.inf.rn.mobilis.android.services;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.IBBProviders;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.app.Activity;
import android.app.Application;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import de.tudresden.inf.rn.mobilis.android.R;
import de.tudresden.inf.rn.mobilis.android.login.LoginLoop;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.xmpp.packet.BuddylistIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.LocationIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.NetworkIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.SettingsIQ;
import de.tudresden.inf.rn.mobilis.xmpp.provider.BuddylistIQProvider;
import de.tudresden.inf.rn.mobilis.xmpp.provider.LocationIQProvider;
import de.tudresden.inf.rn.mobilis.xmpp.provider.NetworkIQProvider;
import de.tudresden.inf.rn.mobilis.xmpp.provider.SettingsIQProvider;

public class SessionService {

	// own instance
	private static SessionService sessionService = new SessionService();

	// members
	private Context mContext;
	private XMPPConnection mConnection;
	private String mSessionCoordinator;
	private String mAndroidbuddyAgent;
	public static String mPubSubServer;
	private LocalActivityManager activityManager;
	private InfoViewer infoViewer;
	private SharedPreferences preferences;
	private boolean isDebugMode;

	// Mobilis services
	private GroupMemberService mMemberService;
	private MessageHandlerService mMessageService;
	private ContextManagementService mContextService;
	private PlacesManagementService mPlacesService;
	private GroupManagementService mGroupService;
	
	// Androidbuddy
	private SocialNetworkManagementService mSocialNetworkManagementService;
	private LoginLoop loginLoop;


	private SessionService() {
		mMemberService = new GroupMemberService();
		mMessageService = new MessageHandlerService();
		mContextService = new ContextManagementService();
		mPlacesService = new PlacesManagementService();
		mGroupService = new GroupManagementService();
		mSocialNetworkManagementService = new SocialNetworkManagementService();
		ProviderManager pm = ProviderManager.getInstance();
		pm.addIQProvider(LocationIQ.elementName, LocationIQ.namespace, new LocationIQProvider());
		pm.addIQProvider(NetworkIQ.elementName, NetworkIQ.namespace, new NetworkIQProvider());
		pm.addIQProvider(SettingsIQ.elementName, SettingsIQ.namespace, new SettingsIQProvider());
		pm.addIQProvider(BuddylistIQ.elementName, BuddylistIQ.namespace, new BuddylistIQProvider());
	}

	public void initializeIntentReceivers() {
	    mMemberService.initIntentReceiver();
	    mMessageService.initIntentReceiver();
	    mContextService.initIntentReceiver();
	    mPlacesService.initIntentReceiver();
	    mGroupService.initIntentReceiver();
	    mSocialNetworkManagementService.initIntentReceivers();
	    loginLoop.initIntentReceiver();
	}
	
	public void unregisterIntentReceivers() {
	    mMemberService.unregisterIntentReceiver();
	    mMessageService.unregisterIntentReceiver();
	    mContextService.unregisterIntentReceiver();
	    mPlacesService.unregisterIntentReceiver();
	    mGroupService.unregisterIntentReceiver();
	    mSocialNetworkManagementService.unregisterIntentReceivers();
	    loginLoop.unregisterIntentReceiver();
	}
	
    public void startIntentService() {
        // creating and starting an android service, which can be used for intents from every activity
        Application androidApp = activityManager.getCurrentActivity().getApplication();
        //androidApp.
    }
	
	/**
	 * Opens the shared preference file. If none is found, a new one will be created.
	 */
	public void initializePreferences() {
	    
	    // open preferences file
	    preferences = mContext.getSharedPreferences(Const.PREFERENCES, 0);
	    // check for first launch
	    boolean isFirstLaunch = preferences.getBoolean("firstLaunch", true);
	    isDebugMode = Boolean.parseBoolean(
	            mContext.getResources().getString(R.string.pref_debug_mode));
	    // fetch all prefs from the localconfig.xml (new preferences have to be added here manually)
	    SharedPreferences.Editor prefsEditor = preferences.edit();
	    if (isFirstLaunch || isDebugMode) {    
	        
	        prefsEditor.putString("pref_host", 
	                mContext.getResources().getString(
	                        R.string.pref_host_default));
	        prefsEditor.putString("pref_port", 
	                mContext.getResources().getString(
	                        R.string.pref_port_default));
	        prefsEditor.putString("pref_service", 
	                mContext.getResources().getString(
	                        R.string.pref_service_default));
	        prefsEditor.putString("pref_resource", 
	                mContext.getResources().getString(
	                        R.string.pref_resource_default));
	        prefsEditor.putString("pref_coordinator", 
	                mContext.getResources().getString(
	                        R.string.pref_coordinator_default));
	        prefsEditor.putString("pref_androidbuddyagent", 
	                mContext.getResources().getString(
	                        R.string.pref_androidbuddyagent_default));
	        
	        prefsEditor.putLong("pref_xmpp_timeout", 
                    Long.parseLong(mContext.getResources().getString(
                            R.string.pref_xmpp_timeout)));
	        prefsEditor.putBoolean("pref_networks_connectall", 
	                Boolean.parseBoolean(mContext.getResources().getString(
	                        R.string.pref_networks_connectall)));
	        prefsEditor.putString("pref_credential_mobilis_default_user", 
	                mContext.getResources().getString(
	                        R.string.pref_credential_mobilis_default_user));
	        prefsEditor.putString("pref_credential_mobilis_default_pwd", 
	                mContext.getResources().getString(
	                        R.string.pref_credential_mobilis_default_pwd));
	        prefsEditor.putString("pref_credential_facebook_default_user", 
	                mContext.getResources().getString(
	                        R.string.pref_credential_facebook_default_user));
	        prefsEditor.putString("pref_credential_facebook_default_pwd", 
	                mContext.getResources().getString(
	                        R.string.pref_credential_facebook_default_pwd));
	        
	        prefsEditor.putString("pref_db_file", 
	                mContext.getResources().getString(
	                        R.string.pref_db_file));

	    }
	    
	    // set as already launched before, so the preferences won't be fetched again from the string values
	    prefsEditor.putBoolean("firstLaunch", false);
	    prefsEditor.commit(); 
	}
	
	/**
	 * Connects to the server and initializes the other mobilis services.
	 */
	public boolean initializeConnection() {

	    // Check if Preferences have been initialized already, if not, use default values.
		String host = preferences.getString("pref_host", mContext.getResources()
				.getString(R.string.pref_host_default));
		int port = Integer.parseInt(preferences.getString("pref_port", "5222"));
		String serviceName = preferences.getString("pref_service", mContext
				.getResources().getString(R.string.pref_service_default));
		mSessionCoordinator = preferences.getString("pref_coordinator", mContext
				.getResources().getString(R.string.pref_coordinator_default));
		mAndroidbuddyAgent = preferences.getString("pref_androidbuddyagent",
				mContext.getResources().getString(R.string.pref_androidbuddyagent_default));
		mPubSubServer = "pubsub." + serviceName;
		SmackConfiguration.setPacketReplyTimeout(new Long(SessionService.getInstance().getPreferences().getLong(
                "pref_xmpp_timeout", 10000)).intValue());
		
		// connect to server
		boolean success = connect(host, port, serviceName);

		if (success) {
		    // workaround: add general packet provider
		    ProviderManager pm = ProviderManager.getInstance();
		    configureProviderManager(pm);

		    // initialize the other services (add packet provider and listener)
		    mGroupService.initialize(mConnection);
		    mMessageService.initialize(mConnection);
		    mMemberService.initialize(mConnection);
		    mContextService.initialize(mConnection);
		    mPlacesService.initialize(mConnection);
		    // initialize all network authentication services for logins
		    mSocialNetworkManagementService.initialize(mConnection);
		    return true;
		} else {
		    return false;
		}
	}
	
	/**
	 * Connects to the XMPP server.
	 * 
	 * @param host
	 *            The server address.
	 * @param port
	 *            The port of the server.
	 * @param serviceName
	 *            The service name under which the server operates (may be
	 *            different from the server address).
	 */
	private boolean connect(String host, int port, String serviceName) {
		
		ConnectionConfiguration connConfig = new ConnectionConfiguration(host,
				port, serviceName);
		connConfig.setSASLAuthenticationEnabled(false);
		
		mConnection = new XMPPConnection(connConfig);
		try {
			mConnection.connect();
			return true;
		} catch (XMPPException e) {
			// TODO throw protocol independent connection exception.
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * WORKAROUND for Android only! The necessary configuration files for Smack
	 * library are not included in Android's apk-Package.
	 * 
	 * @param pm
	 *            A ProviderManager instance.
	 */
	private void configureProviderManager(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time", Class
					.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version", Class
					.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm
				.addIQProvider("query", "jabber:iq:last",
						new LastActivity.Provider());

		// User Search
		pm
				.addIQProvider("query", "jabber:iq:search",
						new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());
		pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
				new IBBProviders.Open());
		pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
				new IBBProviders.Close());
		pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
				new IBBProviders.Data());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
	}

	/**
     * Get the Singleton Instance of the SessionService.
     * 
     * @return
     */
    public static SessionService getInstance() {
        return sessionService;
    }

    public GroupMemberService getGroupMemberService() {
        return mMemberService;
    }

    public MessageHandlerService getMessageHandler() {
        return mMessageService;
    }

    public ContextManagementService getContextManagementService() {
        return mContextService;
    }

    public PlacesManagementService getPlacesManagementService() {
        return mPlacesService;
    }

    public GroupManagementService getGroupManagement() {
        return mGroupService;
    }

    public SocialNetworkManagementService getSocialNetworkManagementService() {
        return mSocialNetworkManagementService;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public String getCoordinator() {
        return mSessionCoordinator;
    }

    public String getAndroidbuddyAgent() {
        return mAndroidbuddyAgent;
    }
	
	public void initLoginLoop() {
	    loginLoop = new LoginLoop();
	}
	
    public LoginLoop getLoginLoop() {
        if (loginLoop == null) {
            initLoginLoop();
        }
        return loginLoop;
    }
    
    public InfoViewer getInfoViewer() {
        return infoViewer;
    }

    public void setInfoViewer(InfoViewer infoViewer) {
        this.infoViewer = infoViewer;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebugMode(boolean isDebugMode) {
        this.isDebugMode = isDebugMode;
    }

    public void setActivityManager(LocalActivityManager localActivityManager) {
        activityManager = localActivityManager;
    }

    public Activity getCurrentActivity() {
        return activityManager.getCurrentActivity();
    }
    
    public XMPPConnection getConnection() {
        return mConnection;
    }
}
