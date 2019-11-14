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

package com.android.packageinstaller.permission.ui;

import android.app.Activity;

import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.packageinstaller.DeviceUtils;
import com.android.packageinstaller.R;
import com.android.packageinstaller.permission.ui.handheld.ReviewPermissionsFragment;
import com.android.packageinstaller.permission.ui.ConfirmActionDialogFragment.OnActionConfirmedListener;
import com.android.packageinstaller.permission.ui.wear.ReviewPermissionsWearFragment;
import com.android.packageinstaller.permission.model.AppPermissionGroup;
import com.android.packageinstaller.permission.model.AppPermissions;
import com.android.packageinstaller.permission.model.Permission;
import com.android.packageinstaller.permission.ui.ConfirmActionDialogFragment;
import com.android.packageinstaller.permission.ui.ManagePermissionsActivity;
import com.android.packageinstaller.permission.utils.ArrayUtils;
import com.android.packageinstaller.permission.utils.Utils;
import android.util.Log;
import java.util.List;
import android.os.RemoteCallback;


public final class ReviewPermissionsActivity extends Activity
        implements OnActionConfirmedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageInfo packageInfo = getTargetPackageInfo();
        if (packageInfo == null) {
            Log.e("ReviewPermissionsActivity", "packageInfo isNull");
            finish();
            return;
        }

        if (DeviceUtils.isWear(this)) {
            Fragment fragment = ReviewPermissionsWearFragment.newInstance(packageInfo);
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment).commit();
        } else {
            //setContentView(R.layout.review_permissions);
            if (getFragmentManager().findFragmentById(R.id.preferences_frame) == null) {
                /*getFragmentManager().beginTransaction().add(R.id.preferences_frame,
                        ReviewPermissionsFragment.newInstance(packageInfo)).commit();*/
                //pjz 20190404
                Log.e("ReviewPermissionsActivity", "pkgName=" + packageInfo.packageName);
                slientGrantRuntimePermission(packageInfo);
                /*if (packageInfo.packageName.contains("kaer") || packageInfo.packageName.contains("com.android")) {
                    slientGrantRuntimePermission(packageInfo);
                }else{
                    setContentView(R.layout.review_permissions);
                    getFragmentManager().beginTransaction().add(R.id.preferences_frame, 
                        ReviewPermissionsFragment.newInstance(packageInfo)).commit();
                } */       
            }
        }
    }

    //pjz 20190404 set activity theme translucent when show
    @Override
    public void setTheme(int resid) {
        super.setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        /*String packageName = getIntent().getStringExtra(Intent.EXTRA_PACKAGE_NAME);
        if(packageName.contains("kaer") || packageName.contains("com.android")){
            Log.e("ReviewPermissionsActivity", "setTheme Theme_Translucent_NoTitleBar");
            super.setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        }else{
            Log.e("ReviewPermissionsActivity", "setTheme ActivityNormal");
            super.setTheme(R.style.ActivityNormal);
        }*/
    }

    @Override
    public void onActionConfirmed(String action) {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.preferences_frame);
        if (fragment instanceof OnActionConfirmedListener) {
            ((OnActionConfirmedListener) fragment).onActionConfirmed(action);
        }
    }

    private PackageInfo getTargetPackageInfo() {
        String packageName = getIntent().getStringExtra(Intent.EXTRA_PACKAGE_NAME);
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        try {
            return getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }


    //pjz 20190404 [S]
    public void slientGrantRuntimePermission(PackageInfo packageInfo){
        
       AppPermissions  mAppPermissions = new AppPermissions(this, packageInfo, null, false,
                new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });

       Log.e("ReviewPermissionsActivity", " AppPermissionGroup size=="+mAppPermissions.getPermissionGroups().size());
       if (mAppPermissions.getPermissionGroups().isEmpty()) {
            Log.e("ReviewPermissionsActivity", "mAppPermissions size isEmpty");
            finish();
            return;
        }
        for (AppPermissionGroup group : mAppPermissions.getPermissionGroups()) {
            String[] permissionsToGrant = null;
            final int permissionCount = group.getPermissions().size();
            for (int j = 0; j < permissionCount; j++) {
                final Permission permission = group.getPermissions().get(j);
                if (permission.isReviewRequired()) {
                    permissionsToGrant = ArrayUtils.appendString(
                            permissionsToGrant, permission.getName());
                     Log.e("ReviewPermissionsActivity", "permissionName=" + permission.getName());
                }
            }
            if (permissionsToGrant != null) {
                group.grantRuntimePermissions(false, permissionsToGrant);
                //group.revokeRuntimePermissions(false);
            }
            group.resetReviewRequired();
        }

        executeCallback(true);
    }

    private void executeCallback(boolean success) {
        if (success) {
            IntentSender intent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
            if (intent != null) {
                try {
                    int flagMask = 0;
                    int flagValues = 0;
                    if (getIntent().getBooleanExtra(
                            Intent.EXTRA_RESULT_NEEDED, false)) {
                        flagMask = Intent.FLAG_ACTIVITY_FORWARD_RESULT;
                        flagValues = Intent.FLAG_ACTIVITY_FORWARD_RESULT;
                    }
                    startIntentSenderForResult(intent, -1, null,
                            flagMask, flagValues, 0);
                } catch (IntentSender.SendIntentException e) {
                        /* ignore */
                }
                finish();
                return;
            }
        }

        RemoteCallback callback = getIntent().getParcelableExtra(
                Intent.EXTRA_REMOTE_CALLBACK);
        if (callback != null) {
            Bundle result = new Bundle();
            result.putBoolean(Intent.EXTRA_RETURN_RESULT, success);
            callback.sendResult(result);
        }
        finish();
    }

    //[E]
}
