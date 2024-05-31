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

import com.peternaggschga.gwent.ui.introduction.IntroductionActivity;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.Objects;

/**
 * An {@link AppCompatActivity} implementing {@link PreferenceFragmentCompat.OnPreferenceStartFragmentCallback}
 * that is used by the user to manage the {@link SharedPreferences} of the application.
 */
public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    /**
     * Initializes layout and {@link ActionBar} as well as creates and displays a new {@link HeaderFragment}.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

    /**
     * Called, whenever the user chooses to navigate Up from the action bar.
     * If a {@link Fragment} is present on top of the back stack of
     * the current {@link androidx.fragment.app.FragmentManager}, it is popped.
     *
     * @return {@link Boolean} defining whether the call has been handled.
     */
    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    /**
     * Called whenever a {@link MenuItem} in the options menu is selected.
     * Returns to the calling {@link android.app.Activity} when the {@link android.R.id#home} item was selected.
     * @param item {@link MenuItem} that was selected.
     *
     * @return {@link Boolean} defining whether the call has been handled.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user has clicked on a {@link Preference} that has a {@link Fragment} class name
     * associated with it.
     * Switches to an instance of the given {@link Fragment}.
     * @param caller {@link PreferenceFragmentCompat} requesting navigation.
     * @param pref   {@link Preference} requesting the {@link Fragment}.
     * @return {@link Boolean} defining whether the {@link Fragment} creation has been handled.
     */
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

    /**
     * A {@link PreferenceFragmentCompat} class encapsulating the main preference screen,
     * i.e., the {@link Preference}s defined in {@link R.xml#header_preferences}.
     * @todo Move to own class file.
     */
    public static class HeaderFragment extends PreferenceFragmentCompat {
        /**
         * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
         * Sets shown {@link Preference}s from {@link R.xml#header_preferences}
         * and registers an {@link androidx.preference.Preference.OnPreferenceClickListener}
         * on the {@link Preference} at {@link R.string#preference_key_introduction} to start a new {@link IntroductionActivity}.
         * @param savedInstanceState If the fragment is being re-created from a previous saved state,
         *                           this is the state.
         * @param rootKey            If non-null, this preference fragment should be rooted at the
         *                           {@link androidx.preference.PreferenceScreen} with this key.
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey);

            Preference introductionPreference = Objects.requireNonNull(findPreference(getString(R.string.preference_key_introduction)));
            introductionPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), IntroductionActivity.class));
                return true;
            });
        }
    }

    /**
     * A {@link PreferenceFragmentCompat} class encapsulating the sound preference screen,
     * i.e., the {@link Preference}s defined in {@link R.xml#sound_preferences}.
     * @todo Move to own class file.
     */
    public static class SoundFragment extends PreferenceFragmentCompat {
        /**
         * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
         * Sets shown {@link Preference}s from {@link R.xml#sound_preferences}.
         * @param savedInstanceState If the fragment is being re-created from a previous saved state,
         *                           this is the state.
         * @param rootKey            If non-null, this preference fragment should be rooted at the
         *                           {@link androidx.preference.PreferenceScreen} with this key.
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.sound_preferences, rootKey);
        }
    }

    /**
     * A {@link PreferenceFragmentCompat} class encapsulating the rule preference screen,
     * i.e., the rule sections defined in {@link R.xml#rule_preferences}.
     * @todo Move to own class file.
     */
    public static class RuleHeaderFragment extends PreferenceFragmentCompat {
        /**
         * Creates an {@link Preference.OnPreferenceClickListener} that starts a new {@link RuleActivity}
         * for the given {@link RuleSection} using an {@link Intent}.
         * The {@link Intent} provides the requested {@link RuleSection} to the {@link RuleActivity}
         * using {@link Intent#putExtra(String, Serializable)} with {@link RuleActivity#INTENT_EXTRA_TAG} as a tag.
         * @param section {@link RuleSection} that is requested.
         * @return An {@link Preference.OnPreferenceClickListener} calling a {@link RuleActivity}.
         */
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

        /**
         * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
         * Sets shown {@link Preference}s from {@link R.xml#rule_preferences}.
         * Also provides each element with an {@link Preference.OnPreferenceClickListener}
         * that starts a new {@link RuleActivity} for the respective {@link RuleSection}.
         * @see #getSectionClickListener(RuleSection)
         * @param savedInstanceState If the fragment is being re-created from a previous saved state,
         *                           this is the state.
         * @param rootKey            If non-null, this preference fragment should be rooted at the
         *                           {@link androidx.preference.PreferenceScreen} with this key.
         */
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
