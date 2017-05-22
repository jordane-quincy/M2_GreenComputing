# M2_GreenComputing

## TP de ouf !!!

Créer un logiciel qui permet de réduire la consommation énergétique en fonction des données utilisateurs

Rendre à la fin du mois de mai (avant le 26 mai)
Langage libre (C / C++ / java / C# / Android)
Travail demandé ouvert et libre
Squelette à respecter.

Phase de collecte de données (qu'on peut essayer de détourner)  (application qui tourne en back ou foreground, CPU, niveau luminosité, toutes données qui existent sur le système sont les bienvenue) (fichier json/xml/text). On peut imaginer nos données (scénario réel)

Phase de traitement de données (réseau de neuronne, réseau baesien, prédiction (GSP, generalize, Sequenced,pattern) : WEKA choisir 2 algorithmes au moins, GSP Et classifier(cluster)

Phase d'application de Power Management, couper ce qu'on peut



Gain en énergie => coupé un module de l'appareil sans altérer l'utilisateur. Quand on est dans un certain cas, on peut couper une ressource. Plateform invoquing

A partir du traitement de données

Pour les mesures

Imaginer les scénarios (ou collecter des données)
Ressources : gérer au moins 3 ressources, Luminosité, wifi, cpu, son, bluetooth etc etc


Power tutor


## RENDU
Petite page word qui explique ce qu'on a fait (les scénarios utilisés, les techniques d'apprentissage etc)

## Links :
https://developer.android.com/reference/android/app/ActivityManager.html

Data Collection module
Puis
Data processing
Puis
Power policies (bases de règles) et Current state module (Pour vérifier l'état en live de l'utilisateur)
Puis
Ressources managements (couper les différents modules en fonction des power policies)


Scénario sous forme d'un logger


### Code Android :
```
private static void setBrightness(Context context, int brightnessValue) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessValue);
}
```

```
private static void adjustVolume(Context context, boolean isVolumeMustBeLower) {
    Log.d(TAG, "adjustVolume :" + isVolumeMustBeLower);
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    audioManager.adjustVolume(isVolumeMustBeLower ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
}
```

```
private static void bluetoothToggleState() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    if (bluetoothAdapter == null) {
        Log.e(TAG, "Bluetooth is not supported on this hardware platform.");
        return;
    }

    if (bluetoothAdapter.isEnabled()) {
        bluetoothAdapter.disable();
    } else {
        bluetoothAdapter.enable();
    }
}
```

```
private static void wifiToggleState(Context context) {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

    if (wifiManager == null) {
        Log.e(TAG, "Wifi is not supported on this hardware platform.");
        return;
    }

    wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
}
```

```
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
```


