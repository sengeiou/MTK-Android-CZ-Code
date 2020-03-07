package com.android.systemui.screenrecord;

import android.app.Service;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Binder;
import android.util.Log;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.IMediaProjection;
import android.media.projection.MediaProjectionManager;
import android.media.projection.IMediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.R;
import android.graphics.PixelFormat;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.support.v4.content.FileProvider;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.net.Uri;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import com.android.systemui.statusbar.phone.SharedConfig;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;


public class RecordService extends Service {

    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;

    private MediaProjection mediaProjection;
    private MediaProjectionManager projectionManager;

    private WindowManager mWindowManager;

    private boolean running;
    private int width = 800;
    private int height = 1280;
    private int dpi = 320;

    private final String TAG = "ScreenRecordService";

    @Override
    public IBinder onBind(Intent intent) {
        return new RecordBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "RecordService onCreate...");
        HandlerThread serviceThread = new HandlerThread("service_thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        running = false;

        screenRecordBroadcastReceiver = new ScreenRecordBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_START);
        intentFilter.addAction(ACTION_STOP);
        registerReceiver(screenRecordBroadcastReceiver, intentFilter);

        initConfig();
      
    }

    @Override
    public void onDestroy() {
        try{
            unregisterReceiver(screenRecordBroadcastReceiver);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
        Log.e(TAG, "RecordService onDestroy...");
    }

    private void initMediaProjection(){
        try {
            IBinder b = ServiceManager.getService(Context.MEDIA_PROJECTION_SERVICE);
            IMediaProjectionManager mService = IMediaProjectionManager.Stub.asInterface(b);
            String mPackageName = "com.android.systemui";
            ApplicationInfo aInfo = getPackageManager().getApplicationInfo(mPackageName, 0);
            IMediaProjection projection = mService.createProjection(aInfo.uid, mPackageName,
                 MediaProjectionManager.TYPE_SCREEN_CAPTURE, true);
            mediaProjection = new MediaProjection(RecordService.this, 
                  IMediaProjection.Stub.asInterface(projection.asBinder()));
        } catch (Exception e) {
            Log.e(TAG, "initMediaProjection happen some Exception", e);
            e.printStackTrace();
        }
        //projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        //mediaProjection = mProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    private ScreenRecordBroadcastReceiver screenRecordBroadcastReceiver;

    private static String ACTION_START = "com.android.action.START_SCREEN_RECORD";
    private static String ACTION_STOP  = "com.android.action.STOP_SCREEN_RECORD";

    private class ScreenRecordBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                String action = intent.getAction();
                Log.e(TAG, "screenRecordBroadcastReceiver action==" + action);

                if (ACTION_START.equals(action)) {
                    //startRecord();
                    showCountDownWindow();
                } else if (ACTION_STOP.equals(action)) {
                    stopRecord();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void initConfig() {
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = mWindowManager.getDefaultDisplay();

        Point point = new Point();
        defaultDisplay.getRealSize(point);

        DisplayMetrics outMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(outMetrics);

        this.width = point.x;
        this.height = point.y;
        this.dpi = outMetrics.densityDpi;
    }

    private void saveRunningState(){
        SharedConfig.getInstance(RecordService.this).writeData(SharedConfig.KEY_SCREEN_RECORDING, running);
        setStatusBarColor();
    }

    private void setStatusBarColor(){
        sendBroadcast(new Intent("com.android.action.SET_STATUSBAR_COLOR").putExtra("is_recording", running));
    }

    public boolean startRecord() {
        if (inflateWindow != null && isShowCountDownView) {
            mWindowManager.removeViewImmediate(inflateWindow);
            inflateWindow = null;
            isShowCountDownView =  false;
        }

        initMediaProjection();

        if (mediaProjection == null || running) {
            return false;
        }

        initRecorder();
        createVirtualDisplay();
        mediaRecorder.start();
        running = true;
        saveRunningState();
        return true;
    }

    public boolean stopRecord() {
        if (!running) {
            return false;
        }
        running = false;
        saveRunningState();
        mediaRecorder.stop();
        mediaRecorder.reset();
        virtualDisplay.release();
        mediaProjection.stop();
        showHeadUpNotification();
        return true;
    }

    private void createVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
    }

    private void initRecorder() {
      if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(getSaveOutputFilePath());
        mediaRecorder.setVideoSize(width, height);
        Log.e(TAG, "width=" + width + " height=" + height);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncodingBitRate((int)(width * height * 3.6));
        mediaRecorder.setVideoFrameRate(20);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String recordFilePath = "default.mp4";
    public String getSaveOutputFilePath() {
          String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ScreenRecord/";
          File file = new File(rootDir);
          if (!file.exists()) {
              file.mkdirs();
          }

          String fileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
          recordFilePath = rootDir + fileName + ".mp4";
          Log.i(TAG, "recordFilePath=" + recordFilePath);
          return recordFilePath;
    }

    private void showHeadUpNotification(){
        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = java.util.UUID.randomUUID().toString().replaceAll("-", "");
        NotificationChannel notificationChannel = new NotificationChannel(channelId,
                "recordScreen", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setSound(null, null);//mute
        mNotificationManager.createNotificationChannel(notificationChannel);

        Notification.Builder builder = new Notification.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_rs_on);
        builder.setSubText(getResources().getString(R.string.record_notification_subtext));
        builder.setAutoCancel(true);
        builder.setContentText(getResources().getString(R.string.record_notification_contentext));

        //for goto gallery
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File recordFile = new File(recordFilePath);
        Uri uriForFile = FileProvider.getUriForFile(this,"com.android.systemui.fileprovider", recordFile);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uriForFile, "video/*");

        //scan media file to gallery
        MediaScannerConnection.scanFile(this, new String[]{recordFile.getAbsolutePath()},
                new String[]{"video/*"}, null);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        final int notifyId = 100;
        mNotificationManager.notify(notifyId, builder.build());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mNotificationManager.cancel(notifyId);
            }
        },4500);

    }

    private void showCountDownWindow() {
        if (isShowCountDownView || inflateWindow != null || running) return;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 500;
        layoutParams.height = 500;

        inflateWindow = LayoutInflater.from(RecordService.this).inflate(R.layout.countdown_layout, null);
        TextView animNumberTv = (TextView) inflateWindow.findViewById(R.id.tv_number_anim);

        mWindowManager.addView(inflateWindow, layoutParams);
        isShowCountDownView = true;

        doCountDownAnim(animNumberTv);
    }

    View inflateWindow;
    boolean isShowCountDownView;
    int sCurCount = 3;
    int repeatCount = 2;
    private void doCountDownAnim(final TextView animationViewTv){
        animationViewTv.setText(String.valueOf(sCurCount));
        animationViewTv.setVisibility(View.VISIBLE);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.1f, 1.3f, 0.1f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        scaleAnimation.setRepeatCount(repeatCount);
        alphaAnimation.setRepeatCount(repeatCount);
        alphaAnimation.setDuration(1000);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationViewTv.setVisibility(View.GONE);
                startRecord();
                sCurCount = 3;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                animationViewTv.setText(String.valueOf(--sCurCount));
            }
        });

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationViewTv.startAnimation(animationSet);
    }

    public class RecordBinder extends Binder {
        public RecordService getRecordService() {
            return RecordService.this;
        }
    }
}