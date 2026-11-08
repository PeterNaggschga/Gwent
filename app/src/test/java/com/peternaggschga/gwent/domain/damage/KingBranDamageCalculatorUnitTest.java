package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class KingBranDamageCalculatorUnitTest {
    @Test
    public void weatherHalvesBaseStrengthAndRoundsDownIncludingOneToZero() {
        DamageCalculator calculator = new KingBranDamageCalculator(true);
        for (int strength = 0; strength <= 20; strength++) {
            assertThat(calculator.calculateDamage(1, strength)).isEqualTo(strength / 2);
        }
        assertThat(calculator.isBuffed(1)).isEqualTo(DamageCalculator.Color.DEBUFFED);
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateDamage(1, -1));
    }

    @Test
    public void noWeatherLeavesStrengthAndColorUnchanged() {
        DamageCalculator calculator = new KingBranDamageCalculator(false);
        for (int strength = 0; strength <= 20; strength++) {
            assertThat(calculator.calculateDamage(1, strength)).isEqualTo(strength);
        }
        assertThat(calculator.isBuffed(1)).isEqualTo(DamageCalculator.Color.DEFAULT);
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateDamage(1, -1));
    }

    @Test
    public void ordinaryWeatherRemainsUnchangedWithoutKingBran() {
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, false, Collections.emptyList())
                .calculateDamage(1, 5)).isEqualTo(1);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, true, Collections.emptyList())
                .calculateDamage(1, 5)).isEqualTo(2);
    }

    @Test
    public void weatherAppliesBeforeBondMoraleAndHorn() {
        UnitEntity first = mock(UnitEntity.class);
        UnitEntity second = mock(UnitEntity.class);
        UnitEntity morale = mock(UnitEntity.class);
        when(first.getId()).thenReturn(1);
        when(second.getId()).thenReturn(2);
        when(morale.getId()).thenReturn(3);
        when(first.getAbility()).thenReturn(Ability.BINDING);
        when(second.getAbility()).thenReturn(Ability.BINDING);
        when(morale.getAbility()).thenReturn(Ability.MORAL_BOOST);
        when(first.getSquad()).thenReturn(1);
        when(second.getSquad()).thenReturn(1);
        DamageCalculator calculator = DamageCalculatorBuildDirector.getCalculator(true, true, true,
                Arrays.asList(first, second, morale));
        // floor(5 / 2) = 2, then bond x2, morale +1, horn x2.
        assertThat(calculator.calculateDamage(1, 5)).isEqualTo(10);
    }

}
