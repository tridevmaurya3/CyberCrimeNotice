package in.gov.chandauli.cybernotice;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public final class IfscLookupHelper {

    private static final String LOOKUP_URL =
            "https://ifsc.razorpay.com/";

    private IfscLookupHelper() {
    }

    public interface Callback {
        void onSuccess(BranchDetails branchDetails);

        void onError(String message);
    }

    public static class BranchDetails {

        private final String ifscCode;
        private final String bankName;
        private final String branchName;
        private final String address;

        BranchDetails(
                String ifscCode,
                String bankName,
                String branchName,
                String address
        ) {
            this.ifscCode = ifscCode;
            this.bankName = bankName;
            this.branchName = branchName;
            this.address = address;
        }

        public String getIfscCode() {
            return ifscCode;
        }

        public String getBankName() {
            return bankName;
        }

        public String getBranchName() {
            return branchName;
        }

        public String getAddress() {
            return address;
        }
    }

    public static void lookup(
            String rawIfscCode,
            Callback callback
    ) {
        String ifscCode = rawIfscCode == null
                ? ""
                : rawIfscCode.trim().toUpperCase(Locale.ROOT);

        if (!ifscCode.matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
            postError(
                    callback,
                    "Enter a valid 11-character IFSC code."
            );
            return;
        }

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                String encodedIfsc = URLEncoder.encode(
                        ifscCode,
                        "UTF-8"
                );

                URL url = new URL(LOOKUP_URL + encodedIfsc);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    postError(
                            callback,
                            "Branch details were not found for this IFSC."
                    );
                    return;
                }

                java.io.BufferedReader reader =
                        new java.io.BufferedReader(
                                new java.io.InputStreamReader(
                                        connection.getInputStream()
                                )
                        );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject jsonObject =
                        new JSONObject(response.toString());

                String bankName = jsonObject.optString(
                        "BANK",
                        ""
                );

                String branchName = jsonObject.optString(
                        "BRANCH",
                        ""
                );

                String address = jsonObject.optString(
                        "ADDRESS",
                        ""
                );

                if (bankName.isEmpty()
                        || branchName.isEmpty()
                        || address.isEmpty()) {
                    postError(
                            callback,
                            "Incomplete branch data received. Please enter details manually."
                    );
                    return;
                }

                BranchDetails branchDetails = new BranchDetails(
                        ifscCode,
                        bankName,
                        branchName,
                        address
                );

                postSuccess(callback, branchDetails);

            } catch (Exception exception) {
                postError(
                        callback,
                        "Unable to look up IFSC. Check internet and try again."
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private static void postSuccess(
            Callback callback,
            BranchDetails branchDetails
    ) {
        new Handler(Looper.getMainLooper()).post(
                () -> callback.onSuccess(branchDetails)
        );
    }

    private static void postError(
            Callback callback,
            String message
    ) {
        new Handler(Looper.getMainLooper()).post(
                () -> callback.onError(message)
        );
    }
}