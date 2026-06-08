package com.example.flowtimer;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class MemoStore {
private static final String PREF_NAME="flowtimer_memos";
private final SharedPreferences preferences;
public MemoStore(Context context){preferences=context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);}
public List<MemoItem> getAll(String userIdentifier){List<MemoItem> items=new ArrayList<>();for(String value:preferences.getStringSet(indexKey(userIdentifier),new HashSet<>())){try{long memoId=Long.parseLong(value);MemoItem item=get(userIdentifier,memoId);if(item!=null)items.add(item);}catch(NumberFormatException ignored){}}Collections.sort(items,Comparator.comparing(MemoItem::isCompleted).thenComparingLong(item->item.getScheduledAtMillis()>0L?item.getScheduledAtMillis():Long.MAX_VALUE).thenComparingLong(MemoItem::getCreatedAtMillis));return items;}
public MemoItem get(String userIdentifier,long memoId){String prefix=memoKey(userIdentifier,memoId);if(!preferences.contains(prefix+"title"))return null;return new MemoItem(memoId,preferences.getString(prefix+"title",""),preferences.getString(prefix+"content",""),preferences.getLong(prefix+"time",0L),preferences.getString(prefix+"mode",MemoItem.MODE_NONE),preferences.getBoolean(prefix+"done",false),preferences.getLong(prefix+"created",memoId));}
public void save(String userIdentifier,MemoItem item){Set<String> ids=new HashSet<>(preferences.getStringSet(indexKey(userIdentifier),new HashSet<>()));ids.add(String.valueOf(item.getId()));String prefix=memoKey(userIdentifier,item.getId());preferences.edit().putStringSet(indexKey(userIdentifier),ids).putString(prefix+"title",item.getTitle()).putString(prefix+"content",item.getContent()).putLong(prefix+"time",item.getScheduledAtMillis()).putString(prefix+"mode",item.getReminderMode()).putBoolean(prefix+"done",item.isCompleted()).putLong(prefix+"created",item.getCreatedAtMillis()).apply();}
public void delete(String userIdentifier,long memoId){Set<String> ids=new HashSet<>(preferences.getStringSet(indexKey(userIdentifier),new HashSet<>()));ids.remove(String.valueOf(memoId));String prefix=memoKey(userIdentifier,memoId);preferences.edit().putStringSet(indexKey(userIdentifier),ids).remove(prefix+"title").remove(prefix+"content").remove(prefix+"time").remove(prefix+"mode").remove(prefix+"done").remove(prefix+"created").apply();}
public void clear(String userIdentifier){for(MemoItem item:getAll(userIdentifier))delete(userIdentifier,item.getId());preferences.edit().remove(indexKey(userIdentifier)).apply();}
private String indexKey(String userIdentifier){return"memo_ids_"+userIdentifier;}
private String memoKey(String userIdentifier,long memoId){return"memo_"+userIdentifier+"_"+memoId+"_";}
}
