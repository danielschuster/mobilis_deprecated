package de.tudresden.inf.rn.mobilis.context;

import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends TabActivity {
	
	/** The TAG for the Log. */
	private final static String TAG = "MainActivity";
	
	//XMPP
	private static XMPPManager xmppManager;
	
	private static MainActivity instance;
	
	public static MainActivity getInstance() {
		if(instance==null) {
			instance = new MainActivity(); 
		}
		return instance;
	}
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
              
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, MoodActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("mood").setIndicator("Mood",
                          res.getDrawable(R.layout.ic_tab_mood))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, TuneActivity.class);
        spec = tabHost.newTabSpec("tune").setIndicator("Tune",
                          res.getDrawable(R.layout.ic_tab_tune))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, LocationActivity.class);
        spec = tabHost.newTabSpec("location").setIndicator("Location",
                          res.getDrawable(R.layout.ic_tab_location))
                      .setContent(intent);
        tabHost.addTab(spec);
        
//        intent = new Intent().setClass(this, SubscriptionActivity.class);
//        spec = tabHost.newTabSpec("subscrptions").setIndicator("Subscribe",
//                          res.getDrawable(R.layout.ic_tab_subscribe))
//                      .setContent(intent);
//        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
        
                 
        MainActivity.xmppManager = XMPPManager.getInstance();
        xmppManager.setMainActivity(this);        
        //Connect application to MXA 
    	xmppManager.connectToMXA();
    }
            
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.options_menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;
        // Handle item selection
    	switch (item.getItemId()) {
    	
        case R.id.menu_pref:      	  	
            this.startActivity(new Intent(this.getApplicationContext(), PrefActivity.class));
            return true;
        case R.id.menu_pref_xmpp:
        	i = new Intent(ConstMXA.INTENT_PREFERENCES);
        	this.startActivity(Intent.createChooser(i, "MXA not found. Please install."));
      	  	return true;        
        case R.id.menu_publish:
        	this.startActivity(new Intent(this.getApplicationContext(), PublishActivity.class));           
      	  	return true; 
        case R.id.menu_subscribe:
        	this.startActivity(new Intent(this.getApplicationContext(), SubscribeActivity.class)); 
      	  	return true; 
        default:
        	return super.onOptionsItemSelected(item);
    	}
    }
        
    /** Shows a short Toast message on the map */
    public void makeToast(String text) {
    	Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
    
}