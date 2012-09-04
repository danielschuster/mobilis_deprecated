package de.tud.android.locpairs;

import de.tud.android.mapbiq.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * Instructions. Here the rules for the game are explained.
 */
public class InstructionsActivity extends Activity {
	
	/**
	 * Called when the activity is first created.
	 * 
	 * @param instruct
	 *            the instructions
	 */
	@Override
	public void onCreate(Bundle instruct) {
		super.onCreate(instruct);
		setContentView(R.layout.locpairs_instructions);
	}
}

