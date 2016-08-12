package com.huangkun.shutdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by hi on 2016/8/12.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MainActivity", "进入到接收器");
        Intent bootIntent = new Intent(context, BootActivity.class);
        bootIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(bootIntent);
    }
}
