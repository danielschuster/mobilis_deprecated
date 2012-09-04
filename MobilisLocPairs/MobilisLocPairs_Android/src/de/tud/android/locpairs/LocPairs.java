
package de.tud.android.locpairs;

import java.util.List;
import java.util.Random;

import de.tud.android.locpairs.controller.LocPairsController;
import de.tud.android.locpairs.model.Game;
import de.tud.android.locpairs.model.Settings;
import de.tud.android.mapbiq.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * LocPairs is the central Activity of the game.
 * The game will be started here and the LocPairsController is registered.
 */
public class LocPairs extends Activity {
	
	/** The lp controller. */
	private LocPairsController lpController;
	
	/** The lp controller intent. */
	private Intent lpControllerIntent;
	
	/** The m progress. */
	private ProgressBar mProgress;
	
	/** The m progress status. */
	private int mProgressStatus = 0;

	/** The m handler. */
	private Handler mHandler = new Handler();
	
	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locpairs_menu);
        
		//init game
		Game.getInstance();
		
		//register LocPairsController
		lpControllerIntent = new Intent(this, LocPairsController.class);
		bindService(lpControllerIntent, onService, BIND_AUTO_CREATE);
		
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    /**
     * The ClientPlayer will be added to the Gameinstanz and a name will be set automatically.
     * The connection to the XMPP Server is set here.
     */
    public void onStart()
    {
    	super.onStart();
        
        String name = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).getString("playername", "not_set");
        if(name.equals("not_set"))
        {
        	String[] startgen={"Del","Bos","Los","Kin","Win", "Ste"};
            String[] addgen={"lov","ieb","er","ect","dows", "rot","angy", "bol", "oses", "tec", "fred", "fan"};
            Random generator = new Random();
            String genname = startgen[generator.nextInt(startgen.length)] + addgen[generator.nextInt(addgen.length)];
        	name = genname;
        	Editor e = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).edit();
        	e.putString("playername", name);
            e.commit();
        }
        Game.getInstance().getClientPlayer().setPlayername(name);
        
        
        name = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).getString("locpairsAdress", "not_set");
        if(!name.equals("not_set"))
        {
        		Settings.getInstance().setLocpairsAddress(name);
        }
        name = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).getString("xmppserver", "not_set");
        if(!name.equals("not_set"))
        {
        		Settings.getInstance().setXmppServer(name);
        }
        name = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).getString("xmppport", "not_set");
        if(!name.equals("not_set"))
        {
        		Settings.getInstance().setXmppPort(new Integer(name));
        }
    }
    
    /**
	 * Handle "lobby" action.
	 * 
	 * @param v
	 *            the v
	 */
    public void onLobbyClick(View v) {
    	Intent LobbyIntent = new Intent(getBaseContext(), GameInstancesActivity.class);
		startActivity(LobbyIntent);
    }
    
    /**
	 * Handle "instructions" action.
	 * 
	 * @param v
	 *            the v
	 */
    public void onInstructionsClick(View v) {
    	Intent instructionsIntent = new Intent(getBaseContext(), InstructionsActivity.class);
		startActivity(instructionsIntent);
    }
    
    /**
	 * Handle "settings" action.
	 * 
	 * @param v
	 *            the v
	 */
    public void onSettingsClick(View v) {
    	Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
		startActivity(settingsIntent);
    }
    
    /**
	 * Checks if is intent available.
	 * 
	 * @param context
	 *            the context
	 * @param action
	 *            the action
	 * @return true, if is intent available
	 */
    public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
    
    /**
	 * Handle "refresh" action.
	 * 
	 * @param v
	 *            the v
	 */
    public void onRefreshClick(View v) {
    }
    
    /**
	 * Handle "chat" action.
	 * 
	 * @param v
	 *            the v
	 */
    public void onChatClick(View v) {
    }
    
    /**
	 * Handles the scan of a QR Code.
	 * 
	 * @param requestCode
	 *            the request code
	 * @param resultCode
	 *            the result code
	 * @param intent
	 *            the intent
	 */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
                Context context = getApplicationContext();
                CharSequence text = "You scanned: "+contents;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	lpController.disconnect();
    	lpController.unbindService(onService);
    }
    
    /**
     * Connects a service if the LocpairsController is available.
     */
    private ServiceConnection onService = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className,
				IBinder rawBinder) {
			lpController = ((LocPairsController.LocalBinder) rawBinder).getService();
			lpController.connectWFS();
    		lpController.initializeMXA();
    		
			Log.i("Service", "connected");
		}

		public void onServiceDisconnected(ComponentName className) {
			lpController  = null;
			Log.i("Service", "disConnected");
		}
	};
}