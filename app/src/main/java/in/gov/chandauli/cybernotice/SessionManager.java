package in.gov.chandauli.cybernotice;

import android.os.Handler;
import android.os.Looper;

public class SessionManager {
    private static final long TIMEOUT = 5 * 60 * 1000; // 5 मिनट
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable logoutRunnable;

    public static void startSessionTimer(Runnable logoutAction) {
        stopSessionTimer();
        logoutRunnable = logoutAction;
        handler.postDelayed(logoutRunnable, TIMEOUT);
    }

    public static void stopSessionTimer() {
        if (logoutRunnable != null) {
            handler.removeCallbacks(logoutRunnable);
        }
    }
}