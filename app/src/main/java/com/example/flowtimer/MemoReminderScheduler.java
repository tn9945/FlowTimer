package com.example.flowtimer;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.List;
public class MemoReminderScheduler {
private MemoReminderScheduler(){}
public static void schedule(Context context,String userIdentifier,MemoItem item){cancel(context,userIdentifier,item.getId());if(item.isCompleted()||MemoItem.MODE_NONE.equals(item.getReminderMode())||item.getScheduledAtMillis()<=System.currentTimeMillis())return;AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);if(manager==null)return;PendingIntent pending=createPendingIntent(context,userIdentifier,item.getId());if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,item.getScheduledAtMillis(),pending);else manager.set(AlarmManager.RTC_WAKEUP,item.getScheduledAtMillis(),pending);}
public static void cancel(Context context,String userIdentifier,long memoId){AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);if(manager!=null)manager.cancel(createPendingIntent(context,userIdentifier,memoId));MemoNotificationHelper.cancel(context,userIdentifier,memoId);}
public static void cancelAll(Context context,String userIdentifier){for(MemoItem item:new MemoStore(context).getAll(userIdentifier))cancel(context,userIdentifier,item.getId());}
public static void rescheduleAll(Context context){SessionManager session=new SessionManager(context);if(!session.isLoggedIn())return;String userIdentifier=session.getUserIdentifier();List<MemoItem> items=new MemoStore(context).getAll(userIdentifier);for(MemoItem item:items)schedule(context,userIdentifier,item);}
private static PendingIntent createPendingIntent(Context context,String userIdentifier,long memoId){Intent intent=new Intent(context,MemoReminderReceiver.class);intent.setAction("com.example.flowtimer.MEMO_REMINDER");intent.putExtra(MemoReminderReceiver.EXTRA_USER_IDENTIFIER,userIdentifier);intent.putExtra(MemoReminderReceiver.EXTRA_MEMO_ID,memoId);int flags=PendingIntent.FLAG_UPDATE_CURRENT;if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)flags|=PendingIntent.FLAG_IMMUTABLE;return PendingIntent.getBroadcast(context,requestCode(userIdentifier,memoId),intent,flags);}
private static int requestCode(String userIdentifier,long memoId){return(userIdentifier+"#"+memoId).hashCode()&0x7fffffff;}
}
