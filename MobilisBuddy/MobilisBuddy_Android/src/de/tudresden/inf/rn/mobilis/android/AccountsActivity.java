package de.tudresden.inf.rn.mobilis.android;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import de.tudresden.inf.rn.mobilis.android.login.LoginLoop;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.android.util.DBHelper;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Credential;

/**
 * Activity to show all account data stored on the Android device.
 * @author Dirk
 */
public class AccountsActivity extends ListActivity {
    
    private static final String TAG = "AccountsActivity";
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private DBHelper db;

    // GUI
    private static int listItemId = R.id.acc_act_lst_item;
    private static int listItemLayout =  R.layout.accounts_act_item;
    private TextView markedView;
    private Credential markedAccount;
    private TextView selectedView;
    private ListView accountList;
    private Button autoConnectButton;
    private Button selConnectButton;
    private ImageButton newButton;
    private ImageButton editButton;
    private ImageButton delButton;
    private Button backButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DBHelper.getDB();
        createGUIContents();
    }
    
    private void createGUIContents() {
        
        setContentView(R.layout.accounts_activity);

        accountList = getListView();
        accountList.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> listView, View view,
                    int position, long rowId) {
                // reading out focused entry, no marking yet!
                deselectCurrentListEntry();
                selectListEntry(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // do nothing
            }
            
        });

        accountList.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!accountList.hasFocus()) {
                    deselectCurrentListEntry();
                }
            }
        });
        
        autoConnectButton = (Button) findViewById(R.id.acc_act_btn_autocon);
        autoConnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                enableSelectionDependentButtons(false);
                performAutoConnect();
            }
        });
        
        selConnectButton = (Button) findViewById(R.id.acc_act_btn_selcon);
        selConnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                enableSelectionDependentButtons(false);
                if (markedAccount != null) {
                    SessionService.getInstance().getLoginLoop().requestSingleLogin(markedAccount);
                }
            }
        });
        
        newButton = (ImageButton) findViewById(R.id.acc_act_btn_new);
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(AccountsActivity.this, AccountEditActivity.class);
                startActivityForResult(i, ACTIVITY_CREATE);
            }
        });        
        
        editButton = (ImageButton) findViewById(R.id.acc_act_btn_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (markedAccount != null) {
                    Intent i = new Intent(AccountsActivity.this, AccountEditActivity.class);
                    i.putExtra(Const.INTENT_PREFIX + "accedit.network", markedAccount.getNetworkName());
                    i.putExtra(Const.INTENT_PREFIX + "accedit.userid", markedAccount.getUserId());
                    startActivityForResult(i, ACTIVITY_EDIT);
                }
            }
        });
        
        delButton = (ImageButton) findViewById(R.id.acc_act_btn_del);
        delButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (markedAccount != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AccountsActivity.this);
                    builder.setTitle(R.string.acc_delconfirmdlg_title);
                    builder.setMessage(getResources().getText(R.string.acc_delconfirmdlg_text)
                            + " " + markedAccount.toString());
                    builder.setPositiveButton(R.string.acc_delconfirmdlg_btn_yes,
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int button) {
                            dialog.dismiss();
                            db.delete(markedAccount);
                            discardListSelection();
                            updateAccountList();
                        }
                    });
                    builder.setNegativeButton(R.string.acc_delconfirmdlg_btn_no,
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int button) {
                            dialog.dismiss();
                            discardListSelection();
                        }
                    });
                    Dialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        
        backButton = (Button) findViewById(R.id.acc_act_btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });
        
        enableSelectionDependentButtons(false);
        updateAccountList();
    }
    
    private void updateAccountList() {
        List<Credential> accounts = db.getAllCredentials();
        
        ArrayAdapter<Credential> a = new ArrayAdapter<Credential>(
                this, listItemLayout, accounts);
        setListAdapter(a);
    }
    
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long rowId) {
        super.onListItemClick(listView, view, position, rowId);
        
        // marking the clicked entry
        unmarkCurrentListEntry();
        markedAccount = (Credential) listView.getAdapter().getItem(position);
        Log.v(TAG, "Selected Credential: " + markedAccount.toString());
        markListEntry(view);
        enableSelectionDependentButtons(true);
    }
    
    private void deselectCurrentListEntry() {
        if ((selectedView != null) && (selectedView != markedView)) {
            selectedView.setBackgroundColor(Color.rgb(68, 68, 68));
            selectedView.setTextColor(Color.rgb(190, 190, 190));
        }
    }
    
    private void selectListEntry(View view) {
        selectedView = (TextView) view;
        if (selectedView != markedView) {
            selectedView = (TextView) view;
            view.setBackgroundColor(Color.rgb(255, 125, 0));
            TextView listEntry = (TextView) view.findViewById(listItemId);
            listEntry.setTextColor(Color.rgb(0, 0, 0));
        }
    }
    
    private void unmarkCurrentListEntry() {
        if (markedView != null) {
            markedView.setBackgroundColor(Color.rgb(68, 68, 68));
            markedView.setTextColor(Color.rgb(190, 190, 190));
        }
    }
    
    private void markListEntry(View view) {
        markedView = (TextView) view;
        view.setBackgroundColor(Color.rgb(234, 171, 0));
        TextView listEntry = (TextView) view.findViewById(listItemId);
        listEntry.setTextColor(Color.rgb(0, 0, 0));
    }
    
    private void performAutoConnect() {
        LoginLoop loginLoop = SessionService.getInstance().getLoginLoop();
        loginLoop.startLoop();
    }
    
    private void discardListSelection() {
        markedView = null;
        markedAccount = null;
        enableSelectionDependentButtons(false);
    }
    
    private void enableSelectionDependentButtons(boolean enabled) {
        selConnectButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
        delButton.setEnabled(enabled);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        discardListSelection();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        updateAccountList();
    }
}
