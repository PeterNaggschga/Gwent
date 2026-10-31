package com.peternaggschga.gwent.ui.settings;

import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.peternaggschga.gwent.R;

/**
 * A PreferenceFragmentCompat class encapsulating the sound preference screen,
 * i.e., the Preferences defined in R.xmlsound_preferences.
 */
@Keep
public class SettingsSoundFragment extends PreferenceFragmentCompat {
    /**
     * Called during onCreate(Bundle) to supply the preferences for this fragment.
     * Sets shown Preferences from R.xmlsound_preferences.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     * @param rootKey            If non-null, this preference fragment should be rooted at the
     *                           androidx.preference.PreferenceScreen with this key.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sound_preferences, rootKey);
    }
}
