package com.peternaggschga.gwent.ui.sounds;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.SoundPool;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SoundUnitTest {
    private final Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void createSoundInitializesActivatedAttribute() {
        for (boolean allSound : new boolean[]{false, true}) {
            for (boolean weatherSound : new boolean[]{false, true}) {
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .putBoolean(context.getString(R.string.preference_key_sounds_all), allSound)
                        .putBoolean(context.getString(R.string.preference_key_sounds_weather), weatherSound)
                        .apply();
                assertThat(Sound.createSound(context,
                        R.string.preference_key_sounds_weather,
                        new SoundPool.Builder().build(),
                        R.raw.weather_good).isActivated()).isEqualTo(weatherSound && allSound);
            }
        }
    }

    @Test
    public void createSoundInitializesPreferenceKeyAttribute() {
        assertThat(Sound.createSound(context,
                R.string.preference_key_sounds_weather,
                new SoundPool.Builder().build(),
                R.raw.weather_good).getPreferenceKey()).isEqualTo(context.getString(R.string.preference_key_sounds_weather));
    }

    @Test
    public void setActivatedSharedPreferencesSetsCorrectActiveValue() {
        Sound testSound = Sound.createSound(context, R.string.preference_key_sounds_weather, new SoundPool.Builder().build(), R.raw.weather_good);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        for (boolean weatherSound : new boolean[]{false, true}) {
            pref.edit()
                    .putBoolean(context.getString(R.string.preference_key_sounds_weather), weatherSound)
                    .apply();
            testSound.setActivated(pref);
            assertThat(testSound.isActivated()).isEqualTo(weatherSound);
        }
    }
}
