package de.tudresden.inf.rn.mobilis.android;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

/**
 * Dialog which stays for 8 seconds at application start.
 * @author Dirk
 */
public class SplashScreen extends Dialog {
    
	private final int SPLASH_DISPLAY_LENGHT = 8000;
	
    public SplashScreen(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
	}

    @Override
    public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.splashscreen);
         
         /* New Handler to close this Splash-Screen after SPLASH_DISPLAY_LENGHT. */
         new Handler().postDelayed(new Runnable(){
              @Override
              public void run() {
                   /* Create an Intent that will start the Menu-Activity. */
                   SplashScreen.this.dismiss();
              }
         }, SPLASH_DISPLAY_LENGHT);
    }
}