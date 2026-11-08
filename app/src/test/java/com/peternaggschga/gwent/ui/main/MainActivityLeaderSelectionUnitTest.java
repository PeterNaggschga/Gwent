package com.peternaggschga.gwent.ui.main;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_PREFERENCE_KEY;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SKELLIGE;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.data.UnitRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MainActivityLeaderSelectionUnitTest {
    private MainActivity activity;
    private GameBoardViewModel model;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private UnitRepository repository;

    @Before
    public void initMocks() throws Exception {
        activity = mock(MainActivity.class);
        model = mock(GameBoardViewModel.class);
        preferences = mock(SharedPreferences.class);
        editor = mock(SharedPreferences.Editor.class);
        repository = mock(UnitRepository.class);
        when(repository.setKingBran(anyBoolean())).thenReturn(Completable.complete());
        when(preferences.edit()).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
        Field modelField = MainActivity.class.getDeclaredField("gameBoardViewModel");
        modelField.setAccessible(true);
        modelField.set(activity, model);
        Field disposablesField = MainActivity.class.getDeclaredField("disposables");
        disposablesField.setAccessible(true);
        disposablesField.set(activity, new CompositeDisposable());
    }

    private void select(int theme, boolean kingBran) throws Exception {
        try (MockedStatic<PreferenceManager> manager = mockStatic(PreferenceManager.class);
             MockedStatic<GwentApplication> application = mockStatic(GwentApplication.class)) {
            manager.when(() -> PreferenceManager.getDefaultSharedPreferences(activity)).thenReturn(preferences);
            application.when(() -> GwentApplication.getRepository(activity)).thenReturn(Single.just(repository));
            Method method = MainActivity.class.getDeclaredMethod("selectFaction", int.class, boolean.class);
            method.setAccessible(true);
            method.invoke(activity, theme, kingBran);
        }
    }

    @Test
    public void reselectingSkelligeSavesNewLeaderWithoutResettingBoard() throws Exception {
        when(preferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL)).thenReturn(THEME_SKELLIGE);
        select(THEME_SKELLIGE, true);
        verify(repository).setKingBran(true);
        verify(editor).apply();
        verifyNoInteractions(model);
    }

    @Test
    public void switchingAwayClearsLeaderAndUsesExistingResetFlow() throws Exception {
        when(preferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL)).thenReturn(THEME_SKELLIGE);
        when(model.onFactionSwitchReset(activity)).thenReturn(Completable.complete());
        select(THEME_SCOIATAEL, false);
        verify(repository).setKingBran(false);
        verify(editor).putInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL);
        verify(editor).apply();
        verify(model).onFactionSwitchReset(activity);
    }

    @Test
    public void enteringSkelligeSavesCrachSelectionAndUsesExistingResetFlow() throws Exception {
        when(preferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL)).thenReturn(THEME_SCOIATAEL);
        when(model.onFactionSwitchReset(activity)).thenReturn(Completable.complete());
        select(THEME_SKELLIGE, false);
        verify(repository).setKingBran(false);
        verify(editor).putInt(THEME_PREFERENCE_KEY, THEME_SKELLIGE);
        verify(model).onFactionSwitchReset(activity);
    }
}
