package in.gov.chandauli.cybernotice;

public class NoticeRecord {

    private final String noticeNumber;
    private final String noticeType;
    private final String primaryValue;
    private final String secondaryValue;
    private final String status;
    private final long createdAt;
    private final String documentSnapshot;

    public NoticeRecord(
            String noticeNumber,
            String noticeType,
            String primaryValue,
            String secondaryValue,
            String status,
            long createdAt
    ) {
        this(
                noticeNumber,
                noticeType,
                primaryValue,
                secondaryValue,
                status,
                createdAt,
                ""
        );
    }

    public NoticeRecord(
            String noticeNumber,
            String noticeType,
            String primaryValue,
            String secondaryValue,
            String status,
            long createdAt,
            String documentSnapshot
    ) {
        this.noticeNumber = noticeNumber;
        this.noticeType = noticeType;
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
        this.status = status;
        this.createdAt = createdAt;
        this.documentSnapshot = documentSnapshot == null
                ? ""
                : documentSnapshot;
    }

    public String getNoticeNumber() {
        return noticeNumber;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public String getPrimaryValue() {
        return primaryValue;
    }

    public String getSecondaryValue() {
        return secondaryValue;
    }

    public String getStatus() {
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getDocumentSnapshot() {
        return documentSnapshot;
    }

    public boolean hasDocumentSnapshot() {
        return !documentSnapshot.trim().isEmpty();
    }
}