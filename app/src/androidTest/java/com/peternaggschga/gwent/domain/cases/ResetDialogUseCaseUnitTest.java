package com.peternaggschga.gwent.domain.cases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.R;
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
public class ResetDialogUseCaseUnitTest {
    private UnitRepository repository;
    @Mock
    private SoundManager soundManager;

    @Before
    public void initMocks() {
        repository = mock(UnitRepository.class);
        when(repository.isWeather(any())).thenReturn(Single.just(true));
        when(repository.isHorn(any())).thenReturn(Single.just(true));
        when(repository.getUnits()).thenReturn(Single.just(Collections.emptyList()));
        when(repository.reset(any())).thenReturn(Completable.complete());
        when(repository.reset()).thenReturn(Completable.complete());
    }

    @Test
    public void resetCallsResetOnRepository() {
        Context context = ApplicationProvider.getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.preference_key_warning), false)
                .apply();
        ResetDialogUseCase.reset(ApplicationProvider.getApplicationContext(),
                        repository,
                        ResetDialogUseCase.Trigger.BUTTON_CLICK,
                        soundManager)
                .blockingSubscribe();
        verify(repository, atLeastOnce()).reset(null);
    }
}
