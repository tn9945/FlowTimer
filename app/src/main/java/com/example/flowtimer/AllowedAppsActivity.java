package com.example.flowtimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.FocusPermissionHelper;
import com.example.flowtimer.focus.StrictFocusPackagePolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllowedAppsActivity extends AppCompatActivity {

    public static final String PREF_NAME = "strict_allowed_apps";
    public static final String KEY_ALLOWED_PACKAGES = "allowed_packages";

    private LinearLayout layoutAllowedApps;
    private TextView tvDefaultAllowedApps;
    private Button btnStartStrictFocus;
    private Button btnCancelAllowedApps;
    private final List<CheckBox> appCheckBoxes = new ArrayList<>();
    private final Set<String> defaultAllowedPackages = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allowed_apps);

        bindViews();
        loadDefaultAllowedPackages();
        renderDefaultAllowedApps();
        renderInstalledApps();
        bindActions();
        showInitialPermissionGuideIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStartButtonState();
    }

    private void bindViews() {
        layoutAllowedApps = findViewById(R.id.layoutAllowedApps);
        tvDefaultAllowedApps = findViewById(R.id.tvDefaultAllowedApps);
        btnStartStrictFocus = findViewById(R.id.btnStartStrictFocus);
        btnCancelAllowedApps = findViewById(R.id.btnCancelAllowedApps);
    }

    private void loadDefaultAllowedPackages() {
        defaultAllowedPackages.clear();
        defaultAllowedPackages.addAll(StrictFocusPackagePolicy.getDefaultAllowedPackages(this));
    }

    private void renderDefaultAllowedApps() {
        PackageManager packageManager = getPackageManager();
        StringBuilder builder = new StringBuilder();
        for (String packageName : defaultAllowedPackages) {
            String appName = getAppName(packageManager, packageName);
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(appName);
        }
        tvDefaultAllowedApps.setText(builder.toString());
    }

    private void renderInstalledApps() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> launchableApps = new ArrayList<>();
        for (ApplicationInfo app : apps) {
            Intent launchIntent = packageManager.getLaunchIntentForPackage(app.packageName);
            if (launchIntent != null && !defaultAllowedPackages.contains(app.packageName)) {
                launchableApps.add(app);
            }
        }
        Collections.sort(launchableApps, Comparator.comparing(app -> getAppName(packageManager, app.packageName).toLowerCase()));
        Set<String> savedPackages = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getStringSet(KEY_ALLOWED_PACKAGES, new HashSet<>());
        for (ApplicationInfo app : launchableApps) {
            layoutAllowedApps.addView(createAppRow(packageManager, app, savedPackages.contains(app.packageName)));
        }
    }

    private LinearLayout createAppRow(PackageManager packageManager, ApplicationInfo app, boolean checked) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        ImageView iconView = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        iconParams.rightMargin = dp(12);
        iconView.setLayoutParams(iconParams);
        iconView.setImageDrawable(packageManager.getApplicationIcon(app));
        row.addView(iconView);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(getAppName(packageManager, app.packageName));
        checkBox.setTextColor(getColor(R.color.flow_text_primary));
        checkBox.setTextSize(15f);
        checkBox.setTag(app.packageName);
        checkBox.setChecked(checked);
        row.addView(checkBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        appCheckBoxes.add(checkBox);
        row.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));
        return row;
    }

    private String getAppName(PackageManager packageManager, String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private void bindActions() {
        btnStartStrictFocus.setOnClickListener(v -> {
            if (!hasRequiredStrictFocusPermissions()) {
                showPermissionGuideDialog();
                return;
            }
            saveAllowedPackages();
            Toast.makeText(this, "허용 앱 설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StrictFocusActivity.class);
            intent.putExtra(FocusModeSelectActivity.EXTRA_STRICT_MODE_TYPE, FocusModeSelectActivity.STRICT_MODE_ALLOWED_APPS);
            startActivity(intent);
            finish();
        });
        btnCancelAllowedApps.setOnClickListener(v -> finish());
    }

    private void showInitialPermissionGuideIfNeeded() {
        if (!hasRequiredStrictFocusPermissions()) {
            showPermissionGuideDialog();
        }
    }

    private boolean hasRequiredStrictFocusPermissions() {
        return FocusPermissionHelper.hasUsageAccess(this) && FocusPermissionHelper.hasAccessibilityAccess(this);
    }

    private void updateStartButtonState() {
        if (btnStartStrictFocus == null) {
            return;
        }
        btnStartStrictFocus.setText(hasRequiredStrictFocusPermissions() ? "강제 집중 시작" : "필수 설정 후 시작");
    }

    private void showPermissionGuideDialog() {
        boolean usageGranted = FocusPermissionHelper.hasUsageAccess(this);
        boolean serviceGranted = FocusPermissionHelper.hasAccessibilityAccess(this);
        String message = "강제 집중 모드를 정상적으로 사용하려면 아래 설정이 필요합니다.\n\n"
                + "1. 사용 기록 접근: " + (usageGranted ? "완료" : "필요") + "\n"
                + "2. FlowTimer 보조 기능: " + (serviceGranted ? "완료" : "필요") + "\n\n"
                + "두 설정을 모두 완료한 뒤 강제 집중을 시작해 주십시오.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("강제 집중 모드 설정 안내")
                .setMessage(message)
                .setNegativeButton("닫기", null);
        if (!usageGranted) {
            builder.setPositiveButton("사용 기록 설정", (dialog, which) -> FocusPermissionHelper.openUsageAccessSettings(this));
        } else if (!serviceGranted) {
            builder.setPositiveButton("보조 기능 설정", (dialog, which) -> FocusPermissionHelper.openAccessibilitySettings(this));
        }
        builder.show();
    }

    private void saveAllowedPackages() {
        Set<String> selectedPackages = new HashSet<>(defaultAllowedPackages);
        for (CheckBox checkBox : appCheckBoxes) {
            if (checkBox.isChecked()) {
                Object tag = checkBox.getTag();
                if (tag instanceof String) {
                    selectedPackages.add((String) tag);
                }
            }
        }
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putStringSet(KEY_ALLOWED_PACKAGES, selectedPackages);
        editor.apply();
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
