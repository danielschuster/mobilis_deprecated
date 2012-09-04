package de.tudresden.inf.rn.mobilis.android.login;

import de.tudresden.inf.rn.mobilis.android.dialog.LoginDialog;
import de.tudresden.inf.rn.mobilis.android.dialog.MobilisLoginDialog;
import de.tudresden.inf.rn.mobilis.android.util.Const;

/**
 * Specific login part of a LoginLoop for Mobilis.
 * @author Dirk
 */
public class LoginPartMobilis extends LoginPart {

    public LoginPartMobilis(LoginLoop l) {
        super(l);
    }

    @Override
    public String getNetworkName() {
        return Const.MOBILIS;
    }

    @Override
    protected LoginDialog createLoginDialog() {
        return new MobilisLoginDialog(loop.getContext(), createStandardDialogClickListener());
    }

}
