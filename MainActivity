package com.github.jordane_quincy.m2_greencomputing;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;

public class MainActivity extends AppCompatActivity {

    private Button btn1;
    private TextView txtView1;

    private Button btnCpu;
    private TextView txtViewCpu;

    private Button btn2;
    private TextView txtView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btn1 = (Button) findViewById(R.id.btn1);
        this.btn1.setOnClickListener(handlerBtn1);
        this.txtView1 = (TextView) findViewById(R.id.txtView1);


        this.btnCpu = (Button) findViewById(R.id.btnCpu);
        this.btnCpu.setOnClickListener(handlerBtnCpu);
        this.txtViewCpu = (TextView) findViewById(R.id.txtViewCpu);


        this.btn2 = (Button) findViewById(R.id.btn2);
        this.btn2.setOnClickListener(handlerBtn2);
        this.txtView2 = (TextView) findViewById(R.id.txtView2);

    }

    View.OnClickListener handlerBtn1 = new View.OnClickListener() {
        public void onClick(View v) {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            double availableMegs = mi.availMem / 0x100000L;

            //Percentage can be calculated for API 16+
            double percentAvail = mi.availMem / (double)mi.totalMem;

            txtView1.setText("availableMegs : "+ availableMegs +" ("+ percentAvail +")");
        }
    };

    View.OnClickListener handlerBtnCpu = new View.OnClickListener() {
        public void onClick(View v) {
            ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> lstRunningApp = manager.getRunningAppProcesses();

            StringBuilder sb = new StringBuilder();
            for(ActivityManager.RunningAppProcessInfo runningApp : lstRunningApp){
                sb.append(runningApp.processName);

                sb.append(" - ");
                switch (runningApp.importance){
                    case ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
                        sb.append("foreground");
                        break;
                    case ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
                        sb.append("background");
                        break;
                }
                sb.append("\n");
            }

            txtViewCpu.setText(sb.toString());
        }
    };


    View.OnClickListener handlerBtn2 = new View.OnClickListener() {
        public void onClick(View v) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            StringBuilder sb = new StringBuilder();
            sb.append("wifi").append(" ");
            switch (wifiManager.getWifiState()){
                case WIFI_STATE_DISABLED:
                    sb.append("disabled");
                    break;
                case WIFI_STATE_ENABLED:
                    sb.append("enabled");
                    break;
            }

            txtView2.setText(sb.toString());
        }
    };
}
