package com.example.flowtimer;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

public class FlowTimerApplication extends Application implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (!(activity instanceof MainActivity)) {
            return;
        }
        bindMain((MainActivity) activity);
    }

    private void bindMain(MainActivity activity) {
        View memo = activity.findViewById(R.id.btnMemo);
        View menu = activity.findViewById(R.id.btnMenu);
        View overlay = activity.findViewById(R.id.menuOverlay);
        LinearLayout panel = activity.findViewById(R.id.sideMenu);
        if (memo == null || menu == null || overlay == null || panel == null) {
            return;
        }
        bindPet(activity);
        memo.setOnClickListener(v -> activity.startActivity(new Intent(activity, MemoListActivity.class)));
        menu.setOnClickListener(v -> {
            overlay.setVisibility(View.VISIBLE);
            panel.setVisibility(View.VISIBLE);
        });
        overlay.setOnClickListener(v -> {
            overlay.setVisibility(View.GONE);
            panel.setVisibility(View.GONE);
        });
        View settings = activity.findViewById(R.id.tvPersonalSettings);
        View memberInfo = activity.findViewById(R.id.tvMemberInfo);
        if (settings != null) {
            settings.setOnClickListener(v -> new AlertDialog.Builder(activity)
                    .setTitle("개인 설정")
                    .setMessage("새 메모 기본 알림 방식과 AI 집중 분석 연결 설정을 관리할 수 있습니다.")
                    .setPositiveButton("확인", null)
                    .show());
        }
        if (memberInfo != null) {
            memberInfo.setOnClickListener(v -> {
                SessionManager session = new SessionManager(activity);
                new AlertDialog.Builder(activity)
                        .setTitle("회원정보")
                        .setMessage("이름: " + session.getUserName() + "\n사용자 ID: " + session.getUserIdentifier())
                        .setPositiveButton("확인", null)
                        .show();
            });
        }
    }

    private void bindPet(MainActivity activity) {
        SessionManager session = new SessionManager(activity);
        SharedPreferences preferences = activity.getSharedPreferences("game_data", MODE_PRIVATE);
        String character = preferences.getString(session.getUserIdentifier() + "_character", "rabbit");
        ImageView image = activity.findViewById(R.id.imgPetCharacter);
        if (image == null) {
            return;
        }
        if ("dog".equals(character)) {
            image.setImageResource(R.drawable.dog);
        } else if ("bear".equals(character)) {
            image.setImageResource(R.drawable.bear);
        } else {
            image.setImageResource(R.drawable.rabbit);
        }
    }

    @Override public void onActivityCreated(Activity activity, Bundle bundle) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}
