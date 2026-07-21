package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Iterator;

public class NoticeDuplicateHelper {

    private NoticeDuplicateHelper() {
    }

    public static Intent createDuplicateIntent(
            Context context,
            NoticeRecord noticeRecord
    ) {
        if (context == null
                || noticeRecord == null
                || !noticeRecord.hasDocumentSnapshot()) {
            return null;
        }

        try {
            JSONObject snapshot = new JSONObject(
                    noticeRecord.getDocumentSnapshot()
            );

            JSONObject formData = snapshot.optJSONObject(
                    "form_data"
            );

            if (formData == null || formData.length() == 0) {
                return null;
            }

            String preferenceName = getPreferenceName(
                    noticeRecord.getNoticeType()
            );

            Intent formIntent = getFormIntent(
                    context,
                    noticeRecord.getNoticeType()
            );

            if (preferenceName == null || formIntent == null) {
                return null;
            }

            SharedPreferences preferences =
                    UserSessionManager.getScopedPreferences(
                            context,
                            preferenceName
                    );

            SharedPreferences.Editor editor =
                    preferences.edit().clear();

            Iterator<String> keys = formData.keys();

            while (keys.hasNext()) {
                String key = keys.next();

                if ("notice_number".equals(key)) {
                    continue;
                }

                Object value = formData.opt(key);

                saveValue(editor, key, value);
            }

            /*
             * नया notice number Preview खोलते समय अपने-आप बनेगा।
             * इसलिए पुराने notice का number कभी copy नहीं होगा।
             */
            editor.remove("notice_number").apply();

            return formIntent;

        } catch (Exception ignored) {
            return null;
        }
    }

    private static String getPreferenceName(String noticeType) {
        if ("SECTION_94".equals(noticeType)) {
            return "section_94_draft";
        }

        if ("SECTION_35".equals(noticeType)) {
            return "section_35_draft";
        }

        if ("CDR".equals(noticeType)) {
            return "cdr_proforma_draft";
        }

        if ("COURT_RELEASE".equals(noticeType)) {
            return CourtReleaseActivity.DRAFT_PREFERENCES;
        }

        return null;
    }

    private static Intent getFormIntent(
            Context context,
            String noticeType
    ) {
        if ("SECTION_94".equals(noticeType)) {
            return new Intent(
                    context,
                    Section94FormActivity.class
            );
        }

        if ("SECTION_35".equals(noticeType)) {
            return new Intent(
                    context,
                    Section35FormActivity.class
            );
        }

        if ("CDR".equals(noticeType)) {
            return new Intent(
                    context,
                    CdrProformaActivity.class
            );
        }

        if ("COURT_RELEASE".equals(noticeType)) {
            return new Intent(
                    context,
                    CourtReleaseActivity.class
            );
        }

        return null;
    }

    private static void saveValue(
            SharedPreferences.Editor editor,
            String key,
            Object value
    ) {
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
            return;
        }

        if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
            return;
        }

        if (value instanceof Long) {
            editor.putLong(key, (Long) value);
            return;
        }

        if (value instanceof Double) {
            editor.putFloat(
                    key,
                    ((Double) value).floatValue()
            );
            return;
        }

        if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
            return;
        }

        if (value != null && value != JSONObject.NULL) {
            editor.putString(key, String.valueOf(value));
        }
    }
}
