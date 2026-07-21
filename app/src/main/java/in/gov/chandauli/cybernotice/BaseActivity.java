package in.gov.chandauli.cybernotice;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        resetTimer();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetTimer(); // हर टच पर टाइमर रीसेट होगा
    }

    private void resetTimer() {
        // अगर वर्तमान स्क्रीन MainActivity (लॉगिन पेज) है, तो टाइमर न चलाएं
        SessionManager.startSessionTimer(() -> {
            // 5 मिनट बाद यह कोड चलेगा
            RememberMeManager.lockForReauthentication(this);
            UserSessionManager.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SessionManager.stopSessionTimer();
    }
}
