package com.peternaggschga.gwent.ui.main;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.lifecycle.ViewModelStore;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.RowBuffState;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.cases.ResetDialogUseCase;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class GameBoardViewModelUnitTest {
    private GwentApplication application;
    private GameBoardViewModel model;
    private UnitRepository repository;
    private SoundManager soundManager;

    @Before
    public void initModel() throws Exception {
        application = mock(GwentApplication.class);
        Constructor<GameBoardViewModel> constructor = GameBoardViewModel.class
                .getDeclaredConstructor(GwentApplication.class);
        constructor.setAccessible(true);
        model = constructor.newInstance(application);

        soundManager = mock(SoundManager.class);
        Field soundField = GameBoardViewModel.class.getDeclaredField("soundManager");
        soundField.setAccessible(true);
        soundField.set(model, soundManager);
        repository = mock(UnitRepository.class);
    }

    @After
    public void resetSchedulers() {
        RxJavaPlugins.reset();
        RxAndroidPlugins.reset();
    }

    @Test
    public void mardroemeClickDelegatesToRepository() {
        when(repository.switchMardroeme(RowType.MELEE)).thenReturn(Completable.complete());
        try (MockedStatic<GwentApplication> app = mockStatic(GwentApplication.class)) {
            app.when(() -> GwentApplication.getRepository(application)).thenReturn(Single.just(repository));
            model.onMardroemeViewPressed(RowType.MELEE).test().assertComplete();
            verify(repository).switchMardroeme(RowType.MELEE);
            verify(repository, never()).switchHorn(RowType.MELEE);
            verify(repository, never()).isHorn(RowType.MELEE);
        }
    }

    @Test
    public void hornClickDelegatesToRepositoryAndOnlyPlaysSoundWhenActivated() {
        when(repository.switchHorn(RowType.MELEE)).thenReturn(Completable.complete());
        when(repository.isHorn(RowType.MELEE)).thenReturn(Single.just(false), Single.just(true));
        try (MockedStatic<GwentApplication> app = mockStatic(GwentApplication.class)) {
            app.when(() -> GwentApplication.getRepository(application)).thenReturn(Single.just(repository));
            model.onHornViewPressed(RowType.MELEE).test().assertComplete();
            verify(repository).switchHorn(RowType.MELEE);
            verify(soundManager, never()).playHornSound();

            model.onHornViewPressed(RowType.MELEE).test().assertComplete();
            verify(soundManager).playHornSound();
        }
    }

    @Test
    public void factionSwitchAlwaysClearsMardroemeAndOnlyOptionallyResetsBoard() {
        Context context = mock(Context.class);
        Resources resources = mock(Resources.class);
        SharedPreferences preferences = mock(SharedPreferences.class);
        when(context.getResources()).thenReturn(resources);
        when(context.getString(R.string.preference_key_faction_reset)).thenReturn("reset_on_faction_switch");
        when(resources.getBoolean(R.bool.faction_reset_preference_default)).thenReturn(false);
        AtomicBoolean cleared = new AtomicBoolean();
        when(repository.clearMardroeme()).thenReturn(Completable.fromAction(() -> cleared.set(true)));

        try (MockedStatic<GwentApplication> app = mockStatic(GwentApplication.class);
             MockedStatic<PreferenceManager> preferenceManager = mockStatic(PreferenceManager.class);
             MockedStatic<ResetDialogUseCase> reset = mockStatic(ResetDialogUseCase.class)) {
            app.when(() -> GwentApplication.getRepository(application)).thenReturn(Single.just(repository));
            preferenceManager.when(() -> PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(preferences);

            when(preferences.getBoolean("reset_on_faction_switch", false)).thenReturn(false);
            model.onFactionSwitchReset(context).test().assertComplete();
            assertTrue(cleared.get());
            reset.verifyNoInteractions();

            when(preferences.getBoolean("reset_on_faction_switch", false)).thenReturn(true);
            for (boolean confirmed : new boolean[]{false, true}) {
                cleared.set(false);
                reset.when(() -> ResetDialogUseCase.reset(context, ResetDialogUseCase.Trigger.FACTION_SWITCH,
                                soundManager))
                        .thenAnswer(invocation -> {
                            assertTrue(cleared.get());
                            return Single.just(confirmed);
                        });
                model.onFactionSwitchReset(context).test().assertComplete();
                assertTrue(cleared.get());
            }
            verify(soundManager).playResetSound();
        }
    }

    @Test
    public void rowAndMenuStatesFollowPersistedBuffIncludingInitialMardroeme() {
        TestScheduler scheduler = new TestScheduler();
        RxJavaPlugins.setIoSchedulerHandler(ignored -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(ignored -> scheduler);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(ignored -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(ignored -> Schedulers.trampoline());

        BehaviorProcessor<RowBuffState> buff = BehaviorProcessor.createDefault(RowBuffState.MARDROEME);
        for (RowType row : RowType.values()) {
            when(repository.isWeatherFlowable(row)).thenReturn(Flowable.just(false));
            when(repository.getBuffStateFlowable(row)).thenReturn(row == RowType.MELEE
                    ? buff : Flowable.just(RowBuffState.NONE));
            when(repository.getUnitsFlowable(row)).thenReturn(Flowable.just(Collections.emptyList()));
        }
        when(repository.hasNonEpicUnitsFlowable()).thenReturn(Flowable.just(false));
        ViewModelStore store = new ViewModelStore();
        store.put("androidx.lifecycle.ViewModelProvider.DefaultKey:" + GameBoardViewModel.class.getCanonicalName(), model);
        GameBoardViewModel.getModel(() -> store, repository, soundManager);

        TestSubscriber<RowUiState> rows = model.getRowUiState(RowType.MELEE).test();
        TestSubscriber<MenuUiState> menu = model.getMenuUiState().test();
        try {
            scheduler.advanceTimeBy(30, TimeUnit.MILLISECONDS);
            rows.assertValueAt(0, RowUiState::isMardroeme);
            assertTrue(menu.values().get(0).isReset());

            buff.onNext(RowBuffState.NONE);
            scheduler.advanceTimeBy(30, TimeUnit.MILLISECONDS);
            rows.assertValueAt(1, state -> !state.isHorn() && !state.isMardroeme());
            assertFalse(menu.values().get(1).isReset());

            buff.onNext(RowBuffState.HORN);
            scheduler.advanceTimeBy(30, TimeUnit.MILLISECONDS);
            rows.assertValueAt(2, state -> state.isHorn() && !state.isMardroeme());
            rows.assertNoErrors().assertValueCount(3);
        } finally {
            rows.cancel();
            menu.cancel();
            store.clear();
        }
    }
}
