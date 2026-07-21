package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Cloud backup and restore scoped strictly to the authenticated Firebase UID. */
public final class CloudSyncManager {

    private CloudSyncManager() {
    }

    public static void backupDataToCloud(Context context) {
        String userId = UserSessionManager.getCurrentUserId();
        if (userId.isEmpty()) {
            show(context, "Please sign in again before cloud backup.");
            return;
        }

        List<NoticeRecord> notices = NoticeStore.getAllNotices(context);
        if (notices.isEmpty()) {
            show(context, "No notices are available for cloud backup.");
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        int[] completed = {0};
        int[] failed = {0};

        show(context, "Secure cloud backup started.");

        for (NoticeRecord notice : notices) {
            Map<String, Object> noticeMap = new HashMap<>();
            noticeMap.put("noticeNumber", notice.getNoticeNumber());
            noticeMap.put("noticeType", notice.getNoticeType());
            noticeMap.put("primaryValue", notice.getPrimaryValue());
            noticeMap.put("secondaryValue", notice.getSecondaryValue());
            noticeMap.put("status", notice.getStatus());
            noticeMap.put("createdAt", notice.getCreatedAt());
            noticeMap.put("documentSnapshot", notice.getDocumentSnapshot());
            noticeMap.put("syncedAt", System.currentTimeMillis());

            String documentId = safeDocumentId(notice.getNoticeNumber());

            firestore.collection("users")
                    .document(userId)
                    .collection("notices")
                    .document(documentId)
                    .set(noticeMap, SetOptions.merge())
                    .addOnSuccessListener(ignored -> {
                        completed[0]++;
                        notifyBackupCompletion(
                                context,
                                notices.size(),
                                completed[0],
                                failed[0]
                        );
                    })
                    .addOnFailureListener(error -> {
                        failed[0]++;
                        notifyBackupCompletion(
                                context,
                                notices.size(),
                                completed[0],
                                failed[0]
                        );
                    });
        }
    }

    public static void restoreDataFromCloud(Context context) {
        String userId = UserSessionManager.getCurrentUserId();
        if (userId.isEmpty()) {
            show(context, "Please sign in again before cloud restore.");
            return;
        }

        show(context, "Checking your secure cloud backup.");

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notices")
                .get()
                .addOnSuccessListener(documents -> {
                    if (documents.isEmpty()) {
                        show(context, "No backup was found for this account.");
                        return;
                    }

                    int restoredCount = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot document
                            : documents) {
                        try {
                            NoticeRecord record = new NoticeRecord(
                                    document.getString("noticeNumber"),
                                    document.getString("noticeType"),
                                    document.getString("primaryValue"),
                                    document.getString("secondaryValue"),
                                    document.getString("status"),
                                    document.getLong("createdAt") == null
                                            ? 0L
                                            : document.getLong("createdAt"),
                                    document.getString("documentSnapshot")
                            );

                            NoticeStore.saveNotice(context, record);
                            restoredCount++;
                        } catch (Exception ignored) {
                            // A malformed remote record must not stop other records restoring.
                        }
                    }

                    show(
                            context,
                            restoredCount + " notices restored for this account."
                    );
                })
                .addOnFailureListener(error -> show(
                        context,
                        "Cloud restore could not be completed."
                ));
    }

    private static String safeDocumentId(String noticeNumber) {
        String value = noticeNumber == null ? "" : noticeNumber.trim();
        if (value.isEmpty()) {
            value = "notice_" + System.currentTimeMillis();
        }
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private static void notifyBackupCompletion(
            Context context,
            int total,
            int completed,
            int failed
    ) {
        if (completed + failed != total) {
            return;
        }

        if (failed == 0) {
            show(context, "Secure cloud backup completed.");
        } else {
            show(context, completed + " notices backed up; " + failed + " failed.");
        }
    }

    private static void show(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
