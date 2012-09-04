package de.tudresden.inf.rn.mobilis.android.login;

import de.tudresden.inf.rn.mobilis.android.dialog.FacebookLoginDialog;
import de.tudresden.inf.rn.mobilis.android.dialog.LoginDialog;
import de.tudresden.inf.rn.mobilis.android.util.Const;

/**
 * Specific login part of a LoginLoop for Facebook.
 * @author Dirk
 */
public class LoginPartFacebook extends LoginPart {

    public LoginPartFacebook(LoginLoop l) {
        super(l);
    }

    @Override
    public String getNetworkName() {
        return Const.FACEBOOK;
    }

    @Override
    protected LoginDialog createLoginDialog() {
        return new FacebookLoginDialog(loop.getContext(), createStandardDialogClickListener());
    }
}
