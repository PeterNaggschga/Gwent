package com.peternaggschga.gwent.domain.cases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Collections;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@RunWith(AndroidJUnit4.class)
public class BurnDialogUseCaseUnitTest {
    private UnitRepository repository;
    @Mock
    private SoundManager soundManager;

    @Before
    public void initMocks() {
        repository = mock(UnitRepository.class);
        when(repository.delete(any())).thenReturn(Completable.complete());
    }

    @Test
    public void burnReturnsFalseForEmptyBoard() {
        when(repository.getUnits()).thenReturn(Single.just(Collections.emptyList()));
        BurnDialogUseCase.burn(ApplicationProvider.getApplicationContext(), repository, soundManager)
                .test()
                .assertValue(false)
                .dispose();
    }
}
