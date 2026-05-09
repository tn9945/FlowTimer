package com.example.flowtimer;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;

public final class InputRuleHelper {

    private InputRuleHelper() {
    }

    public static void applyNameRules(EditText editText) {
        editText.setFilters(new InputFilter[]{new NameFilter(), new InputFilter.LengthFilter(20)});
    }

    public static void applyUserIdRules(EditText editText) {
        editText.setFilters(new InputFilter[]{new UserIdFilter(), new InputFilter.LengthFilter(20)});
        editText.addTextChangedListener(new LowercaseWatcher(editText));
    }

    public static void applyPasswordRules(EditText editText) {
        editText.setFilters(new InputFilter[]{new PasswordFilter(), new InputFilter.LengthFilter(20)});
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches("^[가-힣A-Za-z ]{1,20}$");
    }

    public static boolean isValidUserId(String userId) {
        return userId != null && userId.matches("^[a-z0-9]{1,20}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z0-9]{6,20}$");
    }

    private static final class LowercaseWatcher implements TextWatcher {
        private final EditText editText;
        private boolean editing;

        private LowercaseWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (editing) {
                return;
            }
            String original = s.toString();
            String lowered = original.toLowerCase();
            if (!original.equals(lowered)) {
                editing = true;
                int selection = editText.getSelectionStart();
                editText.setText(lowered);
                int safeSelection = Math.min(selection, lowered.length());
                editText.setSelection(Math.max(safeSelection, 0));
                editing = false;
            }
        }
    }

    private static final class UserIdFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            boolean changed = false;
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Character.isDigit(c)) {
                    builder.append(c);
                } else if (c >= 'a' && c <= 'z') {
                    builder.append(c);
                } else if (c >= 'A' && c <= 'Z') {
                    builder.append(Character.toLowerCase(c));
                    changed = true;
                } else {
                    changed = true;
                }
            }
            if (!changed) {
                return null;
            }
            return builder.toString();
        }
    }

    private static final class PasswordFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            boolean changed = false;
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    builder.append(c);
                } else {
                    changed = true;
                }
            }
            if (!changed) {
                return null;
            }
            return builder.toString();
        }
    }

    private static final class NameFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            boolean changed = false;
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Character.isLetter(c) || c == ' ') {
                    builder.append(c);
                } else {
                    changed = true;
                }
            }
            if (!changed) {
                return null;
            }
            return builder.toString();
        }
    }
}
