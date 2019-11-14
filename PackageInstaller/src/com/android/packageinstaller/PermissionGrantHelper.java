
package com.android.packageinstaller;

import android.content.Context;
import android.util.Log;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.android.packageinstaller.permission.model.AppPermissionGroup;
import com.android.packageinstaller.permission.model.AppPermissions;
import com.android.packageinstaller.permission.model.Permission;
import com.android.packageinstaller.permission.utils.ArrayUtils;
import com.android.packageinstaller.permission.utils.Utils;
import java.util.List;


public class PermissionGrantHelper{

	//pjz 20190404 [S]
    public static void slientGrantRuntimePermission(Context context, String packageName){
        PackageInfo packageInfo;

        try {
            packageInfo =  context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("permission", "can't get PackageInfo for packageName="+ packageName);
            return;
        }
        
       AppPermissions  mAppPermissions = new AppPermissions(context, packageInfo, null, false,
                new Runnable() {
                    @Override
                    public void run() {
                        
                    }
                });

       Log.e("permission", " AppPermissionGroup size=="+mAppPermissions.getPermissionGroups().size());
       if (mAppPermissions.getPermissionGroups().isEmpty()) {
            Log.e("permission", "mAppPermissions size isEmpty");
            return;
        }
        for (AppPermissionGroup group : mAppPermissions.getPermissionGroups()) {
            String[] permissionsToGrant = null;
            final int permissionCount = group.getPermissions().size();
            for (int j = 0; j < permissionCount; j++) {
                final Permission permission = group.getPermissions().get(j);
                if (!permission.isGranted()) {
                    permissionsToGrant = ArrayUtils.appendString(
                            permissionsToGrant, permission.getName());
                     Log.e("permission", "permissionName=" + permission.getName());
                }
            }
            if (permissionsToGrant != null) {
                group.grantRuntimePermissions(false, permissionsToGrant);
                Log.i("permission", "grantRuntimePermissions permissionsToGrant");
                //group.revokeRuntimePermissions(false);
            }
            //group.resetReviewRequired();
        }
    }
    //[E]
	
}