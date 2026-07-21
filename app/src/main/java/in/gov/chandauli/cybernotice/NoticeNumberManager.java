package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoticeNumberManager {

    private static final String PREF_NAME = "notice_number_preferences";

    public static String getNextNoticeNumber(Context context) {
        String year = new SimpleDateFormat(
                "yyyy",
                Locale.ENGLISH
        ).format(new Date());

        String counterKey = "notice_counter_" + year;

        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                context,
                PREF_NAME
        );

        int lastNumber = preferences.getInt(counterKey, 0);
        int nextNumber = lastNumber + 1;

        preferences.edit()
                .putInt(counterKey, nextNumber)
                .apply();

        return String.format(
                Locale.ENGLISH,
                "CCN/%s/%04d",
                year,
                nextNumber
        );
    }
}
