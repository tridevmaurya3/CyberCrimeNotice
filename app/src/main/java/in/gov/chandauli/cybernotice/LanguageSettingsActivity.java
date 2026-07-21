package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LanguageSettingsActivity extends BaseActivity {

    private Button btnBack;
    private Button btnHindi;
    private Button btnEnglish;
    private Button btnOfficerProfile;
    private Button btnBackupRestore;

    private TextView tvBackupRestoreTitle;
    private TextView tvBackupRestoreSubtitle;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(
                LanguageManager.applyLanguage(newBase)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        bindViews();
        setupBackupText();
        setupListeners();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHindi = findViewById(R.id.btnHindi);
        btnEnglish = findViewById(R.id.btnEnglish);

        btnOfficerProfile = findViewById(
                R.id.btnOfficerProfile
        );

        btnBackupRestore = findViewById(
                R.id.btnBackupRestore
        );

        tvBackupRestoreTitle = findViewById(
                R.id.tvBackupRestoreTitle
        );

        tvBackupRestoreSubtitle = findViewById(
                R.id.tvBackupRestoreSubtitle
        );
    }

    private void setupBackupText() {
        if ("hi".equals(LanguageManager.getLanguage(this))) {
            tvBackupRestoreTitle.setText(
                    "बैकअप और रिस्टोर"
            );

            tvBackupRestoreSubtitle.setText(
                    "Notice Register का सुरक्षित बैकअप बनाएं या पुराना बैकअप रिस्टोर करें।"
            );

            btnBackupRestore.setText("खोलें");

            return;
        }

        tvBackupRestoreTitle.setText(
                "Backup & Restore"
        );

        tvBackupRestoreSubtitle.setText(
                "Create or restore a secure Notice Register backup."
        );

        btnBackupRestore.setText("OPEN");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(view -> finish());

        btnHindi.setOnClickListener(
                view -> changeLanguage("hi")
        );

        btnEnglish.setOnClickListener(
                view -> changeLanguage("en")
        );

        btnOfficerProfile.setOnClickListener(view -> {
            Intent intent = new Intent(
                    LanguageSettingsActivity.this,
                    OfficerProfileActivity.class
            );

            startActivity(intent);
        });

        btnBackupRestore.setOnClickListener(view -> {
            Intent intent = new Intent(
                    LanguageSettingsActivity.this,
                    NoticeBackupActivity.class
            );

            startActivity(intent);
        });
    }

    private void changeLanguage(String languageCode) {
        LanguageManager.setLanguage(this, languageCode);

        Intent intent = new Intent(
                LanguageSettingsActivity.this,
                DashboardActivity.class
        );

        intent.putExtra(
                "USERNAME",
                getIntent().getStringExtra("USERNAME")
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK
        );

        startActivity(intent);
        finish();
    }
}