package com.peternaggschga.gwent;

import static com.peternaggschga.gwent.RuleSection.CARDS;
import static com.peternaggschga.gwent.RuleSection.CARD_ABILITIES;
import static com.peternaggschga.gwent.RuleSection.COMMANDER;
import static com.peternaggschga.gwent.RuleSection.COURSE;
import static com.peternaggschga.gwent.RuleSection.FACTIONS;
import static com.peternaggschga.gwent.RuleSection.GENERAL;
import static com.peternaggschga.gwent.RuleSection.SPECIAL_CARDS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NILFGAARD;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_PREFERENCE_KEY;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * @todo Documentation
 */
public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // TODO: move mapping from preference to style id to FactionSwitchListener
        switch (sharedPreferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL)) {
            case THEME_MONSTER:
                setTheme(R.style.MonsterTheme);
                break;
            case THEME_NILFGAARD:
                setTheme(R.style.NilfgaardTheme);
                break;
            case THEME_NORTHERN_KINGDOMS:
                setTheme(R.style.NorthernKingdomsTheme);
                break;
            case THEME_SCOIATAEL:
                setTheme(R.style.ScoiataelTheme);
        }

        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.settingsToolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingsFrameLayout, new HeaderFragment())
                .commit();
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

        Fragment fragment = getSupportFragmentManager()
                .getFragmentFactory()
                .instantiate(getClassLoader(), Objects.requireNonNull(pref.getFragment()));
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

            Preference onboardingSupport = findPreference("onboardingSupport");
            Objects.requireNonNull(onboardingSupport).setOnPreferenceClickListener(preference -> {
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
        @NonNull
        @Contract(pure = true)
        private Preference.OnPreferenceClickListener getSectionClickListener(@NonNull RuleSection section) {
            return preference -> {
                startActivity(
                        new Intent(getContext(), RuleActivity.class)
                                .putExtra(RuleActivity.INTENT_EXTRA_TAG, section)
                );
                return true;
            };
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.rule_preferences, rootKey);

            Preference rulesGeneral = Objects.requireNonNull(findPreference(getString(R.string.preference_rules_general_key)));
            rulesGeneral.setOnPreferenceClickListener(getSectionClickListener(GENERAL));

            Preference rulesCourse = Objects.requireNonNull(findPreference(getString(R.string.preference_rules_course_key)));
            rulesCourse.setOnPreferenceClickListener(getSectionClickListener(COURSE));

            Preference ruleFactions = Objects.requireNonNull(findPreference(getString(R.string.preference_rules_factions_key)));
            ruleFactions.setOnPreferenceClickListener(getSectionClickListener(FACTIONS));

            Preference rulesCommander = Objects.requireNonNull(findPreference(getString(R.string.preference_rules_commander_key)));
            rulesCommander.setOnPreferenceClickListener(getSectionClickListener(COMMANDER));

            Preference rulesCards = Objects.requireNonNull(findPreference(getString(R.string.preference_rules_cards_key)));
            rulesCards.setOnPreferenceClickListener(getSectionClickListener(CARDS));

            Preference ruleCardAbilities = Objects.requireNonNull(findPreference(getString(R.string.preference_rules_card_abilities_key)));
            ruleCardAbilities.setOnPreferenceClickListener(getSectionClickListener(CARD_ABILITIES));

            Preference rulesSpecialCards = Objects.requireNonNull(findPreference(getString(R.string.preference_rules_special_cards_key)));
            rulesSpecialCards.setOnPreferenceClickListener(getSectionClickListener(SPECIAL_CARDS));
        }
    }
}
