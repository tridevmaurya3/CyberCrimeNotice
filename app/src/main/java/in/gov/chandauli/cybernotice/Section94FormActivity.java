package in.gov.chandauli.cybernotice;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Section94FormActivity extends BaseActivity {

    private TextInputLayout tilFirNumber;
    private TextInputLayout tilFirDate;
    private TextInputLayout tilNcrpNumber;

    private TextInputEditText etFirNumber;
    private TextInputEditText etFirDate;
    private TextInputEditText etNcrpNumber;
    private TextInputEditText etSections;

    private TextInputEditText etManagerName;
    private TextInputEditText etIfscCode;
    private TextInputEditText etBankName;
    private TextInputEditText etBranchName;
    private TextInputEditText etBranchAddress;

    private TextInputEditText etAccountNumbers;

    private TextInputEditText etStatementFrom;
    private TextInputEditText etStatementTo;

    private TextInputEditText etOfficerName;
    private TextInputEditText etRank;
    private TextInputEditText etMobile;
    private TextInputEditText etEmail;

    private RadioGroup radioNoticeBasis;
    private Button btnBack;
    private Button btnSaveDraft;
    private Button btnPreview;

    private MaterialButton btnLookupIfsc;
    private MaterialButton btnAddAccount;

    private TextView tvIfscStatus;
    private LinearLayout accountContainer;

    private final List<AccountField> accountFields = new ArrayList<>();

    private boolean isFirSelected = true;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section94_form);

        bindViews();
        setupListeners();
        loadDraft();
        setupDatePickers();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnPreview = findViewById(R.id.btnPreview);

        btnLookupIfsc = findViewById(R.id.btnLookupIfsc);
        btnAddAccount = findViewById(R.id.btnAddAccount);

        radioNoticeBasis = findViewById(R.id.radioNoticeBasis);

        tilFirNumber = findViewById(R.id.tilFirNumber);
        tilFirDate = findViewById(R.id.tilFirDate);
        tilNcrpNumber = findViewById(R.id.tilNcrpNumber);

        etFirNumber = findViewById(R.id.etFirNumber);
        etFirDate = findViewById(R.id.etFirDate);
        etNcrpNumber = findViewById(R.id.etNcrpNumber);
        etSections = findViewById(R.id.etSections);

        etManagerName = findViewById(R.id.etManagerName);
        etIfscCode = findViewById(R.id.etIfscCode);
        etBankName = findViewById(R.id.etBankName);
        etBranchName = findViewById(R.id.etBranchName);
        etBranchAddress = findViewById(R.id.etBranchAddress);

        etAccountNumbers = findViewById(R.id.etAccountNumbers);
        etStatementFrom = findViewById(R.id.etStatementFrom);
        etStatementTo = findViewById(R.id.etStatementTo);

        etOfficerName = findViewById(R.id.etOfficerName);
        etRank = findViewById(R.id.etRank);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);

        tvIfscStatus = findViewById(R.id.tvIfscStatus);
        accountContainer = findViewById(R.id.accountContainer);
    }

    private void setupListeners() {
        radioNoticeBasis.setOnCheckedChangeListener((group, checkedId) -> {
            isFirSelected = checkedId == R.id.rbFir;
            updateNoticeBasisFields();
        });

        btnBack.setOnClickListener(view -> finish());

        btnLookupIfsc.setOnClickListener(view -> lookupIfsc());

        btnAddAccount.setOnClickListener(view -> addAccountField(""));

        btnSaveDraft.setOnClickListener(view -> {
            saveDraft();

            Toast.makeText(
                    Section94FormActivity.this,
                    getString(R.string.draft_saved),
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnPreview.setOnClickListener(view -> {
            saveDraft();

            Intent intent = new Intent(
                    Section94FormActivity.this,
                    Section94PreviewActivity.class
            );

            startActivity(intent);
        });
    }

    private void setupDatePickers() {
        setupDatePicker(etFirDate);
        setupDatePicker(etStatementFrom);
        setupDatePicker(etStatementTo);
    }

    private void setupDatePicker(TextInputEditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);

        editText.setOnClickListener(view -> showDatePicker(editText));
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = getCalendarFromField(editText);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                    );

                    editText.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private Calendar getCalendarFromField(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        String value = getText(editText);

        try {
            String[] dateParts = value.split("/");

            if (dateParts.length == 3) {
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int year = Integer.parseInt(dateParts[2]);

                calendar.set(year, month, day);
            }
        } catch (Exception ignored) {
            // Date खाली होने पर आज की तारीख खुलेगी।
        }

        return calendar;
    }

    private void lookupIfsc() {
        String ifscCode = getText(etIfscCode)
                .toUpperCase(Locale.ROOT);

        etIfscCode.setText(ifscCode);

        btnLookupIfsc.setEnabled(false);

        tvIfscStatus.setText(
                getString(R.string.section94_ifsc_lookup_hint)
        );

        IfscLookupHelper.lookup(
                ifscCode,
                new IfscLookupHelper.Callback() {
                    @Override
                    public void onSuccess(
                            IfscLookupHelper.BranchDetails branchDetails
                    ) {
                        btnLookupIfsc.setEnabled(true);

                        etBankName.setText(
                                branchDetails.getBankName()
                        );

                        etBranchName.setText(
                                branchDetails.getBranchName()
                        );

                        etBranchAddress.setText(
                                branchDetails.getAddress()
                        );

                        tvIfscStatus.setText(
                                branchDetails.getIfscCode()
                                        + " - "
                                        + branchDetails.getBranchName()
                        );

                        Toast.makeText(
                                Section94FormActivity.this,
                                getIfscSuccessMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onError(String message) {
                        btnLookupIfsc.setEnabled(true);

                        etIfscCode.setError(message);

                        Toast.makeText(
                                Section94FormActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private String getIfscSuccessMessage() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "बैंक विवरण भर दिए गए हैं। जारी करने से पहले सत्यापित करें।";
        }

        return "Bank details filled. Verify before issuing.";
    }

    private void updateNoticeBasisFields() {
        if (isFirSelected) {
            tilFirNumber.setVisibility(View.VISIBLE);
            tilFirDate.setVisibility(View.VISIBLE);
            tilNcrpNumber.setVisibility(View.GONE);
        } else {
            tilFirNumber.setVisibility(View.GONE);
            tilFirDate.setVisibility(View.GONE);
            tilNcrpNumber.setVisibility(View.VISIBLE);
        }
    }

    private void addAccountField(String accountValue) {
        LinearLayout row = new LinearLayout(this);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        row.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout accountLayout = new TextInputLayout(this);

        accountLayout.setBoxBackgroundMode(
                TextInputLayout.BOX_BACKGROUND_OUTLINE
        );

        TextInputEditText accountInput = new TextInputEditText(this);

        accountInput.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        accountInput.setText(accountValue);
        accountLayout.addView(accountInput);

        LinearLayout.LayoutParams fieldParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        fieldParams.setMargins(0, 0, 0, dp(4));
        row.addView(accountLayout, fieldParams);

        MaterialButton removeButton = new MaterialButton(this);

        removeButton.setText(
                getString(R.string.section94_remove_account)
        );

        removeButton.setTextSize(12);
        removeButton.setTextColor(
                Color.parseColor("#B3261E")
        );

        removeButton.setCornerRadius(dp(10));

        removeButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor("#FDECEA")
                )
        );

        LinearLayout.LayoutParams removeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(40)
                );

        removeParams.setMargins(0, 0, 0, dp(12));
        row.addView(removeButton, removeParams);

        AccountField accountField = new AccountField(
                row,
                accountLayout,
                accountInput,
                removeButton
        );

        removeButton.setOnClickListener(view -> {
            if (accountFields.size() <= 1) {
                accountInput.setText("");
                return;
            }

            accountContainer.removeView(row);
            accountFields.remove(accountField);

            refreshAccountFields();
        });

        accountFields.add(accountField);
        accountContainer.addView(row);

        refreshAccountFields();
    }

    private void refreshAccountFields() {
        for (int index = 0; index < accountFields.size(); index++) {
            AccountField accountField = accountFields.get(index);

            accountField.inputLayout.setHint(
                    getString(
                            R.string.section94_account_number_with_index,
                            index + 1
                    )
            );

            if (accountFields.size() == 1) {
                accountField.removeButton.setVisibility(View.GONE);
            } else {
                accountField.removeButton.setVisibility(View.VISIBLE);
            }
        }

        updateLegacyAccountField();
    }

    private void updateLegacyAccountField() {
        etAccountNumbers.setText(getAccountNumbersForNotice());
    }

    private void saveDraft() {
        String accountNumbers = getAccountNumbersForNotice();

        etAccountNumbers.setText(accountNumbers);

        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_94_draft"
        );

        preferences.edit()
                .putBoolean("is_fir_selected", isFirSelected)
                .putString("fir_number", getText(etFirNumber))
                .putString("fir_date", getText(etFirDate))
                .putString("ncrp_number", getText(etNcrpNumber))
                .putString("sections", getText(etSections))
                .putString("manager_name", getText(etManagerName))
                .putString("bank_ifsc", getText(etIfscCode))
                .putString("bank_name", getText(etBankName))
                .putString("branch_name", getText(etBranchName))
                .putString("branch_address", getText(etBranchAddress))
                .putString("account_numbers", accountNumbers)
                .putString("account_list_json", getAccountListJson())
                .putString("statement_from", getText(etStatementFrom))
                .putString("statement_to", getText(etStatementTo))
                .putString("officer_name", getText(etOfficerName))
                .putString("rank", getText(etRank))
                .putString("mobile", getText(etMobile))
                .putString("email", getText(etEmail))
                .apply();

        saveOfficerProfileSafely();
    }

    private void loadDraft() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_94_draft"
        );

        isFirSelected = preferences.getBoolean(
                "is_fir_selected",
                true
        );

        if (isFirSelected) {
            radioNoticeBasis.check(R.id.rbFir);
        } else {
            radioNoticeBasis.check(R.id.rbNcrp);
        }

        updateNoticeBasisFields();

        etFirNumber.setText(
                preferences.getString("fir_number", "")
        );

        etFirDate.setText(
                preferences.getString("fir_date", "")
        );

        etNcrpNumber.setText(
                preferences.getString("ncrp_number", "")
        );

        etSections.setText(
                preferences.getString("sections", "")
        );

        etManagerName.setText(
                preferences.getString("manager_name", "")
        );

        etIfscCode.setText(
                preferences.getString("bank_ifsc", "")
        );

        etBankName.setText(
                preferences.getString("bank_name", "")
        );

        etBranchName.setText(
                preferences.getString("branch_name", "")
        );

        etBranchAddress.setText(
                preferences.getString("branch_address", "")
        );

        etStatementFrom.setText(
                preferences.getString("statement_from", "")
        );

        etStatementTo.setText(
                preferences.getString("statement_to", "")
        );

        loadAccountFields(preferences);

        OfficerProfileManager.OfficerProfile profile =
                OfficerProfileManager.getProfile(this);

        setOfficerField(
                etOfficerName,
                preferences.getString("officer_name", ""),
                profile.getName()
        );

        setOfficerField(
                etRank,
                preferences.getString("rank", ""),
                profile.getRank()
        );

        setOfficerField(
                etMobile,
                preferences.getString("mobile", ""),
                profile.getMobile()
        );

        String savedEmail = preferences.getString("email", "");

        if (savedEmail.isEmpty()) {
            savedEmail = profile.getEmail();
        }

        if (savedEmail.isEmpty()) {
            savedEmail = getString(
                    R.string.official_email_default
            );
        }

        etEmail.setText(savedEmail);
    }

    private void loadAccountFields(
            SharedPreferences preferences
    ) {
        accountContainer.removeAllViews();
        accountFields.clear();

        String savedJson = preferences.getString(
                "account_list_json",
                ""
        );

        boolean addedAccount = false;

        if (!savedJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(savedJson);

                for (int index = 0; index < jsonArray.length(); index++) {
                    String accountValue =
                            jsonArray.optString(index, "").trim();

                    if (!accountValue.isEmpty()) {
                        addAccountField(accountValue);
                        addedAccount = true;
                    }
                }
            } catch (Exception ignored) {
                // पुराने draft का गलत JSON होने पर नीचे वाला विकल्प चलेगा।
            }
        }

        if (!addedAccount) {
            String oldAccountValue = preferences.getString(
                    "account_numbers",
                    ""
            );

            addAccountField(oldAccountValue);
        }
    }

    private String getAccountNumbersForNotice() {
        StringBuilder builder = new StringBuilder();
        int serialNumber = 1;

        for (AccountField accountField : accountFields) {
            String accountValue = getText(accountField.input);

            if (!accountValue.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }

                builder.append(serialNumber)
                        .append(". ")
                        .append(accountValue);

                serialNumber++;
            }
        }

        return builder.toString();
    }

    private String getAccountListJson() {
        JSONArray jsonArray = new JSONArray();

        for (AccountField accountField : accountFields) {
            String accountValue = getText(accountField.input);

            if (!accountValue.isEmpty()) {
                jsonArray.put(accountValue);
            }
        }

        return jsonArray.toString();
    }

    private void saveOfficerProfileSafely() {
        OfficerProfileManager.OfficerProfile existingProfile =
                OfficerProfileManager.getProfile(this);

        String officerName = firstAvailable(
                getText(etOfficerName),
                existingProfile.getName()
        );

        String rank = firstAvailable(
                getText(etRank),
                existingProfile.getRank()
        );

        String mobile = firstAvailable(
                getText(etMobile),
                existingProfile.getMobile()
        );

        String email = firstAvailable(
                getText(etEmail),
                existingProfile.getEmail()
        );

        if (!officerName.isEmpty()
                || !rank.isEmpty()
                || !mobile.isEmpty()
                || !email.isEmpty()) {

            OfficerProfileManager.saveProfile(
                    this,
                    officerName,
                    rank,
                    mobile,
                    email
            );
        }
    }

    private String firstAvailable(
            String currentValue,
            String savedValue
    ) {
        if (currentValue != null && !currentValue.trim().isEmpty()) {
            return currentValue.trim();
        }

        if (savedValue != null) {
            return savedValue.trim();
        }

        return "";
    }

    private void setOfficerField(
            TextInputEditText editText,
            String savedValue,
            String profileValue
    ) {
        if (!savedValue.isEmpty()) {
            editText.setText(savedValue);
        } else {
            editText.setText(profileValue);
        }
    }

    private String getText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    private static class AccountField {

        private final LinearLayout row;
        private final TextInputLayout inputLayout;
        private final TextInputEditText input;
        private final MaterialButton removeButton;

        AccountField(
                LinearLayout row,
                TextInputLayout inputLayout,
                TextInputEditText input,
                MaterialButton removeButton
        ) {
            this.row = row;
            this.inputLayout = inputLayout;
            this.input = input;
            this.removeButton = removeButton;
        }
    }
}
