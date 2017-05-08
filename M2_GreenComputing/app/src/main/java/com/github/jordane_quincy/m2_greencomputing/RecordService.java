package com.github.jordane_quincy.m2_greencomputing;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Debug;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by jordane on 06/04/17.
 */

public class RecordService extends Service {

    private static final String TAG = RecordService.class.getSimpleName();
    //The thread have to wait SLEEP_TIME_FOR_THREAD_IN_MILLIS milliseconds before new loop
    private static final long SLEEP_TIME_FOR_THREAD_IN_MILLIS = 2000;
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    private final int MILLE_VINGT_QUATRE = 1024;
    private final int BYTES_TO_MO = MILLE_VINGT_QUATRE * MILLE_VINGT_QUATRE;
    private final int FILE_SIZE_LIMIT_IN_MO = 1;


    ActivityManager activityManager;
    Debug.MemoryInfo memoryInfo;
    CpuInfo cpuInfo;

    boolean shouldContinue = true;
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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        launchLogging();

//        Pour relancer automatiquement le service (sans l'intent utilisé initialement)
//        https://developer.android.com/reference/android/app/Service.html#START_STICKY
        return Service.START_NOT_STICKY; //FIXME: mettre START_STICKY une fois le debug done
    }

    @Override
    public void onDestroy() {

        // stop the thread
        shouldContinue = false;

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
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        cpuInfo = initCpuInfo();

    }

    private void launchLogging() {
        Log.d(TAG, "launchLogging");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                String[] infos = new String[16];

                ActivityManager.RunningAppProcessInfo appForeground = null;
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

                LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                Sensor sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);


                FileOutputStream outputStream = null;
                try {
                    File recordFile = new File(getApplicationContext().getFilesDir(), "recordServiceFile.txt");
                    Log.d(TAG, "create recordFile : " + recordFile);

                    outputStream = new FileOutputStream(recordFile, false); //not in append mode
                } catch (Exception e) {
                    Log.e(TAG, "impossible to create FileOutputStream " + e);
                }

                while (shouldContinue) {

                    //Running application
                    for (ActivityManager.RunningAppProcessInfo runningApp : activityManager.getRunningAppProcesses()) {
                        if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND == runningApp.importance) {
                            appForeground = runningApp;
                            sb.append("\n").append("appForeground : ").append(appForeground.processName);
                            infos[0] = appForeground.processName;
                            break;
                        }
                    }

                    //Ram / Memory
                    activityManager.getMemoryInfo(memoryInfo);

                    sb.append("\n").append("availMem : ").append(memoryInfo.availMem);
                    infos[1] = String.valueOf(memoryInfo.availMem);
                    sb.append(" (").append(convertbytesToMo(memoryInfo.availMem)).append(" Mo)");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        //non disponible avant Android API 16
                        sb.append("\n").append("totalMem : ").append(memoryInfo.totalMem);
                        infos[2] = String.valueOf(memoryInfo.totalMem);
                    }

                    //CPU
                    updateCpuInfo();
                    sb.append("\n").append("cpuInfo : ").append(cpuInfo);
                    infos[3] = String.valueOf(cpuInfo.getMinFreq());
                    infos[4] = String.valueOf(cpuInfo.getMaxFreq());
                    infos[5] = String.valueOf(cpuInfo.getCurFreq());

                    //Location
                    boolean isLocationEnabledGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean isLocationEnabledNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    boolean isLocationEnabledPassive = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
                    sb.append("\n").append("location ").append("GPS ? ").append(isLocationEnabledGps).append(", network ? ").append(isLocationEnabledNetwork).append(", passive ? ").append(isLocationEnabledPassive);
                    infos[6] = String.valueOf(isLocationEnabledGps);
                    infos[7] = String.valueOf(isLocationEnabledNetwork);
                    infos[8] = String.valueOf(isLocationEnabledPassive);

                    //Wifi
                    sb.append("\n").append("wifi enabled ? ").append(wifiManager.isWifiEnabled());
                    infos[9] = String.valueOf(wifiManager.isWifiEnabled());

                    //mobile data : 3g/4g
                    NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(connectivityManager.TYPE_MOBILE); //TODO; deprecated >= api 23
                    sb.append("\n").append(mobileNetworkInfo.getTypeName()).append(" : ").append(mobileNetworkInfo.isAvailable()).append(" ").append(mobileNetworkInfo.isConnected());
                    infos[10] = String.valueOf(mobileNetworkInfo.isAvailable());
                    infos[11] = String.valueOf(mobileNetworkInfo.isConnected());

                    //Bluetooth
                    sb.append("\n").append("bluetooth enabled ? ").append((bluetoothAdapter != null && bluetoothAdapter.isEnabled()));
                    infos[12] = String.valueOf(bluetoothAdapter != null && bluetoothAdapter.isEnabled());


                    //flight mode
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        //non disponible avant Android API 17 : https://developer.android.com/reference/android/provider/Settings.Global.html#AIRPLANE_MODE_ON
                        boolean isAirPlaneModeEnabled = Settings.Global.getInt(getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
                        sb.append("\n").append("airplane mode enabled ? ").append(isAirPlaneModeEnabled);
                        infos[13] = String.valueOf(isAirPlaneModeEnabled);
                    }

                    //light sensor ( Illuminance )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                        //non disponible avant Android API 3 : https://developer.android.com/reference/android/hardware/Sensor.html#TYPE_LIGHT
                        sb.append("\n").append("ambiant light level : ").append(sensorLight.getResolution()).append(" lx (max : ").append(sensorLight.getMaximumRange());
                        infos[14] = String.valueOf(sensorLight.getResolution());
                        infos[15] = String.valueOf(sensorLight.getMaximumRange());
                    }


                    writeToFile(outputStream, sb.toString(), infos);

                    //Clean stringBuilder for next loop (performance)
                    sb.delete(0, sb.length());

                    try {
                        Thread.sleep(SLEEP_TIME_FOR_THREAD_IN_MILLIS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //when the thread is stopped, we have to close the outputStream
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "impossible to close outputStream : " + e);
                }
            }
        };
        new Thread(runnable).start();
    }

    private long convertbytesToMo(long bytes) {
        return bytes / BYTES_TO_MO;
    }

    private void writeToFile(FileOutputStream outputStream, String stringToWrite, String[] infosArr) {
        Log.d(TAG, "stringToWrite =" + stringToWrite);

        String infos = TextUtils.join(",", infosArr) + '\n';
        Log.e(TAG, "infos = " + infos);

        //outputStream creation could fail during init
        if (outputStream != null) {
            try {
                if (stringToWrite == null) {
                    Log.w(TAG, "no stringToWrite");
                    return;
                }

//                outputStream.write(stringToWrite.getBytes());
                outputStream.write(infos.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "error in writeToFile:" + e);
            }
        }
    }

    private String readFile(File fileToRead, boolean addNewLineChar) {
        StringBuilder sb = new StringBuilder();
//        Log.d(TAG, "fileToRead =" + fileToRead);

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileToRead));
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
                if (addNewLineChar) {
                    sb.append('\n');
                }
            }
            br.close();
        } catch (IOException e) {
            Log.e(TAG, "error in readFile ('" + fileToRead + "') :" + e);
        }

        return sb.toString();
    }

    private CpuInfo initCpuInfo() {
//        // nbCpu may change !
//        // cf : https://developer.android.com/reference/java/lang/Runtime.html#availableProcessors()
//        int nbCpu = Runtime.getRuntime().availableProcessors();
//        Log.d(TAG, "nbCpu :" + nbCpu);


        File cpuInfoMinFreqFile = new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq");
        String cpuInfoMinFreqString = readFile(cpuInfoMinFreqFile, false);
//        Log.d(TAG, "cpuInfoMinFreqString:" + cpuInfoMinFreqString);
        int cpuInfoMinFreq = isNullOrEmpty(cpuInfoMinFreqString) ? 0 : Integer.parseInt(cpuInfoMinFreqString);

        File cpuInfoMaxFreqFile = new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
        String cpuInfoMaxFreqString = readFile(cpuInfoMaxFreqFile, false);
//        Log.d(TAG, "cpuInfoMaxFreqString:" + cpuInfoMaxFreqString);
        int cpuInfoMaxFreq = isNullOrEmpty(cpuInfoMaxFreqString) ? 0 : Integer.parseInt(cpuInfoMaxFreqString);

        // avoid duplication :cpuInfoCurFreq will be set by updateCpuInfo()
        CpuInfo cpuInfo = new CpuInfo(cpuInfoMinFreq, cpuInfoMaxFreq, 0);

        return cpuInfo;
    }

    private void updateCpuInfo() {
        File cpuInfoCurFreqFile = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
        String cpuInfoCurFreqString = readFile(cpuInfoCurFreqFile, false);
//        Log.d(TAG, "cpuInfoCurFreqString:" + cpuInfoCurFreqString);
        int cpuInfoCurFreq = isNullOrEmpty(cpuInfoCurFreqString) ? 0 : Integer.parseInt(cpuInfoCurFreqString);
        cpuInfo.setCurFreq(cpuInfoCurFreq);
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
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
