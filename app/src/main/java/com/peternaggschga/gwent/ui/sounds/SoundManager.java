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

public class SoundManager {
    public static final int SOUND_WEATHER_GOOD = 0;
    public static final int SOUND_WEATHER_FROST = 1;
    public static final int SOUND_WEATHER_FOG = 2;
    public static final int SOUND_WEATHER_RAIN = 3;
    public static final int SOUND_HORN = 4;
    public static final int SOUND_CARDS_EPIC = 5;
    public static final int SOUND_CARDS_MELEE = 6;
    public static final int SOUND_CARDS_RANGE = 7;
    public static final int SOUND_CARDS_SIEGE = 8;
    public static final int SOUND_RESET = 9;
    public static final int SOUND_BURN = 10;
    public static final int SOUND_COIN = 11;

    @NonNull
    private final SoundPool soundPool;
    @NonNull
    private final Sound[] sounds = new Sound[12];
    @NonNull
    @SuppressWarnings("FieldCanBeLocal")
    private final SharedPreferences.OnSharedPreferenceChangeListener changeListener;


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

    public void playSound(@IntRange(from = 0, to = 11) int soundId) {
        if (sounds[soundId].isActivated()) {
            soundPool.play(sounds[soundId].getSoundId(), 1, 1, 0, 0, 1);
        }
    }

    public void playClearWeatherSound() {
        playSound(SOUND_WEATHER_GOOD);
    }

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

    public void playHornSound() {
        playSound(SOUND_HORN);
    }

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

    public void playCardRemovedSound() {
        playSound(SOUND_RESET);
    }

    public void playResetSound() {
        playSound(SOUND_RESET);
    }

    public void playBurnSound() {
        playSound(SOUND_BURN);
    }

    public void playCoinSound() {
        playSound(SOUND_COIN);
    }
}
