package de.tudresden.inf.rn.mobilis.groups.activities;

import de.tudresden.inf.rn.mobilis.groups.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

/**
 * 
 * @author Robert Lübke
 */
public class SplashScreen extends Dialog {
    
	private final int SPLASH_DISPLAY_DELAY_MILLIS = 2500;
	
	private MainActivity mainActivity;
	
    public SplashScreen(MainActivity mainActivity) {
		super(mainActivity);
		this.mainActivity=mainActivity;
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
	}

    @Override
    public void onCreate(Bundle b) {
         super.onCreate(b);
         setContentView(R.layout.splashscreen);
                  
         /* New Handler to close this Splash-Screen after SPLASH_DISPLAY_DELAY_MILLIS. */
         new Handler().postDelayed(new Runnable(){
              @Override
              public void run() {                   
                   SplashScreen.this.dismiss();
              }
         }, SPLASH_DISPLAY_DELAY_MILLIS);
    }
    
    
    @Override
    public void onStop() {
         super.onStop();
         mainActivity.onSplashScreenStop();
    }
}