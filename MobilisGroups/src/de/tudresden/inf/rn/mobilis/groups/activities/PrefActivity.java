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
package de.tudresden.inf.rn.mobilis.groups.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.groups.ApplicationManager;
import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.groups.XMPPManager;

/**
 * 
 * @author Robert Lübke
 *
 */
public class PrefActivity extends PreferenceActivity {
	
	private PrefActivity prefActivity;
	private boolean personalPrefChanged,mobilisServerJidChanged,switchServiceChanged;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefActivity=this;   
        this.setTitle(R.string.app_name_prefactivity);
        // Load the preferences page layout from an XML resource
        addPreferencesFromResource(R.xml.preferences);        
        
        // OnPreferenceChangeListener's for social network settings      
		Preference pref = findPreference(this.getString(R.string.pref_overlays_foursquare_key));
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		        @Override
		        public boolean onPreferenceChange(Preference preference, Object newValue) {
		        	Boolean newVal = (Boolean) newValue;
		            ApplicationManager.getInstance().getMainActivity().setFoursquareLayerChecked(newVal);
		            return true;
		        }	
		});
		
//		pref = findPreference(this.getString(R.string.pref_overlays_gowalla_key));
//		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//		        @Override
//		        public boolean onPreferenceChange(Preference preference, Object newValue) {
//		            Boolean newVal = (Boolean) newValue;
//		            ApplicationManager.getInstance().getMainActivity().setGowallaLayerChecked(newVal);
//		            return true;
//		        }	
//		});
		
		pref = findPreference(this.getString(R.string.pref_overlays_foursquare_limit_key));
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		        @Override
		        public boolean onPreferenceChange(Preference preference, Object newValue) {
		            if (newValue instanceof String) {
		            	int limit = Integer.valueOf(((String) newValue));
		            	if (limit < 1 || limit > 50) {
		            		Toast.makeText(prefActivity, "Limit must be a number between 1 and 50.", Toast.LENGTH_SHORT).show();
		            		//Return false to keep the old value
		            		return false;
		            	}
		            }
		            return true;
		        }	
		});
		
		// OnPreferenceChangeListener for MobilisServerJID
		pref = findPreference(this.getString(R.string.pref_mobilisserver_key));
		pref.setOnPreferenceChangeListener(mobilisServerJidChangeListener);	
		// OnPreferenceChangeListener for switching Grouping Service
		pref = findPreference(this.getString(R.string.pref_communication_switch_key));
		pref.setOnPreferenceChangeListener(switchServiceChangeListener);	
		
		// OnPreferenceChangeListener's for personal information   
		
		pref = findPreference(this.getString(R.string.pref_personal_information_realname_key));
		pref.setOnPreferenceChangeListener(personalPrefChangeListener);		
		pref = findPreference(this.getString(R.string.pref_personal_information_age_key));
		pref.setOnPreferenceChangeListener(personalPrefChangeListener);	
		pref = findPreference(this.getString(R.string.pref_personal_information_city_key));
		pref.setOnPreferenceChangeListener(personalPrefChangeListener);	
		pref = findPreference(this.getString(R.string.pref_personal_information_email_key));
		pref.setOnPreferenceChangeListener(personalPrefChangeListener);	
		pref = findPreference(this.getString(R.string.pref_personal_information_homepage_key));
		pref.setOnPreferenceChangeListener(personalPrefChangeListener);
	}	
	
	@Override
	public void onStop() {
		super.onStop();
		if (personalPrefChanged) {
			//Send new GroupMemberInfoBean with updated preferences
			XMPPManager.getInstance().sendGroupMemberInfoBeanSet(null);	
		}
		if (mobilisServerJidChanged || switchServiceChanged) {
			//Send new ServiceDiscovery IQ
			XMPPManager.getInstance().sendServiceDiscoveryIQ();
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(this.getString(R.string.pref_communication_switch_key), false);
			
		}		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		personalPrefChanged=false;	
		mobilisServerJidChanged=false;
	}
	
	private OnPreferenceChangeListener personalPrefChangeListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
        	personalPrefChanged=true;
        	return true;
        }	
	};
	
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
