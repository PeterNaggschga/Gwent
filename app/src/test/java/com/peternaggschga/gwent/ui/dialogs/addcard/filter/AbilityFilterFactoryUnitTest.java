package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NILFGAARD;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SKELLIGE;
import static org.junit.Assert.assertThrows;

import com.peternaggschga.gwent.data.RowType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AbilityFilterFactoryUnitTest {
    @Test
    public void createsFactionFilterOverNeutralBaseForEveryRow() {
        int[] factions = {THEME_MONSTER, THEME_NILFGAARD, THEME_NORTHERN_KINGDOMS,
                THEME_SCOIATAEL, THEME_SKELLIGE};
        Class<?>[] filters = {MonsterFactionAbilityFilter.class,
                NilfgaardFactionAbilityFilter.class, NorthernKingdomsFactionAbilityFilter.class,
                ScoiataelFactionAbilityFilter.class, SkelligeFactionAbilityFilter.class};

        for (RowType row : RowType.values()) {
            for (int i = 0; i < factions.length; i++) {
                AbilityFilter filter = AbilityFilter.create(factions[i], row);
                assertThat(filter).isInstanceOf(filters[i]);
                FactionAbilityFilter factionFilter = (FactionAbilityFilter) filter;
                assertThat(factionFilter.base).isInstanceOf(NeutralAbilityFilter.class);
                assertThat(factionFilter.row).isEqualTo(row);
            }
        }
    }

    @Test
    public void rejectsUnknownFaction() {
        for (RowType row : RowType.values()) {
            for (int faction : new int[]{Integer.MIN_VALUE, -1, 5, Integer.MAX_VALUE}) {
                assertThrows(IllegalArgumentException.class,
                        () -> AbilityFilter.create(faction, row));
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void rejectsNullRow() {
        for (int faction : new int[]{THEME_MONSTER, THEME_NILFGAARD, THEME_NORTHERN_KINGDOMS,
                THEME_SCOIATAEL, THEME_SKELLIGE}) {
            assertThrows(NullPointerException.class,
                    () -> AbilityFilter.create(faction, null));
        }
    }
}
