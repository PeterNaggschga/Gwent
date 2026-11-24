package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.ui.dialogs.addcard.SquadManager.MAX_NR_SQUADS;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;

@RunWith(JUnit4.class)
public class SquadManagerUnitTest {
    @Test
    public void emptySquadDoesNotChangeDamage() {
        DamageValuePicker picker = mock(DamageValuePicker.class);
        SquadManager manager = new SquadManager(Collections.emptyList());
        assertThat(manager.hasNonEmptySquad()).isFalse();
        manager.setSquadDamage(1, picker);
        verify(picker, never()).setValue(anyInt());
    }

    @Test
    public void unitsWithoutTightBondDoNotFormASquad() {
        UnitEntity unit = mock(UnitEntity.class);
        when(unit.getAbility()).thenReturn(Ability.NONE);
        assertThat(new SquadManager(Collections.singletonList(unit)).hasNonEmptySquad()).isFalse();
    }

    @Test
    public void occupiedSquadAppliesItsBaseDamage() {
        UnitEntity unit = mock(UnitEntity.class);
        when(unit.getAbility()).thenReturn(Ability.BINDING);
        when(unit.getSquad()).thenReturn(MAX_NR_SQUADS);
        when(unit.getDamage()).thenReturn(4);
        DamageValuePicker picker = mock(DamageValuePicker.class);
        SquadManager manager = new SquadManager(Collections.singletonList(unit));
        assertThat(manager.hasNonEmptySquad()).isTrue();
        manager.setSquadDamage(MAX_NR_SQUADS, picker);
        verify(picker).setValue(4);
        assertThat(manager.getFirstSquadWithMembers(MAX_NR_SQUADS)).isEqualTo(MAX_NR_SQUADS);
        assertThat(manager.getFirstSquadWithMembers(1)).isEqualTo(1);
    }

    @Test
    public void rejectsSquadOutsideSelectableRange() {
        SquadManager manager = new SquadManager(Collections.emptyList());
        DamageValuePicker picker = mock(DamageValuePicker.class);
        assertThrows(IllegalArgumentException.class, () -> manager.setSquadDamage(0, picker));
        assertThrows(IllegalArgumentException.class, () -> manager.setSquadDamage(MAX_NR_SQUADS + 1, picker));
    }
}
