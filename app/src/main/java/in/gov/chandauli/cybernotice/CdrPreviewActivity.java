package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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

public class CdrPreviewActivity extends BaseActivity {

    private TextView tvNoticeDate;
    private TextView tvTo;
    private TextView tvSubject;
    private TextView tvBody;
    private TextView tvOfficeSummary;
    private TextView tvCaseSummary;
    private TextView tvIdentifierSummary;
    private TextView tvRequestedInformation;
    private TextView tvPeriod;
    private TextView tvJustification;
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
        setContentView(R.layout.activity_cdr_preview);

        bindViews();
        loadPreview();
        addPrintButton();
        addShareButton(); // नया शेयर बटन यहाँ जोड़ा गया

        btnBack.setOnClickListener(view -> finish());

        btnEditForm.setText(getString(R.string.save_to_register));

        btnEditForm.setOnClickListener(view -> {
            saveNoticeToRegister();

            Toast.makeText(
                    CdrPreviewActivity.this,
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
        tvOfficeSummary = findViewById(R.id.tvOfficeSummary);
        tvCaseSummary = findViewById(R.id.tvCaseSummary);
        tvIdentifierSummary = findViewById(R.id.tvIdentifierSummary);
        tvRequestedInformation = findViewById(
                R.id.tvRequestedInformation
        );

        tvPeriod = findViewById(R.id.tvPeriod);
        tvJustification = findViewById(R.id.tvJustification);
        tvSignature = findViewById(R.id.tvSignature);

        btnBack = findViewById(R.id.btnBack);
        btnEditForm = findViewById(R.id.btnEditForm);
    }

    private void addPrintButton() {
        ViewParent parent = btnEditForm.getParent();

        if (!(parent instanceof LinearLayout)) {
            return;
        }

        LinearLayout buttonLayout = (LinearLayout) parent;

        MaterialButton btnPrint = new MaterialButton(this);

        btnPrint.setText(getPrintButtonText());
        btnPrint.setTextColor(Color.WHITE);
        btnPrint.setTextSize(15);
        btnPrint.setAllCaps(false);
        btnPrint.setCornerRadius(dp(14));

        btnPrint.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor("#062E6E")
                )
        );

        LinearLayout.LayoutParams printParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        printParams.setMargins(0, dp(12), 0, dp(8));

        int saveButtonPosition = buttonLayout.indexOfChild(btnEditForm);

        buttonLayout.addView(
                btnPrint,
                saveButtonPosition, // Save बटन से पहले Print बटन
                printParams
        );

        btnPrint.setOnClickListener(view -> printCdr());
    }

    // नया शेयर बटन बनाने वाला फंक्शन
    private void addShareButton() {
        ViewParent parent = btnEditForm.getParent();

        if (!(parent instanceof LinearLayout)) {
            return;
        }

        LinearLayout buttonLayout = (LinearLayout) parent;

        MaterialButton btnShare = new MaterialButton(this);

        boolean isHindi = "hi".equals(LanguageManager.getLanguage(this));
        btnShare.setText(isHindi ? "पीडीएफ शेयर करें (WhatsApp/Email)" : "SHARE PDF (WhatsApp/Email)");
        btnShare.setTextColor(Color.WHITE);
        btnShare.setTextSize(15);
        btnShare.setAllCaps(false);
        btnShare.setCornerRadius(dp(14));

        btnShare.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor("#2E7D32")
                )
        );

        LinearLayout.LayoutParams shareParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        shareParams.setMargins(0, dp(0), 0, dp(8));

        int saveButtonPosition = buttonLayout.indexOfChild(btnEditForm);

        buttonLayout.addView(
                btnShare,
                saveButtonPosition + 1, // Print बटन के बाद शेयर बटन
                shareParams
        );

        btnShare.setOnClickListener(view -> sharePdfDirectly());
    }

    // नया शेयर फंक्शन (साइलेंट PDF बनाकर शेयर करना)
    private void sharePdfDirectly() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "cdr_proforma_draft"
        );
        String noticeNumber = getOrCreateNoticeNumber(preferences);

        // सुरक्षित फाइल का नाम बनाना (जैसे: CDR_Proforma_123_2026.pdf)
        String safeNoticeNumber = noticeNumber.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String finalFileName = "CDR_Proforma_" + safeNoticeNumber + ".pdf";

        File cacheDir = getExternalCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File pdfFile = new File(cacheDir, finalFileName);

        // 2. बैकग्राउंड में PDF बनाना
        boolean isGenerated = CdrPdfHelper.generateSilentPdf(
                this,
                pdfFile,
                getText(tvNoticeDate),
                getText(tvTo),
                getText(tvSubject),
                getText(tvBody),
                getText(tvOfficeSummary),
                getText(tvCaseSummary),
                getText(tvIdentifierSummary),
                getText(tvRequestedInformation),
                getText(tvPeriod),
                getText(tvJustification),
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

    private String getPrintButtonText() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "प्रिंट / पीडीएफ सहेजें";
        }

        return "PRINT / SAVE PDF";
    }

    private void printCdr() {
        CdrPdfHelper.printCdr(
                CdrPreviewActivity.this,
                "CDR_Request",
                getText(tvNoticeDate),
                getText(tvTo),
                getText(tvSubject),
                getText(tvBody),
                getText(tvOfficeSummary),
                getText(tvCaseSummary),
                getText(tvIdentifierSummary),
                getText(tvRequestedInformation),
                getText(tvPeriod),
                getText(tvJustification),
                getText(tvSignature)
        );
    }

    private void loadPreview() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "cdr_proforma_draft"
        );

        String noticeNumber = getOrCreateNoticeNumber(preferences);

        String officerName = getValue(
                preferences,
                "submitting_officer"
        );

        String rank = getValue(preferences, "rank");
        String mobile = getValue(preferences, "mobile");
        String email = getValue(preferences, "email");

        String firNumber = getValue(preferences, "fir_number");
        String caseDate = getValue(preferences, "case_date");
        String sections = getValue(preferences, "sections");

        String serviceProvider = getValue(
                preferences,
                "service_provider"
        );

        String targetIdentifier = getValue(
                preferences,
                "target_identifier"
        );

        String requestedInformation = getValue(
                preferences,
                "requested_information"
        );

        String fromDate = getValue(preferences, "from_date");
        String toDate = getValue(preferences, "to_date");
        String justification = getValue(preferences, "justification");

        String officeStation = getOfficialOfficeTitle();

        if (serviceProvider.isEmpty()) {
            serviceProvider = getString(
                    R.string.cdr_nodal_officer_default
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

        tvTo.setText(getOfficialRecipientText());

        tvOfficeSummary.setText(getString(
                R.string.cdr_office_summary,
                officeStation,
                officerName,
                rank,
                mobile,
                email
        ));

        hideOfficeSummarySection();

        tvSubject.setText(getString(
                R.string.cdr_preview_subject,
                firNumber,
                sections
        ));

        tvBody.setText(getString(R.string.cdr_preview_body));

        tvCaseSummary.setText(getString(
                R.string.cdr_case_summary,
                firNumber,
                caseDate,
                sections
        ));

        tvIdentifierSummary.setText(getString(
                R.string.cdr_identifier_summary,
                serviceProvider,
                targetIdentifier
        ));

        tvRequestedInformation.setText(requestedInformation);

        tvPeriod.setText(getString(
                R.string.cdr_period_summary,
                fromDate,
                toDate
        ));

        tvJustification.setText(justification);

        tvSignature.setText(
                getOfficerSignatureBlock(
                        officerName,
                        rank,
                        mobile,
                        email
                )
        );
    }

    private void hideOfficeSummarySection() {
        tvOfficeSummary.setVisibility(View.GONE);

        ViewParent parent = tvOfficeSummary.getParent();

        if (!(parent instanceof ViewGroup)) {
            return;
        }

        ViewGroup contentLayout = (ViewGroup) parent;

        int summaryPosition = contentLayout.indexOfChild(
                tvOfficeSummary
        );

        if (summaryPosition > 0) {
            View officeHeading = contentLayout.getChildAt(
                    summaryPosition - 1
            );

            officeHeading.setVisibility(View.GONE);
        }
    }

    private String getOfficialRecipientText() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "सेवा में,\n"
                    + "श्रीमान् पुलिस अधीक्षक महोदय,\n"
                    + "जनपद - चन्दौली";
        }

        return "To,\n"
                + "The Superintendent of Police,\n"
                + "District - Chandauli";
    }

    private String getOfficialOfficeTitle() {
        OfficerProfileManager.OfficerProfile profile =
                OfficerProfileManager.getProfile(this);

        if (!profile.getDistrict().isEmpty()
                && !profile.getPoliceStation().isEmpty()) {
            return profile.getPoliceStation()
                    + ", "
                    + profile.getDistrict()
                    + ", Uttar Pradesh";
        }

        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "साइबर क्राइम थाना, चन्दौली";
        }

        return "Uttar Pradesh Police";
    }

    private String getOfficerSignatureBlock(
            String officerName,
            String rank,
            String mobile,
            String email
    ) {
        String safeName = officerName.trim();
        String safeRank = rank.trim();
        String safeMobile = mobile.trim();
        String safeEmail = email.trim();

        if (safeRank.isEmpty()) {
            safeRank = "-";
        }

        if (safeMobile.isEmpty()) {
            safeMobile = "-";
        }

        if (safeEmail.isEmpty()) {
            safeEmail = "-";
        }

        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "(" + safeName + ")"
                    + "\n" + safeRank
                    + "\nमोबाइल: " + safeMobile
                    + "\nईमेल: " + safeEmail;
        }

        return "(" + safeName + ")"
                + "\n" + safeRank
                + "\nMobile: " + safeMobile
                + "\nEmail: " + safeEmail;
    }

    private void saveNoticeToRegister() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "cdr_proforma_draft"
        );

        String noticeNumber = getOrCreateNoticeNumber(preferences);

        String primaryValue = getString(
                R.string.fir_reference,
                getValue(preferences, "fir_number")
        );

        String secondaryValue = getValue(
                preferences,
                "service_provider"
        );

        NoticeRecord noticeRecord = new NoticeRecord(
                noticeNumber,
                "CDR",
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
                    "office_summary",
                    getText(tvOfficeSummary)
            );

            snapshot.put(
                    "case_summary",
                    getText(tvCaseSummary)
            );

            snapshot.put(
                    "identifier_summary",
                    getText(tvIdentifierSummary)
            );

            snapshot.put(
                    "requested_information",
                    getText(tvRequestedInformation)
            );

            snapshot.put("period", getText(tvPeriod));

            snapshot.put(
                    "justification",
                    getText(tvJustification)
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
                "cdr_proforma_draft"
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
