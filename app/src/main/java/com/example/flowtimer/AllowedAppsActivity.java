package com.example.flowtimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        if (telecomManager != null && telecomManager.getDefaultDialerPackage() != null) {
            defaultAllowedPackages.add(telecomManager.getDefaultDialerPackage());
        }
        String smsPackageName = Telephony.Sms.getDefaultSmsPackage(this);
        if (smsPackageName != null) {
            defaultAllowedPackages.add(smsPackageName);
        }
    }

    private void renderDefaultAllowedApps() {
        PackageManager packageManager = getPackageManager();
        StringBuilder builder = new StringBuilder();
        for (String packageName : defaultAllowedPackages) {
            String appName = getAppName(packageManager, packageName);
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(appName).append(" (").append(packageName).append(")");
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
            CheckBox checkBox = new CheckBox(this);
            String appName = getAppName(packageManager, app.packageName);
            checkBox.setText(appName + "\n" + app.packageName);
            checkBox.setTextColor(getColor(R.color.flow_text_primary));
            checkBox.setTextSize(14f);
            checkBox.setPadding(0, 14, 0, 14);
            checkBox.setTag(app.packageName);
            checkBox.setChecked(savedPackages.contains(app.packageName));
            layoutAllowedApps.addView(checkBox, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            appCheckBoxes.add(checkBox);
        }
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
            saveAllowedPackages();
            Toast.makeText(this, "상호작용 허용 앱 설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StrictFocusActivity.class);
            intent.putExtra(FocusModeSelectActivity.EXTRA_STRICT_MODE_TYPE, FocusModeSelectActivity.STRICT_MODE_ALLOWED_APPS);
            startActivity(intent);
            finish();
        });
        btnCancelAllowedApps.setOnClickListener(v -> finish());
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
}
