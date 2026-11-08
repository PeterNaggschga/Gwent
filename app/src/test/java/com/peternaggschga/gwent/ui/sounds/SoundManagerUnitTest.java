package com.peternaggschga.gwent.ui.sounds;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.SoundPool;

import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.R;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

public class SoundManagerUnitTest {
    @Test
    public void clearingPreferencesRestoresDefaultSoundActivation() {
        verifyNullKeyRefresh(true, true, true);
    }

    @Test
    public void nullKeyRefreshRespectsMasterSwitch() {
        verifyNullKeyRefresh(false, true, false);
    }

    @Test
    public void nullKeyRefreshRespectsIndividualSoundPreference() {
        verifyNullKeyRefresh(true, false, false);
    }

    private void verifyNullKeyRefresh(boolean masterEnabled, boolean weatherEnabled, boolean expected) {
        Context context = mock(Context.class);
        Resources resources = mock(Resources.class);
        SharedPreferences preferences = mock(SharedPreferences.class);
        Sound sound = mock(Sound.class);
        when(context.getResources()).thenReturn(resources);
        when(context.getString(R.string.preference_key_sounds_all)).thenReturn("sound_all");
        when(resources.getBoolean(R.bool.sound_preference_default)).thenReturn(true);
        when(preferences.getBoolean(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        if (!masterEnabled) {
            when(preferences.getBoolean("sound_all", true)).thenReturn(false);
        }
        if (!weatherEnabled) {
            when(preferences.getBoolean("sound_weather", true)).thenReturn(false);
        }
        when(sound.getPreferenceKey()).thenReturn("sound_weather");

        try (MockedConstruction<AudioAttributes.Builder> attributes = mockConstruction(
                AudioAttributes.Builder.class, withSettings().defaultAnswer(RETURNS_SELF));
             MockedConstruction<SoundPool.Builder> pools = mockConstruction(
                     SoundPool.Builder.class, withSettings().defaultAnswer(RETURNS_SELF),
                     (builder, construction) -> when(builder.build()).thenReturn(mock(SoundPool.class)));
             MockedStatic<PreferenceManager> manager = mockStatic(PreferenceManager.class);
             MockedStatic<Sound> sounds = mockStatic(Sound.class)) {
            manager.when(() -> PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(preferences);
            sounds.when(() -> Sound.createSound(any(), anyInt(), any(), anyInt())).thenReturn(sound);
            new SoundManager(context);
            ArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> listener =
                    ArgumentCaptor.forClass(SharedPreferences.OnSharedPreferenceChangeListener.class);
            verify(preferences).registerOnSharedPreferenceChangeListener(listener.capture());

            listener.getValue().onSharedPreferenceChanged(preferences, null);

            verify(sound, times(12)).setActivated(expected);
        }
    }
}
