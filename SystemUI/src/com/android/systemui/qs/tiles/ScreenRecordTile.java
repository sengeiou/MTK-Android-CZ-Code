/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.service.quicksettings.Tile;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.widget.Switch;
import android.util.Log;


import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

import android.content.Intent;
import android.util.DisplayMetrics;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import com.android.systemui.screenrecord.RecordService;
import com.android.systemui.statusbar.phone.SharedConfig;

public class ScreenRecordTile extends QSTileImpl<BooleanState> {

    private final String TAG = "ScreenRecordTile";

    private boolean mListening;

    public ScreenRecordTile(QSHost host) {
        super(host);
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        //Log.d(TAG, "handleSetListening listening = " + listening);
        if (mListening) {
            //mController.addCallback(mZenCallback);
        } else {
            //mController.removeCallback(mZenCallback);
        }
    }

     @Override
    public void handleClick() {
        //MetricsLogger.action(mContext, getMetricsCategory(), !mState.value);
        Log.d(TAG, "handleClick, RecordScreen enable = " + mState.value);
        
        boolean newState = !mState.value;
        refreshState(newState);
        if (mState.value) {       
            mContext.startActivity(new Intent("com.android.systemui.stoprecrodtipactivity")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
           
        }else{
           mContext.sendBroadcast(new Intent("com.android.action.START_SCREEN_RECORD"));
            
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mHost.collapsePanels();
                
            }
        },500);
    }

   @Override
    protected void handleLongClick() {
        handleClick();
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent();
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_screenrecord_label);
    }

    private boolean getRecordStatus(){
        return SharedConfig.getInstance(mContext).readBoolean(SharedConfig.KEY_SCREEN_RECORDING, false);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        Log.i(TAG, "handleUpdateState arg = " + arg);
        final boolean running = arg instanceof Boolean ? (Boolean)arg : getRecordStatus();
        state.value = running;
        state.label = mContext.getString(R.string.quick_settings_screenrecord_label);
        state.icon = ResourceIcon.get(R.drawable.ic_rs_on);
        if (state.slash == null) {
            state.slash = new SlashState();
        }
        state.slash.isSlashed = !running;
        state.state = running ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
        state.contentDescription = state.label;
        state.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_FLASHLIGHT;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_screen_record_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_screen_record_off);
        }
    }

}
