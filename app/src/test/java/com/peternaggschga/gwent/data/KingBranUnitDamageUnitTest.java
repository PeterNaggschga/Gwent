package com.peternaggschga.gwent.data;

import static org.junit.Assert.assertEquals;

import com.peternaggschga.gwent.domain.damage.DamageCalculator;
import com.peternaggschga.gwent.domain.damage.DamageCalculatorBuildDirector;

import org.junit.Test;

import java.util.Collections;

public class KingBranUnitDamageUnitTest {
    @Test
    public void heroesBypassKingBransWeatherPenalty() {
        UnitEntity hero = new UnitEntity(true, 11, Ability.NONE, null, RowType.MELEE);
        DamageCalculator calculator = DamageCalculatorBuildDirector.getCalculator(true, false, true,
                Collections.singletonList(hero));
        assertEquals(11, hero.calculateDamage(calculator));
        assertEquals(DamageCalculator.Color.DEFAULT, hero.isBuffed(calculator));
    }
}
