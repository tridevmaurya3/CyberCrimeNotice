package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton; // <-- यहाँ यह इंपोर्ट जोड़ा गया है
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends BaseActivity {

    private static final int MAX_RECENT_ACTIVITY = 10;

    private TextView tvDashboardName;
    private TextView tvStationName;
    private TextView tvWelcome;
    private TextView tvDateTime;
    private TextView tvQuickInformationTitle;
    private TextView tvQuickTaskTitle;
    private TextView tvRecentActivityTitle;

    private LinearLayout noticeStatisticsContainer;
    private LinearLayout activityContainer;

    private Button btnManageNotices;
    private Button btnSection94;
    private Button btnSection35;
    private Button btnCdrProforma;
    private Button btnExportReport;
    private Button btnCloudSync;
    private Button btnOfficerProfile;
    private Button btnLogout;
    private ImageButton btnSettings;
    private BottomNavigationView bottomNavigation;
    private MaterialButton btnOpenCourtRelease; // <-- नया बटन यहाँ डिक्लेयर किया गया

    private String username;
    private boolean shouldAnimateCards = true;

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateDateTime();
            clockHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!UserSessionManager.isSignedIn()) {
            openLogin();
            return;
        }

        setContentView(R.layout.activity_dashboard);

        bindViews();
        setupClicks();
        setupBottomNavigation();
        updateDashboardLanguage();
        applyGenericDashboardHeader();

        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.trim().isEmpty()) {
            username = UserSessionManager.getCurrentUserEmail();
        }
        if (username == null || username.trim().isEmpty()) {
            username = "Officer";
        }

        tvWelcome.setText(getString(R.string.welcome_user, username));
        clockHandler.post(clockRunnable);

        updateQuickStatistics();

        if (shouldAnimateCards) {
            animateStatisticsCards();
            shouldAnimateCards = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!UserSessionManager.isSignedIn()) {
            openLogin();
            return;
        }

        applyGenericDashboardHeader();
        updateQuickStatistics();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void bindViews() {
        tvDashboardName = findViewById(R.id.tvDashboardName);
        tvStationName = findViewById(R.id.tvStationName);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvQuickInformationTitle = findViewById(R.id.tvQuickInformationTitle);
        tvQuickTaskTitle = findViewById(R.id.tvQuickTaskTitle);
        tvRecentActivityTitle = findViewById(R.id.tvRecentActivityTitle);
        noticeStatisticsContainer = findViewById(R.id.noticeStatisticsContainer);
        activityContainer = findViewById(R.id.activityContainer);
        btnManageNotices = findViewById(R.id.btnManageNotices);
        btnSection94 = findViewById(R.id.btnSection94);
        btnSection35 = findViewById(R.id.btnSection35);
        btnCdrProforma = findViewById(R.id.btnCdrProforma);
        btnExportReport = findViewById(R.id.btnExportReport);
        btnCloudSync = findViewById(R.id.btnCloudSync);
        btnOfficerProfile = findViewById(R.id.btnOfficerProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnSettings = findViewById(R.id.btnSettings);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        btnOpenCourtRelease = findViewById(R.id.btnOpenCourtRelease); // <-- नया बटन बाइंड किया गया
    }

    private void setupClicks() {
        btnManageNotices.setOnClickListener(view ->
                startActivity(new Intent(DashboardActivity.this, NoticeListActivity.class))
        );

        btnSection94.setOnClickListener(view -> openNewForm(
                "section_94_draft",
                Section94FormActivity.class
        ));

        btnSection35.setOnClickListener(view -> openNewForm(
                "section_35_draft",
                Section35FormActivity.class
        ));

        btnCdrProforma.setOnClickListener(view -> {
            UserSessionManager.getScopedPreferences(
                    this,
                    "cdr_proforma_draft"
            ).edit().clear().apply();
            startActivity(new Intent(DashboardActivity.this, CdrProformaActivity.class));
        });

        // <-- नया फॉर्म खोलने का क्लिक इवेंट
        if (btnOpenCourtRelease != null) {
            btnOpenCourtRelease.setOnClickListener(view -> {
                startActivity(new Intent(DashboardActivity.this, CourtReleaseActivity.class));
            });
        }

        if (btnExportReport != null) {
            btnExportReport.setOnClickListener(view -> {
                CsvExportManager.exportAndShareReport(DashboardActivity.this);
            });
        }

        if (btnCloudSync != null) {
            btnCloudSync.setOnClickListener(view -> showCloudSyncDialog());
        }

        btnOfficerProfile.setOnClickListener(view ->
                startActivity(new Intent(DashboardActivity.this, OfficerProfileActivity.class))
        );

        btnSettings.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, LanguageSettingsActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(view -> {
            RememberMeManager.clearOnLogout(DashboardActivity.this);
            UserSessionManager.signOut();
            Toast.makeText(DashboardActivity.this, getString(R.string.secure_logout), Toast.LENGTH_SHORT).show();
            openLogin();
        });
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_notices) {
                    startActivity(new Intent(DashboardActivity.this, NoticeListActivity.class));
                    return false;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(DashboardActivity.this, OfficerProfileActivity.class));
                    return false;
                }
                return false;
            });
        }
    }

    private void showCloudSyncDialog() {
        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
        String title = isHindi ? "क्लाउड सिंक (Firebase)" : "Cloud Sync (Firebase)";
        String[] options = isHindi
                ? new String[]{"क्लाउड पर बैकअप लें (Backup)", "क्लाउड से रिस्टोर करें (Restore)"}
                : new String[]{"Backup Data to Cloud", "Restore Data from Cloud"};

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        CloudSyncManager.backupDataToCloud(DashboardActivity.this);
                    } else {
                        CloudSyncManager.restoreDataFromCloud(DashboardActivity.this);
                        new Handler(Looper.getMainLooper()).postDelayed(this::updateQuickStatistics, 3000);
                    }
                })
                .setNegativeButton(isHindi ? "रद्द करें" : "CANCEL", null)
                .show();
    }

    private void openNoticeList(String filter, String noticeType) {
        startActivity(DashboardNoticeListActivity.createIntent(
                this,
                filter,
                noticeType
        ));
    }

    private void applyGenericDashboardHeader() {
        tvDashboardName.setText(R.string.dashboard_app_heading);

        OfficerProfileManager.OfficerProfile profile =
                OfficerProfileManager.getProfile(this);

        if (profile.getDistrict().isEmpty()
                || profile.getPoliceStation().isEmpty()) {
            tvStationName.setText(R.string.dashboard_location_pending);
            return;
        }

        tvStationName.setText(getString(
                R.string.dashboard_location_selected,
                profile.getPoliceStation(),
                profile.getDistrict()
        ));
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }

    private void openNewForm(
            String draftPreference,
            Class<?> activityClass
    ) {
        UserSessionManager.getScopedPreferences(this, draftPreference)
                .edit()
                .clear()
                .apply();

        startActivity(new Intent(this, activityClass));
    }

    private void updateDashboardLanguage() {
        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));

        tvDashboardName.setText(isHindi ? "साइबर क्राइम पुलिस स्टेशन" : "CYBER CRIME POLICE STATION");
        tvStationName.setText(isHindi ? "चन्दौली, उत्तर प्रदेश" : "Chandauli, Uttar Pradesh");
        tvQuickInformationTitle.setText(isHindi ? "नोटिस आंकड़े" : "Notice Statistics");
        tvQuickTaskTitle.setText(isHindi ? "त्वरित कार्य" : "Quick Actions");
        tvRecentActivityTitle.setText(isHindi ? "हाल की गतिविधि" : "Recent Activity");
        btnSection94.setText(isHindi ? "धारा 94" : "Section 94");
        btnSection35.setText(isHindi ? "धारा 35(3)" : "Section 35(3)");
        btnCdrProforma.setText(isHindi ? "सीडीआर प्रोफार्मा" : "CDR Proforma");
        btnManageNotices.setText(isHindi ? "नोटिस रजिस्टर" : "Notice Register");
        btnOfficerProfile.setText(isHindi ? "अधिकारी प्रोफाइल" : "Officer Profile");
        btnLogout.setText(isHindi ? "लॉग आउट" : "Logout");

        if (btnExportReport != null) {
            btnExportReport.setText(isHindi ? "रिपोर्ट निर्यात" : "Export Report");
        }
        if (btnCloudSync != null) {
            btnCloudSync.setText(isHindi ? "क्लाउड सिंक" : "Cloud Sync");
        }

        if (btnOpenCourtRelease != null) {
            btnOpenCourtRelease.setText(isHindi ? "न्यायालय रिलीज आदेश" : "Court Release Order");
        }
        applyGenericDashboardHeader();
    }

    private void updateQuickStatistics() {
        List<NoticeRecord> noticeList = NoticeStore.getAllNotices(this);
        renderNoticeStatistics(noticeList);
        updateRecentActivity(noticeList);
    }

    private void renderNoticeStatistics(List<NoticeRecord> noticeList) {
        if (noticeStatisticsContainer == null) {
            return;
        }

        noticeStatisticsContainer.removeAllViews();

        addNoticeTypeStatistics(noticeList, "SECTION_94");
        addNoticeTypeStatistics(noticeList, "SECTION_35");
        addNoticeTypeStatistics(noticeList, "CDR");
        addNoticeTypeStatistics(noticeList, "COURT_RELEASE");
    }

    private void addNoticeTypeStatistics(
            List<NoticeRecord> noticeList,
            String noticeType
    ) {
        LinearLayout sectionLayout = new LinearLayout(this);
        sectionLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams sectionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        sectionParams.setMargins(0, 0, 0, dp(20));
        sectionLayout.setLayoutParams(sectionParams);

        TextView typeTitle = createTextView(
                getShortNoticeType(noticeType),
                15,
                Color.parseColor("#242424"),
                Typeface.BOLD
        );
        typeTitle.setPadding(dp(2), 0, 0, dp(8));
        sectionLayout.addView(typeTitle);

        sectionLayout.addView(createStatisticsRow(
                noticeList,
                noticeType,
                DashboardNoticeListActivity.FILTER_ALL,
                DashboardNoticeListActivity.FILTER_DRAFT
        ));
        sectionLayout.addView(createStatisticsRow(
                noticeList,
                noticeType,
                DashboardNoticeListActivity.FILTER_ISSUED,
                DashboardNoticeListActivity.FILTER_COMPLETED
        ));

        noticeStatisticsContainer.addView(sectionLayout);
    }

    private LinearLayout createStatisticsRow(
            List<NoticeRecord> noticeList,
            String noticeType,
            String firstStatus,
            String secondStatus
    ) {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(82)
                );
        rowParams.setMargins(0, 0, 0, dp(8));
        rowLayout.setLayoutParams(rowParams);

        rowLayout.addView(createStatisticsCard(
                noticeList,
                noticeType,
                firstStatus,
                true
        ));
        rowLayout.addView(createStatisticsCard(
                noticeList,
                noticeType,
                secondStatus,
                false
        ));

        return rowLayout;
    }

    private MaterialCardView createStatisticsCard(
            List<NoticeRecord> noticeList,
            String noticeType,
            String status,
            boolean isLeftCard
    ) {
        MaterialCardView cardView = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(0, dp(82), 1f);

        if (isLeftCard) {
            cardParams.setMargins(0, 0, dp(4), 0);
        } else {
            cardParams.setMargins(dp(4), 0, 0, 0);
        }

        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dp(8));
        cardView.setCardElevation(0f);
        cardView.setStrokeWidth(dp(1));
        cardView.setStrokeColor(Color.parseColor("#E1DFDD"));
        cardView.setCardBackgroundColor(Color.WHITE);
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setForeground(getDrawable(
                android.R.drawable.list_selector_background
        ));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setGravity(Gravity.CENTER);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView countView = new TextView(this);
        countView.setText(String.valueOf(
                getNoticeCount(noticeList, noticeType, status)
        ));
        countView.setTextColor(getStatisticColor(status));
        countView.setTextSize(25);
        countView.setTypeface(null, Typeface.BOLD);
        countView.setGravity(Gravity.CENTER);

        TextView labelView = new TextView(this);
        labelView.setText(getStatisticLabel(status));
        labelView.setTextColor(Color.parseColor("#616161"));
        labelView.setTextSize(12);
        labelView.setGravity(Gravity.CENTER);
        labelView.setPadding(0, dp(3), 0, 0);

        contentLayout.addView(countView);
        contentLayout.addView(labelView);
        cardView.addView(contentLayout);

        cardView.setOnClickListener(view ->
                openNoticeList(status, noticeType)
        );

        return cardView;
    }

    private int getNoticeCount(
            List<NoticeRecord> noticeList,
            String noticeType,
            String status
    ) {
        int count = 0;

        for (NoticeRecord notice : noticeList) {
            if (!noticeType.equals(notice.getNoticeType())) {
                continue;
            }

            if (DashboardNoticeListActivity.FILTER_ALL.equals(status)
                    || status.equalsIgnoreCase(notice.getStatus())) {
                count++;
            }
        }

        return count;
    }

    private String getStatisticLabel(String status) {
        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));

        if (DashboardNoticeListActivity.FILTER_ALL.equals(status)) {
            return isHindi ? "कुल" : "Total";
        }
        if (DashboardNoticeListActivity.FILTER_DRAFT.equals(status)) {
            return isHindi ? "ड्राफ्ट" : "Draft";
        }
        if (DashboardNoticeListActivity.FILTER_ISSUED.equals(status)) {
            return isHindi ? "भेजी गई" : "Issued";
        }
        return isHindi ? "पूर्ण" : "Completed";
    }

    private int getStatisticColor(String status) {
        if (DashboardNoticeListActivity.FILTER_DRAFT.equals(status)) {
            return Color.parseColor("#D83B01");
        }
        if (DashboardNoticeListActivity.FILTER_ISSUED.equals(status)) {
            return Color.parseColor("#5C2D91");
        }
        if (DashboardNoticeListActivity.FILTER_COMPLETED.equals(status)) {
            return Color.parseColor("#107C41");
        }
        return Color.parseColor("#0F6CBD");
    }

    private void updateRecentActivity(List<NoticeRecord> noticeList) {
        activityContainer.removeAllViews();
        List<NoticeActivityRecord> activityList = NoticeActivityStore.getRecentActivities(this, MAX_RECENT_ACTIVITY);

        if (!activityList.isEmpty()) {
            for (NoticeActivityRecord activity : activityList) {
                NoticeRecord notice = findNotice(noticeList, activity.getNoticeNumber());
                addActivityCard(
                        getAuditActivityText(activity),
                        getActivityDate(activity.getActivityTime()),
                        getActionColor(activity.getAction()),
                        notice
                );
            }
            return;
        }

        addOldNoticeFallback(noticeList);
    }

    private void addOldNoticeFallback(List<NoticeRecord> noticeList) {
        if (noticeList.isEmpty()) {
            addEmptyActivityMessage();
            return;
        }

        List<NoticeRecord> oldNotices = new ArrayList<>(noticeList);
        Collections.sort(oldNotices, (first, second) -> Long.compare(second.getCreatedAt(), first.getCreatedAt()));

        int visibleCount = Math.min(MAX_RECENT_ACTIVITY, oldNotices.size());
        for (int index = 0; index < visibleCount; index++) {
            NoticeRecord notice = oldNotices.get(index);
            addActivityCard(
                    getOldNoticeText(notice),
                    getActivityDate(notice.getCreatedAt()),
                    getStatusColor(notice.getStatus()),
                    notice
            );
        }
    }

    private NoticeRecord findNotice(List<NoticeRecord> noticeList, String noticeNumber) {
        for (NoticeRecord notice : noticeList) {
            if (notice.getNoticeNumber().equals(noticeNumber)) {
                return notice;
            }
        }
        return null;
    }

    private void addActivityCard(String activityText, String activityDate, int indicatorColor, NoticeRecord linkedNotice) {
        MaterialCardView cardView = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(10));
        cardView.setLayoutParams(cardParams);

        cardView.setRadius(dp(8));
        cardView.setCardElevation(dp(1));
        cardView.setStrokeWidth(dp(1));
        cardView.setStrokeColor(Color.parseColor("#E1DFDD"));
        cardView.setCardBackgroundColor(Color.WHITE);

        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        rowLayout.setPadding(dp(14), dp(12), dp(14), dp(12));

        TextView statusDot = new TextView(this);
        statusDot.setLayoutParams(new LinearLayout.LayoutParams(dp(12), dp(12)));
        statusDot.setBackground(createRoundedBackground(indicatorColor, dp(12)));

        LinearLayout textLayout = new LinearLayout(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textParams.setMargins(dp(12), 0, dp(8), 0);
        textLayout.setLayoutParams(textParams);
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tvActivityText = createTextView(activityText, 14, Color.parseColor("#242424"), Typeface.BOLD);
        TextView tvActivityDate = createTextView(activityDate, 12, Color.parseColor("#616161"), Typeface.NORMAL);
        tvActivityDate.setPadding(0, dp(4), 0, 0);

        TextView tvOpen = createTextView(linkedNotice == null ? "" : "›", 24, Color.parseColor("#0F6CBD"), Typeface.BOLD);
        tvOpen.setGravity(Gravity.CENTER);
        tvOpen.setLayoutParams(new LinearLayout.LayoutParams(dp(24), LinearLayout.LayoutParams.WRAP_CONTENT));

        textLayout.addView(tvActivityText);
        textLayout.addView(tvActivityDate);
        rowLayout.addView(statusDot);
        rowLayout.addView(textLayout);
        rowLayout.addView(tvOpen);
        cardView.addView(rowLayout);

        if (linkedNotice != null) {
            cardView.setClickable(true);
            cardView.setFocusable(true);
            cardView.setOnClickListener(view -> {
                Intent intent = NoticeDetailActivity.createIntent(DashboardActivity.this, linkedNotice);
                startActivity(intent);
            });
        }
        activityContainer.addView(cardView);
    }

    private void addEmptyActivityMessage() {
        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
        TextView emptyView = createTextView(
                isHindi ? "अभी कोई नोटिस गतिविधि उपलब्ध नहीं है।" : "No notice activity is available yet.",
                14, Color.parseColor("#616161"), Typeface.NORMAL
        );
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(0, dp(24), 0, dp(24));
        activityContainer.addView(emptyView);
    }

    private String getAuditActivityText(NoticeActivityRecord activity) {
        return activity.getNoticeNumber() + " • " + getShortNoticeType(activity.getNoticeType()) + " • " + getActionText(activity.getAction());
    }

    private String getOldNoticeText(NoticeRecord notice) {
        return notice.getNoticeNumber() + " • " + getShortNoticeType(notice.getNoticeType()) + " • " + getStatusText(notice.getStatus());
    }

    private String getShortNoticeType(String noticeType) {
        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
        if ("SECTION_94".equals(noticeType)) return isHindi ? "धारा 94 बीएनएसएस" : "Section 94 BNSS";
        if ("SECTION_35".equals(noticeType)) return isHindi ? "धारा 35(3) बीएनएसएस" : "Section 35(3) BNSS";
        if ("CDR".equals(noticeType)) return isHindi ? "सीडीआर" : "CDR";
        if ("COURT_RELEASE".equals(noticeType)) return isHindi ? "न्यायालय रिलीज आदेश" : "Court Release Order";
        return noticeType == null ? "" : noticeType;
    }

    private String getActionText(String action) {
        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
        if (NoticeActivityStore.ACTION_CREATED.equals(action)) return isHindi ? "नोटिस बनाया गया" : "Notice Created";
        if (NoticeActivityStore.ACTION_UPDATED.equals(action)) return isHindi ? "नोटिस अपडेट किया गया" : "Notice Updated";
        if (NoticeActivityStore.ACTION_DRAFT.equals(action)) return isHindi ? "ड्राफ्ट सहेजा गया" : "Draft Saved";
        if (NoticeActivityStore.ACTION_ISSUED.equals(action)) return isHindi ? "नोटिस जारी किया गया" : "Notice Issued";
        if (NoticeActivityStore.ACTION_COMPLETED.equals(action)) return isHindi ? "नोटिस पूर्ण किया गया" : "Notice Completed";
        return action == null ? "" : action;
    }

    private int getActionColor(String action) {
        if (NoticeActivityStore.ACTION_DRAFT.equals(action)) return Color.parseColor("#D83B01");
        if (NoticeActivityStore.ACTION_COMPLETED.equals(action)) return Color.parseColor("#107C41");
        if (NoticeActivityStore.ACTION_ISSUED.equals(action)) return Color.parseColor("#0F6CBD");
        if (NoticeActivityStore.ACTION_CREATED.equals(action)) return Color.parseColor("#5C2D91");
        return Color.parseColor("#616161");
    }

    private String getStatusText(String status) {
        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
        if ("DRAFT".equalsIgnoreCase(status)) return isHindi ? "ड्राफ्ट सहेजा गया" : "Draft Saved";
        if ("ISSUED".equalsIgnoreCase(status)) return isHindi ? "नोटिस जारी किया गया" : "Notice Issued";
        if ("COMPLETED".equalsIgnoreCase(status)) return isHindi ? "नोटिस पूर्ण किया गया" : "Notice Completed";
        return status == null ? "" : status;
    }

    private int getStatusColor(String status) {
        if ("DRAFT".equalsIgnoreCase(status)) return Color.parseColor("#D83B01");
        if ("COMPLETED".equalsIgnoreCase(status)) return Color.parseColor("#107C41");
        return Color.parseColor("#0F6CBD");
    }

    private String getActivityDate(long activityTime) {
        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
        Locale locale = isHindi ? new Locale("hi", "IN") : Locale.ENGLISH;
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", locale).format(new Date(activityTime));
    }

    private TextView createTextView(String text, int textSize, int textColor, int typeface) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(textColor);
        textView.setTypeface(null, typeface);
        return textView;
    }

    private GradientDrawable createRoundedBackground(int color, int cornerRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius);
        return drawable;
    }

    private void animateStatisticsCards() {
        if (noticeStatisticsContainer == null) {
            return;
        }

        for (int index = 0; index < noticeStatisticsContainer.getChildCount(); index++) {
            View section = noticeStatisticsContainer.getChildAt(index);
            section.setAlpha(0f);
            section.setScaleX(0.96f);
            section.setScaleY(0.96f);
            section.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(280)
                    .setStartDelay(index * 70L)
                    .start();
        }
    }

    private void updateDateTime() {
        String selectedLanguage = LanguageManager.getLanguage(this);
        Locale dateLocale = "hi".equals(selectedLanguage) ? new Locale("hi", "IN") : Locale.ENGLISH;
        String currentDateTime = new SimpleDateFormat("dd MMMM yyyy | hh:mm:ss a", dateLocale).format(new Date());
        tvDateTime.setText(getString(R.string.date_label, currentDateTime));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacks(clockRunnable);
    }
}
