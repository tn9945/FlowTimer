package com.example.flowtimer;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
public class MemoNotificationHelper {
private static final String CHANNEL_SILENT="memo_silent";
private static final String CHANNEL_NOTIFICATION="memo_notification";
private static final String CHANNEL_SOUND="memo_sound";
private static final String CHANNEL_ALERT="memo_alert";
private MemoNotificationHelper(){}
public static void ensureChannels(Context context){
if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O)return;
NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
if(manager==null)return;
NotificationChannel silent=new NotificationChannel(CHANNEL_SILENT,"메모 무음 알림",NotificationManager.IMPORTANCE_LOW);silent.setSound(null,null);
NotificationChannel normal=new NotificationChannel(CHANNEL_NOTIFICATION,"메모 일반 알림",NotificationManager.IMPORTANCE_DEFAULT);normal.setSound(null,null);
NotificationChannel sound=new NotificationChannel(CHANNEL_SOUND,"메모 소리 알림",NotificationManager.IMPORTANCE_DEFAULT);
NotificationChannel alert=new NotificationChannel(CHANNEL_ALERT,"메모 알람 형식 알림",NotificationManager.IMPORTANCE_HIGH);
Uri uri=Settings.System.DEFAULT_NOTIFICATION_URI;
AudioAttributes attributes=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
alert.setSound(uri,attributes);alert.enableVibration(true);
manager.createNotificationChannel(silent);manager.createNotificationChannel(normal);manager.createNotificationChannel(sound);manager.createNotificationChannel(alert);
}
public static void show(Context context,String userIdentifier,MemoItem item){
if(MemoItem.MODE_NONE.equals(item.getReminderMode()))return;
ensureChannels(context);
NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(manager==null)return;
Intent openIntent=new Intent(context,MemoListActivity.class);openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
PendingIntent contentIntent=PendingIntent.getActivity(context,notificationId(userIdentifier,item.getId()),openIntent,pendingFlags());
Notification.Builder builder=Build.VERSION.SDK_INT>=Build.VERSION_CODES.O?new Notification.Builder(context,channel(item.getReminderMode())):new Notification.Builder(context);
builder.setSmallIcon(R.drawable.flow_timer_app_icon_transparent).setContentTitle(item.getTitle()).setContentText(item.getContent().isEmpty()?"예약한 할 일 시간이 되었습니다.":item.getContent()).setContentIntent(contentIntent).setAutoCancel(true).setCategory(Notification.CATEGORY_REMINDER).setVisibility(Notification.VISIBILITY_PUBLIC);
if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O&&(MemoItem.MODE_SOUND.equals(item.getReminderMode())||MemoItem.MODE_ALERT.equals(item.getReminderMode())))builder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE);
manager.notify(notificationId(userIdentifier,item.getId()),builder.build());
}
public static void cancel(Context context,String userIdentifier,long memoId){NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(manager!=null)manager.cancel(notificationId(userIdentifier,memoId));}
public static int notificationId(String userIdentifier,long memoId){return(userIdentifier+"@"+memoId).hashCode()&0x7fffffff;}
private static String channel(String mode){if(MemoItem.MODE_SILENT.equals(mode))return CHANNEL_SILENT;if(MemoItem.MODE_SOUND.equals(mode))return CHANNEL_SOUND;if(MemoItem.MODE_ALERT.equals(mode))return CHANNEL_ALERT;return CHANNEL_NOTIFICATION;}
private static int pendingFlags(){int flags=PendingIntent.FLAG_UPDATE_CURRENT;if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)flags|=PendingIntent.FLAG_IMMUTABLE;return flags;}
}
