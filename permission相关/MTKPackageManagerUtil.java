/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.server.pm;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import android.content.pm.IPackageManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.Manifest;
import com.android.internal.util.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

public class MTKPackageManagerUtil{

    private static String TAG = "MTKPackageManagerUtil";

    //copy from vendor\mediatek\proprietary\frameworks\base\services\core\java\com\mediatek\server\pm\PmsExtImpl.java
    private static final File GRANT_SYS_APP_LIST_SYSTEM = Environment
            .buildPath(Environment.getRootDirectory(), "etc", "permissions",
                    "pms_sysapp_grant_permission_list.txt");

    private static HashSet<String> sGrantSystemAppSet = new HashSet<String>();

    private static HashSet<String> sGrantPermissionSet = new HashSet<String>();

    private static  IPackageManager mIpm;
    private static  AppOpsManager mAppOpsManager;

    public static void slientGrantRuntimePermission(Context mContext, Settings mSettings){
        sGetGrantSystemAppFromFile(sGrantSystemAppSet, GRANT_SYS_APP_LIST_SYSTEM);

        PackageManager mPackageManager = mContext.getPackageManager();

        mIpm = AppGlobals.getPackageManager();
        mAppOpsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);

        Iterator<String> it = sGrantSystemAppSet.iterator();
        Log.d(TAG, "sGrantSystemAppSet:");
        while (it.hasNext()) {
            sGrantPermissionSet.clear();
            String pkgName = it.next();
            Log.d(TAG, "pkgName="+pkgName);
            try {
                PackageInfo mPackageInfo =   mPackageManager.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
                 for (String permission : mPackageInfo.requestedPermissions){
                     int status = mPackageManager.checkPermission(permission, pkgName);
                     final BasePermission bp = mSettings.mPermissions.get(permission);
                     if (status != PackageManager.PERMISSION_GRANTED && bp != null) {
                        if (!bp.isRuntime() && !bp.isDevelopment()) {
                            Log.d(TAG, "Permission " + bp.name + " is not a changeable permission type");
                            continue;
                        }
                        sGrantPermissionSet.add(permission);
                     }
                 }
                 Log.d(TAG, " need grantRuntimePermission size:"+sGrantPermissionSet.size());
                 for (String permission : sGrantPermissionSet) {
                    mPackageManager.grantRuntimePermission(pkgName,
                            permission, Process.myUserHandle());
                 }

                 if (checkInstallPackagesPermission(pkgName, mPackageInfo)) {
                     Log.e(TAG, pkgName + " need grant INSTALL_PACKAGES permission");
                     mAppOpsManager.setMode(AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                        mPackageInfo.applicationInfo.uid, pkgName, AppOpsManager.MODE_ALLOWED);
                     Log.e(TAG, "grant INSTALL_PACKAGES permission done");
                 }
            } catch (Exception e) {
                //e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }

        }

    }

    private static boolean checkInstallPackagesPermission(String packageName, PackageInfo mPackageInfo){
        int uid = mPackageInfo.applicationInfo.uid;
        //boolean permissionGranted = hasPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES, uid);
        boolean permissionRequested = hasRequestedAppOpPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES, packageName);
        int appOpMode = getAppOpMode(AppOpsManager.OP_REQUEST_INSTALL_PACKAGES, uid, packageName);

        return appOpMode != AppOpsManager.MODE_DEFAULT || permissionRequested;
    }

    private static int getAppOpMode(int appOpCode, int uid, String packageName) {
        return mAppOpsManager.checkOpNoThrow(appOpCode, uid, packageName);
    }

    private static boolean hasRequestedAppOpPermission(String permission, String packageName) {
        try {
            String[] packages = mIpm.getAppOpPermissionPackages(permission);
            return ArrayUtils.contains(packages, packageName);
        } catch (Exception exc) {
            Log.e(TAG, "PackageManager dead. Cannot get permission info");
            return false;
        }
    }

    private static boolean hasPermission(String permission, int uid) {
        try {
            int result = mIpm.checkUidPermission(permission, uid);
            return result == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            Log.e(TAG, "PackageManager dead. Cannot get permission info");
            return false;
        }
    }

    /**
     * Get removable system app list from config file
     *
     * @param resultSet
     *            Returned result list
     * @param file
     *            The config file
     */
    private static void sGetGrantSystemAppFromFile(
            HashSet<String> resultSet, File file) {
        resultSet.clear();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            if (file.exists()) {
                fr = new FileReader(file);
            } else {
                Log.d(TAG, "file in " + file + " does not exist!");
                return;
            }
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!TextUtils.isEmpty(line)) {
                    Log.d(TAG, "read line " + line);
                    resultSet.add(line);
                }
            }
            Log.e(TAG,"GRANT_SYS_APP_LIST_SYSTEM size="+resultSet.size());
        } catch (Exception io) {
            Log.d(TAG, io.getMessage());
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException io) {
                Log.d(TAG, io.getMessage());
            }
        }
    }
}