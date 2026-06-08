package com.example.flowtimer;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.flowtimer.focus.AiFocusAnalysisClient;

public class FlowTimerApplication extends Application implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof MainActivity) {
            bindMain((MainActivity) activity);
            return;
        }
        if (activity instanceof FocusStatsActivity) {
            bindAiStatus((FocusStatsActivity) activity);
        }
    }

    private void bindMain(MainActivity activity) {
        View memo = activity.findViewById(R.id.btnMemo);
        View menu = activity.findViewById(R.id.btnMenu);
        View overlay = activity.findViewById(R.id.menuOverlay);
        LinearLayout panel = activity.findViewById(R.id.sideMenu);
        if (memo == null || menu == null || overlay == null || panel == null) {
            return;
        }
        MemoNotificationHelper.ensureChannels(activity);
        MemoReminderScheduler.rescheduleAll(activity);
        bindPet(activity);
        memo.setOnClickListener(v -> activity.startActivity(new Intent(activity, MemoListActivity.class)));
        menu.setOnClickListener(v -> openDrawer(overlay, panel));
        overlay.setOnClickListener(v -> closeDrawer(overlay, panel));
        View settings = activity.findViewById(R.id.tvPersonalSettings);
        View memberInfo = activity.findViewById(R.id.tvMemberInfo);
        View logout = activity.findViewById(R.id.btnLogout);
        View withdraw = activity.findViewById(R.id.tvWithdraw);
        View developerMode = activity.findViewById(R.id.tvDeveloperMode);
        if (settings != null) {
            settings.setOnClickListener(v -> {
                closeDrawer(overlay, panel);
                showPersonalSettings(activity);
            });
        }
        if (memberInfo != null) {
            memberInfo.setOnClickListener(v -> {
                closeDrawer(overlay, panel);
                showMemberInfo(activity);
            });
        }
        if (logout != null) {
            logout.setOnClickListener(v -> {
                closeDrawer(overlay, panel);
                logout(activity);
            });
        }
        if (withdraw != null) {
            withdraw.setOnClickListener(v -> {
                closeDrawer(overlay, panel);
                activity.startActivity(new Intent(activity, WithdrawActivity.class));
            });
        }
        if (developerMode != null) {
            developerMode.setOnClickListener(v -> {
                closeDrawer(overlay, panel);
                activity.startActivity(new Intent(activity, DeveloperModeActivity.class));
            });
        }
    }

    private void bindPet(MainActivity activity) {
        SessionManager session = new SessionManager(activity);
        if (!session.isLoggedIn()) {
            return;
        }
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

    private void bindAiStatus(FocusStatsActivity activity) {
        TextView status = activity.findViewById(R.id.tvAiConnectionStatus);
        if (status == null) {
            return;
        }
        status.setText(new AiFocusAnalysisClient(activity).getStatusText());
    }

    private void openDrawer(View overlay, LinearLayout panel) {
        overlay.setVisibility(View.VISIBLE);
        panel.setVisibility(View.VISIBLE);
        overlay.setAlpha(0f);
        panel.post(() -> {
            panel.setTranslationX(panel.getWidth());
            panel.animate().translationX(0f).setDuration(240L).start();
            overlay.animate().alpha(0.42f).setDuration(240L).start();
        });
    }

    private void closeDrawer(View overlay, LinearLayout panel) {
        if (overlay.getVisibility() != View.VISIBLE && panel.getVisibility() != View.VISIBLE) {
            return;
        }
        overlay.animate().alpha(0f).setDuration(180L).withEndAction(() -> overlay.setVisibility(View.GONE)).start();
        panel.animate().translationX(panel.getWidth()).setDuration(180L).withEndAction(() -> panel.setVisibility(View.GONE)).start();
    }

    private void showPersonalSettings(MainActivity activity) {
        String[] items = {"새 메모 기본 알림 방식", "AI 집중 분석 연결 설정"};
        new AlertDialog.Builder(activity)
                .setTitle("개인 설정")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showDefaultMemoMode(activity);
                    } else {
                        showAiSettings(activity);
                    }
                })
                .setNegativeButton("닫기", null)
                .show();
    }

    private void showDefaultMemoMode(MainActivity activity) {
        String[] labels = {"알림 없음", "무음 알림", "일반 알림", "소리 알림", "알람 형식"};
        String[] values = {MemoItem.MODE_NONE, MemoItem.MODE_SILENT, MemoItem.MODE_NOTIFICATION, MemoItem.MODE_SOUND, MemoItem.MODE_ALERT};
        SharedPreferences preferences = activity.getSharedPreferences(AiFocusAnalysisClient.PREF_NAME, MODE_PRIVATE);
        String current = preferences.getString(AiFocusAnalysisClient.KEY_DEFAULT_MEMO_MODE, MemoItem.MODE_NOTIFICATION);
        int checked = 0;
        for (int index = 0; index < values.length; index++) {
            if (values[index].equals(current)) {
                checked = index;
                break;
            }
        }
        final int[] selected = {checked};
        new AlertDialog.Builder(activity)
                .setTitle("새 메모 기본 알림 방식")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> selected[0] = which)
                .setPositiveButton("저장", (dialog, which) -> {
                    preferences.edit().putString(AiFocusAnalysisClient.KEY_DEFAULT_MEMO_MODE, values[selected[0]]).apply();
                    Toast.makeText(activity, "기본 메모 알림 방식을 저장하였습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showAiSettings(MainActivity activity) {
        SharedPreferences preferences = activity.getSharedPreferences(AiFocusAnalysisClient.PREF_NAME, MODE_PRIVATE);
        LinearLayout wrapper = new LinearLayout(activity);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(activity, 18);
        wrapper.setPadding(padding, padding / 2, padding, 0);
        CheckBox enabled = new CheckBox(activity);
        enabled.setText("AI 집중 분석 연결 준비 활성화");
        enabled.setChecked(preferences.getBoolean(AiFocusAnalysisClient.KEY_AI_ENABLED, false));
        EditText endpoint = new EditText(activity);
        endpoint.setHint("AI 서버 주소 예: https://example.com/api");
        endpoint.setText(preferences.getString(AiFocusAnalysisClient.KEY_AI_ENDPOINT, ""));
        wrapper.addView(enabled);
        wrapper.addView(endpoint);
        new AlertDialog.Builder(activity)
                .setTitle("AI 서버 연결 설정")
                .setView(wrapper)
                .setPositiveButton("저장", (dialog, which) -> {
                    preferences.edit()
                            .putBoolean(AiFocusAnalysisClient.KEY_AI_ENABLED, enabled.isChecked())
                            .putString(AiFocusAnalysisClient.KEY_AI_ENDPOINT, AiFocusAnalysisClient.normalizeEndpoint(endpoint.getText().toString()))
                            .apply();
                    Toast.makeText(activity, "AI 서버 연결 설정을 저장하였습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showMemberInfo(MainActivity activity) {
        SessionManager session = new SessionManager(activity);
        String message = "이름: " + session.getUserName()
                + "\n사용자 ID: " + session.getUserIdentifier()
                + "\n내부 회원 번호: " + session.getUserId();
        new AlertDialog.Builder(activity)
                .setTitle("회원정보")
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }

    private void logout(MainActivity activity) {
        SessionManager session = new SessionManager(activity);
        if (session.isLoggedIn()) {
            MemoReminderScheduler.cancelAll(activity, session.getUserIdentifier());
        }
        session.clearSession();
        Toast.makeText(activity, "성공적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    private int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    @Override public void onActivityCreated(Activity activity, Bundle bundle) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}
