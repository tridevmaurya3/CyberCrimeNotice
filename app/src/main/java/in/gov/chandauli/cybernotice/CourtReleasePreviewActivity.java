package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;

public class CourtReleasePreviewActivity extends BaseActivity {

    private ImageButton btnBack;
    private Button btnSaveToRegister;
    private Button btnPrintPdf;
    private Button btnSharePdf;
    private Button btnBackBottom;
    private TextView tvNoticeDate;
    private TextView tvTo;
    private TextView tvSubject;
    private TextView tvCaseSummary;
    private TextView tvTransactionSummary;
    private TextView tvInstruction;
    private TextView tvSignature;

    private CourtReleasePdfHelper.CourtReleaseData releaseData;
    private String noticeNumber;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_release_preview);

        bindViews();
        releaseData = CourtReleaseActivity.readDraftData(this);
        noticeNumber = getOrCreateNoticeNumber();
        releaseData.noticeNumber = noticeNumber;
        loadPreview();
        setupListeners();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveToRegister = findViewById(R.id.btnSaveToRegister);
        btnPrintPdf = findViewById(R.id.btnPrintPdf);
        btnSharePdf = findViewById(R.id.btnSharePdf);
        btnBackBottom = findViewById(R.id.btnBackBottom);

        tvNoticeDate = findViewById(R.id.tvNoticeDate);
        tvTo = findViewById(R.id.tvTo);
        tvSubject = findViewById(R.id.tvSubject);
        tvCaseSummary = findViewById(R.id.tvCaseSummary);
        tvTransactionSummary = findViewById(R.id.tvTransactionSummary);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvSignature = findViewById(R.id.tvSignature);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(view -> finish());
        btnBackBottom.setOnClickListener(view -> finish());
        btnSaveToRegister.setOnClickListener(view -> saveNoticeToRegister());
        btnPrintPdf.setOnClickListener(view -> printPdf());
        btnSharePdf.setOnClickListener(view -> sharePdf());
    }

    private void loadPreview() {
        tvNoticeDate.setText(getString(
                R.string.cr_notice_reference,
                noticeNumber,
                safe(releaseData.letterDate)
        ));
        tvTo.setText(getString(R.string.cr_recipient, safe(releaseData.nodalBank)));
        tvSubject.setText(getString(
                R.string.cr_subject_text,
                safe(releaseData.firNo)
        ));
        tvCaseSummary.setText(getString(
                R.string.cr_case_summary_text,
                safe(releaseData.appName),
                safe(releaseData.firNo),
                safe(releaseData.ncrpNo),
                safe(releaseData.appBank),
                safe(releaseData.fraudAmount),
                safe(releaseData.holdAmount),
                safe(releaseData.releaseAmount)
        ));
        tvTransactionSummary.setText(buildTransactionSummary());
        tvInstruction.setText(getString(
                R.string.cr_release_instruction_text,
                safe(releaseData.releaseAmount),
                safe(releaseData.appName),
                safe(releaseData.appAccount),
                safe(releaseData.appIfsc)
        ));
        tvSignature.setText(R.string.cr_signature_block);
    }

    private String buildTransactionSummary() {
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < releaseData.transactions.size(); index++) {
            CourtReleasePdfHelper.TransactionData transaction =
                    releaseData.transactions.get(index);

            if (builder.length() > 0) {
                builder.append("\n\n");
            }

            builder.append(getString(
                    R.string.cr_transaction_item,
                    index + 1,
                    safe(transaction.appAccIfsc),
                    safe(transaction.fraudAccIfsc),
                    safe(transaction.utr),
                    safe(transaction.date),
                    safe(transaction.amount)
            ));
        }

        return builder.toString();
    }

    private void saveNoticeToRegister() {
        NoticeRecord noticeRecord = new NoticeRecord(
                noticeNumber,
                "COURT_RELEASE",
                getString(R.string.cr_fir_reference, safe(releaseData.firNo)),
                getString(R.string.cr_applicant_reference, safe(releaseData.appName)),
                "DRAFT",
                System.currentTimeMillis(),
                createDocumentSnapshot()
        );

        NoticeStore.saveNotice(this, noticeRecord);
        Toast.makeText(this, R.string.cr_saved_to_register, Toast.LENGTH_SHORT).show();
    }

    private String createDocumentSnapshot() {
        try {
            JSONObject courtReleaseData = CourtReleasePdfHelper.toJson(releaseData);
            JSONObject snapshot = new JSONObject();
            snapshot.put("notice_date", getText(tvNoticeDate));
            snapshot.put("to", getText(tvTo));
            snapshot.put("subject", getText(tvSubject));
            snapshot.put("body", getText(tvCaseSummary));
            snapshot.put("court_release_summary", getText(tvCaseSummary));
            snapshot.put("transaction_details", getText(tvTransactionSummary));
            snapshot.put("instruction", getText(tvInstruction));
            snapshot.put("signature", getText(tvSignature));
            snapshot.put("court_release_data", courtReleaseData);
            snapshot.put("form_data", courtReleaseData);
            return snapshot.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private void printPdf() {
        CourtReleasePdfHelper.printCourtRelease(
                this,
                "Court_Release_" + safeFileName(noticeNumber),
                releaseData,
                isHindi()
        );
    }

    private void sharePdf() {
        File cacheDirectory = getExternalCacheDir();
        if (cacheDirectory == null) {
            cacheDirectory = getCacheDir();
        }

        if (!cacheDirectory.exists() && !cacheDirectory.mkdirs()) {
            Toast.makeText(this, R.string.cr_pdf_failed, Toast.LENGTH_LONG).show();
            return;
        }

        File pdfFile = new File(
                cacheDirectory,
                "Court_Release_" + safeFileName(noticeNumber) + ".pdf"
        );

        boolean generated = CourtReleasePdfHelper.generateSilentPdf(
                this,
                pdfFile,
                releaseData,
                isHindi()
        );

        if (generated && pdfFile.exists()) {
            ShareManager.sharePdf(this, pdfFile, noticeNumber);
        } else {
            Toast.makeText(this, R.string.cr_pdf_failed, Toast.LENGTH_LONG).show();
        }
    }

    private String getOrCreateNoticeNumber() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                CourtReleaseActivity.DRAFT_PREFERENCES
        );
        String existingNumber = preferences.getString("notice_number", "");

        if (!existingNumber.isEmpty()) {
            return existingNumber;
        }

        String newNumber = NoticeNumberManager.getNextNoticeNumber(this);
        preferences.edit().putString("notice_number", newNumber).apply();
        return newNumber;
    }

    private boolean isHindi() {
        return "hi".equals(LanguageManager.getLanguage(this));
    }

    private String getText(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeFileName(String value) {
        return safe(value).replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
