package com.github.jordane_quincy.m2_greencomputing;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    private static int getBrightness(Context context) {
        int brightnessValue = 255;

        try {
            brightnessValue = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "getBrightness :" + e);
        }

        return brightnessValue;
    }

    private static void setBrightness(Context context, int brightnessValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {

            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessValue);
    }

    private static void adjustVolume(Context context, boolean isVolumeMustBeLower) {
        Log.d(TAG, "adjustVolume :" + isVolumeMustBeLower);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        //TODO : a voir s'il faut aussi afficher le " toast containing the current volume. " via
        // AudioManager.FLAG_PLAY_SOUND + AudioManager.FLAG_SHOW_UI
        audioManager.adjustVolume(isVolumeMustBeLower ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);

    }

    private static void bluetoothToggleState() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this hardware platform.");
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
//            Log.d(TAG, "Bluetooth disable");
            bluetoothAdapter.disable();
        } else {
//            Log.d(TAG, "Bluetooth enable");
            bluetoothAdapter.enable();
        }

    }

    private static void wifiToggleState(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            Log.e(TAG, "Wifi is not supported on this hardware platform.");
            return;
        }

        wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
    }

    private static void dataToggleState(Context context) {
        try {
            final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
            connectivityManagerField.setAccessible(true);
            final Object connectivityManager = connectivityManagerField.get(conman);
            final Class connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());


            final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            boolean isDataEnabled = (boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
            Log.d(TAG, "dataToggleState isDataEnabled ? " + isDataEnabled);

            final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(connectivityManager, !isDataEnabled);

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            //FIXME : Ces méthodes ne sont utilisables jusque android kitkat mais pas de tél android marshmallow pour tenter mieux
            Log.e(TAG, "Error during dataToggleState :" + e);
        }
    }

    private static void readLogFile(final Context context, final TextView textViewToUpdate) {
        try {
            File recordFile = new File(context.getFilesDir(), "recordServiceFile.txt");
            Log.d(TAG, "log file : " + recordFile);

            long recordFileLength = recordFile.length();

            FileInputStream inputStream = new FileInputStream(recordFile);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }

            textViewToUpdate.setText(total);

        } catch (Exception e) {
            Log.e(TAG, "impossible to read logs " + e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        /////////////////////////////////SetData Activity//////////////////////////////////
//        Button decrease_sound_button = (Button) findViewById(R.id.decrease_sound_button);
//        Button increase_sound_button = (Button) findViewById(R.id.increase_sound_button);
//        Button decrease_lights_button = (Button) findViewById(R.id.decrease_lights_button);
//        Button increase_lights_button = (Button) findViewById(R.id.increase_lights_button);
//        //impossible d'implementer le position button car ce n'est plus possible sous android kitkat
//        Button position_button = (Button) findViewById(R.id.position_button);
//        Button wifi_button = (Button) findViewById(R.id.wifi_button);
//        Button bluetooth_button = (Button) findViewById(R.id.bluetooth_button);
//        Button mobile_data_button = (Button) findViewById(R.id.mobile_data_button);


        askRuntimePermissions();

    }

    private void askRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            //FIXME: ask other runtime permissions
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        askRuntimePermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return getView(inflater, container, savedInstanceState);
        }
        private View getView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
            int section = getArguments().getInt(ARG_SECTION_NUMBER);
            View view = inflater.inflate(R.layout.fragment_main, container, false);


            Button btnIntent = (Button) view.findViewById(R.id.btnIntent);
            Log.d(TAG, "btnIntent finded "+ btnIntent);

            btnIntent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "btnIntent onClick start "+ getActivity());
                    getContext().startService(new Intent(getActivity(), RecordService.class));

                    Log.d(TAG, "btnIntent onClick end");
                }
            });

            Button btnStopService = (Button) view.findViewById(R.id.btnStopService);
            btnStopService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "btnStopService onClick");
                    boolean serviceCorrectlyStopped = getContext().stopService(new Intent(getActivity(), RecordService.class));
                    Log.d(TAG, "serviceCorrectlyStopped ? " + serviceCorrectlyStopped);
                }
            });

            final TextView logs = (TextView) view.findViewById(R.id.logs);
            Button btnReadLog = (Button) view.findViewById(R.id.btnReadLog);
            btnReadLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "btnReadLog onClick");

                    readLogFile(getContext(), logs);
                }

            });


//            Thread timer = new Thread(){
//                public void run(){
//                    while(CONTINUE_AUTO_REFRESH_LOG){
//                        try{
//                            sleep(5000);
//
//                            readLogFile(getContext(), logs);
//                        } catch (InterruptedException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            };
//            timer.start();


            switch(section)
            {
                case 1:
                    view = inflater.inflate(R.layout.activity_main, container, false);
                break;
                case 2:
                    view = inflater.inflate(R.layout.activity_set_data, container, false);

                    SeekBar seekBarBrightness = (SeekBar) view.findViewById(R.id.seekBarBrightness);

                    final int BRIGHTNESS_MIN = 1;
                    final int BRIGHTNESS_STEP = 1;
                    final int BRIGHTNESS_MAX = 255;

                    seekBarBrightness.setMax((BRIGHTNESS_MAX - BRIGHTNESS_MIN) / BRIGHTNESS_STEP);

                    // set le seekbar progress to current brighness
                    seekBarBrightness.setProgress(getBrightness(getContext()));

                    seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                                      boolean fromUser) {
                            int brightnessValue = BRIGHTNESS_MIN + (progress * BRIGHTNESS_STEP);
                            Log.d(TAG, "brightnessValue :" + brightnessValue);

                            setBrightness(getContext(), brightnessValue);
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                    });


                    Button increaseSoundButton = (Button) view.findViewById(R.id.increase_sound_button);
                    increaseSoundButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            adjustVolume(getContext(), false);
                        }
                    });

                    Button decreaseSoundButton = (Button) view.findViewById(R.id.decrease_sound_button);
                    decreaseSoundButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            adjustVolume(getContext(), true);
                        }
                    });


                    Button bluetoothButton = (Button) view.findViewById(R.id.bluetooth_button);
                    bluetoothButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            bluetoothToggleState();
                        }
                    });


                    Button wifiButton = (Button) view.findViewById(R.id.wifi_button);
                    wifiButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            wifiToggleState(getContext());
                        }
                    });

                    Button mobile_data_button = (Button) view.findViewById(R.id.mobile_data_button);
                    mobile_data_button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dataToggleState(getContext());
                        }
                    });


                    break;
            }
            return view;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Collecter";
                case 1:
                    return "Agir";
                case 2:
                    return "Service";
            }
            return null;
        }
    }
}
