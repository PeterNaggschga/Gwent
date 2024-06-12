package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.ui.dialogs.addcard.SquadManager.MAX_NR_SQUADS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class SquadManagerUnitTest {
    @Test
    public void getFirstSquadWithMembersReturns1ByDefault() {
        assertThat(new SquadManager(Collections.emptyList()).getFirstSquadWithMembers()).isEqualTo(1);
    }

    @Test
    public void getFirstSquadWithMembersReturnsLowestSquad() {
        List<UnitEntity> units = new ArrayList<>(MAX_NR_SQUADS);
        for (int squad = MAX_NR_SQUADS; squad > 0; squad--) {
            UnitEntity mockUnit = mock(UnitEntity.class);
            when(mockUnit.getAbility()).thenReturn(Ability.BINDING);
            when(mockUnit.getSquad()).thenReturn(squad);
            units.add(mockUnit);
            assertThat(new SquadManager(units).getFirstSquadWithMembers()).isEqualTo(squad);
        }
    }
}
