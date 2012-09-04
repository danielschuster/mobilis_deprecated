package de.tudresden.inf.rn.mobilis.android.login;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.MainView;
import de.tudresden.inf.rn.mobilis.android.R;
import de.tudresden.inf.rn.mobilis.android.dialog.LoginDialog;
import de.tudresden.inf.rn.mobilis.android.services.AbstractAuthService;
import de.tudresden.inf.rn.mobilis.android.services.InfoViewer;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.android.services.SocialNetworkManagementService;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.android.util.DBHelper;
import de.tudresden.inf.rn.mobilis.android.util.Util;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Credential;

/**
 * Represents an automatically looping login procedure, possibly containing GUI outputs. 
 * A new login is trigged as a LoginPart by a received callback from a former LoginPart. 
 * Single logins can also be initiated.
 * @author Dirk
 */
public class LoginLoop {

    private static final String TAG = "LoginLoop";
    private SessionService ss;
    private SocialNetworkManagementService snms;
    private Context context;
    private DBHelper db;
    private ArrayList<Credential> accounts;
    private boolean connectToAllKnownNetworks;
    private InfoViewer infoBar;
    private SharedPreferences preferences;
    private String networkToLogIn;
    private boolean running;
    private LoginDialog currentLoginDialog;
    private List<LoginPart> availableLoginParts = new ArrayList<LoginPart>();
    private BroadcastReceiver ir;
    private Handler mainThreadHandler;
    
    public LoginLoop() {
        ss = SessionService.getInstance();
        snms = ss.getSocialNetworkManagementService();
        this.context = ss.getContext();
        infoBar = ss.getInfoViewer();
        preferences = ss.getPreferences();
        mainThreadHandler = MainView.getMainThreadHandler();
        db = DBHelper.getDB();
        initDefaultValues();
        initLoginParts();
    }
    
    public void initIntentReceiver() {
        ir = new IntentReceiver();
        context.registerReceiver(ir, new IntentFilter(Const.INTENT_PREFIX + "callback.login"));
    }

    public void unregisterIntentReceiver() {
        context.unregisterReceiver(ir);
    }
    
    private class IntentReceiver extends BroadcastReceiver {
        /**
         * Listens to Intents with callback actions for the login loop.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received callback intent: " + action);
            if (action.equals(Const.INTENT_PREFIX + "callback.login")) {
                handleLoginCallback(intent);
            }
        }
    }
        
    private void initDefaultValues() {
        connectToAllKnownNetworks = 
            Boolean.parseBoolean(context.getResources().getString(R.string.pref_networks_connectall));
        networkToLogIn = Const.MOBILIS;
        running = false;
    }
    
    private void initLoginParts() {
        availableLoginParts.add(new LoginPartMobilis(this));
        availableLoginParts.add(new LoginPartFacebook(this));
    }
    
    /**
     * Starts the LoginLoop as a new thread, loading all stored Credentials for autoconnect
     * and initiating Mobilis login as the first.
     */
    public void startLoop() {
        if (testAndSetRunning()) {
            Log.i(TAG, "Retrieving stored credentials for login from db");
            accounts = db.getArrayList(db.getAutoConCredentials(null));

            // if in debug mode and nothing in db, use credentials from localconfig.xml
            if (ss.isDebugMode()) readCredentialsFromConfig();

            initNextLogin();
        }
    }
    
    private void readCredentialsFromConfig() {
        boolean foundMobilisCred = false;
        boolean foundFacebookCred = false;
        for (Credential c : accounts) {
            if (c.getNetworkName().equals(Const.MOBILIS)) foundMobilisCred = true;
            if (c.getNetworkName().equals(Const.FACEBOOK)) foundFacebookCred = true;
        }
        if (!foundMobilisCred) accounts.add(new Credential(
                Const.MOBILIS,
                preferences.getString("pref_credential_mobilis_default_user", null), 
                preferences.getString("pref_credential_mobilis_default_pwd", null), 
                true));
        if (!foundFacebookCred) accounts.add(new Credential(
                Const.FACEBOOK,
                preferences.getString("pref_credential_facebook_default_user", null), 
                preferences.getString("pref_credential_facebook_default_pwd", null), 
                true)); 
    }
    
    /**
     * Initiates any next found credential for login.
     */
    private void initNextLogin() {
        if (networkToLogIn == null) {
            initLogin(null);
        } else {
            initLogin(networkToLogIn);
            networkToLogIn = null;
        }
	}
    
    /**
     * Initiates the single login for the given network name constant by fetching
     * username and password from a matching credential.
     * If null is provided for network name, the next found credential will be taken.
     * @param networkName the network to log in, or null for any one
     */
    private void initLogin(String networkName) {
                
        Credential cred = pickCredential(networkName);
        if (cred == null) { 
            List<AbstractAuthService> remainingNetworks = snms.getNotAuthenticatedServices();
            if ((connectToAllKnownNetworks) && (!remainingNetworks.isEmpty())) {
                String network = networkName;
                if (networkName == null) {
                    network = remainingNetworks.get(0).getNetworkName();
                }
                if (!snms.isAuthenticated(network)) {
                    getLoginPart(network).openLoginDialog();
                }
            } else {
                // automatic login procedure ended
                finishLoop();
            }
        } else {
            if (!snms.isAuthenticated(cred.getNetworkName())) {
                // sends an intent to the corresponding authorization service
                getLoginPart(cred.getNetworkName()).requestLogin(cred, false);
            } else initNextLogin();
        }
    }
        
    /**
     * Removes and returns the first found credential from the login list, matching
     * the given network name. If null is provided for network name, the next found 
     * credential will be taken.
     * @param networkName the network to log in, or null for any one
     * @return Credential
     */
    private Credential pickCredential(String networkName) {
        Credential foundCred = null;
        for (Credential credential : accounts) {
            if ((networkName == null) || 
                    (credential.getNetworkName().equals(networkName))) {
                foundCred = credential;
                accounts.remove(credential);
                break;
            }
        }
        return foundCred;
    }
    
    /**
     * Checks whether a login loop is currently running. If not, it sets the state to "running"
     * and returns true, indicating that we can start a new loop.
     * @return boolean - false, if we have to wait before starting a new loop
     */
    private synchronized boolean testAndSetRunning() {
        if (running) return false;
        else {
            running = true;
            return true;
        }
    }
    
    /**
     * Requests a single login outside an automatic login loop for a given credential, 
     * by sending an intent to the authorization service corresponding to the network name.
     * @param credential the credential for log in
     */
    public void requestSingleLogin(final Credential credential) {
        Log.d(TAG, "Using credential for login: " + credential.toString());
        if (testAndSetRunning()) {
            String network = credential.getNetworkName();
            if (!snms.isAuthenticated(network)) {
                getLoginPart(network).requestLogin(credential, true);
            }
        }
    }
       
    public void notifyAboutProgress(String networkName) {
        infoBar.showProgress(context.getResources().getString(R.string.infobar_loggingin) 
                + " " + Util.getDisplayedNetworkName(networkName) + " ...");
    }
    
    public void notifyAboutLoginSuccess(String networkName) {
        Log.i(TAG, "Successfully logged into " + networkName);
        infoBar.showInfo(context.getResources().getString(R.string.infobar_loginsuccess) 
                + " " + Util.getDisplayedNetworkName(networkName));
    }
    
    public void notifyAboutLoginFailure(final String networkName) {
        Log.w(TAG, "Failed logging into " + networkName);
        infoBar.showWarning(context.getResources().getString(R.string.infobar_loginfailure) 
                + " " + Util.getDisplayedNetworkName(networkName));
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Util.showAlertDialog(context, 
                        Util.getDisplayedNetworkName(networkName) + " " + 
                        context.getResources().getString(R.string.login_failure_alert_title), 
                        context.getResources().getString(R.string.login_failure_alert_msg));
            }
        });
    }
    
    /**
     * Proceeds to the next login.
     */
    private void handleLoginCallback(Intent intent) {
        Bundle extras = intent.getExtras();
        String networkName = extras.getString(Const.INTENT_PREFIX + "callback.login.network");
        boolean proceed = true;
        boolean success = extras.getBoolean(Const.INTENT_PREFIX + "callback.login.success");
        boolean singleLogin = extras.getBoolean(Const.INTENT_PREFIX + "callback.login.single");
        if (networkName != null) {
            if (success) {
                notifyAboutLoginSuccess(networkName);
            } else {
                notifyAboutLoginFailure(networkName);
                if (networkName.equals(Const.MOBILIS)) proceed = false;
            }
            if (singleLogin) {
                proceed = false;
                running = false;
            }
        }
        if (proceed) initNextLogin();
    }
    
    private void finishLoop() {
        Log.i(TAG, "Login procedure finished");
        initDefaultValues();
        // callback to SessionService
        Intent intent = new Intent(Const.INTENT_PREFIX + "callback.loginloop_finished");
        context.sendBroadcast(intent);
    }

    public Context getContext() {
        return context;
    }

    public LoginDialog getCurrentLoginDialog() {
        return currentLoginDialog;
    }

    public void setCurrentLoginDialog(LoginDialog currentLoginDialog) {
        this.currentLoginDialog = currentLoginDialog;
    }

    private LoginPart getLoginPart(String networkName) {
        for (LoginPart part : availableLoginParts) {
            if (part.getNetworkName().equals(networkName)) return part;
        }
        return null;
    }
    
    public Handler getMainThreadHandler() {
        return mainThreadHandler;
    }
}
