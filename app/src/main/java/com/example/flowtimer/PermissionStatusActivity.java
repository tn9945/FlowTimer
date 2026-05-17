package com.example.flowtimer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.example.flowtimer.focus.FocusPermissionHelper;

public class PermissionStatusActivity extends AppCompatActivity {

    private TextView tvPermissionStatus;
    private Button btnUsageAccess;
    private Button btnAccessibility;
    private Button btnNotification;
    private Button btnBattery;
    private Button btnGoMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_status);

        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);
        btnUsageAccess = findViewById(R.id.btnUsageAccess);
        btnAccessibility = findViewById(R.id.btnAccessibility);
        btnNotification = findViewById(R.id.btnNotification);
        btnBattery = findViewById(R.id.btnBattery);
        btnGoMain = findViewById(R.id.btnGoMain);

        btnUsageAccess.setOnClickListener(v -> FocusPermissionHelper.openUsageAccessSettings(this));
        btnAccessibility.setOnClickListener(v -> FocusPermissionHelper.openAccessibilitySettings(this));
        btnNotification.setOnClickListener(v -> openNotificationSettings());
        btnBattery.setOnClickListener(v -> openBatteryOptimizationSettings());
        btnGoMain.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindStatus();
    }

    private void bindStatus() {
        boolean usage = FocusPermissionHelper.hasUsageAccess(this);
        boolean accessibility = FocusPermissionHelper.hasAccessibilityAccess(this);
        boolean notification = NotificationManagerCompat.from(this).areNotificationsEnabled();
        boolean battery = isIgnoringBatteryOptimizations();
        String status = "사용 기록 접근: " + toStatus(usage)
                + "\nFlowTimer 보조 기능: " + toStatus(accessibility)
                + "\n알림 권한: " + toStatus(notification)
                + "\n배터리 최적화 제외: " + toStatus(battery);
        tvPermissionStatus.setText(status);
    }

    private String toStatus(boolean granted) {
        return granted ? "완료" : "필요";
    }

    private boolean isIgnoringBatteryOptimizations() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        return powerManager != null && powerManager.isIgnoringBatteryOptimizations(getPackageName());
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void openBatteryOptimizationSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
}
