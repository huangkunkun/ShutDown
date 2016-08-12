package com.huangkun.shutdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Created by hi on 2016/8/11.
 */
public class AlarmBroadcastReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"关机啦", Toast.LENGTH_SHORT).show();
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(
                    process.getOutputStream());
            out.writeBytes("reboot -p\n");
            out.writeBytes("exit\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

}
}
