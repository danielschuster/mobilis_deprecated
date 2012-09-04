package de.tudresden.inf.rn.mobilis.android.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.android.R;

/**
 * Facebook login dialog to enter a user ID and a password.
 * @author Dirk
 */
public class FacebookLoginDialog extends LoginDialog {
    
    public FacebookLoginDialog(Context context, View.OnClickListener btnClickListener) {
        super(context, btnClickListener);
        networkName = getContext().getString(R.string.network_fb_name);
    }
    
    @Override
    protected void initTextLabels() {
        // Facebook text labels
        TextView tv1 = (TextView) findViewById(R.id.login_dlg_txt_username);
        TextView tv2= (TextView) findViewById(R.id.login_dlg_txt_password);
        setTitle(R.string.fblogin_dlg_title);
        tv1.setText(R.string.fblogin_dlg_txt_username);
        tv2.setText(R.string.fblogin_dlg_txt_password); 
    }
}
