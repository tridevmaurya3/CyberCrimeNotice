package in.gov.chandauli.cybernotice;

public class NoticeActivityRecord {

    private final String noticeNumber;
    private final String noticeType;
    private final String action;
    private final long activityTime;

    public NoticeActivityRecord(
            String noticeNumber,
            String noticeType,
            String action,
            long activityTime
    ) {
        this.noticeNumber = noticeNumber;
        this.noticeType = noticeType;
        this.action = action;
        this.activityTime = activityTime;
    }

    public String getNoticeNumber() {
        return noticeNumber;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public String getAction() {
        return action;
    }

    public long getActivityTime() {
        return activityTime;
    }
}