package in.gov.chandauli.cybernotice;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Offline UP district and station directory packaged with the app. The source
 * data is the UP Police public station directory dated 12 September 2023.
 */
public final class UpPoliceDirectory {

    private static final String ASSET_NAME = "up_police_stations.json";
    private static final Map<String, List<String>> STATIONS_BY_DISTRICT =
            new LinkedHashMap<>();
    private static boolean loaded;

    private UpPoliceDirectory() {
    }

    public static List<String> getDistricts(Context context) {
        ensureLoaded(context);
        return new ArrayList<>(STATIONS_BY_DISTRICT.keySet());
    }

    public static List<String> getStations(
            Context context,
            String district
    ) {
        ensureLoaded(context);

        List<String> stations = STATIONS_BY_DISTRICT.get(safeText(district));
        return stations == null
                ? new ArrayList<>()
                : new ArrayList<>(stations);
    }

    private static synchronized void ensureLoaded(Context context) {
        if (loaded || context == null) {
            return;
        }

        try (InputStream inputStream = context.getAssets().open(ASSET_NAME);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream)
             )) {
            StringBuilder json = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            JSONArray districts = new JSONObject(json.toString())
                    .getJSONArray("districts");

            for (int i = 0; i < districts.length(); i++) {
                JSONObject districtObject = districts.getJSONObject(i);
                String district = safeText(districtObject.optString("name"));
                JSONArray stationArray = districtObject.optJSONArray("stations");

                if (district.isEmpty() || stationArray == null) {
                    continue;
                }

                List<String> stations = new ArrayList<>();
                for (int j = 0; j < stationArray.length(); j++) {
                    String station = safeText(stationArray.optString(j));
                    if (!station.isEmpty()) {
                        stations.add(station);
                    }
                }

                Collections.sort(stations, String.CASE_INSENSITIVE_ORDER);
                STATIONS_BY_DISTRICT.put(district, stations);
            }
        } catch (Exception ignored) {
            // An empty dropdown is safer than allowing an unverified entry.
        }

        loaded = true;
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
