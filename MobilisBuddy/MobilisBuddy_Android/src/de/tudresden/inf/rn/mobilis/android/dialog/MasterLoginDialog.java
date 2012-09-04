package de.tudresden.inf.rn.mobilis.android.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import de.tudresden.inf.rn.mobilis.android.R;

/**
 * Login dialog which can be used to type in a master password for decrypting locally stored
 * account credentials.
 * @author Dirk
 */
public class MasterLoginDialog extends LoginDialog {

    public MasterLoginDialog(Context context, View.OnClickListener btnClickListener) {
        super(context, btnClickListener);
        networkName = getContext().getString(R.string.masterlogin_target);
    }
        
    @Override
    protected void initComponents() {
        setContentView(R.layout.masterlogindialog);
        edtPassword = (EditText) findViewById(R.id.masterlogin_dlg_edit_password);
        btnLogin = (Button) findViewById(R.id.masterlogin_dlg_btn_autocon);
        btnLogin.setOnClickListener(mBtnClickListener);
        setTitle(R.string.masterlogin_dlg_title);
    }
    
    @Override
    public String getUsername() {
        return "";
    }
}
