package com.peternaggschga.gwent.ui.settings;

import static com.peternaggschga.gwent.ui.settings.RuleSection.CARDS;
import static com.peternaggschga.gwent.ui.settings.RuleSection.CARD_ABILITIES;
import static com.peternaggschga.gwent.ui.settings.RuleSection.COMMANDER;
import static com.peternaggschga.gwent.ui.settings.RuleSection.COURSE;
import static com.peternaggschga.gwent.ui.settings.RuleSection.FACTIONS;
import static com.peternaggschga.gwent.ui.settings.RuleSection.GENERAL;
import static com.peternaggschga.gwent.ui.settings.RuleSection.SPECIAL_CARDS;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.peternaggschga.gwent.R;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.Objects;

/**
 * A PreferenceFragmentCompat class encapsulating the rule preference screen,
 * i.e., the rule sections defined in R.xmlrule_preferences.
 */
@Keep
public class SettingsRuleFragment extends PreferenceFragmentCompat {
    /**
     * Creates an Preference.OnPreferenceClickListener that starts a new RuleActivity
     * for the given RuleSection using an Intent.
     * The Intent provides the requested RuleSection to the RuleActivity
     * using IntentputExtra(String, with RuleActivityINTENT_EXTRA_TAG as a tag.
     *
     * @param section RuleSection that is requested.
     * @return An Preference.OnPreferenceClickListener calling a RuleActivity.
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
     * Called during onCreate(Bundle) to supply the preferences for this fragment.
     * Sets shown Preferences from R.xmlrule_preferences.
     * Also provides each element with an Preference.OnPreferenceClickListener
     * that starts a new RuleActivity for the respective RuleSection.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     * @param rootKey            If non-null, this preference fragment should be rooted at the
     *                           androidx.preference.PreferenceScreen with this key.
     * @see .getSectionClickListener(RuleSection)
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
