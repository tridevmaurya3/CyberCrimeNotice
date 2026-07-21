package in.gov.chandauli.cybernotice;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class Section35FormActivity extends BaseActivity {

    private TextInputEditText etCrimeNumber;
    private TextInputEditText etCrimeDate;
    private TextInputEditText etSections;

    private TextInputEditText etRecipientName;
    private TextInputEditText etFatherSpouseName;
    private TextInputEditText etRecipientAddress;
    private TextInputEditText etRecipientMobile;

    private TextInputEditText etTransactionAmount;
    private TextInputEditText etTransactionDate;
    private TextInputEditText etIfscCode;
    private TextInputEditText etBankName;
    private TextInputEditText etBranchName;
    private TextInputEditText etBranchAddress;
    private TextInputEditText etAccountNumber;
    private TextInputEditText etUtrNumber;
    private TextInputEditText etWithdrawalDate;

    private TextInputEditText etAppearanceDate;
    private TextInputEditText etAppearanceTime;
    private TextInputEditText etAppearancePlace;

    private TextInputEditText etOfficerName;
    private TextInputEditText etRank;
    private TextInputEditText etMobile;
    private TextInputEditText etEmail;

    private Button btnBack;
    private Button btnSaveDraft;
    private Button btnPreview;

    private MaterialButton btnLookupIfsc;
    private TextView tvIfscStatus;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section35_form);

        bindViews();
        loadDraft();
        setupDateAndTimePickers();

        btnBack.setOnClickListener(view -> finish());

        btnLookupIfsc.setOnClickListener(view -> lookupIfsc());

        btnSaveDraft.setOnClickListener(view -> {
            saveDraft();

            Toast.makeText(
                    Section35FormActivity.this,
                    getString(R.string.section_35_draft_saved),
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnPreview.setOnClickListener(view -> {
            saveDraft();

            Intent intent = new Intent(
                    Section35FormActivity.this,
                    Section35PreviewActivity.class
            );

            startActivity(intent);
        });
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnPreview = findViewById(R.id.btnPreview);

        btnLookupIfsc = findViewById(R.id.btnLookupIfsc);
        tvIfscStatus = findViewById(R.id.tvIfscStatus);

        etCrimeNumber = findViewById(R.id.etCrimeNumber);
        etCrimeDate = findViewById(R.id.etCrimeDate);
        etSections = findViewById(R.id.etSections);

        etRecipientName = findViewById(R.id.etRecipientName);
        etFatherSpouseName = findViewById(R.id.etFatherSpouseName);
        etRecipientAddress = findViewById(R.id.etRecipientAddress);
        etRecipientMobile = findViewById(R.id.etRecipientMobile);

        etTransactionAmount = findViewById(R.id.etTransactionAmount);
        etTransactionDate = findViewById(R.id.etTransactionDate);
        etIfscCode = findViewById(R.id.etIfscCode);
        etBankName = findViewById(R.id.etBankName);
        etBranchName = findViewById(R.id.etBranchName);
        etBranchAddress = findViewById(R.id.etBranchAddress);
        etAccountNumber = findViewById(R.id.etAccountNumber);
        etUtrNumber = findViewById(R.id.etUtrNumber);
        etWithdrawalDate = findViewById(R.id.etWithdrawalDate);

        etAppearanceDate = findViewById(R.id.etAppearanceDate);
        etAppearanceTime = findViewById(R.id.etAppearanceTime);
        etAppearancePlace = findViewById(R.id.etAppearancePlace);

        etOfficerName = findViewById(R.id.etOfficerName);
        etRank = findViewById(R.id.etRank);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
    }

    private void setupDateAndTimePickers() {
        setupDatePicker(etCrimeDate);
        setupDatePicker(etTransactionDate);
        setupDatePicker(etWithdrawalDate);
        setupDatePicker(etAppearanceDate);

        setupTimePicker(etAppearanceTime);
    }

    private void setupDatePicker(TextInputEditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);

        editText.setOnClickListener(view -> showDatePicker(editText));
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = getCalendarFromDateField(editText);

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

    private void setupTimePicker(TextInputEditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);

        editText.setOnClickListener(view -> showTimePicker(editText));
    }

    private void showTimePicker(TextInputEditText editText) {
        Calendar calendar = getCalendarFromTimeField(editText);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (timePicker, hourOfDay, minute) -> {
                    String selectedTime = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hourOfDay,
                            minute
                    );

                    editText.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        dialog.show();
    }

    private Calendar getCalendarFromDateField(
            TextInputEditText editText
    ) {
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
            // खाली या गलत date होने पर आज की तारीख खुलेगी।
        }

        return calendar;
    }

    private Calendar getCalendarFromTimeField(
            TextInputEditText editText
    ) {
        Calendar calendar = Calendar.getInstance();
        String value = getText(editText);

        try {
            String[] timeParts = value.split(":");

            if (timeParts.length == 2) {
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            }
        } catch (Exception ignored) {
            // खाली या गलत time होने पर वर्तमान समय खुलेगा।
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
                                Section35FormActivity.this,
                                getIfscSuccessMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onError(String message) {
                        btnLookupIfsc.setEnabled(true);

                        etIfscCode.setError(
                                getIfscErrorMessage()
                        );

                        Toast.makeText(
                                Section35FormActivity.this,
                                getIfscErrorMessage(),
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

    private String getIfscErrorMessage() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "आईएफएससी विवरण नहीं मिला। इंटरनेट जाँचकर पुनः प्रयास करें।";
        }

        return "IFSC details were not found. Check internet and try again.";
    }

    private void saveDraft() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_35_draft"
        );

        preferences.edit()
                .putString("crime_number", getText(etCrimeNumber))
                .putString("crime_date", getText(etCrimeDate))
                .putString("sections", getText(etSections))
                .putString("recipient_name", getText(etRecipientName))
                .putString(
                        "father_spouse_name",
                        getText(etFatherSpouseName)
                )
                .putString(
                        "recipient_address",
                        getText(etRecipientAddress)
                )
                .putString(
                        "recipient_mobile",
                        getText(etRecipientMobile)
                )
                .putString(
                        "transaction_amount",
                        getText(etTransactionAmount)
                )
                .putString(
                        "transaction_date",
                        getText(etTransactionDate)
                )
                .putString("ifsc_code", getText(etIfscCode))
                .putString("bank_name", getText(etBankName))
                .putString("branch_name", getText(etBranchName))
                .putString(
                        "branch_address",
                        getText(etBranchAddress)
                )
                .putString(
                        "account_number",
                        getText(etAccountNumber)
                )
                .putString("utr_number", getText(etUtrNumber))
                .putString(
                        "withdrawal_date",
                        getText(etWithdrawalDate)
                )
                .putString(
                        "appearance_date",
                        getText(etAppearanceDate)
                )
                .putString(
                        "appearance_time",
                        getText(etAppearanceTime)
                )
                .putString(
                        "appearance_place",
                        getText(etAppearancePlace)
                )
                .putString(
                        "officer_name",
                        getText(etOfficerName)
                )
                .putString("rank", getText(etRank))
                .putString("mobile", getText(etMobile))
                .putString("email", getText(etEmail))
                .apply();

        saveOfficerProfileSafely();
    }

    private void loadDraft() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "section_35_draft"
        );

        etCrimeNumber.setText(
                preferences.getString("crime_number", "")
        );

        etCrimeDate.setText(
                preferences.getString("crime_date", "")
        );

        etSections.setText(
                preferences.getString("sections", "")
        );

        etRecipientName.setText(
                preferences.getString("recipient_name", "")
        );

        etFatherSpouseName.setText(
                preferences.getString("father_spouse_name", "")
        );

        etRecipientAddress.setText(
                preferences.getString("recipient_address", "")
        );

        etRecipientMobile.setText(
                preferences.getString("recipient_mobile", "")
        );

        etTransactionAmount.setText(
                preferences.getString("transaction_amount", "")
        );

        etTransactionDate.setText(
                preferences.getString("transaction_date", "")
        );

        etIfscCode.setText(
                preferences.getString("ifsc_code", "")
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

        etAccountNumber.setText(
                preferences.getString("account_number", "")
        );

        etUtrNumber.setText(
                preferences.getString("utr_number", "")
        );

        etWithdrawalDate.setText(
                preferences.getString("withdrawal_date", "")
        );

        etAppearanceDate.setText(
                preferences.getString("appearance_date", "")
        );

        etAppearanceTime.setText(
                preferences.getString("appearance_time", "")
        );

        etAppearancePlace.setText(
                preferences.getString(
                        "appearance_place",
                        getString(R.string.appearance_place_default)
                )
        );

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
}
