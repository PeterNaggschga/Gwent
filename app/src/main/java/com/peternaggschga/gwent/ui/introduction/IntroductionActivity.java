package com.peternaggschga.gwent.ui.introduction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.peternaggschga.gwent.R;

/**
 * An AppCompatActivity that gives the user an introduction into the usage of the application.
 * Is called when the app is first started 
 * (as tracked by the androidx.preference.Preference at key R.stringpreference_first_use_key.
 */
public class IntroductionActivity extends AppCompatActivity {
    /**
     * IndicatorManager used to update the progress indicators according to the currently shown page.
     * Is initialized in onCreate(Bundle).
     */
    private IndicatorManager indicatorManager;

    /**
     * The ViewPager2 used to display the introduction-fragments.
     */
    private ViewPager2 viewPager;

    /**
     * The OnBackPressedCallback handling presses of the back-button.
     */
    private OnBackPressedCallback callback;

    /**
     * The skip button.
     */
    private Button skipButton;

    /**
     * The next button.
     */
    private ImageButton nextButton;

    /**
     * The finish button.
     */
    private Button finishButton;

    /**
     * Sets layout to R.layout.activity_introduction, initializes all attributes
     * (viewPager, callback, skipButton, nextButton, finishButton, indicatorManager),
     * sets listeners on the buttons, and creates a
     * new OnBackPressedCallback that switches to the previous page, if it is not the first one.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_introduction);

        if (indicatorManager == null) {
            indicatorManager = new IndicatorManager(getWindow());
        }

        viewPager = findViewById(R.id.introduction_viewPager);
        callback = new OnBackPressedCallback(false) {
            /**
             * Called when enabled and the user clicks on the back-button.
             * Switches the ViewPager2 containing the IntroductionFragments to the last position.
             */
            @Override
            public void handleOnBackPressed() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        };
        getOnBackPressedDispatcher().addCallback(callback);

        View.OnClickListener onFinish = view -> {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putBoolean(getString(R.string.preference_first_use_key), false)
                    .apply();
            callback.setEnabled(false);
            getOnBackPressedDispatcher().onBackPressed();
        };

        skipButton = findViewById(R.id.introduction_button_skip);
        nextButton = findViewById(R.id.introduction_button_next);
        finishButton = findViewById(R.id.introduction_button_finish);

        skipButton.setOnClickListener(onFinish);
        nextButton.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
        finishButton.setOnClickListener(onFinish);

        viewPager.setAdapter(new SectionsPagerAdapter(this));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            // Keep the bottom bar in sync with the selected page.
            @Override
            public void onPageSelected(int position) {
                updateBottomBar(position);
            }
        });
    }

    /**
     * Refreshes the bottom bar for the currently selected page when the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateBottomBar(viewPager.getCurrentItem());
    }

    /**
     * Updates the progress indicators, button visibility, and back-button callback
     * for the selected introduction page.
     *
     * @param position Position index of the selected page.
     * @see IndicatorManager.updateIndicators(int)
     */
    private void updateBottomBar(int position) {
        indicatorManager.updateIndicators(position);
        skipButton.setVisibility(position == IntroductionFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
        nextButton.setVisibility(position == IntroductionFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
        finishButton.setVisibility(position == IntroductionFragment.PAGES_COUNT - 1 ? View.VISIBLE : View.GONE);
        callback.setEnabled(position != 0);
    }
}
