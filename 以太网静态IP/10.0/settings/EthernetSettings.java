/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.ethernet;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.CheckBoxPreference;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.System;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.InetAddress;

import android.net.EthernetManager;
import android.net.StaticIpConfiguration;
import android.net.LinkAddress;
import android.net.IpConfiguration;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import android.os.Handler;
import android.database.ContentObserver; 
import android.net.Uri;
/*
	public static final int ETHERNET = 239;
    public static final int ETHERNET_STATIC = 240;
	K:\android6.0\kt8001_volte\6737_volte\frameworks\base\core\java\com\android\internal\logging\MetricsConstants.java

    */

public class EthernetSettings extends SettingsPreferenceFragment 
		implements Preference.OnPreferenceChangeListener{
			
    private static final String TAG = "EthernetSettings";

    private static final String USE_ETHERNET_SETTINGS = "ethernet";
	
    public static final String IS_ETHERNET_OPEN = Settings.IS_ETHERNET_OPEN;	
    private SwitchPreference mUseEthernet;
    private IntentFilter mIntentFilter;

    private boolean isEthernetEnabled() {
		return Settings.System.getInt(getActivity().getContentResolver(), IS_ETHERNET_OPEN,0) == 1 ? true : false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ethernet_settings);
		mUseEthernet = (SwitchPreference) findPreference(USE_ETHERNET_SETTINGS);
		mUseEthernet.setOnPreferenceChangeListener(this);
		if(isEthernetEnabled()) {
		   mUseEthernet.setChecked(true);
		} else {
		   mUseEthernet.setChecked(false);
		}
		/*File f = new File("sys/class/net/eth0/address");
		if (f.exists()) {
		   mUseEthernet.setEnabled(true);		
		} else {
		   mUseEthernet.setEnabled(false);
		}*/
    }
	
    @Override
    public void onResume() {
        super.onResume();
    }

     @Override
     public int getMetricsCategory(){return MetricsEvent.ETHERNET;}

     @Override
     public void onPause() {
        super.onPause();
    }	

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
    	boolean result = true;
    	final String key = preference.getKey();
    	if (USE_ETHERNET_SETTINGS.equals(key)) {
    		Settings.System.putInt(getActivity().getContentResolver(), IS_ETHERNET_OPEN, 
    			((Boolean) value) ? 1 : 0);
    	}

    	return result;
    }

    //for CheckBoxPreference
    /*@Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mUseEthernet) {
            boolean newState = mUseEthernet.isChecked();

            if (newState) {
                Settings.System.putInt(getActivity().getContentResolver(), IS_ETHERNET_OPEN,1);
            } else {
                Settings.System.putInt(getActivity().getContentResolver(), IS_ETHERNET_OPEN,0);
            }
        } 
        return super.onPreferenceTreeClick(preference);
    }*/

}

