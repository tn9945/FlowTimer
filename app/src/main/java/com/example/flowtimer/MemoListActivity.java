package com.example.flowtimer;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class MemoListActivity extends AppCompatActivity {
private final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm",Locale.KOREA);
private SessionManager sessionManager;
private MemoStore memoStore;
private LinearLayout list;
@Override protected void onCreate(Bundle state){super.onCreate(state);sessionManager=new SessionManager(this);if(!sessionManager.isLoggedIn()){finish();return;}memoStore=new MemoStore(this);MemoNotificationHelper.ensureChannels(this);setContentView(createView());}
@Override protected void onResume(){super.onResume();bindList();}
private View createView(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(20),dp(20),dp(20));root.setBackgroundColor(getColor(R.color.flow_ivory));root.addView(text("할 일 메모",27f,true));root.addView(text("날짜와 시간을 지정하고 메모별 알림 방식을 설정할 수 있습니다.",14f,false));Button add=button("할 일 메모 추가");add.setOnClickListener(v->openEditor(-1L));root.addView(add);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);ScrollView scroll=new ScrollView(this);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));Button close=button("메인 화면으로 이동");close.setOnClickListener(v->finish());root.addView(close);return root;}
private void bindList(){list.removeAllViews();List<MemoItem> items=memoStore.getAll(sessionManager.getUserIdentifier());if(items.isEmpty()){list.addView(text("등록된 할 일 메모가 없습니다.",15f,false));return;}for(MemoItem item:items)list.addView(card(item));}
private View card(MemoItem item){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(14),dp(12),dp(14),dp(12));card.setBackgroundResource(R.drawable.bg_card);card.addView(text((item.isCompleted()?"✓ ":"")+item.getTitle(),17f,true));card.addView(text(dateFormat.format(new Date(item.getScheduledAtMillis()))+" · "+label(item.getReminderMode()),14f,false));if(!item.getContent().isEmpty())card.addView(text(item.getContent(),14f,false));LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.END);Button complete=small(item.isCompleted()?"미완료":"완료");Button edit=small("수정");Button delete=small("삭제");actions.addView(complete);actions.addView(edit);actions.addView(delete);card.addView(actions);complete.setOnClickListener(v->toggle(item));edit.setOnClickListener(v->openEditor(item.getId()));delete.setOnClickListener(v->remove(item));LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.bottomMargin=dp(10);card.setLayoutParams(params);return card;}
private void toggle(MemoItem item){item.setCompleted(!item.isCompleted());memoStore.save(sessionManager.getUserIdentifier(),item);MemoReminderScheduler.schedule(this,sessionManager.getUserIdentifier(),item);bindList();}
private void remove(MemoItem item){new AlertDialog.Builder(this).setTitle("메모 삭제 확인").setMessage("선택한 할 일 메모를 삭제하시겠습니까?").setPositiveButton("삭제",(dialog,which)->{memoStore.delete(sessionManager.getUserIdentifier(),item.getId());MemoReminderScheduler.cancel(this,sessionManager.getUserIdentifier(),item.getId());bindList();}).setNegativeButton("취소",null).show();}
private void openEditor(long memoId){Intent intent=new Intent(this,MemoEditActivity.class);intent.putExtra(MemoEditActivity.EXTRA_MEMO_ID,memoId);startActivity(intent);}
private TextView text(String value,float size,boolean bold){TextView view=new TextView(this);view.setText(value);view.setTextSize(size);view.setTextColor(getColor(bold?R.color.flow_text_primary:R.color.flow_text_hint));if(bold)view.setTypeface(view.getTypeface(),Typeface.BOLD);view.setPadding(0,dp(6),0,dp(6));return view;}
private Button button(String value){Button view=new Button(this);view.setText(value);view.setAllCaps(false);return view;}
private Button small(String value){Button view=button(value);view.setLayoutParams(new LinearLayout.LayoutParams(dp(84),dp(44)));return view;}
private String label(String value){if(MemoItem.MODE_SILENT.equals(value))return"무음 알림";if(MemoItem.MODE_NOTIFICATION.equals(value))return"일반 알림";if(MemoItem.MODE_SOUND.equals(value))return"소리 알림";if(MemoItem.MODE_ALERT.equals(value))return"알람 형식";return"알림 없음";}
private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
