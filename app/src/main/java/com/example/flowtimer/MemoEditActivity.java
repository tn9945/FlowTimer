package com.example.flowtimer;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.flowtimer.focus.AiFocusAnalysisClient;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
public class MemoEditActivity extends AppCompatActivity {
public static final String EXTRA_MEMO_ID = "extra_memo_id";
private static final String[] LABELS = {"알림 없음", "무음 알림", "일반 알림", "소리 알림", "알람 형식"};
private static final String[] VALUES = {MemoItem.MODE_NONE, MemoItem.MODE_SILENT, MemoItem.MODE_NOTIFICATION, MemoItem.MODE_SOUND, MemoItem.MODE_ALERT};
private final SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA);
private final Calendar selected = Calendar.getInstance();
private SessionManager session;
private MemoStore store;
private MemoItem editing;
private EditText title;
private EditText content;
private TextView time;
private Spinner mode;
@Override
protected void onCreate(Bundle state) {
super.onCreate(state);
session = new SessionManager(this);
if (!session.isLoggedIn()) {
finish();
return;
}
store = new MemoStore(this);
long memoId = getIntent().getLongExtra(EXTRA_MEMO_ID, -1L);
editing = memoId > 0L ? store.get(session.getUserIdentifier(), memoId) : null;
selected.add(Calendar.HOUR_OF_DAY, 1);
if (editing != null) selected.setTimeInMillis(editing.getScheduledAtMillis());
setContentView(createView());
}
private LinearLayout createView() {
LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setPadding(dp(20), dp(20), dp(20), dp(20));
root.setBackgroundColor(getColor(R.color.flow_ivory));
title = new EditText(this);
title.setHint("할 일 제목");
content = new EditText(this);
content.setHint("세부 내용");
content.setMinLines(3);
time = new TextView(this);
mode = new Spinner(this);
ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, LABELS);
adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
mode.setAdapter(adapter);
if (editing != null) {
title.setText(editing.getTitle());
content.setText(editing.getContent());
mode.setSelection(index(editing.getReminderMode()));
} else {
String configured = getSharedPreferences(AiFocusAnalysisClient.PREF_NAME, MODE_PRIVATE)
.getString(AiFocusAnalysisClient.KEY_DEFAULT_MEMO_MODE, MemoItem.MODE_NOTIFICATION);
mode.setSelection(index(configured));
}
refreshTime();
Button date = button("날짜 선택");
Button clock = button("시간 선택");
Button save = button("저장");
Button cancel = button("취소");
date.setOnClickListener(v -> pickDate());
clock.setOnClickListener(v -> pickTime());
save.setOnClickListener(v -> save());
cancel.setOnClickListener(v -> finish());
root.addView(title);
root.addView(content);
root.addView(time);
root.addView(date);
root.addView(clock);
root.addView(mode);
root.addView(save);
root.addView(cancel);
return root;
}
private void pickDate() {
new DatePickerDialog(this, (view, year, month, day) -> {
selected.set(Calendar.YEAR, year);
selected.set(Calendar.MONTH, month);
selected.set(Calendar.DAY_OF_MONTH, day);
refreshTime();
}, selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH)).show();
}
private void pickTime() {
new TimePickerDialog(this, (view, hour, minute) -> {
selected.set(Calendar.HOUR_OF_DAY, hour);
selected.set(Calendar.MINUTE, minute);
selected.set(Calendar.SECOND, 0);
selected.set(Calendar.MILLISECOND, 0);
refreshTime();
}, selected.get(Calendar.HOUR_OF_DAY), selected.get(Calendar.MINUTE), true).show();
}
private void save() {
String value = title.getText().toString().trim();
if (TextUtils.isEmpty(value)) {
title.setError("할 일 제목을 입력해 주십시오.");
return;
}
String selectedMode = VALUES[mode.getSelectedItemPosition()];
long when = selected.getTimeInMillis();
if (!MemoItem.MODE_NONE.equals(selectedMode) && when <= System.currentTimeMillis()) {
Toast.makeText(this, "알림 예약 시각은 현재 이후로 선택해 주십시오.", Toast.LENGTH_SHORT).show();
return;
}
long now = System.currentTimeMillis();
MemoItem item = new MemoItem(editing == null ? now : editing.getId(), value, content.getText().toString().trim(), when, selectedMode, editing != null && editing.isCompleted(), editing == null ? now : editing.getCreatedAtMillis());
store.save(session.getUserIdentifier(), item);
Toast.makeText(this, "할 일 메모를 저장하였습니다.", Toast.LENGTH_SHORT).show();
finish();
}
private void refreshTime() {
time.setText("예약 시각: " + format.format(selected.getTime()));
}
private Button button(String value) {
Button button = new Button(this);
button.setText(value);
button.setTextAllCaps(false);
return button;
}
private int index(String value) {
for (int index = 0; index < VALUES.length; index++) if (VALUES[index].equals(value)) return index;
return 0;
}
private int dp(int value) {
return Math.round(value * getResources().getDisplayMetrics().density);
}
}
