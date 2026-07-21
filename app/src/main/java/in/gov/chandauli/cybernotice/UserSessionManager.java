package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Gives every authenticated officer an isolated, encrypted local data store.
 * Access is intentionally denied when no Firebase user is signed in.
 */
public final class UserSessionManager {

    private UserSessionManager() {
    }

    public static boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user == null ? "" : user.getUid();
    }

    public static String getCurrentUserEmail() {
        FirebaseUser user = getCurrentUser();
        return user == null || user.getEmail() == null
                ? ""
                : user.getEmail().trim();
    }

    public static SharedPreferences getScopedPreferences(
            Context context,
            String logicalPreferenceName
    ) {
        if (context == null) {
            throw new IllegalArgumentException("Context is required.");
        }

        String userId = getCurrentUserId();
        if (userId.isEmpty()) {
            throw new IllegalStateException("An authenticated user is required.");
        }

        return openEncryptedPreferences(
                context,
                "secure_" + logicalPreferenceName + "_" + userId
        );
    }

    /**
     * Stores device-only sign-in preferences without ever storing a password.
     * This preference is intentionally not tied to one UID because it is read
     * before a user has signed in.
     */
    public static SharedPreferences getDeviceSecurePreferences(
            Context context,
            String logicalPreferenceName
    ) {
        if (context == null) {
            throw new IllegalArgumentException("Context is required.");
        }

        return openEncryptedPreferences(
                context,
                "secure_device_" + logicalPreferenceName
        );
    }

    private static SharedPreferences openEncryptedPreferences(
            Context context,
            String preferenceName
    ) {
        try {
            Context appContext = context.getApplicationContext();
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    appContext,
                    preferenceName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to open the protected local data store.",
                    exception
            );
        }
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
