package com.example.flowtimer.focus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiFocusAnalysisClient {
    public interface Callback {
        void onSuccess(AiFocusAnalysisResult result);
        void onFailure(String message);
    }

    public static final String PREF_NAME = "flowtimer_personal_settings";
    public static final String KEY_DEFAULT_MEMO_MODE = "default_memo_mode";
    public static final String KEY_AI_ENABLED = "ai_enabled";
    public static final String KEY_AI_ENDPOINT = "ai_endpoint";

    private final SharedPreferences preferences;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AiFocusAnalysisClient(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isConfigured() {
        return preferences.getBoolean(KEY_AI_ENABLED, false) && !getEndpoint().isEmpty();
    }

    public String getEndpoint() {
        return preferences.getString(KEY_AI_ENDPOINT, "");
    }

    public String getStatusText() {
        if (!preferences.getBoolean(KEY_AI_ENABLED, false)) {
            return "AI 집중 분석 연결 준비가 비활성화되어 있습니다.";
        }
        if (getEndpoint().isEmpty()) {
            return "AI 서버 주소가 설정되지 않았습니다.";
        }
        return "AI 서버 연결 정보가 준비되었습니다.";
    }

    public Map<String, Object> buildStatsPayload(String userIdentifier, long sessionCount, long focusMillis, long breakMillis, long distractionMillis, double averageScore) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userIdentifier", userIdentifier);
        payload.put("sessionCount", sessionCount);
        payload.put("focusMillis", focusMillis);
        payload.put("breakMillis", breakMillis);
        payload.put("distractionMillis", distractionMillis);
        payload.put("averageScore", averageScore);
        return payload;
    }

    public void requestAnalysis(String userIdentifier, long sessionCount, long focusMillis, long breakMillis, long distractionMillis, double averageScore, Callback callback) {
        if (!isConfigured()) {
            callback.onFailure("AI 서버 연결 설정을 먼저 완료해 주십시오.");
            return;
        }
        new Thread(() -> executeRequest(userIdentifier, sessionCount, focusMillis, breakMillis, distractionMillis, averageScore, callback)).start();
    }

    private void executeRequest(String userIdentifier, long sessionCount, long focusMillis, long breakMillis, long distractionMillis, double averageScore, Callback callback) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(getEndpoint() + "/focus-analysis");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            JSONObject payload = new JSONObject();
            payload.put("userIdentifier", userIdentifier);
            payload.put("sessionCount", sessionCount);
            payload.put("focusMillis", focusMillis);
            payload.put("breakMillis", breakMillis);
            payload.put("distractionMillis", distractionMillis);
            payload.put("averageScore", averageScore);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300 ? connection.getInputStream() : connection.getErrorStream();
            String response = readAll(stream);
            if (responseCode < 200 || responseCode >= 300) {
                postFailure(callback, "AI 서버 요청에 실패하였습니다. 응답 코드: " + responseCode);
                return;
            }

            JSONObject json = new JSONObject(response);
            AiFocusAnalysisResult result = new AiFocusAnalysisResult(
                    json.optString("summary", "분석 요약을 확인할 수 없습니다."),
                    toList(json.optJSONArray("strengths")),
                    toList(json.optJSONArray("improvements")),
                    json.optString("generatedAt", "")
            );
            mainHandler.post(() -> callback.onSuccess(result));
        } catch (Exception exception) {
            postFailure(callback, "AI 서버 연결에 실패하였습니다. 서버 실행 상태와 주소를 확인해 주십시오.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<String> toList(JSONArray array) {
        List<String> items = new ArrayList<>();
        if (array == null) {
            return items;
        }
        for (int index = 0; index < array.length(); index++) {
            String value = array.optString(index, "");
            if (!value.isEmpty()) {
                items.add(value);
            }
        }
        return items;
    }

    private String readAll(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private void postFailure(Callback callback, String message) {
        mainHandler.post(() -> callback.onFailure(message));
    }

    public static String normalizeEndpoint(String endpoint) {
        String normalized = endpoint == null ? "" : endpoint.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
