package com.peternaggschga.gwent.domain.cases;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;
import com.peternaggschga.gwent.domain.damage.DamageCalculatorBuildDirector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

@RunWith(MockitoJUnitRunner.class)
public class DamageCalculatorUseCaseUnitTest {
    private static final UnitRepository REPOSITORY = mock(UnitRepository.class);

    public List<UnitEntity> getMockUnitList(boolean binding, boolean moral, boolean horn) {
        List<UnitEntity> result = new ArrayList<>();
        if (binding) {
            UnitEntity bindingUnit = Mockito.mock(UnitEntity.class);
            when(bindingUnit.getAbility()).thenReturn(Ability.BINDING);
            result.add(bindingUnit);
        }
        if (moral) {
            UnitEntity moralUnit = Mockito.mock(UnitEntity.class);
            when(moralUnit.getAbility()).thenReturn(Ability.MORAL_BOOST);
            result.add(moralUnit);
        }
        if (horn) {
            UnitEntity hornUnity = Mockito.mock(UnitEntity.class);
            when(hornUnity.getAbility()).thenReturn(Ability.HORN);
            result.add(hornUnity);
        }
        return result;
    }

    @Test
    public void getDamageCalculatorReturnsCorrectCalculator() {
        for (RowType row : RowType.values()) {
            for (boolean weather : new boolean[]{false, true}) {
                for (boolean horn : new boolean[]{false, true}) {
                    testRowWeatherHornCombination(row, weather, horn);
                }
            }
        }
    }

    private void testRowWeatherHornCombination(@NonNull RowType row, boolean weather, boolean horn) {
        when(REPOSITORY.isWeather(row)).thenReturn(Single.just(weather));
        when(REPOSITORY.isHorn(row)).thenReturn(Single.just(horn));
        for (boolean bindingUnit : new boolean[]{false, true}) {
            for (boolean moralUnit : new boolean[]{false, true}) {
                for (boolean hornUnit : new boolean[]{false, true}) {
                    List<UnitEntity> testList = getMockUnitList(bindingUnit, moralUnit, hornUnit);
                    when(REPOSITORY.getUnits(row)).thenReturn(Single.just(testList));
                    DamageCalculatorUseCase.getDamageCalculator(REPOSITORY, row)
                            .test()
                            .assertValue(calculator -> {
                                DamageCalculator calculator1 = DamageCalculatorBuildDirector.getCalculator(weather, horn, testList);
                                boolean equal = calculator.getClass() == calculator1.getClass();
                                for (UnitEntity unit : testList) {
                                    equal &= unit.calculateDamage(calculator) == unit.calculateDamage(calculator1);
                                }
                                return equal;
                            })
                            .dispose();
                }
            }
        }
    }
}
