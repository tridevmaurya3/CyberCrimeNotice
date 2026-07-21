package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LanguageSelectionActivity extends BaseActivity {

    private Button btnHindi;
    private Button btnEnglish;
    private String username;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        btnHindi = findViewById(R.id.btnHindi);
        btnEnglish = findViewById(R.id.btnEnglish);

        username = getIntent().getStringExtra("USERNAME");

        btnHindi.setOnClickListener(view -> selectLanguage("hi"));
        btnEnglish.setOnClickListener(view -> selectLanguage("en"));
    }

    private void selectLanguage(String languageCode) {
        LanguageManager.saveLanguage(this, languageCode);

        Intent intent = new Intent(
                LanguageSelectionActivity.this,
                DashboardActivity.class
        );

        intent.putExtra("USERNAME", username);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK
        );

        startActivity(intent);
        finish();
    }
}