package com.peternaggschga.gwent.ui.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.peternaggschga.gwent.R;

/**
 * A {@link PreferenceFragmentCompat} class encapsulating the sound preference screen,
 * i.e., the {@link Preference}s defined in {@link R.xml#sound_preferences}.
 */
public class SettingsSoundFragment extends PreferenceFragmentCompat {
    /**
     * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
     * Sets shown {@link Preference}s from {@link R.xml#sound_preferences}.
     *
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
