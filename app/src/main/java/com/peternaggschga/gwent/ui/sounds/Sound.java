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
    private boolean muted;

    private Sound(@NonNull String preferenceKey, int soundId, boolean muted) {
        this.preferenceKey = preferenceKey;
        this.soundId = soundId;
        this.muted = muted;
    }

    @NonNull
    static Sound createSound(@NonNull Context context, @StringRes int preferenceRes,
                             @NonNull SoundPool soundPool, @RawRes int resId) {
        String preferenceKey = context.getString(preferenceRes);
        int soundId = soundPool.load(context, resId, 1);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean muted = context.getResources().getBoolean(R.bool.sound_preference_default);
        muted = pref.getBoolean(context.getString(R.string.preference_key_sounds_all), muted)
                && pref.getBoolean(preferenceKey, muted);
        return new Sound(preferenceKey, soundId, muted);
    }

    @NonNull
    String getPreferenceKey() {
        return preferenceKey;
    }

    int getSoundId() {
        return soundId;
    }

    boolean isMuted() {
        return muted;
    }

    void setMuted(boolean muted) {
        this.muted = muted;
    }
}
