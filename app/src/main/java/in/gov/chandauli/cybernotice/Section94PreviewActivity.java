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

public class Section94PreviewActivity extends BaseActivity {

    private TextView tvNoticeDate;
    private TextView tvTo;
    private TextView tvSubject;
    private TextView tvBody;
    private TextView tvRequirements;
    private TextView tvInstruction;
    private TextView tvSignature;

    private Button btnBack;
    private Button btnEditForm; // यह अब "ड्राफ्ट सुरक्षित करें" (Save to Register) बटन है

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section94_preview);

        bindViews();
        loadNoticePreview();
        addPrintButton();
        addShareButton(); // नया शेयर बटन यहाँ जोड़ा गया

        btnBack.setOnClickListener(view -> finish());

        btnEditForm.setText(getString(R.string.save_to_register));

        btnEditForm.setOnClickListener(view -> {
            saveNoticeToRegister();

            Toast.makeText(
                    Section94PreviewActivity.this,
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
        tvRequirements = findViewById(R.id.tvRequirements);
        tvInstruction = findViewById(R.id.tvInstruction);
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

        // शेयर बटन के लिए जगह छोड़ने के लिए मार्जिन सेट किया गया है
        printParams.setMargins(0, dp(12), 0, dp(0));

        int buttonIndex = parentLayout.indexOfChild(btnEditForm);

        parentLayout.addView(
                btnPrint,
                buttonIndex + 1,
                printParams
        );

        btnPrint.setOnClickListener(view -> {
            Section94PdfHelper.printSection94(
                    Section94PreviewActivity.this,
                    getString(R.string.pdf_job_section94),
                    getText(tvNoticeDate),
                    getText(tvTo),
                    getText(tvSubject),
                    getText(tvBody),
                    getText(tvRequirements),
                    getText(tvInstruction),
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

        // भाषा के अनुसार टेक्स्ट
        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
        btnShare.setText(isHindi ? "पीडीएफ शेयर करें (WhatsApp/Email)" : "SHARE PDF (WhatsApp/Email)");
        btnShare.setTextColor(Color.WHITE);
        btnShare.setTextSize(14);
        btnShare.setCornerRadius(dp(14));

        btnShare.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor("#2E7D32") // हरा रंग
                )
        );

        LinearLayout.LayoutParams shareParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                );

        shareParams.setMargins(0, dp(12), 0, dp(18)); // नीचे का मार्जिन यहाँ दिया है

        // शेयर बटन को Print बटन के ठीक नीचे जोड़ना
        int buttonIndex = parentLayout.indexOfChild(btnEditForm);
        parentLayout.addView(
                btnShare,
                buttonIndex + 2,
                shareParams
        );

        // शेयर बटन क्लिक करने पर कार्रवाई
        btnShare.setOnClickListener(view -> sharePdfDirectly());
    }

    // नया शेयर फंक्शन (साइलेंट PDF बनाकर शेयर करना)
    private void sharePdfDirectly() {
        // नोटिस नंबर लाना
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_94_draft"
        );
        String noticeNumber = getOrCreateNoticeNumber(preferences);

        // 1. सुरक्षित फाइल का नाम बनाना (जैसे: Sec_94_BNSS_123_2026.pdf)
        String safeNoticeNumber = noticeNumber.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String finalFileName = "Sec_94_BNSS_" + safeNoticeNumber + ".pdf";

        File cacheDir = getExternalCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File pdfFile = new File(cacheDir, finalFileName);

        // 2. बैकग्राउंड में PDF बनाना
        boolean isGenerated = Section94PdfHelper.generateSilentPdf(
                this,
                pdfFile,
                getText(tvNoticeDate),
                getText(tvTo),
                getText(tvSubject),
                getText(tvBody),
                getText(tvRequirements),
                getText(tvInstruction),
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
                "section_94_draft"
        );

        String noticeNumber = getOrCreateNoticeNumber(preferences);

        boolean isFirSelected = preferences.getBoolean(
                "is_fir_selected",
                true
        );

        String firNumber = getValue(preferences, "fir_number");
        String firDate = getValue(preferences, "fir_date");
        String ncrpNumber = getValue(preferences, "ncrp_number");
        String sections = getValue(preferences, "sections");

        String managerName = getValue(preferences, "manager_name");
        String bankName = getValue(preferences, "bank_name");
        String branchName = getValue(preferences, "branch_name");
        String branchAddress = getValue(
                preferences,
                "branch_address"
        );

        String accountNumbers = getValue(
                preferences,
                "account_numbers"
        );

        String statementFrom = getValue(
                preferences,
                "statement_from"
        );

        String statementTo = getValue(
                preferences,
                "statement_to"
        );

        String officerName = getValue(
                preferences,
                "officer_name"
        );

        String rank = getValue(preferences, "rank");
        String mobile = getValue(preferences, "mobile");
        String email = getValue(preferences, "email");

        if (managerName.isEmpty()) {
            managerName = getString(
                    R.string.branch_manager_default
            );
        }

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
                R.string.preview_to,
                managerName,
                bankName,
                branchName,
                branchAddress
        ));

        if (isFirSelected) {
            tvSubject.setText(getString(
                    R.string.subject_fir,
                    firNumber,
                    firDate,
                    sections
            ));

            tvBody.setText(getString(R.string.body_fir));
        } else {
            tvSubject.setText(getString(
                    R.string.subject_ncrp,
                    ncrpNumber,
                    sections
            ));

            tvBody.setText(getString(R.string.body_ncrp));
        }

        String requirements = getString(
                R.string.request_kyc,
                accountNumbers
        ) + "\n\n" + getString(
                R.string.request_statement,
                statementFrom,
                statementTo
        ) + "\n\n" + getString(
                R.string.request_transactions
        );

        tvRequirements.setText(requirements);

        tvInstruction.setText(
                getString(R.string.response_instruction)
        );

        tvSignature.setText(getString(
                R.string.signature_block,
                officerName,
                rank,
                mobile,
                email
        ));
    }

    private void saveNoticeToRegister() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_94_draft"
        );

        String noticeNumber = getOrCreateNoticeNumber(preferences);

        boolean isFirSelected = preferences.getBoolean(
                "is_fir_selected",
                true
        );

        String primaryValue;

        if (isFirSelected) {
            primaryValue = getString(
                    R.string.fir_reference,
                    getValue(preferences, "fir_number")
            );
        } else {
            primaryValue = getString(
                    R.string.ncrp_reference,
                    getValue(preferences, "ncrp_number")
            );
        }

        String bankName = getValue(preferences, "bank_name");

        NoticeRecord noticeRecord = new NoticeRecord(
                noticeNumber,
                "SECTION_94",
                primaryValue,
                bankName,
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
                    "requirements",
                    getText(tvRequirements)
            );

            snapshot.put(
                    "instruction",
                    getText(tvInstruction)
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
                "section_94_draft"
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
