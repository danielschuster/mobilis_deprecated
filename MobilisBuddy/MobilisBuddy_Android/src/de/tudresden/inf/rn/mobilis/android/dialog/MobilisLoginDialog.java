package de.tudresden.inf.rn.mobilis.android.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.android.R;

/**
 * Mobilis login dialog to enter a user ID and a password.
 * @author Dirk
 */
public class MobilisLoginDialog extends LoginDialog {

    public MobilisLoginDialog(Context context, View.OnClickListener btnClickListener) {
        super(context, btnClickListener);
        networkName = getContext().getString(R.string.network_mb_name);
    }
    
    @Override
    protected void initTextLabels() {
        // MocoGuide text labels
        TextView tv1 = (TextView) findViewById(R.id.login_dlg_txt_username);
        TextView tv2= (TextView) findViewById(R.id.login_dlg_txt_password);
        setTitle(R.string.mblogin_dlg_title);
        tv1.setText(R.string.mblogin_dlg_txt_username);
        tv2.setText(R.string.mblogin_dlg_txt_password);  
    }
}
