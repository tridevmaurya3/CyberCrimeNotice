package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.SharedPreferences;

public final class OfficerProfileManager {

    private static final String PREF_NAME = "officer_profile";

    private static final String KEY_NAME = "officer_name";
    private static final String KEY_RANK = "officer_rank";
    private static final String KEY_MOBILE = "officer_mobile";
    private static final String KEY_EMAIL = "officer_email";
    private static final String KEY_DISTRICT = "officer_district";
    private static final String KEY_POLICE_STATION = "officer_police_station";

    private static final String DEFAULT_DISTRICT = "CHANDAULI";
    private static final String DEFAULT_POLICE_STATION =
            "Chandauli Cyber Crime Police Station";

    private OfficerProfileManager() {
    }

    public static void saveProfile(
            Context context,
            String name,
            String rank,
            String mobile,
            String email
    ) {
        OfficerProfile existingProfile = getProfile(context);

        saveProfile(
                context,
                name,
                rank,
                mobile,
                email,
                existingProfile.getDistrict(),
                existingProfile.getPoliceStation()
        );
    }

    public static void saveProfile(
            Context context,
            String name,
            String rank,
            String mobile,
            String email,
            String district,
            String policeStation
    ) {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                context,
                PREF_NAME
        );

        preferences.edit()
                .putString(KEY_NAME, safeText(name))
                .putString(KEY_RANK, safeText(rank))
                .putString(KEY_MOBILE, safeText(mobile))
                .putString(KEY_EMAIL, safeText(email))
                .putString(KEY_DISTRICT, safeText(district))
                .putString(KEY_POLICE_STATION, safeText(policeStation))
                .apply();
    }

    public static OfficerProfile getProfile(Context context) {
        SharedPreferences preferences = UserSessionManager.getScopedPreferences(
                context,
                PREF_NAME
        );

        return new OfficerProfile(
                preferences.getString(KEY_NAME, ""),
                preferences.getString(KEY_RANK, ""),
                preferences.getString(KEY_MOBILE, ""),
                preferences.getString(KEY_EMAIL, ""),
                valueOrDefault(
                        preferences.getString(KEY_DISTRICT, ""),
                        DEFAULT_DISTRICT
                ),
                valueOrDefault(
                        preferences.getString(KEY_POLICE_STATION, ""),
                        DEFAULT_POLICE_STATION
                )
        );
    }

    public static boolean hasProfile(Context context) {
        OfficerProfile profile = getProfile(context);

        return !profile.getName().isEmpty()
                || !profile.getRank().isEmpty()
                || !profile.getMobile().isEmpty()
                || !profile.getEmail().isEmpty()
                || !profile.getDistrict().isEmpty()
                || !profile.getPoliceStation().isEmpty();
    }

    public static class OfficerProfile {

        private final String name;
        private final String rank;
        private final String mobile;
        private final String email;
        private final String district;
        private final String policeStation;

        OfficerProfile(
                String name,
                String rank,
                String mobile,
                String email,
                String district,
                String policeStation
        ) {
            this.name = safeText(name);
            this.rank = safeText(rank);
            this.mobile = safeText(mobile);
            this.email = safeText(email);
            this.district = safeText(district);
            this.policeStation = safeText(policeStation);
        }

        public String getName() {
            return name;
        }

        public String getRank() {
            return rank;
        }

        public String getMobile() {
            return mobile;
        }

        public String getEmail() {
            return email;
        }

        public String getDistrict() {
            return district;
        }

        public String getPoliceStation() {
            return policeStation;
        }
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static String valueOrDefault(String value, String defaultValue) {
        String safeValue = safeText(value);
        return safeValue.isEmpty() ? defaultValue : safeValue;
    }
}
