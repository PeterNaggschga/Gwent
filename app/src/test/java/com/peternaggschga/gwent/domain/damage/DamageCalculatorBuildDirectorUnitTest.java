package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DamageCalculatorBuildDirectorUnitTest {
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
    public void getCalculatorNoHornEmptyListReturnsWeatherDamageCalculator() {
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, Collections.emptyList()).getClass()).isEqualTo(WeatherDamageCalculator.class);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, Collections.emptyList()).getClass()).isEqualTo(WeatherDamageCalculator.class);
    }

    @Test
    public void getCalculatorWithHornReturnsHornDamageCalculator() {
        List<UnitEntity> list = getMockUnitList(true, true, false);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list).getClass()).isEqualTo(HornDamageCalculatorDecorator.class);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list).getClass()).isEqualTo(HornDamageCalculatorDecorator.class);
    }

    @Test
    public void getCalculatorWithHornUnitReturnsHornDamageCalculator() {
        List<UnitEntity> list = getMockUnitList(true, true, true);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list).getClass()).isEqualTo(HornDamageCalculatorDecorator.class);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list).getClass()).isEqualTo(HornDamageCalculatorDecorator.class);
    }

    @Test
    public void getCalculatorWithBindingUnitReturnsBondDamageCalculator() {
        List<UnitEntity> list = getMockUnitList(true, false, false);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list).getClass()).isEqualTo(BondDamageCalculatorDecorator.class);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list).getClass()).isEqualTo(BondDamageCalculatorDecorator.class);
    }

    @Test
    public void getCalculatorWithMoralUnitReturnsMoralDamageCalculator() {
        List<UnitEntity> list = getMockUnitList(true, true, false);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list).getClass()).isEqualTo(MoralDamageCalculatorDecorator.class);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list).getClass()).isEqualTo(MoralDamageCalculatorDecorator.class);
    }
}
