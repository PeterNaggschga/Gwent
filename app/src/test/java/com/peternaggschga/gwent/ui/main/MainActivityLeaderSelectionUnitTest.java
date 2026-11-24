package com.peternaggschga.gwent.ui.main;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_PREFERENCE_KEY;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SKELLIGE;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import android.content.DialogInterface;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.dialogs.ChangeFactionDialog;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.CompletableSubject;

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

    private void select(int theme, Runnable afterSelection) throws Exception {
        AtomicReference<ChangeFactionDialog.Callback> callback = new AtomicReference<>();
        try (MockedStatic<PreferenceManager> manager = mockStatic(PreferenceManager.class);
             MockedStatic<GwentApplication> application = mockStatic(GwentApplication.class);
             MockedConstruction<ChangeFactionDialog> pickers = mockConstruction(ChangeFactionDialog.class,
                     (picker, construction) ->
                             callback.set((ChangeFactionDialog.Callback) construction.arguments().get(1)))) {
            manager.when(() -> PreferenceManager.getDefaultSharedPreferences(activity)).thenReturn(preferences);
            application.when(() -> GwentApplication.getRepository(activity)).thenReturn(Single.just(repository));
            Method method = MainActivity.class.getDeclaredMethod("inflateFactionPopup");
            method.setAccessible(true);
            method.invoke(activity);
            verify(pickers.constructed().get(0)).show();
            callback.get().onThemeSelect(theme);
            afterSelection.run();
        }
    }

    private void chooseLeader(AlertDialog.Builder builder, boolean kingBran) {
        ArgumentCaptor<DialogInterface.OnClickListener> choice =
                ArgumentCaptor.forClass(DialogInterface.OnClickListener.class);
        if (kingBran) {
            verify(builder).setPositiveButton(eq(R.string.alertDialog_yes), choice.capture());
        } else {
            verify(builder).setNegativeButton(eq(R.string.alertDialog_no), choice.capture());
        }
        choice.getValue().onClick(null, kingBran ? DialogInterface.BUTTON_POSITIVE : DialogInterface.BUTTON_NEGATIVE);
    }

    @Test
    public void reselectingSkelligeSavesNewLeaderWithoutResettingBoard() throws Exception {
        when(preferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL)).thenReturn(THEME_SKELLIGE);
        try (MockedConstruction<AlertDialog.Builder> dialogs =
                     mockConstruction(AlertDialog.Builder.class, withSettings().defaultAnswer(RETURNS_SELF))) {
            select(THEME_SKELLIGE, () -> {
                AlertDialog.Builder builder = dialogs.constructed().get(0);
                verify(builder).setIconAttribute(android.R.attr.alertDialogIcon);
                verify(builder).show();
                verifyNoInteractions(repository, editor);
                chooseLeader(builder, true);
            });
        }
        verify(repository).setKingBran(true);
        verifyNoInteractions(model);
    }

    @Test
    public void switchingAwayClearsLeaderAndUsesExistingResetFlow() throws Exception {
        when(preferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL)).thenReturn(THEME_SKELLIGE);
        CompletableSubject reset = CompletableSubject.create();
        when(model.onFactionSwitchReset(activity)).thenReturn(reset);
        select(THEME_SCOIATAEL, () -> {
            verify(editor).putInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL);
            verify(editor).apply();
            verifyNoInteractions(repository);
            reset.onComplete();
        });
        verify(repository).setKingBran(false);
        verify(model).onFactionSwitchReset(activity);
    }

    @Test
    public void enteringSkelligeSwitchesThemeAndWaitsForResetBeforeShowingLeaderDialog() throws Exception {
        when(preferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL)).thenReturn(THEME_SCOIATAEL);
        CompletableSubject reset = CompletableSubject.create();
        when(model.onFactionSwitchReset(activity)).thenReturn(reset);
        try (MockedConstruction<AlertDialog.Builder> dialogs =
                     mockConstruction(AlertDialog.Builder.class, withSettings().defaultAnswer(RETURNS_SELF))) {
            select(THEME_SKELLIGE, () -> {
                verify(editor).putInt(THEME_PREFERENCE_KEY, THEME_SKELLIGE);
                verify(editor).apply();
                assertThat(dialogs.constructed()).isEmpty();
                reset.onComplete();
                AlertDialog.Builder builder = dialogs.constructed().get(0);
                verify(builder).setIconAttribute(android.R.attr.alertDialogIcon);
                verify(builder).show();
                verifyNoInteractions(repository);
                chooseLeader(builder, false);
            });
        }
        verify(repository).setKingBran(false);
        verify(model).onFactionSwitchReset(activity);
    }
}
