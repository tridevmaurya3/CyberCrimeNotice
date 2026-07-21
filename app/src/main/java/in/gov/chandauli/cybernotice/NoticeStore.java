package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoticeStore {

    private static final String PREF_NAME = "notice_register";
    private static final String KEY_NOTICES = "saved_notices";

    private NoticeStore() {
    }

    public static void saveNotice(
            Context context,
            NoticeRecord noticeRecord
    ) {
        if (context == null || noticeRecord == null) {
            return;
        }

        JSONArray noticesArray = getNoticesArray(context);
        JSONArray updatedArray = new JSONArray();
        boolean noticeAlreadyExists = false;

        try {
            for (int i = 0; i < noticesArray.length(); i++) {
                JSONObject noticeObject = noticesArray.getJSONObject(i);

                String oldNoticeNumber = noticeObject.optString(
                        "notice_number",
                        ""
                );

                if (oldNoticeNumber.equals(
                        noticeRecord.getNoticeNumber()
                )) {
                    noticeAlreadyExists = true;
                } else {
                    updatedArray.put(noticeObject);
                }
            }

            JSONObject newNoticeObject = new JSONObject();

            newNoticeObject.put(
                    "notice_number",
                    noticeRecord.getNoticeNumber()
            );

            newNoticeObject.put(
                    "notice_type",
                    noticeRecord.getNoticeType()
            );

            newNoticeObject.put(
                    "primary_value",
                    noticeRecord.getPrimaryValue()
            );

            newNoticeObject.put(
                    "secondary_value",
                    noticeRecord.getSecondaryValue()
            );

            newNoticeObject.put(
                    "status",
                    noticeRecord.getStatus()
            );

            newNoticeObject.put(
                    "created_at",
                    noticeRecord.getCreatedAt()
            );

            newNoticeObject.put(
                    "document_snapshot",
                    noticeRecord.getDocumentSnapshot()
            );

            JSONArray finalArray = new JSONArray();

            finalArray.put(newNoticeObject);

            for (int i = 0; i < updatedArray.length(); i++) {
                finalArray.put(updatedArray.getJSONObject(i));
            }

            saveNoticesArray(context, finalArray);
            NoticeActivityStore.addActivity(
                    context,
                    noticeRecord,
                    noticeAlreadyExists
                            ? NoticeActivityStore.ACTION_UPDATED
                            : NoticeActivityStore.ACTION_CREATED
            );

        } catch (Exception ignored) {
        }
    }

    public static void updateNoticeStatus(
            Context context,
            String noticeNumber,
            String status
    ) {
        JSONArray noticesArray = getNoticesArray(context);
        JSONArray updatedArray = new JSONArray();
        String noticeType = "";
        String oldStatus = "";
        boolean statusChanged = false;

        try {
            for (int i = 0; i < noticesArray.length(); i++) {
                JSONObject noticeObject = noticesArray.getJSONObject(i);

                String savedNoticeNumber = noticeObject.optString(
                        "notice_number",
                        ""
                );

                if (savedNoticeNumber.equals(noticeNumber)) {
                    noticeType = noticeObject.optString(
                            "notice_type",
                            ""
                    );

                    oldStatus = noticeObject.optString(
                            "status",
                            ""
                    );

                    if (!oldStatus.equalsIgnoreCase(status)) {
                        noticeObject.put("status", status);
                        statusChanged = true;
                    }
                }

                updatedArray.put(noticeObject);
            }

            saveNoticesArray(context, updatedArray);
            if (statusChanged) {
                NoticeActivityStore.addActivity(
                        context,
                        noticeNumber,
                        noticeType,
                        NoticeActivityStore.getStatusAction(status)
                );
            }

        } catch (Exception ignored) {
        }
    }

    public static boolean deleteDraftNotice(
            Context context,
            String noticeNumber
    ) {
        if (context == null
                || noticeNumber == null
                || noticeNumber.trim().isEmpty()) {
            return false;
        }

        JSONArray noticesArray = getNoticesArray(context);
        JSONArray updatedArray = new JSONArray();

        boolean deleted = false;

        try {
            for (int i = 0; i < noticesArray.length(); i++) {
                JSONObject noticeObject = noticesArray.getJSONObject(i);

                String savedNoticeNumber = noticeObject.optString(
                        "notice_number",
                        ""
                );

                String savedStatus = noticeObject.optString(
                        "status",
                        "DRAFT"
                );

                boolean isTargetDraft =
                        savedNoticeNumber.equals(noticeNumber)
                                && "DRAFT".equals(savedStatus);

                if (isTargetDraft) {
                    deleted = true;
                    continue;
                }

                updatedArray.put(noticeObject);
            }

            if (deleted) {
                saveNoticesArray(context, updatedArray);
            }

        } catch (Exception ignored) {
            return false;
        }

        return deleted;
    }

    public static JSONArray getNoticesForBackup(Context context) {
        try {
            return new JSONArray(
                    getNoticesArray(context).toString()
            );
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    public static boolean restoreNoticesFromBackup(
            Context context,
            JSONArray backupNotices
    ) {
        if (context == null || backupNotices == null) {
            return false;
        }

        try {
            JSONArray validatedNotices = new JSONArray();
            Set<String> savedNoticeNumbers = new HashSet<>();

            for (int i = 0; i < backupNotices.length(); i++) {
                JSONObject noticeObject =
                        backupNotices.getJSONObject(i);

                String noticeNumber = noticeObject.optString(
                        "notice_number",
                        ""
                ).trim();

                String noticeType = noticeObject.optString(
                        "notice_type",
                        ""
                ).trim();

                if (noticeNumber.isEmpty() || noticeType.isEmpty()) {
                    return false;
                }

                if (savedNoticeNumbers.contains(noticeNumber)) {
                    return false;
                }

                savedNoticeNumbers.add(noticeNumber);

                validatedNotices.put(noticeObject);
            }

            saveNoticesArray(context, validatedNotices);

            return true;

        } catch (Exception ignored) {
            return false;
        }
    }

    public static List<NoticeRecord> getAllNotices(Context context) {
        List<NoticeRecord> noticeList = new ArrayList<>();

        JSONArray noticesArray = getNoticesArray(context);

        try {
            for (int i = 0; i < noticesArray.length(); i++) {
                JSONObject noticeObject = noticesArray.getJSONObject(i);

                NoticeRecord noticeRecord = new NoticeRecord(
                        noticeObject.optString("notice_number", ""),
                        noticeObject.optString("notice_type", ""),
                        noticeObject.optString("primary_value", ""),
                        noticeObject.optString("secondary_value", ""),
                        noticeObject.optString("status", "DRAFT"),
                        noticeObject.optLong("created_at", 0),
                        noticeObject.optString(
                                "document_snapshot",
                                ""
                        )
                );

                noticeList.add(noticeRecord);
            }
        } catch (Exception ignored) {
        }

        return noticeList;
    }

    private static JSONArray getNoticesArray(Context context) {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                context,
                PREF_NAME
        );

        String savedData = preferences.getString(KEY_NOTICES, "[]");

        try {
            return new JSONArray(savedData);
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    private static void saveNoticesArray(
            Context context,
            JSONArray noticesArray
    ) {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                context,
                PREF_NAME
        );

        preferences.edit()
                .putString(KEY_NOTICES, noticesArray.toString())
                .apply();
    }
}
