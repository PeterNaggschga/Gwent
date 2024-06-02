package com.peternaggschga.gwent.domain.cases;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.rxjava3.core.Single;

@RunWith(AndroidJUnit4.class)
public class RemoveUnitsUseCaseUnitTest {
    private static final int TESTING_DEPTH = 50;
    private UnitRepository mockRepository;
    private UnitEntity mockEntity;

    @Before
    public void initMocks() {
        mockRepository = mock(UnitRepository.class);
        mockEntity = mock(UnitEntity.class);
        when(mockEntity.getAbility()).thenReturn(Ability.NONE);
    }

    public Collection<UnitEntity> getUnitMockList(int numberOfUnits) {
        Collection<UnitEntity> result = new ArrayList<>(numberOfUnits);
        for (int i = 0; i < numberOfUnits; i++) {
            result.add(mockEntity);
        }
        return result;
    }

    @Test
    public void removeCollectionThrowsNullPointerExceptionWhenNullIsInCollection() {
        for (int numberOfUnits = 0; numberOfUnits < TESTING_DEPTH; numberOfUnits++) {
            Collection<UnitEntity> units = getUnitMockList(numberOfUnits);
            units.add(null);
            try {
                RemoveUnitsUseCase.remove(ApplicationProvider.getApplicationContext(), mockRepository, units);
                fail();
            } catch (NullPointerException ignored) {
                verify(mockRepository, never()).delete(any());
            }
        }
    }

    @Test
    public void removeCollectionCallsDeleteOnRepositoryWithoutRevengeUnits() {
        for (int numberOfUnits = 0; numberOfUnits < TESTING_DEPTH; numberOfUnits++) {
            Collection<UnitEntity> units = getUnitMockList(numberOfUnits);
            RemoveUnitsUseCase.remove(ApplicationProvider.getApplicationContext(), mockRepository, units);
            verify(mockRepository, atLeastOnce()).delete(units);
        }
    }

    @Test
    public void removeCallsDeleteOnRepositoryIfNotRevengeUnit() {
        UnitEntity mockUnit = mock(UnitEntity.class);
        when(mockUnit.getAbility()).thenReturn(Ability.NONE);
        when(mockRepository.getUnit(anyInt())).thenReturn(Single.just(mockUnit));
        RemoveUnitsUseCase.remove(ApplicationProvider.getApplicationContext(), mockRepository, 0);
    }
}
