package com.peternaggschga.gwent.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@RunWith(MockitoJUnitRunner.class)
public class BurnUnitsUseCaseUnitTest {
    private static final int TESTING_DEPTH = 20;
    private UnitRepository repository;
    private BurnUnitsUseCase testUseCase;

    @NonNull
    private List<UnitEntity> getTestList(int listSize, int maxDamage, int maxDamageUnits) {
        List<UnitEntity> result = new ArrayList<>(listSize);
        Random rand = new Random();
        for (int i = 0; i < listSize - maxDamageUnits; i++) {
            UnitEntity entity = mock(UnitEntity.class);
            when(entity.calculateDamage(any())).thenReturn(rand.nextInt(maxDamage));
            when(entity.getRow()).thenReturn(RowType.MELEE);
            result.add(entity);
        }
        for (int i = 0; i < maxDamageUnits; i++) {
            UnitEntity entity = mock(UnitEntity.class);
            when(entity.calculateDamage(any())).thenReturn(maxDamage);
            when(entity.getRow()).thenReturn(RowType.MELEE);
            result.add(entity);
        }
        return result;
    }

    @Before
    public void initUseCase() {
        repository = mock(UnitRepository.class);
        when(repository.isWeather(any())).thenReturn(Single.just(false));
        when(repository.isHorn(any())).thenReturn(Single.just(false));
        when(repository.getUnits(any())).thenReturn(Single.just(Collections.emptyList()));
        when(repository.delete(any())).thenReturn(Completable.complete());
        testUseCase = new BurnUnitsUseCase(repository);
    }

    @Test
    public void getBurnUnitsReturnsSameListAfterMultipleCalls() {
        when(repository.getUnits()).thenReturn(Single.just(Collections.emptyList()));
        List<UnitEntity> firstResult = testUseCase.getBurnUnits().blockingGet();
        testUseCase.getBurnUnits()
                .test()
                .assertValue(unitEntities -> firstResult == unitEntities)
                .dispose();
    }

    @Test
    public void getBurnUnitsReturnsEmptyListForEmptyBoard() {
        when(repository.getUnits()).thenReturn(Single.just(Collections.emptyList()));
        testUseCase.getBurnUnits()
                .test()
                .assertValue(List::isEmpty)
                .dispose();
    }

    @Test
    public void getBurnUnitsReturnsZeroDamageUnits() {
        for (int listSize = 1; listSize <= TESTING_DEPTH; listSize++) {
            initUseCase();
            List<UnitEntity> testList = getTestList(listSize, 0, listSize);
            when(repository.getUnits()).thenReturn(Single.just(testList));
            testUseCase.getBurnUnits()
                    .test()
                    .assertValue(unitEntities -> unitEntities.size() == testList.size())
                    .assertValue(unitEntities ->
                            unitEntities.stream().allMatch(unitEntity ->
                                    unitEntity.calculateDamage(mock(DamageCalculator.class)) == 0))
                    .dispose();
        }
    }

    @Test
    public void getBurnUnitsReturnsMaxDamageUnit() {
        for (int listSize = 1; listSize <= TESTING_DEPTH; listSize++) {
            for (int maxDamage = 1; maxDamage <= TESTING_DEPTH; maxDamage++) {
                initUseCase();
                List<UnitEntity> testList = getTestList(listSize, maxDamage, 1);
                when(repository.getUnits()).thenReturn(Single.just(testList));
                int finalMaxDamage = maxDamage;
                testUseCase.getBurnUnits()
                        .test()
                        .assertValue(unitEntities -> unitEntities.size() == 1)
                        .assertValue(unitEntities ->
                                unitEntities.stream().allMatch(unitEntity ->
                                        unitEntity.calculateDamage(mock(DamageCalculator.class)) == finalMaxDamage))
                        .dispose();
            }
        }
    }

    @Test
    public void getBurnUnitsReturnsMultipleMaxDamageUnits() {
        for (int listSize = 1; listSize <= TESTING_DEPTH; listSize++) {
            for (int maxUnitNumber = 1; maxUnitNumber <= listSize; maxUnitNumber++) {
                initUseCase();
                List<UnitEntity> testList = getTestList(listSize, TESTING_DEPTH, maxUnitNumber);
                when(repository.getUnits()).thenReturn(Single.just(testList));
                int finalMaxUnitNumber = maxUnitNumber;
                testUseCase.getBurnUnits()
                        .test()
                        .assertValue(unitEntities -> unitEntities.size() == finalMaxUnitNumber)
                        .assertValue(unitEntities ->
                                unitEntities.stream().allMatch(unitEntity ->
                                        unitEntity.calculateDamage(mock(DamageCalculator.class)) == TESTING_DEPTH))
                        .dispose();
            }
        }
    }

    @Test
    public void removeBurnUnitsRemovesBurnUnits() {
        Random rand = new Random();
        for (int listSize = 1; listSize <= TESTING_DEPTH; listSize++) {
            for (int maxDamage = 1; maxDamage <= TESTING_DEPTH; maxDamage++) {
                int maxDamageUnits = rand.nextInt(listSize) + 1;
                initUseCase();
                List<UnitEntity> testList = getTestList(listSize, maxDamage, maxDamageUnits);
                when(repository.getUnits()).thenReturn(Single.just(testList));
                List<UnitEntity> burnList = new BurnUnitsUseCase(repository).getBurnUnits().blockingGet();
                testUseCase.removeBurnUnits().blockingAwait();
                verify(repository, atLeastOnce()).delete(burnList);
            }
        }
    }
}
