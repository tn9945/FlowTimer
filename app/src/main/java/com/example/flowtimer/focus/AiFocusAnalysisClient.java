package com.example.flowtimer.focus;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;
public class AiFocusAnalysisClient {
public static final String PREF_NAME = "flowtimer_personal_settings";
public static final String KEY_DEFAULT_MEMO_MODE = "default_memo_mode";
public static final String KEY_AI_ENABLED = "ai_enabled";
public static final String KEY_AI_ENDPOINT = "ai_endpoint";
private final SharedPreferences preferences;
public AiFocusAnalysisClient(Context context) { preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); }
public boolean isConfigured() { return preferences.getBoolean(KEY_AI_ENABLED, false) && !preferences.getString(KEY_AI_ENDPOINT, "").isEmpty(); }
public String getEndpoint() { return preferences.getString(KEY_AI_ENDPOINT, ""); }
public String getStatusText() { if (!preferences.getBoolean(KEY_AI_ENABLED, false)) return "AI 집중 분석 연결 준비가 비활성화되어 있습니다."; if (getEndpoint().isEmpty()) return "AI 서버 주소가 설정되지 않았습니다."; return "AI 서버 연결 정보가 준비되었습니다."; }
public Map<String, Object> buildStatsPayload(String userIdentifier,long sessionCount,long focusMillis,long breakMillis,long distractionMillis,double averageScore){Map<String,Object> payload=new HashMap<>();payload.put("userIdentifier",userIdentifier);payload.put("sessionCount",sessionCount);payload.put("focusMillis",focusMillis);payload.put("breakMillis",breakMillis);payload.put("distractionMillis",distractionMillis);payload.put("averageScore",averageScore);return payload;}
public static String normalizeEndpoint(String endpoint){String normalized=endpoint==null?"":endpoint.trim();while(normalized.endsWith("/"))normalized=normalized.substring(0,normalized.length()-1);return normalized;}
}
