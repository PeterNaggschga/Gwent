package com.peternaggschga.gwent.domain.cases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.peternaggschga.gwent.data.UnitRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RevengeDialogFactoryUnitTest {
    private static final int TESTING_DEPTH = 50;

    @Test
    public void insertAvengersInsertsUnits() {
        UnitRepository repository = mock(UnitRepository.class);
        for (int numberOfAvengers = 0; numberOfAvengers < TESTING_DEPTH; numberOfAvengers++) {
            RevengeDialogFactory.insertAvengers(repository, numberOfAvengers);
            verify(repository, atLeastOnce()).insertUnit(anyBoolean(), anyInt(), any(), any(), any(), eq(numberOfAvengers));
        }
    }
}