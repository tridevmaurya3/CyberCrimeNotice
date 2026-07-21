package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

/** Entry point for officers to sign in, create an account, or reset a password. */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnForgotPassword;
    private MaterialButton btnSignUp;
    private MaterialCheckBox cbRememberMe;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        cbRememberMe.setChecked(RememberMeManager.isRemembered(this));
        etEmail.setText(RememberMeManager.getRememberedEmail(this));

        btnLogin.setOnClickListener(view -> signIn());
        btnForgotPassword.setOnClickListener(view -> showForgotPasswordDialog());
        btnSignUp.setOnClickListener(view ->
                startActivity(new Intent(this, SignUpActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth == null
                ? null
                : firebaseAuth.getCurrentUser();

        if (currentUser != null
                && RememberMeManager.canRestoreSession(
                        this,
                        currentUser.getUid()
                )) {
            verifyApprovalAndOpenDashboard(currentUser, false);
        } else if (currentUser != null) {
            firebaseAuth.signOut();
        }
    }

    private void signIn() {
        String email = getText(etEmail);
        String password = getText(etPassword);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.login_email_required));
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.login_password_required));
            etPassword.requestFocus();
            return;
        }

        etEmail.setError(null);
        etPassword.setError(null);
        setLoading(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        showSignInFailure();
                        return;
                    }
                    verifyApprovalAndOpenDashboard(user, true);
                })
                .addOnFailureListener(error -> showSignInFailure());
    }

    private void verifyApprovalAndOpenDashboard(
            @NonNull FirebaseUser user,
            boolean saveRememberChoice
    ) {
        setLoading(true);

        firestore.collection("users")
                .document(user.getUid())
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    if (isApprovedAndActive(snapshot)) {
                        if (saveRememberChoice) {
                            RememberMeManager.saveApprovedSignIn(
                                    this,
                                    user.getUid(),
                                    user.getEmail(),
                                    cbRememberMe != null
                                            && cbRememberMe.isChecked()
                            );
                        }
                        String displayName = snapshot.getString("displayName");
                        if (displayName == null || displayName.trim().isEmpty()) {
                            displayName = user.getEmail();
                        }
                        openDashboard(displayName);
                        return;
                    }

                    firebaseAuth.signOut();
                    setLoading(false);
                    Toast.makeText(
                            this,
                            R.string.login_access_denied,
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(error -> {
                    firebaseAuth.signOut();
                    setLoading(false);
                    Toast.makeText(
                            this,
                            R.string.login_required,
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private boolean isApprovedAndActive(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return false;
        }

        Boolean approved = snapshot.getBoolean("approved");
        String status = snapshot.getString("status");

        return Boolean.TRUE.equals(approved)
                && "ACTIVE".equalsIgnoreCase(status == null ? "" : status);
    }

    private void showSignInFailure() {
        firebaseAuth.signOut();
        setLoading(false);
        Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
    }

    private void showForgotPasswordDialog() {
        TextInputLayout emailLayout = new TextInputLayout(this);
        emailLayout.setHint(R.string.login_email);
        emailLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        TextInputEditText emailInput = new TextInputEditText(emailLayout.getContext());
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setSingleLine(true);
        emailInput.setText(getText(etEmail));
        emailLayout.addView(emailInput);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.login_reset_password_title)
                .setMessage(R.string.login_reset_password_message)
                .setView(emailLayout)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.login_send_reset_link, null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener(view -> {
            String email = getText(emailInput);
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.setError(getString(R.string.login_email_required));
                emailInput.requestFocus();
                return;
            }

            emailLayout.setError(null);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(
                                this,
                                R.string.login_reset_link_sent,
                                Toast.LENGTH_LONG
                        ).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(error -> {
                        dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        ).setEnabled(true);
                        emailLayout.setError(
                                getString(R.string.login_reset_password_failed)
                        );
                    });
        }));

        dialog.show();
    }

    private void setLoading(boolean loading) {
        if (btnLogin == null) {
            return;
        }

        btnLogin.setEnabled(!loading);
        btnLogin.setText(
                loading ? R.string.login_checking_access : R.string.login_sign_in
        );
    }

    private void openDashboard(String displayName) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("USERNAME", displayName == null ? "" : displayName.trim());
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
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
