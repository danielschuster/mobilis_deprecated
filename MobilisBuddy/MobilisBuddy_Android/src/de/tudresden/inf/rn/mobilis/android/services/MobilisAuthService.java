package de.tudresden.inf.rn.mobilis.android.services;

import org.jivesoftware.smack.XMPPException;

import android.os.Bundle;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.R;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.xmpp.packet.SettingsIQ;

public class MobilisAuthService extends AbstractAuthService {

    private static final String TAG = "MobilisAuthService";

    /**
     * Login to the mobilis server. 
     * The intent has to provide a username and a password for login in the bundle.
     * @return boolean true, if login was successful, false otherwise
     */
    @Override
    public boolean performLogin(Bundle b) {

        SessionService ss = SessionService.getInstance();
        boolean success = ss.initializeConnection();

        if (success) {
            try {
                String username = b.getString(Const.INTENT_PREFIX + "login.userid").replaceAll(" ", "");
                String password = b.getString(Const.INTENT_PREFIX + "login.password").replaceAll(" ", "");

                String resource = ss.getPreferences().getString(
                        "pref_resource", ss.getContext().getResources()
                        .getString(R.string.pref_resource_default));

                if ((username == null) || (username.equals("")) ||
                        (password == null) || (password.equals("")) ||
                        (resource == null) || (resource.equals(""))) {
                    success = false;
                } else {

                    // login to xmpp server via the xmpp connection
                    connection.login(username, password, resource);
                }
                if (success) connectedUserId = username;

            } catch (XMPPException e) {
                // TODO Add protocol independent login exception.
                e.printStackTrace();
                success = false;
            }
        }

        initLocationUpdates();

        // TODO implement mobilis login success / failure response
        return success;
    }

    private void initLocationUpdates() {
        SettingsIQ sIQ = new SettingsIQ();
        sIQ.setFrom(connection.getUser());
        sIQ.setTo(SessionService.getInstance().getAndroidbuddyAgent());
        sIQ.setService("BuddyFinderService");
        sIQ.setName("Status");
        sIQ.setValue("true");
        connection.sendPacket(sIQ);
    }

    @Override
    public boolean performLogout(Bundle extras) {
        Log.i(TAG, "Logging out of Mobilis");
        connection.disconnect();
        return true;
    }

    @Override
    public String getNetworkName() {
        return Const.MOBILIS;
    }
}
