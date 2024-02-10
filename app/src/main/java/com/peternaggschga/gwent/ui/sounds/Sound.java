package com.peternaggschga.gwent.ui.sounds;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.SoundPool;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.R;

class Sound {
    @NonNull
    private final String preferenceKey;
    private final int soundId;
    private boolean activated;

    private Sound(@NonNull String preferenceKey, int soundId, boolean activated) {
        this.preferenceKey = preferenceKey;
        this.soundId = soundId;
        this.activated = activated;
    }

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

    int getSoundId() {
        return soundId;
    }

    boolean isActivated() {
        return activated;
    }

    void setActivated(boolean activated) {
        this.activated = activated;
    }

    void setActivated(@NonNull SharedPreferences sharedPreferences) {
        setActivated(sharedPreferences.getBoolean(preferenceKey, activated));
    }
}
