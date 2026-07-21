package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NoticeActivityStore {

    public static final String ACTION_CREATED = "CREATED";
    public static final String ACTION_UPDATED = "UPDATED";
    public static final String ACTION_DRAFT = "DRAFT";
    public static final String ACTION_ISSUED = "ISSUED";
    public static final String ACTION_COMPLETED = "COMPLETED";

    private static final String PREF_NAME = "notice_activity_log";
    private static final String KEY_ACTIVITIES = "activities";
    private static final int MAX_SAVED_ACTIVITIES = 250;

    private NoticeActivityStore() {
    }

    public static void addActivity(
            Context context,
            NoticeRecord noticeRecord,
            String action
    ) {
        if (noticeRecord == null) {
            return;
        }

        addActivity(
                context,
                noticeRecord.getNoticeNumber(),
                noticeRecord.getNoticeType(),
                action
        );
    }

    public static void addActivity(
            Context context,
            String noticeNumber,
            String noticeType,
            String action
    ) {
        if (noticeNumber == null || noticeNumber.trim().isEmpty()) {
            return;
        }

        JSONArray oldArray = getActivitiesArray(context);
        JSONArray updatedArray = new JSONArray();

        try {
            JSONObject newActivity = new JSONObject();

            newActivity.put("notice_number", noticeNumber);
            newActivity.put("notice_type", noticeType);
            newActivity.put("action", action);
            newActivity.put(
                    "activity_time",
                    System.currentTimeMillis()
            );

            updatedArray.put(newActivity);

            int allowedOldItems = Math.min(
                    oldArray.length(),
                    MAX_SAVED_ACTIVITIES - 1
            );

            for (int index = 0;
                 index < allowedOldItems;
                 index++) {

                updatedArray.put(
                        oldArray.getJSONObject(index)
                );
            }

            saveActivitiesArray(context, updatedArray);

        } catch (Exception ignored) {
        }
    }

    public static List<NoticeActivityRecord> getRecentActivities(
            Context context,
            int limit
    ) {
        List<NoticeActivityRecord> activityList =
                new ArrayList<>();

        JSONArray activitiesArray = getActivitiesArray(context);

        int finalLimit = Math.min(
                Math.max(limit, 0),
                activitiesArray.length()
        );

        try {
            for (int index = 0;
                 index < finalLimit;
                 index++) {

                JSONObject activityObject =
                        activitiesArray.getJSONObject(index);

                NoticeActivityRecord activityRecord =
                        new NoticeActivityRecord(
                                activityObject.optString(
                                        "notice_number",
                                        ""
                                ),
                                activityObject.optString(
                                        "notice_type",
                                        ""
                                ),
                                activityObject.optString(
                                        "action",
                                        ""
                                ),
                                activityObject.optLong(
                                        "activity_time",
                                        0
                                )
                        );

                activityList.add(activityRecord);
            }
        } catch (Exception ignored) {
        }

        return activityList;
    }

    public static String getStatusAction(String status) {
        if ("DRAFT".equalsIgnoreCase(status)) {
            return ACTION_DRAFT;
        }

        if ("ISSUED".equalsIgnoreCase(status)) {
            return ACTION_ISSUED;
        }

        if ("COMPLETED".equalsIgnoreCase(status)) {
            return ACTION_COMPLETED;
        }

        return ACTION_UPDATED;
    }

    private static JSONArray getActivitiesArray(Context context) {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                context,
                PREF_NAME
        );

        String savedData = preferences.getString(
                KEY_ACTIVITIES,
                "[]"
        );

        try {
            return new JSONArray(savedData);
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    private static void saveActivitiesArray(
            Context context,
            JSONArray activitiesArray
    ) {
        UserSessionManager.getScopedPreferences(
                context,
                PREF_NAME
        ).edit().putString(
                KEY_ACTIVITIES,
                activitiesArray.toString()
        ).apply();
    }
}
