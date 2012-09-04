package de.tudresden.inf.rn.mobilis.android.login;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import de.tudresden.inf.rn.mobilis.android.dialog.LoginDialog;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Credential;

/**
 * Stands for one login (possibly as a part of a currently running LoginLoop). 
 * Defines a login dialog to show up, if no account credentials are supplied.
 * Login parts for specific networks have to be instantiated from the corresponding subclasses.
 * @author Dirk
 */
public abstract class LoginPart {

    private static final String TAG = "LoginPart";
    protected LoginLoop loop;
    
    public LoginPart(LoginLoop l) {
        this.loop = l;
    }
    
    /**
     * Requests the login for a given credential, by sending an 
     * intent to the authorization service corresponding to the network name.
     * @param credential the credential for log in
     * @param singleLogin the boolean value indicating if this is a login as a part of a login loop
     */
    public void requestLogin(Credential credential, boolean singleLogin) {
        String networkName = credential.getNetworkName();
        Log.i(TAG, "Start logging into " + networkName);
        loop.notifyAboutProgress(networkName);
        Intent intent = createLoginIntent(credential, singleLogin);
        loop.getContext().sendOrderedBroadcast(intent, null);
    }
    
    protected Intent createLoginIntent(Credential credential, boolean singleLogin) {
        Intent intent = new Intent(Const.INTENT_PREFIX + "login");
        intent.putExtra(Const.INTENT_PREFIX + "login.network", credential.getNetworkName());
        intent.putExtra(Const.INTENT_PREFIX + "login.userid", credential.getUserId());
        intent.putExtra(Const.INTENT_PREFIX + "login.password", credential.getPassword());
        intent.putExtra(Const.INTENT_PREFIX + "login.single", singleLogin);
        return intent;
    }
    
    protected OnClickListener createStandardDialogClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                LoginDialog dialog = loop.getCurrentLoginDialog();
                String userId = dialog.getUsername();
                String password = dialog.getPassword();
                dialog.dismiss();
                requestLogin(new Credential(getNetworkName(), userId, password, true), false);
            }
        };
    }
    
    /**
     * Opens a login dialog for the login part.
     */
    public void openLoginDialog() {
        loop.getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                LoginDialog dialog = createLoginDialog();
                loop.setCurrentLoginDialog(dialog);
                dialog.show();
            }
        });
    }
    
    /**
     * Returns a new login dialog for this login part.
     * @return LoginDialog
     */
    protected abstract LoginDialog createLoginDialog();
    
    /**
     * Returns the network name for the login part.
     * @return String
     */
    public abstract String getNetworkName();
    
}
