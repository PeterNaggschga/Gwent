package com.peternaggschga.gwent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(sharedPreferences.getInt("faction", MainActivity.THEME.SCOIATAEL.ordinal()));
        setContentView(R.layout.activity_settings);
        setSupportActionBar(findViewById(R.id.settingsToolbar));
        MainActivity.hideSystemUI(getWindow());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsFrameLayout, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        setTitle(R.string.settings_title);
                    }
                });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current activity title, so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();

        Fragment fragment = Objects.equals(pref.getFragment(), "com.peternaggschga.gwent.SettingsActivity$SoundFragment") ? getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), SoundFragment.class.getName()) : getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), RuleHeaderFragment.class.getName());
        fragment.setArguments(args);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingsFrameLayout, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey);
            Preference design = findPreference("design");
            assert design != null;
            design.setOnPreferenceChangeListener((preference, newValue) -> {
                MainActivity.hideSystemUI(requireActivity().getWindow());
                return true;
            });
            Preference unitString = findPreference("unit_string");
            assert unitString != null;
            unitString.setOnPreferenceChangeListener((preference, newValue) -> {
                Unit unit = new Unit(5, false, false, false, 1, false);
                unit.setRow(Row.ROW_MELEE);
                unit.setBuffAD(10);
                unit.setBuffed(true);
                Toast.makeText(getContext(), getString(R.string.preference_unit_string_alert, unit.toString(requireContext(), newValue)), Toast.LENGTH_SHORT).show();
                return true;
            });
            Preference onboardingSupport = findPreference("onboardingSupport");
            assert onboardingSupport != null;
            onboardingSupport.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), OnboardingSupportActivity.class));
                return true;
            });
        }
    }

    public static class SoundFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.sound_preferences, rootKey);
        }
    }

    public static class RuleHeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.rule_preferences, rootKey);
            Preference rulesGeneral = findPreference("rules_general");
            assert rulesGeneral != null;
            rulesGeneral.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), RuleActivity.class).putExtra(RuleActivity.INTENT_EXTRA_TAG, RuleActivity.RULES.GENERAL));
                return true;
            });
            Preference rulesCourse = findPreference("rules_course");
            assert rulesCourse != null;
            rulesCourse.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), RuleActivity.class).putExtra(RuleActivity.INTENT_EXTRA_TAG, RuleActivity.RULES.COURSE));
                return true;
            });
            Preference ruleFactions = findPreference("rules_factions");
            assert ruleFactions != null;
            ruleFactions.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), RuleActivity.class).putExtra(RuleActivity.INTENT_EXTRA_TAG, RuleActivity.RULES.FACTIONS));
                return true;
            });
            Preference rulesCommander = findPreference("rules_commander");
            assert rulesCommander != null;
            rulesCommander.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), RuleActivity.class).putExtra(RuleActivity.INTENT_EXTRA_TAG, RuleActivity.RULES.COMMANDER));
                return true;
            });
            Preference rulesCards = findPreference("rules_cards");
            assert rulesCards != null;
            rulesCards.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), RuleActivity.class).putExtra(RuleActivity.INTENT_EXTRA_TAG, RuleActivity.RULES.CARDS));
                return true;
            });
            Preference ruleCardAbilities = findPreference("rules_card_abilities");
            assert ruleCardAbilities != null;
            ruleCardAbilities.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), RuleActivity.class).putExtra(RuleActivity.INTENT_EXTRA_TAG, RuleActivity.RULES.CARD_ABILITIES));
                return true;
            });
            Preference rulesSpecialCards = findPreference("rules_special_cards");
            assert rulesSpecialCards != null;
            rulesSpecialCards.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), RuleActivity.class).putExtra(RuleActivity.INTENT_EXTRA_TAG, RuleActivity.RULES.SPECIAL_CARDS));
                return true;
            });
        }
    }
}