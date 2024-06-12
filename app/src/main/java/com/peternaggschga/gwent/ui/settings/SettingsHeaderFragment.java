package com.peternaggschga.gwent.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.ui.introduction.IntroductionActivity;

import java.util.Objects;

/**
 * A {@link PreferenceFragmentCompat} class encapsulating the main preference screen,
 * i.e., the {@link Preference}s defined in {@link R.xml#header_preferences}.
 */
public class SettingsHeaderFragment extends PreferenceFragmentCompat {
    /**
     * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
     * Sets shown {@link Preference}s from {@link R.xml#header_preferences}
     * and registers an {@link androidx.preference.Preference.OnPreferenceClickListener}
     * on the {@link Preference} at {@link R.string#preference_key_introduction} to start a new {@link IntroductionActivity}.
     *
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
