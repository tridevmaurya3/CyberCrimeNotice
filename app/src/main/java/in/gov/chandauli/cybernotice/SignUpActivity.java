package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnCreateAccount;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etSignUpName);
        etEmail = findViewById(R.id.etSignUpEmail);
        etPassword = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(R.id.etSignUpConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(view -> createAccount());
        findViewById(R.id.btnBackToLogin).setOnClickListener(view -> finish());
    }

    private void createAccount() {
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (name.isEmpty()) {
            etName.setError(getString(R.string.sign_up_name_required));
            etName.requestFocus();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.sign_up_email_required));
            etEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(getString(R.string.sign_up_password_short));
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(
                    getString(R.string.sign_up_password_mismatch)
            );
            etConfirmPassword.requestFocus();
            return;
        }

        etName.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);
        setLoading(true);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        showCreateFailure();
                        return;
                    }
                    createUserProfile(user, name, email);
                })
                .addOnFailureListener(error -> showCreateFailure());
    }

    private void createUserProfile(FirebaseUser user, String name, String email) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("displayName", name);
        profile.put("email", email);
        profile.put("role", "OFFICER");
        profile.put("approved", true);
        profile.put("status", "ACTIVE");
        profile.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection("users")
                .document(user.getUid())
                .set(profile)
                .addOnSuccessListener(unused -> openDashboard(name))
                .addOnFailureListener(error -> {
                    user.delete();
                    firebaseAuth.signOut();
                    setLoading(false);
                    Toast.makeText(
                            this,
                            R.string.sign_up_profile_failed,
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void showCreateFailure() {
        firebaseAuth.signOut();
        setLoading(false);
        Toast.makeText(this, R.string.sign_up_failed, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        btnCreateAccount.setEnabled(!loading);
        btnCreateAccount.setText(
                loading ? R.string.sign_up_creating : R.string.sign_up_create_account
        );
    }

    private void openDashboard(String name) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("USERNAME", name);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }
}
