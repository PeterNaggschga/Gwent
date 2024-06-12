package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.BUFFED;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.DEBUFFED;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.DEFAULT;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;

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
public class DamageCalculatorIntegrationTest {
    private static final int UNIT_NUMBER = 5;
    private static final int TESTING_DAMAGE = 5;

    public List<UnitEntity> getUnitEntityList(@NonNull Ability ability, boolean horn) {
        List<UnitEntity> list = new ArrayList<>();
        for (int id = 0; id < UNIT_NUMBER; id++) {
            UnitEntity entity = Mockito.mock(UnitEntity.class);
            when(entity.getAbility()).thenReturn(ability);
            when(entity.getSquad()).thenReturn(ability == Ability.BINDING ? 0 : null);
            when(entity.getId()).thenReturn(id);
            list.add(entity);
        }
        if (horn) {
            UnitEntity entity = Mockito.mock(UnitEntity.class);
            when(entity.getAbility()).thenReturn(Ability.HORN);
            when(entity.getId()).thenReturn(UNIT_NUMBER);
            list.add(entity);
        }
        return list;
    }

    @Test
    public void calculateDamageNoAbilities() {
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, Collections.emptyList())
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, Collections.emptyList())
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, Collections.emptyList())
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(1);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, Collections.emptyList())
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(2);
    }

    @Test
    public void isBuffedNoAbilities() {
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, Collections.emptyList())
                .isBuffed(0)).isEqualTo(DEFAULT);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, Collections.emptyList())
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, Collections.emptyList())
                .isBuffed(0)).isEqualTo(DEBUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, Collections.emptyList())
                .isBuffed(0)).isEqualTo(BUFFED);
    }

    @Test
    public void calculateDamageBindingAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.BINDING, false);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * UNIT_NUMBER);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * UNIT_NUMBER * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(UNIT_NUMBER);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(UNIT_NUMBER * 2);
    }

    @Test
    public void isBuffedBindingAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.BINDING, false);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
    }

    @Test
    public void calculateDamageMoralAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, false);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE + UNIT_NUMBER);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo((TESTING_DAMAGE + UNIT_NUMBER) * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(1 + UNIT_NUMBER);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo((1 + UNIT_NUMBER) * 2);
    }

    @Test
    public void isBuffedMoralAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, false);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
    }

    @Test
    public void calculateDamageMoralAbilityExcludesMoralUnit() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, false);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE + UNIT_NUMBER - 1);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((TESTING_DAMAGE + UNIT_NUMBER - 1) * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(1 + UNIT_NUMBER - 1);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((1 + UNIT_NUMBER - 1) * 2);
    }

    @Test
    public void isBuffedMoralAbilityExcludesMoralUnit() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, false);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
    }

    @Test
    public void calculateDamageBindingMoralAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, false);
        list.addAll(getUnitEntityList(Ability.BINDING, false));

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * UNIT_NUMBER + UNIT_NUMBER - 1);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((TESTING_DAMAGE * UNIT_NUMBER + UNIT_NUMBER - 1) * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(UNIT_NUMBER + UNIT_NUMBER - 1);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((UNIT_NUMBER + UNIT_NUMBER - 1) * 2);
    }

    @Test
    public void isBuffedBindingMoralAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, false);
        list.addAll(getUnitEntityList(Ability.BINDING, false));

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
    }

    @Test
    public void calculateDamageHornAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.NONE, true);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(2);
    }

    @Test
    public void isBuffedHornAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.NONE, true);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
    }

    @Test
    public void calculateDamageHornAbilityExcludesHornUnit() {
        List<UnitEntity> list = getUnitEntityList(Ability.NONE, true);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(1);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(2);
    }

    @Test
    public void isBuffedHornAbilityExcludesHornUnit() {
        List<UnitEntity> list = getUnitEntityList(Ability.NONE, true);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(DEFAULT);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(DEBUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
    }

    @Test
    public void calculateDamageHornAbilityMultipleUnitsBuffAnother() {
        List<UnitEntity> list = getUnitEntityList(Ability.NONE, true);
        list.addAll(getUnitEntityList(Ability.NONE, true));

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(UNIT_NUMBER, TESTING_DAMAGE)).isEqualTo(2);
    }

    @Test
    public void isBuffedHornAbilityMultipleUnitsBuffAnother() {
        List<UnitEntity> list = getUnitEntityList(Ability.NONE, true);
        list.addAll(getUnitEntityList(Ability.NONE, true));

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .isBuffed(UNIT_NUMBER)).isEqualTo(BUFFED);
    }

    @Test
    public void calculateDamageHornBindingAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.BINDING, true);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * UNIT_NUMBER * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * UNIT_NUMBER * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(UNIT_NUMBER * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo(UNIT_NUMBER * 2);
    }

    @Test
    public void calculateDamageHornMoralAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, true);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((TESTING_DAMAGE + UNIT_NUMBER - 1) * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((TESTING_DAMAGE + UNIT_NUMBER - 1) * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((1 + UNIT_NUMBER - 1) * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((1 + UNIT_NUMBER - 1) * 2);
    }

    @Test
    public void isBuffedHornMoralAbility() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, true);

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
    }

    @Test
    public void calculateDamageAllAbilities() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, true);
        list.addAll(getUnitEntityList(Ability.BINDING, false));

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((UNIT_NUMBER * TESTING_DAMAGE + UNIT_NUMBER - 1) * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((UNIT_NUMBER * TESTING_DAMAGE + UNIT_NUMBER - 1) * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((UNIT_NUMBER + UNIT_NUMBER - 1) * 2);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .calculateDamage(0, TESTING_DAMAGE)).isEqualTo((UNIT_NUMBER + UNIT_NUMBER - 1) * 2);
    }

    @Test
    public void isBuffedAllAbilities() {
        List<UnitEntity> list = getUnitEntityList(Ability.MORAL_BOOST, true);
        list.addAll(getUnitEntityList(Ability.BINDING, false));

        assertThat(DamageCalculatorBuildDirector.getCalculator(false, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(false, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, false, list)
                .isBuffed(0)).isEqualTo(BUFFED);
        assertThat(DamageCalculatorBuildDirector.getCalculator(true, true, list)
                .isBuffed(0)).isEqualTo(BUFFED);
    }
}
