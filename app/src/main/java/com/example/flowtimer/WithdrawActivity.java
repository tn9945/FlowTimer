package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.data.UserRepository;

public class WithdrawActivity extends AppCompatActivity {

    private EditText etUserId;
    private EditText etPassword;
    private Button btnWithdraw;
    private Button btnGoMain;
    private TextView tvCancel;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_withdraw);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            openLoginAndFinish();
            return;
        }

        userRepository = new UserRepository(this);

        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        btnGoMain = findViewById(R.id.btnGoMain);
        tvCancel = findViewById(R.id.tvCancel);
        progressBar = findViewById(R.id.progressBar);

        InputRuleHelper.applyUserIdRules(etUserId);
        InputRuleHelper.applyPasswordRules(etPassword);

        etUserId.setText(sessionManager.getUserIdentifier());

        btnWithdraw.setOnClickListener(v -> attemptWithdraw());
        btnGoMain.setOnClickListener(v -> goMain());
        tvCancel.setOnClickListener(v -> finish());
    }

    private void attemptWithdraw() {
        String currentUserId = sessionManager.getUserIdentifier();
        String inputUserId = etUserId.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(inputUserId)) {
            etUserId.setError("아이디를 입력해 주십시오.");
            etUserId.requestFocus();
            return;
        }

        if (!InputRuleHelper.isValidUserId(inputUserId)) {
            etUserId.setError("아이디는 영문 소문자와 숫자만 입력할 수 있습니다.");
            etUserId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("비밀번호를 입력해 주십시오.");
            etPassword.requestFocus();
            return;
        }

        if (!InputRuleHelper.isValidPassword(password)) {
            etPassword.setError("비밀번호는 영문과 숫자를 포함한 6자리 이상이어야 합니다.");
            etPassword.requestFocus();
            return;
        }

        if (!currentUserId.equalsIgnoreCase(inputUserId)) {
            etUserId.setError("현재 로그인된 계정의 아이디를 입력해 주십시오.");
            etUserId.requestFocus();
            return;
        }

        setLoading(true);
        userRepository.withdraw(inputUserId, password, (success, message) -> {
            setLoading(false);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            if (success) {
                sessionManager.clearSession();
                openLoginAndFinish();
            }
        });
    }

    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void openLoginAndFinish() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnWithdraw.setEnabled(!loading);
        btnGoMain.setEnabled(!loading);
        tvCancel.setEnabled(!loading);
    }
}
