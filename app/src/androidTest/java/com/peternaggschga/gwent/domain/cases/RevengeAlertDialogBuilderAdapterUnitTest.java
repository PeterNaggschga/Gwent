package com.peternaggschga.gwent.domain.cases;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import io.reactivex.rxjava3.core.Completable;

@RunWith(AndroidJUnit4.class)
public class RevengeAlertDialogBuilderAdapterUnitTest {
    private static final int TESTING_DEPTH = 50;
    @Mock
    private SoundManager soundManager;

    @Test
    public void insertAvengersInsertsUnits() {
        UnitRepository repository = mock(UnitRepository.class);
        when(repository.insertUnit(anyBoolean(), anyInt(), any(), any(), any(), anyInt())).thenReturn(Completable.complete());
        for (int numberOfAvengers = 0; numberOfAvengers < TESTING_DEPTH; numberOfAvengers++) {
            RevengeAlertDialogBuilderAdapter.insertAvengers(repository, numberOfAvengers, soundManager);
            verify(repository, atLeastOnce()).insertUnit(anyBoolean(), anyInt(), any(), any(), any(), eq(numberOfAvengers));
        }
    }

    @Test
    public void createReturnsNonNullDialog() {
        Looper.prepare();
        assertThat(new RevengeAlertDialogBuilderAdapter(ApplicationProvider.getApplicationContext()).create()).isNotNull();
    }
}
