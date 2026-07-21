package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoticeDetailActivity extends BaseActivity {

    private static final String EXTRA_NOTICE_NUMBER =
            "extra_notice_number";

    private static final String EXTRA_NOTICE_TYPE =
            "extra_notice_type";

    private static final String EXTRA_PRIMARY_VALUE =
            "extra_primary_value";

    private static final String EXTRA_SECONDARY_VALUE =
            "extra_secondary_value";

    private static final String EXTRA_STATUS =
            "extra_status";

    private static final String EXTRA_CREATED_AT =
            "extra_created_at";

    private static final String EXTRA_DOCUMENT_SNAPSHOT =
            "extra_document_snapshot";

    private TextView tvTitle;
    private TextView tvType;
    private TextView tvStatus;
    private TextView tvNoticeNumber;
    private TextView tvSummary;
    private TextView tvCreatedOn;
    private TextView tvDocumentTitle;
    private TextView tvDocumentContent;

    private Button btnBack;
    private Button btnDuplicateNotice;
    private Button btnDeleteDraft;
    private Button btnPrintPdf;
    private Button btnSharePdf;
    private Button btnBackBottom;

    private NoticeRecord noticeRecord;

    public static Intent createIntent(
            Context context,
            NoticeRecord record
    ) {
        Intent intent = new Intent(
                context,
                NoticeDetailActivity.class
        );

        intent.putExtra(
                EXTRA_NOTICE_NUMBER,
                record.getNoticeNumber()
        );

        intent.putExtra(
                EXTRA_NOTICE_TYPE,
                record.getNoticeType()
        );

        intent.putExtra(
                EXTRA_PRIMARY_VALUE,
                record.getPrimaryValue()
        );

        intent.putExtra(
                EXTRA_SECONDARY_VALUE,
                record.getSecondaryValue()
        );

        intent.putExtra(
                EXTRA_STATUS,
                record.getStatus()
        );

        intent.putExtra(
                EXTRA_CREATED_AT,
                record.getCreatedAt()
        );

        intent.putExtra(
                EXTRA_DOCUMENT_SNAPSHOT,
                record.getDocumentSnapshot()
        );

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
        setContentView(R.layout.activity_notice_detail);

        bindViews();
        readNoticeData();
        setupScreen();
        setupListeners();
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvType = findViewById(R.id.tvType);
        tvStatus = findViewById(R.id.tvStatus);
        tvNoticeNumber = findViewById(R.id.tvNoticeNumber);
        tvSummary = findViewById(R.id.tvSummary);
        tvCreatedOn = findViewById(R.id.tvCreatedOn);
        tvDocumentTitle = findViewById(R.id.tvDocumentTitle);
        tvDocumentContent = findViewById(
                R.id.tvDocumentContent
        );

        btnBack = findViewById(R.id.btnBack);

        btnDuplicateNotice = findViewById(
                R.id.btnDuplicateNotice
        );

        btnDeleteDraft = findViewById(
                R.id.btnDeleteDraft
        );

        btnPrintPdf = findViewById(R.id.btnPrintPdf);
        btnSharePdf = findViewById(R.id.btnSharePdf);
        btnBackBottom = findViewById(R.id.btnBackBottom);
    }

    private void readNoticeData() {
        Intent intent = getIntent();

        noticeRecord = new NoticeRecord(
                intent.getStringExtra(EXTRA_NOTICE_NUMBER),
                intent.getStringExtra(EXTRA_NOTICE_TYPE),
                intent.getStringExtra(EXTRA_PRIMARY_VALUE),
                intent.getStringExtra(EXTRA_SECONDARY_VALUE),
                intent.getStringExtra(EXTRA_STATUS),
                intent.getLongExtra(EXTRA_CREATED_AT, 0),
                intent.getStringExtra(EXTRA_DOCUMENT_SNAPSHOT)
        );

        if (isEmpty(noticeRecord.getNoticeNumber())) {
            Toast.makeText(
                    this,
                    isHindi()
                            ? "नोटिस विवरण उपलब्ध नहीं है।"
                            : "Notice details are unavailable.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    private void setupScreen() {
        tvTitle.setText(
                isHindi() ? "नोटिस विवरण" : "Notice Details"
        );

        tvType.setText(getNoticeTypeText());
        tvStatus.setText(getStatusText());
        tvStatus.setTextColor(getStatusTextColor());

        tvNoticeNumber.setText(
                safe(noticeRecord.getNoticeNumber())
        );

        tvSummary.setText(getSummaryText());

        tvCreatedOn.setText(
                isHindi()
                        ? "रजिस्टर में सहेजा गया: "
                          + getFormattedDate()
                        : "Saved to register: "
                          + getFormattedDate()
        );

        tvDocumentTitle.setText(
                isHindi()
                        ? "सहेजा हुआ नोटिस"
                        : "Saved Notice"
        );

        tvDocumentContent.setText(buildDocumentContent());

        btnDuplicateNotice.setText(
                isHindi()
                        ? "नए ड्राफ्ट के रूप में कॉपी करें"
                        : "DUPLICATE AS NEW DRAFT"
        );

        btnPrintPdf.setText(
                isHindi()
                        ? "पीडीएफ देखें / सहेजें"
                        : "VIEW / SAVE PDF"
        );

        btnSharePdf.setText(
                isHindi()
                        ? "पीडीएफ शेयर करें (WhatsApp/Email)"
                        : "SHARE PDF (WhatsApp/Email)"
        );

        btnBackBottom.setText(
                isHindi()
                        ? "रजिस्टर पर वापस जाएँ"
                        : "BACK TO REGISTER"
        );

        if (isDraftNotice()) {
            btnDeleteDraft.setVisibility(View.VISIBLE);

            btnDeleteDraft.setText(
                    isHindi()
                            ? "ड्राफ्ट हटाएँ"
                            : "REMOVE DRAFT"
            );
        } else {
            btnDeleteDraft.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(view -> finish());
        btnBackBottom.setOnClickListener(view -> finish());

        btnDuplicateNotice.setOnClickListener(
                view -> duplicateNotice()
        );

        btnDeleteDraft.setOnClickListener(
                view -> confirmDeleteDraft()
        );

        btnPrintPdf.setOnClickListener(view -> openPdf());

        // डायरेक्ट शेयर बटन का क्लिक लिसनर
        btnSharePdf.setOnClickListener(view -> sharePdf());
    }

    private void duplicateNotice() {
        Intent duplicateIntent =
                NoticeDuplicateHelper.createDuplicateIntent(
                        this,
                        noticeRecord
                );

        if (duplicateIntent == null) {
            Toast.makeText(
                    this,
                    getDuplicateUnavailableMessage(),
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        isHindi()
                                ? "नोटिस कॉपी करें"
                                : "Duplicate Notice"
                )
                .setMessage(
                        isHindi()
                                ? "पुराने नोटिस का data नए ड्राफ्ट में भर दिया जाएगा। नया नोटिस नंबर Preview खोलने पर अपने-आप बनेगा।"
                                : "The saved data will be copied into a new draft. A new notice number will be created when Preview is opened."
                )
                .setNegativeButton(
                        isHindi() ? "रद्द करें" : "CANCEL",
                        null
                )
                .setPositiveButton(
                        isHindi()
                                ? "कॉपी करें"
                                : "DUPLICATE",
                        (dialog, which) -> {
                            startActivity(duplicateIntent);

                            Toast.makeText(
                                    NoticeDetailActivity.this,
                                    isHindi()
                                            ? "नया ड्राफ्ट तैयार है। आवश्यक बदलाव करके Preview खोलें।"
                                            : "New draft is ready. Make required changes and open Preview.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .show();
    }

    private void confirmDeleteDraft() {
        if (!isDraftNotice()) {
            Toast.makeText(
                    this,
                    isHindi()
                            ? "केवल ड्राफ्ट नोटिस हटाया जा सकता है।"
                            : "Only a draft notice can be removed.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        isHindi()
                                ? "ड्राफ्ट हटाएँ?"
                                : "Remove Draft?"
                )
                .setMessage(
                        isHindi()
                                ? "यह ड्राफ्ट Notice Register से हट जाएगा। जारी या पूर्ण नोटिस कभी नहीं हटाए जा सकते।"
                                : "This draft will be removed from the Notice Register. Issued or completed notices cannot be removed."
                )
                .setNegativeButton(
                        isHindi() ? "रद्द करें" : "CANCEL",
                        null
                )
                .setPositiveButton(
                        isHindi()
                                ? "ड्राफ्ट हटाएँ"
                                : "REMOVE DRAFT",
                        (dialog, which) -> removeDraft()
                )
                .show();
    }

    private void removeDraft() {
        boolean deleted = NoticeStore.deleteDraftNotice(
                this,
                noticeRecord.getNoticeNumber()
        );

        if (deleted) {
            Toast.makeText(
                    this,
                    isHindi()
                            ? "ड्राफ्ट हटा दिया गया है।"
                            : "Draft has been removed.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        } else {
            Toast.makeText(
                    this,
                    isHindi()
                            ? "ड्राफ्ट हटाया नहीं जा सका।"
                            : "Unable to remove this draft.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openPdf() {
        if (!noticeRecord.hasDocumentSnapshot()) {
            Toast.makeText(
                    this,
                    isHindi()
                            ? "इस पुराने नोटिस का PDF snapshot उपलब्ध नहीं है।"
                            : "PDF snapshot is unavailable for this older notice.",
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
                    isHindi()
                            ? "इस नोटिस का PDF नहीं बनाया जा सका।"
                            : "Unable to create PDF for this notice.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // नया बैकग्राउंड (Silent) शेयर फंक्शन फ़ाइल नाम के लॉजिक के साथ
    private void sharePdf() {
        if (!noticeRecord.hasDocumentSnapshot()) {
            Toast.makeText(
                    this,
                    isHindi()
                            ? "इस नोटिस का डेटा उपलब्ध नहीं है।"
                            : "Data is unavailable for this notice.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // Cache Directory का उपयोग करें
        File cacheDir = getExternalCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        // नोटिस के प्रकार के अनुसार फाइल का नाम तय करें
        String noticeType = safe(noticeRecord.getNoticeType());
        String filePrefix = "Notice_";

        if ("SECTION_94".equals(noticeType)) {
            filePrefix = "Sec_94_BNSS_";
        } else if ("SECTION_35".equals(noticeType)) {
            filePrefix = "Sec_35_BNSS_";
        } else if ("CDR".equals(noticeType)) {
            filePrefix = "CDR_Proforma_";
        } else if ("COURT_RELEASE".equals(noticeType)) {
            filePrefix = "Court_Release_";
        }

        // नोटिस नंबर से '/', '\', ':', आदि विशेष चिन्ह हटाकर सुरक्षित फाइल का नाम बनाना
        String safeNoticeNumber = noticeRecord.getNoticeNumber() != null
                ? noticeRecord.getNoticeNumber().replaceAll("[^a-zA-Z0-9_\\-]", "_")
                : "Unknown";

        // फाइनल फाइल का नाम (उदा: Sec_94_BNSS_123_2026.pdf)
        String finalFileName = filePrefix + safeNoticeNumber + ".pdf";
        File pdfFile = new File(cacheDir, finalFileName);

        // 1. बैकग्राउंड में चुपचाप PDF बनाना
        boolean isGenerated = RegisteredNoticePdfHelper.generateSilentPdfForShare(this, noticeRecord, pdfFile);

        if (isGenerated && pdfFile.exists()) {
            // 2. तुरंत WhatsApp/Email शेयरिंग खोलना
            ShareManager.sharePdf(this, pdfFile, noticeRecord.getNoticeNumber());
        } else {
            Toast.makeText(
                    this,
                    isHindi()
                            ? "शेयर करने के लिए पीडीएफ नहीं बन सका। कृपया 'पीडीएफ देखें' का उपयोग करें।"
                            : "Unable to generate PDF for sharing. Please use 'VIEW PDF'.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private String getDuplicateUnavailableMessage() {
        if (isHindi()) {
            return "इस पुराने नोटिस का form-data उपलब्ध नहीं है। नया notice बनाकर Preview से Register में Save करने के बाद ही Duplicate सुविधा काम करेगी।";
        }

        return "Form data is unavailable for this older notice. Duplicate works for notices saved to the register after this update.";
    }

    private String getNoticeTypeText() {
        String noticeType = safe(noticeRecord.getNoticeType());

        if ("SECTION_94".equals(noticeType)) {
            return isHindi()
                    ? "धारा 94 बीएनएसएस, 2023 के अंतर्गत नोटिस"
                    : "NOTICE U/S 94 BNSS, 2023";
        }

        if ("SECTION_35".equals(noticeType)) {
            return isHindi()
                    ? "धारा 35(3) बीएनएसएस, 2023 के अंतर्गत नोटिस"
                    : "NOTICE U/S 35(3) BNSS, 2023";
        }

        if ("CDR".equals(noticeType)) {
            return isHindi()
                    ? "सीडीआर प्रोफार्मा"
                    : "CDR PROFORMA";
        }

        if ("COURT_RELEASE".equals(noticeType)) {
            return isHindi()
                    ? "न्यायालय रिलीज आदेश"
                    : "COURT RELEASE ORDER";
        }

        return noticeType;
    }

    private String getStatusText() {
        String status = safe(noticeRecord.getStatus());

        if ("ISSUED".equals(status)) {
            return isHindi() ? "जारी किया गया" : "ISSUED";
        }

        if ("COMPLETED".equals(status)) {
            return isHindi() ? "पूर्ण" : "COMPLETED";
        }

        return isHindi() ? "ड्राफ्ट" : "DRAFT";
    }

    private int getStatusTextColor() {
        String status = safe(noticeRecord.getStatus());

        if ("ISSUED".equals(status)) {
            return Color.parseColor("#0D47A1");
        }

        if ("COMPLETED".equals(status)) {
            return Color.parseColor("#2E7D32");
        }

        return Color.parseColor("#8A5500");
    }

    private String getSummaryText() {
        String primaryValue = safe(
                noticeRecord.getPrimaryValue()
        );

        String secondaryValue = safe(
                noticeRecord.getSecondaryValue()
        );

        String noticeType = safe(
                noticeRecord.getNoticeType()
        );

        if ("SECTION_94".equals(noticeType)) {
            return isHindi()
                    ? "एफआईआर: " + primaryValue
                      + "\nबैंक: " + secondaryValue
                    : "FIR: " + primaryValue
                      + "\nBank: " + secondaryValue;
        }

        if ("SECTION_35".equals(noticeType)) {
            return isHindi()
                    ? "एफआईआर: " + primaryValue
                      + "\nखाता / लेनदेन: " + secondaryValue
                    : "FIR: " + primaryValue
                      + "\nAccount / Transaction: " + secondaryValue;
        }

        if ("CDR".equals(noticeType)) {
            return isHindi()
                    ? "प्रकरण: " + primaryValue
                      + "\nमोबाइल / पहचानकर्ता: " + secondaryValue
                    : "Case: " + primaryValue
                      + "\nMobile / Identifier: " + secondaryValue;
        }

        if ("COURT_RELEASE".equals(noticeType)) {
            return isHindi()
                    ? primaryValue + "\n" + secondaryValue
                    : primaryValue + "\n" + secondaryValue;
        }

        return primaryValue + "\n" + secondaryValue;
    }

    private String buildDocumentContent() {
        if (!noticeRecord.hasDocumentSnapshot()) {
            return isHindi()
                    ? "इस पुराने रिकॉर्ड के लिए विस्तृत नोटिस snapshot उपलब्ध नहीं है।"
                    : "A detailed notice snapshot is unavailable for this older record.";
        }

        try {
            JSONObject snapshot = new JSONObject(
                    noticeRecord.getDocumentSnapshot()
            );

            StringBuilder builder = new StringBuilder();

            addField(
                    builder,
                    isHindi() ? "दिनांक" : "Date",
                    snapshot.optString("notice_date", "")
            );

            addField(
                    builder,
                    isHindi() ? "प्रति" : "To",
                    snapshot.optString("to", "")
            );

            addField(
                    builder,
                    isHindi() ? "विषय" : "Subject",
                    snapshot.optString("subject", "")
            );

            addField(
                    builder,
                    isHindi() ? "नोटिस विवरण" : "Notice Details",
                    snapshot.optString("body", "")
            );

            String noticeType = safe(
                    noticeRecord.getNoticeType()
            );

            if ("SECTION_94".equals(noticeType)) {
                addField(
                        builder,
                        isHindi()
                                ? "मांगी गई सूचना / दस्तावेज"
                                : "Information / Documents Requested",
                        snapshot.optString(
                                "requirements",
                                ""
                        )
                );

                addField(
                        builder,
                        isHindi() ? "निर्देश" : "Instruction",
                        snapshot.optString(
                                "instruction",
                                ""
                        )
                );
            }

            if ("SECTION_35".equals(noticeType)) {
                addField(
                        builder,
                        isHindi()
                                ? "प्रकरण संदर्भ"
                                : "Case Reference",
                        snapshot.optString(
                                "case_reference",
                                ""
                        )
                );

                addField(
                        builder,
                        isHindi()
                                ? "लेनदेन विवरण"
                                : "Transaction Details",
                        snapshot.optString(
                                "transaction_details",
                                ""
                        )
                );

                addField(
                        builder,
                        isHindi()
                                ? "उपस्थिति निर्देश"
                                : "Appearance Direction",
                        snapshot.optString(
                                "appearance_direction",
                                ""
                        )
                );
            }

            if ("CDR".equals(noticeType)) {
                addField(
                        builder,
                        isHindi()
                                ? "प्रकरण विवरण"
                                : "Case Summary",
                        snapshot.optString(
                                "case_summary",
                                ""
                        )
                );

                addField(
                        builder,
                        isHindi()
                                ? "मोबाइल / पहचानकर्ता विवरण"
                                : "Identifier Details",
                        snapshot.optString(
                                "identifier_summary",
                                ""
                        )
                );

                addField(
                        builder,
                        isHindi()
                                ? "मांगी गई सूचना"
                                : "Information Requested",
                        snapshot.optString(
                                "requested_information",
                                ""
                        )
                );

                addField(
                        builder,
                        isHindi()
                                ? "अवधि"
                                : "Period",
                        snapshot.optString("period", "")
                );

                addField(
                        builder,
                        isHindi()
                                ? "औचित्य"
                                : "Justification",
                        snapshot.optString(
                                "justification",
                                ""
                        )
                );
            }

            addField(
                    builder,
                    isHindi() ? "हस्ताक्षरकर्ता" : "Signatory",
                    snapshot.optString("signature", "")
            );

            if ("COURT_RELEASE".equals(noticeType)) {
                addField(
                        builder,
                        isHindi() ? "न्यायालय रिलीज विवरण" : "Court Release Details",
                        snapshot.optString("court_release_summary", "")
                );

                addField(
                        builder,
                        isHindi() ? "लेन-देन विवरण" : "Transaction Details",
                        snapshot.optString("transaction_details", "")
                );

                addField(
                        builder,
                        isHindi() ? "रिलीज निर्देश" : "Release Instruction",
                        snapshot.optString("instruction", "")
                );
            }

            if (builder.length() == 0) {
                return isHindi()
                        ? "कोई विस्तृत विवरण उपलब्ध नहीं है।"
                        : "No detailed information is available.";
            }

            return builder.toString();

        } catch (Exception ignored) {
            return isHindi()
                    ? "सहेजे हुए नोटिस का विवरण पढ़ा नहीं जा सका।"
                    : "The saved notice details could not be read.";
        }
    }

    private void addField(
            StringBuilder builder,
            String label,
            String value
    ) {
        if (isEmpty(value)) {
            return;
        }

        if (builder.length() > 0) {
            builder.append("\n\n");
        }

        builder.append(label)
                .append(":\n")
                .append(value.trim());
    }

    private String getFormattedDate() {
        long createdAt = noticeRecord.getCreatedAt();

        if (createdAt <= 0) {
            return isHindi() ? "उपलब्ध नहीं" : "Not available";
        }

        Locale locale = isHindi()
                ? new Locale("hi", "IN")
                : Locale.ENGLISH;

        return new SimpleDateFormat(
                "dd MMMM yyyy, hh:mm a",
                locale
        ).format(new Date(createdAt));
    }

    private boolean isDraftNotice() {
        return "DRAFT".equals(
                safe(noticeRecord.getStatus())
        );
    }

    private boolean isHindi() {
        return "hi".equals(
                LanguageManager.getLanguage(this)
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
