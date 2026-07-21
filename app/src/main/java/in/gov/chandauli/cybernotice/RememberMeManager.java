package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Controls whether an approved officer can reopen this specific device without
 * entering credentials again. Passwords are never written to local storage.
 */
public final class RememberMeManager {

    private static final String PREF_NAME = "remember_me";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_SESSION_LOCKED = "session_locked";

    private RememberMeManager() {
    }

    public static void saveApprovedSignIn(
            Context context,
            String userId,
            String email,
            boolean enabled
    ) {
        SharedPreferences.Editor editor = getPreferences(context).edit()
                .putBoolean(KEY_SESSION_LOCKED, false);

        if (enabled) {
            editor.putBoolean(KEY_ENABLED, true)
                    .putString(KEY_USER_ID, safeText(userId))
                    .putString(KEY_EMAIL, safeText(email));
        } else {
            editor.remove(KEY_ENABLED)
                    .remove(KEY_USER_ID)
                    .remove(KEY_EMAIL);
        }

        editor.apply();
    }

    public static boolean canRestoreSession(Context context, String userId) {
        SharedPreferences preferences = getPreferences(context);
        return !preferences.getBoolean(KEY_SESSION_LOCKED, false)
                && preferences.getBoolean(KEY_ENABLED, false)
                && safeText(userId).equals(
                        preferences.getString(KEY_USER_ID, "")
                );
    }

    public static boolean isRemembered(Context context) {
        return getPreferences(context).getBoolean(KEY_ENABLED, false);
    }

    public static String getRememberedEmail(Context context) {
        return getPreferences(context).getString(KEY_EMAIL, "");
    }

    public static void lockForReauthentication(Context context) {
        getPreferences(context).edit()
                .putBoolean(KEY_SESSION_LOCKED, true)
                .apply();
    }

    public static void clearOnLogout(Context context) {
        getPreferences(context).edit().clear().apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return UserSessionManager.getDeviceSecurePreferences(
                context,
                PREF_NAME
        );
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
