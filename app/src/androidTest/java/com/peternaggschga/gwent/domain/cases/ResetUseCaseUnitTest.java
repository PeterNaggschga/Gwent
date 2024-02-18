package com.peternaggschga.gwent.domain.cases;

import static com.peternaggschga.gwent.domain.cases.ResetUseCase.TRIGGER_BUTTON_CLICK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@RunWith(AndroidJUnit4.class)
public class ResetUseCaseUnitTest {
    private UnitRepository repository;
    private UnitEntity mockUnit;

    @Before
    public void initMocks() {
        repository = mock(UnitRepository.class);
        when(repository.reset(any())).thenReturn(Completable.complete());
        when(repository.reset()).thenReturn(Completable.complete());

        mockUnit = mock(UnitEntity.class);
        when(mockUnit.isEpic()).thenReturn(false);
        when(repository.getUnits()).thenReturn(Single.just(Collections.singletonList(mockUnit)));
    }

    @Test
    public void resetCallsResetOnRepository() {
        new ResetUseCase(repository, TRIGGER_BUTTON_CLICK, false)
                .reset()
                .blockingAwait();
        verify(repository, atLeastOnce()).reset();
    }

    @Test
    public void showMonsterDialogChecksMonsterResetTrue() {
        for (boolean monsterTheme : new boolean[]{true, false}) {
            new ResetUseCase(repository, TRIGGER_BUTTON_CLICK, monsterTheme)
                    .showMonsterDialog()
                    .test()
                    .assertValue(monsterTheme)
                    .dispose();
        }
    }

    @Test
    public void showMonsterDialogChecksNonEpicUnitsExist() {
        new ResetUseCase(repository, TRIGGER_BUTTON_CLICK, true)
                .showMonsterDialog()
                .test()
                .assertValue(true)
                .dispose();
        when(mockUnit.isEpic()).thenReturn(true);
        new ResetUseCase(repository, TRIGGER_BUTTON_CLICK, true)
                .showMonsterDialog()
                .test()
                .assertValue(false)
                .dispose();
    }
}
