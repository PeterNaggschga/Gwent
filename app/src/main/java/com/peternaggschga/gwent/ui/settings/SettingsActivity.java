package com.peternaggschga.gwent.ui.settings;

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

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.ui.main.FactionSwitchListener;

import java.util.Objects;

/**
 * An {@link AppCompatActivity} implementing {@link PreferenceFragmentCompat.OnPreferenceStartFragmentCallback}
 * that is used by the user to manage the {@link SharedPreferences} of the application.
 */
public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    /**
     * {@link String} constant defining the key in savedInstanceState {@link Bundle}s
     * where the last shown {@link Fragment} is saved.
     */
    private static final String CURRENT_FRAGMENT_KEY = "currentFragment";

    /**
     * Initializes layout and {@link ActionBar} as well as creates and displays a new {@link SettingsHeaderFragment}.
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FactionSwitchListener.setTheme(this);

        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.settingsToolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Fragment currentFragment = null;
        if (savedInstanceState != null) {
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT_KEY);
        }
        currentFragment = currentFragment == null ? new SettingsHeaderFragment() : currentFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingsFrameLayout, currentFragment)
                .commit();
    }

    /**
     * Saves the currently visible {@link Fragment} into the given {@link Bundle}.
     * @param outState Bundle in which the currently used {@link Fragment} is saved.
     *
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().getFragments()
                .stream()
                .filter(Fragment::isVisible)
                .findAny()
                .ifPresent(fragment ->
                        getSupportFragmentManager().putFragment(outState, CURRENT_FRAGMENT_KEY, fragment));
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
}
