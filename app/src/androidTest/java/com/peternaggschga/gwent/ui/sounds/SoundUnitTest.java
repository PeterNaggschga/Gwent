package com.peternaggschga.gwent.ui.sounds;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.content.Context;
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
                        mock(SoundPool.class),
                        R.raw.weather_good).isActivated()).isEqualTo(weatherSound && allSound);
            }
        }
    }

    @Test
    public void createSoundInitializesPreferenceKeyAttribute() {
        assertThat(Sound.createSound(context,
                R.string.preference_key_sounds_weather,
                mock(SoundPool.class),
                R.raw.weather_good).getPreferenceKey()).isEqualTo(context.getString(R.string.preference_key_sounds_weather));
    }
}
