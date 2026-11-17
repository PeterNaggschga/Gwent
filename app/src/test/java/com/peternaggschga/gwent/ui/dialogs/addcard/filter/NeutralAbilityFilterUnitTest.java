package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.data.Ability.HORN;
import static com.peternaggschga.gwent.data.Ability.NONE;
import static com.peternaggschga.gwent.data.Ability.REVENGE;
import static com.peternaggschga.gwent.data.RowType.MELEE;
import static com.peternaggschga.gwent.data.RowType.RANGE;
import static com.peternaggschga.gwent.data.RowType.SIEGE;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
public class NeutralAbilityFilterUnitTest {
    private final AbilityFilter filter;
    private final Collection<Ability> normalAbilities;
    private final Collection<Integer> heroDamageValues;

    public NeutralAbilityFilterUnitTest(RowType row, Collection<Ability> normalAbilities,
                                        Collection<Integer> heroDamageValues) {
        filter = new NeutralAbilityFilter(row);
        this.normalAbilities = normalAbilities;
        this.heroDamageValues = heroDamageValues;
    }

    @Parameterized.Parameters(name = "row={0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {MELEE, Arrays.asList(NONE, HORN), Arrays.asList(0, 7, 15)},
                {RANGE, Arrays.asList(NONE, REVENGE), Collections.singletonList(7)},
                {SIEGE, Collections.singletonList(NONE), Collections.emptyList()}
        });
    }

    @Test
    public void normalAbilitiesIncludeNoneAndRowAbility() {
        assertThat(filter.getAbilities(false)).containsExactlyElementsIn(normalAbilities);
    }

    @Test
    public void heroesOnlyHaveNoneWhenAvailable() {
        if (heroDamageValues.isEmpty()) {
            assertThat(filter.getAbilities(true)).isEmpty();
        } else {
            assertThat(filter.getAbilities(true)).containsExactly(NONE);
        }
    }

    @Test
    public void heroDamageValuesMatchRow() {
        assertThat(filter.getHeroDamageValues()).containsExactlyElementsIn(heroDamageValues);
    }

    @Test
    public void heroesAvailableMatchesExpectedDamageValues() {
        assertThat(filter.heroesAvailable()).isEqualTo(!heroDamageValues.isEmpty());
    }

    @Test
    public void neutralCardsHaveNoSelectableSquads() {
        assertThat(filter.getSquadCount()).isEqualTo(0);
    }

    @Test
    public void heroAvailabilityDoesNotDependOnReturnedSetMutations() {
        filter.getHeroDamageValues().clear();
        assertThat(filter.heroesAvailable()).isEqualTo(!heroDamageValues.isEmpty());
    }
}
