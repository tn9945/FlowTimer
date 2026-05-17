package com.example.flowtimer.focus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Telephony;
import android.telecom.TelecomManager;

import com.example.flowtimer.AllowedAppsActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class StrictFocusPackagePolicy {

    private StrictFocusPackagePolicy() {
    }

    public static boolean isAllowedPackage(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return false;
        }
        if (packageName.equals(context.getPackageName())) {
            return true;
        }
        if (packageName.equals("com.android.systemui")) {
            return true;
        }
        if (isHomeLauncherPackage(context, packageName)) {
            return true;
        }
        Set<String> allowedPackages = getSavedAllowedPackages(context);
        if (allowedPackages.contains(packageName)) {
            return true;
        }
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null && packageName.equals(telecomManager.getDefaultDialerPackage())) {
            return true;
        }
        String smsPackageName = Telephony.Sms.getDefaultSmsPackage(context);
        return smsPackageName != null && smsPackageName.equals(packageName);
    }

    public static Set<String> getDefaultAllowedPackages(Context context) {
        Set<String> packages = new HashSet<>();
        packages.add(context.getPackageName());
        packages.add("com.android.systemui");
        packages.addAll(resolveHomeLauncherPackages(context));
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null && telecomManager.getDefaultDialerPackage() != null) {
            packages.add(telecomManager.getDefaultDialerPackage());
        }
        String smsPackageName = Telephony.Sms.getDefaultSmsPackage(context);
        if (smsPackageName != null) {
            packages.add(smsPackageName);
        }
        return packages;
    }

    private static Set<String> getSavedAllowedPackages(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(AllowedAppsActivity.PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getStringSet(AllowedAppsActivity.KEY_ALLOWED_PACKAGES, new HashSet<>());
    }

    private static boolean isHomeLauncherPackage(Context context, String packageName) {
        return resolveHomeLauncherPackages(context).contains(packageName);
    }

    private static Set<String> resolveHomeLauncherPackages(Context context) {
        Set<String> packages = new HashSet<>();
        PackageManager packageManager = context.getPackageManager();
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
}
