package com.example.flowtimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.UsageAccessHelper;

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
    }

    private void bindViews() {
        layoutAllowedApps = findViewById(R.id.layoutAllowedApps);
        tvDefaultAllowedApps = findViewById(R.id.tvDefaultAllowedApps);
        btnStartStrictFocus = findViewById(R.id.btnStartStrictFocus);
        btnCancelAllowedApps = findViewById(R.id.btnCancelAllowedApps);
    }

    private void loadDefaultAllowedPackages() {
        defaultAllowedPackages.add(getPackageName());
        defaultAllowedPackages.addAll(resolveHomeLauncherPackages());
        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        if (telecomManager != null && telecomManager.getDefaultDialerPackage() != null) {
            defaultAllowedPackages.add(telecomManager.getDefaultDialerPackage());
        }
        String smsPackageName = Telephony.Sms.getDefaultSmsPackage(this);
        if (smsPackageName != null) {
            defaultAllowedPackages.add(smsPackageName);
        }
    }

    private Set<String> resolveHomeLauncherPackages() {
        Set<String> packages = new HashSet<>();
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<android.content.pm.ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (android.content.pm.ResolveInfo resolveInfo : resolveInfos) {
            if (resolveInfo.activityInfo != null && resolveInfo.activityInfo.packageName != null) {
                packages.add(resolveInfo.activityInfo.packageName);
            }
        }
        android.content.pm.ResolveInfo defaultHome = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (defaultHome != null && defaultHome.activityInfo != null && defaultHome.activityInfo.packageName != null) {
            packages.add(defaultHome.activityInfo.packageName);
        }
        return packages;
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
            if (!UsageAccessHelper.hasUsageAccess(this)) {
                showUsageAccessDialog();
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

    private void showUsageAccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("사용 기록 접근 권한 안내")
                .setMessage("강제 집중 모드에서 비허용 앱 실행을 감지하려면 사용 기록 접근 권한이 필요합니다.\n\n설정 화면에서 FlowTimer를 선택한 뒤 허용해 주십시오.")
                .setPositiveButton("설정 화면으로 이동", (dialog, which) -> UsageAccessHelper.openSettings(this))
                .setNegativeButton("취소", null)
                .show();
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
