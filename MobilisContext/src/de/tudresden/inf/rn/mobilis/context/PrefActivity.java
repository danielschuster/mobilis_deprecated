/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.context;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * 
 * @author Robert Lübke
 *
 */
public class PrefActivity extends PreferenceActivity {
	
	private boolean mobilisServerJidChanged,switchServiceChanged;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences page layout from an XML resource
        addPreferencesFromResource(R.layout.preferences);    
        
        
        // OnPreferenceChangeListener for MobilisServerJID
        Preference pref = findPreference(this.getString(R.string.pref_mobilisserver_key));
		pref.setOnPreferenceChangeListener(mobilisServerJidChangeListener);	
		// OnPreferenceChangeListener for switching UserContextService
		pref = findPreference(this.getString(R.string.pref_communication_switch_key));
		pref.setOnPreferenceChangeListener(switchServiceChangeListener);                
	}	
	
	@Override
	public void onStop() {
		super.onStop();
		if (mobilisServerJidChanged || switchServiceChanged) {
			//Send new ServiceDiscovery IQ
			XMPPManager.getInstance().sendServiceDiscoveryIQ();
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(this.getString(R.string.pref_communication_switch_key), false);
		}		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mobilisServerJidChanged=false;
	}
	
	
	private OnPreferenceChangeListener mobilisServerJidChangeListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {            
        	mobilisServerJidChanged=true;
        	return true;
        }	
	};
	
	private OnPreferenceChangeListener switchServiceChangeListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {            
        	if (newValue instanceof Boolean)
        		switchServiceChanged= (Boolean) newValue;
        	return true;
        }	
	};
	
	
}
