package com.peternaggschga.gwent.data;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class UnitRepositoryKingBranUnitTest {
    @Test
    public void leaderStateIsSavedObservedAndSharedAcrossRepositoryInstances() throws Exception {
        Context context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(context);
        SharedPreferences preferences = mock(SharedPreferences.class);
        SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);
        AtomicBoolean saved = new AtomicBoolean();
        AtomicBoolean pending = new AtomicBoolean();
        List<SharedPreferences.OnSharedPreferenceChangeListener> listeners = new ArrayList<>();
        when(preferences.getBoolean("kingBran", false)).thenAnswer(ignored -> saved.get());
        when(preferences.edit()).thenReturn(editor);
        when(editor.putBoolean(eq("kingBran"), anyBoolean())).thenAnswer(invocation -> {
            pending.set(invocation.getArgument(1));
            return editor;
        });
        doAnswer(invocation -> {
            listeners.add(invocation.getArgument(0));
            return null;
        }).when(preferences).registerOnSharedPreferenceChangeListener(any());
        doAnswer(invocation -> {
            listeners.remove(invocation.getArgument(0));
            return null;
        }).when(preferences).unregisterOnSharedPreferenceChangeListener(any());
        doAnswer(ignored -> {
            saved.set(pending.get());
            for (SharedPreferences.OnSharedPreferenceChangeListener listener : listeners) {
                listener.onSharedPreferenceChanged(preferences, "kingBran");
            }
            return null;
        }).when(editor).apply();

        Constructor<UnitRepository> constructor = UnitRepository.class.getDeclaredConstructor(AppDatabase.class, Context.class);
        constructor.setAccessible(true);
        try (MockedStatic<PreferenceManager> manager = mockStatic(PreferenceManager.class)) {
            manager.when(() -> PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(preferences);
            UnitRepository repository = constructor.newInstance(mock(AppDatabase.class), context);
            assertThat(repository.isKingBran()).isFalse();
            TestSubscriber<Boolean> observer = repository.isKingBranFlowable().test();
            observer.assertValues(false);
            Completable enable = repository.setKingBran(true);
            assertThat(repository.isKingBran()).isFalse();
            enable.test().assertComplete();
            observer.assertValues(false, true);
            repository.setKingBran(true).test().assertComplete();
            observer.assertValues(false, true);
            UnitRepository recreated = constructor.newInstance(mock(AppDatabase.class), context);
            assertThat(recreated.isKingBran()).isTrue();
            recreated.setKingBran(false).test().assertComplete();
            observer.assertValues(false, true, false);
            assertThat(repository.isKingBran()).isFalse();
            repository.setKingBran(true).test().assertComplete();
            saved.set(false);
            listeners.get(0).onSharedPreferenceChanged(preferences, null);
            observer.assertValues(false, true, false, true, false);
            observer.cancel();
            assertThat(listeners).isEmpty();
        }
    }
}
