package com.peternaggschga.gwent.ui.sounds;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.SoundPool;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.R;

/**
 * A data class encapsulating information on a certain sound effect,
 * e.g., its id in a SoundPool and whether it is activated in settings.
 */
class Sound {
    /**
     * String representing a key of the default SharedPreferences.
     * The referenced preference decides whether the sound is activated
     * (assuming that sounds are not completely disabled).
     */
    @NonNull
    private final String preferenceKey;

    /**
     * Integer representing the id of the registered sound at a SoundPool.
     */
    private final int soundId;

    /**
     * Boolean defining whether the sound is muted in the settings.
     */
    private boolean activated;

    /**
     * Constructor of a Sound encapsulating the given values.
     * Should only be called by factory method #createSound()
     *
     * @param preferenceKey String representing the key where the activated status is defined in SharedPreferences.
     * @param soundId       Integer representing the id of the registered sound at a SoundPool.
     * @param activated     Boolean defining whether the sound is activated in the settings.
     * @see #createSound(Context, int, SoundPool, int)
     */
    private Sound(@NonNull String preferenceKey, int soundId, boolean activated) {
        this.preferenceKey = preferenceKey;
        this.soundId = soundId;
        this.activated = activated;
    }

    /**
     * Returns a new Sound object encapsulating data for a newly registered sound effect.
     * Factory method for the Sound class.
     * Gets #preferenceKey from the given Context.
     * Registers the sound with the given resId at the given SoundPool.
     * Decides the value of #activated from SharedPreferences obtained using the given Context.
     *
     * @param context       Context of the application where this Sound is used.
     * @param preferenceRes Integer referencing the String resource of the preference key for this Sound.
     * @param soundPool     SoundPool where the Sound is registered.
     * @param resId         Integer referencing the raw resource of the Sound.
     * @return A Sound object referencing the newly registered sound effect.
     */
    @NonNull
    static Sound createSound(@NonNull Context context, @StringRes int preferenceRes,
                             @NonNull SoundPool soundPool, @RawRes int resId) {
        String preferenceKey = context.getString(preferenceRes);
        int soundId = soundPool.load(context, resId, 1);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean activated = context.getResources().getBoolean(R.bool.sound_preference_default);
        activated = pref.getBoolean(context.getString(R.string.preference_key_sounds_all), activated)
                && pref.getBoolean(preferenceKey, activated);
        return new Sound(preferenceKey, soundId, activated);
    }

    /**
     * Returns the id of this sound effect at the SoundPool.
     *
     * @return An Integer referencing the sound effect at the SoundPool.
     */
    int getSoundId() {
        return soundId;
    }

    /**
     * Returns whether the sound is activated in the settings.
     *
     * @return A Boolean defining whether the Sound is activated.
     */
    boolean isActivated() {
        return activated;
    }

    /**
     * Changes the value of #activated to the given Boolean.
     *
     * @param activated Boolean defining whether the Sound is activated.
     * @see #setActivated(SharedPreferences)
     */
    void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Changes the value of #activated based on the given SharedPreferences.
     * Only queries the boolean at key #preferenceKey, not whether all sounds are deactivated!
     *
     * @param sharedPreferences SharedPreferences containing information on the activated sounds.
     */
    void setActivated(@NonNull SharedPreferences sharedPreferences) {
        setActivated(sharedPreferences.getBoolean(preferenceKey, activated));
    }
}
