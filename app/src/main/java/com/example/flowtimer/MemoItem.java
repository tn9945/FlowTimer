package com.example.flowtimer;
import org.json.JSONException;
import org.json.JSONObject;
public class MemoItem {
public static final String MODE_NONE = "none";
public static final String MODE_SILENT = "silent";
public static final String MODE_NOTIFICATION = "notification";
public static final String MODE_SOUND = "sound";
public static final String MODE_ALERT = "alert";
private final long id;
private String title;
private String content;
private long scheduledAtMillis;
private String reminderMode;
private boolean completed;
private final long createdAtMillis;
public MemoItem(long id, String title, String content, long scheduledAtMillis, String reminderMode, boolean completed, long createdAtMillis) {
this.id = id;
this.title = title;
this.content = content;
this.scheduledAtMillis = scheduledAtMillis;
this.reminderMode = reminderMode;
this.completed = completed;
this.createdAtMillis = createdAtMillis;
}
public long getId() { return id; }
public String getTitle() { return title; }
public String getContent() { return content; }
public long getScheduledAtMillis() { return scheduledAtMillis; }
public String getReminderMode() { return reminderMode; }
public boolean isCompleted() { return completed; }
public long getCreatedAtMillis() { return createdAtMillis; }
public void setTitle(String title) { this.title = title; }
public void setContent(String content) { this.content = content; }
public void setScheduledAtMillis(long scheduledAtMillis) { this.scheduledAtMillis = scheduledAtMillis; }
public void setReminderMode(String reminderMode) { this.reminderMode = reminderMode; }
public void setCompleted(boolean completed) { this.completed = completed; }
public JSONObject toJson() throws JSONException {
JSONObject json = new JSONObject();
json.put("id", id);
json.put("title", title);
json.put("content", content);
json.put("scheduledAtMillis", scheduledAtMillis);
json.put("reminderMode", reminderMode);
json.put("completed", completed);
json.put("createdAtMillis", createdAtMillis);
return json;
}
public static MemoItem fromJson(JSONObject json) {
if (json == null) return new MemoItem(0L, "", "", 0L, MODE_NONE, false, System.currentTimeMillis());
return new MemoItem(json.optLong("id", 0L), json.optString("title", ""), json.optString("content", ""), json.optLong("scheduledAtMillis", 0L), json.optString("reminderMode", MODE_NONE), json.optBoolean("completed", false), json.optLong("createdAtMillis", System.currentTimeMillis()));
}
}
