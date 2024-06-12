package com.peternaggschga.gwent.domain.cases;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.data.UnitRepository;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RevengeAlertDialogBuilderAdapterUnitTest {
    private static final int TESTING_DEPTH = 50;

    @Test
    public void insertAvengersInsertsUnits() {
        UnitRepository repository = mock(UnitRepository.class);
        for (int numberOfAvengers = 0; numberOfAvengers < TESTING_DEPTH; numberOfAvengers++) {
            RevengeAlertDialogBuilderAdapter.insertAvengers(repository, numberOfAvengers);
            verify(repository, atLeastOnce()).insertUnit(anyBoolean(), anyInt(), any(), any(), any(), eq(numberOfAvengers));
        }
    }

    @Test
    public void createReturnsNonNullDialog() {
        Looper.prepare();
        assertThat(new RevengeAlertDialogBuilderAdapter(ApplicationProvider.getApplicationContext()).create()).isNotNull();
    }
}
