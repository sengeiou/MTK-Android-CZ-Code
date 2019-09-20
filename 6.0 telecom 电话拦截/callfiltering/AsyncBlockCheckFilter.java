/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.server.telecom.callfiltering;

import android.content.Context;
import android.os.AsyncTask;
import android.content.Intent;

import com.android.server.telecom.Call;
import android.util.Log;

/**
 * An {@link AsyncTask} that checks if a call needs to be blocked.
 * <p> An {@link AsyncTask} is used to perform the block check to avoid blocking the main thread.
 * The block check itself is performed in the {@link AsyncTask#doInBackground(Object[])}.
 */
public class AsyncBlockCheckFilter extends AsyncTask<String, Void, Boolean>{
    private final Context mContext;
    private final BlockCheckerAdapter mBlockCheckerAdapter;
    private Call mIncomingCall;
    private CallFilterResultCallback mCallback;
    private String blockedNumber;

    public AsyncBlockCheckFilter(Context context, BlockCheckerAdapter blockCheckerAdapter) {
        mContext = context;
        mBlockCheckerAdapter = blockCheckerAdapter;
    }

    public void startFilterLookup(Call call, CallFilterResultCallback callback) {
        mCallback = callback;
        mIncomingCall = call;
        String number = call.getHandle() == null ?
                null : call.getHandle().getSchemeSpecificPart();
        this.execute(number);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            blockedNumber = params[0];
            Log.e("InterceptInfos", "mIncomingCall, LogUtils.Events.BLOCK_CHECK_INITIATED ="+ blockedNumber);
            return mBlockCheckerAdapter.isBlocked(mContext, params[0]);
        } finally {
        }
    }

    @Override
    protected void onPostExecute(Boolean isBlocked) {
        try {
            CallFilteringResult result;
            if (isBlocked) {
                Log.e("InterceptInfos","blockedNumber=="+blockedNumber + " start add db..");
                addInterceptNumber();
                result = new CallFilteringResult(
                        false, // shouldAllowCall
                        true, //shouldReject
                        false, //shouldAddToCallLog
                        false // shouldShowNotification
                );
            } else {
                result = new CallFilteringResult(
                        true, // shouldAllowCall
                        false, // shouldReject
                        true, // shouldAddToCallLog
                        true // shouldShowNotification
                );
            }
            mCallback.onCallFilteringComplete(mIncomingCall, result);
        } finally {
        }
    }

    //add for intetcept telinfo to db
    public void addInterceptNumber(){
        android.content.ContentResolver contentResolver = mContext.getContentResolver();
        android.content.ContentValues newValues = new android.content.ContentValues();
        newValues.put("type", 1);//TEL
        newValues.put("number", blockedNumber);//number
        newValues.put("content", "");

        java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = new java.util.Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        newValues.put("time", time);

        contentResolver.insert(android.net.Uri.parse("content://cn.kaer.blockeddata/intercept"), newValues);

        Intent blockIntent = new Intent("cn.kaer.blockedNumber.intetcept");
        blockIntent.putExtra("number", blockedNumber);
        blockIntent.putExtra("time", time);
        mContext.sendBroadcast(blockIntent);

        android.util.Log.e("InterceptInfos","add db end...");
    }

}
