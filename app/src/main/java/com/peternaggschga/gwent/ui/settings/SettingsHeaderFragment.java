package com.peternaggschga.gwent.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.ui.introduction.IntroductionActivity;

import java.util.Objects;

/**
 * A PreferenceFragmentCompat class encapsulating the main preference screen,
 * i.e., the Preferences defined in R.xmlheader_preferences.
 */
public class SettingsHeaderFragment extends PreferenceFragmentCompat {
    /**
     * Called during onCreate(Bundle) to supply the preferences for this fragment.
     * Sets shown Preferences from R.xmlheader_preferences
     * and registers an androidx.preference.Preference.OnPreferenceClickListener
     * on the Preference at R.stringpreference_key_introduction to start a new IntroductionActivity.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     * @param rootKey            If non-null, this preference fragment should be rooted at the
     *                           androidx.preference.PreferenceScreen with this key.
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
