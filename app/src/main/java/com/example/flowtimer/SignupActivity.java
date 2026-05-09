package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.data.UserRepository;

public class SignupActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etUserId;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private ImageButton btnSignup;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        userRepository = new UserRepository(this);

        etName = findViewById(R.id.etName);
        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnSignup = findViewById(R.id.btnSignup);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.progressBar);

        InputRuleHelper.applyNameRules(etName);
        InputRuleHelper.applyUserIdRules(etUserId);
        InputRuleHelper.applyPasswordRules(etPassword);
        InputRuleHelper.applyPasswordRules(etPasswordConfirm);

        String prefillUserId = getIntent().getStringExtra(LoginActivity.EXTRA_PREFILL_USER_ID);
        if (!TextUtils.isEmpty(prefillUserId)) {
            etUserId.setText(prefillUserId.toLowerCase());
        }

        btnSignup.setOnClickListener(v -> attemptSignup());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void attemptSignup() {
        String name = etName.getText().toString().trim();
        String userId = etUserId.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etPasswordConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("이름을 입력해 주십시오.");
            etName.requestFocus();
            return;
        }

        if (!InputRuleHelper.isValidName(name)) {
            etName.setError("이름은 한글 또는 영문만 입력할 수 있습니다.");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            etUserId.setError("아이디를 입력해 주십시오.");
            etUserId.requestFocus();
            return;
        }

        if (!InputRuleHelper.isValidUserId(userId)) {
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

        if (userId.equalsIgnoreCase(password)) {
            etPassword.setError("아이디와 비밀번호는 서로 동일할 수 없습니다.");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etPasswordConfirm.setError("비밀번호 확인을 입력해 주십시오.");
            etPasswordConfirm.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etPasswordConfirm.setError("비밀번호가 일치하지 않습니다.");
            etPasswordConfirm.requestFocus();
            return;
        }

        setLoading(true);
        userRepository.register(name, userId, password, (success, message) -> {
            setLoading(false);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            if (success) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(LoginActivity.EXTRA_PREFILL_USER_ID, userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignup.setEnabled(!loading);
        tvBackToLogin.setEnabled(!loading);
    }
}
