package com.peternaggschga.gwent.domain.cases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.main.RowUiState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

@RunWith(MockitoJUnitRunner.class)
public class RowStateUseCaseUnitTest {
    private static final int TESTING_DAMAGE = 5;
    private static final int TESTING_DEPTH = 50;

    @Test
    public void getRowStateReturnsCorrectState() {
        UnitRepository repository = mock(UnitRepository.class);
        UnitEntity entity = mock(UnitEntity.class);
        when(entity.getAbility()).thenReturn(Ability.NONE);
        when(entity.calculateDamage(any())).thenReturn(TESTING_DAMAGE);
        for (RowType row : RowType.values()) {
            for (boolean weather : new boolean[]{false, true}) {
                for (boolean horn : new boolean[]{false, true}) {
                    when(repository.isWeather(row)).thenReturn(Single.just(weather));
                    when(repository.isHorn(row)).thenReturn(Single.just(horn));
                    List<UnitEntity> entities = new ArrayList<>(TESTING_DEPTH);
                    for (int unitNumber = 0; unitNumber < TESTING_DEPTH; unitNumber++) {
                        when(repository.getUnits(row)).thenReturn(Single.just(entities));
                        RowStateUseCase.getRowState(repository, row)
                                .test()
                                .assertValue(new RowUiState(unitNumber * TESTING_DAMAGE, weather, horn, unitNumber))
                                .dispose();
                        entities.add(entity);
                    }
                }
            }
        }
    }
}
