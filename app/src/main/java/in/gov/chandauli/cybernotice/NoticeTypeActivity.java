package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class NoticeTypeActivity extends BaseActivity {

    private Button btnBack;
    private MaterialCardView cardSection94;
    private MaterialCardView cardSection35;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_type);

        btnBack = findViewById(R.id.btnBack);
        cardSection94 = findViewById(R.id.cardSection94);
        cardSection35 = findViewById(R.id.cardSection35);

        btnBack.setOnClickListener(view -> finish());

        cardSection94.setOnClickListener(view -> {
            clearDraft("section_94_draft");

            Intent intent = new Intent(
                    NoticeTypeActivity.this,
                    Section94FormActivity.class
            );

            startActivity(intent);
        });

        cardSection35.setOnClickListener(view -> {
            clearDraft("section_35_draft");

            Intent intent = new Intent(
                    NoticeTypeActivity.this,
                    Section35FormActivity.class
            );

            startActivity(intent);
        });
    }

    private void clearDraft(String preferenceName) {
        UserSessionManager.getScopedPreferences(this, preferenceName)
                .edit()
                .clear()
                .apply();
    }
}
