package net.gstark.foldersync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Intent myIntent = new Intent(context, FileSystemObserverService.class);
        //context.startService(myIntent);

        Log.i("StartupReceiver", "started service");
    }
}