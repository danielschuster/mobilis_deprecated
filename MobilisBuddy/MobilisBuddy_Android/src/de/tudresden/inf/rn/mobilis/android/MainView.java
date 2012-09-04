package de.tudresden.inf.rn.mobilis.android;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActivityGroup;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import de.tudresden.inf.rn.mobilis.android.buddylist.BuddyList;
import de.tudresden.inf.rn.mobilis.android.dialog.GroupDialog;
import de.tudresden.inf.rn.mobilis.android.login.LoginLoop;
import de.tudresden.inf.rn.mobilis.android.services.BuddyListService;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.android.util.DBHelper;

/**
 * Activity to start first, initiates SessionService and automatic LoginLoop.
 * @author Dirk, Istvan
 */
public class MainView extends ActivityGroup {

    private static final String TAG = "MainView";
    private SessionService sessionService;
    private TabHost mTabHost;
    private InfoBar infoBar;
    private GroupDialog mGroupDialog;
    private static Handler mainThreadHandler;
    private static ProgressDialog progressDialog;
    private IntentReceiver ir;
    private boolean firstRun;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
            
            // activity got recreated from a former state
            Log.v(TAG, "Recreating MainView Activity");
            firstRun = false;
            
        } else {
            
            // activity must have been started through a new intent
            Log.v(TAG, "Creating new MainView Activity");
            firstRun = true;
            mainThreadHandler = new Handler();
            createGUIContents();
            initSessionService();
            initIntentReceivers();
        }

    }

	@Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "Starting MainView Activity");
        if (firstRun) {
            firstRun = false;
            initDatabase();
            startLoginProcedure();
            showSplashScreen();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "Stopping MainView Activity");
        //unregisterIntentReceivers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Destroying MainView Activity");
        if (sessionService != null) {
            Log.v(TAG, "Unregister IntentReceivers");
            unregisterIntentReceivers();
            Log.i(TAG, "Logging out of Mobilis");
            sessionService.getConnection().disconnect();
        }
        DBHelper.getDB().close();
        
        // kill the process, important as long as reconnect to mobilis does not work
        int pid = Process.myPid();
        Process.killProcess(pid);
    }

    private void createGUIContents() {
        setContentView(R.layout.mainview);
        mTabHost = (TabHost) findViewById(R.id.main_tabhost);
        mTabHost.setup(this.getLocalActivityManager());
        
        // map tab
        TabSpec ts1 = mTabHost.newTabSpec("one");
        ts1.setIndicator("Map", getResources().getDrawable(R.drawable.map));
        Intent i1 = new Intent(this, LocationMapActivity.class);
        ts1.setContent(i1);
        mTabHost.addTab(ts1);
        
        // buddy list tab
        TabSpec ts3 = mTabHost.newTabSpec("buddylist");
        ts3.setIndicator("Buddies", getResources().getDrawable(R.drawable.ic_menu_agenda));
        Intent i3 = new Intent(this, BuddyList.class);
        ts3.setContent(i3);
        mTabHost.addTab(ts3);
        
        mTabHost.setCurrentTab(0);
        mTabHost.setEnabled(false);
        // TODO check if location provider is available, if not, choose manually
        
        // InfoBar
        infoBar = (InfoBar) findViewById(R.id.main_infobar);
        infoBar.setMainThreadHandler(mainThreadHandler);
    }

	private void showSplashScreen() {
		new SplashScreen(this).show();
	}    
    
    /**
     * Creates a SessionService instance and stores the MainView's context for
     * other objects
     */
    private void initSessionService() {
        sessionService = SessionService.getInstance();
        sessionService.setContext(this);
        sessionService.setActivityManager(getLocalActivityManager());
        sessionService.startIntentService();
        sessionService.initializePreferences();
        sessionService.setInfoViewer(infoBar);
        sessionService.initLoginLoop();
    }

    /**
     * Registers all intent receivers.
     */
    private void initIntentReceivers() {
        sessionService.initializeIntentReceivers();
        ir = new IntentReceiver();
        registerReceiver(ir, new IntentFilter(
                Const.INTENT_PREFIX + "callback.groupsquery"));
        registerReceiver(ir, new IntentFilter(
                Const.INTENT_PREFIX + "callback.creategroup"));
        registerReceiver(ir, new IntentFilter(
                Const.INTENT_PREFIX + "callback.joingroup"));
        registerReceiver(ir, new IntentFilter(
                Const.INTENT_PREFIX + "callback.loginloop_finished"));        
    }
    
    /**
     * Unregisters all intent receivers.
     */
    private void unregisterIntentReceivers() {
        sessionService.unregisterIntentReceivers();
        unregisterReceiver(ir);
    }
    
    /**
     * Just opens the database file for the first time.
     */
    private void initDatabase() {
        DBHelper.getDB();
    }

    private void startLoginProcedure() {
        LoginLoop loginLoop = SessionService.getInstance().getLoginLoop();
        
        // if you have implemented encryption for local stored data, use the 
        // MasterLoginDialog to ask for a device password to decrypt everything
        //loginLoop.openMasterLoginDialog();
        
        // starts the login procedure
        loginLoop.startLoop();
    }

    /**
     * Sends a create group intent with the new group's name as extra.
     */
    private void callCreateGroup(String groupName) {
        Intent i = new Intent(Const.INTENT_PREFIX + "servicecall.creategroup");
        i.putExtra(Const.INTENT_PREFIX + "servicecall.creategroup.groupname", groupName);
        sendBroadcast(i);
    }

    /**
     * Sends a join group intent with the group's name as extra.
     */
    private void callJoinGroup(String groupName) {
        Intent i = new Intent(Const.INTENT_PREFIX + "servicecall.joingroup");
        i.putExtra(Const.INTENT_PREFIX + "servicecall.joingroup.groupname", groupName);
        sendBroadcast(i);
    }

    /**
     * Dismisses the group query progress dialog and shows a list of all
     * available groups.
     * 
     * @param groups
     */
    public void handleGroupsQueryCallback(HashMap<String, String> groups) {
        // mProgressDialog.dismiss(); //introduce ThreadManager -> dismiss
        // registered mProgressDialog
        mGroupDialog = new GroupDialog(this, new ArrayList<String>(groups.keySet()), 
                new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String groupName = mGroupDialog.getNewGroupName();
                // get selected group's or new group's name
                if (mGroupDialog.isNewGroupRequested()) {
                    callCreateGroup(groupName);
                } else {
                    // join selected room
                    groupName = mGroupDialog.getSelectedGroupName();
                    callJoinGroup(groupName);
                }
                mGroupDialog.dismiss();
            }
        });
        mGroupDialog.show();
    }

    /**
     * Enables the view and activates the group chat tab.
     */
    public void handleJoinGroupCallback() {
        TabSpec ts2 = mTabHost.newTabSpec("chat");
        ts2.setIndicator("Chat", getResources().getDrawable(R.drawable.chat));
        Intent i2 = new Intent(this, ChatActivity.class);
        ts2.setContent(i2);
        mTabHost.addTab(ts2);
        mTabHost.setEnabled(true);
    }

    public void handleCreateGroupCallback() {
        // TODO Auto-generated method stub
    }

    /**
     * After logging into all networks the known buddies of the user will be published to the server.
     */
    public void handleLoginLoopFinishedCallback() {
        BuddyListService buddyService = sessionService.
            getSocialNetworkManagementService().getBuddyListService();
        //FIXME: Crashes on real phones    
//        buddyService.publishPhoneContacts();
        buddyService.publishRosterContacts();
    }
    
    /**
     * Listens to Intents with callback actions of this class.
     */
    private class IntentReceiver extends BroadcastReceiver {

        @Override
        @SuppressWarnings("unchecked")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action
                    .equals(Const.INTENT_PREFIX + "callback.groupsquery")) {
                HashMap<String, String> groups = (HashMap<String, String>) intent
                        .getSerializableExtra(Const.INTENT_PREFIX + "callback.groupsquery.groups");
                MainView.this.handleGroupsQueryCallback(groups);
            } else if (action
                    .equals(Const.INTENT_PREFIX + "callback.joingroup")) {
                MainView.this.handleJoinGroupCallback();
            } else if (action
                    .equals(Const.INTENT_PREFIX + "callback.creategroup")) {
                MainView.this.handleCreateGroupCallback();
            } else if (action
                    .equals(Const.INTENT_PREFIX + "callback.loginloop_finished")) {
                MainView.this.handleLoginLoopFinishedCallback();
            }
        }
    }

    public static Handler getMainThreadHandler() {
        return mainThreadHandler;
    }

    public static ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public static void setProgressDialog(ProgressDialog progressDialog) {
        MainView.progressDialog = progressDialog;
    }
}