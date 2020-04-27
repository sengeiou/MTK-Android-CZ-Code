package com.android.eastereggs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.android.megg.MLandActivity;
import com.android.pegg.octo.Ocquarium;
import com.android.qegg.quares.QuaresActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSimCardNumber();
    }


    private void getSimCardNumber(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String line1Number = tm.getLine1Number();
        try {
            Intent phoneIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"));
            phoneIntent.putExtra("android.telecom.extra.START_CALL_WITH_VIDEO_STATE", 0x1 | 0x2);
            phoneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //mContext.startActivity(phoneIntent);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                   // String ui = android.os.SystemProperties.get("ro.volte.ui", "requested");
                }
            },2000);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.e("number","line1Number="+line1Number);
    }

    public void startPlatLogoQ(View view){
        Intent intent = new Intent(this, QuaresActivity.class);
        startActivity(intent);
        /*try {
            startActivity(new Intent(Intent.ACTION_MAIN)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    .addCategory("com.android.internal.category.PLATLOGO"));
        } catch (ActivityNotFoundException ex) {
            Log.e("PlatLogoActivity", "No more eggs.");
        }*/
    }

    public void startPlatLogoP(View view){
        Intent intent = new Intent(this, Ocquarium.class);
        startActivity(intent);
    }

    public void startPlatLogoM(View view){
        //Intent intent = new Intent(this, NekoActivationActivity.class);
        Intent intent = new Intent(this, MLandActivity.class);
        startActivity(intent);
    }
}
