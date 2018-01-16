package com.example.zhanglulu.redenvelopeplgu_in;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Switch mStartSwitch;
    private EditText mDelayTimeEd;
    private EditText mX, mY;
    private Switch mSetOwnerSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartSwitch = ((Switch) findViewById(R.id.start));
        mDelayTimeEd = findViewById(R.id.delay_timte);
        mX = findViewById(R.id.x_et);
        mY = findViewById(R.id.y_et);

        mStartSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    if (!isAccessibilitySettingsOn(MainActivity.this)) {
                        Toast.makeText(MainActivity.this, "请前往辅助功能给予权限", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                    RedEnvelopAccessibility.sCurState = RedEnvelopAccessibility.DEFAULT;
                } else {
                    RedEnvelopAccessibility.sCurState = RedEnvelopAccessibility.STOP_RED;
                }
            }
        });

        //设置延迟时间
        findViewById(R.id.set_delay_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String delayTime = mDelayTimeEd.getText().toString();
                if (!TextUtils.isEmpty(delayTime)) {
                    SharedPreferences sp = getSharedPreferences(RedEnvelopAccessibility.RED_ENVELOP_SP, MODE_PRIVATE);
                    Integer time = Integer.valueOf(delayTime);
                    if (time < 0) {
                        time = 0;
                    }
                    sp.edit().putInt(RedEnvelopAccessibility.SET_DELAY_TIME, time).apply();
                    Toast.makeText(MainActivity.this, "成功设置延迟抢:" + time + "毫秒", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //设置位置
        findViewById(R.id.set_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences(RedEnvelopAccessibility.RED_ENVELOP_SP, MODE_PRIVATE);
                sp.edit().putInt(RedEnvelopAccessibility.SET_LOCATION_X, Integer.valueOf(mX.getText().toString())).apply();
                sp.edit().putInt(RedEnvelopAccessibility.SET_LOCATION_Y, Integer.valueOf(mY.getText().toString())).apply();
                Toast.makeText(MainActivity.this, "成功设置抢红包位置 x:" + mX.getText().toString() + " y:" + mY.getText().toString() , Toast.LENGTH_SHORT).show();
            }
        });

        //设置抢自己的红包
        mSetOwnerSwitcher = (Switch) findViewById(R.id.set_get_owner_red);
        mSetOwnerSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getSharedPreferences(RedEnvelopAccessibility.RED_ENVELOP_SP, MODE_PRIVATE);
                if (buttonView.isChecked()) {
                    sp.edit().putBoolean(RedEnvelopAccessibility.SET_OWNER_RED, true).apply();
                } else {
                    sp.edit().putBoolean(RedEnvelopAccessibility.SET_OWNER_RED, false).apply();
                }
            }
        });

        //还原默认位置
        findViewById(R.id.reset_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences(RedEnvelopAccessibility.RED_ENVELOP_SP, MODE_PRIVATE);
                sp.edit().putInt(RedEnvelopAccessibility.SET_LOCATION_X, 500).apply();
                sp.edit().putInt(RedEnvelopAccessibility.SET_LOCATION_Y, 1120).apply();
                onResume();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAccessibilitySettingsOn(this) && RedEnvelopAccessibility.sCurState != RedEnvelopAccessibility.STOP_RED) {
            mStartSwitch.setChecked(true);
        } else {
            mStartSwitch.setChecked(false);
        }

        SharedPreferences sp = getSharedPreferences(RedEnvelopAccessibility.RED_ENVELOP_SP, MODE_PRIVATE);
        if (sp.getBoolean(RedEnvelopAccessibility.SET_OWNER_RED, false)) {
            mSetOwnerSwitcher.setChecked(true);
        } else {
            mSetOwnerSwitcher.setChecked(false);
        }
        mDelayTimeEd.setText(String.valueOf(sp.getInt(RedEnvelopAccessibility.SET_DELAY_TIME, 500)));
        mX.setText(String.valueOf(sp.getInt(RedEnvelopAccessibility.SET_LOCATION_X, 500)));
        mY.setText(String.valueOf(sp.getInt(RedEnvelopAccessibility.SET_LOCATION_Y, 1120)));
    }

    /**
     * 此方法用来判断当前应用的辅助功能服务是否开启
     */
    public boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.i(TAG, e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }

    /**
     * 判断自己的应用的AccessibilityService是否在运行
     *
     * @return
     */
    /**
     * 判断服务是否正在运行
     * @return
     */
    public boolean isServiceRunning() {
        boolean ret = false;
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Short.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo runningService : runningServices) {
                if (runningService.service.getClassName().equalsIgnoreCase(RedEnvelopAccessibility.class.getName())) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    /*
    // 请求Accessibility权限
                if (!isAccessibilitySettingsOn(MainActivity.this)) {
                    // 引导至辅助功能设置页面
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                } else {
                    // 执行辅助功能服务相关操作
                    Toast.makeText(MainActivity.this, "Perfect 您有该权限", Toast.LENGTH_SHORT).show();
                }
                break;

     */
}
