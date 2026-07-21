package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Section35PreviewActivity extends BaseActivity {

    private TextView tvNoticeDate;
    private TextView tvTo;
    private TextView tvSubject;
    private TextView tvBody;
    private TextView tvCaseReference;
    private TextView tvTransactionDetails;
    private TextView tvAppearanceDirection;
    private TextView tvSignature;

    private Button btnBack;
    private Button btnEditForm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section35_preview);

        bindViews();
        loadNoticePreview();
        addPrintButton();
        addShareButton(); // नया शेयर बटन

        btnBack.setOnClickListener(view -> finish());

        btnEditForm.setText(getString(R.string.save_to_register));

        btnEditForm.setOnClickListener(view -> {
            saveNoticeToRegister();

            Toast.makeText(
                    Section35PreviewActivity.this,
                    getString(R.string.notice_saved_to_register),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void bindViews() {
        tvNoticeDate = findViewById(R.id.tvNoticeDate);
        tvTo = findViewById(R.id.tvTo);
        tvSubject = findViewById(R.id.tvSubject);
        tvBody = findViewById(R.id.tvBody);
        tvCaseReference = findViewById(R.id.tvCaseReference);
        tvTransactionDetails = findViewById(
                R.id.tvTransactionDetails
        );

        tvAppearanceDirection = findViewById(
                R.id.tvAppearanceDirection
        );

        tvSignature = findViewById(R.id.tvSignature);

        btnBack = findViewById(R.id.btnBack);
        btnEditForm = findViewById(R.id.btnEditForm);
    }

    private void addPrintButton() {
        LinearLayout parentLayout = (LinearLayout) btnEditForm.getParent();

        if (parentLayout == null) {
            return;
        }

        MaterialButton btnPrint = new MaterialButton(this);

        btnPrint.setText(getString(R.string.print_save_pdf));
        btnPrint.setTextColor(Color.WHITE);
        btnPrint.setTextSize(14);
        btnPrint.setCornerRadius(dp(14));

        btnPrint.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor("#0D47A1")
                )
        );

        LinearLayout.LayoutParams printParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                );

        printParams.setMargins(0, dp(12), 0, dp(0)); // मार्जिन शेयर बटन के लिए एडजस्ट किया गया

        int buttonIndex = parentLayout.indexOfChild(btnEditForm);

        parentLayout.addView(
                btnPrint,
                buttonIndex + 1,
                printParams
        );

        btnPrint.setOnClickListener(view -> {
            Section35PdfHelper.printSection35(
                    Section35PreviewActivity.this,
                    getString(R.string.pdf_job_section35),
                    getText(tvNoticeDate),
                    getText(tvTo),
                    getText(tvSubject),
                    getText(tvBody),
                    getText(tvCaseReference),
                    getText(tvTransactionDetails),
                    getText(tvAppearanceDirection),
                    getText(tvSignature)
            );
        });
    }

    // नया शेयर बटन बनाने वाला फंक्शन
    private void addShareButton() {
        LinearLayout parentLayout = (LinearLayout) btnEditForm.getParent();

        if (parentLayout == null) {
            return;
        }

        MaterialButton btnShare = new MaterialButton(this);

        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
        btnShare.setText(isHindi ? "पीडीएफ शेयर करें (WhatsApp/Email)" : "SHARE PDF (WhatsApp/Email)");
        btnShare.setTextColor(Color.WHITE);
        btnShare.setTextSize(14);
        btnShare.setCornerRadius(dp(14));

        btnShare.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor("#2E7D32")
                )
        );

        LinearLayout.LayoutParams shareParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                );

        shareParams.setMargins(0, dp(12), 0, dp(18));

        int buttonIndex = parentLayout.indexOfChild(btnEditForm);
        parentLayout.addView(
                btnShare,
                buttonIndex + 2,
                shareParams
        );

        btnShare.setOnClickListener(view -> sharePdfDirectly());
    }

    // नया शेयर फंक्शन (साइलेंट PDF बनाकर शेयर करना)
    private void sharePdfDirectly() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_35_draft"
        );
        String noticeNumber = getOrCreateNoticeNumber(preferences);

        // सुरक्षित फाइल का नाम बनाना (जैसे: Sec_35_BNSS_123_2026.pdf)
        String safeNoticeNumber = noticeNumber.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String finalFileName = "Sec_35_BNSS_" + safeNoticeNumber + ".pdf";

        File cacheDir = getExternalCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File pdfFile = new File(cacheDir, finalFileName);

        // 2. बैकग्राउंड में PDF बनाना
        boolean isGenerated = Section35PdfHelper.generateSilentPdf(
                this,
                pdfFile,
                getText(tvNoticeDate),
                getText(tvTo),
                getText(tvSubject),
                getText(tvBody),
                getText(tvCaseReference),
                getText(tvTransactionDetails),
                getText(tvAppearanceDirection),
                getText(tvSignature)
        );

        // 3. शेयर मेनू खोलना
        if (isGenerated && pdfFile.exists()) {
            ShareManager.sharePdf(this, pdfFile, noticeNumber);
        } else {
            boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
            Toast.makeText(
                    this,
                    isHindi ? "शेयर करने के लिए पीडीएफ नहीं बन सका।" : "Unable to generate PDF for sharing.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void loadNoticePreview() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_35_draft"
        );

        String noticeNumber = getOrCreateNoticeNumber(preferences);

        String crimeNumber = getValue(preferences, "crime_number");
        String crimeDate = getValue(preferences, "crime_date");
        String sections = getValue(preferences, "sections");

        String recipientName = getValue(
                preferences,
                "recipient_name"
        );

        String fatherSpouseName = getValue(
                preferences,
                "father_spouse_name"
        );

        String recipientAddress = getValue(
                preferences,
                "recipient_address"
        );

        String recipientMobile = getValue(
                preferences,
                "recipient_mobile"
        );

        String transactionAmount = getValue(
                preferences,
                "transaction_amount"
        );

        String transactionDate = getValue(
                preferences,
                "transaction_date"
        );

        String bankName = getValue(preferences, "bank_name");
        String branchName = getValue(preferences, "branch_name");

        String branchAddress = getValue(
                preferences,
                "branch_address"
        );

        String accountNumber = getValue(
                preferences,
                "account_number"
        );

        String ifscCode = getValue(preferences, "ifsc_code");
        String utrNumber = getValue(preferences, "utr_number");

        String withdrawalDate = getValue(
                preferences,
                "withdrawal_date"
        );

        String appearanceDate = getValue(
                preferences,
                "appearance_date"
        );

        String appearanceTime = getValue(
                preferences,
                "appearance_time"
        );

        String appearancePlace = getValue(
                preferences,
                "appearance_place"
        );

        String officerName = getValue(
                preferences,
                "officer_name"
        );

        String rank = getValue(preferences, "rank");
        String mobile = getValue(preferences, "mobile");
        String email = getValue(preferences, "email");

        if (email.isEmpty()) {
            email = getString(
                    R.string.official_email_default
            );
        }

        tvNoticeDate.setText(getString(
                R.string.notice_reference_with_date,
                noticeNumber,
                getCurrentDate()
        ));

        tvTo.setText(getString(
                R.string.section35_preview_to,
                recipientName,
                fatherSpouseName,
                recipientAddress,
                recipientMobile
        ));

        tvSubject.setText(getString(
                R.string.section35_subject,
                crimeNumber,
                crimeDate,
                sections
        ));

        tvBody.setText(getString(R.string.section35_body));

        tvCaseReference.setText(getString(
                R.string.section35_case_reference,
                crimeNumber,
                crimeDate,
                sections
        ));

        String transactionDetails = getString(
                R.string.section35_transaction_summary,
                transactionAmount,
                transactionDate,
                bankName,
                accountNumber,
                ifscCode,
                utrNumber,
                withdrawalDate
        );

        if (!branchName.isEmpty()) {
            transactionDetails += "\n\n"
                    + getBranchNameLabel()
                    + ": "
                    + branchName;
        }

        if (!branchAddress.isEmpty()) {
            transactionDetails += "\n"
                    + getBranchAddressLabel()
                    + ": "
                    + branchAddress;
        }

        tvTransactionDetails.setText(transactionDetails);

        tvAppearanceDirection.setText(getString(
                R.string.appearance_direction,
                appearanceDate,
                appearanceTime,
                appearancePlace
        ));

        tvSignature.setText(getString(
                R.string.signature_block,
                officerName,
                rank,
                mobile,
                email
        ));
    }

    private String getBranchNameLabel() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "शाखा";
        }

        return "Branch";
    }

    private String getBranchAddressLabel() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "शाखा पता";
        }

        return "Branch Address";
    }

    private void saveNoticeToRegister() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_35_draft"
        );

        String noticeNumber = getOrCreateNoticeNumber(preferences);

        String primaryValue = getString(
                R.string.fir_reference,
                getValue(preferences, "crime_number")
        );

        String secondaryValue = getValue(
                preferences,
                "recipient_name"
        );

        NoticeRecord noticeRecord = new NoticeRecord(
                noticeNumber,
                "SECTION_35",
                primaryValue,
                secondaryValue,
                "DRAFT",
                System.currentTimeMillis(),
                createDocumentSnapshot()
        );

        NoticeStore.saveNotice(this, noticeRecord);
    }

    private String createDocumentSnapshot() {
        JSONObject snapshot = new JSONObject();

        try {
            snapshot.put("notice_date", getText(tvNoticeDate));
            snapshot.put("to", getText(tvTo));
            snapshot.put("subject", getText(tvSubject));
            snapshot.put("body", getText(tvBody));

            snapshot.put(
                    "case_reference",
                    getText(tvCaseReference)
            );

            snapshot.put(
                    "transaction_details",
                    getText(tvTransactionDetails)
            );

            snapshot.put(
                    "appearance_direction",
                    getText(tvAppearanceDirection)
            );

            snapshot.put(
                    "signature",
                    getText(tvSignature)
            );

            snapshot.put(
                    "form_data",
                    createFormDataSnapshot()
            );

            return snapshot.toString();

        } catch (Exception ignored) {
            return "";
        }
    }

    private JSONObject createFormDataSnapshot() {
        JSONObject formData = new JSONObject();

        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_35_draft"
        );

        try {
            for (Map.Entry<String, ?> entry
                    : preferences.getAll().entrySet()) {

                Object value = entry.getValue();

                if (value instanceof String
                        || value instanceof Boolean
                        || value instanceof Integer
                        || value instanceof Long
                        || value instanceof Float) {

                    formData.put(entry.getKey(), value);
                }
            }
        } catch (Exception ignored) {
        }

        return formData;
    }

    private String getOrCreateNoticeNumber(
            SharedPreferences preferences
    ) {
        String noticeNumber = preferences.getString(
                "notice_number",
                ""
        );

        if (noticeNumber.isEmpty()) {
            noticeNumber = NoticeNumberManager.getNextNoticeNumber(this);

            preferences.edit()
                    .putString("notice_number", noticeNumber)
                    .apply();
        }

        return noticeNumber;
    }

    private String getCurrentDate() {
        String selectedLanguage = LanguageManager.getLanguage(this);

        Locale dateLocale;

        if (selectedLanguage.equals("hi")) {
            dateLocale = new Locale("hi", "IN");
        } else {
            dateLocale = Locale.ENGLISH;
        }

        return new SimpleDateFormat(
                "dd MMMM yyyy",
                dateLocale
        ).format(new Date());
    }

    private String getValue(
            SharedPreferences preferences,
            String key
    ) {
        return preferences.getString(key, "");
    }

    private String getText(TextView textView) {
        if (textView == null || textView.getText() == null) {
            return "";
        }

        return textView.getText().toString().trim();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }
}
