package com.huangkun.shutdown;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Calendar;

/**
 * Created by hi on 2016/8/12.
 */
public class BootActivity extends Activity {

    private int hourGet = 0;
    private int minuteGet = 0;
    private long time;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private boolean checked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getData(); //获取上一次程序退出时保存的数据
            getTime(); //将设定的时间转换为alarmManager.set()方法中所需参数
            setAlarm(); //设置定时
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

    }

    private void getData() {
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        hourGet = Integer.parseInt(sp.getString("hour", "00"));
        minuteGet = Integer.parseInt(sp.getString("minute", "00"));
        checked = sp.getBoolean("checked", false);

        if (checked == false) {
            finish();   //如果获取到的checked为false，那说明设置的不是循环定时，就直接把这个activity结束掉
        }

    }

    private void getTime() {
        Calendar calender = Calendar.getInstance();
        calender.set(Calendar.MINUTE, minuteGet);
        calender.set(Calendar.HOUR_OF_DAY, hourGet);
        time = calender.getTimeInMillis();
        //Toast.makeText(getApplicationContext(), time + "last", Toast.LENGTH_LONG).show();
        //做一个判断，如果获取的设置时间小于当前时间，则需要把提醒设置为后一天，否则会立马执行提醒
        if (time < System.currentTimeMillis()) {
            calender.set(Calendar.DAY_OF_MONTH, calender.get(Calendar.DAY_OF_MONTH) + 1);
            calender.set(Calendar.MINUTE, minuteGet);
            calender.set(Calendar.HOUR_OF_DAY, hourGet);
            time = calender.getTimeInMillis();
        }
       // Toast.makeText(getApplicationContext(), time + "finalily", Toast.LENGTH_LONG).show();
    }

    private void setAlarm() {
        //Log.d("BootActivity", "进入设置核心");
        Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReciver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        finish();
    }

}
