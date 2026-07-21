package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageManager {

    private static final String PREF_NAME = "cyber_notice_preferences";
    private static final String KEY_LANGUAGE = "selected_language";

    public static void setLanguage(Context context, String languageCode) {
        if (!"hi".equals(languageCode)) {
            languageCode = "en";
        }

        SharedPreferences preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );

        preferences.edit()
                .putString(KEY_LANGUAGE, languageCode)
                .apply();
    }

    public static void saveLanguage(Context context, String languageCode) {
        setLanguage(context, languageCode);
    }

    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );

        return preferences.getString(KEY_LANGUAGE, "en");
    }

    public static Context applyLanguage(Context context) {
        String languageCode = getLanguage(context);

        Locale locale;

        if ("hi".equals(languageCode)) {
            locale = new Locale("hi", "IN");
        } else {
            locale = Locale.ENGLISH;
        }

        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = new Configuration(
                resources.getConfiguration()
        );

        configuration.setLocale(locale);

        resources.updateConfiguration(
                configuration,
                resources.getDisplayMetrics()
        );

        return context;
    }
}