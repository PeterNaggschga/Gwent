package com.peternaggschga.gwent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.peternaggschga.gwent.ui.onboarding.PlaceholderFragment;
import com.peternaggschga.gwent.ui.onboarding.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class OnboardingSupportActivity extends AppCompatActivity {

    private OnBackPressedCallback callback;
    private final List<ImageView> indicators = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_onboarding_support);

        final Button skipButton = findViewById(R.id.onboarding_button_skip);
        final ImageButton nextButton = findViewById(R.id.onboarding_button_next);
        final Button finishButton = findViewById(R.id.onboarding_button_finish);
        indicators.add(findViewById(R.id.onboarding_indicator_0));
        indicators.add(findViewById(R.id.onboarding_indicator_1));
        indicators.add(findViewById(R.id.onboarding_indicator_2));
        indicators.add(findViewById(R.id.onboarding_indicator_3));
        indicators.add(findViewById(R.id.onboarding_indicator_4));
        View.OnClickListener onFinish = view -> {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putBoolean("firstUse", false)
                    .apply();
            callback.setEnabled(false);
            getOnBackPressedDispatcher().onBackPressed();
        };
        skipButton.setOnClickListener(onFinish);
        finishButton.setOnClickListener(onFinish);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        final ViewPager2 viewPager = findViewById(R.id.onboarding_viewPager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                skipButton.setVisibility(position == PlaceholderFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
                nextButton.setVisibility(position == PlaceholderFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
                finishButton.setVisibility(position == PlaceholderFragment.PAGES_COUNT - 1 ? View.VISIBLE : View.GONE);
                callback.setEnabled(position != 0);
            }
        });
        nextButton.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));

        callback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                int item = viewPager.getCurrentItem();
                if (item != 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                } else {
                    callback.setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(callback);
    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.size(); i++) {
            indicators.get(i).setImageResource(i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected);
        }
    }
}