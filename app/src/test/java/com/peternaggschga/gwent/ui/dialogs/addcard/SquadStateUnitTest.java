package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.ui.dialogs.addcard.SquadManager.MAX_NR_SQUADS;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.peternaggschga.gwent.data.UnitEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class SquadStateUnitTest {
    private static final int TESTING_DEPTH = 50;

    @Test
    public void getStateAssertsSquadNumber() {
        try {
            SquadState.getState(0, Collections.emptyList());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            SquadState.getState(MAX_NR_SQUADS + 1, Collections.emptyList());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getStateCountsSquadMembers() {
        List<UnitEntity> testList = new ArrayList<>(TESTING_DEPTH);
        for (int i = 0; i < TESTING_DEPTH; i++) {
            assertThat(SquadState.getState(1, testList).getSquadMembers()).isEqualTo(i / 2);
            UnitEntity mockUnit = mock(UnitEntity.class);
            when(mockUnit.getSquad()).thenReturn(((i % 2) == 0) ? MAX_NR_SQUADS : 1);
            testList.add(mockUnit);
        }
        assertThat(SquadState.getState(1, testList).getSquadMembers()).isEqualTo(TESTING_DEPTH / 2);
    }

    @Test
    public void getStateSetsBaseDamageOnEmptySquad() {
        assertThat(SquadState.getState(1, Collections.emptyList()).getMemberBaseDamage()).isEqualTo(5);
    }

    @Test
    public void getStateSetsMemberBaseDamage() {
        UnitEntity mockUnit = mock(UnitEntity.class);
        when(mockUnit.getSquad()).thenReturn(1);
        for (int damage = 0; damage < UnitEntity.NON_EPIC_DAMAGE_VALUES_UPPER_BOUND; damage++) {
            when(mockUnit.getDamage()).thenReturn(damage);
            assertThat(SquadState.getState(1, Collections.singletonList(mockUnit)).getMemberBaseDamage())
                    .isEqualTo(damage);
        }
    }
}
