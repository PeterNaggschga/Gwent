package com.peternaggschga.gwent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Px;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.peternaggschga.gwent.ui.main.PlaceholderFragment;
import com.peternaggschga.gwent.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class OnboardingSupportActivity extends AppCompatActivity {

    private final List<ImageView> indicators = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_support);
        MainActivity.hideSystemUI(getWindow());
        final Button skipButton = findViewById(R.id.onboarding_button_skip);
        final ImageButton nextButton = findViewById(R.id.onboarding_button_next);
        final Button finishButton = findViewById(R.id.onboarding_button_finish);
        indicators.add((ImageView) findViewById(R.id.onboarding_indicator_0));
        indicators.add((ImageView) findViewById(R.id.onboarding_indicator_1));
        indicators.add((ImageView) findViewById(R.id.onboarding_indicator_2));
        indicators.add((ImageView) findViewById(R.id.onboarding_indicator_3));
        indicators.add((ImageView) findViewById(R.id.onboarding_indicator_4));
        View.OnClickListener onFinish = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("firstUse", false).apply();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        };
        skipButton.setOnClickListener(onFinish);
        finishButton.setOnClickListener(onFinish);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        final ViewPager viewPager = findViewById(R.id.onboarding_viewPager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, @Px int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                skipButton.setVisibility(position == PlaceholderFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
                nextButton.setVisibility(position == PlaceholderFragment.PAGES_COUNT - 1 ? View.GONE : View.VISIBLE);
                finishButton.setVisibility(position == PlaceholderFragment.PAGES_COUNT - 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.arrowScroll(View.SCROLLBAR_POSITION_RIGHT);
            }
        });
    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.size(); i++) {
            indicators.get(i).setBackgroundResource(i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected);
        }
    }
}