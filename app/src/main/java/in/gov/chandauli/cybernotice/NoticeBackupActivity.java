package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoticeBackupActivity extends BaseActivity {

    private static final int REQUEST_CREATE_BACKUP = 701;
    private static final int REQUEST_SELECT_BACKUP = 702;

    private TextView tvTitle;
    private TextView tvBackupHeading;
    private TextView tvBackupDescription;
    private TextView tvRestoreHeading;
    private TextView tvRestoreDescription;
    private TextView tvSecurityNote;

    private Button btnBack;
    private Button btnCreateBackup;
    private Button btnRestoreBackup;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(
                LanguageManager.applyLanguage(newBase)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_backup);

        bindViews();
        setupText();
        setupListeners();
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvBackupHeading = findViewById(R.id.tvBackupHeading);
        tvBackupDescription = findViewById(
                R.id.tvBackupDescription
        );

        tvRestoreHeading = findViewById(R.id.tvRestoreHeading);
        tvRestoreDescription = findViewById(
                R.id.tvRestoreDescription
        );

        tvSecurityNote = findViewById(R.id.tvSecurityNote);

        btnBack = findViewById(R.id.btnBack);
        btnCreateBackup = findViewById(R.id.btnCreateBackup);
        btnRestoreBackup = findViewById(R.id.btnRestoreBackup);
    }

    private void setupText() {
        if (isHindi()) {
            tvTitle.setText("बैकअप और रिस्टोर");

            tvBackupHeading.setText("बैकअप बनाएं");

            tvBackupDescription.setText(
                    "सभी रजिस्टर किए गए नोटिस को एक backup file में सुरक्षित करें।"
            );

            btnCreateBackup.setText("बैकअप फाइल बनाएं");

            tvRestoreHeading.setText("बैकअप रिस्टोर करें");

            tvRestoreDescription.setText(
                    "पुष्टि के बाद restore करने पर वर्तमान Notice Register को backup file के data से बदल दिया जाएगा।"
            );

            btnRestoreBackup.setText("बैकअप फाइल चुनें");

            tvSecurityNote.setText(
                    "बैकअप फाइल में संवेदनशील notice जानकारी हो सकती है। इसे केवल सुरक्षित सरकारी स्थान पर रखें।"
            );

            return;
        }

        tvTitle.setText("Backup & Restore");

        tvBackupHeading.setText("Create Backup");

        tvBackupDescription.setText(
                "Save all registered notices in one backup file."
        );

        btnCreateBackup.setText("CREATE BACKUP FILE");

        tvRestoreHeading.setText("Restore Backup");

        tvRestoreDescription.setText(
                "Restore replaces the current Notice Register after confirmation."
        );

        btnRestoreBackup.setText("SELECT BACKUP FILE");

        tvSecurityNote.setText(
                "Keep backup files in a secure official location. They may contain sensitive notice information."
        );
    }

    private void setupListeners() {
        btnBack.setOnClickListener(view -> finish());

        btnCreateBackup.setOnClickListener(
                view -> chooseBackupLocation()
        );

        btnRestoreBackup.setOnClickListener(
                view -> chooseBackupFile()
        );
    }

    private void chooseBackupLocation() {
        Intent intent = new Intent(
                Intent.ACTION_CREATE_DOCUMENT
        );

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(
                Intent.EXTRA_TITLE,
                getBackupFileName()
        );

        startActivityForResult(intent, REQUEST_CREATE_BACKUP);
    }

    private void chooseBackupFile() {
        Intent intent = new Intent(
                Intent.ACTION_OPEN_DOCUMENT
        );

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_SELECT_BACKUP);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }

        Uri selectedUri = data.getData();

        if (requestCode == REQUEST_CREATE_BACKUP) {
            writeBackupFile(selectedUri);
            return;
        }

        if (requestCode == REQUEST_SELECT_BACKUP) {
            readBackupFile(selectedUri);
        }
    }

    private void writeBackupFile(Uri uri) {
        try {
            JSONObject backupRoot = new JSONObject();

            backupRoot.put(
                    "app_name",
                    "Cyber Crime Notice Management System"
            );

            backupRoot.put("format_version", 1);

            backupRoot.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            backupRoot.put(
                    "notice_register",
                    NoticeStore.getNoticesForBackup(this)
            );

            OutputStream outputStream =
                    getContentResolver().openOutputStream(uri);

            if (outputStream == null) {
                showMessage(
                        isHindi()
                                ? "बैकअप फाइल नहीं बनाई जा सकी।"
                                : "Unable to create backup file."
                );

                return;
            }

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            outputStream,
                            StandardCharsets.UTF_8
                    )
            );

            writer.write(backupRoot.toString(2));
            writer.flush();
            writer.close();

            int noticeCount =
                    NoticeStore.getAllNotices(this).size();

            showMessage(
                    isHindi()
                            ? noticeCount
                              + " नोटिस का बैकअप सुरक्षित कर दिया गया है।"
                            : "Backup created for "
                              + noticeCount
                              + " notice(s)."
            );

        } catch (Exception ignored) {
            showMessage(
                    isHindi()
                            ? "बैकअप फाइल नहीं बनाई जा सकी।"
                            : "Unable to create backup file."
            );
        }
    }

    private void readBackupFile(Uri uri) {
        try {
            InputStream inputStream =
                    getContentResolver().openInputStream(uri);

            if (inputStream == null) {
                showMessage(
                        isHindi()
                                ? "चुनी गई backup file पढ़ी नहीं जा सकी।"
                                : "The selected backup file could not be read."
                );

                return;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            inputStream,
                            StandardCharsets.UTF_8
                    )
            );

            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            reader.close();

            JSONObject backupRoot = new JSONObject(
                    content.toString()
            );

            int version = backupRoot.optInt(
                    "format_version",
                    0
            );

            JSONArray backupNotices =
                    backupRoot.optJSONArray(
                            "notice_register"
                    );

            if (version != 1 || backupNotices == null) {
                showMessage(
                        isHindi()
                                ? "यह मान्य backup file नहीं है।"
                                : "This is not a valid backup file."
                );

                return;
            }

            confirmRestore(backupNotices);

        } catch (Exception ignored) {
            showMessage(
                    isHindi()
                            ? "चुनी गई file मान्य backup file नहीं है।"
                            : "The selected file is not a valid backup file."
            );
        }
    }

    private void confirmRestore(JSONArray backupNotices) {
        int backupCount = backupNotices.length();
        int currentCount =
                NoticeStore.getAllNotices(this).size();

        String message;

        if (isHindi()) {
            message = "इस backup में "
                    + backupCount
                    + " नोटिस हैं।\n\n"
                    + "वर्तमान Register के "
                    + currentCount
                    + " नोटिस इस backup के data से बदल दिए जाएंगे।";
        } else {
            message = "This backup contains "
                    + backupCount
                    + " notice(s).\n\n"
                    + "The current "
                    + currentCount
                    + " notice(s) in the Register will be replaced by this backup.";
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        isHindi()
                                ? "बैकअप रिस्टोर करें?"
                                : "Restore Backup?"
                )
                .setMessage(message)
                .setNegativeButton(
                        isHindi() ? "रद्द करें" : "CANCEL",
                        null
                )
                .setPositiveButton(
                        isHindi()
                                ? "रिस्टोर करें"
                                : "RESTORE",
                        (dialog, which) -> restoreBackup(
                                backupNotices
                        )
                )
                .show();
    }

    private void restoreBackup(JSONArray backupNotices) {
        boolean restored =
                NoticeStore.restoreNoticesFromBackup(
                        this,
                        backupNotices
                );

        if (restored) {
            showMessage(
                    isHindi()
                            ? "बैकअप सफलतापूर्वक रिस्टोर हो गया है।"
                            : "Backup restored successfully."
            );
        } else {
            showMessage(
                    isHindi()
                            ? "बैकअप रिस्टोर नहीं हो सका।"
                            : "Unable to restore this backup."
            );
        }
    }

    private String getBackupFileName() {
        String dateText = new SimpleDateFormat(
                "yyyyMMdd_HHmm",
                Locale.ENGLISH
        ).format(new Date());

        return "CyberCrimeNoticeBackup_"
                + dateText
                + ".json";
    }

    private boolean isHindi() {
        return "hi".equals(
                LanguageManager.getLanguage(this)
        );
    }

    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}