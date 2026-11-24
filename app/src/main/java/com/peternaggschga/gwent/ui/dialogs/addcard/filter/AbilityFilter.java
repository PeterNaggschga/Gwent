package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NILFGAARD;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SKELLIGE;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.ui.dialogs.addcard.SquadManager;

import java.util.Objects;
import java.util.Set;

/**
 * Provides selectable abilities, hero damage values, and the number of distinct Tight Bond squads for a row.
 */
public interface AbilityFilter {
    /**
     * Creates a neutral baseline wrapped in exactly one faction decorator for the same row.
     * Concrete filters are package-private so callers use this construction entry point.
     *
     * @param faction Faction theme constant from FactionSwitchListener.
     * @param row     Row shared by the neutral filter and faction decorator.
     * @return The filter structure, without evaluating any card rules.
     * @throws IllegalArgumentException If the faction is unknown.
     * @throws NullPointerException     If the row is null.
     */
    @NonNull
    static AbilityFilter create(int faction, @NonNull RowType row) {
        Objects.requireNonNull(row, "Row must not be null.");
        switch (faction) {
            case THEME_MONSTER:
                return new MonsterFactionAbilityFilter(row);
            case THEME_NILFGAARD:
                return new NilfgaardFactionAbilityFilter(row);
            case THEME_NORTHERN_KINGDOMS:
                return new NorthernKingdomsFactionAbilityFilter(row);
            case THEME_SCOIATAEL:
                return new ScoiataelFactionAbilityFilter(row);
            case THEME_SKELLIGE:
                return new SkelligeFactionAbilityFilter(row);
            default:
                throw new IllegalArgumentException("Unknown faction: " + faction + ".");
        }
    }

    /**
     * Returns the abilities available to heroes or normal units.
     * Includes Ability.NONE when a unit without a special ability is selectable.
     * The hero ability set is empty when heroes are unavailable.
     * Use heroesAvailable() to determine whether hero selection can be offered.
     *
     * @param hero Whether the unit is a hero.
     * @return The selectable abilities.
     */
    @NonNull
    Set<Ability> getAbilities(boolean hero);

    /**
     * Returns the selectable hero damage values for this row.
     * An empty set means that no heroes are available.
     *
     * @return The distinct selectable hero damage values.
     */
    @NonNull
    Set<Integer> getHeroDamageValues();

    /**
     * Returns the number of selectable Tight Bond squads, or zero if none are available.
     *
     * @return The number of distinct squads, not the number of cards in a squad.
     */
    @IntRange(from = 0, to = SquadManager.MAX_NR_SQUADS)
    int getSquadCount();

    /**
     * Returns whether this faction and row have any selectable heroes.
     * A hero with zero damage still counts as available.
     *
     * @return True if getHeroDamageValues() is not empty, otherwise false.
     */
    default boolean heroesAvailable() {
        return !getHeroDamageValues().isEmpty();
    }
}
