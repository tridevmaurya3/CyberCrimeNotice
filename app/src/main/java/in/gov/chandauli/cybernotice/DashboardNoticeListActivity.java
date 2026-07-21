package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardNoticeListActivity extends BaseActivity {

    public static final String EXTRA_FILTER = "dashboard_filter";
    public static final String EXTRA_NOTICE_TYPE = "dashboard_notice_type";

    public static final String FILTER_ALL = "ALL";
    public static final String FILTER_DRAFT = "DRAFT";
    public static final String FILTER_ISSUED = "ISSUED";
    public static final String FILTER_COMPLETED = "COMPLETED";

    private TextView tvListTitle;
    private TextView tvListSubtitle;
    private TextView tvEmptyState;
    private LinearLayout noticeListContainer;
    private String selectedFilter;
    private String selectedNoticeType;

    public static Intent createIntent(
            Context context,
            String filter
    ) {
        Intent intent = new Intent(
                context,
                DashboardNoticeListActivity.class
        );

        intent.putExtra(EXTRA_FILTER, filter);
        return intent;
    }

    public static Intent createIntent(
            Context context,
            String filter,
            String noticeType
    ) {
        Intent intent = createIntent(context, filter);
        intent.putExtra(EXTRA_NOTICE_TYPE, noticeType);
        return intent;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(
                LanguageManager.applyLanguage(newBase)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_notice_list);

        tvListTitle = findViewById(R.id.tvListTitle);
        tvListSubtitle = findViewById(R.id.tvListSubtitle);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        noticeListContainer = findViewById(
                R.id.noticeListContainer
        );

        Button btnBack = findViewById(R.id.btnBack);

        selectedFilter = getIntent().getStringExtra(EXTRA_FILTER);
        selectedNoticeType = getIntent().getStringExtra(EXTRA_NOTICE_TYPE);

        if (selectedFilter == null
                || selectedFilter.trim().isEmpty()) {
            selectedFilter = FILTER_ALL;
        }

        if (selectedNoticeType == null) {
            selectedNoticeType = "";
        }

        btnBack.setOnClickListener(view -> finish());

        updateHeader();
        refreshNoticeList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (noticeListContainer != null) {
            refreshNoticeList();
        }
    }

    private void updateHeader() {
        boolean isHindi = "hi".equals(
                LanguageManager.getLanguage(this)
        );

        String typeText = selectedNoticeType.trim().isEmpty()
                ? ""
                : getNoticeTypeText(selectedNoticeType);
        String statusText = getFilterText(isHindi);

        if (typeText.isEmpty()) {
            tvListTitle.setText(statusText);
            tvListSubtitle.setText(isHindi
                    ? "डैशबोर्ड से खोले गए नोटिस"
                    : "Notices opened from dashboard");
            return;
        }

        tvListTitle.setText(FILTER_ALL.equals(selectedFilter)
                ? typeText
                : typeText + " - " + statusText);
        tvListSubtitle.setText(isHindi
                ? typeText + " के " + statusText + " नोटिस"
                : statusText + " " + typeText + " notices");
    }

    private void refreshNoticeList() {
        List<NoticeRecord> allNotices =
                NoticeStore.getAllNotices(this);

        List<NoticeRecord> filteredNotices =
                new ArrayList<>();

        for (NoticeRecord notice : allNotices) {
            if (shouldShowNotice(notice)) {
                filteredNotices.add(notice);
            }
        }

        Collections.sort(
                filteredNotices,
                (first, second) -> Long.compare(
                        second.getCreatedAt(),
                        first.getCreatedAt()
                )
        );

        noticeListContainer.removeAllViews();

        if (filteredNotices.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyState.setVisibility(View.GONE);

        for (NoticeRecord notice : filteredNotices) {
            addNoticeCard(notice);
        }
    }

    private boolean shouldShowNotice(NoticeRecord notice) {
        if (!selectedNoticeType.trim().isEmpty()
                && !selectedNoticeType.equals(notice.getNoticeType())) {
            return false;
        }

        if (FILTER_DRAFT.equals(selectedFilter)) {
            return "DRAFT".equalsIgnoreCase(
                    notice.getStatus()
            );
        }

        if (FILTER_ISSUED.equals(selectedFilter)) {
            return "ISSUED".equalsIgnoreCase(
                    notice.getStatus()
            );
        }

        if (FILTER_COMPLETED.equals(selectedFilter)) {
            return "COMPLETED".equalsIgnoreCase(
                    notice.getStatus()
            );
        }

        return true;
    }

    private String getFilterText(boolean isHindi) {
        if (FILTER_DRAFT.equals(selectedFilter)) {
            return isHindi ? "ड्राफ्ट" : "Draft Notices";
        }
        if (FILTER_ISSUED.equals(selectedFilter)) {
            return isHindi ? "भेजे गए" : "Issued Notices";
        }
        if (FILTER_COMPLETED.equals(selectedFilter)) {
            return isHindi ? "पूर्ण" : "Completed Notices";
        }
        return isHindi ? "सभी नोटिस" : "All Notices";
    }

    private void addNoticeCard(NoticeRecord notice) {
        MaterialCardView cardView =
                new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(12));

        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dp(18));
        cardView.setCardElevation(dp(6));
        cardView.setStrokeWidth(dp(1));
        cardView.setStrokeColor(
                Color.parseColor("#C9DCF9")
        );
        cardView.setCardBackgroundColor(Color.WHITE);
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setForeground(
                getDrawable(
                        android.R.drawable
                                .list_selector_background
                )
        );

        LinearLayout contentLayout =
                new LinearLayout(this);

        contentLayout.setOrientation(
                LinearLayout.VERTICAL
        );
        contentLayout.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );

        TextView tvNoticeNumber = createTextView(
                notice.getNoticeNumber(),
                17,
                Color.parseColor("#063B82"),
                Typeface.BOLD
        );

        TextView tvNoticeType = createTextView(
                getNoticeTypeText(notice.getNoticeType()),
                13,
                Color.parseColor("#4B5C74"),
                Typeface.NORMAL
        );

        TextView tvPrimaryValue = createTextView(
                getSafeValue(notice.getPrimaryValue()),
                14,
                Color.parseColor("#263238"),
                Typeface.NORMAL
        );

        TextView tvCreatedDate = createTextView(
                getCreatedText(notice.getCreatedAt()),
                12,
                Color.parseColor("#708090"),
                Typeface.NORMAL
        );

        TextView tvStatus = createTextView(
                getStatusText(notice.getStatus()),
                12,
                Color.WHITE,
                Typeface.BOLD
        );

        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(
                dp(12),
                dp(5),
                dp(12),
                dp(5)
        );
        tvStatus.setBackground(
                createStatusBackground(
                        getStatusColor(notice.getStatus())
                )
        );

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(10);
        tvStatus.setLayoutParams(statusParams);

        addBottomMargin(tvNoticeType, 5);
        addBottomMargin(tvPrimaryValue, 8);
        addBottomMargin(tvCreatedDate, 4);

        contentLayout.addView(tvNoticeNumber);
        contentLayout.addView(tvNoticeType);

        if (!getSafeValue(notice.getPrimaryValue()).isEmpty()) {
            contentLayout.addView(tvPrimaryValue);
        }

        contentLayout.addView(tvCreatedDate);
        contentLayout.addView(tvStatus);

        cardView.addView(contentLayout);

        cardView.setOnClickListener(view -> {
            Intent intent = NoticeDetailActivity.createIntent(
                    DashboardNoticeListActivity.this,
                    notice
            );

            startActivity(intent);
        });

        noticeListContainer.addView(cardView);
    }

    private TextView createTextView(
            String text,
            int textSize,
            int textColor,
            int textStyle
    ) {
        TextView textView = new TextView(this);

        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(textColor);
        textView.setTypeface(null, textStyle);
        textView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return textView;
    }

    private void addBottomMargin(
            TextView textView,
            int marginDp
    ) {
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams)
                        textView.getLayoutParams();

        params.bottomMargin = dp(marginDp);
        textView.setLayoutParams(params);
    }

    private GradientDrawable createStatusBackground(
            int color
    ) {
        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(20));

        return drawable;
    }

    private String getNoticeTypeText(String noticeType) {
        boolean isHindi = "hi".equals(
                LanguageManager.getLanguage(this)
        );

        if ("SECTION_94".equals(noticeType)) {
            return isHindi
                    ? "धारा 94 बीएनएसएस नोटिस"
                    : "Section 94 BNSS Notice";
        }

        if ("SECTION_35".equals(noticeType)) {
            return isHindi
                    ? "धारा 35(3) बीएनएसएस नोटिस"
                    : "Section 35(3) BNSS Notice";
        }

        if ("CDR".equals(noticeType)) {
            return isHindi
                    ? "सीडीआर प्रोफार्मा"
                    : "CDR Proforma";
        }

        if ("COURT_RELEASE".equals(noticeType)) {
            return isHindi
                    ? "न्यायालय रिलीज आदेश"
                    : "Court Release Order";
        }

        return noticeType == null ? "" : noticeType;
    }

    private String getStatusText(String status) {
        boolean isHindi = "hi".equals(
                LanguageManager.getLanguage(this)
        );

        if ("DRAFT".equalsIgnoreCase(status)) {
            return isHindi ? "ड्राफ्ट" : "DRAFT";
        }

        if ("ISSUED".equalsIgnoreCase(status)) {
            return isHindi ? "जारी" : "ISSUED";
        }

        if ("COMPLETED".equalsIgnoreCase(status)) {
            return isHindi ? "पूर्ण" : "COMPLETED";
        }

        return status == null ? "" : status;
    }

    private int getStatusColor(String status) {
        if ("DRAFT".equalsIgnoreCase(status)) {
            return Color.parseColor("#F57C00");
        }

        if ("COMPLETED".equalsIgnoreCase(status)) {
            return Color.parseColor("#2E7D32");
        }

        return Color.parseColor("#1565C0");
    }

    private String getCreatedText(long createdAt) {
        boolean isHindi = "hi".equals(
                LanguageManager.getLanguage(this)
        );

        Locale locale = isHindi
                ? new Locale("hi", "IN")
                : Locale.ENGLISH;

        String date = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                locale
        ).format(new Date(createdAt));

        return isHindi
                ? "बनाया गया: " + date
                : "Created: " + date;
    }

    private String getSafeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
