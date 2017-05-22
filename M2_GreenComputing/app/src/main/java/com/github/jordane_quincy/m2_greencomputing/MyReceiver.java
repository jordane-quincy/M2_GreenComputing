package com.github.jordane_quincy.m2_greencomputing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Sert à lancer le service au démarrage du téléphone.
 * Created by jordane on 15/04/17.
 */

public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = MyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "intent recu :" + intent);

        //Lancement du service (UNE SEULE FOIS)
        Intent intent2 = new Intent(context, RecordService.class);
        Log.d(TAG, "intent creer :" + intent2);
        context.startService(intent2);
    }
}
