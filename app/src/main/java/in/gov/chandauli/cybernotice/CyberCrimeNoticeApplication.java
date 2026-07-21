package in.gov.chandauli.cybernotice;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class CyberCrimeNoticeApplication extends Application {

    private static final String HOME_BUTTON_TAG =
            "common_floating_home_button";

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(
                            @NonNull Activity activity,
                            @Nullable Bundle savedInstanceState
                    ) {
                        applySystemBarSpacing(activity);
                    }

                    @Override
                    public void onActivityStarted(
                            @NonNull Activity activity
                    ) {
                    }

                    @Override
                    public void onActivityResumed(
                            @NonNull Activity activity
                    ) {
                        addHomeButtonIfNeeded(activity);
                    }

                    @Override
                    public void onActivityPaused(
                            @NonNull Activity activity
                    ) {
                    }

                    @Override
                    public void onActivityStopped(
                            @NonNull Activity activity
                    ) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(
                            @NonNull Activity activity,
                            @NonNull Bundle outState
                    ) {
                    }

                    @Override
                    public void onActivityDestroyed(
                            @NonNull Activity activity
                    ) {
                    }
                }
        );
    }

    private void applySystemBarSpacing(Activity activity) {
        Window window = activity.getWindow();

        WindowCompat.setDecorFitsSystemWindows(window, false);

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        window,
                        window.getDecorView()
                );

        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        View contentView = activity.findViewById(
                android.R.id.content
        );

        if (contentView == null) {
            return;
        }

        final int initialLeftPadding =
                contentView.getPaddingLeft();

        final int initialTopPadding =
                contentView.getPaddingTop();

        final int initialRightPadding =
                contentView.getPaddingRight();

        final int initialBottomPadding =
                contentView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(
                contentView,
                (view, windowInsets) -> {
                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );

                    view.setPadding(
                            initialLeftPadding + systemBars.left,
                            initialTopPadding + systemBars.top,
                            initialRightPadding + systemBars.right,
                            initialBottomPadding + systemBars.bottom
                    );

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(contentView);
    }

    private void addHomeButtonIfNeeded(Activity activity) {
        if (!shouldShowHomeButton(activity)) {
            return;
        }

        View contentView = activity.findViewById(
                android.R.id.content
        );

        if (!(contentView instanceof FrameLayout)) {
            return;
        }

        FrameLayout rootLayout = (FrameLayout) contentView;

        View alreadyAdded = rootLayout.findViewWithTag(
                HOME_BUTTON_TAG
        );

        if (alreadyAdded != null) {
            return;
        }

        TextView homeButton = new TextView(activity);

        homeButton.setTag(HOME_BUTTON_TAG);
        homeButton.setText("⌂");
        homeButton.setTextColor(Color.WHITE);
        homeButton.setTextSize(28);
        homeButton.setGravity(Gravity.CENTER);
        homeButton.setContentDescription(
                isHindi(activity) ? "होम" : "Home"
        );

        homeButton.setClickable(true);
        homeButton.setFocusable(true);
        homeButton.setElevation(dp(activity, 8));

        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(Color.parseColor("#0D47A1"));
        background.setStroke(
                dp(activity, 2),
                Color.WHITE
        );

        homeButton.setBackground(background);

        int buttonSize = dp(activity, 58);

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        buttonSize,
                        buttonSize
                );

        params.gravity = Gravity.END | Gravity.BOTTOM;
        params.setMargins(
                dp(activity, 16),
                dp(activity, 16),
                dp(activity, 20),
                dp(activity, 20)
        );

        rootLayout.addView(homeButton, params);

        homeButton.setOnClickListener(view -> openDashboard(activity));
    }

    private boolean shouldShowHomeButton(Activity activity) {
        /*
         * Login और language selection से Dashboard पर जाना सही flow नहीं है।
         * बाकी सभी functional pages पर Home button दिखेगा।
         */
        return !(activity instanceof DashboardActivity)
                && !(activity instanceof MainActivity)
                && !(activity instanceof LanguageSelectionActivity);
    }

    private void openDashboard(Activity activity) {
        Intent intent = new Intent(
                activity,
                DashboardActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        activity.startActivity(intent);
    }

    private boolean isHindi(Activity activity) {
        return "hi".equals(
                LanguageManager.getLanguage(activity)
        );
    }

    private int dp(Activity activity, int value) {
        float density =
                activity.getResources()
                        .getDisplayMetrics()
                        .density;

        return (int) (value * density + 0.5f);
    }
}