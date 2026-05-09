package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.FocusBackupManager;
import com.example.flowtimer.focus.FocusRepository;
import com.example.flowtimer.focus.UsageAccessHelper;

public class DeveloperModeActivity extends AppCompatActivity {

    private TextView tvDeveloperSummary;
    private TextView tvUsageAccessStatus;
    private TextView tvBackupStatus;
    private SessionManager sessionManager;
    private FocusRepository focusRepository;
    private FocusBackupManager backupManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_mode);

        sessionManager = new SessionManager(this);
        focusRepository = new FocusRepository(this);
        backupManager = new FocusBackupManager(this);

        tvDeveloperSummary = findViewById(R.id.tvDeveloperSummary);
        tvUsageAccessStatus = findViewById(R.id.tvUsageAccessStatus);
        tvBackupStatus = findViewById(R.id.tvBackupStatus);
        Button btnClearFocusRecords = findViewById(R.id.btnClearFocusRecords);
        Button btnCreateSampleData = findViewById(R.id.btnCreateSampleData);
        Button btnExportBackup = findViewById(R.id.btnExportBackup);
        Button btnImportBackup = findViewById(R.id.btnImportBackup);
        Button btnOpenUsageSettings = findViewById(R.id.btnOpenUsageSettings);
        Button btnGoMain = findViewById(R.id.btnGoMain);

        bindStatus();

        btnClearFocusRecords.setOnClickListener(v -> confirmClear());
        btnCreateSampleData.setOnClickListener(v -> createSampleData());
        btnExportBackup.setOnClickListener(v -> exportBackup());
        btnImportBackup.setOnClickListener(v -> importBackup());
        btnOpenUsageSettings.setOnClickListener(v -> UsageAccessHelper.openSettings(this));
        btnGoMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindStatus();
    }

    private void bindStatus() {
        tvDeveloperSummary.setText("현재 로그인 사용자: " + sessionManager.getUserIdentifier() + "\n테스트용 집중 기록 초기화, 더미 데이터 생성, 백업 및 복원을 수행할 수 있습니다.");
        tvUsageAccessStatus.setText(UsageAccessHelper.hasUsageAccess(this)
                ? "사용 기록 접근 설정 상태: 허용됨"
                : "사용 기록 접근 설정 상태: 허용되지 않음");
        if (backupManager.hasBackup()) {
            tvBackupStatus.setText("백업 상태: 저장됨 (" + DurationFormatter.formatDateTime(backupManager.getLastBackupTimeMillis()) + ")");
        } else {
            tvBackupStatus.setText("백업 상태: 저장된 백업이 없습니다.");
        }
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle("기록 초기화 확인")
                .setMessage("현재 로그인 사용자의 집중 활동 기록을 모두 삭제합니다.")
                .setPositiveButton("삭제", (dialog, which) -> focusRepository.clearAllFocusData(sessionManager.getUserIdentifier(), () -> {
                    Toast.makeText(this, "집중 활동 기록이 초기화되었습니다.", Toast.LENGTH_SHORT).show();
                    bindStatus();
                }))
                .setNegativeButton("취소", null)
                .show();
    }

    private void createSampleData() {
        focusRepository.generateSampleData(sessionManager.getUserIdentifier(), sessionManager.getUserName(), () -> {
            Toast.makeText(this, "테스트용 더미 기록이 생성되었습니다.", Toast.LENGTH_SHORT).show();
            bindStatus();
        });
    }

    private void exportBackup() {
        boolean success = backupManager.exportUserData(sessionManager.getUserIdentifier());
        Toast.makeText(this, success ? "집중 기록 백업이 생성되었습니다." : "백업 생성에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        bindStatus();
    }

    private void importBackup() {
        new AlertDialog.Builder(this)
                .setTitle("백업 복원 확인")
                .setMessage("현재 로그인 사용자의 집중 기록을 백업 파일 기준으로 복원합니다.")
                .setPositiveButton("복원", (dialog, which) -> {
                    boolean success = backupManager.importUserData(sessionManager.getUserIdentifier());
                    Toast.makeText(this, success ? "집중 기록 복원이 완료되었습니다." : "복원 가능한 백업이 없습니다.", Toast.LENGTH_SHORT).show();
                    bindStatus();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
