package com.example.zhanglulu.redenvelopeplgu_in;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.check_permission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 请求Accessibility权限
                if (!isAccessibilitySettingsOn(MainActivity.this)) {
                    // 引导至辅助功能设置页面
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                } else {
                    // 执行辅助功能服务相关操作
                    Toast.makeText(MainActivity.this, "Perfect 您有该权限", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.check_services_running).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    Toast.makeText(MainActivity.this, "服务正在运行", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "服务已停止运行", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
}
