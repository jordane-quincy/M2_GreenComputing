package com.github.jordane_quincy.m2_greencomputing;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

/**
 * Created by jordane on 06/04/17.
 */

public class RecordService extends Service {

    private static final String TAG = RecordService.class.getSimpleName();
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    private final int MILLE_VINGT_QUATRE = 1024;
    private final int BYTES_TO_MO = MILLE_VINGT_QUATRE * MILLE_VINGT_QUATRE;
    ActivityManager activitymanager;
    Debug.MemoryInfo memoryInfo;
    private NotificationManager mNM;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 42;//R.string.local_service_started;

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("getText(R.string.notification_title)")
                .setContentText("getText(R.string.notification_message)")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .setTicker("getText(R.string.ticker_text)")
                .build();

        startForeground(42, notification);

        initLogging();

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();

//        OneSignal.appContext = this.getApplicationContext();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                OneSignal.appId = OneSignal.getSavedAppId();
//
//                OneSignalStateSynchronizer.initUserState(OneSignal.appContext);
//                OneSignalStateSynchronizer.syncUserState(true);
//                checkOnFocusSync();
//
//                stopSelf();
//            }
//        }, "OS_SYNCSRV_ONCREATE").start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        launchLogging();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, "R.string.local_service_stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Starteed"; //getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                //new Intent(this, LocalServiceActivities.Controller.class), 0);
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
//                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentTitle("R.string.local_service_label")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    private void initLogging() {
        Log.d(TAG, "initLogging");

        //TODO: trouver un autre moyen de montrer que le service a bien été lancé.
        openWebPage("http://www.google.com?q=" + Calendar.getInstance().getTimeInMillis());
        activitymanager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

    }

    private void launchLogging() {
        Log.d(TAG, "launchLogging");

//        ActivityManager activitymanager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lstRunningApp = activitymanager.getRunningAppProcesses();

//        sb.append(runningApp.processName);
        for (ActivityManager.RunningAppProcessInfo runningApp : lstRunningApp) {
            if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND == runningApp.importance) {
                int[] pids = {runningApp.pid};
                Debug.MemoryInfo[] memoryInfos = activitymanager.getProcessMemoryInfo(pids);
                memoryInfo = memoryInfos[0];
//                Log.d(TAG, convertKbToMo(memoryInfo.getTotalPrivateDirty()) +", "+ convertKbToMo(memoryInfo.getTotalPss()) +", "+ convertKbToMo(memoryInfo.getTotalPrivateClean()));
                break;
            }

        }

//        RAM
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        Log.d(TAG, convertbytesToMo(memoryInfo.availMem) + "," + convertbytesToMo(memoryInfo.totalMem) + "," + convertbytesToMo(memoryInfo.totalMem - memoryInfo.availMem));

    }

    private long convertbytesToMo(long bytes) {
        return bytes / BYTES_TO_MO;
    }

    private void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //imperatif car on est dans un service (useless dans une activity)
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        RecordService getService() {
            return RecordService.this;
        }
    }
}
