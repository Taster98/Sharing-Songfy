package ml.luiggi.geosongfy.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * Questa classe rappresenta il mio broadcast receiver*/
public class NotificationActionService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("ALL_SONGS")
                .putExtra("actionName", intent.getAction()));
    }
}
