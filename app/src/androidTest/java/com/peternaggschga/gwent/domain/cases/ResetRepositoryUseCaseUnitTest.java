package com.peternaggschga.gwent.domain.cases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@RunWith(AndroidJUnit4.class)
public class ResetRepositoryUseCaseUnitTest {
    private static final int TESTING_DEPTH = 50;
    private UnitRepository mockRepository;
    private UnitEntity mockEntity;

    @Before
    public void initMocks() {
        mockRepository = mock(UnitRepository.class);
        when(mockRepository.reset(any())).thenReturn(Completable.complete());
        mockEntity = mock(UnitEntity.class);
        when(mockEntity.getAbility()).thenReturn(Ability.NONE);
    }

    public List<UnitEntity> getUnitMockList(int numberOfUnits) {
        List<UnitEntity> result = new ArrayList<>(numberOfUnits);
        for (int i = 0; i < numberOfUnits; i++) {
            result.add(mockEntity);
        }
        return result;
    }

    @Test
    public void resetNoUnitCallsResetNullUnit() {
        when(mockRepository.getUnits()).thenReturn(Single.just(Collections.emptyList()));
        ResetRepositoryUseCase.reset(ApplicationProvider.getApplicationContext(), mockRepository).blockingAwait();
        verify(mockRepository, atLeastOnce()).reset(null);
    }

    @Test
    public void resetCallsResetRepositoryWithNoRevengeUnits() {
        Context context = ApplicationProvider.getApplicationContext();
        for (int numberOfUnits = 0; numberOfUnits < TESTING_DEPTH; numberOfUnits++) {
            when(mockRepository.getUnits()).thenReturn(Single.just(getUnitMockList(numberOfUnits)));
            ResetRepositoryUseCase.reset(context, mockRepository).blockingAwait();
            verify(mockRepository, atLeast(numberOfUnits + 1)).reset(null);
        }
    }

    @Test
    public void resetCallsResetRepositoryWithKeptRevengeUnit() {
        Context context = ApplicationProvider.getApplicationContext();
        UnitEntity revengeUnit = mock(UnitEntity.class);
        when(revengeUnit.getAbility()).thenReturn(Ability.REVENGE);
        for (int numberOfUnits = 0; numberOfUnits < TESTING_DEPTH; numberOfUnits++) {
            List<UnitEntity> testList = getUnitMockList(numberOfUnits);
            testList.add(revengeUnit);
            when(mockRepository.getUnits()).thenReturn(Single.just(testList));
            ResetRepositoryUseCase.reset(context, mockRepository, revengeUnit).blockingAwait();
            verify(mockRepository, atLeast(numberOfUnits + 1)).reset(revengeUnit);
        }
    }
}