package com.example.flowtimer.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.flowtimer.InputRuleHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    public interface UserCallback {
        void onResult(UserEntity user);
    }

    public interface ActionCallback {
        void onComplete(boolean success, String message);
    }

    private final UserDao userDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public UserRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void findByUserId(String userId, UserCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.findByUserId(normalizeUserId(userId));
            mainHandler.post(() -> callback.onResult(user));
        });
    }

    public void register(String name, String userId, String password, ActionCallback callback) {
        executorService.execute(() -> {
            String normalizedUserId = normalizeUserId(userId);
            String trimmedName = name == null ? "" : name.trim();
            String trimmedPassword = password == null ? "" : password.trim();

            if (!InputRuleHelper.isValidName(trimmedName)) {
                mainHandler.post(() -> callback.onComplete(false, "이름은 한글 또는 영문만 입력할 수 있습니다."));
                return;
            }

            if (!InputRuleHelper.isValidUserId(normalizedUserId)) {
                mainHandler.post(() -> callback.onComplete(false, "아이디는 영문 소문자와 숫자만 사용할 수 있습니다."));
                return;
            }

            if (!InputRuleHelper.isValidPassword(trimmedPassword)) {
                mainHandler.post(() -> callback.onComplete(false, "비밀번호는 영문과 숫자를 포함한 6자리 이상이어야 합니다."));
                return;
            }

            if (normalizedUserId.equalsIgnoreCase(trimmedPassword)) {
                mainHandler.post(() -> callback.onComplete(false, "아이디와 비밀번호는 서로 동일할 수 없습니다."));
                return;
            }

            UserEntity existingUser = userDao.findByUserId(normalizedUserId);
            if (existingUser != null) {
                mainHandler.post(() -> callback.onComplete(false, "이미 사용 중인 아이디입니다."));
                return;
            }

            userDao.insert(new UserEntity(trimmedName, normalizedUserId, trimmedPassword));
            mainHandler.post(() -> callback.onComplete(true, "회원가입이 완료되었습니다."));
        });
    }

    public void withdraw(String userId, String password, ActionCallback callback) {
        executorService.execute(() -> {
            int deletedCount = userDao.deleteByUserIdAndPassword(normalizeUserId(userId), password == null ? "" : password.trim());
            if (deletedCount > 0) {
                mainHandler.post(() -> callback.onComplete(true, "회원 탈퇴가 완료되었습니다."));
            } else {
                mainHandler.post(() -> callback.onComplete(false, "아이디 또는 비밀번호가 올바르지 않습니다."));
            }
        });
    }

    private String normalizeUserId(String userId) {
        return userId == null ? "" : userId.trim().toLowerCase();
    }
}
