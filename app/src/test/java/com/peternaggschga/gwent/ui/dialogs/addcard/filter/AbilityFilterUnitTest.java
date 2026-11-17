package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.data.Ability.BERSERKER;
import static com.peternaggschga.gwent.data.Ability.BINDING;
import static com.peternaggschga.gwent.data.Ability.HORN;
import static com.peternaggschga.gwent.data.Ability.MARDROEME;
import static com.peternaggschga.gwent.data.Ability.MORAL_BOOST;
import static com.peternaggschga.gwent.data.Ability.NONE;
import static com.peternaggschga.gwent.data.Ability.REVENGE;
import static com.peternaggschga.gwent.data.RowType.MELEE;
import static com.peternaggschga.gwent.data.RowType.RANGE;
import static com.peternaggschga.gwent.data.RowType.SIEGE;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NILFGAARD;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SKELLIGE;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.ui.dialogs.addcard.SquadManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RunWith(Parameterized.class)
public class AbilityFilterUnitTest {
    private final AbilityFilter filter;
    private final RowType row;
    private final Set<Ability> normalAbilities;
    private final Set<Ability> heroAbilities;
    private final Set<Integer> heroDamageValues;
    private final int squadCount;

    public AbilityFilterUnitTest(int faction, RowType row, Set<Ability> normalAbilities,
                                 Set<Ability> heroAbilities, Set<Integer> heroDamageValues,
                                 int squadCount) {
        filter = AbilityFilter.create(faction, row);
        this.row = row;
        this.normalAbilities = normalAbilities;
        this.heroAbilities = heroAbilities;
        this.heroDamageValues = heroDamageValues;
        this.squadCount = squadCount;
    }

    @Parameterized.Parameters(name = "faction={0}, row={1}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {THEME_MONSTER, MELEE, abilities(NONE, HORN), abilities(NONE), damage(0, 7, 8, 10, 15), 0},
                {THEME_MONSTER, RANGE, abilities(NONE, REVENGE), abilities(NONE, MORAL_BOOST), damage(7, 8, 10), 0},
                {THEME_MONSTER, SIEGE, abilities(NONE), abilities(), damage(), 0},
                {THEME_NILFGAARD, MELEE, abilities(NONE, HORN, BINDING), abilities(NONE), damage(0, 7, 10, 15), 3},
                {THEME_NILFGAARD, RANGE, abilities(NONE, REVENGE), abilities(NONE), damage(7, 10), 0},
                {THEME_NILFGAARD, SIEGE, abilities(NONE), abilities(NONE), damage(10), 0},
                {THEME_NORTHERN_KINGDOMS, MELEE, abilities(NONE, HORN, BINDING, MORAL_BOOST),
                        abilities(NONE), damage(0, 7, 10, 15), 2},
                {THEME_NORTHERN_KINGDOMS, RANGE, abilities(NONE, REVENGE, BINDING, MORAL_BOOST),
                        abilities(NONE), damage(7, 10), 1},
                {THEME_NORTHERN_KINGDOMS, SIEGE, abilities(NONE, BINDING, MORAL_BOOST),
                        abilities(), damage(), 1},
                {THEME_SCOIATAEL, MELEE, abilities(NONE, HORN), abilities(NONE, MORAL_BOOST), damage(0, 7, 10, 15), 0},
                {THEME_SCOIATAEL, RANGE, abilities(NONE, REVENGE, MORAL_BOOST), abilities(NONE), damage(7, 10), 0},
                {THEME_SCOIATAEL, SIEGE, abilities(NONE), abilities(), damage(), 0},
                {THEME_SKELLIGE, MELEE, abilities(NONE, HORN, MORAL_BOOST, BERSERKER, BINDING, REVENGE),
                        abilities(NONE), damage(0, 7, 10, 15), 2},
                {THEME_SKELLIGE, RANGE, abilities(NONE, REVENGE, MORAL_BOOST, BERSERKER),
                        abilities(NONE, MARDROEME), damage(7, 8, 10), 0},
                {THEME_SKELLIGE, SIEGE, abilities(NONE, BINDING, HORN), abilities(), damage(), 1}
        });
    }

    private static Set<Ability> abilities(Ability... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    private static Set<Integer> damage(Integer... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    @Test
    public void normalAbilitiesMatchRules() {
        assertThat(filter.getAbilities(false)).containsExactlyElementsIn(normalAbilities);
    }

    @Test
    public void heroAbilitiesMatchRules() {
        assertThat(filter.getAbilities(true)).containsExactlyElementsIn(heroAbilities);
    }

    @Test
    public void heroDamageValuesMatchRules() {
        assertThat(filter.getHeroDamageValues()).containsExactlyElementsIn(heroDamageValues);
        assertThat(Arrays.asList(UnitEntity.EPIC_DAMAGE_VALUES))
                .containsAtLeastElementsIn(filter.getHeroDamageValues());
    }

    @Test
    public void heroesAvailableMatchesExpectedDamageValues() {
        assertThat(filter.heroesAvailable()).isEqualTo(!heroDamageValues.isEmpty());
    }

    @Test
    public void normalUnitsAlwaysOfferNone() {
        assertThat(filter.getAbilities(false)).contains(NONE);
    }

    @Test
    public void heroAbilitySelectionMatchesHeroAvailability() {
        if (heroDamageValues.isEmpty()) {
            assertThat(filter.getAbilities(true)).isEmpty();
        } else {
            assertThat(filter.getAbilities(true)).contains(NONE);
        }
    }

    @Test
    public void squadCountMatchesRules() {
        assertThat(filter.getSquadCount()).isEqualTo(squadCount);
        assertThat(filter.getSquadCount()).isAtLeast(0);
        assertThat(filter.getSquadCount()).isAtMost(SquadManager.MAX_NR_SQUADS);
    }

    @Test
    public void bindingAvailabilityMatchesSquadAvailability() {
        assertThat(filter.getAbilities(false).contains(BINDING))
                .isEqualTo(filter.getSquadCount() > 0);
    }

    @Test
    public void factionPreservesNeutralOptions() {
        AbilityFilter neutral = new NeutralAbilityFilter(row);
        assertThat(filter.getAbilities(false)).containsAtLeastElementsIn(neutral.getAbilities(false));
        assertThat(filter.getAbilities(true)).containsAtLeastElementsIn(neutral.getAbilities(true));
        assertThat(filter.getHeroDamageValues()).containsAtLeastElementsIn(neutral.getHeroDamageValues());
    }

    @Test
    public void returnedSetsDoNotLeakChangesToLaterCalls() {
        Set<Ability> expectedNormal = new HashSet<>(filter.getAbilities(false));
        Set<Ability> expectedHero = new HashSet<>(filter.getAbilities(true));
        Set<Integer> expectedDamage = new HashSet<>(filter.getHeroDamageValues());
        filter.getAbilities(false).clear();
        filter.getAbilities(true).clear();
        filter.getHeroDamageValues().clear();
        assertThat(filter.getAbilities(false)).containsExactlyElementsIn(expectedNormal);
        assertThat(filter.getAbilities(true)).containsExactlyElementsIn(expectedHero);
        assertThat(filter.getHeroDamageValues()).containsExactlyElementsIn(expectedDamage);
    }
}
