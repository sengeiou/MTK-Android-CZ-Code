/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.systemui.statusbar.phone;

import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedConfig {
    
    private SharedConfig(){};
    private static SharedConfig instance;
    private static final String CONFIG_FILE_NAME =  "CONFIG";
    private SharedPreferences sharedPreferences;
    private Editor editor;

    public static final String KEY_PANEL_BAR = "KEY_PANEL_BAR";
    public static final String KEY_PANEL_GONE = "KEY_PANEL_GONE";
    public static final String KEY_NAVIGATION_BAR = "KEY_NAVIGATION_BAR";
    public static final String KEY_NAVIGATION_BUTTON = "KEY_NAVIGATION_BUTTON";
    public static final String KEY_SCREEN_RECORDING = "KEY_SCREEN_RECORDING";
    
    private SharedConfig(Context context) {
        sharedPreferences = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static SharedConfig getInstance(Context context) {
		if (instance == null) {
			instance = new SharedConfig(context);
		}
		return instance;
	}

	public boolean writeData(String key, boolean value) {
		Log.e("systemui", "put   key=" + key + "   value="+value);
		boolean result = false;
		editor.putBoolean(key, value);
		result = editor.commit();
		return result;
	}

	public boolean readBoolean(String key, boolean def) {
		boolean result = sharedPreferences.getBoolean(key, def);
		Log.e("systemui", "get   key=" + key + "   value="+result);
		return result;
	}

	public boolean writeData(String key, String value) {
		Log.e("systemui", "put   key=" + key + "   value="+value);
		boolean result = false;
		editor.putString(key, value);
		result = editor.commit();
		return result;
	}

	public String readString(String key, String def) {
		String result = sharedPreferences.getString(key, def);
		Log.e("systemui", "get   key=" + key + "   value="+result);
		return result;
	}


}
