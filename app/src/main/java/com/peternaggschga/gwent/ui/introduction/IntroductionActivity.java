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
 * An {@link AppCompatActivity} that gives the user an introduction into the usage of the application.
 * Is called when the app is first started 
 * (as tracked by the {@link androidx.preference.Preference} at key {@link R.string#preference_first_use_key}.
 * @todo Introduce ViewModel for indicators and buttons.
 */
public class IntroductionActivity extends AppCompatActivity {
    /**
     * {@link IndicatorManager} used to update the progress indicators according to the currently shown page.
     * Is initialized in {@link #onCreate(Bundle)}.
     */
    private IndicatorManager indicatorManager;

    /**
     * Sets layout to {@link R.layout#activity_introduction}, initializes {@link #indicatorManager},
     * sets listeners on the buttons, and creates a new {@link OnBackPressedCallback} that switches to the
     * previous page, if it is not the first one.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_introduction);

        if (indicatorManager == null) {
            indicatorManager = new IndicatorManager(getWindow());
        }

        final ViewPager2 viewPager = findViewById(R.id.introduction_viewPager);
        OnBackPressedCallback callback = new OnBackPressedCallback(false) {
            /**
             * Called when enabled and the user clicks on the back-button.
             * Switches the {@link ViewPager2} containing the {@link IntroductionFragment}s to the last position.
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

        final Button skipButton = findViewById(R.id.introduction_button_skip);
        final ImageButton nextButton = findViewById(R.id.introduction_button_next);
        final Button finishButton = findViewById(R.id.introduction_button_finish);

        skipButton.setOnClickListener(onFinish);
        nextButton.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
        finishButton.setOnClickListener(onFinish);

        viewPager.setAdapter(new SectionsPagerAdapter(this));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            /**
             * Updates the {@link IntroductionActivity#indicatorManager}, changes button-visibility
             * and whether the callback for onBackPressed is active.
             * @see IndicatorManager#updateIndicators(int)
             * @param position Position index of the new selected page.
             */
            @Override
            public void onPageSelected(int position) {
                indicatorManager.updateIndicators(position);
                skipButton.setVisibility(position == IntroductionFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
                nextButton.setVisibility(position == IntroductionFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
                finishButton.setVisibility(position == IntroductionFragment.PAGES_COUNT - 1 ? View.VISIBLE : View.GONE);
                callback.setEnabled(position != 0);
            }
        });
    }
}
