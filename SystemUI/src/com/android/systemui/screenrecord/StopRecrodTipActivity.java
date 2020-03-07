
package com.android.systemui.screenrecord;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import android.os.Bundle;
import android.view.View;
import android.app.Dialog;
import com.android.systemui.R;

public class StopRecrodTipActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "StopRecrodTipActivity";

    private Dialog mDialog;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mDialog = new Dialog(this);
        mDialog.setContentView(R.layout.stoprecordtip_layout);
        mDialog.setCancelable(false);
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mDialog.findViewById(R.id.messageDialog_btnRight).setOnClickListener(this);
        mDialog.findViewById(R.id.messageDialog_btnLeft).setOnClickListener(this);
        mDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.messageDialog_btnRight){
            //to recordServices
            sendBroadcast(new Intent("com.android..action.STOP_SCREEN_RECORD"));
        }
        if (mDialog != null) 
            mDialog.dismiss();
        finish();
    }

    
}
