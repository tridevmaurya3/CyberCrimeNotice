package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeListActivity extends BaseActivity {

    private Button btnBack;
    private Button btnAddNotice;

    private TextInputEditText etNoticeSearch;
    private TextView tvFilterResult;
    private LinearLayout noticeContainer;

    private MaterialButton btnTypeAll;
    private MaterialButton btnType94;
    private MaterialButton btnType35;
    private MaterialButton btnTypeCdr;

    private MaterialButton btnStatusAll;
    private MaterialButton btnStatusDraft;
    private MaterialButton btnStatusIssued;
    private MaterialButton btnStatusCompleted;

    private final List<NoticeRecord> allNoticeList =
            new ArrayList<>();

    private String selectedType = "ALL";
    private String selectedStatus = "ALL";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_list);

        bindViews();
        setupListeners();
        updateFilterButtonStyles();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddNotice = findViewById(R.id.btnAddNotice);

        etNoticeSearch = findViewById(R.id.etNoticeSearch);
        tvFilterResult = findViewById(R.id.tvFilterResult);
        noticeContainer = findViewById(R.id.noticeContainer);

        btnTypeAll = findViewById(R.id.btnTypeAll);
        btnType94 = findViewById(R.id.btnType94);
        btnType35 = findViewById(R.id.btnType35);
        btnTypeCdr = findViewById(R.id.btnTypeCdr);

        btnStatusAll = findViewById(R.id.btnStatusAll);
        btnStatusDraft = findViewById(R.id.btnStatusDraft);
        btnStatusIssued = findViewById(R.id.btnStatusIssued);
        btnStatusCompleted = findViewById(
                R.id.btnStatusCompleted
        );
    }

    private void setupListeners() {
        btnBack.setOnClickListener(view -> finish());

        btnAddNotice.setOnClickListener(view -> {
            Intent intent = new Intent(
                    NoticeListActivity.this,
                    NoticeTypeActivity.class
            );

            startActivity(intent);
        });

        etNoticeSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        applyFilters();
                    }
                }
        );

        btnTypeAll.setOnClickListener(
                view -> changeTypeFilter("ALL")
        );

        btnType94.setOnClickListener(
                view -> changeTypeFilter("SECTION_94")
        );

        btnType35.setOnClickListener(
                view -> changeTypeFilter("SECTION_35")
        );

        btnTypeCdr.setOnClickListener(
                view -> changeTypeFilter("CDR")
        );

        btnStatusAll.setOnClickListener(
                view -> changeStatusFilter("ALL")
        );

        btnStatusDraft.setOnClickListener(
                view -> changeStatusFilter("DRAFT")
        );

        btnStatusIssued.setOnClickListener(
                view -> changeStatusFilter("ISSUED")
        );

        btnStatusCompleted.setOnClickListener(
                view -> changeStatusFilter("COMPLETED")
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRegisteredNotices();
    }

    private void changeTypeFilter(String noticeType) {
        selectedType = noticeType;
        updateFilterButtonStyles();
        applyFilters();
    }

    private void changeStatusFilter(String status) {
        selectedStatus = status;
        updateFilterButtonStyles();
        applyFilters();
    }

    private void loadRegisteredNotices() {
        allNoticeList.clear();

        allNoticeList.addAll(
                NoticeStore.getAllNotices(this)
        );

        applyFilters();
    }

    private void applyFilters() {
        if (noticeContainer == null) {
            return;
        }

        List<NoticeRecord> filteredNotices =
                new ArrayList<>();

        String query = getSearchQuery();

        for (NoticeRecord noticeRecord : allNoticeList) {
            if (!matchesSelectedType(noticeRecord)) {
                continue;
            }

            if (!matchesSelectedStatus(noticeRecord)) {
                continue;
            }

            if (!matchesSearchQuery(noticeRecord, query)) {
                continue;
            }

            filteredNotices.add(noticeRecord);
        }

        noticeContainer.removeAllViews();

        tvFilterResult.setText(
                getString(
                        R.string.register_filter_result,
                        filteredNotices.size()
                )
        );

        if (filteredNotices.isEmpty()) {
            addEmptyMessage(allNoticeList.isEmpty());
            return;
        }

        for (NoticeRecord noticeRecord : filteredNotices) {
            addNoticeCard(noticeRecord);
        }
    }

    private String getSearchQuery() {
        if (etNoticeSearch.getText() == null) {
            return "";
        }

        return etNoticeSearch.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private boolean matchesSelectedType(
            NoticeRecord noticeRecord
    ) {
        return selectedType.equals("ALL")
                || selectedType.equals(
                noticeRecord.getNoticeType()
        );
    }

    private boolean matchesSelectedStatus(
            NoticeRecord noticeRecord
    ) {
        return selectedStatus.equals("ALL")
                || selectedStatus.equals(
                noticeRecord.getStatus()
        );
    }

    private boolean matchesSearchQuery(
            NoticeRecord noticeRecord,
            String query
    ) {
        if (query.isEmpty()) {
            return true;
        }

        String searchableText =
                safe(noticeRecord.getNoticeNumber())
                        + " "
                        + safe(noticeRecord.getNoticeType())
                        + " "
                        + safe(getNoticeTypeText(
                        noticeRecord.getNoticeType()
                ))
                        + " "
                        + safe(noticeRecord.getPrimaryValue())
                        + " "
                        + safe(noticeRecord.getSecondaryValue())
                        + " "
                        + safe(noticeRecord.getStatus())
                        + " "
                        + safe(getStatusText(
                        noticeRecord.getStatus()
                ));

        return searchableText
                .toLowerCase(Locale.ROOT)
                .contains(query);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void updateFilterButtonStyles() {
        updateFilterButton(
                btnTypeAll,
                selectedType.equals("ALL"),
                "#0D47A1"
        );

        updateFilterButton(
                btnType94,
                selectedType.equals("SECTION_94"),
                "#0D47A1"
        );

        updateFilterButton(
                btnType35,
                selectedType.equals("SECTION_35"),
                "#0D47A1"
        );

        updateFilterButton(
                btnTypeCdr,
                selectedType.equals("CDR"),
                "#0D47A1"
        );

        updateFilterButton(
                btnStatusAll,
                selectedStatus.equals("ALL"),
                "#0D47A1"
        );

        updateFilterButton(
                btnStatusDraft,
                selectedStatus.equals("DRAFT"),
                "#8A5500"
        );

        updateFilterButton(
                btnStatusIssued,
                selectedStatus.equals("ISSUED"),
                "#0D47A1"
        );

        updateFilterButton(
                btnStatusCompleted,
                selectedStatus.equals("COMPLETED"),
                "#2E7D32"
        );
    }

    private void updateFilterButton(
            MaterialButton button,
            boolean selected,
            String accentColor
    ) {
        int accent = Color.parseColor(accentColor);

        button.setTextColor(
                selected ? Color.WHITE : accent
        );

        button.setBackgroundTintList(
                ColorStateList.valueOf(
                        selected ? accent : Color.WHITE
                )
        );

        button.setStrokeColor(
                ColorStateList.valueOf(accent)
        );

        button.setStrokeWidth(dp(1));
    }

    private void addEmptyMessage(boolean noNoticesSaved) {
        TextView emptyMessage = new TextView(this);

        if (noNoticesSaved) {
            emptyMessage.setText(
                    getString(R.string.no_registered_notices)
            );
        } else {
            emptyMessage.setText(
                    getString(
                            R.string.register_no_matching_notices
                    )
            );
        }

        emptyMessage.setTextColor(Color.parseColor("#5F6368"));
        emptyMessage.setTextSize(15);
        emptyMessage.setGravity(Gravity.CENTER);
        emptyMessage.setPadding(
                dp(20),
                dp(30),
                dp(20),
                dp(30)
        );

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(18));
        background.setStroke(dp(1), Color.parseColor("#D7E4F5"));

        emptyMessage.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        noticeContainer.addView(emptyMessage, params);
    }

    private void addNoticeCard(NoticeRecord noticeRecord) {
        MaterialCardView cardView = new MaterialCardView(this);

        cardView.setCardBackgroundColor(Color.WHITE);
        cardView.setRadius(dp(18));
        cardView.setCardElevation(dp(3));
        cardView.setStrokeWidth(dp(1));
        cardView.setStrokeColor(Color.parseColor("#D7E4F5"));
        cardView.setClickable(true);
        cardView.setFocusable(true);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(12));
        cardView.setLayoutParams(cardParams);

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView typeText = new TextView(this);

        typeText.setText(
                getNoticeTypeText(
                        noticeRecord.getNoticeType()
                )
        );

        typeText.setTextColor(Color.parseColor("#0D47A1"));
        typeText.setTextSize(13);
        typeText.setTypeface(null, 1);

        LinearLayout.LayoutParams typeParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        topRow.addView(typeText, typeParams);

        TextView statusText = new TextView(this);

        statusText.setText(
                getStatusText(noticeRecord.getStatus())
        );

        statusText.setTextColor(
                getStatusTextColor(noticeRecord.getStatus())
        );

        statusText.setTextSize(11);
        statusText.setTypeface(null, 1);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(dp(10), dp(5), dp(10), dp(5));

        GradientDrawable statusBackground = new GradientDrawable();

        statusBackground.setColor(
                getStatusBackgroundColor(
                        noticeRecord.getStatus()
                )
        );

        statusBackground.setCornerRadius(dp(20));
        statusText.setBackground(statusBackground);

        topRow.addView(statusText);

        TextView noticeNumberText = new TextView(this);

        noticeNumberText.setText(
                noticeRecord.getNoticeNumber()
        );

        noticeNumberText.setTextColor(Color.parseColor("#062E6E"));
        noticeNumberText.setTextSize(19);
        noticeNumberText.setTypeface(null, 1);

        LinearLayout.LayoutParams numberParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        numberParams.setMargins(0, dp(12), 0, 0);
        noticeNumberText.setLayoutParams(numberParams);

        TextView summaryText = new TextView(this);

        summaryText.setText(
                getNoticeSummary(noticeRecord)
        );

        summaryText.setTextColor(Color.parseColor("#4E5D6C"));
        summaryText.setTextSize(14);
        summaryText.setMaxLines(2);

        LinearLayout.LayoutParams summaryParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        summaryParams.setMargins(0, dp(6), 0, 0);
        summaryText.setLayoutParams(summaryParams);

        TextView dateText = new TextView(this);

        dateText.setText(getString(
                R.string.record_created,
                getFormattedDate(noticeRecord.getCreatedAt())
        ));

        dateText.setTextColor(Color.parseColor("#7B8794"));
        dateText.setTextSize(12);

        LinearLayout.LayoutParams dateParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        dateParams.setMargins(0, dp(10), 0, 0);
        dateText.setLayoutParams(dateParams);

        contentLayout.addView(topRow);
        contentLayout.addView(noticeNumberText);
        contentLayout.addView(summaryText);
        contentLayout.addView(dateText);

        cardView.addView(contentLayout);

        cardView.setOnClickListener(view ->
                showNoticeActionsDialog(noticeRecord)
        );

        noticeContainer.addView(cardView);
    }

    private void showNoticeActionsDialog(
            NoticeRecord noticeRecord
    ) {
        String[] actionOptions = {
                getViewDetailsText(),
                getString(R.string.record_status_draft),
                getString(R.string.record_status_issued),
                getString(R.string.record_status_completed),
                getPdfOptionText()
        };

        new AlertDialog.Builder(this)
                .setTitle(getNoticeActionTitle())
                .setItems(actionOptions, (dialog, which) -> {
                    if (which == 0) {
                        Intent detailIntent =
                                NoticeDetailActivity.createIntent(
                                        NoticeListActivity.this,
                                        noticeRecord
                                );

                        startActivity(detailIntent);
                        return;
                    }

                    if (which == 4) {
                        printRegisteredNotice(noticeRecord);
                        return;
                    }

                    String selectedNoticeStatus;

                    if (which == 1) {
                        selectedNoticeStatus = "DRAFT";
                    } else if (which == 2) {
                        selectedNoticeStatus = "ISSUED";
                    } else {
                        selectedNoticeStatus = "COMPLETED";
                    }

                    NoticeStore.updateNoticeStatus(
                            NoticeListActivity.this,
                            noticeRecord.getNoticeNumber(),
                            selectedNoticeStatus
                    );

                    Toast.makeText(
                            NoticeListActivity.this,
                            getString(
                                    R.string.notice_status_updated
                            ),
                            Toast.LENGTH_SHORT
                    ).show();

                    loadRegisteredNotices();
                })
                .show();
    }

    private void printRegisteredNotice(
            NoticeRecord noticeRecord
    ) {
        if (!noticeRecord.hasDocumentSnapshot()) {
            Toast.makeText(
                    this,
                    getPdfUnavailableMessage(),
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        boolean printStarted =
                RegisteredNoticePdfHelper.printRegisteredNotice(
                        this,
                        noticeRecord
                );

        if (!printStarted) {
            Toast.makeText(
                    this,
                    getPdfUnableMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private String getNoticeActionTitle() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "नोटिस विकल्प";
        }

        return "Notice Actions";
    }

    private String getViewDetailsText() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "नोटिस विवरण देखें";
        }

        return "VIEW NOTICE DETAILS";
    }

    private String getPdfOptionText() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "पीडीएफ देखें / सहेजें";
        }

        return "VIEW / SAVE PDF";
    }

    private String getPdfUnavailableMessage() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "इस पुराने नोटिस का PDF snapshot उपलब्ध नहीं है। Preview खोलकर इसे फिर से Register में Save करें।";
        }

        return "PDF snapshot is unavailable for this older notice. Open its preview and save it to the register again.";
    }

    private String getPdfUnableMessage() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "इस नोटिस का PDF नहीं बनाया जा सका।";
        }

        return "Unable to create PDF for this notice.";
    }

    private String getNoticeTypeText(String noticeType) {
        if (noticeType.equals("SECTION_94")) {
            return getString(R.string.record_type_section94);
        }

        if (noticeType.equals("SECTION_35")) {
            return getString(R.string.record_type_section35);
        }

        if (noticeType.equals("CDR")) {
            return getString(R.string.record_type_cdr);
        }

        if (noticeType.equals("COURT_RELEASE")) {
            return getString(R.string.cr_type_name);
        }

        return noticeType;
    }

    private String getNoticeSummary(NoticeRecord noticeRecord) {
        if (noticeRecord.getNoticeType().equals("SECTION_94")) {
            return getString(
                    R.string.register_summary_section94,
                    noticeRecord.getPrimaryValue(),
                    noticeRecord.getSecondaryValue()
            );
        }

        if (noticeRecord.getNoticeType().equals("SECTION_35")) {
            return getString(
                    R.string.register_summary_section35,
                    noticeRecord.getPrimaryValue(),
                    noticeRecord.getSecondaryValue()
            );
        }

        if (noticeRecord.getNoticeType().equals("CDR")) {
            return getString(
                    R.string.register_summary_cdr,
                    noticeRecord.getPrimaryValue(),
                    noticeRecord.getSecondaryValue()
            );
        }

        if (noticeRecord.getNoticeType().equals("COURT_RELEASE")) {
            return getString(
                    R.string.cr_register_summary,
                    noticeRecord.getPrimaryValue(),
                    noticeRecord.getSecondaryValue()
            );
        }

        return noticeRecord.getPrimaryValue();
    }

    private String getStatusText(String status) {
        if (status.equals("ISSUED")) {
            return getString(R.string.record_status_issued);
        }

        if (status.equals("COMPLETED")) {
            return getString(R.string.record_status_completed);
        }

        return getString(R.string.record_status_draft);
    }

    private int getStatusTextColor(String status) {
        if (status.equals("ISSUED")) {
            return Color.parseColor("#0D47A1");
        }

        if (status.equals("COMPLETED")) {
            return Color.parseColor("#2E7D32");
        }

        return Color.parseColor("#8A5500");
    }

    private int getStatusBackgroundColor(String status) {
        if (status.equals("ISSUED")) {
            return Color.parseColor("#E4EFFC");
        }

        if (status.equals("COMPLETED")) {
            return Color.parseColor("#E7F6EC");
        }

        return Color.parseColor("#FFF3CD");
    }

    private String getFormattedDate(long timestamp) {
        if (timestamp <= 0) {
            return getString(R.string.record_date_unavailable);
        }

        String selectedLanguage = LanguageManager.getLanguage(this);

        Locale dateLocale;

        if (selectedLanguage.equals("hi")) {
            dateLocale = new Locale("hi", "IN");
        } else {
            dateLocale = Locale.ENGLISH;
        }

        return new SimpleDateFormat(
                "dd MMMM yyyy, hh:mm a",
                dateLocale
        ).format(new Date(timestamp));
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;

        return (int) (value * density + 0.5f);
    }
}
