package com.example.flowtimer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class MemoReminderReceiver extends BroadcastReceiver {
public static final String EXTRA_USER_IDENTIFIER="extra_user_identifier";
public static final String EXTRA_MEMO_ID="extra_memo_id";
@Override public void onReceive(Context context,Intent intent){if(intent==null)return;String userIdentifier=intent.getStringExtra(EXTRA_USER_IDENTIFIER);long memoId=intent.getLongExtra(EXTRA_MEMO_ID,-1L);if(userIdentifier==null||memoId<=0L)return;MemoItem item=new MemoStore(context).get(userIdentifier,memoId);if(item==null||item.isCompleted())return;MemoNotificationHelper.show(context,userIdentifier,item);}
}
