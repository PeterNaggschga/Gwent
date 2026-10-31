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
 * An AppCompatActivity implementing PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
 * that is used by the user to manage the SharedPreferences of the application.
 */
public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    /**
     * String constant defining the key in savedInstanceState Bundles
     * where the last shown Fragment is saved.
     */
    private static final String CURRENT_FRAGMENT_KEY = "currentFragment";

    /**
     * Initializes layout and ActionBar as well as creates and displays a new SettingsHeaderFragment.
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState.  <b><i>Note: Otherwise it is null.</i></b>
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
     * Saves the currently visible Fragment into the given Bundle.
     * @param outState Bundle in which the currently used Fragment is saved.
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
     * Called whenever a MenuItem in the options menu is selected.
     * Returns to the calling android.app.Activity when the android.R.idhome item was selected.
     * @param item MenuItem that was selected.
     *
     * @return Boolean defining whether the call has been handled.
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
     * Called when the user has clicked on a Preference that has a Fragment class name
     * associated with it.
     * Switches to an instance of the given Fragment.
     * @param caller PreferenceFragmentCompat requesting navigation.
     * @param pref   Preference requesting the Fragment.
     * @return Boolean defining whether the Fragment creation has been handled.
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
