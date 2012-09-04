package de.tudresden.inf.rn.mobilis.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.android.util.DBHelper;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Credential;

/**
 * Activity to type in and store account data for login.
 * @author Dirk
 */
public class AccountEditActivity extends Activity {

    private DBHelper db;
    private boolean editMode;
    private boolean overwrite;

    // data
    private String networkName;
    private String userId;
    private String password;
    private boolean autoConnect;
    private Credential credential;
    private Credential comparedCrd;

    // GUI
    private static final int CHOOSE_NETWORK_DLG = 1;
    private static final int OVERWRITE_CRD_DLG = 2;
    private int networkRadioId;
    private RadioGroup radioNetworks;
    private EditText editId;
    private EditText editPwd;
    private CheckBox checkboxAutoCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DBHelper.getDB();
        networkRadioId = -1;

        // fetch initial values
        if (savedInstanceState != null) {

            // activity got recreated from a former state
            networkRadioId = savedInstanceState.getInt(Const.SAVED_PREFIX
                    + "editacc.networkradio");
            userId = savedInstanceState.getString(Const.SAVED_PREFIX
                    + "editacc.userid");
            password = savedInstanceState.getString(Const.SAVED_PREFIX
                    + "editacc.password");
            autoConnect = savedInstanceState.getBoolean(Const.SAVED_PREFIX
                    + "editacc.autocon");
            editMode = savedInstanceState.getBoolean(Const.SAVED_PREFIX
                    + "editacc.editmode");

        } else {

            // activity must have been started through a new intent
            editMode = false;
            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                // edit account chosen
                editMode = true;
                networkName = extras.getString(Const.INTENT_PREFIX
                        + "accedit.network");
                if (networkName != null)
                    networkRadioId = getRadioIdForNetworkName(networkName);
                userId = extras.getString(Const.INTENT_PREFIX
                        + "accedit.userid");

                // fetch account credential from db
                credential = db.getCredential(userId, networkName);
                password = credential.getPassword();
                autoConnect = credential.isAutoConnect();
            }
        }

        createGUIContents();
        populateFields();
    }

    private void createGUIContents() {
        setContentView(R.layout.accountedit_activity);

        radioNetworks = (RadioGroup) findViewById(R.id.accedit_act_radio_group);
        checkboxAutoCon = (CheckBox) findViewById(R.id.accedit_act_check_autocon);
        editId = (EditText) findViewById(R.id.accedit_act_edit_id);
        editPwd = (EditText) findViewById(R.id.accedit_act_edit_pwd);

        Button saveButton = (Button) findViewById(R.id.accedit_act_btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handleSaveButtonClick();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.accedit_act_btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handleCancelButtonClick();
            }
        });
    }

    private void handleSaveButtonClick() {
        readOutValues();
        if (checkValues()) {
            storeAndFinish();
        }
    }

    private void handleCancelButtonClick() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void populateFields() {
        if (userId != null) {

            // fill fields for edit account or restored state
            radioNetworks.check(networkRadioId);
            editId.setText(userId);
            editPwd.setText(password);
            checkboxAutoCon.setChecked(autoConnect);

        } else {

            // case of new account -> default entries for fields
            radioNetworks.check(R.id.accedit_act_radio_mb);
            checkboxAutoCon.setChecked(true);
        }
    }

    /**
     * Returns the id of the radio button which corresponds to the network name.
     * @param networkName the name of the social network
     * @return int the id of the corresponding button, -1 if no matching button
     *         could be found
     */
    private int getRadioIdForNetworkName(String networkName) {
        if (networkName.equals(Const.MOBILIS)) {
            return R.id.accedit_act_radio_mb;
        } else if (networkName.equals(Const.FACEBOOK)) {
            return R.id.accedit_act_radio_fb;
        } else
            return -1;
    }

    /**
     * Returns the network name to the id of the corresponding radio button.
     * @param networkRadioId the id of the corresponding button
     * @return String the name of the social network or null if no match
     */
    private String getNetworkNameForRadioId(int networkRadioId) {
        switch (networkRadioId) {
        case R.id.accedit_act_radio_mb:
            return Const.MOBILIS;
        case R.id.accedit_act_radio_fb:
            return Const.FACEBOOK;
        }
        return null;
    }

    /**
     * Read out every changes the user may have done in the edit account screen.
     * Needed before saving.
     */
    private void readOutValues() {
        networkRadioId = radioNetworks.getCheckedRadioButtonId();
        networkName = getNetworkNameForRadioId(networkRadioId);
        userId = editId.getText().toString();
        password = editPwd.getText().toString();
        autoConnect = checkboxAutoCon.isChecked();
    }
        
    private boolean checkValues() {
        
        if (networkRadioId == -1) {
            // show alert dialog for no network selection
            showDialog(CHOOSE_NETWORK_DLG);
            return false;
        }
        
        // look in db for an existing credential with the current typed-in userID and network
        comparedCrd = db.getCredential(userId, networkName);
        if (comparedCrd != null) {
            if (credential != comparedCrd) {
                // entered credential equals other existing credential in db, ask user for overwrite
                showDialog(OVERWRITE_CRD_DLG);
                return false;
            }
        }
        
        return true;
    }

    private void prepareCredentialForStoring() {
        if (editMode) {
            // update existing credential
            credential.setUserId(userId);
            credential.setPassword(password);
            credential.setNetworkName(networkName);
            credential.setAutoConnect(autoConnect);
        } else {
            // create a new credential to store in db
            credential = new Credential(networkName, userId, password, autoConnect);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        switch (id) {
        case CHOOSE_NETWORK_DLG:
            builder = new AlertDialog.Builder(AccountEditActivity.this);
            builder.setTitle(R.string.accedit_validatedlg_title);
            builder.setMessage(R.string.accedit_validatedlg_text);
            builder.setPositiveButton(R.string.accedit_validatedlg_btn_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int button) {
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        case OVERWRITE_CRD_DLG:
            builder = new AlertDialog.Builder(AccountEditActivity.this);
            builder.setTitle(R.string.accedit_overwritedlg_title);
            builder.setMessage(R.string.accedit_overwritedlg_text);
            builder.setPositiveButton(R.string.accedit_overwritedlg_btn_yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int button) {
                            overwrite = true;
                            dialog.dismiss();
                            storeAndFinish();
                        }
                    });
            builder.setNegativeButton(R.string.accedit_overwritedlg_btn_no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int button) {
                            overwrite = false;
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        }
        return null;
    }

    private void storeAndFinish() {
        prepareCredentialForStoring();
        storeCredential();
        if (autoConnect) updateOtherAutoCredentials();
        setResult(RESULT_OK);
        finish();
    }
    
    private void storeCredential() {
        if (overwrite) db.delete(comparedCrd);
        db.store(credential);
        db.commit();
    }
    
    /**
     * Disables autoconnect for all other stored credentials found for the current network.
     */
    private void updateOtherAutoCredentials() {
        ArrayList<Credential> otherCreds = db.getArrayList(db.getAutoConCredentials(networkName));
        for (Credential c : otherCreds) {
            if (c != credential) {
                c.setAutoConnect(false);
                db.store(c);
            }
        }
        db.commit();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        readOutValues();
        outState.putInt(Const.SAVED_PREFIX + "editacc.networkradio", networkRadioId);
        outState.putString(Const.SAVED_PREFIX + "editacc.userid", userId);
        outState.putString(Const.SAVED_PREFIX + "editacc.password", password);
        outState.putBoolean(Const.SAVED_PREFIX + "editacc.autocon", autoConnect);
        outState.putBoolean(Const.SAVED_PREFIX + "editacc.editmode", editMode);
    }

}
