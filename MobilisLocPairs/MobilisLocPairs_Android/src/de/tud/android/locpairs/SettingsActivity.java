package de.tud.android.locpairs;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.tud.android.locpairs.controller.LocPairsController;
import de.tud.android.locpairs.model.Game;
import de.tud.android.locpairs.model.Settings;
import de.tud.android.mapbiq.R;
import de.tud.iiogis.wfs.WFSServer;
import de.tud.server.model.LocationModelAPI;

/**
 * This class represents the SettingsActivity. You can see the settings of the games here
 * and change them. Most of the parts are equivalent to the settings of the mapbiq-project.
 */
public class SettingsActivity extends Activity {
    
	/** The lp controller. */
	private LocPairsController lpController;
	
	/** The lp controller intent. */
	private Intent lpControllerIntent;
	
	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //set view to layout main.xml
        setContentView(R.layout.locpairs_settings);
        
        lpControllerIntent = new Intent(this, LocPairsController.class);
		bindService(lpControllerIntent, onService, BIND_AUTO_CREATE);
        
      //define the functionality of the Sample Data Button
        Button xmppButton = (Button) findViewById(R.id.XmppButton);
        xmppButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		lpController.initializeMXA();        	
	        }
    	});
        
        //playername
        String[] startgen={"Del","Bos","Los","Kin","Win", "Ste"};
        String[] addgen={"lov","ieb","er","ect","dows", "rot","angy", "bol", "oses", "tec", "fred", "fan"};
        Random generator = new Random();
        String genname = startgen[generator.nextInt(startgen.length)] + addgen[generator.nextInt(addgen.length)];
        String name = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).getString("playername", "not_set");
        if(name.equals("not_set"))
        {
        	name = genname;
        	Editor e = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).edit();
        	e.putString("playername", name);
            e.commit();
        }
        ((EditText) findViewById(R.id.entry)).setText(name);
        
        Button ok_name = (Button)this.findViewById(R.id.ok);
        ok_name.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText et = (EditText) findViewById(R.id.entry);
            	Editor e = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).edit();
            	e.putString("playername", et.getText().toString());
                e.commit();
                Game.getInstance().getClientPlayer().setPlayername(et.getText().toString());
                // Perform action on click
            }
        });
        Button cancel_name = (Button)this.findViewById(R.id.cancel);
        cancel_name.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String name = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).getString("playername", "Loser");
                ((EditText) findViewById(R.id.entry)).setText(name);
                // Perform action on click
            }
        });
        
    }
    
    /** The on service. */
    private ServiceConnection onService = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className,
				IBinder rawBinder) {
			lpController = ((LocPairsController.LocalBinder) rawBinder).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			lpController  = null;
		}
	};
	
	/**
	 * On server click.
	 * 
	 * @param v
	 *            the v
	 */
	public void onServerClick(View v) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Xmpp Server IP");
		alert.setMessage("Xmpp Server IP:");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setText(Settings.getInstance().getXmppServer());
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			Editable value = input.getText();
			Settings.getInstance().setXmppServer(value.toString());
			Editor e = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).edit();
        	e.putString("xmppserver", value.toString());
            e.commit();
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	 }
	
	/**
	 * On port click.
	 * 
	 * @param v
	 *            the v
	 */
	public void onPortClick(View v) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Xmpp Server Port");
		alert.setMessage("Xmpp Server Port");

		/// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setText("" + Settings.getInstance().getXmppPort());
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			Editable value = input.getText();
			Settings.getInstance().setXmppPort(new Integer(value.toString()));
			Editor e = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).edit();
        	e.putString("xmppport", value.toString());
            e.commit();
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	 }
	
	/**
	 * On lp click.
	 * 
	 * @param v
	 *            the v
	 */
	public void onLPClick(View v) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("locpairsAdress");
		alert.setMessage("locpairsAdress");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setText(Settings.getInstance().getLocpairsAddress());
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			Editable value = input.getText();
			Settings.getInstance().setLocpairsAddress(value.toString());
			Editor e = getSharedPreferences(Settings.sharedKey, Context.MODE_PRIVATE).edit();
        	e.putString("locpairsAdress", value.toString());
            e.commit();
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	 }
    
    /**
	 * Dotoast.
	 * 
	 * @param val
	 *            the val
	 */
    public void dotoast(String val) {
    	Toast toast = Toast.makeText(getBaseContext(), val, 5);
        toast.show();
    }
}