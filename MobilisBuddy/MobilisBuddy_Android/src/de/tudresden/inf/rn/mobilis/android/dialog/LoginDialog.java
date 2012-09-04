package de.tudresden.inf.rn.mobilis.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.android.R;

/**
 * Standard login dialog to enter a user ID and a password.
 * @author Dirk
 */
public class LoginDialog extends Dialog {

    // views
    protected EditText edtUsername;
    protected EditText edtPassword;
    protected Button btnLogin;
    protected String networkName;
    protected boolean showProgressDialog;

    // fields
    protected View.OnClickListener mBtnClickListener;

    public LoginDialog(Context context, View.OnClickListener btnClickListener) {
        super(context);
        mBtnClickListener = btnClickListener;
        networkName = getContext().getString(R.string.network_std_name);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initComponents();
    }

    protected void initComponents() {
        setContentView(R.layout.logindialog);
        initTextLabels();
        edtUsername = (EditText) findViewById(R.id.login_dlg_edit_username);
        edtPassword = (EditText) findViewById(R.id.login_dlg_edit_password);
        btnLogin = (Button) findViewById(R.id.login_dlg_btn_login);
        btnLogin.setOnClickListener(mBtnClickListener);
    }

    protected void initTextLabels() {
        // Standard Login Screen
        TextView tv1 = (TextView) findViewById(R.id.login_dlg_txt_username);
        TextView tv2= (TextView) findViewById(R.id.login_dlg_txt_password);
        setTitle(R.string.stdlogin_dlg_title);
        tv1.setText(R.string.stdlogin_dlg_txt_username);
        tv2.setText(R.string.stdlogin_dlg_txt_password);
    }
    
    
    public String getUsername() {
        return edtUsername.getText().toString();
    }

    public String getPassword() {
        return edtPassword.getText().toString();
    }
    
    public String getNetworkName() {
        return networkName;
    }
}
