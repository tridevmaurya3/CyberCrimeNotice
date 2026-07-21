package in.gov.chandauli.cybernotice;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CourtReleaseActivity extends BaseActivity {

    public static final String DRAFT_PREFERENCES = "court_release_draft";

    private LinearLayout transactionContainer;
    private MaterialButton btnAddTransaction;
    private MaterialButton btnSaveDraft;
    private MaterialButton btnPreview;
    private AutoCompleteTextView dropdownReportType;
    private TextInputLayout tilReportDate;

    private TextInputEditText etLetterYear;
    private TextInputEditText etLetterDate;
    private TextInputEditText etNodalBank;
    private TextInputEditText etApplicantName;
    private TextInputEditText etFatherName;
    private TextInputEditText etAddress;
    private TextInputEditText etApplicantAccount;
    private TextInputEditText etApplicantBankName;
    private TextInputEditText etApplicantIFSC;
    private TextInputEditText etApplicantBranch;
    private TextInputEditText etApplicantBankAddress;
    private TextInputEditText etFraudDate;
    private TextInputEditText etFraudAmount;
    private TextInputEditText etNcrpNo;
    private TextInputEditText etFirNo;
    private TextInputEditText etSections;
    private TextInputEditText etReportDate;
    private TextInputEditText etHoldAmount;
    private TextInputEditText etReleaseAmount;
    private MaterialButton btnLookupApplicantIfsc;
    private TextView tvApplicantIfscStatus;

    private final ArrayList<View> transactionRows = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_release_order);

        bindViews();
        setupDropdown();
        setupDatePickers();
        loadDraft();

        btnLookupApplicantIfsc.setOnClickListener(view -> lookupIfsc(
                etApplicantIFSC,
                btnLookupApplicantIfsc,
                etApplicantBankName,
                etApplicantBranch,
                etApplicantBankAddress,
                tvApplicantIfscStatus
        ));
        btnAddTransaction.setOnClickListener(view -> addTransactionRow(null));
        btnSaveDraft.setOnClickListener(view -> {
            saveDraftToRegister();
        });
        btnPreview.setOnClickListener(view -> openPreview());
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void bindViews() {
        transactionContainer = findViewById(R.id.transactionContainer);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnPreview = findViewById(R.id.btnGeneratePdf);
        dropdownReportType = findViewById(R.id.dropdownReportType);
        tilReportDate = findViewById(R.id.tilReportDate);

        etLetterYear = findViewById(R.id.etLetterYear);
        etLetterDate = findViewById(R.id.etLetterDate);
        etNodalBank = findViewById(R.id.etNodalBank);
        etApplicantName = findViewById(R.id.etApplicantName);
        etFatherName = findViewById(R.id.etFatherName);
        etAddress = findViewById(R.id.etAddress);
        etApplicantAccount = findViewById(R.id.etApplicantAccount);
        etApplicantBankName = findViewById(R.id.etApplicantBankName);
        etApplicantIFSC = findViewById(R.id.etApplicantIFSC);
        etApplicantBranch = findViewById(R.id.etApplicantBranch);
        etApplicantBankAddress = findViewById(R.id.etApplicantBankAddress);
        etFraudDate = findViewById(R.id.etFraudDate);
        etFraudAmount = findViewById(R.id.etFraudAmount);
        etNcrpNo = findViewById(R.id.etNcrpNo);
        etFirNo = findViewById(R.id.etFirNo);
        etSections = findViewById(R.id.etSections);
        etReportDate = findViewById(R.id.etReportDate);
        etHoldAmount = findViewById(R.id.etHoldAmount);
        etReleaseAmount = findViewById(R.id.etReleaseAmount);
        btnLookupApplicantIfsc = findViewById(R.id.btnLookupApplicantIfsc);
        tvApplicantIfscStatus = findViewById(R.id.tvApplicantIfscStatus);
    }

    private void setupDropdown() {
        String[] reportOptions = {
                getString(R.string.cr_report_final),
                getString(R.string.cr_report_chargesheet),
                getString(R.string.cr_report_pending)
        };

        dropdownReportType.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                reportOptions
        ));

        dropdownReportType.setOnItemClickListener((parent, view, position, id) ->
                updateReportDateVisibility(position == 2)
        );
    }

    private void setupDatePickers() {
        setupDatePicker(etLetterDate);
        setupDatePicker(etFraudDate);
        setupDatePicker(etReportDate);
    }

    private void setupDatePicker(TextInputEditText field) {
        field.setFocusable(false);
        field.setClickable(true);
        field.setCursorVisible(false);
        field.setOnClickListener(view -> showDatePicker(field));
    }

    private void showDatePicker(TextInputEditText field) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> field.setText(String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        dayOfMonth,
                        month + 1,
                        year
                )),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateReportDateVisibility(boolean isPending) {
        tilReportDate.setVisibility(isPending ? View.GONE : View.VISIBLE);
        if (isPending) {
            etReportDate.setText("");
            etReportDate.setError(null);
        }
    }

    private boolean isPendingReport(String reportType) {
        return getString(R.string.cr_report_pending).equals(reportType);
    }

    private void lookupIfsc(
            TextInputEditText ifscInput,
            MaterialButton lookupButton,
            TextInputEditText bankInput,
            TextInputEditText branchInput,
            TextInputEditText addressInput,
            TextView statusView
    ) {
        String ifscCode = getText(ifscInput).toUpperCase(Locale.ROOT);
        ifscInput.setText(ifscCode);
        ifscInput.setError(null);
        lookupButton.setEnabled(false);
        statusView.setText(R.string.cr_ifsc_lookup_hint);

        IfscLookupHelper.lookup(ifscCode, new IfscLookupHelper.Callback() {
            @Override
            public void onSuccess(IfscLookupHelper.BranchDetails branchDetails) {
                lookupButton.setEnabled(true);
                bankInput.setText(branchDetails.getBankName());
                branchInput.setText(branchDetails.getBranchName());
                addressInput.setText(branchDetails.getAddress());
                statusView.setText(
                        branchDetails.getIfscCode()
                                + " - "
                                + branchDetails.getBranchName()
                );
                Toast.makeText(
                        CourtReleaseActivity.this,
                        R.string.cr_ifsc_success,
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onError(String message) {
                lookupButton.setEnabled(true);
                ifscInput.setError(message);
                statusView.setText(message);
                Toast.makeText(
                        CourtReleaseActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void addTransactionRow(CourtReleasePdfHelper.TransactionData transaction) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View rowView = inflater.inflate(
                R.layout.item_transaction_row,
                transactionContainer,
                false
        );

        ImageButton btnRemoveRow = rowView.findViewById(R.id.btnRemoveRow);
        btnRemoveRow.setOnClickListener(view -> {
            transactionContainer.removeView(rowView);
            transactionRows.remove(rowView);
        });

        TextInputEditText etFraudIfsc = rowView.findViewById(R.id.etFraudIFSC);
        TextInputEditText etFraudBank = rowView.findViewById(R.id.etFraudBankName);
        TextInputEditText etFraudBranch = rowView.findViewById(R.id.etFraudBranchName);
        TextInputEditText etFraudAddress = rowView.findViewById(R.id.etFraudBankAddress);
        MaterialButton btnLookupFraudIfsc = rowView.findViewById(R.id.btnLookupFraudIfsc);
        TextView tvFraudIfscStatus = rowView.findViewById(R.id.tvFraudIfscStatus);

        btnLookupFraudIfsc.setOnClickListener(view -> lookupIfsc(
                etFraudIfsc,
                btnLookupFraudIfsc,
                etFraudBank,
                etFraudBranch,
                etFraudAddress,
                tvFraudIfscStatus
        ));

        if (transaction != null) {
            setRowValue(rowView, R.id.etFraudAccount, transaction.fraudAccount);
            setRowValue(rowView, R.id.etFraudIFSC, transaction.fraudIfsc);
            setRowValue(rowView, R.id.etFraudBankName, transaction.fraudBank);
            setRowValue(rowView, R.id.etFraudBranchName, transaction.fraudBranch);
            setRowValue(rowView, R.id.etFraudBankAddress, transaction.fraudAddress);
            setRowValue(rowView, R.id.etUtr, transaction.utr);
            setRowValue(rowView, R.id.etTransDate, transaction.date);
            setRowValue(rowView, R.id.etTransAmount, transaction.amount);
        }

        transactionContainer.addView(rowView);
        transactionRows.add(rowView);
    }

    private void setRowValue(View rowView, int fieldId, String value) {
        EditText field = rowView.findViewById(fieldId);
        field.setText(value == null ? "" : value);
    }

    private void openPreview() {
        if (!validateForPreview()) {
            return;
        }

        saveDraft();
        try {
            startActivity(new Intent(this, CourtReleasePreviewActivity.class));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(
                    this,
                    R.string.cr_preview_unavailable,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private boolean validateForPreview() {
        boolean valid = true;
        valid &= requireValue(etLetterYear);
        valid &= requireValue(etLetterDate);
        valid &= requireValue(etNodalBank);
        valid &= requireValue(etApplicantName);
        valid &= requireValue(etApplicantAccount);
        valid &= requireValue(etApplicantBankName);
        valid &= requireValue(etApplicantIFSC);
        valid &= requireValue(etNcrpNo);
        valid &= requireValue(etFirNo);
        valid &= requireValue(etHoldAmount);
        valid &= requireValue(etReleaseAmount);

        String reportType = getText(dropdownReportType);
        if (reportType.isEmpty()) {
            dropdownReportType.setError(getString(R.string.cr_required_field));
            valid = false;
        } else {
            dropdownReportType.setError(null);
        }

        if (!isPendingReport(reportType)) {
            valid &= requireValue(etReportDate);
        }

        if (getTransactionDataFromRows().isEmpty()) {
            Toast.makeText(this, R.string.cr_transaction_required, Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }

    private boolean requireValue(TextInputEditText field) {
        if (getText(field).isEmpty()) {
            field.setError(getString(R.string.cr_required_field));
            return false;
        }

        field.setError(null);
        return true;
    }

    private void saveDraft() {
        SharedPreferences.Editor editor = UserSessionManager.getScopedPreferences(
                this,
                DRAFT_PREFERENCES
        ).edit();

        editor.putString("letter_year", getText(etLetterYear));
        editor.putString("letter_date", getText(etLetterDate));
        editor.putString("nodal_bank", getText(etNodalBank));
        editor.putString("applicant_name", getText(etApplicantName));
        editor.putString("father_name", getText(etFatherName));
        editor.putString("address", getText(etAddress));
        editor.putString("applicant_account", getText(etApplicantAccount));
        editor.putString("applicant_bank", getText(etApplicantBankName));
        editor.putString("applicant_ifsc", getText(etApplicantIFSC));
        editor.putString("applicant_branch", getText(etApplicantBranch));
        editor.putString("applicant_bank_address", getText(etApplicantBankAddress));
        editor.putString("fraud_date", getText(etFraudDate));
        editor.putString("fraud_amount", getText(etFraudAmount));
        editor.putString("ncrp_number", getText(etNcrpNo));
        editor.putString("fir_number", getText(etFirNo));
        editor.putString("sections", getText(etSections));
        editor.putString("report_type", getText(dropdownReportType));
        editor.putString("report_date", getText(etReportDate));
        editor.putString("hold_amount", getText(etHoldAmount));
        editor.putString("release_amount", getText(etReleaseAmount));
        editor.putString("transactions_json", getTransactionsJson().toString());
        editor.apply();
    }

    private void saveDraftToRegister() {
        saveDraft();

        CourtReleasePdfHelper.CourtReleaseData data = readDraftData(this);
        String noticeNumber = getOrCreateNoticeNumber();
        data.noticeNumber = noticeNumber;

        NoticeRecord draftRecord = new NoticeRecord(
                noticeNumber,
                "COURT_RELEASE",
                getString(R.string.cr_fir_reference, safe(data.firNo)),
                getString(R.string.cr_applicant_reference, safe(data.appName)),
                "DRAFT",
                System.currentTimeMillis(),
                createDraftSnapshot(data)
        );

        NoticeStore.saveNotice(this, draftRecord);
        Toast.makeText(this, R.string.cr_draft_registered, Toast.LENGTH_SHORT).show();
    }

    private String getOrCreateNoticeNumber() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                DRAFT_PREFERENCES
        );
        String existingNumber = preferences.getString("notice_number", "");

        if (!existingNumber.isEmpty()) {
            return existingNumber;
        }

        String newNumber = NoticeNumberManager.getNextNoticeNumber(this);
        preferences.edit().putString("notice_number", newNumber).apply();
        return newNumber;
    }

    private String createDraftSnapshot(
            CourtReleasePdfHelper.CourtReleaseData data
    ) {
        try {
            JSONObject formData = CourtReleasePdfHelper.toJson(data);
            JSONObject snapshot = new JSONObject();
            String caseSummary = getString(
                    R.string.cr_case_summary_text,
                    safe(data.appName),
                    safe(data.firNo),
                    safe(data.ncrpNo),
                    safe(data.appBank),
                    safe(data.fraudAmount),
                    safe(data.holdAmount),
                    safe(data.releaseAmount)
            );

            snapshot.put("notice_date", safe(data.letterDate));
            snapshot.put("to", safe(data.nodalBank));
            snapshot.put(
                    "subject",
                    getString(R.string.cr_subject_text, safe(data.firNo))
            );
            snapshot.put("body", caseSummary);
            snapshot.put("court_release_summary", caseSummary);
            snapshot.put("transaction_details", buildDraftTransactionSummary(data));
            snapshot.put(
                    "instruction",
                    getString(
                            R.string.cr_release_instruction_text,
                            safe(data.releaseAmount),
                            safe(data.appName),
                            safe(data.appAccount),
                            safe(data.appIfsc)
                    )
            );
            snapshot.put("court_release_data", formData);
            snapshot.put("form_data", formData);
            return snapshot.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String buildDraftTransactionSummary(
            CourtReleasePdfHelper.CourtReleaseData data
    ) {
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < data.transactions.size(); index++) {
            CourtReleasePdfHelper.TransactionData transaction =
                    data.transactions.get(index);

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

    private void loadDraft() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                DRAFT_PREFERENCES
        );

        etLetterYear.setText(preferences.getString("letter_year", ""));
        etLetterDate.setText(preferences.getString("letter_date", ""));
        etNodalBank.setText(preferences.getString("nodal_bank", ""));
        etApplicantName.setText(preferences.getString("applicant_name", ""));
        etFatherName.setText(preferences.getString("father_name", ""));
        etAddress.setText(preferences.getString("address", ""));
        etApplicantAccount.setText(preferences.getString("applicant_account", ""));
        etApplicantBankName.setText(preferences.getString("applicant_bank", ""));
        etApplicantIFSC.setText(preferences.getString("applicant_ifsc", ""));
        etApplicantBranch.setText(preferences.getString("applicant_branch", ""));
        etApplicantBankAddress.setText(preferences.getString("applicant_bank_address", ""));
        etFraudDate.setText(preferences.getString("fraud_date", ""));
        etFraudAmount.setText(preferences.getString("fraud_amount", ""));
        etNcrpNo.setText(preferences.getString("ncrp_number", ""));
        etFirNo.setText(preferences.getString("fir_number", ""));
        etSections.setText(preferences.getString("sections", ""));
        etReportDate.setText(preferences.getString("report_date", ""));
        etHoldAmount.setText(preferences.getString("hold_amount", ""));
        etReleaseAmount.setText(preferences.getString("release_amount", ""));

        String savedReportType = preferences.getString("report_type", "");
        dropdownReportType.setText(savedReportType, false);
        updateReportDateVisibility(isPendingReport(savedReportType));

        transactionContainer.removeAllViews();
        transactionRows.clear();

        List<CourtReleasePdfHelper.TransactionData> transactions =
                readTransactions(preferences.getString("transactions_json", "[]"));

        if (transactions.isEmpty()) {
            addTransactionRow(null);
        } else {
            for (CourtReleasePdfHelper.TransactionData transaction : transactions) {
                addTransactionRow(transaction);
            }
        }
    }

    public static CourtReleasePdfHelper.CourtReleaseData readDraftData(Context context) {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                context,
                DRAFT_PREFERENCES
        );

        return new CourtReleasePdfHelper.CourtReleaseData(
                preferences.getString("letter_year", ""),
                preferences.getString("letter_date", ""),
                preferences.getString("nodal_bank", ""),
                preferences.getString("applicant_name", ""),
                preferences.getString("father_name", ""),
                preferences.getString("address", ""),
                preferences.getString("applicant_account", ""),
                preferences.getString("applicant_bank", ""),
                preferences.getString("applicant_ifsc", ""),
                preferences.getString("applicant_branch", ""),
                preferences.getString("applicant_bank_address", ""),
                preferences.getString("fraud_date", ""),
                preferences.getString("fraud_amount", ""),
                preferences.getString("ncrp_number", ""),
                preferences.getString("fir_number", ""),
                preferences.getString("sections", ""),
                preferences.getString("report_type", ""),
                preferences.getString("report_date", ""),
                preferences.getString("hold_amount", ""),
                preferences.getString("release_amount", ""),
                readTransactions(preferences.getString("transactions_json", "[]"))
        );
    }

    private JSONArray getTransactionsJson() {
        JSONArray transactions = new JSONArray();

        for (CourtReleasePdfHelper.TransactionData transaction : getTransactionDataFromRows()) {
            JSONObject object = new JSONObject();
            try {
                object.put("app_acc_ifsc", transaction.appAccIfsc);
                object.put("fraud_acc_ifsc", transaction.fraudAccIfsc);
                object.put("fraud_account", transaction.fraudAccount);
                object.put("fraud_ifsc", transaction.fraudIfsc);
                object.put("fraud_bank", transaction.fraudBank);
                object.put("fraud_branch", transaction.fraudBranch);
                object.put("fraud_address", transaction.fraudAddress);
                object.put("utr", transaction.utr);
                object.put("date", transaction.date);
                object.put("amount", transaction.amount);
                transactions.put(object);
            } catch (Exception ignored) {
            }
        }

        return transactions;
    }

    private List<CourtReleasePdfHelper.TransactionData> getTransactionDataFromRows() {
        List<CourtReleasePdfHelper.TransactionData> transactions = new ArrayList<>();
        String applicantAccountIfsc = formatAccountAndIfsc(
                getText(etApplicantAccount),
                getText(etApplicantIFSC)
        );

        for (View rowView : transactionRows) {
            String fraudAccount = getRowValue(rowView, R.id.etFraudAccount);
            String fraudIfsc = getRowValue(rowView, R.id.etFraudIFSC);
            CourtReleasePdfHelper.TransactionData transaction =
                    new CourtReleasePdfHelper.TransactionData(
                            applicantAccountIfsc,
                            formatAccountAndIfsc(fraudAccount, fraudIfsc),
                            getRowValue(rowView, R.id.etUtr),
                            getRowValue(rowView, R.id.etTransDate),
                            getRowValue(rowView, R.id.etTransAmount)
                    );
            transaction.fraudAccount = fraudAccount;
            transaction.fraudIfsc = fraudIfsc;
            transaction.fraudBank = getRowValue(rowView, R.id.etFraudBankName);
            transaction.fraudBranch = getRowValue(rowView, R.id.etFraudBranchName);
            transaction.fraudAddress = getRowValue(rowView, R.id.etFraudBankAddress);

            if (hasTransactionValue(transaction)) {
                transactions.add(transaction);
            }
        }

        return transactions;
    }

    private String getRowValue(View rowView, int fieldId) {
        EditText field = rowView.findViewById(fieldId);
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private String formatAccountAndIfsc(String account, String ifsc) {
        if (account.isEmpty()) {
            return ifsc;
        }
        if (ifsc.isEmpty()) {
            return account;
        }
        return account + " / " + ifsc;
    }

    private boolean hasTransactionValue(CourtReleasePdfHelper.TransactionData transaction) {
        return !transaction.fraudAccount.isEmpty()
                || !transaction.fraudIfsc.isEmpty()
                || !transaction.fraudBank.isEmpty()
                || !transaction.fraudBranch.isEmpty()
                || !transaction.fraudAddress.isEmpty()
                || !transaction.utr.isEmpty()
                || !transaction.date.isEmpty()
                || !transaction.amount.isEmpty();
    }

    private static List<CourtReleasePdfHelper.TransactionData> readTransactions(String json) {
        List<CourtReleasePdfHelper.TransactionData> transactions = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(json);
            for (int index = 0; index < array.length(); index++) {
                JSONObject object = array.getJSONObject(index);
                CourtReleasePdfHelper.TransactionData transaction =
                        new CourtReleasePdfHelper.TransactionData(
                        object.optString("app_acc_ifsc", ""),
                        object.optString("fraud_acc_ifsc", ""),
                        object.optString("utr", ""),
                        object.optString("date", ""),
                        object.optString("amount", "")
                );
                transaction.fraudAccount = object.optString(
                        "fraud_account",
                        transaction.fraudAccount
                );
                transaction.fraudIfsc = object.optString("fraud_ifsc", "");
                transaction.fraudBank = object.optString("fraud_bank", "");
                transaction.fraudBranch = object.optString("fraud_branch", "");
                transaction.fraudAddress = object.optString("fraud_address", "");
                transactions.add(transaction);
            }
        } catch (Exception ignored) {
        }

        return transactions;
    }

    private String getText(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private String getText(AutoCompleteTextView field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
