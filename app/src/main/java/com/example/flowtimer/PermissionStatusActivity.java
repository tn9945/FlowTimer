package com.example.flowtimer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
    private Button btnGoMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_status);

        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);
        btnUsageAccess = findViewById(R.id.btnUsageAccess);
        btnAccessibility = findViewById(R.id.btnAccessibility);
        btnNotification = findViewById(R.id.btnNotification);
        btnGoMain = findViewById(R.id.btnGoMain);

        btnUsageAccess.setOnClickListener(v -> FocusPermissionHelper.openUsageAccessSettings(this));
        btnAccessibility.setOnClickListener(v -> FocusPermissionHelper.openAccessibilitySettings(this));
        btnNotification.setOnClickListener(v -> openNotificationSettings());
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
        String status = "사용 기록 접근: " + toStatus(usage)
                + "\nFlowTimer 보조 기능: " + toStatus(accessibility)
                + "\n알림 권한: " + toStatus(notification);
        tvPermissionStatus.setText(status);
    }

    private String toStatus(boolean granted) {
        return granted ? "완료" : "필요";
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
}
