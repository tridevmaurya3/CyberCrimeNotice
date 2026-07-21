package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class OfficerProfileActivity extends BaseActivity {

    private TextInputEditText etOfficerName;
    private TextInputEditText etOfficerRank;
    private TextInputEditText etOfficerMobile;
    private TextInputEditText etOfficerEmail;
    private AutoCompleteTextView actDistrict;
    private AutoCompleteTextView actPoliceStation;

    private Button btnBack;
    private Button btnSaveProfile;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_profile);

        bindViews();
        setupLocationSelectors();
        loadProfile();

        btnBack.setOnClickListener(view -> finish());

        btnSaveProfile.setOnClickListener(view -> saveProfile());
    }

    private void bindViews() {
        etOfficerName = findViewById(R.id.etOfficerName);
        etOfficerRank = findViewById(R.id.etOfficerRank);
        etOfficerMobile = findViewById(R.id.etOfficerMobile);
        etOfficerEmail = findViewById(R.id.etOfficerEmail);
        actDistrict = findViewById(R.id.actDistrict);
        actPoliceStation = findViewById(R.id.actPoliceStation);

        btnBack = findViewById(R.id.btnBack);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void setupLocationSelectors() {
        List<String> districts = UpPoliceDirectory.getDistricts(this);
        actDistrict.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                districts
        ));

        actDistrict.setOnItemClickListener((parent, view, position, id) ->
                updateStationOptions(
                        String.valueOf(parent.getItemAtPosition(position)),
                        ""
                )
        );
    }

    private void loadProfile() {
        OfficerProfileManager.OfficerProfile profile =
                OfficerProfileManager.getProfile(this);

        etOfficerName.setText(profile.getName());
        etOfficerRank.setText(profile.getRank());
        etOfficerMobile.setText(profile.getMobile());
        etOfficerEmail.setText(profile.getEmail());
        actDistrict.setText(profile.getDistrict(), false);
        updateStationOptions(
                profile.getDistrict(),
                profile.getPoliceStation()
        );
    }

    private void updateStationOptions(
            String district,
            String selectedStation
    ) {
        List<String> stations = UpPoliceDirectory.getStations(this, district);
        actPoliceStation.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                stations
        ));
        actPoliceStation.setEnabled(!stations.isEmpty());
        actPoliceStation.setText(
                stations.contains(selectedStation) ? selectedStation : "",
                false
        );
    }

    private void saveProfile() {
        String district = getText(actDistrict);
        String policeStation = getText(actPoliceStation);

        if (district.isEmpty()
                || !UpPoliceDirectory.getDistricts(this).contains(district)) {
            actDistrict.setError(getString(R.string.officer_profile_district_required));
            actDistrict.requestFocus();
            return;
        }

        if (policeStation.isEmpty()
                || !UpPoliceDirectory.getStations(this, district)
                .contains(policeStation)) {
            actPoliceStation.setError(
                    getString(R.string.officer_profile_station_required)
            );
            actPoliceStation.requestFocus();
            return;
        }

        OfficerProfileManager.saveProfile(
                this,
                getText(etOfficerName),
                getText(etOfficerRank),
                getText(etOfficerMobile),
                getText(etOfficerEmail),
                district,
                policeStation
        );

        Toast.makeText(
                this,
                getProfileSavedMessage(),
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private String getProfileSavedMessage() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            return "अधिकारी प्रोफाइल सुरक्षित हो गई।";
        }

        return "Officer profile saved.";
    }

    private String getText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private String getText(AutoCompleteTextView textView) {
        if (textView == null || textView.getText() == null) {
            return "";
        }

        return textView.getText().toString().trim();
    }
}
