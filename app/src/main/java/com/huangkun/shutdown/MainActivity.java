package com.huangkun.shutdown;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private CheckBox checkBox;
    private Button openAlarm;
    private Button closeAlarm;
    private TextView tvHour;
    private TextView tvMinute;

    private int hourGet = 0;
    private int minuteGet = 0;
    private long time;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    private String hourFinal = "00";
    private String minuteFinal = "00";
    private boolean checkedFinal = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initView();  //初始化各控件
        setData(); //获取保存的数据，并将之前的设置信息显示出来
        setAlarm(); //设置AlarmManager的相关数据
        initListener(); //为按钮初始化监听器
        getTime();  //将由TimePicker获取的设定时间转换为alarmManager.set()方法中所需参数
    }

    /**
     * 该方法用于在用户打开软件时，显示出之前已经设置的定时时间，如果读取不到，则显示时间为 00 : 00
     */
    private void setData() {
        try {
            SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
            hourFinal = sp.getString("hour", "00");
            minuteFinal = sp.getString("minute", "00");
            checkedFinal = sp.getBoolean("checked", false);
            tvHour.setText(hourFinal);
            tvMinute.setText(minuteFinal);
            checkBox.setChecked(checkedFinal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 这个方法主要是考虑到，如果用户在TimePicker上并没有选择时间或者只是选择了分钟和小时之间的一个，
     * 那么OnTimeChangedListener便不会调用或者只获取到一个值，所以这个时候minuteGet或hourGet的值就是
     * 初始化时的0，这样明显不合理，因此使用该方法将minuteGet和hourGet中没有发生变化的那个值设置为系统
     * 当前的时间值
     */
    private void setTime() {
        Calendar calender = Calendar.getInstance();
        if (minuteGet == 0) {
            calender.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        } else {
            calender.set(Calendar.MINUTE, minuteGet);
        }
        if (hourGet == 0) {
            calender.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        } else {
            calender.set(Calendar.HOUR_OF_DAY, hourGet);
        }
        time = calender.getTimeInMillis();
    }

    /**
     * 设置具体的参数
     */
    private void setAlarm() {
        Intent intent = new Intent(MainActivity.this, AlarmBroadcastReciver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    }

    private void initListener() {
        BtnClickListener listener = new BtnClickListener();
        openAlarm.setOnClickListener(listener);
        closeAlarm.setOnClickListener(listener);
    }

    private class BtnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_open:
                    setTime();
                    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

                    Toast.makeText(getApplicationContext(), "已设置", Toast.LENGTH_SHORT).show();
                    tvHour.setText(judgeTime(hourGet));
                    tvMinute.setText(judgeTime(minuteGet));
                    setData();
                    break;
                case R.id.btn_close:
                    alarmManager.cancel(pendingIntent);
                    checkBox.setChecked(false);
                    tvHour.setText("00");
                    tvMinute.setText("00");
                    setData();
                    Toast.makeText(getApplicationContext(), "已取消", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        /**
         * 该方法用于记录当前用户设定的数据
         */
        private void setData(){
            hourFinal = tvHour.getText().toString();
            minuteFinal = tvMinute.getText().toString();
            checkedFinal = checkBox.isChecked();
        }

        /**
         * 主要是在TimePicker中获取数据时，如果获取的时间是小于10的，那么它的值就会是0,1，2，...这样的
         * 数据在显示时会出现  0:0 这种样式的时间，显得很难看
         * @param t  传入的时间值
         * @return  返回用来显示的时间的字符串
         */
        private String judgeTime(int t) {
            String time = "";
            if (t <= 9) {
                time = 0 + "" + t;
            } else {
                time = t + "";
            }
            return time;
        }
    }

    /**
     * 通过TimePicker的OnTimeChangedListener获取时间值
     */
    private void getTime() {
        timePicker.setIs24HourView(true);
        hourGet = timePicker.getCurrentHour();
        minuteGet = timePicker.getCurrentMinute();

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hourGet = hourOfDay;
                minuteGet = minute;
            }
        });
    }

    private void initView() {
        timePicker = (TimePicker) findViewById(R.id.tp_show);
        checkBox = (CheckBox) findViewById(R.id.cb_show);
        openAlarm = (Button) findViewById(R.id.btn_open);
        closeAlarm = (Button) findViewById(R.id.btn_close);
        tvHour = (TextView) findViewById(R.id.tv_hour);
        tvMinute = (TextView) findViewById(R.id.tv_minute);
    }

    /**
     * 在软件进入OnStop态时保存已经设置的数据
     */
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("hour", hourFinal);
        editor.putString("minute", minuteFinal);
        editor.putBoolean("checked", checkedFinal);
        editor.commit();

    }

}
