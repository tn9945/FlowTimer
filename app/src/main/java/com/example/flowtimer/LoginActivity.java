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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.data.UserRepository;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_PREFILL_USER_ID = "extra_prefill_user_id";

    private EditText etUserId;
    private EditText etPassword;
    private ImageButton btnLogin;
    private TextView tvGoSignup;
    private ProgressBar progressBar;

    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openMainAndFinish();
            return;
        }

        userRepository = new UserRepository(this);

        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoSignup = findViewById(R.id.tvGoSignup);
        progressBar = findViewById(R.id.progressBar);

        InputRuleHelper.applyUserIdRules(etUserId);
        InputRuleHelper.applyPasswordRules(etPassword);

        applyPrefillUserId(getIntent());

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoSignup.setOnClickListener(v -> openSignup(etUserId.getText().toString().trim()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applyPrefillUserId(intent);
    }

    private void applyPrefillUserId(Intent intent) {
        if (intent == null) {
            return;
        }
        String prefillUserId = intent.getStringExtra(EXTRA_PREFILL_USER_ID);
        if (!TextUtils.isEmpty(prefillUserId) && etUserId != null) {
            etUserId.setText(prefillUserId.toLowerCase());
            etPassword.setText("");
            etPassword.requestFocus();
        }
    }

    private void attemptLogin() {
        String userId = etUserId.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();

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

        setLoading(true);
        userRepository.findByUserId(userId, user -> {
            if (user == null) {
                setLoading(false);
                showSignupDialog(userId);
                return;
            }

            if (!password.equals(user.getPassword())) {
                setLoading(false);
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                etPassword.requestFocus();
                etPassword.selectAll();
                return;
            }

            sessionManager.saveLogin(user);
            setLoading(false);
            Toast.makeText(this, "성공적으로 로그인되었습니다.", Toast.LENGTH_SHORT).show();
            openMainAndFinish();
        });
    }

    private void showSignupDialog(String userId) {
        new AlertDialog.Builder(this)
                .setTitle("회원가입 안내")
                .setMessage("가입 이력이 없는 계정입니다. 회원가입 화면으로 이동하시겠습니까?")
                .setPositiveButton("이동", (dialog, which) -> openSignup(userId))
                .setNegativeButton("취소", null)
                .show();
    }

    private void openSignup(@Nullable String userId) {
        Intent intent = new Intent(this, SignupActivity.class);
        intent.putExtra(EXTRA_PREFILL_USER_ID, userId == null ? "" : userId.toLowerCase());
        startActivity(intent);
    }

    private void openMainAndFinish() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        tvGoSignup.setEnabled(!loading);
    }
}
