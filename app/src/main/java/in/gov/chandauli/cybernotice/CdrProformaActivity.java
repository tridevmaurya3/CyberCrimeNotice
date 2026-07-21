package in.gov.chandauli.cybernotice;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class CdrProformaActivity extends BaseActivity {

    private TextInputEditText etOfficeStation;
    private TextInputEditText etSubmittingOfficer;
    private TextInputEditText etRank;
    private TextInputEditText etMobile;
    private TextInputEditText etEmail;

    private TextInputEditText etFirNumber;
    private TextInputEditText etCaseDate;
    private TextInputEditText etSections;

    private TextInputEditText etServiceProvider;
    private TextInputEditText etTargetIdentifier;
    private TextInputEditText etRequestedInformation;

    private TextInputEditText etFromDate;
    private TextInputEditText etToDate;
    private TextInputEditText etJustification;

    private Button btnBack;
    private Button btnSaveDraft;
    private Button btnPreview;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cdr_proforma);

        bindViews();
        loadDraft();
        setupDatePickers();

        btnBack.setOnClickListener(view -> finish());

        btnSaveDraft.setOnClickListener(view -> {
            saveDraft();

            Toast.makeText(
                    CdrProformaActivity.this,
                    getString(R.string.cdr_draft_saved),
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnPreview.setOnClickListener(view -> {
            saveDraft();

            Intent intent = new Intent(
                    CdrProformaActivity.this,
                    CdrPreviewActivity.class
            );

            startActivity(intent);
        });
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnPreview = findViewById(R.id.btnPreview);

        etOfficeStation = findViewById(R.id.etOfficeStation);
        etSubmittingOfficer = findViewById(R.id.etSubmittingOfficer);
        etRank = findViewById(R.id.etRank);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);

        etFirNumber = findViewById(R.id.etFirNumber);
        etCaseDate = findViewById(R.id.etCaseDate);
        etSections = findViewById(R.id.etSections);

        etServiceProvider = findViewById(R.id.etServiceProvider);
        etTargetIdentifier = findViewById(R.id.etTargetIdentifier);
        etRequestedInformation = findViewById(R.id.etRequestedInformation);

        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        etJustification = findViewById(R.id.etJustification);
    }

    private void setupDatePickers() {
        setupDatePicker(etCaseDate);
        setupDatePicker(etFromDate);
        setupDatePicker(etToDate);
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
            // खाली या गलत date होने पर आज की तारीख खुलेगी।
        }

        return calendar;
    }

    private void saveDraft() {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "cdr_proforma_draft"
        );

        preferences.edit()
                .putString(
                        "office_station",
                        getOfficialOfficeTitle()
                )
                .putString(
                        "submitting_officer",
                        getText(etSubmittingOfficer)
                )
                .putString("rank", getText(etRank))
                .putString("mobile", getText(etMobile))
                .putString("email", getText(etEmail))
                .putString("fir_number", getText(etFirNumber))
                .putString("case_date", getText(etCaseDate))
                .putString("sections", getText(etSections))
                .putString(
                        "service_provider",
                        getText(etServiceProvider)
                )
                .putString(
                        "target_identifier",
                        getText(etTargetIdentifier)
                )
                .putString(
                        "requested_information",
                        getText(etRequestedInformation)
                )
                .putString("from_date", getText(etFromDate))
                .putString("to_date", getText(etToDate))
                .putString(
                        "justification",
                        getText(etJustification)
                )
                .apply();

        saveOfficerProfileSafely();
    }

    private void loadDraft() {
        etOfficeStation.setText(getOfficialOfficeTitle());
        etOfficeStation.setFocusable(false);
        etOfficeStation.setClickable(false);
        etOfficeStation.setCursorVisible(false);

        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                this,
                "cdr_proforma_draft"
        );

        OfficerProfileManager.OfficerProfile profile =
                OfficerProfileManager.getProfile(this);

        setOfficerField(
                etSubmittingOfficer,
                preferences.getString("submitting_officer", ""),
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

        etFirNumber.setText(
                preferences.getString("fir_number", "")
        );

        etCaseDate.setText(
                preferences.getString("case_date", "")
        );

        etSections.setText(
                preferences.getString("sections", "")
        );

        etServiceProvider.setText(
                preferences.getString("service_provider", "")
        );

        etTargetIdentifier.setText(
                preferences.getString("target_identifier", "")
        );

        etRequestedInformation.setText(
                preferences.getString(
                        "requested_information",
                        ""
                )
        );

        etFromDate.setText(
                preferences.getString("from_date", "")
        );

        etToDate.setText(
                preferences.getString("to_date", "")
        );

        etJustification.setText(
                preferences.getString("justification", "")
        );
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

    private void saveOfficerProfileSafely() {
        OfficerProfileManager.OfficerProfile existingProfile =
                OfficerProfileManager.getProfile(this);

        String officerName = firstAvailable(
                getText(etSubmittingOfficer),
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
