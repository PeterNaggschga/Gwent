package com.peternaggschga.gwent.ui.sounds;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;

import java.util.Arrays;

/**
 * A class responsible for initializing and playing sounds when they are enabled.
 *
 * @see Sound
 */
public class SoundManager {
    /**
     * Constant Integer representing the clear weather sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playClearWeatherSound()
     */
    public static final int SOUND_WEATHER_GOOD = 0;

    /**
     * Constant Integer representing the frost weather sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playCardAddSound(RowType, boolean)
     */
    public static final int SOUND_WEATHER_FROST = 1;

    /**
     * Constant Integer representing the fog weather sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playCardAddSound(RowType, boolean)
     */
    public static final int SOUND_WEATHER_FOG = 2;

    /**
     * Constant Integer representing the rain weather sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playCardAddSound(RowType, boolean)
     */
    public static final int SOUND_WEATHER_RAIN = 3;

    /**
     * Constant Integer representing the horn sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playHornSound()
     */
    public static final int SOUND_HORN = 4;

    /**
     * Constant Integer representing the epic unit sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playCardAddSound(RowType, boolean)
     */
    public static final int SOUND_CARDS_EPIC = 5;

    /**
     * Constant Integer representing the melee unit sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playCardAddSound(RowType, boolean)
     */
    public static final int SOUND_CARDS_MELEE = 6;

    /**
     * Constant Integer representing the range unit sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playCardAddSound(RowType, boolean)
     */
    public static final int SOUND_CARDS_RANGE = 7;

    /**
     * Constant Integer representing the siege unit sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playCardAddSound(RowType, boolean)
     */
    public static final int SOUND_CARDS_SIEGE = 8;

    /**
     * Constant Integer representing the reset sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playResetSound()
     */
    public static final int SOUND_RESET = 9;

    /**
     * Constant Integer representing the scorch sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playBurnSound()
     */
    public static final int SOUND_BURN = 10;

    /**
     * Constant Integer representing the coin-flip sound.
     * Index of the respective Sound object in #sounds.
     *
     * @see #playCoinSound()
     */
    public static final int SOUND_COIN = 11;

    /**
     * Array of Sound objects representing the different sound effects.
     * Indices are the public constants defined here,
     * i.e., #SOUND_WEATHER_GOOD, #SOUND_WEATHER_FROST, #SOUND_WEATHER_FOG,
     * #SOUND_WEATHER_RAIN, #SOUND_HORN, #SOUND_CARDS_EPIC, #SOUND_CARDS_MELEE,
     * #SOUND_CARDS_RANGE, #SOUND_CARDS_SIEGE, #SOUND_RESET, #SOUND_BURN, and #SOUND_COIN.
     */
    @NonNull
    private final Sound[] sounds = new Sound[12];

    /**
     * SoundPool where sound effects are registered.
     */
    @NonNull
    private final SoundPool soundPool;

    /**
     * OnSharedPreferenceChangeListener that is registered for the default SharedPreferences
     * and updates the Sound#active attributes of all #sounds when a preference change occurs.
     * Reference must be kept (even if not used)
     * to avoid garbage collection of the registered listener
     * (see <a href="https://developer.android.com/reference/android/content/SharedPreferences.html#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)">here</a> for more information).
     */
    @NonNull
    @SuppressWarnings("FieldCanBeLocal")
    private final SharedPreferences.OnSharedPreferenceChangeListener changeListener;

    /**
     * Constructor of a SoundManager in the given Context.
     * Creates a new #soundPool and registers #sounds using Sound#createSound().
     * Also registers a new #changeListener
     * that updates the Sound#activated status when sound settings are updated.
     *
     * @param context Context, this SoundManager is used in.
     * @see Sound#createSound(Context, int, SoundPool, int)
     */
    public SoundManager(@NonNull Context context) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            soundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attributes).setContext(context).build();
        } else {
            soundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attributes).build();
        }

        sounds[SOUND_WEATHER_GOOD] = Sound.createSound(context, R.string.preference_key_sounds_weather, soundPool, R.raw.weather_good);
        sounds[SOUND_WEATHER_FROST] = Sound.createSound(context, R.string.preference_key_sounds_weather, soundPool, R.raw.weather_frost);
        sounds[SOUND_WEATHER_FOG] = Sound.createSound(context, R.string.preference_key_sounds_weather, soundPool, R.raw.weather_fog);
        sounds[SOUND_WEATHER_RAIN] = Sound.createSound(context, R.string.preference_key_sounds_weather, soundPool, R.raw.weather_rain);
        sounds[SOUND_HORN] = Sound.createSound(context, R.string.preference_key_sounds_horn, soundPool, R.raw.horn);
        sounds[SOUND_CARDS_EPIC] = Sound.createSound(context, R.string.preference_key_sounds_cards, soundPool, R.raw.card_epic);
        sounds[SOUND_CARDS_MELEE] = Sound.createSound(context, R.string.preference_key_sounds_cards, soundPool, R.raw.card_melee);
        sounds[SOUND_CARDS_RANGE] = Sound.createSound(context, R.string.preference_key_sounds_cards, soundPool, R.raw.card_range);
        sounds[SOUND_CARDS_SIEGE] = Sound.createSound(context, R.string.preference_key_sounds_cards, soundPool, R.raw.card_siege);
        sounds[SOUND_RESET] = Sound.createSound(context, R.string.preference_key_sounds_reset, soundPool, R.raw.reset);
        sounds[SOUND_BURN] = Sound.createSound(context, R.string.preference_key_sounds_burn, soundPool, R.raw.burn);
        sounds[SOUND_COIN] = Sound.createSound(context, R.string.preference_key_sounds_coin, soundPool, R.raw.coin);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        changeListener = (sharedPreferences, key) -> {
            boolean newValue = sharedPreferences.getBoolean(key,
                    context.getResources().getBoolean(R.bool.sound_preference_default));
            if (context.getResources().getString(R.string.preference_key_sounds_all).equals(key)) {
                if (newValue) {
                    Arrays.stream(sounds).forEach(sound -> sound.setActivated(sharedPreferences));
                } else {
                    Arrays.stream(sounds).forEach(sound -> sound.setActivated(false));
                }
            } else {
                Arrays.stream(sounds)
                        .filter(sound -> sound.getPreferenceKey().equals(key))
                        .forEach(sound -> sound.setActivated(newValue));
            }
        };
        pref.registerOnSharedPreferenceChangeListener(changeListener);
    }

    /**
     * Plays the given Sound if it is #activated.
     * Given soundId must be either #SOUND_WEATHER_GOOD, #SOUND_WEATHER_FROST, #SOUND_WEATHER_FOG,
     * #SOUND_WEATHER_RAIN, #SOUND_HORN, #SOUND_CARDS_EPIC, #SOUND_CARDS_MELEE,
     * #SOUND_CARDS_RANGE, #SOUND_CARDS_SIEGE, #SOUND_RESET, #SOUND_BURN, or #SOUND_COIN.
     * Alternately, #playClearWeatherSound(), #playWeatherSound(), #playHornSound(),
     * #playCardAddSound(), #playCardRemovedSound(), #playResetSound(), #playBurnSound(),
     * or #playCoinSound() may be used.
     *
     * @param soundId Integer representing the Sound that should be played.
     */
    public void playSound(@IntRange(from = 0, to = 11) int soundId) {
        if (sounds[soundId].isActivated()) {
            soundPool.play(sounds[soundId].getSoundId(), 1, 1, 0, 0, 1);
        }
    }

    /**
     * Plays the clear weather sound.
     * Wrapper for #playSound().
     *
     * @see #playSound(int)
     */
    public void playClearWeatherSound() {
        playSound(SOUND_WEATHER_GOOD);
    }

    /**
     * Plays the weather sound of the given row.
     * Wrapper for #playSound().
     *
     * @param row RowType referencing the row for which the sound should be played.
     * @see #playSound(int)
     */
    public void playWeatherSound(@NonNull RowType row) {
        switch (row) {
            case MELEE:
                playSound(SOUND_WEATHER_FROST);
                break;
            case RANGE:
                playSound(SOUND_WEATHER_FOG);
                break;
            case SIEGE:
                playSound(SOUND_WEATHER_RAIN);
        }
    }

    /**
     * Plays the horn sound.
     * Wrapper for #playSound().
     *
     * @see #playSound(int)
     */
    public void playHornSound() {
        playSound(SOUND_HORN);
    }

    /**
     * Plays the card add sound for the given row.
     * If the unit is epic, the epic sound is played.
     * Wrapper for #playSound().
     *
     * @param row  RowType referencing the row for which the sound should be played.
     * @param epic Boolean defining whether the added unit is epic.
     * @see #playSound(int)
     */
    public void playCardAddSound(@NonNull RowType row, boolean epic) {
        if (epic) {
            playSound(SOUND_CARDS_EPIC);
            return;
        }
        switch (row) {
            case MELEE:
                playSound(SOUND_CARDS_MELEE);
                break;
            case RANGE:
                playSound(SOUND_CARDS_RANGE);
                break;
            case SIEGE:
                playSound(SOUND_CARDS_SIEGE);
        }
    }

    /**
     * Plays the reset sound.
     * Wrapper for #playSound().
     *
     * @see #playSound(int)
     */
    public void playCardRemovedSound() {
        playSound(SOUND_RESET);
    }

    /**
     * Plays the reset sound.
     * Wrapper for #playSound().
     *
     * @see #playSound(int)
     */
    public void playResetSound() {
        playSound(SOUND_RESET);
    }

    /**
     * Plays the burn sound.
     * Wrapper for #playSound().
     *
     * @see #playSound(int)
     */
    public void playBurnSound() {
        playSound(SOUND_BURN);
    }

    /**
     * Plays the coin sound.
     * Wrapper for #playSound().
     * @see #playSound(int)
     */
    public void playCoinSound() {
        playSound(SOUND_COIN);
    }
}
