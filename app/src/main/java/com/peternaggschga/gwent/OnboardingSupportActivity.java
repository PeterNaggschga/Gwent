package com.peternaggschga.gwent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.peternaggschga.gwent.ui.onboarding.PlaceholderFragment;
import com.peternaggschga.gwent.ui.onboarding.SectionsPagerAdapter;

/**
 * @todo Documentation
 */
public class OnboardingSupportActivity extends AppCompatActivity {
    private IndicatorManager indicatorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_onboarding_support);

        if (indicatorManager == null) {
            indicatorManager = new IndicatorManager(getWindow());
        }

        final ViewPager2 viewPager = findViewById(R.id.onboarding_viewPager);
        OnBackPressedCallback callback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                int item = viewPager.getCurrentItem();
                if (item != 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
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

        final Button skipButton = findViewById(R.id.onboarding_button_skip);
        final ImageButton nextButton = findViewById(R.id.onboarding_button_next);
        final Button finishButton = findViewById(R.id.onboarding_button_finish);

        skipButton.setOnClickListener(onFinish);
        nextButton.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
        finishButton.setOnClickListener(onFinish);

        viewPager.setAdapter(new SectionsPagerAdapter(this));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                indicatorManager.updateIndicators(position);
                skipButton.setVisibility(position == PlaceholderFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
                nextButton.setVisibility(position == PlaceholderFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
                finishButton.setVisibility(position == PlaceholderFragment.PAGES_COUNT - 1 ? View.VISIBLE : View.GONE);
                callback.setEnabled(position != 0);
            }
        });
    }
}
