package com.example.flowtimer.focus;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FocusClassifier {

    private static final List<String> STUDY_KEYWORDS = Arrays.asList(
            "classroom", "docs", "sheets", "slides", "drive", "pdf", "adobe", "kindle",
            "book", "ebook", "dictionary", "cambridge", "coursera", "udemy", "edx",
            "duolingo", "notion", "onenote", "evernote", "zoom", "teams", "meet",
            "word", "excel", "powerpoint", "lecture", "study", "learn", "github", "code"
    );

    private static final List<String> DISTRACTION_KEYWORDS = Arrays.asList(
            "youtube", "netflix", "instagram", "facebook", "messenger", "kakaotalk", "discord",
            "tiktok", "twitter", "x.com", "twitch", "afreeca", "vlive", "game", "supercell",
            "riot", "steam", "epic", "brawl", "roblox", "battle", "music", "melon", "spotify"
    );

    public String classify(String packageName, String appName) {
        String source = ((packageName == null ? "" : packageName) + " " + (appName == null ? "" : appName)).toLowerCase(Locale.ROOT);
        for (String keyword : STUDY_KEYWORDS) {
            if (source.contains(keyword)) {
                return FocusCategory.STUDY;
            }
        }
        for (String keyword : DISTRACTION_KEYWORDS) {
            if (source.contains(keyword)) {
                return FocusCategory.DISTRACTION;
            }
        }
        return FocusCategory.NEUTRAL;
    }
}
