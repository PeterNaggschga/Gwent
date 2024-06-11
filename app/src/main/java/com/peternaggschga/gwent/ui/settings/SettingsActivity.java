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
     * Initializes layout and {@link ActionBar} as well as creates and displays a new {@link SettingsHeaderFragment}.
     * @todo Save current Fragment to keep correct view on rotation.
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

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingsFrameLayout, new SettingsHeaderFragment())
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
}
